#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct PitchHoldNode : NodeProcessor {
    float frequency=440,confidence=1,threshold=.8f,initial=440,held=440;bool accepted=false;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 2;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::PitchHoldNodeParameters::read(in);sources=p.sources();frequency=p.frequency_value;confidence=p.confidence_value;threshold=p.threshold_value;initial=p.initial_value;held=initial;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const PitchHoldNode&>(other);frequency=p.frequency;confidence=p.confidence;threshold=p.threshold;initial=p.initial;}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const PitchHoldNode&>(other);held=p.held;accepted=p.accepted;}
    void process(ProcessContext& c)noexcept override{
        auto f=c.numericInput(0,frequency),confidenceIn=c.numericInput(1,confidence),t=c.numericInput(2,threshold);auto* valid=c.outputAt(1);
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool ok=std::isfinite(f[i])&&f[i]>0&&std::isfinite(confidenceIn[i])&&std::isfinite(t[i])&&confidenceIn[i]>=std::clamp(t[i],0.f,1.f);
            if(ok){held=f[i];accepted=true;}else if(!accepted)held=initial;
            c.output[i]=held;if(valid)valid[i]=ok?1.f:0.f;
        }
    }
};
EASYKNACK_NODE(PitchHoldNode,"pitchhold")
}
