#pragma once
#include <cstdint>
#include "NodeProcessor.hpp"
namespace easyknack {
inline std::uint64_t counterSeed(ProcessContext &context, std::uint64_t fallback) noexcept {
    if (!context.rawInput(0))
        return fallback;
    double value = context.numericInput(0)[0];
    return std::isfinite(value)
               ? static_cast<std::uint64_t>(std::clamp(std::floor(value), 0., 2147483647.))
               : fallback;
}
inline std::uint64_t partialHash(std::uint64_t x) noexcept {
    x += 0x9e3779b97f4a7c15ULL;
    x = (x ^ (x >> 30)) * 0xbf58476d1ce4e5b9ULL;
    x = (x ^ (x >> 27)) * 0x94d049bb133111ebULL;
    return x ^ (x >> 31);
}
// Exact in float, bounded for the ordinary Noise/Digital Noise Seed input.
inline float partialSeed(std::uint64_t id) noexcept { return float(partialHash(id) & 0xffffffULL); }
} // namespace easyknack
