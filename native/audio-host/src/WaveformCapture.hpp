#pragma once
#include "WaveformResource.hpp"
#include <atomic>
#include <thread>
#include <chrono>
namespace easyknack {
/** Single pending capture. Audio only copies a snapshot and exchanges ready buffers. */
struct WaveformCapture {
    SpectrumFrame spectrum;
    double fundamental=440;
    bool normalize=true;
    std::unique_ptr<WaveformResource> result=std::make_unique<WaveformResource>();
    std::atomic<int> state{0}; // idle, submitted, ready
    std::atomic<bool> stopping{false};
    std::thread worker;
    WaveformCapture():worker([this]{run();}){}
    ~WaveformCapture(){stopping.store(true);if(worker.joinable())worker.join();}
    static void extract(const SpectrumFrame& source,double hz,bool normalize,WaveformResource& output)noexcept{
        std::array<std::complex<double>,WaveformResource::size> bins{};
        if(!source.valid()||!std::isfinite(hz)||hz<=0||source.windowSum<=0){output.build(bins);return;}
        double center=source.windowFrames*.5;
        auto centered=[&](std::size_t k){return source.bins[k]*std::polar(1.,Wavetables::tau*k*center/source.fftFrames);};
        for(std::size_t h=1;h<WaveformResource::size/2;h++){
            double bin=h*hz*source.fftFrames/source.sampleRate;
            if(bin>=source.fftFrames/2.)break;
            auto k=static_cast<std::size_t>(bin);double fraction=bin-k;
            auto coefficient=centered(k)*(1-fraction)+centered(k+1)*fraction;
            bins[h]=coefficient*(double(WaveformResource::size)/source.windowSum);
            bins[WaveformResource::size-h]=std::conj(bins[h]);
        }
        output.build(bins,normalize);
    }
    bool submit(const SpectrumFrame& frame,double hz,bool norm)noexcept{
        if(state.load(std::memory_order_acquire)!=0)return false;
        spectrum=frame;fundamental=hz;normalize=norm;state.store(1,std::memory_order_release);return true;
    }
    void run()noexcept{
        while(!stopping.load(std::memory_order_relaxed)){
            if(state.load(std::memory_order_acquire)==1){extract(spectrum,fundamental,normalize,*result);state.store(2,std::memory_order_release);}
            else std::this_thread::sleep_for(std::chrono::milliseconds(2));
        }
    }
};
}
