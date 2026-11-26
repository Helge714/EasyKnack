#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../dsp/Dynamics.hpp"
namespace easyknack {
// First-order bilinear allpass, or RBJ Cookbook second-order allpass.
struct AllpassNode final:NodeProcessor {
    double frequency=1000,q=.7071067811865476,z1=0,z2=0,rate=0,cachedF=-1,cachedQ=-1,b0=0,b1=0,a2=0;int order=1,cachedOrder=0;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::AllpassNodeParameters::read(in);sources=p.sources();frequency=p.frequency_value;q=p.resonance_value;order=std::stoi(p.order_value);}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const AllpassNode&>(other);frequency=p.frequency;q=p.q;if(order!=p.order){z1=z2=0;order=p.order;}}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const AllpassNode&>(other);if(order==p.order){z1=p.z1;z2=p.z2;}}
    void process(ProcessContext& c)noexcept override{
        auto input=c.numericInput(0),f=c.numericInput(1,frequency),quality=c.numericInput(2,q);
        for(std::size_t i=0;i<c.bufferSize;i++){
            double fc=dynamics::bound(f[i],1,c.sampleRate*.49,1000),qq=dynamics::bound(quality[i],.1,30,.7071067811865476);
            if(fc!=cachedF||qq!=cachedQ||order!=cachedOrder||rate!=c.sampleRate){
                cachedF=fc;cachedQ=qq;cachedOrder=order;rate=c.sampleRate;
                double w=6.2831853071795864769*fc/rate;
                if(order==1){double t=std::tan(w*.5);b0=(t-1)/(t+1);}
                else{double alpha=std::sin(w)/(2*qq);b0=(1-alpha)/(1+alpha);b1=-2*std::cos(w)/(1+alpha);a2=b0;}
            }
            double x=dynamics::finite(input[i]),y=b0*x+z1;
            if(order==1)z1=x-b0*y;
            else{z1=b1*x-b1*y+z2;z2=x-a2*y;}
            if(!std::isfinite(y)||!std::isfinite(z1)||!std::isfinite(z2)){y=0;z1=z2=0;}
            if(std::abs(z1)<1e-25)z1=0;if(std::abs(z2)<1e-25)z2=0;c.output[i]=dynamics::sample(y);
        }
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
};
EASYKNACK_NODE(AllpassNode,"allpass")
}
