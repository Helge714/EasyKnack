#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct ChannelSeparateNode : NodeProcessor {
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::ChannelSeparateNodeParameters::read(in);
        sources=p.sources();
        return;}
 sources={readInput(in)}; }
    bool canShareChannels() const noexcept override { return true; }
    bool separatesChannels() const noexcept override { return true; }
    std::size_t outputCount() const noexcept override { return 2; }
    void process(ProcessContext& c) noexcept override {
        std::copy_n(c.inputOrSilence(0),c.bufferSize,c.output);
        const float* right=c.rightInputs?c.rightInputs[0]:nullptr;
        std::copy_n(right?right:c.silence,c.bufferSize,c.outputAt(1));
    }
};
EASYKNACK_NODE(ChannelSeparateNode,"channelseparate")
}
