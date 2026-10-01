#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../OwnedArray.hpp"
#include <algorithm>
#include <array>
namespace easyknack {
struct MidiDelayNode : NodeProcessor {
    struct Pending {std::uint64_t due=0;MidiEvent event;};
    OwnedArray<Pending,4096> queue;
    std::size_t head=0,count=0;
    std::uint64_t clock=0,lastDue=0,source=0,generation=0;
    double milliseconds=100;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsControlRate() const noexcept override{return true;}
    bool producesMidi() const noexcept override{return true;}
    bool acceptsMidi(std::size_t p) const noexcept override{return p==0;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const MidiDelayNode&>(configuration);
        milliseconds=p.milliseconds;
    }
    void configure(std::istream& in) override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::MidiDelayNodeParameters::read(in);
        sources=p.sources();milliseconds=p.delay_value;
        return;}
sources={readInput(in)};milliseconds=readNumber(in,0,10000);}
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<MidiDelayNode&>(previous);if(sources!=p.sources)return;
        queue.swap(p.queue);head=p.head;count=p.count;clock=p.clock;lastDue=p.lastDue;source=p.source;generation=p.generation;
    }
    void inheritState(const NodeProcessor& other) noexcept override{
        auto* old=dynamic_cast<const MidiDelayNode*>(&other);if(!old||sources!=old->sources)return;
        queue=old->queue;head=old->head;count=old->count;clock=old->clock;lastDue=old->lastDue;source=old->source;generation=old->generation;
    }
    void process(ProcessContext& c) noexcept override{
        c.output[0]=0;auto* out=c.midiOutput;if(!out)return;out->clear();
        auto* input=c.midi(0);
        if(!input||input->panic||source!=input->source||generation!=input->generation){count=0;head=0;lastDue=clock;out->panic=true;}
        if(input){
            source=input->source;generation=input->generation;
            for(std::size_t i=0;i<input->count;i++){
                if(count==queue.size()){count=0;head=0;out->panic=true;break;}
                const auto& event=input->events[i];
                // Never move NoteOff ahead of its NoteOn when the delay is reduced.
                lastDue=std::max(lastDue,clock+event.offset+static_cast<std::uint64_t>(std::llround(milliseconds*c.sampleRate/1000)));
                queue[(head+count++)%queue.size()]={lastDue,event};
            }
        }
        out->source=source;out->generation=generation;
        auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        while(count&&queue[head].due<clock+frames){
            if(out->count==MidiBlock::capacity){out->count=0;out->panic=true;count=0;break;}
            auto pending=queue[head];head=(head+1)%queue.size();--count;
            pending.event.offset=static_cast<std::uint32_t>(pending.due>clock?pending.due-clock:0);
            out->events[out->count++]=pending.event;
        }
        clock+=frames;
    }
};
EASYKNACK_NODE(MidiDelayNode,"mididelay")
}
