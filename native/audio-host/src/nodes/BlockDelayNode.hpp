#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <array>
#include <algorithm>
namespace easyknack {
struct BlockDelayNode : NodeProcessor {
    std::array<float,256> history{};
    std::size_t cursor=0, delayFrames=256;
    float fallback=0;
    bool defersInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::BlockDelayNodeParameters::read(in);sources=p.sources();fallback=p.in_value;}
        else{sources={readInput(in)};fallback=readNumber(in);}
    }
    void prepare(double,std::size_t frames)override{if(!frames||frames>history.size())throw std::invalid_argument("Invalid block delay size");delayFrames=frames;cursor=0;history.fill(0);}
    void applyParameters(const NodeProcessor& p)noexcept override{fallback=static_cast<const BlockDelayNode&>(p).fallback;}
    void inheritState(const NodeProcessor& p)noexcept override{auto& old=static_cast<const BlockDelayNode&>(p);if(delayFrames==old.delayFrames){history=old.history;cursor=old.cursor;}}
    void process(ProcessContext& c)noexcept override{for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=history[(cursor+i)%delayFrames];}
    void commitBlock(ProcessContext& c)noexcept override{
        auto input=c.numericInput(0,fallback);
        for(std::size_t i=0;i<c.bufferSize;i++){float value=input[i];history[(cursor+i)%delayFrames]=std::isfinite(value)?value:0.f;}
        cursor=(cursor+c.bufferSize)%delayFrames;
    }
};
EASYKNACK_NODE(BlockDelayNode,"blockdelay")
}
