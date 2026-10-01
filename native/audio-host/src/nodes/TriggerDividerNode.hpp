#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../TriggerInput.hpp"
#include <algorithm>
namespace easyknack {
struct TriggerDividerNode : NodeProcessor {
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsTriggerFeedback()const noexcept override{return true;}
    float divisor=2;
    unsigned count=0;
    bool previousTrigger=false,previousReset=false;
    ManualTriggerReceiver trigger,reset;
    void bindIdentity(const std::string& id)override{trigger.bind(id+":trigger");reset.bind(id+":reset");}
    void shareChannelState(NodeProcessor& other)override{auto& p=static_cast<TriggerDividerNode&>(other);trigger=p.trigger;reset=p.reset;}
    void shareVoiceState(NodeProcessor& other)override{shareChannelState(other);}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    bool evaluatesOnEvents()const noexcept override{return true;}
    bool processesEventBlocks()const noexcept override{return true;}
    char resolveAutoRate(const char*,std::size_t)noexcept override{return 'e';}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{divisor=static_cast<const TriggerDividerNode&>(other).divisor;}
    void configure(std::istream& in)override{auto p=parameters::TriggerDividerNodeParameters::read(in);sources=p.sources();divisor=p.divisor_value;}
    void inheritState(const NodeProcessor& other)noexcept override{
        auto* p=dynamic_cast<const TriggerDividerNode*>(&other);if(!p)return;
        count=p->count;previousTrigger=p->previousTrigger;previousReset=p->previousReset;trigger.seen=p->trigger.seen;reset.seen=p->reset.seen;
    }
    bool advance(float value)noexcept{
        unsigned division=static_cast<unsigned>(std::round(std::clamp(std::isfinite(value)?value:divisor,1.f,1024.f)));
        if(++count>=division){count=0;return true;}return false;
    }
    template<class Emit> void run(ProcessContext& c,std::size_t frames,Emit emit)noexcept{
        auto* in=c.events(0);auto* rst=c.events(2);
        auto clicks=trigger.take(),resets=reset.take();
        bool manualReset=resets&&!rst&&!c.input(2);
        auto manualClicks=(!in&&!c.input(0))?std::min<std::uint64_t>(clicks,ValueEventBuffer::capacity):0;
        bool pendingManual=manualReset||manualClicks;
        std::size_t a=0,b=0;
        // Sparse event paths visit only event timestamps; dense legacy inputs visit samples.
        const bool dense=(!in&&c.input(0))||(!rst&&c.input(2));
        std::uint32_t i=0;
        while(i<frames){
            if(!dense){i=pendingManual?0:static_cast<std::uint32_t>(frames);if(in&&a<in->count)i=std::min(i,in->events[a].sampleOffset);if(rst&&b<rst->count)i=std::min(i,rst->events[b].sampleOffset);if(i>=frames)break;}
            if(i==0&&manualReset)count=0;
            if(rst){while(b<rst->count&&rst->events[b].sampleOffset==i){if(rst->events[b++].kind==ValueEventKind::trigger)count=0;}}
            else {bool now=c.valueAt(2,i)>0;if(now&&!previousReset)count=0;previousReset=now;}
            if(i==0)for(std::uint64_t n=0;n<manualClicks;++n)if(advance(c.valueAt(1,0,divisor)))emit(0);
            pendingManual=false;
            if(in){while(a<in->count&&in->events[a].sampleOffset==i){if(in->events[a++].kind==ValueEventKind::trigger&&advance(c.valueAt(1,i,divisor)))emit(i);}}
            else {bool now=c.valueAt(0,i)>0;if(now&&!previousTrigger&&advance(c.valueAt(1,i,divisor)))emit(i);previousTrigger=now;}
            ++i;
        }
    }
    void processEventBlock(ProcessContext& c)noexcept override{
        auto* out=c.eventOutput();if(!out)return;out->initial=0;
        run(c,c.blockFrames,[&](std::uint32_t i){out->push(i,1,ValueEventKind::trigger);});
    }
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);run(c,c.bufferSize,[&](std::uint32_t i){c.output[i]=1;});
    }
    void processSample(ProcessContext& c)noexcept override{
        TriggerInput input(c,0,trigger),restart(c,2,reset);auto divisions=c.numericInput(1,divisor);auto* out=c.eventOutput();
        for(std::size_t i=0;i<c.bufferSize;++i){
            if(restart.at(i))count=0;
            bool fired=input.at(i)&&advance(divisions[i]);
            c.output[i]=fired?1:0;if(fired&&out)out->push(static_cast<std::uint32_t>(c.sampleOffset+i),1,ValueEventKind::trigger);
        }
    }
};
EASYKNACK_NODE(TriggerDividerNode,"triggerdivider")
}
