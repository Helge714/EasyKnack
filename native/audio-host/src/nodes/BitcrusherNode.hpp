#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

#include <algorithm>
#include <cmath>
#include <cstddef>

namespace easyknack {

struct BitcrusherNode : NodeProcessor {
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
    float bitsValue = 8.f;
    float downsampleValue = 1.f;

    float heldValue = 0.f;
    std::size_t holdCounter = 0;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const BitcrusherNode&>(configuration);
        inputValue=p.inputValue;bitsValue=p.bitsValue;downsampleValue=p.downsampleValue;
    }
    void configure(std::istream& in) override {

        in >> std::ws;

        if (in.peek() == '!') {

            auto p =
                parameters::BitcrusherNodeParameters::read(in);

            sources = p.sources();

            inputValue =
                static_cast<float>(p.audio_in_value);

            bitsValue =
                static_cast<float>(p.bits_value);

            downsampleValue =
                static_cast<float>(p.downsample_value);

            return;
        }

        sources = {
            readInput(in)
        };

        inputValue =
            readNumber(
                in,
                -1.0,
                1.0
            );

        sources.push_back(
            readInput(in)
        );

        bitsValue =
            readNumber(
                in,
                1.0,
                24.0
            );

        sources.push_back(
            readInput(in)
        );

        downsampleValue =
            readNumber(
                in,
                1.0,
                1024.0
            );
    }

    void process(
        ProcessContext& context
    ) noexcept override {

        const auto input=context.numericInput(0);

        const auto bits=context.numericInput(1);

        const auto downsample=context.numericInput(2);

        for (
            std::size_t i = 0;
            i < context.bufferSize;
            ++i
        ) {

            const float inValue =
                safeValue(
                    input
                        ? input[i]
                        : inputValue
                );

            const float bitDepth =
                validBits(
                    bits
                        ? bits[i]
                        : bitsValue
                );

            const std::size_t factor =
                validDownsample(
                    downsample
                        ? downsample[i]
                        : downsampleValue
                );

            if (holdCounter == 0) {

                heldValue =
                    quantize(
                        inValue,
                        bitDepth
                    );

                holdCounter =
                    factor - 1;

            } else {

                --holdCounter;
            }

            context.output[i] =
                heldValue;
        }
    }

    void processExport(
        ProcessContext& context
    ) noexcept {

        process(context);
    }

    void inheritState(
        const NodeProcessor& previous
    ) noexcept override {

        const auto& old =
            static_cast<const BitcrusherNode&>(
                previous
            );

        heldValue =
            old.heldValue;

        holdCounter =
            old.holdCounter;
    }

private:

    static float safeValue(
        float value
    ) noexcept {

        return std::isfinite(value)
            ? value
            : 0.f;
    }

    static float validBits(
        float value
    ) noexcept {

        if (!std::isfinite(value))
            return 8.f;

        return std::clamp(
            value,
            1.f,
            24.f
        );
    }

    static std::size_t validDownsample(
        float value
    ) noexcept {

        if (!std::isfinite(value))
            return 1;

        value =
            std::clamp(
                value,
                1.f,
                1024.f
            );

        return static_cast<std::size_t>(
            std::round(value)
        );
    }

    static float quantize(
        float value,
        float bits
    ) noexcept {

        value =
            std::clamp(
                value,
                -1.f,
                1.f
            );

        /*
         * Integer bit depth.
         *
         * 8 bit -> 256 possible values
         * over approximately [-1, 1].
         */
        const int bitDepth =
            static_cast<int>(
                std::round(
                    std::clamp(
                        bits,
                        1.f,
                        24.f
                    )
                )
            );

        /*
         * Number of intervals across [-1, 1].
         *
         * Example:
         * 8 bit -> 255 intervals / 256 representable levels.
         */
        const float levels =
            std::exp2(
                static_cast<float>(
                    bitDepth
                )
            ) - 1.f;

        if (levels <= 1.f) {

            /*
             * 1-bit audio.
             */
            return value >= 0.f
                ? 1.f
                : -1.f;
        }

        /*
         * Convert [-1, 1] -> [0, 1],
         * quantize,
         * then map back to [-1, 1].
         */
        const float normalized =
            value * 0.5f + 0.5f;

        const float quantized =
            std::round(
                normalized * levels
            ) / levels;

        return
            quantized * 2.f - 1.f;
    }
};

EASYKNACK_NODE(
    BitcrusherNode,
    "bitcrusher"
)

}