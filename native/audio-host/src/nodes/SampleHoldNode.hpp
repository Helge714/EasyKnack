#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

#include <cmath>
#include <cstddef>

namespace easyknack {

struct SampleHoldNode : NodeProcessor {
    char resolveAutoRate(const char* rates,std::size_t count)noexcept override{return sources.size()>1&&sources[1]>=0?'a':NodeProcessor::resolveAutoRate(rates,count);}
    ManualTriggerReceiver manual;
    void bindIdentity(const std::string& id)override{manual.bind(id+":trigger");}
    void shareChannelState(NodeProcessor& p)override{manual=static_cast<SampleHoldNode&>(p).manual;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}


    bool canShareChannels() const noexcept override {
        return true;
    }

    bool supportsControlRate() const noexcept override {
        return true;
    }

    bool supportsEventRate() const noexcept override {
        return true;
    }

    bool evaluatesOnEvents() const noexcept override {
        return true;
    }

    float inputValue = 0.f;
    float triggerValue = 0.f;

    float heldValue = 0.f;
    float previousTrigger = 0.f;

    static constexpr float triggerThreshold = 0.5f;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const SampleHoldNode&>(configuration);
        inputValue=p.inputValue;triggerValue=p.triggerValue;
    }
    void configure(std::istream& in) override {

        in >> std::ws;

        if (in.peek() == '!') {

            auto p =
                parameters::SampleHoldNodeParameters::read(in);

            sources = p.sources();

            inputValue = p.audio_in_value;
            triggerValue = p.trigger_value;

            return;
        }

        sources = {
            readInput(in)
        };

        inputValue =
            readNumber(in, -1000000, 1000000);

        sources.push_back(
            readInput(in)
        );

        triggerValue =
            readNumber(in, 0, 1);
    }

    bool processesEventBlocks()const noexcept override{return true;}
    void processEventBlock(ProcessContext& context)noexcept override {
        auto* out=context.eventOutput();if(!out)return;out->initial=heldValue;
        if(auto* triggers=context.events(1))for(const auto& event:*triggers){
            if(event.kind==ValueEventKind::trigger||risingEdge(event.value)){
                heldValue=safeValue(context.valueAt(0,event.sampleOffset,inputValue));out->push(event.sampleOffset,heldValue);
            }
            if(event.kind==ValueEventKind::value)previousTrigger=event.value;
        }
        else if(!context.sourceRates||context.sourceRates[1]=='c') {
            float trigger=context.valueAt(1,0,triggerValue);
            if(risingEdge(trigger)){heldValue=safeValue(context.valueAt(0,0,inputValue));out->push(0,heldValue);}
            previousTrigger=trigger;
        } else for(std::uint32_t i=0;i<context.blockFrames;i++){
            float trigger=context.valueAt(1,i,triggerValue);
            if(risingEdge(trigger)){heldValue=safeValue(context.valueAt(0,i,inputValue));out->push(i,heldValue);}
            previousTrigger=trigger;
        }
    }

    bool supportsScalarInputs()const noexcept override{return true;}
    void process(ProcessContext& c)noexcept override {
        const auto input=c.numericInput(0,inputValue),trigger=c.numericInput(1,triggerValue);
        auto* events=c.events(1);std::size_t eventIndex=0;const auto clicks=manual.take();
        const auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        for(std::size_t i=0;i<frames;i++){
            bool sample=i==0&&clicks&&!trigger.data&&!events;
            if(events)while(eventIndex<events->count&&events->events[eventIndex].sampleOffset==i){
                auto& event=events->events[eventIndex++];sample|=event.kind==ValueEventKind::trigger;
                if(event.kind==ValueEventKind::value){sample|=risingEdge(event.value);previousTrigger=event.value;}
            }
            else{const float value=trigger[i];sample|=risingEdge(value);previousTrigger=value;}
            if(sample)heldValue=safeValue(input[i]);
            if(i<c.bufferSize)c.output[i]=heldValue;
        }
    }

    void processExport(
        ProcessContext& context
    ) noexcept {

        process(context);
    }

    void inheritState(
        const NodeProcessor& previous
    ) noexcept override {

        const auto& old =
            static_cast<const SampleHoldNode&>(
                previous
            );

        manual.seen=old.manual.seen;
        heldValue =
            old.heldValue;

        previousTrigger =
            old.previousTrigger;
    }

private:

    bool risingEdge(
        float trigger
    ) const noexcept {

        return
            previousTrigger <= triggerThreshold
            && trigger > triggerThreshold;
    }

    static float safeValue(
        float value
    ) noexcept {

        return std::isfinite(value)
            ? value
            : 0.f;
    }
};

EASYKNACK_NODE(
    SampleHoldNode,
    "samplehold"
)

}