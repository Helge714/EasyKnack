#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct PanNode:NodeProcessor {
    double sampleSmoothing = 0;
    void prepare(double rate, std::size_t) override { sampleSmoothing = 1 - std::exp(-1.0 / (.005 * rate)); }
    bool supportsSampleFeedback() const noexcept override { return true; }
    float pan=0,current=0;
    // Channel-local gains require separate left/right evaluations, even for a mono source.
    bool canShareChannels()const noexcept override{return false;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const PanNode&>(configuration);
        pan=p.pan;
    }
    void configure(std::istream& in)override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::PanNodeParameters::read(in);
        sources=p.sources();pan=p.pan_value;current=pan;
        return;}
sources={readInput(in),readInput(in)};pan=readNumber(in,-1,1);current=pan;}
    void inheritState(const NodeProcessor& previous)noexcept override{current=static_cast<const PanNode&>(previous).current;}
    void process(ProcessContext& c)noexcept override{
        const float smoothing=static_cast<float>(c.samplewise?sampleSmoothing:1-std::exp(-1/(.005*c.sampleRate)));
        const float* input=c.inputOrSilence(0);const float* modulation=c.input(1);
        for(std::size_t i=0;i<c.bufferSize;i++){
            current+=smoothing*(pan-current);
            float position=modulation?(std::isfinite(modulation[i])?std::clamp(modulation[i],-1.f,1.f):0.f):current;
            float angle=(position+1)*.7853981633974483f;
            float gain=std::sqrt(2.f)*(c.channelIndex==0?std::cos(angle):std::sin(angle));
            c.output[i]=input[i]*gain;
        }
    }
};
EASYKNACK_NODE(PanNode,"pan")
}
