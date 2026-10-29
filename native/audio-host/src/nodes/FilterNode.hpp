#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
#include <array>
namespace easyknack {
/** TPT state-variable filter; equivalent-integrator formulation described by Andrew Simper:
 * https://www.cytomic.com/files/dsp/SvfLinearTrapOptimised.pdf */
struct FilterNode : NodeProcessor {
    double sampleSmoothing=0;
    int stages=1,cachedStages=0;bool compensate=false;
    std::array<double,3> extra1{},extra2{};
    std::array<double,4> stageBlend{1,0,0,0},baseQ{},damping{};
    double cachedQ=-1,inputPower=0,outputPower=0,makeup=1;
    bool supportsSampleFeedback()const noexcept override{return true;}
    void prepare(double rate,std::size_t)override{sampleSmoothing=1-std::exp(-1/(.005*rate));for(int s=0;s<4;s++)stageBlend[s]=s<stages?1:0;}
    void processSample(ProcessContext& c)noexcept override{processPrepared(c,sampleSmoothing);}
    double cutoff=1000,resonance=.707,smoothedCutoff=1000,smoothedQ=.707,ic1=0,ic2=0;
    std::array<double,3> mix{1,0,0};int mode=0;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const FilterNode&>(configuration);
        cutoff=p.cutoff;resonance=p.resonance;mode=p.mode;stages=p.stages;compensate=p.compensate;
    }
    void configure(std::istream& in) override{
        in>>std::ws;auto start=in.tellg();std::string marker;
        if(in.peek()=='!'){
        in>>marker;if(marker!="!cd23044f7c58d242"){in.seekg(start);auto p=parameters::FilterNodeParameters::read(in);
        sources=p.sources();cutoff=p.cutoff_value;resonance=p.resonance_value;
        stages=std::stoi(p.order_value)/2;compensate=p.compensate_value;
        if(p.mode_value=="lowpass")mode=0;else if(p.mode_value=="highpass")mode=1;else if(p.mode_value=="bandpass")mode=2;else throw std::runtime_error("Invalid filter mode");
        smoothedCutoff=cutoff;smoothedQ=resonance;mix={0,0,0};mix[mode]=1;
        for(int s=0;s<4;s++)stageBlend[s]=s<stages?1:0;
        return;}}

        stages=1;compensate=false;stageBlend={1,0,0,0};
        sources={readInput(in),readInput(in)};cutoff=readNumber(in,0,24000);
        sources.push_back(readInput(in));resonance=readNumber(in,.1f,30);
        std::string type;if(!(in>>type))throw std::runtime_error("missing filter mode");
        if(type=="lowpass")mode=0;else if(type=="highpass")mode=1;else if(type=="bandpass")mode=2;else throw std::runtime_error("invalid filter mode");
        smoothedCutoff=cutoff;smoothedQ=resonance;mix={0,0,0};mix[mode]=1;
    }
    void inheritState(const NodeProcessor& other) noexcept override{
        auto* p=dynamic_cast<const FilterNode*>(&other);if(p){ic1=p->ic1;ic2=p->ic2;smoothedCutoff=p->smoothedCutoff;smoothedQ=p->smoothedQ;mix=p->mix;extra1=p->extra1;extra2=p->extra2;stageBlend=p->stageBlend;inputPower=p->inputPower;outputPower=p->outputPower;makeup=p->makeup;}
    }
    void processExport(ProcessContext& c) noexcept {
#if defined(__clang__)
        [[clang::always_inline]]
#endif
        FilterNode::process(c);
    }
    void process(ProcessContext& c) noexcept override{
        const double smoothing=1-std::exp(-1/(.005*c.sampleRate));
#if defined(__clang__)
        [[clang::always_inline]]
#endif
        processPrepared(c,smoothing);
    }
    void processPrepared(ProcessContext& c,double smoothing) noexcept {
        auto* input=c.input(0);auto* fc=c.input(1);auto* q=c.input(2);
        if(cachedStages!=stages){
            for(int s=0;s<4;s++)baseQ[s]=s<stages?1/(2*std::cos((2*s+1)*3.141592653589793/(4*stages))):.707;
            cachedStages=stages;cachedQ=-1;
        }
        const double powerStep=1-std::exp(-1/(.1*c.sampleRate));
        for(std::size_t i=0;i<c.bufferSize;i++){
            double f=fc?fc[i]:cutoff,r=q?q[i]:resonance;
            f=std::isfinite(f)?std::clamp(f,5.0,c.sampleRate*.45):1000;
            r=std::isfinite(r)?std::clamp(r,.1,30.0):.707;
            smoothedCutoff+=(f-smoothedCutoff)*smoothing;smoothedQ+=(r-smoothedQ)*smoothing;
            double g=std::tan(3.14159265358979323846*std::clamp(smoothedCutoff,5.0,c.sampleRate*.45)/c.sampleRate);
            if(cachedQ!=smoothedQ){
                double scale=stages==1?1:std::pow(std::clamp(smoothedQ,.1,30.0)/.707,1./stages);
                for(int s=0;s<4;s++)damping[s]=1/std::clamp(stages==1?smoothedQ:baseQ[s]*scale,.1,30.);
                cachedQ=smoothedQ;
            }
            const double original=input&&std::isfinite(input[i])?input[i]:0;double result=original;
            for(int m=0;m<3;m++)mix[m]+=((m==mode?1:0)-mix[m])*smoothing;
            for(int s=0;s<4;s++){
                if(s){stageBlend[s]+=((s<stages?1:0)-stageBlend[s])*smoothing;if(s>=stages&&stageBlend[s]<1e-8){stageBlend[s]=0;extra1[s-1]=extra2[s-1]=0;continue;}}
                double& z1=s?extra1[s-1]:ic1;double& z2=s?extra2[s-1]:ic2;
                double k=damping[s],a1=1/(1+g*(g+k)),a2=g*a1,a3=g*a2;
                double v0=result,v3=v0-z2,v1=a1*z1+a2*v3,v2=z2+a2*z1+a3*v3;
                z1=2*v1-z1;z2=2*v2-z2;
                double filtered=mix[0]*v2+mix[1]*(v0-k*v1-v2)+mix[2]*v1;
                if(!std::isfinite(filtered)||std::abs(filtered)>std::numeric_limits<float>::max()||!std::isfinite(z1)||!std::isfinite(z2)){z1=z2=0;filtered=0;}
                if(std::abs(z1)<1e-25)z1=0;if(std::abs(z2)<1e-25)z2=0;
                result=s?v0+(filtered-v0)*stageBlend[s]:filtered;
            }
            if(compensate||makeup!=1){
                inputPower+=(original*original-inputPower)*powerStep;outputPower+=(result*result-outputPower)*powerStep;
                double target=compensate&&outputPower>1e-18?std::clamp(std::sqrt(inputPower/outputPower),.25,4.):1;
                makeup+=(target-makeup)*smoothing;
                if(!compensate&&std::abs(makeup-1)<1e-8){makeup=1;inputPower=outputPower=0;}
                result*=makeup;
            }
            if(!std::isfinite(result)||std::abs(result)>std::numeric_limits<float>::max())result=0;
            c.output[i]=static_cast<float>(result);
        }
    }
};
EASYKNACK_NODE(FilterNode,"filter")
}
