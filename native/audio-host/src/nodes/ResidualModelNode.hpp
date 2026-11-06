#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
#include "../PartialRandom.hpp"
namespace easyknack {
/** Stochastic resynthesis, separate from the exact residual subtraction. */
struct ResidualModelNode : NodeProcessor {
    SpectrumStream stream;std::vector<std::complex<double>> work;std::vector<double> prefix,envelope,history;
    double width=200,time=0;std::uint64_t seed=0,identity=0;std::size_t previousSize=0,previousWindow=0;SpectrumWindow previousWindowType=SpectrumWindow::hann;std::int64_t previousStart=0;bool initialized=false;
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{auto p=parameters::ResidualModelNodeParameters::read(in);sources=p.sources();width=p.width_value;time=p.time_value;seed=std::uint64_t(p.seed_value);}
    void applyParameters(const NodeProcessor& p)noexcept override{const auto& o=static_cast<const ResidualModelNode&>(p);width=o.width;time=o.time;seed=o.seed;}
    void prepare(double,std::size_t block)override{stream.prepare(block);work.resize(SpectrumStream::maxSize);prefix.resize(SpectrumStream::maxSize/2+2);envelope.resize(prefix.size());history.resize(prefix.size());initialized=false;}
    void adoptState(NodeProcessor& p)noexcept override{auto& o=static_cast<ResidualModelNode&>(p);history.swap(o.history);identity=o.identity;previousSize=o.previousSize;previousWindow=o.previousWindow;previousWindowType=o.previousWindowType;previousStart=o.previousStart;initialized=o.initialized;}
    static double power(const std::complex<double>* values,std::size_t n)noexcept{double sum=std::norm(values[0])+std::norm(values[n/2]);for(std::size_t k=1;k<n/2;k++)sum+=2*std::norm(values[k]);return sum;}
    void process(ProcessContext& c)noexcept override{
        auto* input=static_cast<const SpectrumStream*>(c.resource(0));if(!input||!input->size){stream.size=stream.count=0;initialized=false;return;}
        stream.metadata(*input);auto n=input->size,last=n/2,length=input->windowFrames();
        if(identity!=input->identity||previousSize!=n||previousWindow!=length||previousWindowType!=input->window){initialized=false;identity=input->identity;previousSize=n;previousWindow=length;previousWindowType=input->window;}
        double amount=c.numericInput(1,float(width))[0];amount=std::isfinite(amount)?std::max(0.,amount):0;
        auto radius=std::size_t(std::clamp(std::round(amount*n/input->sampleRate*.5),0.,double(last)));
        for(std::size_t f=0;f<stream.count;f++){
            const auto& source=input->frames[f];auto& out=stream.frames[f];out.start=source.start;
            if(source.start<0){std::copy_n(source.bins.data(),n,out.bins.data());continue;}
            if(initialized&&source.start<previousStart)initialized=false;
            prefix[0]=0;for(std::size_t k=0;k<=last;k++){double p=std::norm(source.bins[k]);prefix[k+1]=prefix[k]+(std::isfinite(p)?p:0);}
            double total=0;for(std::size_t k=0;k<=last;k++){auto lo=k>radius?k-radius:0,hi=std::min(last,k+radius);envelope[k]=(prefix[hi+1]-prefix[lo])/double(hi-lo+1);if(k==0&&input->dcRemoved)envelope[k]=0;total+=envelope[k]*(k==0||k==last?1:2);}
            double original=power(source.bins.data(),n),scale=total>0&&std::isfinite(original)?original/total:0;
            double dt=initialized?double(source.start-previousStart)/input->sampleRate:0,alpha=time>0?-std::expm1(-dt/(time*.001)):1;double targetPower=0;
            for(std::size_t k=0;k<=last;k++){
                double value=envelope[k]*scale;history[k]=initialized?history[k]+alpha*(value-history[k]):value;targetPower+=history[k]*(k==0||k==last?1:2);
                auto key=partialHash(seed)^partialHash(std::uint64_t(source.start))^partialHash(k+0x5a17ULL);double angle=6.2831853071795864769*double(partialHash(key)>>11)*(1./9007199254740992.);
                work[k]=std::polar(std::sqrt(std::max(0.,history[k])),angle);if(k==0||k==last)work[k]={std::sqrt(std::max(0.,history[k]))*(std::cos(angle)>=0?1:-1),0};
            }
            for(std::size_t k=1;k<last;k++)work[n-k]=std::conj(work[k]);
            spectrumTransform(work.data(),n,true);
            for(std::size_t i=0;i<n;i++)work[i]=i<length?work[i].real()*spectrumWindowValue(input->window,i,length):0;
            spectrumTransform(work.data(),n);if(input->dcRemoved)work[0]=0;double generated=power(work.data(),n);double gain=generated>1e-30?std::sqrt(targetPower/generated):0;
            for(std::size_t k=0;k<n;k++)out.bins[k]=work[k]*gain;
            SpectrumStream::realSymmetry(out,n);previousStart=source.start;initialized=true;
        }
    }
};
EASYKNACK_NODE(ResidualModelNode,"residualmodel")
}
