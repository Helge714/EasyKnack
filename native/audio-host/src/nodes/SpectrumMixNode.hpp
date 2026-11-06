#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
struct SpectrumMixNode : NodeProcessor {
    SpectrumStream stream;double gainA=1,gainB=1;
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{const auto& o=static_cast<const SpectrumMixNode&>(p);gainA=o.gainA;gainB=o.gainB;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{auto p=parameters::SpectrumMixNodeParameters::read(in);sources=p.sources();gainA=p.gain_a_value;gainB=p.gain_b_value;}
    void prepare(double,std::size_t block)override{stream.prepare(block);}
    void process(ProcessContext& c)noexcept override{
        auto* a=static_cast<const SpectrumStream*>(c.resource(0));auto* b=static_cast<const SpectrumStream*>(c.resource(1));
        if(a&&!a->size)a=nullptr;if(b&&!b->size)b=nullptr;
        if(!a&&!b){stream.size=stream.count=0;return;}
        if(a&&b&&(a->size!=b->size||a->windowFrames()!=b->windowFrames()||a->window!=b->window||a->hop!=b->hop||a->sampleRate!=b->sampleRate||a->count!=b->count||a->blockStart!=b->blockStart)){stream.size=stream.count=0;return;}
        stream.metadata(a?*a:*b);
        for(std::size_t f=0;f<stream.count;f++){
            if(a&&b&&a->frames[f].start!=b->frames[f].start){stream.size=stream.count=0;return;}
            auto& out=stream.frames[f];out.start=(a?a:b)->frames[f].start;
            for(std::size_t k=0;k<stream.size;k++)out.bins[k]=(a?a->frames[f].bins[k]*gainA:std::complex<double>{})+(b?b->frames[f].bins[k]*gainB:std::complex<double>{});
        }
    }
};
EASYKNACK_NODE(SpectrumMixNode,"spectrummix")
}
