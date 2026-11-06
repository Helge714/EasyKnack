#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumPair.hpp"
namespace easyknack {
struct SpectrumMathNode : NodeProcessor {
    SpectrumStream stream;enum class Operation{add,subtract,multiply,divide};Operation operation=Operation::add;double gainA=1,gainB=1;
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{const auto& o=static_cast<const SpectrumMathNode&>(p);operation=o.operation;gainA=o.gainA;gainB=o.gainB;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{
        auto p=parameters::SpectrumMathNodeParameters::read(in);sources=p.sources();gainA=p.gain_a_value;gainB=p.gain_b_value;
        if(p.operation_value=="add")operation=Operation::add;else if(p.operation_value=="subtract")operation=Operation::subtract;else if(p.operation_value=="multiply")operation=Operation::multiply;else if(p.operation_value=="divide")operation=Operation::divide;else throw std::runtime_error("Unknown Spectrum Math operation");
    }
    void prepare(double,std::size_t block)override{stream.prepare(block);}
    static std::complex<double> evaluate(std::complex<double> a,std::complex<double> b,Operation op)noexcept{
        switch(op){case Operation::add:return finiteSpectrumValue(a+b);case Operation::subtract:return finiteSpectrumValue(a-b);case Operation::multiply:return finiteSpectrumValue(a*b);case Operation::divide:return b==std::complex<double>{}?std::complex<double>{}:finiteSpectrumValue(a/b);}return {};
    }
    void process(ProcessContext& c)noexcept override{
        auto* a=static_cast<const SpectrumStream*>(c.resource(0));auto* b=static_cast<const SpectrumStream*>(c.resource(1));
        if(!prepareSpectrumPair(stream,a,b))return;
        for(std::size_t f=0;f<stream.count;f++){
            auto& out=stream.frames[f];out.start=(a?a:b)->frames[f].start;
            for(std::size_t k=0;k<=stream.size/2;k++)out.bins[k]=evaluate(a?a->frames[f].bins[k]*gainA:std::complex<double>{},b?b->frames[f].bins[k]*gainB:std::complex<double>{},operation);
            SpectrumStream::realSymmetry(out,stream.size);
        }
    }
};
EASYKNACK_NODE(SpectrumMathNode,"spectrummath")
}
