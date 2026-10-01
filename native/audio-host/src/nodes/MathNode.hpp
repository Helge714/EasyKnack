#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

#include <algorithm>
#include <cmath>
#include <cstddef>
#include <string>

namespace easyknack {

enum class MathOperation {

    // Arithmetic
    add,
    subtract,
    multiply,
    divide,
    power,

    // Remainder / wrapping
    remainder,
    modulo,

    // Range
    minimum,
    maximum,

    // Rounding
    floor,
    ceil,
    round,
    fract,

    // Powers / logarithms
    sqrt,
    root,
    exp,
    log,
    log_base,

    // Unary
    absolute,
    negate
};

struct MathNode : NodeProcessor {
    float sampleSmoothing=0;
    void prepare(double rate,std::size_t)override{sampleSmoothing=static_cast<float>(1-std::exp(-1/(.005*rate)));}
    bool supportsSampleFeedback()const noexcept override{return true;}


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
    float bValue = 1.f;

    float currentA = 0.f;
    float currentB = 1.f;

    MathOperation operation =
        MathOperation::multiply;

    bool clamp = false;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const MathNode&>(configuration);
        aValue=p.aValue;bValue=p.bValue;operation=p.operation;clamp=p.clamp;
    }
    void configure(std::istream& in) override {

        in >> std::ws;

        if (in.peek() == '!') {

            auto p =
                parameters::MathNodeParameters::read(in);

            sources =
                p.sources();

            aValue =
                static_cast<float>(p.a_value);

            bValue =
                static_cast<float>(p.b_value);

            currentA =
                aValue;

            currentB =
                bValue;

            clamp =
                p.clamp_value != 0;

            operation =
                parseOperation(
                    p.operation_value
                );

            if (
                isUnary(operation)
                && sources.size() > 1
                && sources[1] != -1
            ) {
                throw std::runtime_error(
                    "Unary operation has no B input"
                );
            }

            return;
        }

        sources = {
            readInput(in)
        };

        aValue =
            readNumber(
                in,
                -1000000.f,
                1000000.f
            );

        sources.push_back(
            readInput(in)
        );

        bValue =
            readNumber(
                in,
                -1000000.f,
                1000000.f
            );

        currentA =
            aValue;

        currentB =
            bValue;

        std::string name;
        int flag;

        if (
            !(in >> name >> flag)
            || (flag != 0 && flag != 1)
        ) {
            throw std::runtime_error(
                "invalid math parameters"
            );
        }

        clamp =
            flag != 0;

        operation =
            parseOperation(name);

        if (
            isUnary(operation)
            && sources[1] != -1
        ) {
            throw std::runtime_error(
                "Unary operation has no B input"
            );
        }
    }

