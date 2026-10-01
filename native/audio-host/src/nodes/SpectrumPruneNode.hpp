#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
#include <numeric>
namespace easyknack {
struct SpectrumPruneNode : NodeProcessor {
    SpectrumStream stream;float keep=1;std::vector<std::size_t> order;std::vector<double> power;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{auto p=parameters::SpectrumPruneNodeParameters::read(in);sources=p.sources();keep=p.keep_value;}
    void applyParameters(const NodeProcessor& p)noexcept override{keep=static_cast<const SpectrumPruneNode&>(p).keep;}
    void prepare(double,std::size_t block)override{stream.prepare(block);order.resize(SpectrumStream::maxSize/2+1);power.resize(order.size());}
    void process(ProcessContext& c)noexcept override{
        auto* input=static_cast<const SpectrumStream*>(c.resource(0));
        if(!input||!input->size){stream.size=0;stream.count=0;return;}
        double amount=c.numericInput(1,keep)[0];std::size_t last=input->size/2,count=last+1;
        std::size_t retained=std::isfinite(amount)?static_cast<std::size_t>(std::round(std::clamp(amount,0.,1.)*count)):0;
        if(retained==count){stream.copy(*input);return;}
        stream.metadata(*input);
        for(std::size_t f=0;f<stream.count;f++){
            const auto& source=input->frames[f];auto& out=stream.frames[f];out.start=source.start;
            std::fill_n(out.bins.data(),stream.size,std::complex<double>{});
            if(!retained)continue;
            std::iota(order.begin(),order.begin()+count,0);
            for(std::size_t k=0;k<count;k++){
                double value=std::norm(source.bins[k])*(k==0||k==last?1:4);
                power[k]=std::isfinite(value)?value:0;
            }
            std::nth_element(order.begin(),order.begin()+retained,order.begin()+count,[&](auto a,auto b){return power[a]!=power[b]?power[a]>power[b]:a<b;});
            for(std::size_t i=0;i<retained;i++)out.bins[order[i]]=source.bins[order[i]];
            SpectrumStream::realSymmetry(out,stream.size);
        }
    }
};
EASYKNACK_NODE(SpectrumPruneNode,"spectrumprune")
}
