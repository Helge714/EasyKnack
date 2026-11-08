#pragma once
#include "NodeProcessor.hpp"
#include <array>
#include <bitset>
#include <memory>
#include <string>
#include <vector>
namespace easyknack {
constexpr int maxNodes = sourceNodeStride;
constexpr int blockSize = 256;
constexpr int sampleRate = 48000;
struct Node {
    std::string id;
    std::string processorId;
    std::string parameters;
    std::shared_ptr<const std::string> liveParameters;
    const std::string &parameterValues() const noexcept {
        return liveParameters ? *liveParameters : parameters;
    }
    int voiceDomain = -1;
    std::vector<Node> additionalVoices;
    Node &voice(std::size_t i) noexcept { return i == 0 ? *this : additionalVoices[i - 1]; }
    const Node &voice(std::size_t i) const noexcept {
        return i == 0 ? *this : additionalVoices[i - 1];
    }
    std::size_t voiceCount() const noexcept { return additionalVoices.size() + 1; }
    std::unique_ptr<NodeProcessor> processor;
    std::unique_ptr<NodeProcessor> rightProcessor;
    std::vector<const float *> inputs;
    std::vector<char> sourceRates;
    std::vector<const DspResource *> resourceInputs, rightResourceInputs;
    std::vector<std::array<const DspResource *, 2>> resourceHandles;
    std::array<std::vector<const DspResource *const *>, 2> resourceBindings;
    std::string inputRates, outputRates;
    std::vector<bool> denseOutputs;
    std::vector<ResourceKind> resourceKinds;
    char outputRate(std::size_t port) const noexcept {
        return port < outputRates.size() ? outputRates[port]
               : eventRate               ? 'e'
               : controlRate             ? 'c'
                                         : 'a';
    }
    std::size_t mergedVoiceCount = 1;
    bool controlRate = false;
    bool eventRate = false;
    bool stereo = true;
    bool active = false;
    std::vector<std::array<float, blockSize>> heldInputs;
    std::array<float, blockSize> buffer{};
    std::vector<std::array<float, blockSize>> extraBuffers;
    std::vector<float *> outputPointers;
    std::vector<const MidiBlock *> midiInputs;
    MidiBlock midi;
    std::vector<MidiBlock> extraMidi;
    std::vector<MidiBlock *> midiOutputPointers;
    MidiBlock &midiAt(std::size_t output) noexcept {
        return output == 0 ? midi : extraMidi[output - 1];
    }
    // Buffers materialize held event values for ordinary audio consumers. Masks retain
    // exact update positions without allocating event lists in the callback.
    std::vector<std::bitset<blockSize>> eventOffsets;
    std::array<std::vector<ValueEventBuffer>, 2> valueEvents;
    std::vector<const ValueEventBuffer *> valueEventInputs, rightValueEventInputs;
    std::vector<ValueEventBuffer *> valueEventOutputs;
    std::vector<std::array<float, 2>> eventValues, controlValues;
    std::vector<const float *> eventInputs;
    std::vector<float *> eventOutputs;
    std::array<bool, 2> eventReady{};
    std::array<std::uint64_t, 2> eventEvaluations{};
    // Allocated only for graphs that actually read across channels.
    struct CachedBuffer {
        std::shared_ptr<std::array<float, blockSize>> storage =
            std::make_shared<std::array<float, blockSize>>();
        float *data() noexcept { return storage->data(); }
        const float *data() const noexcept { return storage->data(); }
        float &operator[](std::size_t i) noexcept { return (*storage)[i]; }
        const float &operator[](std::size_t i) const noexcept { return (*storage)[i]; }
        void fill(float value) noexcept { storage->fill(value); }
    };
    struct ChannelCache {
        std::vector<CachedBuffer> outputs;
        std::vector<std::bitset<blockSize>> events;
        std::vector<MidiBlock> midi;
    };
    std::vector<ChannelCache> channelCache;
    std::vector<const float *> rightInputs;
    void allocateChannels(bool stereo) {
        rightInputs.resize(inputs.size());
        channelCache.resize(stereo ? 2u : 1u);
        for (std::size_t ch = 0; ch < channelCache.size(); ch++) {
            channelCache[ch].outputs.clear();
            channelCache[ch].outputs.resize(outputPointers.size());
            channelCache[ch].events.resize(outputPointers.size());
            channelCache[ch].midi.resize(outputPointers.size());
        }
    }
    void allocateEvents() {
        sourceRates.resize(inputs.size(), 'c');
        resourceInputs.resize(inputs.size());
        rightResourceInputs.resize(inputs.size());
        for (auto &channel : resourceBindings)
            channel.resize(inputs.size(), nullptr);
        resourceHandles.resize(outputPointers.size());
        resourceKinds.resize(outputPointers.size());
        for (std::size_t p = 0; p < resourceKinds.size(); p++)
            resourceKinds[p] = processor->outputResourceKind(p);
        valueEventInputs.resize(inputs.size());
        rightValueEventInputs.resize(inputs.size());
        valueEventOutputs.resize(outputPointers.size());
        for (auto &channel : valueEvents)
            channel.resize(outputPointers.size());
        eventOffsets.resize(outputPointers.size());
        eventValues.resize(outputPointers.size());
        controlValues.resize(inputs.size());
        eventInputs.resize(inputs.size());
        eventOutputs.resize(outputPointers.size());
    }
};

} // namespace easyknack
