#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

#include <cmath>
#include <cstddef>
#include <string>

namespace easyknack {

enum class EdgeMode {
    rising,
    falling,
    both
};

struct EdgeDetectorNode : NodeProcessor {
    char resolveAutoRate(const char*,std::size_t)noexcept override{return 'e';}

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
        return false;
    }

    /*
     * Do NOT smooth the input or threshold.
     *
     * Edge detection depends on exact state transitions.
     */

    float inputValue = 0.f;
    float thresholdValue = 0.5f;

    bool previousAbove = false;
    bool initialized = false;

    EdgeMode mode =
        EdgeMode::rising;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const EdgeDetectorNode&>(configuration);
        inputValue=p.inputValue;thresholdValue=p.thresholdValue;mode=p.mode;
    }
    void configure(std::istream& in) override {

        in >> std::ws;

        if (in.peek() == '!') {

            auto p =
                parameters::EdgeDetectorNodeParameters::read(in);

            sources =
                p.sources();

            inputValue =
                static_cast<float>(
                    p.in_value
                );

            thresholdValue =
                static_cast<float>(
                    p.threshold_value
                );

            mode =
                parseMode(
                    p.mode_value
                );

            return;
        }

        sources = {
            readInput(in)
        };

        inputValue =
            readNumber(
                in,
                -1000000.f,
                1000000.f
            );

        sources.push_back(
            readInput(in)
        );

        thresholdValue =
            readNumber(
                in,
                -1000000.f,
                1000000.f
            );

        std::string name;

        if (!(in >> name)) {
            throw std::runtime_error(
                "invalid edge detector parameters"
            );
        }

        mode =
            parseMode(name);
    }

    bool writesEventBuffers()const noexcept override{return true;}
    bool processesEventBlocks()const noexcept override{return false;}
    bool supportsScalarInputs()const noexcept override{return true;}
    void process(
        ProcessContext& context
    ) noexcept override {

        const auto input = context.numericInput(0,inputValue);

        const auto threshold = context.numericInput(1,thresholdValue);

        for (
            std::size_t i = 0;
            i < context.bufferSize;
            ++i
        ) {

            const float value =
                safeValue(
                    input[i]
                );

            const float thresholdNow =
                safeValue(
                    threshold[i]
                );

            const bool above =
                value > thresholdNow;

            /*
             * Initial evaluation only establishes state.
             *
             * This prevents a newly created node from emitting
             * a fake rising edge merely because its initial
             * input happens to be above the threshold.
             */
            if (!initialized) {

                previousAbove =
                    above;

                initialized =
                    true;

                context.output[i] =
                    0.f;

                continue;
            }

            const bool rising =
                !previousAbove
                && above;

            const bool falling =
                previousAbove
                && !above;

            bool triggered = false;

            switch (mode) {

            case EdgeMode::rising:
                triggered =
                    rising;
                break;

            case EdgeMode::falling:
                triggered =
                    falling;
                break;

            case EdgeMode::both:
                triggered =
                    rising || falling;
                break;
            }

            if(triggered)if(auto* events=context.eventOutput())events->push(static_cast<std::uint32_t>(i),1,ValueEventKind::trigger);
            previousAbove =
                above;

            /*
             * Transitional representation:
             *
             * 1 = edge occurred
             * 0 = no edge
             *
             * TODO:
             * Once sparse EventBuffer support is implemented,
             * emit an event only at the corresponding sample
             * offset instead of producing dense 0/1 values.
             */
            context.output[i] =
                triggered
                    ? 1.f
                    : 0.f;
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
            static_cast<const EdgeDetectorNode&>(
                previous
            );

        previousAbove =
            old.previousAbove;

        initialized =
            old.initialized;
    }

private:

    static float safeValue(
        float value
    ) noexcept {

        return std::isfinite(value)
            ? value
            : 0.f;
    }

    static EdgeMode parseMode(
        const std::string& name
    ) {

        if (name == "rising")
            return EdgeMode::rising;

        if (name == "falling")
            return EdgeMode::falling;

        if (name == "both")
            return EdgeMode::both;

        throw std::runtime_error(
            "unknown edge detector mode"
        );
    }
};

EASYKNACK_NODE(
    EdgeDetectorNode,
    "edgedetector"
)

}