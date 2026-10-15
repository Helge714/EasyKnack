#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../WaveformResource.hpp"
namespace easyknack {
struct WaveformNode:NodeProcessor {
    std::shared_ptr<const WaveformResource> wave;
    bool supportsControlRate()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::waveform;}
    DspResource* resourceOutput(std::size_t)noexcept override{return const_cast<WaveformResource*>(wave.get());}
    void configure(std::istream& in)override{
        auto p=parameters::WaveformNodeParameters::read(in);sources.clear();auto next=std::make_shared<WaveformResource>();
        std::array<float,WaveformResource::size> samples{};
        for(std::size_t i=0;i<samples.size();i++)samples[i]=float(2*p.curve_value.sample(double(i)/samples.size())-1);
        next->fromSamples(samples);wave=std::move(next);
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    std::shared_ptr<const void> takeParameterResource()noexcept override{return std::move(wave);}
    void applyParameters(const NodeProcessor& other)noexcept override{wave=static_cast<const WaveformNode&>(other).wave;}
    void process(ProcessContext&)noexcept override{}
};
EASYKNACK_NODE(WaveformNode,"waveform")
}
