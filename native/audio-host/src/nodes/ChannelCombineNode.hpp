#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct ChannelCombineNode : NodeProcessor {
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::ChannelCombineNodeParameters::read(in);
        sources=p.sources();
        return;}
 sources={readInput(in),readInput(in)}; }
    void process(ProcessContext& c) noexcept override {
        std::copy_n(c.inputOrSilence(c.channelIndex),c.bufferSize,c.output);
    }
};
EASYKNACK_NODE(ChannelCombineNode,"channelcombine")
}
