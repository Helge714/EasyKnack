#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct SampleDelayNode : NodeProcessor {
    float previous=0,fallback=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool defersInputs()const noexcept override{return true;}
    bool sampleFeedbackBoundary()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::SampleDelayNodeParameters::read(in);sources=p.sources();fallback=p.in_value;}
        else{sources={readInput(in)};fallback=readNumber(in);}
    }
    void prepare(double,std::size_t)override{previous=0;}
    void applyParameters(const NodeProcessor& p)noexcept override{fallback=static_cast<const SampleDelayNode&>(p).fallback;}
    void inheritState(const NodeProcessor& p)noexcept override{previous=static_cast<const SampleDelayNode&>(p).previous;}
    void store(float value)noexcept{previous=std::isfinite(value)?value:0.f;}
    void process(ProcessContext& c)noexcept override{auto input=c.numericInput(0,fallback);for(std::size_t i=0;i<c.bufferSize;i++){c.output[i]=previous;store(input[i]);}}
    void processSample(ProcessContext& c)noexcept override{c.output[0]=previous;}
    void commitSample(ProcessContext& c)noexcept override{store(c.numericInput(0,fallback)[0]);}
};
EASYKNACK_NODE(SampleDelayNode,"sampledelay")
}
