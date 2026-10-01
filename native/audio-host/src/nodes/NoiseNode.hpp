#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../NoiseSharing.hpp"

#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstddef>
#include <cstdint>
#include <string>

namespace easyknack {

enum class NoiseMode {
    white,
    pink,
    brown,
    blue
};

struct NoiseNode : NodeProcessor {
    bool supportsSampleFeedback() const noexcept override { return true; }

    bool canShareChannels() const noexcept override {
        return true;
    }

    bool supportsControlRate() const noexcept override {
        return true;
    }

    bool supportsEventRate() const noexcept override {
        return false;
    }

    bool evaluatesOnEvents() const noexcept override {
        return false;
    }

    NoiseMode mode = NoiseMode::white;
    bool supportsScalarInputs()const noexcept override{return true;}
    int seedValue=-1;std::uint32_t automaticSeed=1,activeStateSeed=0;
    std::shared_ptr<NoiseBlockCache> instanceCache;
    void bindIdentity(const std::string& id)override{automaticSeed=noiseIdentitySeed(id);}
    void shareChannelState(NodeProcessor& p)override{automaticSeed=static_cast<NoiseNode&>(p).automaticSeed;}

    /*
     * Standalone, unbound processors receive distinct temporary seeds.
     * Graph construction replaces these with the logical node's identity seed;
     * its channel/instance copies then start from the same sequence.
     *
     * The atomic counter is only accessed while constructing nodes,
     * never inside the realtime processing path.
     */
    inline static std::atomic<std::uint32_t> seedCounter {
        0x9E3779B9u
    };

    std::uint32_t randomState;

    // Filter/state variables for coloured noise.
    float pink0 = 0.f;
    float pink1 = 0.f;
    float pink2 = 0.f;

    float brownState = 0.f;

    float previousWhite = 0.f;

    NoiseNode() noexcept
        : randomState(nextSeed()) {automaticSeed=randomState;
    }

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const NoiseNode&>(configuration);
        mode=p.mode;seedValue=p.seedValue;
    }
    void configure(std::istream& in) override {

        auto position=in.tellg();std::string marker;in>>marker;
        if(marker=="!742852c54323d505"){std::string name;in>>name;setMode(name);sources={};return;}
        in.clear();in.seekg(position);

        in >> std::ws;

        if (in.peek() == '!') {

            auto p = parameters::NoiseNodeParameters::read(in);

            sources = p.sources();
            seedValue=p.seed_value;

            setMode(p.mode_value);

            return;
        }

        std::string name;

        if (!(in >> name))
            throw std::runtime_error("invalid noise parameters");

        setMode(name);
    }

    void process(ProcessContext& context) noexcept override {
        int selected=noiseSeedValue(context.numericInput(0,float(seedValue))[0]);
        auto seed=selected<0?automaticSeed:noiseExplicitSeed(selected);
        if(seed!=activeStateSeed){activeStateSeed=seed;randomState=seed;pink0=pink1=pink2=brownState=previousWhite=0;}
        if(instanceCache&&!context.samplewise&&context.bufferSize<=256){
            NoiseBlockCache::Key key{double(randomState),pink0,pink1,pink2,brownState,previousWhite,double(mode)};
            instanceCache->process(context,key,[&](ProcessContext& c){render(c);},[&](const auto& s){randomState=std::uint32_t(s[0]);pink0=s[1];pink1=s[2];pink2=s[3];brownState=s[4];previousWhite=s[5];},
                [&]{return std::array<double,8>{double(randomState),pink0,pink1,pink2,brownState,previousWhite};});return;
        }
        render(context);
    }
    void render(ProcessContext& context) noexcept {

        for (std::size_t i = 0; i < context.bufferSize; ++i) {
            context.output[i] = nextSample();
        }
    }

    void processExport(ProcessContext& context) noexcept {
        process(context);
    }

    void inheritState(const NodeProcessor& previous) noexcept override {

        const auto& old =
            static_cast<const NoiseNode&>(previous);

        randomState = old.randomState;
        automaticSeed=old.automaticSeed;activeStateSeed=old.activeStateSeed;

        pink0 = old.pink0;
        pink1 = old.pink1;
        pink2 = old.pink2;

        brownState = old.brownState;

        previousWhite = old.previousWhite;
    }

private:

    static std::uint32_t nextSeed() noexcept {

        /*
         * Golden-ratio step gives us separated starting states.
         *
         * Never return zero because xorshift32 has zero as an
         * absorbing state.
         */
        std::uint32_t seed =
            seedCounter.fetch_add(
                0x9E3779B9u,
                std::memory_order_relaxed
            );

        return seed == 0 ? 1u : seed;
    }

    void setMode(const std::string& name) {

        if (name == "white")
            mode = NoiseMode::white;

        else if (name == "pink")
            mode = NoiseMode::pink;

        else if (name == "brown")
            mode = NoiseMode::brown;

        else if (name == "blue")
            mode = NoiseMode::blue;

        else
            throw std::runtime_error("unknown noise mode");
    }

    std::uint32_t nextRandom() noexcept {

        /*
         * xorshift32:
         *
         * - extremely cheap
         * - no allocation
         * - no locks
         * - appropriate for realtime audio noise
         *
         * It is not intended for cryptography.
         */
        std::uint32_t x = randomState;

        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 5;

        randomState = x;

        return x;
    }

    float whiteNoise() noexcept {

        /*
         * Convert the full uint32 range to approximately [-1, 1].
         */
        constexpr float scale =
            2.0f / 4294967295.0f;

        return static_cast<float>(nextRandom()) * scale - 1.f;
    }

    float pinkNoise() noexcept {

        const float white = whiteNoise();

        /*
         * Lightweight recursive approximation of a pink spectrum.
         * Approximately -3 dB/octave.
         */
        pink0 =
            0.99765f * pink0
            + 0.0990460f * white;

        pink1 =
            0.96300f * pink1
            + 0.2965164f * white;

        pink2 =
            0.57000f * pink2
            + 1.0526913f * white;

        float result =
            pink0
            + pink1
            + pink2
            + 0.1848f * white;

        /*
         * Approximate level normalization.
         */
        result *= 0.25f;

        return std::clamp(result, -1.f, 1.f);
    }

    float brownNoise() noexcept {

        const float white = whiteNoise();

        /*
         * Leaky integration of white noise.
         *
         * The leak prevents unlimited DC drift.
         */
        brownState =
            0.995f * brownState
            + 0.05f * white;

        return std::clamp(
            brownState,
            -1.f,
            1.f
        );
    }

    float blueNoise() noexcept {

        /*
         * First difference of white noise emphasizes
         * high frequencies.
         */
        const float white = whiteNoise();

        const float result =
            0.5f * (white - previousWhite);

        previousWhite = white;

        return std::clamp(
            result,
            -1.f,
            1.f
        );
    }

    float nextSample() noexcept {

        switch (mode) {

        case NoiseMode::white:
            return whiteNoise();

        case NoiseMode::pink:
            return pinkNoise();

        case NoiseMode::brown:
            return brownNoise();

        case NoiseMode::blue:
            return blueNoise();
        }

        return 0.f;
    }
};

EASYKNACK_NODE(NoiseNode, "noise")

}
