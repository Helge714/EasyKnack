#pragma once
#include <complex>
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct BinOutputNode : NodeProcessor {
    bool polar=false;float a=0,b=0;std::complex<double> value{};
    bool supportsScalarInputs()const noexcept override{return true;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    void configure(std::istream& in)override{auto p=parameters::BinOutputNodeParameters::read(in);sources=p.sources();polar=p.mode_value=="polar";a=p.a_value;b=p.b_value;}
    void process(ProcessContext& c)noexcept override{
        double x=c.numericInput(0,a)[0],y=c.numericInput(1,b)[0];
        value=std::isfinite(x)&&std::isfinite(y)?polar?std::polar(std::max(0.,x),y):std::complex<double>(x,y):std::complex<double>{};
    }
};
EASYKNACK_NODE(BinOutputNode,"binoutput")
}
