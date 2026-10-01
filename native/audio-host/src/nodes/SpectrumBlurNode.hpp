#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
struct SpectrumBlurNode : NodeProcessor {
    SpectrumStream stream;float width=0,factor=1,octaves=0;bool logarithmic=false,octaveMode=false;std::vector<double> changes,logPrefix;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!4d61336fc66a85f1"){
            int spectrum,control;if(!(in>>spectrum>>control>>width)||!std::isfinite(width)||width<0||width>8192||spectrum< -1||spectrum>=2048||control< -1||control>=2048)throw std::runtime_error("Invalid legacy spectrum blur");
            sources={spectrum,control,-1,-1};factor=1;logarithmic=false;octaveMode=false;return;
        }
        if(marker=="!5098765edd81760e"){
            int spectrum,control,factorSource;std::string mode;
            if(!(in>>spectrum>>control>>width>>factorSource>>factor>>mode)||!std::isfinite(width)||!std::isfinite(factor)||width<0||width>8192||(mode!="bins"&&mode!="factor"))throw std::runtime_error("Invalid legacy spectrum blur");
            for(int s:{spectrum,control,factorSource})if(s< -1||s>=2048)throw std::runtime_error("Invalid source reference");
            sources={spectrum,control,factorSource,-1};logarithmic=mode=="factor";octaveMode=false;return;
        }
        in.seekg(start);auto p=parameters::SpectrumBlurNodeParameters::read(in);sources=p.sources();width=p.width_value;factor=p.factor_value;octaves=p.octaves_value;octaveMode=p.mode_value=="octaves";logarithmic=p.mode_value!="bins";
    }
    void applyParameters(const NodeProcessor& p)noexcept override{auto& other=static_cast<const SpectrumBlurNode&>(p);width=other.width;factor=other.factor;logarithmic=other.logarithmic;octaves=other.octaves;octaveMode=other.octaveMode;}
    void prepare(double,std::size_t block)override{
        stream.prepare(block);changes.resize(SpectrumStream::maxSize/2+2);logPrefix.resize(changes.size());
        for(std::size_t k=1;k<logPrefix.size();k++)logPrefix[k]=logPrefix[k-1]+1./k;
    }
    void process(ProcessContext& c)noexcept override{
        auto* input=static_cast<const SpectrumStream*>(c.resource(0));
        if(!input||!input->size){stream.size=0;stream.count=0;return;}
        double amount=c.numericInput(octaveMode?3:logarithmic?2:1,octaveMode?octaves:logarithmic?factor:width)[0];
        if(octaveMode)amount=std::isfinite(amount)?std::exp2(std::clamp(amount,0.,std::log2(double(input->size)))):1.;
        double ratio=std::isfinite(amount)?std::max(1.,amount):1.;
        std::size_t last=input->size/2,radius=std::isfinite(amount)?static_cast<std::size_t>(std::round(std::clamp(amount,0.,double(last)))):0;
        if(logarithmic?ratio==1:!radius){stream.copy(*input);return;}
        stream.metadata(*input);
        for(std::size_t f=0;f<stream.count;f++){
            const auto& source=input->frames[f];auto& out=stream.frames[f];out.start=source.start;
            std::fill_n(changes.data(),last+2,0.);
            // Range additions distribute power in O(N), independently of radius.
            for(std::size_t k=0;k<=last;k++){
                if(logarithmic&&k==0)continue;
                auto lo=k>radius?k-radius:0,hi=std::min(last,k+radius);
                if(logarithmic){lo=std::max<std::size_t>(1,static_cast<std::size_t>(std::ceil(k/ratio)));hi=static_cast<std::size_t>(std::floor(std::min(double(last),k*ratio)));}
                double energy=std::norm(source.bins[k])*(k==0||k==last?1:2);
                double share=std::isfinite(energy)?energy/(logarithmic?logPrefix[hi]-logPrefix[lo-1]:double(hi-lo+1)):0;
                changes[lo]+=share;changes[hi+1]-=share;
            }
            double sum=0;
            for(std::size_t k=0;k<=last;k++){
                if(logarithmic&&k==0){out.bins[k]=source.bins[k];continue;}
                sum+=changes[k];double magnitude=std::sqrt(std::max(0.,sum)/(logarithmic?k:1)/(k==0||k==last?1:2));
                if(k==0||k==last)out.bins[k]={std::signbit(source.bins[k].real())?-magnitude:magnitude,0};
                else out.bins[k]=std::polar(magnitude,std::arg(source.bins[k]));
            }
            SpectrumStream::realSymmetry(out,stream.size);
        }
    }
};
EASYKNACK_NODE(SpectrumBlurNode,"spectrumblur")
}
