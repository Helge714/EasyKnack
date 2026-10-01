#pragma once
#include "nodes/OscillatorNode.hpp"
#include "nodes/MathNode.hpp"
#include "nodes/FilterNode.hpp"
namespace easyknack {
// Closed audio-rate chain only. Processor instances retain the ordinary graph's
// state, so state transfer and parameter smoothing do not need a second model.
template<bool Fused=false>
inline void renderOscGainFilter(OscillatorNode& osc,MathNode& gain,FilterNode& filter,
        float* a,float* b,float* output,std::size_t frames,double rate) noexcept {
    if(osc.reset.take())osc.resetPhase(); // Closed chain: Reset has no external source.
    const double prepared=1-std::exp(-1.0/(.005*rate));
    const float smoothing=static_cast<float>(prepared);
    auto multiply=[&](float value){
        gain.currentB+=smoothing*(gain.bValue-gain.currentB); // A is connected; only the manual B fallback glides.
        return MathNode::evaluate(value,gain.currentB,MathOperation::multiply,false);
    };
    if constexpr(Fused){
        for(std::size_t i=0;i<frames;i++){
            float value;
#if defined(__clang__)
            [[clang::always_inline]]
#endif
            osc.renderPrepared(&value,1,osc.frequency,rate,nullptr,nullptr,true,prepared);
            value=multiply(value);const float* inputs[]={&value,nullptr,nullptr};
            ProcessContext context{inputs,3,output+i,1,rate,nullptr};
#if defined(__clang__)
            [[clang::always_inline]]
#endif
            filter.processPrepared(context,prepared);
        }
    }else{
#if defined(__clang__)
        [[clang::always_inline]]
#endif
        osc.renderPrepared(a,frames,osc.frequency,rate,nullptr,nullptr,true,prepared);
        for(std::size_t i=0;i<frames;i++)b[i]=multiply(a[i]);
        const float* inputs[]={b,nullptr,nullptr};
        ProcessContext context{inputs,3,output,frames,rate,nullptr};
#if defined(__clang__)
        [[clang::always_inline]]
#endif
        filter.processPrepared(context,prepared);
    }
}
}
