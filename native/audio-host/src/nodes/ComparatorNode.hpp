#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

#include <algorithm>
#include <cmath>
#include <cstddef>
#include <string>

namespace easyknack {

enum class ComparatorOperation {
    less,
    less_equal,
    greater,
    greater_equal,
    equal,
    not_equal
};

struct ComparatorNode : NodeProcessor {
    bool supportsSampleFeedback()const noexcept override{return true;}
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

    float aValue = 0.f;
    float bValue = 0.f;

    float currentA = 0.f;
    float currentB = 0.f;

    ComparatorOperation operation =
        ComparatorOperation::less;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const ComparatorNode&>(configuration);
        aValue=p.aValue;bValue=p.bValue;operation=p.operation;
    }
    void configure(std::istream& in) override {

        in >> std::ws;

        if (in.peek() == '!') {

            auto p =
                parameters::ComparatorNodeParameters::read(in);

            sources = p.sources();

            aValue =
                static_cast<float>(p.a_value);

            bValue =
                static_cast<float>(p.b_value);

            currentA = aValue;
            currentB = bValue;

            setOperation(
                p.operation_value
            );

            return;
        }

        sources = {
            readInput(in)
        };

        aValue =
            readNumber(
                in,
                -1000000.0,
                1000000.0
            );

        sources.push_back(
            readInput(in)
        );

        bValue =
            readNumber(
                in,
                -1000000.0,
                1000000.0
            );

        currentA = aValue;
        currentB = bValue;

        std::string name;

        if (!(in >> name))
            throw std::runtime_error(
                "invalid comparator parameters"
            );

        setOperation(name);
    }

    void process(
        ProcessContext& context
    ) noexcept override {

        const auto inputA=context.numericInput(0);

        const auto inputB=context.numericInput(1);

        if (context.bufferSize == 1 && !context.samplewise) {

            currentA = aValue;
            currentB = bValue;

            context.output[0] =
                evaluate(
                    inputA
                        ? inputA[0]
                        : aValue,
                    inputB
                        ? inputB[0]
                        : bValue,
                    operation
                );

            return;
        }

        const float smoothing =
            static_cast<float>(
                1.0
                - std::exp(
                    -1.0
                    / (
                        .005
                        * context.sampleRate
                    )
                )
            );

        for (
            std::size_t i = 0;
            i < context.bufferSize;
            ++i
        ) {

            currentA +=
                smoothing
                * (
                    aValue
                    - currentA
                );

            currentB +=
                smoothing
                * (
                    bValue
                    - currentB
                );

            const float a =
                inputA
                    ? inputA[i]
                    : currentA;

            const float b =
                inputB
                    ? inputB[i]
                    : currentB;

            context.output[i] =
                evaluate(
                    a,
                    b,
                    operation
                );
        }
    }

    void processExport(
        ProcessContext& context
    ) noexcept {

        const auto inputA=context.numericInput(0);

        const auto inputB=context.numericInput(1);

        if (context.bufferSize == 1 && !context.samplewise) {

            currentA = aValue;
            currentB = bValue;

            context.output[0] =
                evaluate(
                    inputA
                        ? inputA[0]
                        : aValue,
                    inputB
                        ? inputB[0]
                        : bValue,
                    operation
                );

            return;
        }

        const float smoothing =
            static_cast<float>(
                1.0
                - std::exp(
                    -1.0
                    / (
                        .005
                        * context.sampleRate
                    )
                )
            );

        for (
            std::size_t i = 0;
            i < context.bufferSize;
            ++i
        ) {

            currentA +=
                smoothing
                * (
                    aValue
                    - currentA
                );

            currentB +=
                smoothing
                * (
                    bValue
                    - currentB
                );

            context.output[i] =
                evaluate(
                    inputA
                        ? inputA[i]
                        : currentA,
                    inputB
                        ? inputB[i]
                        : currentB,
                    operation
                );
        }
    }

    void inheritState(
        const NodeProcessor& previous
    ) noexcept override {

        const auto& old =
            static_cast<const ComparatorNode&>(
                previous
            );

        currentA =
            old.currentA;

        currentB =
            old.currentB;
    }

    static float evaluate(
        float a,
        float b,
        ComparatorOperation operation
    ) noexcept {

        if (
            !std::isfinite(a)
            || !std::isfinite(b)
        ) {
            return 0.f;
        }

        constexpr float epsilon =
            1e-6f;

        bool result = false;

        switch (operation) {

        case ComparatorOperation::less:
            result = a < b;
            break;

        case ComparatorOperation::less_equal:
            result = a <= b;
            break;

        case ComparatorOperation::greater:
            result = a > b;
            break;

        case ComparatorOperation::greater_equal:
            result = a >= b;
            break;

        case ComparatorOperation::equal:
            result =
                std::abs(a - b)
                <= epsilon;
            break;

        case ComparatorOperation::not_equal:
            result =
                std::abs(a - b)
                > epsilon;
            break;
        }

        return result
            ? 1.f
            : 0.f;
    }

private:

    void setOperation(
        const std::string& name
    ) {

        if (name == "less")
            operation =
                ComparatorOperation::less;

        else if (name == "less_equal")
            operation =
                ComparatorOperation::less_equal;

        else if (name == "greater")
            operation =
                ComparatorOperation::greater;

        else if (name == "greater_equal")
            operation =
                ComparatorOperation::greater_equal;

        else if (name == "equal")
            operation =
                ComparatorOperation::equal;

        else if (name == "not_equal")
            operation =
                ComparatorOperation::not_equal;

        else
            throw std::runtime_error(
                "unknown comparator operation"
            );
    }
};

EASYKNACK_NODE(
    ComparatorNode,
    "comparator"
)

}