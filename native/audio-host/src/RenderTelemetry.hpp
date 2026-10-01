#pragma once
#include <array>
#include <atomic>
#include <algorithm>
#include <chrono>
#include <cmath>
#include <cstdint>
#include <deque>
#include <sstream>
#include <vector>
namespace easyknack {
/** Single render writer; single protocol reader. Allocation and sorting only in snapshot(). */
struct RenderTelemetry {
    struct Sample { double seconds=0; unsigned frames=0; double rate=0; std::uint64_t time=0; };
    static constexpr unsigned capacity=4096;
    std::array<Sample,capacity> queue{};
    std::atomic<std::uint64_t> written{0},read{0},dropped{0};
    std::atomic<std::uint64_t> resetSerial{0};
    std::deque<Sample> window;
    std::vector<double> percentileScratch; // Protocol-reader-only reusable storage.
    std::uint64_t epoch=1,total=0,overBudget=0,dropBase=0,trimmed=0;
    double maximum=0;
    static std::uint64_t now(){return std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::steady_clock::now().time_since_epoch()).count();}
    void observe(double seconds,unsigned frames,double rate,std::uint64_t time=now())noexcept{
        if(!frames||!std::isfinite(seconds)||seconds<0||!std::isfinite(rate)||rate<=0)return;
        auto w=written.load(std::memory_order_relaxed),r=read.load(std::memory_order_acquire);
        if(w-r==capacity){dropped.fetch_add(1,std::memory_order_relaxed);return;}
        queue[w%capacity]={seconds,frames,rate,time};written.store(w+1,std::memory_order_release);
    }
    void reset(){read.store(written.load(std::memory_order_acquire),std::memory_order_release);window.clear();total=overBudget=trimmed=0;maximum=0;dropBase=dropped.load();++epoch;resetSerial.fetch_add(1,std::memory_order_release);}
    struct Snapshot {std::uint64_t epoch,total,overBudget,dropped;double mean,peak,p99us,maxUs;unsigned frames;double rate;};
    Snapshot snapshot(std::uint64_t time=now()){
        auto r=read.load(std::memory_order_relaxed),w=written.load(std::memory_order_acquire);
        while(r<w){auto sample=queue[r++%capacity];window.push_back(sample);++total;maximum=std::max(maximum,sample.seconds);if(sample.seconds>sample.frames/sample.rate)++overBudget;}
        read.store(r,std::memory_order_release);
        while(!window.empty()&&window.front().time+1000000000ull<time)window.pop_front();
        while(window.size()>65536){window.pop_front();++trimmed;}
        double duration=0,budget=0,peak=0;auto& times=percentileScratch;times.clear();times.reserve(window.size());
        for(const auto& s:window){duration+=s.seconds;budget+=s.frames/s.rate;peak=std::max(peak,s.seconds*s.rate/s.frames*100);times.push_back(s.seconds*1e6);}
        if(!times.empty())std::nth_element(times.begin(),times.begin()+(times.size()*99+99)/100-1,times.end());
        return {epoch,total,overBudget,dropped.load()-dropBase+trimmed,budget?duration/budget*100:0,peak,
            times.empty()?0:times[(times.size()*99+99)/100-1],maximum*1e6,
            window.empty()?0:window.back().frames,window.empty()?0:window.back().rate};
    }
};
}
