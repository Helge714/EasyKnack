#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Wavetables.hpp"
#include "../TriggerInput.hpp"
#include "../LegacyOscillatorParameters.hpp"
#include <string>

namespace easyknack {
struct OscillatorNode : NodeProcessor, Wavetables {
    ManualTriggerReceiver reset;
    void bindIdentity(const std::string& id)override{reset.bind(id+":reset");}
    void shareChannelState(NodeProcessor& p)override{reset=static_cast<OscillatorNode&>(p).reset;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    void resetPhase()noexcept{phase=0;}
    bool supportsSampleFeedback() const noexcept override { return true; }
    bool canShareChannels() const noexcept override { return true; }
    bool supportsScalarInputs() const noexcept override { return true; }
    bool inputUsesBlockData(std::size_t port) const noexcept override { return port == 1; }
    double phase = 0;
    float frequency = 440;
    double phaseOffset = 0; // Degrees, matching the Complex Oscillator parameter contract.
    double currentFrequency = 440;
    unsigned waveform = 0;
    const Tables* tables = nullptr;
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const OscillatorNode&>(configuration);
        frequency=p.frequency;waveform=p.waveform;phaseOffset=p.phaseOffset;
    }
    void configure(std::istream& in) override {
        in>>std::ws;auto begin=in.tellg();std::string marker;
        if(in.peek()=='!'){in>>marker;if(marker=="!e5d6fa7b1b0ad545"){in.seekg(begin);configureParameters(parameters::LegacyOscillatorNodeParameters::read(in));return;}
        if(marker!="!6f1c0f366ccb47fd"){in.seekg(begin);configureParameters(parameters::OscillatorNodeParameters::read(in));return;}}

        sources = {readInput(in)}; frequency = readNumber(in, 0.00001, 20000);
        currentFrequency = frequency;
        in >> std::ws;
        if (in.peek() != '@' && in.peek() != std::char_traits<char>::eof()) {
            std::string marker, shape; in >> marker >> shape;
            if (marker != "wave") throw std::runtime_error("invalid oscillator waveform");
            if (shape == "sine") waveform = 0;
            else if (shape == "triangle") waveform = 1;
            else if (shape == "saw") waveform = 2;
            else if (shape == "square") waveform = 3;
            else throw std::runtime_error("unknown oscillator waveform");
            sources.push_back(readInput(in));
        }
        tables = &builtinTables();
    }
    template<class P> void configureParameters(const P& p){
        sources=p.sources();frequency=p.frequency_value;currentFrequency=frequency;
        phaseOffset=p.phase_value;
        waveform=p.waveform_value=="sine"?0:p.waveform_value=="triangle"?1:p.waveform_value=="saw"?2:p.waveform_value=="square"?3:99;
        if(waveform==99)throw std::runtime_error("Unknown waveform");tables=&builtinTables();
    }
    double sampleSmoothing = 0;
    void prepare(double rate, std::size_t) override { sampleSmoothing = 1 - std::exp(-1.0 / (.005 * rate)); }
    void processSample(ProcessContext& c) noexcept override {
        TriggerInput trigger(c,3,reset);
        renderPrepared(c.output,c.bufferSize,frequency,c.sampleRate,c.input(0),c.input(1),true,sampleSmoothing,c.blockFrames,c.numericInput(0).stride,c.input(2),c.numericInput(2).stride,trigger.active()?&trigger:nullptr);
    }
    void process(ProcessContext& context) noexcept override {
        const double smoothing=sampleSmoothing?sampleSmoothing:1-std::exp(-1.0/(.005*context.sampleRate));
        TriggerInput trigger(context,3,reset);
        renderPrepared(context.output,context.bufferSize,frequency,context.sampleRate,context.input(0),context.input(1),true,smoothing,context.blockFrames,context.numericInput(0).stride,context.input(2),context.numericInput(2).stride,trigger.active()?&trigger:nullptr);
    }
    void inheritState(const NodeProcessor& previous) noexcept override {
        phase = static_cast<const OscillatorNode&>(previous).phase;
        currentFrequency = static_cast<const OscillatorNode&>(previous).currentFrequency;
        reset.seen=static_cast<const OscillatorNode&>(previous).reset.seen;
    }
    void process(float* output, std::size_t bufferSize, double hz, double sampleRate,
            const float* frequencyInput = nullptr) noexcept {
        // Standalone test/offline entry point; live configure() prepares tables before rendering.
        if (!tables) tables = &builtinTables();
        render(output, bufferSize, hz, sampleRate, frequencyInput, nullptr);
    }
    void render(float* output, std::size_t size, double frequency, double sampleRate,
            const float* frequencyInput, const float* custom, bool smooth = false) noexcept {
        const double smoothing = 1 - std::exp(-1.0 / (.005 * sampleRate));
        renderPrepared(output,size,frequency,sampleRate,frequencyInput,custom,smooth,smoothing);
    }
    // Shared sample loop; a generated chain may prepare coefficients once per block.
    struct SampleKernel { double increment;unsigned level; };
    SampleKernel prepareSample(double hz,double sampleRate)const noexcept {
        SampleKernel kernel{std::clamp(clean(hz),0.0,20000.0)/sampleRate,0};
        if(waveform!=0)while(kernel.level+1<levels&&kernel.increment*(1u<<(kernel.level+1))<.5)++kernel.level;
        return kernel;
    }
    float sample(const SampleKernel& kernel,const float* custom=nullptr,std::size_t tableFrames=0)noexcept {
        double cycles=phase/tau;float value=sampleCycles(kernel,cycles,custom,tableFrames);phase=cycles*tau;return value;
    }
    // Fused loops retain normalized phase locally, converting the public state
    // only at block boundaries. The table lookup stays shared with sample().
    float sampleCycles(const SampleKernel& kernel,double& cycles,const float* custom=nullptr,std::size_t tableFrames=0)const noexcept {
        const double position=phaseOffset==0?cycles:wrap(cycles+clean(phaseOffset)/360.);
        const float value=static_cast<float>(custom?lookup(custom,tableFrames,position):lookup(tables->waves[waveform][kernel.level].data(),tableSize,position));
        cycles=wrap(cycles+kernel.increment);return value;
    }
    void processFixedSample(ProcessContext& c,const SampleKernel& kernel)noexcept {
        TriggerInput trigger(c,3,reset);if(trigger.at(0))resetPhase();
        currentFrequency+=sampleSmoothing*(frequency-currentFrequency);
        auto saved=phaseOffset;phaseOffset=c.numericInput(2,float(phaseOffset))[0];
        c.output[0]=sample(kernel,c.input(1),c.blockFrames);phaseOffset=saved;
    }
    void renderPrepared(float* output,std::size_t size,double frequency,double sampleRate,
            const float* frequencyInput,const float* custom,bool smooth,double smoothing,std::size_t tableFrames=0,std::size_t frequencyStride=1,const float* phaseInput=nullptr,std::size_t phaseStride=1,TriggerInput* trigger=nullptr) noexcept {
        const bool fixed=frequencyInput?frequencyStride==0:!smooth||currentFrequency==frequency;
        const auto prepared=prepareSample(frequencyInput?frequencyInput[0]:frequency,sampleRate);
        const double savedPhaseOffset=phaseOffset;
        for (std::size_t i = 0; i < size; ++i) {
            if(trigger&&trigger->at(i))resetPhase();
            if(phaseInput)phaseOffset=phaseInput[i*phaseStride];
            currentFrequency += smoothing * (frequency - currentFrequency);
            const auto kernel=fixed?prepared:prepareSample(frequencyInput?frequencyInput[i*frequencyStride]:smooth?currentFrequency:frequency,sampleRate);
            output[i]=sample(kernel,custom,tableFrames?tableFrames:size);
        }
        phaseOffset=savedPhaseOffset;
    }
};
EASYKNACK_NODE(OscillatorNode, "osc")
}

