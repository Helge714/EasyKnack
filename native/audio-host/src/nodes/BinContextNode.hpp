#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
struct BinAccess : NodeProcessor {const StreamSpectrumFrame* frame=nullptr;std::size_t size=0,bin=0;double rate=48000,windowSum=1;};
struct BinContextNode : BinAccess {
    std::size_t outputCount()const noexcept override{return 8;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    void configure(std::istream& in)override{sources=parameters::BinContextNodeParameters::read(in).sources();}
    void process(ProcessContext& c)noexcept override{
        auto v=frame?frame->bins[bin]:std::complex<double>{};
        if(auto* out=c.outputAt(0))out[0]=float(v.real());
        if(auto* out=c.outputAt(1))out[0]=float(v.imag());
        if(auto* out=c.outputAt(2))out[0]=float(std::abs(v));
        if(auto* out=c.outputAt(3))out[0]=float(std::arg(v));
        if(auto* out=c.outputAt(4))out[0]=size?float(bin*rate/size):0;
        if(auto* out=c.outputAt(5))out[0]=float(bin);
        if(auto* out=c.outputAt(6))out[0]=size?float(double(bin)/(size/2)):0;
        if(auto* out=c.outputAt(7))out[0]=frame?float(frame->start/rate):0;
    }
};
EASYKNACK_NODE(BinContextNode,"bincontext")
}
