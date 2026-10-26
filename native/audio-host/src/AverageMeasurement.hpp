#pragma once
#include "TriggerInput.hpp"
namespace easyknack {
/** Finite, equally weighted acquisition. Settings are latched at Start. */
struct AverageMeasurement {
    ManualTriggerReceiver start;
    bool measurements=false,active=false,countMode=false;
    double duration=1;
    std::uint64_t requestedCount=16,count=0,elapsed=0,target=1;
    void begin(double rate)noexcept{
        active=true;count=elapsed=0;countMode=measurements;
        target=countMode?std::max<std::uint64_t>(1,requestedCount):
            static_cast<std::uint64_t>(std::max(1.,std::ceil(duration*rate-1e-6)));
    }
    void record()noexcept{++count;}
    bool tick()noexcept{
        if(!active)return false;
        ++elapsed;
        if(countMode?count>=target:elapsed>=target){active=false;return true;}
        return false;
    }
    template<class P> void configure(const P& p){
        if(p.mode_value!="time"&&p.mode_value!="measurements")throw std::invalid_argument("Invalid averaging mode");
        measurements=p.mode_value=="measurements";duration=p.duration_value;requestedCount=p.measurements_value;
        if(!std::isfinite(duration)||duration<=0||duration>86400||requestedCount<1||requestedCount>16777216)throw std::invalid_argument("Invalid averaging duration");
    }
    void parameters(const AverageMeasurement& p)noexcept{measurements=p.measurements;duration=p.duration;requestedCount=p.requestedCount;}
    static void finish(ProcessContext& c,std::size_t i)noexcept{
        if(auto* pulses=c.outputAt(2))pulses[i]=1;
        if(auto* events=c.eventOutput(2))events->push(static_cast<std::uint32_t>(c.sampleOffset+i),1,ValueEventKind::trigger);
    }
    static void clearFinished(ProcessContext& c)noexcept{
        if(auto* pulses=c.outputAt(2))std::fill_n(pulses,c.bufferSize,0.f);
        if(auto* events=c.eventOutput(2))events->initial=0;
    }
};
}
