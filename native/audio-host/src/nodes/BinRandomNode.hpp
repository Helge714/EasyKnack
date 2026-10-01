#pragma once
#include "BinContextNode.hpp"
#include <cstdint>
namespace easyknack {
/** Counter-based randomness: independent of traversal order, channel and graph rebuilds. */
struct BinRandomNode : BinAccess {
    std::uint64_t seed=0;bool perFrame=false,perBin=true;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    void configure(std::istream& in)override{
        auto p=parameters::BinRandomNodeParameters::read(in);sources=p.sources();seed=static_cast<std::uint64_t>(p.seed_value);perFrame=p.mode_value=="frame"||p.mode_value=="shared-frame";
        perBin=p.mode_value!="constant"&&p.mode_value!="shared-frame";
    }
    static std::uint64_t hash(std::uint64_t x)noexcept{
        x+=0x9e3779b97f4a7c15ULL;x=(x^(x>>30))*0xbf58476d1ce4e5b9ULL;
        x=(x^(x>>27))*0x94d049bb133111ebULL;return x^(x>>31);
    }
    void process(ProcessContext& c)noexcept override{
        if(!frame){c.output[0]=0;return;}
        auto key=hash(seed)^hash(static_cast<std::uint64_t>(perBin?bin:0)+0x243f6a8885a308d3ULL);
        if(perFrame)key^=hash(static_cast<std::uint64_t>(frame->start)+0x13198a2e03707344ULL);
        c.output[0]=static_cast<float>(hash(key)>>40)*(1.f/16777216.f);
    }
};
EASYKNACK_NODE(BinRandomNode,"binrandom")
}
