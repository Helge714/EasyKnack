#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct MidiCCNode : NodeProcessor {
    int channel=0,controller=1;float value=0;
    std::uint64_t source=0,generation=0;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsEventRate() const noexcept override{return true;}
    bool acceptsMidi(std::size_t p) const noexcept override{return p==0;}
    void configure(std::istream& in) override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::MidiCCNodeParameters::read(in);
        sources=p.sources();channel=p.channel_value;controller=p.controller_value;
        return;}
sources={readInput(in)};channel=static_cast<int>(readNumber(in,0,16));controller=static_cast<int>(readNumber(in,0,127));}
    void inheritState(const NodeProcessor& other) noexcept override{
        auto* p=dynamic_cast<const MidiCCNode*>(&other);if(p&&channel==p->channel&&controller==p->controller&&sources==p->sources){value=p->value;source=p->source;generation=p->generation;}
    }
    void process(ProcessContext& c) noexcept override{
        const auto* block=c.midi(0);
        if(!block||block->panic||block->source!=source||block->generation!=generation)value=0;
        if(block){source=block->source;generation=block->generation;}
        std::size_t index=0;
        for(std::size_t i=0;i<c.bufferSize;i++){
            while(block&&index<block->count&&block->events[index].offset<=i){
                auto msg=block->events[index++].message;int status=msg&255,ch=status&15,a=(msg>>8)&127,b=(msg>>16)&127;
                if(status==255)value=0;
                if((status&240)==176&&(channel==0||ch==channel-1)){if(a==121)value=0;else if(a==controller)value=b/127.f;}
            }
            c.output[i]=value;
        }
    }
};
EASYKNACK_NODE(MidiCCNode,"midicc")
}
