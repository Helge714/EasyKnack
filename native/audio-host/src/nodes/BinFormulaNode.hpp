#pragma once
#include "FormulaNode.hpp"
#include "BinContextNode.hpp"
namespace easyknack {
/** Formula VM in the FFT-frame domain, with independent persistent state per bin. */
struct BinFormulaNode : FormulaNode {
    std::size_t bin=0,binCount=0;
    std::uint64_t sourceIdentity=0,framePosition=0;
    std::vector<float> states;
    std::vector<std::uint64_t> typedStates;
    std::array<bool,16> pending{},capture{};
    std::array<std::uint64_t,16> requested{};
    bool hasState()const noexcept{return !program.states.empty()||!program.typed.states.empty();}
    void configure(std::istream& in)override{
        FormulaNode::configure(in);
        if(bufferInputs)throw std::runtime_error("Spectral formulas accept numeric and trigger inputs, not file/delay buffers");
        // Embedded bytecode is portable: exported spectra never depend on a local live-code DLL.
        module.reset();
    }
    void prepare(double,std::size_t)override{
        states.resize((SpectrumStream::maxSize/2+1)*program.states.size());
        typedStates.resize((SpectrumStream::maxSize/2+1)*program.typed.states.size());
        binCount=0;
    }
    void beginBlock(const SpectrumStream& source)noexcept{
        for(unsigned p=0;p<inputSize;p++)if((triggerInputs&(1u<<p))&&buttons[p].take()){pending[p]=true;requested[p]=source.blockStart;}
    }
    void beginFrame(const SpectrumStream& source,const StreamSpectrumFrame& frame)noexcept{
        auto count=source.size/2+1;
        if(binCount!=count||sourceIdentity!=source.identity){
            binCount=count;sourceIdentity=source.identity;
            for(std::size_t k=0;k<count;k++){
                for(std::size_t s=0;s<program.states.size();s++)states[k*program.states.size()+s]=program.states[s].initial;
                for(std::size_t s=0;s<program.typed.states.size();s++)typedStates[k*program.typed.states.size()+s]=program.typed.states[s].initial;
            }
        }
        framePosition=std::uint64_t(std::max<std::int64_t>(0,frame.start+static_cast<std::int64_t>(source.windowFrames())));
        for(unsigned p=0;p<inputSize;p++){capture[p]=pending[p]&&framePosition>requested[p];if(capture[p])pending[p]=false;}
    }
    void process(ProcessContext& c)noexcept override{
        float input[16]{},output[8]{};
        for(unsigned p=0;p<inputSize;p++){input[p]=c.numericInput(p,fallback[p])[0];if(triggerInputs&(1u<<p))input[p]=capture[p]||(std::isfinite(input[p])&&input[p]!=0)?1:0;}
        FormulaContext context;context.sampleRate=c.sampleRate;context.blockSize=binCount;context.sampleIndex=bin;context.samplePosition=framePosition;context.seed=seed^FormulaContext::hash(bin);context.sineTable=sineTable;
        context.state=states.empty()?nullptr:states.data()+bin*program.states.size();
        context.typedState=typedStates.empty()?nullptr:typedStates.data()+bin*program.typed.states.size();
        program.evaluate(input,output,context);
        for(unsigned p=0;p<outputSize;p++)if(auto* out=c.outputAt(p))*out=triggerOutputs&(1u<<p)?(output[p]!=0?1.f:0.f):output[p];
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto* old=dynamic_cast<BinFormulaNode*>(&previous);if(!old)return;
        binCount=old->binCount;sourceIdentity=old->sourceIdentity;
        pending=old->pending;requested=old->requested;for(unsigned p=0;p<16;p++)buttons[p].seen=old->buttons[p].seen;
        for(std::size_t k=0;k<binCount;k++){
            for(std::size_t i=0;i<program.states.size();i++){
                const auto& s=program.states[i];float value=s.initial;
                for(std::size_t j=0;j<old->program.states.size();j++){const auto& t=old->program.states[j];if(s.name==t.name&&s.type==t.type&&s.initial==t.initial){value=old->states[k*old->program.states.size()+j];break;}}
                states[k*program.states.size()+i]=value;
            }
            for(std::size_t i=0;i<program.typed.states.size();i++){
                const auto& s=program.typed.states[i];auto value=s.initial;
                for(std::size_t j=0;j<old->program.typed.states.size();j++){const auto& t=old->program.typed.states[j];if(s.name==t.name&&s.kind==t.kind&&s.initial==t.initial){value=old->typedStates[k*old->program.typed.states.size()+j];break;}}
                typedStates[k*program.typed.states.size()+i]=value;
            }
        }
    }
};
}
