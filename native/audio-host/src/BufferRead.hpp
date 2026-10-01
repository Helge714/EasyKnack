#pragma once
#include "DspResources.hpp"
namespace easyknack {
inline double wrapBufferPosition(double position,double length)noexcept{
    double value=std::fmod(position,length);if(value<0)value+=length;return value>=length?0:value;
}
struct BufferReadResult {float audio,distance,position;};
inline BufferReadResult readBufferPosition(const HistoryResource& history,double position,std::size_t sample)noexcept{
    const auto size=history.samples.size();double length=static_cast<double>(size);
    position=wrapBufferPosition(position,length);
    auto first=static_cast<std::size_t>(position),second=(first+1)%size;
    float a=history.valueAt(first,sample),b=history.valueAt(second,sample);
    double separation=std::abs(position-static_cast<double>(history.writeHeadAt(sample)));
    return {a+(b-a)*static_cast<float>(position-first),static_cast<float>(2*std::min(separation,length-separation)/length),
        std::min(static_cast<float>(position/length),std::nextafter(1.f,0.f))};
}
// Absolute sample timeline: never interpolate across the newest/oldest valid frame.
inline double bufferHeadAt(const HistoryResource& h,std::size_t sample)noexcept{
 return static_cast<double>(h.end)-(h.savedFrames?h.savedFrames-std::min(sample+1,h.savedFrames):0);
}
inline double clampBufferTimeline(const HistoryResource& h,double position,std::size_t sample)noexcept{
 double head=bufferHeadAt(h,sample),low=std::max(0.,head-h.samples.size()),high=std::max(low,head-1);
 return std::clamp(position,low,high);
}
inline BufferReadResult readBufferClamped(const HistoryResource& h,double position,std::size_t sample)noexcept{
 double head=bufferHeadAt(h,sample),length=static_cast<double>(h.samples.size());
 if(head<=0)return {0,0,0};
 position=clampBufferTimeline(h,position,sample);
 auto first=static_cast<std::uint64_t>(position),second=std::min(first+1,static_cast<std::uint64_t>(head-1));
 float a=h.valueAt(first%h.samples.size(),sample),b=h.valueAt(second%h.samples.size(),sample);
 double physical=wrapBufferPosition(position,length),separation=std::abs(physical-h.writeHeadAt(sample));
 return {a+(b-a)*static_cast<float>(position-first),static_cast<float>(2*std::min(separation,length-separation)/length),static_cast<float>(physical/length)};
}
inline double positionBufferTimeline(const HistoryResource& h,double position,std::size_t sample)noexcept{
 double length=static_cast<double>(h.samples.size()),head=bufferHeadAt(h,sample);
 double physical=std::clamp(position,0.,1.)*length;physical=std::min(physical,length-1);
 if(h.immutable||head<=length)return physical;
 double cycle=std::floor((head-1)/length)*length,target=cycle+physical;
 if(target>head-1)target-=length;
 return target;
}}
