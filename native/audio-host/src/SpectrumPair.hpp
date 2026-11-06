#pragma once
#include "SpectrumStream.hpp"
namespace easyknack {
/** Validate the entire batch before publishing any output frame. Missing inputs are zero. */
inline bool prepareSpectrumPair(SpectrumStream& output,const SpectrumStream*& a,const SpectrumStream*& b)noexcept{
    if(a&&!a->size)a=nullptr;if(b&&!b->size)b=nullptr;
    auto empty=[&](){output.size=output.count=0;return false;};
    if(!a&&!b)return empty();
    if(a&&b){
        if(a->size!=b->size||a->windowFrames()!=b->windowFrames()||a->window!=b->window||a->hop!=b->hop||a->sampleRate!=b->sampleRate||a->count!=b->count||a->blockStart!=b->blockStart)return empty();
        for(std::size_t f=0;f<a->count;f++)if(a->frames[f].start!=b->frames[f].start)return empty();
    }
    output.metadata(a?*a:*b);return true;
}
inline std::complex<double> finiteSpectrumValue(std::complex<double> value)noexcept{
    return std::isfinite(value.real())&&std::isfinite(value.imag())?value:std::complex<double>{};
}
}
