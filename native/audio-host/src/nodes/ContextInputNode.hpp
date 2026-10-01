#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct ContextInputNode:NodeProcessor {
    void configure(std::istream& in)override{in>>std::ws;if(in.peek()=='!')parameters::ContextInputNodeParameters::read(in);}
    bool supportsControlRate()const noexcept override{return true;}
    // Context differs per channel; the graph propagates this requirement downstream.
    bool canShareChannels()const noexcept override{return false;}
    std::size_t outputCount()const noexcept override{return 3;}
    void process(ProcessContext& c)noexcept override{
        const float values[]={c.channelIndex==0?-1.f:1.f,static_cast<float>(c.sampleRate),
            static_cast<float>(c.blockFrames?c.blockFrames:c.bufferSize)};
        for(std::size_t port=0;port<3;port++)if(auto* output=c.outputAt(port))
            std::fill_n(output,c.bufferSize,values[port]);
    }
};
EASYKNACK_NODE(ContextInputNode,"context")
}
