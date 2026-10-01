#pragma once
#include "../NodeProcessor.hpp"
#include "../ManualTriggers.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct TriggerInputNode : NodeProcessor {
    ManualTriggerReceiver trigger;
    void configure(std::istream& in)override{auto p=parameters::TriggerInputNodeParameters::read(in);sources=p.sources();}
    void bindIdentity(const std::string& id)override{trigger.bind(id+":trigger");}
    void shareChannelState(NodeProcessor& p)override{trigger=static_cast<TriggerInputNode&>(p).trigger;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    void inheritState(const NodeProcessor& p)noexcept override{trigger.seen=static_cast<const TriggerInputNode&>(p).trigger.seen;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    bool processesEventBlocks()const noexcept override{return true;}
    bool writesEventBuffers()const noexcept override{return true;}
    void processEventBlock(ProcessContext& c)noexcept override{
        auto count=trigger.take();if(auto* out=c.eventOutput()){
            out->initial=0;auto kept=std::min<std::uint64_t>(count,ValueEventBuffer::capacity);
            for(std::uint64_t i=0;i<kept;i++)out->push(0,1,ValueEventKind::trigger);
            out->dropped+=count-kept;
        }
    }
    void process(ProcessContext& c)noexcept override{std::fill_n(c.output,c.bufferSize,0.f);processEventBlock(c);}
};
EASYKNACK_NODE(TriggerInputNode,"triggerinput")
}
