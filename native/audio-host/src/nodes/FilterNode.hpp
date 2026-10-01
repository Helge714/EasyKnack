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
    bool supportsSampleFeedback()const noexcept override{return true;}
    void prepare(double rate,std::size_t)override{sampleSmoothing=1-std::exp(-1/(.005*rate));}
    void processSample(ProcessContext& c)noexcept override{processPrepared(c,sampleSmoothing);}
    double cutoff=1000,resonance=.707,smoothedCutoff=1000,smoothedQ=.707,ic1=0,ic2=0;
    std::array<double,3> mix{1,0,0};int mode=0;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const FilterNode&>(configuration);
        cutoff=p.cutoff;resonance=p.resonance;mode=p.mode;
    }
    void configure(std::istream& in) override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::FilterNodeParameters::read(in);
        sources=p.sources();cutoff=p.cutoff_value;resonance=p.resonance_value;
        if(p.mode_value=="lowpass")mode=0;else if(p.mode_value=="highpass")mode=1;else if(p.mode_value=="bandpass")mode=2;else throw std::runtime_error("Invalid filter mode");
        smoothedCutoff=cutoff;smoothedQ=resonance;mix={0,0,0};mix[mode]=1;
        return;}

        sources={readInput(in),readInput(in)};cutoff=readNumber(in,0,24000);
        sources.push_back(readInput(in));resonance=readNumber(in,.1f,30);
        std::string type;if(!(in>>type))throw std::runtime_error("missing filter mode");
        if(type=="lowpass")mode=0;else if(type=="highpass")mode=1;else if(type=="bandpass")mode=2;else throw std::runtime_error("invalid filter mode");
        smoothedCutoff=cutoff;smoothedQ=resonance;mix={0,0,0};mix[mode]=1;
    }
    void inheritState(const NodeProcessor& other) noexcept override{
        auto* p=dynamic_cast<const FilterNode*>(&other);if(p){ic1=p->ic1;ic2=p->ic2;smoothedCutoff=p->smoothedCutoff;smoothedQ=p->smoothedQ;mix=p->mix;}
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
        for(std::size_t i=0;i<c.bufferSize;i++){
            double f=fc?fc[i]:cutoff,r=q?q[i]:resonance;
            f=std::isfinite(f)?std::clamp(f,5.0,c.sampleRate*.45):1000;
            r=std::isfinite(r)?std::clamp(r,.1,30.0):.707;
            smoothedCutoff+=(f-smoothedCutoff)*smoothing;smoothedQ+=(r-smoothedQ)*smoothing;
            double g=std::tan(3.14159265358979323846*std::clamp(smoothedCutoff,5.0,c.sampleRate*.45)/c.sampleRate),k=1/std::clamp(smoothedQ,.1,30.0);
            double a1=1/(1+g*(g+k)),a2=g*a1,a3=g*a2;
            double v0=input&&std::isfinite(input[i])?input[i]:0,v3=v0-ic2,v1=a1*ic1+a2*v3,v2=ic2+a2*ic1+a3*v3;
            ic1=2*v1-ic1;ic2=2*v2-ic2;
            for(int m=0;m<3;m++)mix[m]+=((m==mode?1:0)-mix[m])*smoothing;
            double result=mix[0]*v2+mix[1]*(v0-k*v1-v2)+mix[2]*v1;
            if(!std::isfinite(result)||std::abs(result)>std::numeric_limits<float>::max()||!std::isfinite(ic1)||!std::isfinite(ic2)){ic1=ic2=0;result=0;}
            if(std::abs(ic1)<1e-25)ic1=0;if(std::abs(ic2)<1e-25)ic2=0;
            c.output[i]=static_cast<float>(result);
        }
    }
};
EASYKNACK_NODE(FilterNode,"filter")
}
