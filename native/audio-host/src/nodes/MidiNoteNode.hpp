#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../OwnedArray.hpp"
#include <algorithm>
#include <array>
namespace easyknack {
/** Monophonic, last-note priority with held-note fallback and per-channel sustain. */
struct MidiNoteNode : NodeProcessor {
    struct Note{bool down=false,held=false;unsigned velocity=0;std::uint64_t order=0;float transpose=0;};
    OwnedArray<Note,16*128> notes;
    std::array<bool,16> pedal{};
    std::array<double,16> bend{};
    std::uint64_t serial=0,source=0,generation=0;
    int channel=0;
    std::size_t voiceCount=1,voiceIndex=0;
    std::array<int,32> assigned=[] {std::array<int,32> a; a.fill(-1); return a;}();
    std::size_t voices() const noexcept override{return voiceCount;}
    void selectVoice(std::size_t index) noexcept override{voiceIndex=index;}
    double bendRange=2,lastFrequency=440;
    float lastVelocity=0;int lastKey=-1;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsEventRate() const noexcept override{return true;}
    bool writesEventBuffer(std::size_t port)const noexcept override{return port==2;}
    bool acceptsMidi(std::size_t p) const noexcept override{return p==0;}
    std::size_t outputCount() const noexcept override{return 5;}
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::MidiNoteNodeParameters::read(in);
        sources=p.sources();channel=p.channel_value;bendRange=p.bend_range_value;voiceCount=p.voices_value;
        return;}

        sources={readInput(in)};channel=static_cast<int>(readNumber(in,0,16));bendRange=readNumber(in,0,48);
        in>>std::ws;if(in.peek()=='p'){std::string marker;in>>marker;if(marker!="poly")throw std::runtime_error("invalid polyphony");float count=readNumber(in,1,32);if(count!=std::floor(count))throw std::runtime_error("voice count must be integral");voiceCount=static_cast<std::size_t>(count);}
    }
    double frequency(int key) const noexcept {return 440*std::exp2(std::clamp(((key%128)-69+notes[key].transpose+bend[key/128]*bendRange)/12,-100.0,100.0));}
    void reset() noexcept {notes.fill({});pedal={};bend={};serial=0;lastFrequency=440;lastVelocity=0;lastKey=-1;assigned.fill(-1);}
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<MidiNoteNode&>(previous);if(channel!=p.channel||sources!=p.sources||voiceCount!=p.voiceCount||voiceIndex!=p.voiceIndex)return;
        notes.swap(p.notes);assigned=p.assigned;pedal=p.pedal;bend=p.bend;serial=p.serial;source=p.source;generation=p.generation;lastFrequency=p.lastFrequency;lastVelocity=p.lastVelocity;lastKey=p.lastKey;
    }
    void inheritState(const NodeProcessor& other) noexcept override {
        auto* p=dynamic_cast<const MidiNoteNode*>(&other);if(!p||channel!=p->channel||sources!=p->sources||voiceCount!=p->voiceCount||voiceIndex!=p->voiceIndex)return;
        assigned=p->assigned;
        notes=p->notes;pedal=p->pedal;bend=p->bend;serial=p->serial;source=p->source;generation=p->generation;lastFrequency=p->lastFrequency;lastVelocity=p->lastVelocity;lastKey=p->lastKey;
    }
    bool event(std::uint32_t message,float transpose=0) noexcept {
        int status=message&255,ch=status&15,a=(message>>8)&127,b=(message>>16)&127;
        if(status==255){reset();return false;}
        if(status>=240||channel!=0&&ch!=channel-1)return false;
        auto& note=notes[ch*128+a];
        if((status&240)==144&&b>0){
            int lane=-1;
            if(voiceCount>1){
                int key=ch*128+a;
                for(std::size_t v=0;v<voiceCount;v++)if(assigned[v]==key){lane=static_cast<int>(v);break;}
                if(lane<0)for(std::size_t v=0;v<voiceCount;v++)if(assigned[v]<0){lane=static_cast<int>(v);break;}
                if(lane<0)for(std::size_t v=0;v<voiceCount;v++)if(!notes[assigned[v]].held&&(lane<0||notes[assigned[v]].order<notes[assigned[lane]].order))lane=static_cast<int>(v);
                if(lane<0){lane=0;for(std::size_t v=1;v<voiceCount;v++)if(notes[assigned[v]].order<notes[assigned[lane]].order)lane=static_cast<int>(v);}
                assigned[lane]=key;
            }
            note={true,true,static_cast<unsigned>(b),++serial,transpose};return voiceCount==1||lane==static_cast<int>(voiceIndex);
        }
        if((status&240)==128||((status&240)==144&&b==0)){note.down=false;if(!pedal[ch])note.held=false;}
        if((status&240)==224)bend[ch]=((a+128*b)-8192)/8192.0;
        if((status&240)==176){
            if(a==64){pedal[ch]=b>=64;if(!pedal[ch])for(int n=0;n<128;n++)if(!notes[ch*128+n].down)notes[ch*128+n].held=false;}
            if(a==120||a==123)for(int n=0;n<128;n++){auto& v=notes[ch*128+n];v.down=false;if(a==120||!pedal[ch])v.held=false;}
            if(a==120&&lastKey>=0&&lastKey/128==ch)lastVelocity=0;
            if(a==121){pedal[ch]=false;bend[ch]=0;for(int n=0;n<128;n++)if(!notes[ch*128+n].down)notes[ch*128+n].held=false;}
        }
        return false;
    }
    void process(ProcessContext& c) noexcept override {
        const auto* block=c.midi(0);
        if(!block||block->panic||block->source!=source||block->generation!=generation)reset();
        if(block){source=block->source;generation=block->generation;}
        std::size_t eventIndex=0;int selected=-1;
        auto select=[&](){selected=-1;
            if(voiceCount>1){int key=assigned[voiceIndex];if(key>=0&&notes[key].held)selected=key;}
            else for(int i=0;i<16*128;i++)if(notes[i].held&&(selected<0||notes[i].order>notes[selected].order))selected=i;
            if(selected>=0){lastFrequency=frequency(selected);lastVelocity=notes[selected].velocity/127.f;lastKey=selected;}};
        select();
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool trigger=false,changed=false;
            while(block&&eventIndex<block->count&&block->events[eventIndex].offset<=i){const auto& e=block->events[eventIndex++];bool retrigger=event(e.message,e.transpose);if(retrigger&&c.eventOutput(2))c.eventOutput(2)->push(static_cast<std::uint32_t>(i),1,ValueEventKind::trigger);trigger=retrigger||trigger;changed=true;}
            if(changed)select();
            c.output[i]=static_cast<float>(lastFrequency);
            if(auto* gate=c.outputAt(1))gate[i]=selected>=0?1.f:0.f;
            if(auto* pulse=c.outputAt(2))pulse[i]=trigger?1.f:0.f;
            if(auto* velocity=c.outputAt(3))velocity[i]=lastVelocity;
            if(auto* voice=c.outputAt(4))voice[i]=static_cast<float>(voiceIndex);
        }
    }
};
EASYKNACK_NODE(MidiNoteNode,"midinote")
}
