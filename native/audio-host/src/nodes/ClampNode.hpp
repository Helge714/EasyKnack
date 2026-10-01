#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

#include <algorithm>
#include <cmath>
#include <cstddef>

namespace easyknack {

enum class ClampMode {
    clip,
    foldback,
    wrap,
    soft_clip
};

struct ClampNode : NodeProcessor {
    double sampleSmoothing = 0;
    void prepare(double rate, std::size_t) override { sampleSmoothing = 1 - std::exp(-1.0 / (.005 * rate)); }
    bool supportsSampleFeedback() const noexcept override { return true; }
    bool supportsScalarInputs()const noexcept override{return true;}

    bool canShareChannels() const noexcept override {
        return true;
    }

    bool supportsControlRate() const noexcept override {
        return true;
    }

    bool supportsEventRate() const noexcept override {
        return true;
    }

    bool evaluatesOnEvents() const noexcept override {
        return true;
    }

    float inputValue = 0.f;
    float currentInput = 0.f;

    ClampMode mode = ClampMode::clip;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const ClampNode&>(configuration);
        inputValue=p.inputValue;mode=p.mode;
    }
    void configure(std::istream& in) override {

        in >> std::ws;

        if (in.peek() == '!') {

            auto p = parameters::ClampNodeParameters::read(in);

            sources = p.sources();

            inputValue = p.audio_in_value;
            currentInput = inputValue;

            if (p.mode_value == "clip")
                mode = ClampMode::clip;
            else if (p.mode_value == "foldback")
                mode = ClampMode::foldback;
            else if (p.mode_value == "wrap")
                mode = ClampMode::wrap;
            else if (p.mode_value == "soft_clip")
                mode = ClampMode::soft_clip;
            else
                throw std::runtime_error("Unknown clamp mode");

            return;
        }

        sources = { readInput(in) };

        inputValue = readNumber(in, -1000000, 1000000);
        currentInput = inputValue;

        std::string name;

        if (!(in >> name))
            throw std::runtime_error("invalid clamp parameters");

        if (name == "clip")
            mode = ClampMode::clip;
        else if (name == "foldback")
            mode = ClampMode::foldback;
        else if (name == "wrap")
            mode = ClampMode::wrap;
        else if (name == "soft_clip")
            mode = ClampMode::soft_clip;
        else
            throw std::runtime_error("unknown clamp mode");
    }

    void process(ProcessContext& context) noexcept override {

        const auto input=context.numericInput(0);

        if (!context.samplewise && context.bufferSize == 1) {

            currentInput = inputValue;

            context.output[0] = evaluate(
                input ? input[0] : inputValue,
                mode
            );

            return;
        }

        const float smoothing = static_cast<float>(
            context.samplewise ? sampleSmoothing : 1 - std::exp(-1.0 / (.005 * context.sampleRate))
        );

        for (std::size_t i = 0; i < context.bufferSize; ++i) {

            currentInput += smoothing * (inputValue - currentInput);

            const float value =
                input ? input[i] : currentInput;

            context.output[i] = evaluate(value, mode);
        }
    }

    void processExport(ProcessContext& context) noexcept {

        const auto input=context.numericInput(0);

        if (!context.samplewise && context.bufferSize == 1) {

            currentInput = inputValue;

            context.output[0] = evaluate(
                input ? input[0] : inputValue,
                mode
            );

            return;
        }

        const float smoothing = static_cast<float>(
            context.samplewise ? sampleSmoothing : 1 - std::exp(-1.0 / (.005 * context.sampleRate))
        );

        for (std::size_t i = 0; i < context.bufferSize; ++i) {

            currentInput += smoothing * (inputValue - currentInput);

            context.output[i] = evaluate(
                input ? input[i] : currentInput,
                mode
            );
        }
    }

    void inheritState(const NodeProcessor& previous) noexcept override {

        const auto& old =
            static_cast<const ClampNode&>(previous);

        currentInput = old.currentInput;
    }

    static float evaluate(
        float value,
        ClampMode mode
    ) noexcept {

        if (!std::isfinite(value))
            return 0.f;

        switch (mode) {

        case ClampMode::clip:
            return std::clamp(value, -1.f, 1.f);

        case ClampMode::foldback:
            return foldback(value);

        case ClampMode::wrap:
            return wrap(value);

        case ClampMode::soft_clip:
            return std::tanh(value);
        }

        return 0.f;
    }

private:

    static float wrap(float value) noexcept {

        // Maps any value periodically into [-1, 1).
        float result = std::fmod(value + 1.f, 2.f);

        if (result < 0.f)
            result += 2.f;

        return result - 1.f;
    }

    static float foldback(float value) noexcept {

        // Already inside the desired range.
        if (value >= -1.f && value <= 1.f)
            return value;

        // First wrap into a four-unit repeating interval.
        float result = std::fmod(value + 1.f, 4.f);

        if (result < 0.f)
            result += 4.f;

        // Reflect the second half.
        if (result <= 2.f)
            return result - 1.f;

        return 3.f - result;
    }
};

EASYKNACK_NODE(ClampNode, "clamp")

}