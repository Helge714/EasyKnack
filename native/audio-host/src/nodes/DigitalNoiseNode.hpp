#pragma once

#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../NoiseSharing.hpp"

#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstddef>
#include <cstdint>

namespace easyknack {

struct DigitalNoiseNode : NodeProcessor {
    bool supportsSampleFeedback() const noexcept override { return true; }
    bool supportsScalarInputs()const noexcept override{return true;}

    float frequencyValue = 440.f;
    float strengthValue = 0.f;
    int seedValue=-1,activeSeed=-1;

    double phase = 0.0;

    float currentFactor = 1.f;

    std::uint32_t rngState = nextSeed();
    std::uint32_t automaticSeed=rngState,activeStateSeed=0;
    std::shared_ptr<NoiseBlockCache> instanceCache;
    void bindIdentity(const std::string& id)override{automaticSeed=noiseIdentitySeed(id);}
    void shareChannelState(NodeProcessor& p)override{automaticSeed=static_cast<DigitalNoiseNode&>(p).automaticSeed;}

    bool initialized = false;


    bool canShareChannels() const noexcept override {
        return true;
    }


    bool writesEventBuffers()const noexcept override{return true;}
    std::size_t outputCount() const noexcept override {
        return 3;
    }


    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const DigitalNoiseNode&>(configuration);
        frequencyValue=p.frequencyValue;strengthValue=p.strengthValue;seedValue=p.seedValue;
    }
    void configure(
        std::istream& in
    ) override {

        auto position=in.tellg();std::string marker;in>>marker;
        if(marker=="!abfa1179a859e263"){
            int f,s;if(!(in>>f>>frequencyValue>>s>>strengthValue)||f< -1||f>=2048||s< -1||s>=2048||!std::isfinite(frequencyValue)||!std::isfinite(strengthValue)||frequencyValue<.00001f||frequencyValue>20000||strengthValue<0||strengthValue>1)throw std::runtime_error("Invalid legacy Digital Noise");sources={f,s};return;
        }
        in.clear();in.seekg(position);
        in >> std::ws;

        if (in.peek() == '!') {

            auto p =
                parameters::DigitalNoiseNodeParameters::read(in);

            sources =
                p.sources();
            seedValue=p.seed_value;

            frequencyValue =
                static_cast<float>(
                    p.frequency_value
                );

            strengthValue =
                static_cast<float>(
                    p.strength_value
                );

            return;
        }

        sources = {
            readInput(in)
        };

        frequencyValue =
            readNumber(
                in,
                0.00001f,
                20000.f
            );

        sources.push_back(
            readInput(in)
        );

        strengthValue =
            readNumber(
                in,
                0.f,
                1.f
            );
    }


    void inheritState(
        const NodeProcessor& previous
    ) noexcept override {

        const auto& old =
            static_cast<
                const DigitalNoiseNode&
            >(previous);

        phase =
            old.phase;

        currentFactor =
            old.currentFactor;

        rngState =
            old.rngState;

        initialized =
            old.initialized;
        activeSeed=old.activeSeed;
        automaticSeed=old.automaticSeed;activeStateSeed=old.activeStateSeed;
    }


    void process(
        ProcessContext& context
    ) noexcept override {

        float requestedSeed=context.numericInput(2,float(seedValue))[0];
        int selected=noiseSeedValue(requestedSeed);
        auto seed=selected<0?automaticSeed:noiseExplicitSeed(selected);
        if(seed!=activeStateSeed){
            rngState=seed;activeStateSeed=seed;
            activeSeed=selected;phase=0;initialized=false;
        }
        if(instanceCache&&!context.samplewise&&context.bufferSize<=256){
            NoiseBlockCache::Key key{double(rngState),phase,currentFactor,double(initialized),context.numericInput(0,frequencyValue)[0],context.numericInput(1,strengthValue)[0],double(context.input(0)!=nullptr),double(context.input(1)!=nullptr)};
            instanceCache->process(context,key,[&](ProcessContext& c){render(c);},[&](const auto& s){rngState=std::uint32_t(s[0]);phase=s[1];currentFactor=s[2];initialized=s[3]!=0;},
                [&]{return std::array<double,8>{double(rngState),phase,currentFactor,double(initialized)};},2);return;
        }
        render(context);
    }
    void render(ProcessContext& context)noexcept{
        const auto frequency =
            context.numericInput(0);

        const auto strength =
            context.numericInput(1);

        float* factorOut =
            context.output;

        float* triggerOut =
            context.outputAt(1);


        for (
            std::size_t i = 0;
            i < context.bufferSize;
            ++i
        ) {

            const float baseFrequency =
                sanitizeFrequency(
                    frequency
                        ? frequency[i]
                        : frequencyValue
                );

            const float amount =
                sanitizeStrength(
                    strength
                        ? strength[i]
                        : strengthValue
                );

            bool trigger =
                false;


            if (!initialized) {

                currentFactor =
                    drawFactor(
                        amount
                    );

                initialized =
                    true;

                trigger =
                    true;
            }


            /*
             * Current factor is held throughout this sample.
             */
            factorOut[i] =
                currentFactor;


            /*
             * Advance using the factor of the current cycle.
             */
            const double actualFrequency =
                static_cast<double>(
                    baseFrequency
                )
                * static_cast<double>(
                    currentFactor
                );

            if(auto* out=context.outputAt(2))out[i]=static_cast<float>(actualFrequency);

            phase +=
                actualFrequency
                / context.sampleRate;


            /*
             * At the end of the current period,
             * draw the factor for the next period.
             */
            while (phase >= 1.0) {

                phase -=
                    1.0;

                currentFactor =
                    drawFactor(
                        amount
                    );

                trigger =
                    true;
            }


            if(trigger&&context.eventOutput(1))context.eventOutput(1)->push(static_cast<std::uint32_t>(context.sampleOffset+i),1,ValueEventKind::trigger);
            if (triggerOut) {

                triggerOut[i] =
                    trigger
                        ? 1.f
                        : 0.f;
            }
        }
    }


private:

    static std::uint32_t nextSeed() noexcept {

        static std::atomic<std::uint32_t> counter{
            0x9E3779B9u
        };

        std::uint32_t value =
            counter.fetch_add(
                0x6D2B79F5u,
                std::memory_order_relaxed
            );

        return value != 0
            ? value
            : 0xA341316Cu;
    }


    std::uint32_t randomBits() noexcept {

        std::uint32_t x =
            rngState;

        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 5;

        rngState =
            x;

        return x;
    }


    float uniform01() noexcept {

        return static_cast<float>(
            randomBits() >> 8
        )
        * (
            1.f
            / 16777216.f
        );
    }


    float drawFactor(
        float strength
    ) noexcept {

        if (strength <= 0.f) {
            return 1.f;
        }

        const float bipolar =
            uniform01()
            * 2.f
            - 1.f;

        const float exponent =
            bipolar
            * strength;

        return std::exp2(
            exponent
        );
    }


    static float sanitizeFrequency(
        float value
    ) noexcept {

        if (!std::isfinite(value)) {
            return 440.f;
        }

        return std::clamp(
            value,
            0.00001f,
            20000.f
        );
    }


    static float sanitizeStrength(
        float value
    ) noexcept {

        if (!std::isfinite(value)) {
            return 0.f;
        }

        return std::clamp(
            value,
            0.f,
            1.f
        );
    }
};


EASYKNACK_NODE(
    DigitalNoiseNode,
    "digitalnoise"
)

}
