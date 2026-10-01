#pragma once
#include <cmath>
#include <cstddef>
#include <istream>
#include <limits>
#include <memory>
#include <stdexcept>
#include <type_traits>
#include <vector>
#include "audio/AudioBackend.hpp"
#include "midi/MidiEvents.hpp"
#include "ValueEvents.hpp"
#include "ManualTriggers.hpp"
#include "DspResources.hpp"

namespace easyknack {
/** Borrowed buffers for one block. No allocation, ownership or node-specific parameters. */
struct ProcessContext {
    struct NumericInput {
        const float* data;std::size_t stride;float fallback;
        explicit operator bool()const noexcept{return data!=nullptr;}
        float operator[](std::size_t i)const noexcept{return data?data[i*stride]:fallback;}
    };
    NumericInput numericInput(std::size_t port,float fallback=0)const noexcept {
        return {input(port),sourceRates&&port<inputCount&&sourceRates[port]=='c'?std::size_t{0}:std::size_t{1},fallback};
    }
    const float* const* inputs;
    std::size_t inputCount;
    float* output;
    std::size_t bufferSize;
    double sampleRate;
    const float* silence;
    std::size_t blockFrames = 0; // Actual block length, even for a one-value control evaluation.
    AudioIO* audioIO = nullptr;
    std::size_t channelIndex = 0;
    float* const* outputs = nullptr;
    std::size_t outputCount = 1;
    const MidiBlock* const* midiInputs = nullptr;
    MidiBlock* midiOutput = nullptr;
    MidiSource* midiSource = nullptr;
    const float* const* rightInputs = nullptr; // Available to channel-separating processors.
    MidiBlock* const* midiOutputs = nullptr;
    const char* sourceRates = nullptr; // Actual connected source rates, independently of port sampling policy.
    const DspResource* const* resourceInputs=nullptr;
    const DspResource* const* rightResourceInputs=nullptr;
    std::size_t sampleOffset=0; // Absolute offset within this block, including sample-feedback evaluation.
    bool samplewise=false; // Audio feedback chunk (possibly one sample), not a control evaluation.
    const DspResource* resource(std::size_t port)const noexcept{return resourceInputs&&port<inputCount?resourceInputs[port]:nullptr;}
    bool eventRefresh=false;
    bool legacyEventEvaluation=false;
    std::uint64_t* eventEvaluationCount=nullptr;
    const ValueEventBuffer* const* valueEventInputs=nullptr;
    const ValueEventBuffer* const* rightValueEventInputs=nullptr;
    ValueEventBuffer* const* valueEventOutputs=nullptr;
    const ValueEventBuffer* events(std::size_t port)const noexcept{return valueEventInputs&&port<inputCount?valueEventInputs[port]:nullptr;}
    ValueEventBuffer* eventOutput(std::size_t port=0)const noexcept{return valueEventOutputs&&port<outputCount?valueEventOutputs[port]:nullptr;}
    float valueAt(std::size_t port,std::uint32_t offset,float fallback=0)const noexcept {
        if(auto* stream=events(port))return stream->valueAt(offset);
        auto* data=input(port);return data?data[sourceRates&&sourceRates[port]=='c'?0:offset]:fallback;
    }
    MidiBlock* midiOutputAt(std::size_t index) const noexcept { return midiOutputs&&index<outputCount?midiOutputs[index]:index==0?midiOutput:nullptr; }
    float* outputAt(std::size_t index) const noexcept { return index==0?output:index<outputCount&&outputs?outputs[index]:nullptr; }
    const MidiBlock* midi(std::size_t index) const noexcept {return midiInputs&&index<inputCount?midiInputs[index]:nullptr;}
    const float* input(std::size_t index) const noexcept { return index < inputCount ? inputs[index] : nullptr; }
    const float* inputOrSilence(std::size_t index) const noexcept { auto buffer = input(index); return buffer ? buffer : silence; }
};

/** Construct/configure on the control thread; process and inheritState on the audio thread. */
struct NodeProcessor {
    // Opt in to zero-stride control inputs instead of temporary held buffers.
    virtual bool supportsScalarInputs()const noexcept{return false;}
    // Source compatibility for older plugins. Connected signals are never smoothed.
    virtual bool smoothControlInput(std::size_t)const noexcept{return false;}
    std::vector<int> sources;
    const std::string defaultOutputEndpoint = "default";
    virtual ~NodeProcessor() = default;
    virtual void configure(std::istream& in) = 0;
    // Control thread only, before publication. blockFrames is the maximum block size.
    virtual void prepare(double sampleRate, std::size_t blockFrames) { }
    virtual char executionRate() const noexcept { return 0; }
    virtual ResourceKind inputResourceKind(std::size_t)const noexcept{return ResourceKind::none;}
    virtual ResourceKind outputResourceKind(std::size_t)const noexcept{return ResourceKind::none;}
    virtual DspResource* resourceOutput(std::size_t)noexcept{return nullptr;}
    virtual bool supportsLiveParameters() const noexcept { return false; }
    // Validated configuration only; must retain state and perform no allocation.
    virtual void applyParameters(const NodeProcessor&) noexcept { }
    // Move the old immutable parameter resource to a control-thread retirement slot.
    virtual std::shared_ptr<const void> takeParameterResource()noexcept{return {};}
    virtual void process(ProcessContext& context) noexcept = 0;
    // Feedback boundary: process reads retained state; inputs are consumed only
    // after all current-block processors have finished. No callback allocation.
    virtual bool defersInputs() const noexcept { return false; }
    virtual bool defersInput(std::size_t) const noexcept { return defersInputs(); }
    // Latch block-rate delay controls before selecting a feedback chunk size.
    virtual std::size_t minimumFeedbackFrames(double, std::size_t) const noexcept { return 1; }
    virtual std::size_t beginFeedbackBlock(ProcessContext&) noexcept { return 1; }
    virtual void commitBlock(ProcessContext&) noexcept { }
    virtual bool sampleFeedbackBoundary() const noexcept { return false; }
    virtual bool supportsSampleFeedback() const noexcept { return false; }
    virtual bool supportsTriggerFeedback() const noexcept { return false; }
    // Explicit opt-in only: audited processors share their existing DSP kernel.
    virtual void processSample(ProcessContext& context) noexcept { process(context); }
    virtual void commitSample(ProcessContext&) noexcept { }
    // Exclusive audio-thread handover. Large state may be exchanged in O(1);
    // old storage is reclaimed when the retired graph returns to the control thread.
    virtual void adoptState(NodeProcessor& previous) noexcept { inheritState(previous); }
    virtual void inheritState(const NodeProcessor&) noexcept { }
    virtual void bindIdentity(const std::string&) { }
    virtual bool isObserver() const noexcept { return false; }
    virtual bool isSink() const noexcept { return false; }
    virtual bool supportsControlRate() const noexcept { return false; }
    virtual bool supportsEventRate() const noexcept { return producesMidi(); }
    /** Pure numeric operation: the graph invokes process with one sample at each event time. */
    virtual bool evaluatesOnEvents() const noexcept { return false; }
    // Opt-in block event API; legacy processors use the dense compatibility adapter.
    virtual bool writesEventBuffers()const noexcept{return false;}
    virtual bool writesEventBuffer(std::size_t)const noexcept{return writesEventBuffers();}
    virtual bool processesEventBlocks() const noexcept { return evaluatesOnEvents(); }
    virtual void processEventBlock(ProcessContext& c) noexcept {
        if(c.inputCount>256||c.outputCount>8)return;
        std::array<std::uint32_t,256> cursors{};
        std::array<float,256> held{},sample{};
        std::array<const float*,256> inputs{};
        std::array<float,8> values{};std::array<float*,8> outputs{};
        for(std::size_t p=0;p<c.inputCount;p++)held[p]=c.events(p)?c.events(p)->initial:0;
        for(std::size_t p=0;p<c.outputCount;p++)outputs[p]=&values[p];
        bool force=c.eventRefresh;
        auto evaluate=[&](std::uint32_t offset,ValueEventKind kind,std::size_t triggerPort=256,float trigger=0){
            for(std::size_t p=0;p<c.inputCount;p++){
                sample[p]=p==triggerPort?trigger:c.events(p)?held[p]:c.valueAt(p,offset);
                inputs[p]=c.events(p)||c.input(p)?&sample[p]:nullptr;
            }
            auto event=c;event.inputs=inputs.data();event.output=values.data();event.outputs=outputs.data();event.bufferSize=1;event.legacyEventEvaluation=true;
            process(event);if(c.eventEvaluationCount)++*c.eventEvaluationCount;
            for(std::size_t p=0;p<c.outputCount;p++)if(auto* out=c.eventOutput(p))
                if(force||kind==ValueEventKind::trigger||values[p]!=out->valueAt(offset))out->push(offset,values[p],kind);
            force=false;
        };
        bool refresh=c.eventRefresh;
        while(true){
            std::size_t port=c.inputCount;std::uint32_t offset=static_cast<std::uint32_t>(c.blockFrames);
            for(std::size_t p=0;p<c.inputCount;p++)if(auto* stream=c.events(p);stream&&cursors[p]<stream->count)
                if(stream->events[cursors[p]].sampleOffset<offset){port=p;offset=stream->events[cursors[p]].sampleOffset;}
            if(refresh){refresh=false;if(offset>0)evaluate(0,ValueEventKind::value);}
            if(port==c.inputCount)break;
            // Apply one value per port simultaneously. Additional same-port events
            // at this offset form another ordered round; triggers on different inputs remain distinct.
            bool valueChange=false;
            for(std::size_t p=0;p<c.inputCount;p++)if(auto* stream=c.events(p);stream&&cursors[p]<stream->count){
                const auto& e=stream->events[cursors[p]];
                if(e.sampleOffset==offset&&e.kind==ValueEventKind::value){held[p]=e.value;++cursors[p];valueChange=true;}
            }
            if(valueChange)evaluate(offset,ValueEventKind::value);
            else {const auto& event=c.events(port)->events[cursors[port]++];evaluate(offset,event.kind,port,event.value);}
        }
    }
    virtual std::size_t outputCount() const noexcept { return 1; }
    virtual bool producesMidi() const noexcept { return false; }
    virtual bool acceptsMidi(std::size_t) const noexcept { return false; }
    virtual std::uint64_t midiDevice() const noexcept { return 0; }
    virtual std::size_t voices() const noexcept { return 1; }
    virtual void selectVoice(std::size_t) noexcept { }
    // Composite processors can render internal voices/channels once and return cached outputs.
    // Ordinary processors retain independent state and use these no-op defaults.
    virtual void shareVoiceState(NodeProcessor&) { }
    virtual void shareChannelState(NodeProcessor&) { }
    virtual bool outputIsMidi(std::size_t) const noexcept { return producesMidi(); }
    virtual char resolveAutoRate(const char* rates,std::size_t count) noexcept {
        for(std::size_t i=0;i<count;i++)if(rates[i]=='a')return 'a';return 'c';
    }
    virtual void collectMidiDevices(std::vector<std::uint64_t>& devices) const {
        if(auto device=midiDevice())devices.push_back(device);
    }
    virtual bool mergesVoices() const noexcept { return false; }
    /** Opt in only when equal inputs and state produce equal channels (no channel-local IO/randomness).
     * Unknown/plugin processors conservatively run separately for both channels. */
    /** Reads both source channels and emits channel-independent mono outputs. */
    virtual bool separatesChannels() const noexcept { return false; }
    virtual bool canShareChannels() const noexcept { return false; }
    /** Block data (e.g. a wavetable) must not be reduced to its first scalar sample. */
    virtual bool inputUsesBlockData(std::size_t) const noexcept { return false; }
    virtual std::string inputEndpoint() const { return {}; }
    virtual const std::string& outputEndpoint() const noexcept { return defaultOutputEndpoint; }
protected:
    static int readInput(std::istream& in) {
        int index; if (!(in >> index)) throw std::runtime_error("invalid input index"); return index;
    }
    static float readNumber(std::istream& in, float min = -std::numeric_limits<float>::max(),
            float max = std::numeric_limits<float>::max()) {
        float value;
        if (!(in >> value) || !std::isfinite(value) || value < min || value > max)
            throw std::runtime_error("invalid node parameter");
        return value;
    }
};
}
// The build scans these declarations and generates the factory. No static initialization side effects.
#define EASYKNACK_NODE(TYPE, ID) static_assert(std::is_base_of_v<::easyknack::NodeProcessor, TYPE>, "NodeProcessor required");
