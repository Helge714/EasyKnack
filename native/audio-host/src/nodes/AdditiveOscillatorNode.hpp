#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Wavetables.hpp"
#include "../WaveformResource.hpp"
#include "../TriggerInput.hpp"
#include "../LegacyOscillatorParameters.hpp"
#include <algorithm>
#include <array>
#include <string>

namespace easyknack {
struct AdditiveOscillatorNode : NodeProcessor, Wavetables {
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::waveform:ResourceKind::none;}
    ManualTriggerReceiver reset;
    void bindIdentity(const std::string& id)override{reset.bind(id+":reset");}
    void shareChannelState(NodeProcessor& p)override{reset=static_cast<AdditiveOscillatorNode&>(p).reset;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    void resetPhase()noexcept{phases.fill(0);}
    double sampleSmoothing = 0;
    void prepare(double rate, std::size_t) override { sampleSmoothing = 1 - std::exp(-1.0 / (.005 * rate)); }
    bool supportsSampleFeedback() const noexcept override { return true; }
    bool canShareChannels() const noexcept override { return true; }
    bool inputUsesBlockData(std::size_t port) const noexcept override { return port == 0; }
    static constexpr std::size_t maximumPartials = 144;
    const Tables* tables = nullptr;
    std::array<double, maximumPartials> phases{}, ratios{}, weights{}, currentRatios{}, currentWeights{};
    float cachedFalloff = 1, cachedSpread = 1;
    unsigned cachedCount = 1, renderCount = 1;
    double currentFrequency = 440, currentPhase = 0;
    unsigned partials = 1, waveform = 0;
    float falloff = 1, spread = 1, frequency = 440, phaseDegrees = 0;
    bool spectrumReady=false;

    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const AdditiveOscillatorNode&>(configuration);
        partials=p.partials;waveform=p.waveform;falloff=p.falloff;spread=p.spread;frequency=p.frequency;phaseDegrees=p.phaseDegrees;
    }
    template<class P> void configureParameters(const P& p,bool legacy=false){
        sources=p.sources();
        waveform=p.waveform_value=="sine"?0:p.waveform_value=="triangle"?1:p.waveform_value=="saw"?2:p.waveform_value=="square"?3:99;
        if(waveform==99)throw std::runtime_error("Unknown waveform");
        partials=p.harmonics_value;falloff=p.falloff_value;spread=legacy?std::clamp(1.f+static_cast<float>(p.spread_value)/1200.f,0.f,4.f):p.spread_value;frequency=p.frequency_value;phaseDegrees=p.phase_value;
        updateSpectrum(falloff,spread,partials);currentRatios=ratios;currentWeights=weights;renderCount=partials;
        currentFrequency=frequency;currentPhase=phaseDegrees;tables=&builtinTables();
    }
    void configure(std::istream& in) override {
        bool legacyReset=false;
        in>>std::ws;if(in.peek()=='!'){
            auto start=in.tellg();std::string marker;in>>marker;in.seekg(start);
            if(marker=="!51e01ae3a60f12dc"){configureParameters(parameters::LegacyAdditiveOscillatorNodeParameters::read(in),true);return;}
            if(marker=="!e64d76dd5d57451d"){in>>marker;legacyReset=true;}
            else {configureParameters(parameters::AdditiveOscillatorNodeParameters::read(in));return;}
        }

        int waveInput = readInput(in);
        std::string shape; if (!(in >> shape)) throw std::runtime_error("missing additive waveform");
        if (shape == "sine") waveform = 0;
        else if (shape == "triangle") waveform = 1;
        else if (shape == "saw") waveform = 2;
        else if (shape == "square") waveform = 3;
        else throw std::runtime_error("unknown additive waveform");
        float count = readNumber(in, 1, maximumPartials);
        if (count != std::floor(count)) throw std::runtime_error("integer partial count required");
        partials = static_cast<unsigned>(count);
        falloff = readNumber(in, 0, 4); spread = std::clamp(1.f+readNumber(in,-2400,2400)/1200.f,0.f,4.f);
        int frequencyInput = readInput(in); frequency = readNumber(in, 0.00001f, 20000);
        int phaseInput = readInput(in); phaseDegrees = readNumber(in, 0, 360);
        sources = {waveInput, frequencyInput, phaseInput};
        in >> std::ws;
        if (in.peek() != '@' && in.peek() != std::char_traits<char>::eof()) {
            std::string marker; in >> marker;
            if (marker != "inputs") throw std::runtime_error("invalid additive inputs");
            sources.push_back(readInput(in)); sources.push_back(readInput(in)); sources.push_back(readInput(in));
            if(legacyReset)sources.push_back(readInput(in));
        }
        updateSpectrum(falloff, spread, partials);
        currentRatios = ratios; currentWeights = weights; renderCount = partials;
        currentFrequency = frequency; currentPhase = phaseDegrees;
        tables = &builtinTables();
    }
    static const std::array<double,maximumPartials>& partialLogs() noexcept {
        // Initialized by configure/updateSpectrum on the control thread; never allocates.
        static const auto values=[] {std::array<double,maximumPartials> result{};for(unsigned h=0;h<maximumPartials;++h)result[h]=std::log2(h+1);return result;}();
        return values;
    }
    void updateSpectrum(float slope, float exponent, unsigned count) noexcept {
        const bool ratiosChanged=!spectrumReady||exponent!=cachedSpread;
        const bool weightsChanged=!spectrumReady||slope!=cachedFalloff;
        const auto& logarithms=partialLogs();
        for (unsigned h = 0; h < maximumPartials; ++h) {
            if(ratiosChanged)ratios[h]=exponent==1?static_cast<double>(h+1):exponent==2?static_cast<double>(h+1)*(h+1):std::exp2(exponent*logarithms[h]);
            if(weightsChanged || (h<count)!=(h<cachedCount))weights[h]=h<count?std::pow(h+1,-slope):0;
        }
        cachedFalloff=slope;cachedSpread=exponent;cachedCount=count;spectrumReady=true;
    }
    void inheritState(const NodeProcessor& previous) noexcept override {
        const auto& old = static_cast<const AdditiveOscillatorNode&>(previous);
        phases = old.phases; currentRatios = old.currentRatios; currentWeights = old.currentWeights;
        renderCount = std::max(old.renderCount, partials);
        currentFrequency = old.currentFrequency; currentPhase = old.currentPhase;
        reset.seen=old.reset.seen;
    }
    void process(ProcessContext& context) noexcept override {
        TriggerInput trigger(context,6,reset);
        const auto* resource = waveformResource(context.resource(0));

        const auto* hzInput = context.input(1);
        const auto* phaseInput = context.input(2);
        const auto* countInput = context.input(3);
        const auto* falloffInput = context.input(4);
        const auto* spreadInput = context.input(5);
        unsigned count = countInput ? static_cast<unsigned>(std::clamp(clean(countInput[0]), 1.0, 144.0)) : partials;
        renderCount = std::max(renderCount, count);
        const double smoothing = context.samplewise ? sampleSmoothing : 1 - std::exp(-1.0 / (.005 * context.sampleRate));
        for (std::size_t i = 0; i < context.bufferSize; ++i) {
            if(trigger.at(i))resetPhase();
            currentFrequency += smoothing * (frequency - currentFrequency);
            currentPhase += smoothing * (phaseDegrees - currentPhase);
            double hz = std::clamp(clean(hzInput ? hzInput[i] : currentFrequency), 0.0, 20000.0);
            double offset = clean(phaseInput ? phaseInput[i] : currentPhase) / 360.0;
            float slope = falloffInput ? static_cast<float>(std::clamp(clean(falloffInput[i]), 0.0, 4.0)) : falloff;
            float exponent = spreadInput ? static_cast<float>(std::clamp(clean(spreadInput[i]), 0.0, 4.0)) : spread;
            if (slope != cachedFalloff || exponent != cachedSpread || count != cachedCount) updateSpectrum(slope, exponent, count);
            double sum = 0, weightSum = 0;
            for (unsigned h = 0; h < renderCount; ++h) {
                currentRatios[h] += smoothing * (ratios[h] - currentRatios[h]);
                currentWeights[h] += smoothing * (weights[h] - currentWeights[h]);
                double increment = hz * currentRatios[h] / context.sampleRate;
                if (increment < 0.5) {
                    double phase = phases[h] + offset * currentRatios[h];
                    double sample;
                    if (resource) sample = resource->sample(phase,increment);
                    else {
                        unsigned level = 0;
                        if (waveform != 0) while (level + 1 < levels && increment * (1u << (level + 1)) < 0.5) ++level;
                        sample = lookup(tables->waves[waveform][level].data(), tableSize, phase);
                    }
                    sum += sample * currentWeights[h]; weightSum += currentWeights[h];
                }
                phases[h] = wrap(phases[h] + increment);
            }
            context.output[i] = static_cast<float>(weightSum > 0 ? sum / weightSum : 0);
        }
        // Release faded-out partials so lowering the limit also lowers DSP cost.
        while ((!context.samplewise || context.sampleOffset + context.bufferSize == context.blockFrames) && renderCount > count && currentWeights[renderCount - 1] < 1e-7) {
            currentWeights[--renderCount] = 0;
        }
    }
};
EASYKNACK_NODE(AdditiveOscillatorNode, "additive")
}




