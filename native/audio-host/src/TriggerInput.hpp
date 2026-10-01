#pragma once
#include "NodeProcessor.hpp"
namespace easyknack {
/** One borrowed trigger input span; offsets remain absolute in feedback chunks. */
struct TriggerInput {
    const ValueEventBuffer* events;ProcessContext::NumericInput input;std::size_t base,cursor=0;bool manual=false;
    TriggerInput(ProcessContext& c,std::size_t p,ManualTriggerReceiver& receiver)noexcept:events(c.events(p)),input(c.numericInput(p)),base(c.sampleOffset){
        auto count=base==0?receiver.take():0;manual=count&&!input&&!events;
        if(events)while(cursor<events->count&&events->events[cursor].sampleOffset<base)++cursor;
    }
    bool active()const noexcept{return manual||events||bool(input);}
    bool at(std::size_t i)noexcept{
        bool result=manual&&i==0;
        if(events){
            auto offset=base+i;
            while(cursor<events->count&&events->events[cursor].sampleOffset<=offset){
                auto& event=events->events[cursor++];
                result|=event.sampleOffset==offset&&event.kind==ValueEventKind::trigger;
            }
        }else if(input){auto value=input[i];result|=std::isfinite(value)&&value!=0;}
        return result;
    }
};
}
