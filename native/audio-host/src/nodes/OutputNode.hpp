#pragma once
#include "../NodeProcessor.hpp"
#include <cstddef>
#include <algorithm>

namespace easyknack {
struct OutputNode : NodeProcessor {
    bool canShareChannels() const noexcept override { return true; }
    void configure(std::istream& in) override { sources = {readInput(in)}; readNumber(in); }
    bool mergesVoices() const noexcept override { return true; }
    bool isSink() const noexcept override { return true; }
    void process(ProcessContext& context) noexcept override {
        std::fill_n(context.output, context.bufferSize, 0.f);
        for (std::size_t voice=0; voice<context.inputCount; ++voice)
            if (auto* input=context.input(voice))
                for (std::size_t i=0; i<context.bufferSize; ++i) context.output[i]+=input[i];
    }
    void process(const float* input, float* output, std::size_t bufferSize) noexcept {
        for (std::size_t i = 0; i < bufferSize; ++i) output[i] = input[i];
    }
};
EASYKNACK_NODE(OutputNode, "out")
}

