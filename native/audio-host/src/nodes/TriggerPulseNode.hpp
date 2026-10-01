#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct TriggerPulseNode:NodeProcessor {
    bool canShareChannels()const noexcept override{return true;}
    void configure(std::istream& in)override{sources=parameters::TriggerPulseNodeParameters::read(in).sources();}
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);
        if(auto* events=c.events(0))for(auto& e:*events)if(e.kind==ValueEventKind::trigger&&e.sampleOffset<c.bufferSize)c.output[e.sampleOffset]+=1;
    }
};
EASYKNACK_NODE(TriggerPulseNode,"triggerpulse")
}
