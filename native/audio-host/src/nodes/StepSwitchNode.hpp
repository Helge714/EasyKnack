#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Analysis.hpp"
#include <array>
#include <algorithm>
namespace easyknack {
struct StepSwitchNode : NodeProcessor {
    static constexpr unsigned maximumSteps=32;
    unsigned steps=4,index=0;
    std::array<float,maximumSteps> values{};
    ManualTriggerReceiver next,reset;
    std::array<ManualTriggerReceiver,maximumSteps> selections;
    std::shared_ptr<AnalysisMailbox> mailbox;
    std::unique_ptr<AnalysisFrame> frame=std::make_unique<AnalysisFrame>();
    unsigned reportCountdown=0;
    std::size_t outputCount()const noexcept override{return 2;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t port)const noexcept override{return port<2;}
    char resolveAutoRate(const char* rates,std::size_t count)noexcept override{
        for(std::size_t i=0;i<count&&i<sources.size();i++)if(sources[i]>=0&&(i<2||rates[i]=='a'))return 'a';return 'c';
    }
    void bindIdentity(const std::string& id)override{next.bind(id+":next");reset.bind(id+":reset");mailbox=analysisMailbox(id);for(unsigned i=0;i<maximumSteps;i++)selections[i].bind(id+":select-"+std::to_string(i));}
    void shareChannelState(NodeProcessor& other)override{auto& p=static_cast<StepSwitchNode&>(other);next=p.next;reset=p.reset;selections=p.selections;}
    void shareVoiceState(NodeProcessor& other)override{shareChannelState(other);}
    void inheritState(const NodeProcessor& other)noexcept override{
        auto& p=static_cast<const StepSwitchNode&>(other);index=p.index<steps?p.index:0;next.seen=p.next.seen;reset.seen=p.reset.seen;for(unsigned i=0;i<maximumSteps;i++)selections[i].seen=p.selections[i].seen;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{
        auto& p=static_cast<const StepSwitchNode&>(other);steps=p.steps;values=p.values;if(index>=steps)index=0;
    }
    void configure(std::istream& in)override{
        auto p=parameters::StepSwitchNodeParameters::read(in);sources=p.sources();steps=static_cast<unsigned>(p.steps_value);
        if(steps<1||steps>maximumSteps)throw std::runtime_error("Invalid step count");
        values={static_cast<float>(p.step_0_value),
            static_cast<float>(p.step_1_value),
            static_cast<float>(p.step_2_value),
            static_cast<float>(p.step_3_value),
            static_cast<float>(p.step_4_value),
            static_cast<float>(p.step_5_value),
            static_cast<float>(p.step_6_value),
            static_cast<float>(p.step_7_value),
            static_cast<float>(p.step_8_value),
            static_cast<float>(p.step_9_value),
            static_cast<float>(p.step_10_value),
            static_cast<float>(p.step_11_value),
            static_cast<float>(p.step_12_value),
            static_cast<float>(p.step_13_value),
            static_cast<float>(p.step_14_value),
            static_cast<float>(p.step_15_value),
            static_cast<float>(p.step_16_value),
            static_cast<float>(p.step_17_value),
            static_cast<float>(p.step_18_value),
            static_cast<float>(p.step_19_value),
            static_cast<float>(p.step_20_value),
            static_cast<float>(p.step_21_value),
            static_cast<float>(p.step_22_value),
            static_cast<float>(p.step_23_value),
            static_cast<float>(p.step_24_value),
            static_cast<float>(p.step_25_value),
            static_cast<float>(p.step_26_value),
            static_cast<float>(p.step_27_value),
            static_cast<float>(p.step_28_value),
            static_cast<float>(p.step_29_value),
            static_cast<float>(p.step_30_value),
            static_cast<float>(p.step_31_value)};
    }
    struct Triggers {
        const ValueEventBuffer* events;
        ProcessContext::NumericInput data;
        std::size_t cursor=0,base;
        std::uint64_t manual=0;
        Triggers(ProcessContext& c,std::size_t port,ManualTriggerReceiver& receiver):events(c.events(port)),data(c.numericInput(port)),base(c.sampleOffset){
            auto pending=base==0?receiver.take():0;if(!events&&!data)manual=pending;
            if(events)while(cursor<events->count&&events->events[cursor].sampleOffset<base)++cursor;
        }
        std::uint64_t at(std::size_t i)noexcept{
            std::uint64_t count=i==0?manual:0;
            if(events){
                while(cursor<events->count&&events->events[cursor].sampleOffset<=base+i){
                    auto& e=events->events[cursor++];if(e.sampleOffset==base+i&&e.kind==ValueEventKind::trigger)++count;
                }
            }else if(data){auto value=data[i];if(std::isfinite(value)&&value!=0)++count;}
            return count;
        }
    };
    void process(ProcessContext& c)noexcept override{
        Triggers advance(c,0,next),restart(c,1,reset);
        const auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        auto* indexOutput=c.outputAt(1);
        int selected=-1;
        if(c.sampleOffset==0)for(unsigned i=0;i<maximumSteps;i++)if(selections[i].take()&&i<steps)selected=static_cast<int>(i);
        unsigned shown=index;
        // A Control evaluation emits the block-start value; later triggers prepare the next block.
        for(std::size_t i=0;i<frames;i++){
            auto n=advance.at(i);auto r=restart.at(i);
            if(r)index=0;else if(i==0&&selected>=0)index=static_cast<unsigned>(selected);else if(n)index=(index+static_cast<unsigned>(n%steps))%steps;
            if(i<c.bufferSize){
                float value=c.numericInput(2+index,values[index])[i];c.output[i]=std::isfinite(value)?value:0;
                if(indexOutput)indexOutput[i]=static_cast<float>(index);shown=index;
            }
        }
        if(mailbox&&c.channelIndex==0&&(reportCountdown--==0||frame->frequency!=shown)){
            frame->frequency=shown;frame->valueCount=1;frame->values[0]=static_cast<float>(shown);mailbox->publish(*frame);reportCountdown=7;
        }
    }
};
EASYKNACK_NODE(StepSwitchNode,"stepswitch")
}
