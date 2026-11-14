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
    bool gaussian=false,muteBypass=false;
    float distributionAmount=-1;
    bool distributionGaussian=false;
    double lowerFactor=1,factorSpan=0,normalLow=0,normalSpan=1,normalSigma=0,normalShift=0;

    double phase = 0.0; // Elapsed base-frequency cycles, not modulated phase.

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
        frequencyValue=p.frequencyValue;strengthValue=p.strengthValue;seedValue=p.seedValue;gaussian=p.gaussian;muteBypass=p.muteBypass;
    }
    void configure(
        std::istream& in
    ) override {

        muteBypass=false;
        auto position=in.tellg();std::string marker;in>>marker;
        if(marker=="!37322412def6ce9b"){
            int f,s,seed;std::string distribution;
            if(!(in>>f>>frequencyValue>>s>>strengthValue>>seed>>seedValue>>distribution)||f< -1||f>=2048||s< -1||s>=2048||seed< -1||seed>=2048||!std::isfinite(frequencyValue)||!std::isfinite(strengthValue)||frequencyValue<.00001f||frequencyValue>20000||strengthValue<0||strengthValue>1||seedValue< -1||seedValue>16777215||(distribution!="uniform"&&distribution!="gaussian"))throw std::runtime_error("Invalid legacy Digital Noise");
            sources={f,s,seed};gaussian=distribution=="gaussian";return;
        }
        // Pre-distribution source plugins retain this payload when rebuilt with a
        // newer SDK. Keep their parameter layout readable and default to Uniform.
        if(marker=="!5bc07ddcba438cf0"){
            int f,s,seed;
            if(!(in>>f>>frequencyValue>>s>>strengthValue>>seed>>seedValue)||f< -1||f>=2048||s< -1||s>=2048||seed< -1||seed>=2048||
                !std::isfinite(frequencyValue)||!std::isfinite(strengthValue)||frequencyValue<.00001f||frequencyValue>20000||strengthValue<0||strengthValue>1||seedValue< -1||seedValue>16777215)
                throw std::runtime_error("Invalid legacy Digital Noise");
            sources={f,s,seed};gaussian=false;return;
        }
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
            gaussian=p.distribution_value=="gaussian";
            if(p.mute_bypass_value!="0"&&p.mute_bypass_value!="1")throw std::runtime_error("Invalid Digital Noise mute mode");
            muteBypass=p.mute_bypass_value=="1";

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

        if(muteBypass){
            auto frequency=context.numericInput(0,frequencyValue);auto* trigger=context.outputAt(1);auto* out=context.outputAt(2);
            for(std::size_t i=0;i<context.bufferSize;i++){context.output[i]=1.f;if(trigger)trigger[i]=0.f;if(out)out[i]=frequency[i];}
            return;
        }
        float requestedSeed=context.numericInput(2,float(seedValue))[0];
        int selected=noiseSeedValue(requestedSeed);
        auto seed=selected<0?automaticSeed:noiseExplicitSeed(selected);
        if(seed!=activeStateSeed){
            rngState=seed;activeStateSeed=seed;
            activeSeed=selected;phase=0;initialized=false;
        }
        if(instanceCache&&!context.samplewise&&context.bufferSize<=256){
            NoiseBlockCache::Key key{double(rngState),phase,currentFactor,double(initialized),context.numericInput(0,frequencyValue)[0],context.numericInput(1,strengthValue)[0],double(context.input(0)!=nullptr),double(context.input(1)!=nullptr),double(gaussian)};
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

            if(amount==0.f)currentFactor=1.f;
            // Preserve fractional elapsed time across boundaries. Keeping the old
            // modulated phase remainder would bias short periods on the sample grid.
            while(phase>=1.0/currentFactor){
                phase-=1.0/currentFactor;
                currentFactor=drawFactor(amount);
                trigger=true;
            }


            /*
             * Current factor is held throughout this sample.
             */
            factorOut[i] =
                currentFactor;


            /*
             * Advance base-cycle time; each random cycle lasts 1 / factor.
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
                static_cast<double>(baseFrequency)
                / context.sampleRate;


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

        if(distributionAmount!=strength||distributionGaussian!=gaussian){
            distributionAmount=strength;distributionGaussian=gaussian;
            double upper=std::exp2(double(strength));lowerFactor=1/upper;factorSpan=upper-lowerFactor;
            if(gaussian){
                normalSigma=double(strength)/3;
                normalShift=std::log(2.)*normalSigma;
                normalLow=normalCdf(-3-normalShift);
                normalSpan=normalCdf(3-normalShift)-normalLow;
            }
        }
        double u=uniform01();
        // q(x) ∝ 2^x p(x) cancels duration 2^-x. For uniform p(x),
        // drawing the factor uniformly in linear space is exactly this proposal.
        if(!gaussian)return static_cast<float>(lowerFactor+u*factorSpan);
        // Exponential tilting of N(0,sigma²) shifts its mean by ln(2)*sigma².
        // Invert the truncated CDF without rejection or unbounded random retries.
        double p=normalLow+u*normalSpan;
        // p is bounded by the ±3-sigma truncation (including the small tilt).
        // A tail-based starting estimate keeps Newton steps inside that domain.
        double z=std::copysign(std::sqrt(-2*std::log(std::min(p,1-p)))-.5,p-.5);
        for(unsigned i=0;i<6;i++)z-=(normalCdf(z)-p)*2.5066282746310005*std::exp(.5*z*z);
        double x=normalSigma*(z+normalShift);
        return static_cast<float>(std::clamp(std::exp2(x),lowerFactor,lowerFactor+factorSpan));
    }

    static double normalCdf(double x)noexcept{return .5*std::erfc(-x*.7071067811865475);}


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
