#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
#include <array>
namespace easyknack {
struct MixNode : NodeProcessor {
    float sampleSmoothing=0;
    void prepare(double rate,std::size_t)override{sampleSmoothing=static_cast<float>(1-std::exp(-1/(.005*rate)));}
    bool supportsSampleFeedback()const noexcept override{return true;}

    bool supportsScalarInputs()const noexcept override{return true;}
    bool equalPower=false;
    std::array<float,3> values{0,0,.5f}, current=values;
    bool canShareChannels() const noexcept override { return true; }
    bool supportsControlRate() const noexcept override { return true; }
    bool supportsEventRate() const noexcept override { return true; }
    bool evaluatesOnEvents() const noexcept override { return true; }
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const MixNode&>(configuration);
        values=p.values;equalPower=p.equalPower;
    }
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){
        // Source plugins embed versioned graph payloads. The pre-mode contract
        // contains exactly three source/value pairs and always means Linear.
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!12a4ab216b0e0133"){configureLinear(in);return;}
        in.seekg(start);
        auto p=parameters::MixNodeParameters::read(in);
        if(p.mode_value!="linear"&&p.mode_value!="equalpower")throw std::runtime_error("Unknown Mix mode");
        equalPower=p.mode_value=="equalpower";sources=p.sources();values={static_cast<float>(p.a_value),static_cast<float>(p.b_value),static_cast<float>(p.factor_value)};current=values;
        return;}

        configureLinear(in);
    }
    void configureLinear(std::istream& in) {
        equalPower=false;sources.clear();
        for(std::size_t i=0;i<3;i++){sources.push_back(readInput(in));values[i]=readNumber(in);}
        if(values[2]<0||values[2]>1)throw std::runtime_error("Mix factor outside 0–1");
        current=values;
    }
    void inheritState(const NodeProcessor& old) noexcept override { current=static_cast<const MixNode&>(old).current; }
    static float evaluate(float a,float b,float factor,bool power=false) noexcept {
        float f=std::clamp(factor,0.f,1.f),out;
        if(!power)out=a*(1-f)+b*f;
        else if(f==0)out=a;
        else if(f==1)out=b;
        else {double angle=f*1.57079632679489661923;out=static_cast<float>(a*std::cos(angle)+b*std::sin(angle));}
        return std::isfinite(out)?out:0;
    }
    void process(ProcessContext& c) noexcept override {
        processKernel(c,equalPower);
    }
    void processKernel(ProcessContext& c,bool power) noexcept {
        float elapsed=!c.samplewise&&c.bufferSize==1?static_cast<float>(std::max(std::size_t{1},c.blockFrames)):1.f;
        float smoothing=c.samplewise?sampleSmoothing:c.legacyEventEvaluation?1.f:static_cast<float>(1-std::exp(-elapsed/(.005*c.sampleRate)));
        const auto a=c.numericInput(0),b=c.numericInput(1),factor=c.numericInput(2);
        for(std::size_t i=0;i<c.bufferSize;i++){
            for(std::size_t p=0;p<3;p++)if(!c.input(p))current[p]+=smoothing*(values[p]-current[p]);
            float av=a.data?a[i]:current[0],bv=b.data?b[i]:current[1];
            c.output[i]=evaluate(av,bv,factor.data?factor[i]:current[2],power);
        }
    }
};
EASYKNACK_NODE(MixNode,"mix")
}
