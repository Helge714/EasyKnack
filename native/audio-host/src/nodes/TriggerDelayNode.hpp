#pragma once
#include "ParameterContracts.hpp"
#include "../TriggerInput.hpp"
#include <vector>
namespace easyknack {
/** Bounded sample calendar: one pending trigger per due sample, no render allocations. */
struct TriggerDelayNode : NodeProcessor {
    ManualTriggerReceiver trigger,reset;
    std::vector<std::uint32_t> calendar;
    std::uint32_t generation=1;
    std::size_t cursor=0,latchedDelay=1,lastReset=0;
    bool resetInChunk=false;
    double preparedRate=0;
    float time=.25f;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    bool processesEventBlocks()const noexcept override{return true;}
    bool writesEventBuffers()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsTriggerFeedback()const noexcept override{return true;}
    bool sampleFeedbackBoundary()const noexcept override{return true;}
    bool defersInputs()const noexcept override{return true;}
    bool defersInput(std::size_t p)const noexcept override{return p==0;}
    char resolveAutoRate(const char*,std::size_t)noexcept override{return 'e';}
    void bindIdentity(const std::string& id)override{trigger.bind(id+":trigger");reset.bind(id+":reset");}
    void shareChannelState(NodeProcessor& other)override{auto& p=static_cast<TriggerDelayNode&>(other);trigger=p.trigger;reset=p.reset;}
    void shareVoiceState(NodeProcessor& other)override{shareChannelState(other);}
    void configure(std::istream& in)override{auto p=parameters::TriggerDelayNodeParameters::read(in);sources=p.sources();time=p.time_value;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{time=static_cast<const TriggerDelayNode&>(other).time;}
    void prepare(double rate,std::size_t frames)override{
        if(!std::isfinite(rate)||rate<=0||!frames)throw std::invalid_argument("Invalid trigger delay format");
        if(rate==preparedRate&&!calendar.empty())return;
        calendar.assign(static_cast<std::size_t>(std::ceil(rate*10))+1,0);preparedRate=rate;cursor=0;generation=1;
    }
    void copyState(const TriggerDelayNode& p)noexcept{cursor=p.cursor;generation=p.generation;trigger.seen=p.trigger.seen;reset.seen=p.reset.seen;}
    void adoptState(NodeProcessor& other)noexcept override{auto& p=static_cast<TriggerDelayNode&>(other);if(preparedRate==p.preparedRate&&calendar.size()==p.calendar.size()){calendar.swap(p.calendar);copyState(p);}}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const TriggerDelayNode&>(other);if(preparedRate==p.preparedRate&&calendar.size()==p.calendar.size()){std::copy(p.calendar.begin(),p.calendar.end(),calendar.begin());copyState(p);}}
    std::size_t delay(ProcessContext& c)const noexcept{
        double v=c.numericInput(1,time)[0];if(!std::isfinite(v))v=time;
        return static_cast<std::size_t>(std::clamp(std::round(std::clamp(v,0.0,10.0)*preparedRate),1.0,double(calendar.size()-1)));
    }
    // Trigger feedback always uses the audited chunk path, even for long delays.
    std::size_t minimumFeedbackFrames(double,std::size_t)const noexcept override{return 1;}
    std::size_t beginFeedbackBlock(ProcessContext& c)noexcept override{latchedDelay=delay(c);return latchedDelay;}
    void clearPending()noexcept{if(++generation==0){std::fill(calendar.begin(),calendar.end(),0);generation=1;}}
    bool read(std::size_t offset)noexcept{auto& slot=calendar[(cursor+offset)%calendar.size()];bool due=slot==generation;slot=0;return due;}
    void schedule(std::size_t offset,std::size_t distance)noexcept{calendar[(cursor+offset+distance)%calendar.size()]=generation;}
    void emit(ProcessContext& c,std::size_t i,bool fired)noexcept{
        if(c.output)c.output[i]=fired?1:0;
        if(fired)if(auto* events=c.eventOutput())events->push(static_cast<std::uint32_t>(c.sampleOffset+i),1,ValueEventKind::trigger);
    }
    void process(ProcessContext& c)noexcept override{
        if(calendar.empty())return;
        TriggerInput input(c,0,trigger),restart(c,2,reset);auto distance=delay(c);
        if(auto* out=c.eventOutput())out->initial=0;
        for(std::size_t i=0;i<c.bufferSize;++i){
            if(restart.at(i))clearPending();
            bool fired=read(0);
            if(input.at(i))schedule(0,distance);
            emit(c,i,fired);cursor=(cursor+1)%calendar.size();
        }
    }
    void processEventBlock(ProcessContext& c)noexcept override{process(c);}
    void processSample(ProcessContext& c)noexcept override{
        TriggerInput restart(c,2,reset);resetInChunk=false;
        if(auto* out=c.eventOutput())out->initial=0;
        for(std::size_t i=0;i<c.bufferSize;++i){
            if(restart.at(i)){clearPending();lastReset=i;resetInChunk=true;}
            emit(c,i,read(i));
        }
    }
    void commitSample(ProcessContext& c)noexcept override{
        TriggerInput input(c,0,trigger);
        for(std::size_t i=0;i<c.bufferSize;++i){
            bool fired=input.at(i);
            if(fired&&(!resetInChunk||i>=lastReset))schedule(i,latchedDelay);
        }
        cursor=(cursor+c.bufferSize)%calendar.size();
    }
};
EASYKNACK_NODE(TriggerDelayNode,"triggerdelay")
}
