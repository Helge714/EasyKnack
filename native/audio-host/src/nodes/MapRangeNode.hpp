#pragma once
#include "ParameterContracts.hpp"

#include "../NodeProcessor.hpp"

#include <algorithm>
#include <cmath>
#include <cstddef>

namespace easyknack {

struct MapRangeNode : NodeProcessor {
    bool supportsSampleFeedback() const noexcept override { return true; }
    bool canShareChannels() const noexcept override { return true; }

    bool supportsControlRate() const noexcept override { return true; }
    bool supportsEventRate() const noexcept override { return true; }
    bool evaluatesOnEvents() const noexcept override { return true; }

    float valueValue   = 0.f;
    float fromMinValue = 0.f;
    float fromMaxValue = 1.f;
    float toMinValue   = 0.f;
    float toMaxValue   = 1.f;

    bool clamp = false;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const MapRangeNode&>(configuration);
        valueValue=p.valueValue;fromMinValue=p.fromMinValue;fromMaxValue=p.fromMaxValue;toMinValue=p.toMinValue;toMaxValue=p.toMaxValue;clamp=p.clamp;
    }
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::MapRangeNodeParameters::read(in);
        sources=p.sources();valueValue=p.audio_in_value;fromMinValue=p.from_min_value;fromMaxValue=p.from_max_value;toMinValue=p.to_min_value;toMaxValue=p.to_max_value;clamp=p.clamp_value!=0;
        return;}


        sources = { readInput(in) };
        valueValue = readNumber(in, -1000000, 1000000);

        sources.push_back(readInput(in));
        fromMinValue = readNumber(in, -1000000, 1000000);

        sources.push_back(readInput(in));
        fromMaxValue = readNumber(in, -1000000, 1000000);

        sources.push_back(readInput(in));
        toMinValue = readNumber(in, -1000000, 1000000);

        sources.push_back(readInput(in));
        toMaxValue = readNumber(in, -1000000, 1000000);

        int flag;
        if (!(in >> flag) || (flag != 0 && flag != 1))
            throw std::runtime_error("invalid map range parameters");

        clamp = flag != 0;
    }

    bool supportsScalarInputs()const noexcept override{return true;}
    void process(ProcessContext& c)noexcept override {
        const auto value=c.numericInput(0,valueValue),lo=c.numericInput(1,fromMinValue),hi=c.numericInput(2,fromMaxValue),outLo=c.numericInput(3,toMinValue),outHi=c.numericInput(4,toMaxValue);
        for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=evaluate(value[i],lo[i],hi[i],outLo[i],outHi[i],clamp);
    }

    void process(
        const float* inputValue,
        const float* inputFromMin,
        const float* inputFromMax,
        const float* inputToMin,
        const float* inputToMax,
        float* output,
        std::size_t bufferSize,
        float valueValue,
        float fromMinValue,
        float fromMaxValue,
        float toMinValue,
        float toMaxValue,
        bool clamp
    ) noexcept {

        for (std::size_t i = 0; i < bufferSize; ++i) {

            const float value =
                inputValue ? inputValue[i] : valueValue;

            const float fromMin =
                inputFromMin ? inputFromMin[i] : fromMinValue;

            const float fromMax =
                inputFromMax ? inputFromMax[i] : fromMaxValue;

            const float toMin =
                inputToMin ? inputToMin[i] : toMinValue;

            const float toMax =
                inputToMax ? inputToMax[i] : toMaxValue;

            output[i]=evaluate(value,fromMin,fromMax,toMin,toMax,clamp);
        }
    }
    static float evaluate(float value,float fromMin,float fromMax,float toMin,float toMax,bool clamp) noexcept {
            const float range = fromMax - fromMin;

            // Undefined input range
            if (std::abs(range) < 1e-12f) {
                return toMin;
            }

            float t = (value - fromMin) / range;

            if (clamp)
                t = std::clamp(t, 0.f, 1.f);

            float result = toMin + t * (toMax - toMin);

            if (!std::isfinite(result))
                result = 0.f;

            return result;
    }
};

EASYKNACK_NODE(MapRangeNode, "maprange")

}

