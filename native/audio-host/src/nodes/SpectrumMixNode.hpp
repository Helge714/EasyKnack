#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumPair.hpp"
namespace easyknack {
struct SpectrumMixNode : NodeProcessor {
    SpectrumStream stream;double factor=.5,gainA=1,gainB=1;bool equalPower=false,legacySum=false;
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    std::string parameterStructure()const override{return legacySum?"legacy-add":"crossfade";}
    void applyParameters(const NodeProcessor& p)noexcept override{const auto& o=static_cast<const SpectrumMixNode&>(p);factor=o.factor;equalPower=o.equalPower;gainA=o.gainA;gainB=o.gainB;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p<2?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        // Previously exported plugins contain a weighted Add under this processor ID.
        if(marker=="!1317e59a28722f80"){
            legacySum=true;sources={readInput(in),readInput(in)};gainA=readNumber(in);gainB=readNumber(in);return;
        }
        in.seekg(start);auto p=parameters::SpectrumMixNodeParameters::read(in);sources=p.sources();factor=p.factor_value;legacySum=false;
        if(p.mode_value!="linear"&&p.mode_value!="equalpower")throw std::runtime_error("Unknown Spectrum Mix mode");equalPower=p.mode_value=="equalpower";
    }
    void prepare(double,std::size_t block)override{stream.prepare(block);}
    void process(ProcessContext& c)noexcept override{
        auto* a=static_cast<const SpectrumStream*>(c.resource(0));auto* b=static_cast<const SpectrumStream*>(c.resource(1));
        if(!prepareSpectrumPair(stream,a,b))return;
        double amount=legacySum?0:c.numericInput(2,float(factor))[0];amount=std::isfinite(amount)?std::clamp(amount,0.,1.):0;
        const double wa=legacySum?gainA:amount==1?0:equalPower?std::cos(amount*1.57079632679489661923):1-amount;
        const double wb=legacySum?gainB:amount==0?0:equalPower?std::sin(amount*1.57079632679489661923):amount;
        for(std::size_t f=0;f<stream.count;f++){
            auto& out=stream.frames[f];out.start=(a?a:b)->frames[f].start;
            for(std::size_t k=0;k<=stream.size/2;k++)out.bins[k]=finiteSpectrumValue((a?a->frames[f].bins[k]*wa:std::complex<double>{})+(b?b->frames[f].bins[k]*wb:std::complex<double>{}));
            SpectrumStream::realSymmetry(out,stream.size);
        }
    }
};
EASYKNACK_NODE(SpectrumMixNode,"spectrummix")
}
