#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../WaveformResource.hpp"
#include <cstring>
#include <algorithm>
#include <utility>
#include <vector>
namespace easyknack {
struct WaveformNode:NodeProcessor {
    std::shared_ptr<const WaveformResource> wave;
    bool supportsControlRate()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::waveform;}
    DspResource* resourceOutput(std::size_t)noexcept override{return const_cast<WaveformResource*>(wave.get());}
    void configure(std::istream& in)override{
        auto p=parameters::WaveformNodeParameters::read(in);sources.clear();
        // Immutable tables are shared across instances and parameter snapshots.
        // The bounded control-thread cache keeps no resource alive on its own.
        static thread_local std::vector<std::pair<std::string,std::weak_ptr<const WaveformResource>>> cache;
        for(auto& entry:cache)if(entry.first==p.curve_value)if(auto existing=entry.second.lock()){wave=std::move(existing);return;}
        auto next=std::make_shared<WaveformResource>();
        std::array<float,WaveformResource::size> samples{};
        if(p.curve_value.size()!=samples.size()*8)throw std::runtime_error("Invalid wavetable length");
        for(std::size_t i=0;i<samples.size();i++){
            auto token=p.curve_value.substr(i*8,8);std::size_t used=0;auto bits=static_cast<std::uint32_t>(std::stoul(token,&used,16));
            if(used!=8)throw std::runtime_error("Invalid wavetable sample");std::memcpy(&samples[i],&bits,sizeof(float));
            if(!std::isfinite(samples[i]))throw std::runtime_error("Non-finite wavetable sample");
        }
        next->fromSamples(samples);wave=std::move(next);
        cache.erase(std::remove_if(cache.begin(),cache.end(),[](const auto& entry){return entry.second.expired();}),cache.end());
        if(cache.size()>=32)cache.erase(cache.begin());
        cache.emplace_back(p.curve_value,wave);
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    std::shared_ptr<const void> takeParameterResource()noexcept override{return std::move(wave);}
    void applyParameters(const NodeProcessor& other)noexcept override{wave=static_cast<const WaveformNode&>(other).wave;}
    void process(ProcessContext&)noexcept override{}
};
EASYKNACK_NODE(WaveformNode,"waveform")
}
