#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
#include "MathNode.hpp"
#include "MapRangeNode.hpp"
#include "MixNode.hpp"
#include "CurveNode.hpp"
#include "ClampNode.hpp"
#include "UnitConverterNode.hpp"
#include "ValueNode.hpp"
#include "ComparatorNode.hpp"
#include "GainNode.hpp"
#include "FilterNode.hpp"
#include "DelayNode.hpp"
#include "OscillatorNode.hpp"
#include "AdditiveOscillatorNode.hpp"
#include "DigitalNoiseNode.hpp"
#include "NoiseNode.hpp"
#include "PanNode.hpp"
#include "ChannelSeparateNode.hpp"
#include "ChannelCombineNode.hpp"
#include "ContextInputNode.hpp"
#include "SlewNode.hpp"
#include "BitcrusherNode.hpp"
#include "SampleHoldNode.hpp"
#include "EdgeDetectorNode.hpp"
#include "ClockNode.hpp"
#include "RateConverterNode.hpp"
#include "TriggerPulseNode.hpp"
#include "ScaleQuantizerNode.hpp"
#include "PitchHoldNode.hpp"
#include "InstanceContextNode.hpp"
#include "MultipleInputNode.hpp"
#include "MultipleSpectrumInputNode.hpp"
#include "MultipleOutputNode.hpp"
#include "InstanceCombineNode.hpp"
#include "SpectrumNode.hpp"
#include "FFTStreamNode.hpp"
#include "SpectralGraphNode.hpp"
#include "IFFTNode.hpp"
namespace easyknack {
struct MultipleGraphNode : NodeProcessor {
    struct Instance : BakedGraphProcessor {
        // Mono upstream controls/modulators, then one oscillator reduced directly
        // into the shared output. Frequency may be audio; weight remains scalar.
        int reductionOsc=-1;
        std::vector<int> scalarPlan;
        void planReduction(){
            auto& g=runtime->graph;
            if(g.hasSampleSteps||!g.deferred.empty()||runtime->outputs.size()!=1)return;
            auto& out=g.nodes[runtime->outputs[0]];
            auto* combine=dynamic_cast<InstanceCombineNode*>(out.processor.get());
            if(!combine||combine->sources.size()!=2||combine->sources[0]<0||combine->sources[0]>=maxNodes)return;
            int osc=combine->sources[0];
            if(!dynamic_cast<OscillatorNode*>(g.nodes[osc].processor.get()))return;
            for(int index:g.order){
                auto& n=g.nodes[index];if(!n.active)continue;
                if(n.voiceCount()!=1)return;
                if(index==osc||index==runtime->outputs[0])continue;
                const auto& id=n.processorId;
                if(id!="multipleinput"&&id!="instancecontext"&&id!="math"&&id!="maprange"&&id!="clamp"&&id!="curve"&&id!="unitconvert"&&id!="value"&&id!="comparator"&&id!="digitalnoise"&&id!="noise")return;
                for(int ref:n.processor->sources)if(ref>=0&&(ref%maxNodes==osc||ref%maxNodes==runtime->outputs[0]))return;
            }
            for(int index:g.order)if(g.nodes[index].active&&index!=osc&&index!=runtime->outputs[0])scalarPlan.push_back(index);
            reductionOsc=osc;
        }
        bool reductionReady()const noexcept{
            if(reductionOsc<0||!sampleKernelsPrepared)return false;
            const auto& g=runtime->graph;
            for(int index:scalarPlan){const auto& n=g.nodes[index];if(n.eventRate||n.stereo)return false;
                for(int ref:n.processor->sources)if(ref>=0&&g.nodes[ref%maxNodes].outputRate(ref/maxNodes)=='e')return false;}
            const auto& osc=g.nodes[reductionOsc];
            if(osc.stereo||osc.controlRate||osc.eventRate)return false;
            if(osc.processor->sources.size()>1&&osc.processor->sources[1]>=0)return false;
            if(osc.processor->sources.size()>2&&osc.processor->sources[2]>=0)return false;
            const auto& out=g.nodes[runtime->outputs[0]];
            for(int ref:osc.processor->sources)if(ref>=0&&g.nodes[ref%maxNodes].outputRate(ref/maxNodes)=='e')return false;
            int weight=out.processor->sources[1];
            if(weight>=0&&g.nodes[weight%maxNodes].outputRate(weight/maxNodes)!='c')return false;
            return !out.stereo&&!out.controlRate&&!out.eventRate;
        }
        float scalar(int ref,float fallback)noexcept{
            if(ref<0)return fallback;
            return runtime->graph.nodes[ref%maxNodes].voice(0).channelCache[0].outputs[ref/maxNodes][0];
        }
        const float* audioFrequency()const noexcept{
            const auto& g=runtime->graph;const auto& osc=*g.nodes[reductionOsc].processor;
            int ref=osc.sources.empty()?-1:osc.sources[0];
            return ref>=0&&g.nodes[ref%maxNodes].outputRate(ref/maxNodes)=='a'
                ?g.nodes[ref%maxNodes].voice(0).channelCache[0].outputs[ref/maxNodes].data():nullptr;
        }
        template<class Render> void renderUpstream(int index,int frames,Render render)noexcept{
            auto& g=runtime->graph;auto& n=g.nodes[index];auto& lane=n.voice(0);auto& p=*n.processor;
            for(std::size_t port=0;port<p.sources.size();port++){
                int ref=p.sources[port];lane.inputs[port]=ref<0?nullptr:g.nodes[ref%maxNodes].voice(0).channelCache[0].outputs[ref/maxNodes].data();
                lane.sourceRates[port]=ref<0?'c':g.nodes[ref%maxNodes].outputRate(ref/maxNodes);
            }
            for(std::size_t port=0;port<lane.outputPointers.size();port++)lane.outputPointers[port]=lane.channelCache[0].outputs[port].data();
            ProcessContext c{lane.inputs.data(),lane.inputs.size(),lane.outputPointers[0],n.controlRate?1u:std::size_t(frames),g.preparedSampleRate,g.silence.data(),std::size_t(frames)};
            c.outputs=lane.outputPointers.data();c.outputCount=lane.outputPointers.size();c.sourceRates=lane.sourceRates.data();
            render(p,c);
        }
        void renderScalars(int frames)noexcept{
            for(int index:scalarPlan)renderUpstream(index,frames,[](NodeProcessor& p,ProcessContext& c){p.process(c);});
        }
        void configure(std::istream&)override{throw std::runtime_error("Use configureGraph for a Multiple Group instance");}
        void renderGraph(int frames,AudioIO* io,MidiSource* midi)noexcept override{renderFeedbackGraph(frames,io,midi);}
        char resolveAutoRate(const char* rates,std::size_t count)noexcept override{
            auto result=BakedGraphProcessor::resolveAutoRate(rates,count);
            for(int output:runtime->outputs)if(dynamic_cast<InstanceCombineNode*>(runtime->graph.nodes[output].processor.get()))setRate(runtime->graph.nodes[output],'a');
            return result;
        }
        std::vector<InstanceContextNode*> contexts;
    };
    struct State {
        std::vector<std::unique_ptr<Instance>> instances;
        std::vector<float> gains;
        std::vector<std::vector<float>> left,right;
        std::vector<float> weights;
        std::vector<float> fade;
        std::vector<std::vector<float>> weightLeft,weightRight;
        bool initialized=false;
    };
    std::shared_ptr<State> state;
    void bindIdentity(const std::string& id)override{
        if(state)for(auto& instance:state->instances)instance->bindIdentity(id);
    }
    // Exporters provide a concrete per-instance kernel; live graphs keep the
    // generic implementation. Allocation remains entirely on the control thread.
    virtual std::unique_ptr<Instance> createInstance(){return std::make_unique<Instance>();}
    int maximum=16,count=8;bool mean=false,primary=true,explicitCombines=false;std::size_t outputs=1,framesReserved=0;float fadeStep=1;
    bool canShareChannels()const noexcept override{
        // Connected boundaries remain conservative until their source layout is
        // known. An unconnected group can expose its proven internal layout.
        if(!state||state->instances.empty()||std::any_of(sources.begin(),sources.end(),[](int s){return s>=0;}))return false;
        return state->instances[0]->canShareChannels();
    }
    bool separatesChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{
        return state&&!state->instances.empty()?state->instances[0]->inputResourceKind(port):ResourceKind::none;
    }
    bool supportsScalarInputs()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t port)const noexcept override{return port!=0;}
    std::size_t outputCount()const noexcept override{return outputs;}
    void shareChannelState(NodeProcessor& other)override{state=static_cast<MultipleGraphNode&>(other).state;primary=false;}
    static std::unique_ptr<NodeProcessor> factory(const std::string& id){
        if(id=="spectrum")return std::make_unique<SpectrumNode>();
        if(id=="fftstream")return std::make_unique<FFTStreamNode>();
        if(id=="spectralgraph")return std::make_unique<SpectralGraphNode>();
        if(id=="ifft")return std::make_unique<IFFTNode>();
        if(id=="instancecombine")return std::make_unique<InstanceCombineNode>();
        if(id=="math")return std::make_unique<MathNode>();
        if(id=="maprange")return std::make_unique<MapRangeNode>();
        if(id=="mix")return std::make_unique<MixNode>();
        if(id=="curve")return std::make_unique<CurveNode>();
        if(id=="clamp")return std::make_unique<ClampNode>();
        if(id=="unitconvert")return std::make_unique<UnitConverterNode>();
        if(id=="value")return std::make_unique<ValueNode>();
        if(id=="comparator")return std::make_unique<ComparatorNode>();
        if(id=="gain")return std::make_unique<GainNode>();
        if(id=="filter")return std::make_unique<FilterNode>();
        if(id=="delay")return std::make_unique<DelayNode>();
        if(id=="osc")return std::make_unique<OscillatorNode>();
        if(id=="additive")return std::make_unique<AdditiveOscillatorNode>();
        if(id=="digitalnoise")return std::make_unique<DigitalNoiseNode>();
        if(id=="noise")return std::make_unique<NoiseNode>();
        if(id=="pan")return std::make_unique<PanNode>();
        if(id=="channelseparate")return std::make_unique<ChannelSeparateNode>();
        if(id=="channelcombine")return std::make_unique<ChannelCombineNode>();
        if(id=="context")return std::make_unique<ContextInputNode>();
        if(id=="slew")return std::make_unique<SlewNode>();
        if(id=="bitcrusher")return std::make_unique<BitcrusherNode>();
        if(id=="samplehold")return std::make_unique<SampleHoldNode>();
        if(id=="edgedetector")return std::make_unique<EdgeDetectorNode>();
        if(id=="clock")return std::make_unique<ClockNode>();
        if(id=="rateconvert")return std::make_unique<RateConverterNode>();
        if(id=="triggerpulse")return std::make_unique<TriggerPulseNode>();
        if(id=="scalequantizer")return std::make_unique<ScaleQuantizerNode>();
        if(id=="pitchhold")return std::make_unique<PitchHoldNode>();
        if(id=="instancecontext")return std::make_unique<InstanceContextNode>();
        if(id=="multiplespectruminput")return std::make_unique<MultipleSpectrumInputNode>();
        if(id=="multipleinput")return std::make_unique<MultipleInputNode>();
        if(id=="multipleoutput")return std::make_unique<MultipleOutputNode>();
        throw std::runtime_error("Unsupported Multiple Group node: "+id);
    }
    void configure(std::istream& in)override{
        auto p=parameters::MultipleGraphNodeParameters::read(in);sources=p.sources();maximum=p.maximum_value;count=p.count_value;mean=p.mode_value=="mean";
        if(maximum<1||maximum>256)throw std::runtime_error("Invalid instance capacity");
        if(p.program_value.size()>2000000||p.program_value.size()%2)throw std::runtime_error("Invalid Multiple Group program");
        auto digit=[](char c)->unsigned{if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;throw std::runtime_error("Invalid Multiple Group encoding");};
        std::string decoded;for(std::size_t i=0;i<p.program_value.size();i+=2)decoded.push_back(char(digit(p.program_value[i])*16+digit(p.program_value[i+1])));
        std::istringstream program(decoded);std::string marker;std::size_t inputCount;
        if(!(program>>marker>>inputCount>>outputs)||(marker!="MULT1"&&marker!="MULT2"&&marker!="MULT3")||inputCount>9||outputs<1||outputs>8)throw std::runtime_error("Invalid Multiple Group header");
        explicitCombines=marker=="MULT2";
        std::vector<int> inputNodes(inputCount),outputNodes(outputs);
        for(auto& i:inputNodes)if(!(program>>i)||i<0||i>=256)throw std::runtime_error("Invalid Multiple Group input");
        for(auto& i:outputNodes)if(!(program>>i)||i<0||i>=256)throw std::runtime_error("Invalid Multiple Group output");
        std::vector<std::pair<int,std::string>> policies;
        if(marker=="MULT3"){
            int explicitMode,policyCount;
            if(!(program>>explicitMode>>policyCount)||explicitMode<0||explicitMode>1||policyCount<0||policyCount>256)throw std::runtime_error("Invalid instance rate policies");
            explicitCombines=explicitMode!=0;
            for(int p=0;p<policyCount;p++){
                int index;std::string policy;
                if(!(program>>index>>policy)||index<0||index>=256||policy.size()>8||policy.find_first_not_of("ace?")!=std::string::npos)throw std::runtime_error("Invalid instance rate policy");
                policies.emplace_back(index,std::move(policy));
            }
        }
        std::string plan;std::getline(program,plan);
        state=std::make_shared<State>();state->gains.resize(maximum);
        for(int i=0;i<maximum;i++){
            auto instance=createInstance();instance->sources=sources;instance->configureGraph(plan,factory,inputNodes,outputNodes,{});
            for(const auto& policy:policies){
                if(std::size_t(policy.first)>=instance->runtime->graph.nodes.size()||policy.second.size()!=instance->runtime->graph.nodes[policy.first].outputRates.size())throw std::runtime_error("Invalid instance rate policy target");
                instance->runtime->outputPolicies[policy.first]=policy.second;
            }
            for(int index:instance->runtime->graph.order)if(!instance->runtime->outputPolicies[index].empty())instance->runtime->autoNodes.push_back(index);
            if(!inputNodes.empty())instance->setFallback(0,float(count));
            if(instance->runtime->graph.nodes.size()*maximum>4096)throw std::runtime_error("Multiple Group exceeds 4096 node instances; reduce Maximum instances");
            for(auto& node:instance->runtime->graph.nodes){
                // Copies of the same logical generator share an optional block
                // memo, not their evolving RNG state. Divergent histories miss.
                auto index=&node-instance->runtime->graph.nodes.data();
                if(auto* noise=dynamic_cast<NoiseNode*>(node.processor.get())){
                    noise->instanceCache=i==0?std::make_shared<NoiseBlockCache>():static_cast<NoiseNode&>(*state->instances[0]->runtime->graph.nodes[index].processor).instanceCache;
                    static_cast<NoiseNode&>(*node.rightProcessor).instanceCache=noise->instanceCache;
                }
                if(auto* noise=dynamic_cast<DigitalNoiseNode*>(node.processor.get())){
                    noise->instanceCache=i==0?std::make_shared<NoiseBlockCache>():static_cast<DigitalNoiseNode&>(*state->instances[0]->runtime->graph.nodes[index].processor).instanceCache;
                    static_cast<DigitalNoiseNode&>(*node.rightProcessor).instanceCache=noise->instanceCache;
                }
                if(auto* context=dynamic_cast<InstanceContextNode*>(node.processor.get())){context->index=i;instance->contexts.push_back(context);}
                if(auto* context=dynamic_cast<InstanceContextNode*>(node.rightProcessor.get())){context->index=i;instance->contexts.push_back(context);}
            }
            if(explicitCombines)for(int output:instance->runtime->outputs)if(!dynamic_cast<InstanceCombineNode*>(instance->runtime->graph.nodes[output].processor.get()))throw std::runtime_error("Invalid instance reduction boundary");
            instance->planReduction();
            ProcessContext mono{};instance->resolveChannels(mono);
            state->instances.push_back(std::move(instance));
        }
    }
    void prepare(double rate,std::size_t frames)override{
        framesReserved=frames;fadeStep=static_cast<float>(1.0/std::max(1.0,.005*rate));
        if(!primary)return;
        state->left.assign(outputs,std::vector<float>(frames));state->right.assign(outputs,std::vector<float>(frames));state->weights.resize(frames);state->weightLeft.assign(outputs,std::vector<float>(frames));state->weightRight.assign(outputs,std::vector<float>(frames));
        for(auto& instance:state->instances)instance->prepare(rate,frames);
        state->fade.resize(frames);
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& old=static_cast<MultipleGraphNode&>(previous);if(!primary||!state||!old.state)return;
        for(std::size_t i=0;i<std::min(state->instances.size(),old.state->instances.size());i++){
            state->instances[i]->adoptState(*old.state->instances[i]);state->gains[i]=old.state->gains[i];
        }state->initialized=old.state->initialized;
    }
    void process(ProcessContext& c)noexcept override{processInstances<Instance>(c);}
    template<class CompiledInstance> void processInstances(ProcessContext& c)noexcept{
        auto frames=c.blockFrames?c.blockFrames:c.bufferSize;if(!state||frames>framesReserved)return;
        if(c.channelIndex==0){
            float requested=c.numericInput(0,float(count))[0];int active=std::isfinite(requested)?int(std::clamp(std::floor(requested),0.f,float(maximum))):0;
            if(!state->initialized){for(int i=0;i<maximum;i++)state->gains[i]=i<active?1.f:0.f;state->initialized=true;}
            for(std::size_t p=0;p<outputs;p++){std::fill_n(state->left[p].data(),frames,0);std::fill_n(state->right[p].data(),frames,0);std::fill_n(state->weightLeft[p].data(),frames,0);std::fill_n(state->weightRight[p].data(),frames,0);}
            std::fill_n(state->weights.data(),frames,0);
            std::array<char,maxNodes> rates{};for(std::size_t p=0;p<std::min(c.inputCount,rates.size());p++)rates[p]=c.sourceRates?c.sourceRates[p]:c.input(p)?'a':'c';
            // Buffers are already in each instance's scheduler cache; no temporary allocation in process().
            for(int i=0;i<maximum;i++){
                float target=i<active?1.f:0.f;if(target==0&&state->gains[i]==0)continue;
                auto& instance=static_cast<CompiledInstance&>(*state->instances[i]);for(auto* context:instance.contexts)context->count=active;
                instance.resolveChannels(c);
                instance.resolveAutoRate(rates.data(),std::min(c.inputCount,rates.size()));
                for(int input:instance.runtime->inputs){auto& n=instance.runtime->graph.nodes[input];static_cast<BakedInput&>(*n.processor).external=&c;static_cast<BakedInput&>(*n.rightProcessor).external=&c;}
                if(instance.reductionReady()){
                    instance.renderScalars(int(frames));
                    auto& osc=static_cast<OscillatorNode&>(*instance.runtime->graph.nodes[instance.reductionOsc].processor);
                    auto& combine=static_cast<InstanceCombineNode&>(*instance.runtime->graph.nodes[instance.runtime->outputs[0]].processor);
                    float weight=instance.scalar(combine.sources[1],combine.weight);weight=std::isfinite(weight)?std::max(0.f,weight):0.f;
                    bool connected=!osc.sources.empty()&&osc.sources[0]>=0;
                    const float* modulation=instance.audioFrequency();
                    bool fixed=!modulation&&(connected||osc.currentFrequency==osc.frequency);
                    auto kernel=osc.prepareSample(connected?instance.scalar(osc.sources[0],osc.frequency):osc.frequency,c.sampleRate);
                    bool weighted=combine.mode=="weighted";
                    const bool steady=state->gains[i]==target;
                    if(osc.reset.take())osc.resetPhase(); // Connected trigger sources use the general scheduler.
                    double cycles=osc.phase/OscillatorNode::tau;
                    for(std::size_t sample=0;sample<frames;sample++){
                        auto& gain=state->gains[i];if(!steady)gain=target>gain?std::min(target,gain+fadeStep):std::max(target,gain-fadeStep);
                        state->weights[sample]+=gain;
                        osc.currentFrequency+=osc.sampleSmoothing*(osc.frequency-osc.currentFrequency);
                        if(!fixed)kernel=osc.prepareSample(modulation?modulation[sample]:osc.currentFrequency,c.sampleRate);
                        // Zero weight saves the table lookup, but never freezes phase.
                        float value=0;
                        if(weight==0)cycles=OscillatorNode::wrap(cycles+kernel.increment);
                        else value=osc.sampleCycles(kernel,cycles)*weight*gain;
                        state->left[0][sample]+=value;state->right[0][sample]+=value;
                        if(weighted){state->weightLeft[0][sample]+=weight*gain;state->weightRight[0][sample]+=weight*gain;}
                    }
                    osc.phase=cycles*OscillatorNode::tau;
                    continue;
                }
                instance.renderGraph(int(frames),c.audioIO,c.midiSource);
                const bool steady=state->gains[i]==target;
                for(std::size_t sample=0;sample<frames;sample++){
                    auto& gain=state->gains[i];if(!steady)gain=target>gain?std::min(target,gain+fadeStep):std::max(target,gain-fadeStep);
                    state->fade[sample]=gain;state->weights[sample]+=gain;
                }
                // Resolve graph/cache metadata once per output, not once per sample.
                for(std::size_t p=0;p<outputs;p++){
                    auto& node=instance.runtime->graph.nodes[instance.runtime->outputs[p]];auto& lane=node.voice(0);
                    const auto* left=lane.channelCache[0].outputs[0].data();const auto* right=lane.channelCache[node.stereo?1:0].outputs[0].data();
                    const auto stride=node.outputRate(0)=='c'?0u:1u;
                    for(std::size_t sample=0;sample<frames;sample++){
                        const float gain=steady?target:state->fade[sample];
                        state->left[p][sample]+=left[sample*stride]*gain;state->right[p][sample]+=right[sample*stride]*gain;
                    }
                    if(explicitCombines&&static_cast<InstanceCombineNode&>(*node.processor).mode=="weighted"){
                        const auto* l=lane.channelCache[0].outputs[1].data();const auto* r=lane.channelCache[node.stereo?1:0].outputs[1].data();
                        for(std::size_t sample=0;sample<frames;sample++){const float gain=steady?target:state->fade[sample];state->weightLeft[p][sample]+=l[sample]*gain;state->weightRight[p][sample]+=r[sample]*gain;}
                    }
                }
            }
            for(std::size_t p=0;p<outputs;p++){
                std::string mode=explicitCombines?static_cast<InstanceCombineNode&>(*state->instances[0]->runtime->graph.nodes[state->instances[0]->runtime->outputs[p]].processor).mode:mean?"mean":"sum";
                if(mode=="mean")for(std::size_t i=0;i<frames;i++){float d=std::max(1.f,state->weights[i]);state->left[p][i]/=d;state->right[p][i]/=d;}
                else if(mode=="weighted")for(std::size_t i=0;i<frames;i++){
                        float l=state->weightLeft[p][i],r=state->weightRight[p][i];
                        state->left[p][i]=l>0?state->left[p][i]/l:0;state->right[p][i]=r>0?state->right[p][i]/r:0;
                }
            }
        }
        for(std::size_t p=0;p<outputs;p++)if(auto* out=c.outputAt(p))std::copy_n((c.channelIndex==0?state->left[p]:state->right[p]).data(),c.bufferSize,out);
    }
};
EASYKNACK_NODE(MultipleGraphNode,"multiplegraph")
}
