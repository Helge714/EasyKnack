#pragma once
#include "ParameterContracts.hpp"
#include "../TriggerInput.hpp"
namespace easyknack {
struct TriggerCounterNode : NodeProcessor {
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsTriggerFeedback()const noexcept override{return true;}
    ManualTriggerReceiver trigger,reset;
    std::uint32_t next=0,index=0;
    float count=16;
    bool wrapping=false;
    void bindIdentity(const std::string& id)override{trigger.bind(id+":trigger");reset.bind(id+":reset");}
    void shareChannelState(NodeProcessor& other)override{auto& p=static_cast<TriggerCounterNode&>(other);trigger=p.trigger;reset=p.reset;}
    void shareVoiceState(NodeProcessor& other)override{shareChannelState(other);}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 2;}
    bool writesEventBuffer(std::size_t port)const noexcept override{return port==1;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const TriggerCounterNode&>(other);count=p.count;wrapping=p.wrapping;}
    void configure(std::istream& in)override{auto p=parameters::TriggerCounterNodeParameters::read(in);sources=p.sources();count=p.count_value;wrapping=p.wrap_value!=0;}
    void inheritState(const NodeProcessor& other)noexcept override{
        auto* p=dynamic_cast<const TriggerCounterNode*>(&other);if(!p)return;
        next=p->next;index=p->index;trigger.seen=p->trigger.seen;reset.seen=p->reset.seen;
    }
    void process(ProcessContext& c)noexcept override{
        TriggerInput input(c,0,trigger),restart(c,1,reset);auto counts=c.numericInput(2,count);
        auto* events=c.eventOutput(1);auto* pulses=c.outputAt(1);
        if(events)events->initial=0;
        for(std::size_t i=0;i<c.bufferSize;++i){
            if(restart.at(i)){next=0;index=0;}
            bool fired=input.at(i);
            if(fired){
                auto limit=static_cast<std::uint32_t>(std::round(std::clamp(std::isfinite(counts[i])?counts[i]:count,1.f,16777216.f)));
                if(wrapping)next%=limit;
                index=next;
                // Keep every index exactly representable in the float signal format.
                next=wrapping?(next+1)%limit:std::min(next+1,std::uint32_t{16777215});
                if(events)events->push(static_cast<std::uint32_t>(c.sampleOffset+i),1,ValueEventKind::trigger);
            }
            c.output[i]=static_cast<float>(index);
            if(pulses)pulses[i]=fired?1:0;
        }
    }
};
EASYKNACK_NODE(TriggerCounterNode,"triggercounter")
}
