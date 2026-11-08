#pragma once
#include <cstddef>
#include <cstdint>
#include <algorithm>
#include <numeric>
namespace easyknack {
/** Render-thread-local cadence discovery, including nested and compiled graphs.
 * Report on every block, even when no spectrum frame is ready. No allocations. */
struct RenderCadence {
    inline static thread_local unsigned period=1;
    inline static thread_local bool bounded=false;
    static constexpr unsigned maxCycle=131072;
    static void begin() noexcept {period=1;bounded=false;}
    static void include(std::size_t hop) noexcept {
        if(!hop||hop>65536)return;
        auto next=std::uint64_t(period/std::gcd(period,static_cast<unsigned>(hop)))*hop;
        bounded=bounded||next>maxCycle;
        period=bounded?maxCycle:static_cast<unsigned>(next);
    }
    static unsigned cycle(unsigned frames,unsigned hop)noexcept {
        auto exact=std::uint64_t(frames/std::gcd(frames,std::max(1u,hop)))*std::max(1u,hop);
        // Arbitrary overlap ratios can have enormous common multiples. Keep
        // feedback responsive; bounded windows may contain +/- one frame per FFT.
        return exact<=maxCycle?static_cast<unsigned>(exact):((maxCycle+frames-1)/frames)*frames;
    }
};
}
