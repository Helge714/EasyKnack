#pragma once
#include "RenderTelemetry.hpp"
#include "RenderCadence.hpp"
#include <algorithm>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <limits>

namespace easyknack {
/** One audio-thread writer; the control thread only reads lock-free counters. */
struct AudioTiming {
    RenderTelemetry telemetry;
    static_assert(std::atomic<std::uint32_t>::is_always_lock_free);
    std::atomic<std::uint32_t> average{0}, peak{0}, overBudget{0}; // Percent * 100.
    double smoothed = 0;
    std::uint32_t peakValue = 0, overruns = 0;
    bool initialized = false;
    double cycleSeconds=0,cycleRate=0;
    unsigned cycleFrames=0,cyclePeriod=0;
    std::uint64_t observedReset=0;
    void resetCycle() noexcept {cycleSeconds=0;cycleFrames=cyclePeriod=0;}

    void observe(double seconds, unsigned frames, double sampleRate, unsigned hop=1) noexcept {
        if (frames == 0 || sampleRate <= 0 || !std::isfinite(sampleRate) || seconds < 0 || !std::isfinite(seconds)) return;
        auto reset=telemetry.resetSerial.load(std::memory_order_acquire);
        if(reset!=observedReset){resetCycle();observedReset=reset;initialized=false;peakValue=overruns=0;}
        // Backend callbacks can end with a partial internal block. They must
        // not change the measurement cadence or discard pending FFT work.
        const unsigned period=RenderCadence::cycle(256,hop);
        if(cyclePeriod!=period||cycleRate!=sampleRate){resetCycle();cyclePeriod=period;cycleRate=sampleRate;}
        const double perSample=seconds/frames;
        while(frames){
            const unsigned take=std::min(frames,cyclePeriod-cycleFrames);
            cycleSeconds+=perSample*take;cycleFrames+=take;frames-=take;
            if(cycleFrames==cyclePeriod){publish(cycleSeconds,cycleFrames,sampleRate);cycleSeconds=0;cycleFrames=0;}
        }
    }
    void publish(double seconds,unsigned frames,double sampleRate)noexcept {
        telemetry.observe(seconds,frames,sampleRate);
        double load = seconds * sampleRate / frames * 10000.0;
        // Approximately half a second of smoothing, weighted by actual callback length.
        double alpha = std::min(1.0, frames / (sampleRate * 0.5));
        smoothed = initialized ? smoothed + alpha * (load - smoothed) : load;
        initialized = true;
        auto encode = [](double value) { return static_cast<std::uint32_t>(std::min(
                value, static_cast<double>(std::numeric_limits<std::uint32_t>::max()))); };
        peakValue = std::max(peakValue, encode(load));
        if (load > 10000.0 && overruns < std::numeric_limits<std::uint32_t>::max()) ++overruns;
        average.store(encode(smoothed), std::memory_order_relaxed);
        peak.store(peakValue, std::memory_order_relaxed);
        overBudget.store(overruns, std::memory_order_relaxed);
    }
};
}
