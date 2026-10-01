#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct RateConverterNode : NodeProcessor {
    enum class Reduction { first,last,mean,rms,minimum,maximum,peak };
    Reduction reduction=Reduction::first;
    bool control=true;
    float fallback=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const RateConverterNode&>(configuration);
        fallback=p.fallback;control=p.control;reduction=p.reduction;
    }
    void configure(std::istream& in)override{
        auto p=parameters::RateConverterNodeParameters::read(in);sources=p.sources();fallback=p.value_value;control=p.rate_value=="control";
        if(p.reduction_value=="first")reduction=Reduction::first;
        else if(p.reduction_value=="last")reduction=Reduction::last;
        else if(p.reduction_value=="mean")reduction=Reduction::mean;
        else if(p.reduction_value=="rms")reduction=Reduction::rms;
        else if(p.reduction_value=="minimum")reduction=Reduction::minimum;
        else if(p.reduction_value=="maximum")reduction=Reduction::maximum;
        else reduction=Reduction::peak;
    }
    void process(ProcessContext& c)noexcept override{
        const auto* input=c.input(0);
        auto sample=[&](std::size_t i){float v=input?input[i]:fallback;return std::isfinite(v)?v:0.f;};
        if(!control){for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=sample(i);return;}
        auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        if(!frames)return;
        double value=sample(0);
        switch(reduction){
            case Reduction::first:break;
            case Reduction::last:value=sample(frames-1);break;
            case Reduction::mean:case Reduction::rms:
                value=0;for(std::size_t i=0;i<frames;i++){double v=sample(i);value+=reduction==Reduction::rms?v*v:v;}
                value/=frames;if(reduction==Reduction::rms)value=std::sqrt(value);break;
            case Reduction::minimum:for(std::size_t i=1;i<frames;i++)value=std::min(value,double(sample(i)));break;
            case Reduction::maximum:for(std::size_t i=1;i<frames;i++)value=std::max(value,double(sample(i)));break;
            case Reduction::peak:value=0;for(std::size_t i=0;i<frames;i++)value=std::max(value,std::abs(double(sample(i))));break;
        }
        std::fill_n(c.output,c.bufferSize,static_cast<float>(value));
    }
};
EASYKNACK_NODE(RateConverterNode,"rateconvert")
}
