#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <cstddef>

namespace easyknack {
struct GainNode : NodeProcessor {
    bool supportsSampleFeedback()const noexcept override{return true;}

    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels() const noexcept override { return true; }
    float currentGain = 0;
    float targetGain = 1;
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const GainNode&>(configuration);
        targetGain=p.targetGain;
    }
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::GainNodeParameters::read(in);
        sources=p.sources();targetGain=p.gainDb_value<=-160?0:std::pow(10.,p.gainDb_value/20);
        return;}

        sources = {readInput(in)}; float db=readNumber(in,-160,6); targetGain=db<=-160?0:std::pow(10.f,db/20.f);
    }
    void process(ProcessContext& context) noexcept override {
        auto input=context.numericInput(0);auto modulation=context.numericInput(1);
        for(std::size_t i=0;i<context.bufferSize;i++){
            currentGain+=(targetGain-currentGain)*.004f;
            float db=modulation?modulation[i]:0;
            float gain=modulation?(!std::isfinite(db)||db<=-160?0.f:std::pow(10.f,std::min(db,120.f)/20.f)):currentGain;
            context.output[i]=(input?input[i]:0.f)*gain;
        }
    }
    void inheritState(const NodeProcessor& previous) noexcept override {
        currentGain = static_cast<const GainNode&>(previous).currentGain;
    }
    void process(const float* input, float* output, std::size_t bufferSize, float gain) noexcept {
        for (std::size_t i = 0; i < bufferSize; ++i) {
            currentGain += (gain - currentGain) * 0.004f;
            output[i] = input[i] * currentGain;
        }
    }
};
EASYKNACK_NODE(GainNode, "gain")
}

