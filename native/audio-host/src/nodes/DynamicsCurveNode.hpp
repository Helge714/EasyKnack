#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../dsp/Dynamics.hpp"
namespace easyknack {
struct DynamicsCurveNode final:NodeProcessor {
    double level=0,threshold=-12,ratio=4,knee=6;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::DynamicsCurveNodeParameters::read(in);sources=p.sources();level=p.level_value;threshold=p.threshold_value;ratio=p.ratio_value;knee=p.knee_value;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const DynamicsCurveNode&>(other);level=p.level;threshold=p.threshold;ratio=p.ratio;knee=p.knee;}
    void process(ProcessContext& c)noexcept override{
        auto x=c.numericInput(0,level),t=c.numericInput(1,threshold),r=c.numericInput(2,ratio),k=c.numericInput(3,knee);
        for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=dynamics::sample(dynamics::amplitude(dynamics::reduction(dynamics::decibels(std::max(0.,dynamics::finite(x[i]))),dynamics::bound(t[i],-120,24,-12),dynamics::bound(r[i],1,100,4),dynamics::bound(k[i],0,48,6))));
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
};
EASYKNACK_NODE(DynamicsCurveNode,"dynamicscurve")
}
