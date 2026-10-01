#pragma once
#include "ParameterContracts.hpp"
#include "../TriggerInput.hpp"
namespace easyknack {
struct TriggerTimerNode : NodeProcessor {
    ManualTriggerReceiver trigger;
    std::uint64_t elapsedFrames=0;
    double intervalFrames=0;
    bool seenTrigger=false,seenInterval=false;
    float smoothing=0;
    enum class Unit {second,ms,sample};Unit unit=Unit::second;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsTriggerFeedback()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 2;}
    void bindIdentity(const std::string& id)override{trigger.bind(id+":trigger");}
    void shareChannelState(NodeProcessor& p)override{trigger=static_cast<TriggerTimerNode&>(p).trigger;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const TriggerTimerNode&>(other);smoothing=p.smoothing;unit=p.unit;}
    void configure(std::istream& in)override{
        auto p=parameters::TriggerTimerNodeParameters::read(in);sources=p.sources();smoothing=p.smoothing_value;
        if(p.unit_value=="second")unit=Unit::second;else if(p.unit_value=="ms")unit=Unit::ms;else if(p.unit_value=="sample")unit=Unit::sample;else throw std::invalid_argument("Invalid timer unit");
    }
    void inheritState(const NodeProcessor& other)noexcept override{
        if(auto* p=dynamic_cast<const TriggerTimerNode*>(&other)){
            elapsedFrames=p->elapsedFrames;intervalFrames=p->intervalFrames;seenTrigger=p->seenTrigger;seenInterval=p->seenInterval;trigger.seen=p->trigger.seen;
        }
    }
    void process(ProcessContext& c)noexcept override{
        TriggerInput input(c,0,trigger);auto smooth=c.numericInput(1,smoothing);auto* interval=c.outputAt(1);
        const double scale=unit==Unit::sample?1.0:(unit==Unit::ms?1000.0:1.0)/c.sampleRate;
        for(std::size_t i=0;i<c.bufferSize;++i){
            if(input.at(i)){
                if(seenTrigger){
                    double retention=std::clamp(std::isfinite(smooth[i])?smooth[i]:smoothing,0.f,1.f);
                    intervalFrames=seenInterval?intervalFrames*retention+static_cast<double>(elapsedFrames)*(1-retention):static_cast<double>(elapsedFrames);
                    seenInterval=true;
                }
                seenTrigger=true;elapsedFrames=0;
            }
            c.output[i]=static_cast<float>(elapsedFrames*scale);
            if(interval)interval[i]=static_cast<float>(intervalFrames*scale);
            if(seenTrigger&&elapsedFrames<std::numeric_limits<std::uint64_t>::max())++elapsedFrames;
        }
    }
};
EASYKNACK_NODE(TriggerTimerNode,"triggertimer")
}
