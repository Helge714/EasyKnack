#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../PartialRandom.hpp"
namespace easyknack {
/** Counter-based random values independent of sorting, traversal order and channel. */
struct PartialNoiseNode : NodeProcessor {
    std::uint64_t seed=0,partialId=0;std::int64_t frameStart=0;bool perPartial=true,perFrame=true,active=false;
    bool supportsLiveParameters()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::PartialNoiseNodeParameters::read(in);sources=p.sources();seed=std::uint64_t(p.seed_value);perPartial=p.by_partial_value;perFrame=p.by_frame_value;}
    void applyParameters(const NodeProcessor& p)noexcept override{const auto& o=static_cast<const PartialNoiseNode&>(p);seed=o.seed;perPartial=o.perPartial;perFrame=o.perFrame;}
    void process(ProcessContext& c)noexcept override{
        if(!active){c.output[0]=0;return;}
        auto key=partialHash(counterSeed(c,seed))^partialHash((perPartial?partialId:0)+0x243f6a8885a308d3ULL);
        if(perFrame)key^=partialHash(std::uint64_t(frameStart)+0x13198a2e03707344ULL);
        c.output[0]=float(partialHash(key)>>40)*(1.f/16777216.f);
    }
};
EASYKNACK_NODE(PartialNoiseNode,"partialnoise")
}
