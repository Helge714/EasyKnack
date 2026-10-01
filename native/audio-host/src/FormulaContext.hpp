#pragma once
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include "FormulaNumeric.hpp"
namespace easyknack {
// ABI 6: typed persistent state; no ownership or C++ containers cross DLL boundaries.
struct FormulaContext {
    double sampleRate=48000;
    std::uint64_t blockSize=0,sampleIndex=0,samplePosition=0,seed=0;
    void* user=nullptr;
    float (*readBuffer)(void*,unsigned,double,bool,bool,std::uint64_t)=nullptr;
    float (*bufferSize)(void*,unsigned)=nullptr;
    float (*bufferInfo)(void*,unsigned,unsigned,std::uint64_t)=nullptr;
    float (*timelineRead)(void*,unsigned,double,unsigned,unsigned,std::uint64_t)=nullptr;
    float (*inputRate)(void*,unsigned)=nullptr;
    float* state=nullptr; // Host-owned, one bank per processor/channel/voice.
    std::uint64_t* typedState=nullptr;
    const float* sineTable=nullptr; // Borrowed 2048-point oscillator table plus guard sample, prepared off the audio thread.
    float sineWave(double phase)const noexcept {
        if(!sineTable||!std::isfinite(phase))return 0;
        double position=(phase-std::floor(phase))*2048.0;
        auto index=static_cast<unsigned>(position);
        // A tiny negative phase may round the wrapped phase up to exactly one.
        if(index>=2048)return sineTable[0];
        double first=sineTable[index],second=sineTable[index+1];
        return static_cast<float>(first+(second-first)*(position-index));
    }
    float info(unsigned port,unsigned field)const noexcept{return bufferInfo?bufferInfo(user,port,field,sampleIndex):0;}
    float rate(unsigned port)const noexcept{return inputRate?inputRate(user,port):0;}
    float readTimeline(unsigned port,double index,unsigned mode=0,unsigned interpolation=1)const noexcept{return timelineRead?timelineRead(user,port,index,mode,interpolation,sampleIndex):0;}
    float read(unsigned port,double index,bool wrap=false,bool interpolate=true)const noexcept {
        return readBuffer?readBuffer(user,port,index,wrap,interpolate,sampleIndex):0.f;
    }
    float size(unsigned port)const noexcept{return bufferSize?bufferSize(user,port):0.f;}
    static std::uint64_t hash(std::uint64_t x)noexcept {
        x+=0x9e3779b97f4a7c15ULL;x=(x^(x>>30))*0xbf58476d1ce4e5b9ULL;
        x=(x^(x>>27))*0x94d049bb133111ebULL;return x^(x>>31);
    }
    float randomSeed(unsigned site,float low,float high,std::uint64_t selected)const noexcept {
        if(!std::isfinite(low)||!std::isfinite(high))return 0;
        if(high<low)std::swap(low,high);
        float unit=float(hash(selected^hash(samplePosition)^hash(site))>>40)*(1.f/16777216.f);
        return float(double(low)+(double(high)-double(low))*unit);
    }
    float random(unsigned site,float low,float high)const noexcept{return randomSeed(site,low,high,seed);}
    float random(unsigned site,float low,float high,float selected)const noexcept {
        if(!std::isfinite(selected))selected=0;if(selected==0)selected=0; // Canonicalize negative zero.
        std::uint32_t bits;std::memcpy(&bits,&selected,sizeof(bits));return randomSeed(site,low,high,hash(bits));
    }
};
}
