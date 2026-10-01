#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
inline std::complex<double> readSpectrumBin(const StreamSpectrumFrame& frame,std::size_t size,double position,int boundary=0)noexcept{
    double last=static_cast<double>(size/2);
    if(!std::isfinite(position))return {};
    if(boundary==1)position=std::clamp(position,0.,last);
    else if(boundary==2){position=std::fmod(position,last+1);if(position<0)position+=last+1;}
    else if(position<0||position>last)return {};
    auto a=static_cast<std::size_t>(std::floor(position)),b=a+1;double fraction=position-a;
    auto right=b<=size/2?frame.bins[b]:boundary==2?frame.bins[0]:std::complex<double>{};
    return frame.bins[a]*(1-fraction)+right*fraction;
}
struct SpectrumShiftNode : NodeProcessor {
    SpectrumStream stream;float shift=0;bool compensate=true;
    char executionRate()const noexcept override{return 'a';}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{auto p=parameters::SpectrumShiftNodeParameters::read(in);sources=p.sources();shift=p.shift_value;compensate=p.compensate_value;}
    void applyParameters(const NodeProcessor& p)noexcept override{auto& other=static_cast<const SpectrumShiftNode&>(p);shift=other.shift;compensate=other.compensate;}
    void prepare(double,std::size_t block)override{stream.prepare(block);}
    void process(ProcessContext& c)noexcept override{
        auto* input=static_cast<const SpectrumStream*>(c.resource(0));if(!input||!input->size){stream.size=0;stream.count=0;return;}
        stream.metadata(*input);auto control=c.numericInput(1,shift);
        for(std::size_t f=0;f<stream.count;f++){
            const auto& source=input->frames[f];auto& out=stream.frames[f];out.start=source.start;
            auto offset=std::clamp<std::int64_t>(source.start+input->size-static_cast<std::int64_t>(input->blockStart)-1,0,c.bufferSize-1);
            double amount=std::isfinite(control[offset])?control[offset]:0;
            auto phase=compensate?std::polar(1.,std::remainder(6.2831853071795864769*amount*source.start/input->size,6.2831853071795864769)):std::complex<double>(1,0);
            for(std::size_t k=0;k<=stream.size/2;k++)out.bins[k]=readSpectrumBin(source,stream.size,double(k)-amount)*phase;
            SpectrumStream::realSymmetry(out,stream.size);
        }
    }
};
EASYKNACK_NODE(SpectrumShiftNode,"spectrumshift")
}