    bool supportsScalarInputs()const noexcept override{return true;}
    void process(ProcessContext& c)noexcept override {
        processKernel(c,operation,clamp);
    }
    // Bake supplies compile-time operation/mode constants to this shared kernel.
    void processKernel(ProcessContext& c,MathOperation selectedOperation,bool selectedClamp)noexcept {
        const auto a=c.numericInput(0),b=c.numericInput(1);
        const double elapsed=!c.samplewise&&c.bufferSize==1?std::max(std::size_t{1},c.blockFrames):1;
        const float smoothing=c.samplewise?sampleSmoothing:c.legacyEventEvaluation?1.f:static_cast<float>(1-std::exp(-elapsed/(.005*c.sampleRate)));
        for(std::size_t i=0;i<c.bufferSize;i++){
            if(!a.data)currentA+=smoothing*(aValue-currentA);
            if(!b.data)currentB+=smoothing*(bValue-currentB);
            c.output[i]=evaluate(a.data?a[i]:currentA,b.data?b[i]:currentB,selectedOperation,selectedClamp);
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
            static_cast<const MathNode&>(
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
        MathOperation operation,
        bool clamp
    ) noexcept {

        float value = 0.f;

        switch (operation) {

        // ---------------- Arithmetic ----------------

        case MathOperation::add:
            value =
                a + b;
            break;

        case MathOperation::subtract:
            value =
                a - b;
            break;

        case MathOperation::multiply:
            value =
                a * b;
            break;

        case MathOperation::divide:

            value =
                std::abs(b) < 1e-12f
                    ? 0.f
                    : a / b;

            break;

        case MathOperation::power:

            value =
                std::pow(a, b);

            break;


        // ---------------- Remainder / wrapping ----------------

        case MathOperation::remainder:

            value =
                std::abs(b) < 1e-12f
                    ? 0.f
                    : std::fmod(a, b);

            break;

        case MathOperation::modulo:

            value =
                std::abs(b) < 1e-12f
                    ? 0.f
                    : a
                        - b
                        * std::floor(a / b);

            break;


        // ---------------- Range ----------------

        case MathOperation::minimum:

            value =
                std::min(a, b);

            break;

        case MathOperation::maximum:

            value =
                std::max(a, b);

            break;


        // ---------------- Rounding ----------------

        case MathOperation::floor:

            value =
                std::floor(a);

            break;

        case MathOperation::ceil:

            value =
                std::ceil(a);

            break;

        case MathOperation::round:

            value =
                std::round(a);

            break;

        case MathOperation::fract:

            value =
                a
                - std::floor(a);

            break;


        // ---------------- Powers / logarithms ----------------

        case MathOperation::sqrt:

            value =
                a >= 0.f
                    ? std::sqrt(a)
                    : 0.f;

            break;

        case MathOperation::root: {

            if (std::abs(b) < 1e-12f) {

                value = 0.f;
                break;
            }

            if (a >= 0.f) {

                value =
                    std::pow(
                        a,
                        1.f / b
                    );

                break;
            }

            /*
             * Negative real values only have a real result
             * for odd integer roots.
             */
            const float rounded =
                std::round(b);

            const bool integerDegree =
                std::abs(
                    b - rounded
                ) < 1e-6f;

            if (!integerDegree) {

                value = 0.f;
                break;
            }

            const auto degree =
                static_cast<long long>(
                    rounded
                );

            if ((degree & 1LL) == 0) {

                value = 0.f;
                break;
            }

            value =
                -std::pow(
                    -a,
                    1.f / rounded
                );

            break;
        }

        case MathOperation::exp:

            value =
                std::exp(a);

            break;

        case MathOperation::log:

            value =
                a > 0.f
                    ? std::log(a)
                    : 0.f;

            break;

        case MathOperation::log_base:

            value =
                (
                    a > 0.f
                    && b > 0.f
                    && std::abs(
                        b - 1.f
                    ) >= 1e-12f
                )
                    ? std::log(a)
                        / std::log(b)
                    : 0.f;

            break;


        // ---------------- Unary ----------------

        case MathOperation::absolute:

            value =
                std::abs(a);

            break;

        case MathOperation::negate:

            value =
                -a;

            break;
        }

        /*
         * Protect the graph from NaN / infinity produced by
         * operations such as invalid powers.
         */
        if (!std::isfinite(value))
            value = 0.f;

        return clamp
            ? std::clamp(
                value,
                -1.f,
                1.f
            )
            : value;
    }

private:

    static bool isUnary(
        MathOperation operation
    ) noexcept {

        switch (operation) {

        case MathOperation::floor:
        case MathOperation::ceil:
        case MathOperation::round:
        case MathOperation::fract:
        case MathOperation::sqrt:
        case MathOperation::exp:
        case MathOperation::log:
        case MathOperation::absolute:
        case MathOperation::negate:
            return true;

        default:
            return false;
        }
    }

    static MathOperation parseOperation(
        const std::string& name
    ) {

        // Arithmetic

        if (name == "add")
            return MathOperation::add;

        if (name == "subtract")
            return MathOperation::subtract;

        if (name == "multiply")
            return MathOperation::multiply;

        if (name == "divide")
            return MathOperation::divide;

        if (name == "power")
            return MathOperation::power;


        // Remainder / wrapping

        if (name == "remainder")
            return MathOperation::remainder;

        if (name == "modulo")
            return MathOperation::modulo;


        // Range

        if (name == "minimum")
            return MathOperation::minimum;

        if (name == "maximum")
            return MathOperation::maximum;


        // Rounding

        if (name == "floor")
            return MathOperation::floor;

        if (name == "ceil")
            return MathOperation::ceil;

        if (name == "round")
            return MathOperation::round;

        if (name == "fract")
            return MathOperation::fract;


        // Powers / logarithms

        if (name == "sqrt")
            return MathOperation::sqrt;

        if (name == "root")
            return MathOperation::root;

        if (name == "exp")
            return MathOperation::exp;

        if (name == "log")
            return MathOperation::log;

        if (name == "log_base")
            return MathOperation::log_base;


        // Unary

        if (name == "absolute")
            return MathOperation::absolute;

        if (name == "negate")
            return MathOperation::negate;

        throw std::runtime_error(
            "unknown math operation"
        );
    }
};

EASYKNACK_NODE(
    MathNode,
    "math"
)

}
