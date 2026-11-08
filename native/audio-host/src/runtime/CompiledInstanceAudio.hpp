#pragma once
#include "nodes/MultipleGraphNode.hpp"

namespace easyknack {
struct CompiledScalarMemo {
    struct Entry {float a=0,b=0,value=0;double rate=0;bool valid=false;};
    std::array<Entry,256> entries{};
    template<class F> float evaluate(std::size_t slot,float a,float b,double rate,F compute)noexcept{
        auto& e=entries[slot];
        if(!e.valid||e.a!=a||e.b!=b||e.rate!=rate){e.value=compute();e.a=a;e.b=b;e.rate=rate;e.valid=true;}
        return e.value;
    }
};
// Bake-only mono reduction kernel. The graph owns canonical state so switching
// to audio/stereo modulation or inheriting live state needs no state translation.
struct CompiledInstanceAudio {
    std::array<double,256> phases{},increments{};
    std::array<float,256> weights{};
    std::array<const float*,256> tables{};
    std::array<const float*,256> frequencies{};
    std::array<OscillatorNode*,256> oscillators{};
    const MultipleGraphNode::State* plannedState=nullptr;
    int plannedActive=-1;unsigned plannedMask=0;double plannedRate=0;
    void invalidate()noexcept{plannedState=nullptr;}

    template<class Instance> bool process(MultipleGraphNode& owner,ProcessContext& c)noexcept {
        const auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        if(!owner.state||owner.outputs!=1||frames>owner.framesReserved)return false;
        // This narrow mono optimization falls back to the general compiled executor for wider interfaces.
        if(owner.sources.size()>9)return false;
        if(c.channelIndex!=0)return false; // shared cached right output is copied by fallback
        auto& state=*owner.state;
        float requested=c.numericInput(0,float(owner.count))[0];
        int active=std::isfinite(requested)?int(std::clamp(std::floor(requested),0.f,float(owner.maximum))):0;
        // Crossfades and non-scalar modulation use the general kernel.
        for(int i=0;i<owner.maximum;i++)if(state.initialized&&state.gains[i]!=(i<active?1.f:0.f))return false;
        std::array<char,9> rates{};
        for(std::size_t p=0;p<std::min(c.inputCount,rates.size());p++){
            rates[p]=c.sourceRates?c.sourceRates[p]:c.input(p)?'a':'c';
            if(c.input(p)&&(rates[p]!='c'||(c.rightInputs&&c.rightInputs[p]!=c.input(p))))return false;
        }
        unsigned mask=0;for(std::size_t p=0;p<std::min(c.inputCount,rates.size());p++)if(c.input(p))mask|=1u<<p;
        const bool prepared=plannedState==&state&&plannedActive==active&&plannedMask==mask&&plannedRate==c.sampleRate;
        // Validate every instance before advancing any processor state.
        for(int i=0;i<active;i++){
            auto& instance=static_cast<Instance&>(*state.instances[i]);
            if(instance.interpolatingControls()){invalidate();return false;}
            if(!prepared){instance.resolveChannels(c);instance.resolveAutoRate(rates.data(),std::min(c.inputCount,rates.size()));
                if(!instance.reductionReady()){invalidate();return false;}}
            auto& osc=static_cast<OscillatorNode&>(*instance.runtime->graph.nodes[instance.reductionOsc].processor);
            if(osc.sources[0]<0&&osc.currentFrequency!=osc.frequency)return false;
        }
        plannedState=&state;plannedActive=active;plannedMask=mask;plannedRate=c.sampleRate;
        if(!state.initialized){for(int i=0;i<owner.maximum;i++)state.gains[i]=i<active?1.f:0.f;state.initialized=true;}
        float denominator=0;
        bool weighted=false;
        if(active)weighted=static_cast<InstanceCombineNode&>(*state.instances[0]->runtime->graph.nodes[state.instances[0]->runtime->outputs[0]].processor).mode=="weighted";
        for(int i=0;i<active;i++){
            auto& instance=static_cast<Instance&>(*state.instances[i]);
            for(auto* context:instance.contexts)context->count=active;
            for(int input:instance.runtime->inputs){auto& n=instance.runtime->graph.nodes[input];static_cast<BakedInput&>(*n.processor).external=&c;static_cast<BakedInput&>(*n.rightProcessor).external=&c;}
            instance.renderScalars(int(frames));
            auto& osc=static_cast<OscillatorNode&>(*instance.runtime->graph.nodes[instance.reductionOsc].processor);
            auto& combine=static_cast<InstanceCombineNode&>(*instance.runtime->graph.nodes[instance.runtime->outputs[0]].processor);
            float weight=instance.scalar(combine.sources[1],combine.weight);
            weights[i]=std::isfinite(weight)?std::max(0.f,weight):0.f;
            denominator+=weighted?weights[i]:1.f;
            auto kernel=osc.prepareSample(instance.scalar(osc.sources[0],osc.frequency),c.sampleRate);
            if(osc.reset.take())osc.resetPhase();
            phases[i]=osc.phase/OscillatorNode::tau;increments[i]=kernel.increment;
            tables[i]=osc.tables->waves[osc.waveform][kernel.level].data();oscillators[i]=&osc;
            frequencies[i]=instance.audioFrequency();
        }
        bool mean=owner.mean;
        if(owner.explicitCombines)mean=static_cast<InstanceCombineNode&>(*state.instances[0]->runtime->graph.nodes[state.instances[0]->runtime->outputs[0]].processor).mode=="mean";
        // One local accumulation and one output write per sample. No per-partial
        // left/right buffers or samplewise denominator accumulation.
        for(std::size_t sample=0;sample<frames;sample++){
            float sum=0;
            for(int i=0;i<active;i++){
                if(frequencies[i]){
                    const auto kernel=oscillators[i]->prepareSample(frequencies[i][sample],c.sampleRate);
                    increments[i]=kernel.increment;tables[i]=oscillators[i]->tables->waves[oscillators[i]->waveform][kernel.level].data();
                }
                if(weights[i]!=0){const auto offset=oscillators[i]->phaseOffset;sum+=static_cast<float>(OscillatorNode::lookup(tables[i],OscillatorNode::tableSize,offset==0?phases[i]:OscillatorNode::wrap(phases[i]+OscillatorNode::clean(offset)/360.)))*weights[i];}
                phases[i]=OscillatorNode::wrap(phases[i]+increments[i]);
            }
            state.left[0][sample]=(weighted||mean)?(denominator>0?sum/denominator:0):sum;
        }
        for(int i=0;i<active;i++){
            auto& osc=*oscillators[i];osc.phase=phases[i]*OscillatorNode::tau;
            // Retain the exact manual smoothing state for a later fallback.
            if(osc.currentFrequency!=osc.frequency)
                for(std::size_t sample=0;sample<frames;sample++)osc.currentFrequency+=osc.sampleSmoothing*(osc.frequency-osc.currentFrequency);
        }
        std::copy_n(state.left[0].data(),frames,state.right[0].data());
        if(auto* output=c.outputAt(0))std::copy_n(state.left[0].data(),c.bufferSize,output);
        return true;
    }
};
}
