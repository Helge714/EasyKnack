#pragma once
#include <cstdint>
namespace easyknack {
inline std::uint64_t partialHash(std::uint64_t x)noexcept{
    x+=0x9e3779b97f4a7c15ULL;x=(x^(x>>30))*0xbf58476d1ce4e5b9ULL;
    x=(x^(x>>27))*0x94d049bb133111ebULL;return x^(x>>31);
}
// Exact in float, bounded for the ordinary Noise/Digital Noise Seed input.
inline float partialSeed(std::uint64_t id)noexcept{return float(partialHash(id)&0xffffffULL);}
}
