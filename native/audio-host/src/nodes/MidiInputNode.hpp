#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct MidiInputNode : NodeProcessor {
    std::uint64_t device=0;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsControlRate() const noexcept override{return true;}
    bool producesMidi() const noexcept override{return true;}
    std::uint64_t midiDevice() const noexcept override{return device;}
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::MidiInputNodeParameters::read(in);
        device=std::stoull(p.device_value);
        return;}
if(!(in>>device))throw std::runtime_error("invalid MIDI device");}
    void process(ProcessContext& c) noexcept override {
        if(c.output)std::fill_n(c.output,c.bufferSize,0.f);
        if(!c.midiOutput)return;
        const auto* events=c.midiSource?c.midiSource->events(device):nullptr;
        if(events)*c.midiOutput=*events;
        else{c.midiOutput->clear();c.midiOutput->source=device;c.midiOutput->panic=true;}
    }
};
EASYKNACK_NODE(MidiInputNode,"midiin")
}
