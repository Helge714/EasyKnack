#pragma once
#include "ParameterContracts.hpp"
#include "../TriggerInput.hpp"
namespace easyknack {
struct TriggerFilterNode : NodeProcessor {
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsTriggerFeedback()const noexcept override{return true;}
    ManualTriggerReceiver trigger;
    float condition=1;
    void bindIdentity(const std::string& id)override{trigger.bind(id+":trigger");}
    void shareChannelState(NodeProcessor& other)override{trigger=static_cast<TriggerFilterNode&>(other).trigger;}
    void shareVoiceState(NodeProcessor& other)override{shareChannelState(other);}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    bool processesEventBlocks()const noexcept override{return true;}
    bool writesEventBuffers()const noexcept override{return true;}
    char resolveAutoRate(const char*,std::size_t)noexcept override{return 'e';}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{condition=static_cast<const TriggerFilterNode&>(other).condition;}
    void configure(std::istream& in)override{auto p=parameters::TriggerFilterNodeParameters::read(in);sources=p.sources();condition=p.condition_value;}
    void inheritState(const NodeProcessor& other)noexcept override{if(auto* p=dynamic_cast<const TriggerFilterNode*>(&other))trigger.seen=p->trigger.seen;}
    template<class Emit> void run(ProcessContext& c,std::size_t frames,Emit emit)noexcept{
        auto allow=[&](std::uint32_t i){float v=c.valueAt(1,i,condition);if(std::isfinite(v)&&v>0)emit(i);};
        if(auto* events=c.events(0)){
            trigger.take();
            for(auto& e:*events)if(e.kind==ValueEventKind::trigger&&e.sampleOffset<frames)allow(e.sampleOffset);
        }else{
            TriggerInput input(c,0,trigger);
            if(!c.input(0)){if(frames&&input.at(0))allow(0);}
            else for(std::uint32_t i=0;i<frames;++i)if(input.at(i))allow(i);
        }
    }
    void processEventBlock(ProcessContext& c)noexcept override{
        auto* out=c.eventOutput();if(!out)return;out->initial=0;
        run(c,c.blockFrames,[&](std::uint32_t i){out->push(i,1,ValueEventKind::trigger);});
    }
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);auto* out=c.eventOutput();if(out)out->initial=0;
        run(c,c.bufferSize,[&](std::uint32_t i){c.output[i]=1;if(out)out->push(i,1,ValueEventKind::trigger);});
    }
    void processSample(ProcessContext& c)noexcept override{
        TriggerInput input(c,0,trigger);auto values=c.numericInput(1,condition);auto* out=c.eventOutput();
        for(std::size_t i=0;i<c.bufferSize;++i){
            bool fired=input.at(i)&&std::isfinite(values[i])&&values[i]>0;
            c.output[i]=fired?1:0;if(fired&&out)out->push(static_cast<std::uint32_t>(c.sampleOffset+i),1,ValueEventKind::trigger);
        }
    }
};
EASYKNACK_NODE(TriggerFilterNode,"triggerfilter")
}
