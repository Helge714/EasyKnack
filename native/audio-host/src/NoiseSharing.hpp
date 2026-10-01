#pragma once
#include "NodeProcessor.hpp"
#include <array>
#include <algorithm>
#include <string>

namespace easyknack {
inline std::uint32_t noiseIdentitySeed(const std::string& id)noexcept {
    std::uint32_t seed=2166136261u;for(unsigned char c:id){seed^=c;seed*=16777619u;}return seed?seed:1;
}
inline std::uint32_t noiseExplicitSeed(int selected)noexcept {
    std::uint32_t seed=static_cast<std::uint32_t>(selected)+0x9e3779b9u;
    seed^=seed>>16;seed*=0x7feb352du;seed^=seed>>15;return seed?seed:1;
}
inline int noiseSeedValue(float value)noexcept {
    return std::isfinite(value)?int(std::clamp(std::floor(value),-1.f,16777215.f)):-1;
}
// Owned by a logical node in an Instance group; no global mutable DSP cache.
// Exact pre-state and inputs are required: equal seeds alone are insufficient.
struct NoiseBlockCache {
    using Key=std::array<double,16>;
    Key key{};
    std::array<double,8> after{};
    std::array<std::array<float,256>,3> output{};
    std::array<std::array<float,256>,2> inputs{};
    std::array<std::size_t,2> strides{};
    std::size_t frames=0,ports=0;double sampleRate=0;bool valid=false;
    std::uint64_t renders=0,hits=0;
    template<class Render,class Restore,class Capture>
    void process(ProcessContext& c,const Key& before,Render render,Restore restore,Capture capture,std::size_t comparedInputs=0){
        bool hit=valid&&key==before&&frames==c.bufferSize&&ports==c.outputCount&&sampleRate==c.sampleRate;
        for(std::size_t p=0;p<comparedInputs;p++){
            auto input=c.numericInput(p); // Null-input fallback is already part of the key.
            hit&=strides[p]==input.stride;strides[p]=input.stride;
            if(input)for(std::size_t i=0;i<(input.stride?c.bufferSize:1);i++){float value=input[i];hit&=inputs[p][i]==value;inputs[p][i]=value;}
        }
        if(hit){++hits;restore(after);}
        else {
            key=before;frames=c.bufferSize;ports=c.outputCount;sampleRate=c.sampleRate;valid=true;++renders;
            float* outputs[]{output[0].data(),output[1].data(),output[2].data()};auto inner=c;
            inner.output=outputs[0];inner.outputs=outputs;
            render(inner);after=capture();
        }
        for(std::size_t port=0;port<std::min(c.outputCount,output.size());port++)
            if(auto* out=c.outputAt(port))std::copy_n(output[port].data(),frames,out);
        if(hit)if(auto* events=c.eventOutput(1))for(std::size_t i=0;i<frames;i++)
            if(output[1][i]!=0)events->push(static_cast<std::uint32_t>(c.sampleOffset+i),1,ValueEventKind::trigger);
    }
};
}
