#pragma once
#include "../NodeProcessor.hpp"
#include "Dynamics.hpp"
#include <array>
namespace easyknack::dynamics {
inline float rightSample(ProcessContext& c,std::size_t port,std::size_t i,float fallback)noexcept {
    const float* p=c.rightInputs&&port<c.inputCount?c.rightInputs[port]:nullptr;
    return p?p[c.sourceRates&&c.sourceRates[port]=='c'?0:i]:fallback;
}
struct StereoState {std::vector<float> right;double rate=0;};
}
