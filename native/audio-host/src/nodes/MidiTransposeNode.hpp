#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct MidiTransposeNode : NodeProcessor {
    float transpose=0;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsControlRate() const noexcept override{return true;}
    bool producesMidi() const noexcept override{return true;}
    bool acceptsMidi(std::size_t p) const noexcept override{return p==0;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const MidiTransposeNode&>(configuration);
        transpose=p.transpose;
    }
    void configure(std::istream& in) override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::MidiTransposeNodeParameters::read(in);
        sources=p.sources();transpose=p.transpose_value;
        return;}
sources={readInput(in)};transpose=readNumber(in,-48,48);}
    void process(ProcessContext& c) noexcept override{
        c.output[0]=0;
        auto* out=c.midiOutput;if(!out)return;
        auto* input=c.midi(0);if(!input){out->clear();out->panic=true;return;}
        *out=*input;
        // Preserve note identity: tuning is carried separately from the MIDI key.
        for(std::size_t i=0;i<out->count;i++)out->events[i].transpose+=transpose;
    }
};
EASYKNACK_NODE(MidiTransposeNode,"miditranspose")
}
