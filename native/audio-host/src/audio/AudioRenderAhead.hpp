#pragma once
#include "AudioBackend.hpp"
#include <chrono>
#include <condition_variable>
#include <mutex>
#include <thread>
#include "../platform/ThreadPriority.hpp"

namespace easyknack {
/** A single DSP producer, driven by device consumption rather than a second clock.
 * The callback never waits for DSP. Enable only between direct render callbacks;
 * stop/join before replacing routes or transferring DSP ownership to ASIO. */
class AudioRenderAhead {
public:
    static constexpr unsigned block=256,maximumReserve=65536;
    using Render = unsigned (*)(void*,float*,unsigned) noexcept;
private:
    std::unique_ptr<StereoRing<131072>> queue=std::make_unique<StereoRing<131072>>();
    std::atomic<bool> enabled{false},stopping{false};
    std::atomic<std::uint64_t> requested{0};
    std::atomic<std::uint64_t> completed{0};
    std::atomic<unsigned> reserve{0};
    std::thread worker;
    std::mutex mutex;
    std::condition_variable wake;
    static unsigned rounded(unsigned period)noexcept {
        return std::min(maximumReserve,std::max(block*2,((period+block-1)/block)*block+block*2));
    }
public:
    ~AudioRenderAhead(){stop();}
    bool active()const noexcept{return enabled.load(std::memory_order_acquire);}
    unsigned latencyFrames()const noexcept{return reserve.load(std::memory_order_relaxed);}
    std::uint64_t renderedFrames()const noexcept{return completed.load(std::memory_order_acquire);}
    std::uint64_t underruns()const noexcept{return queue->underruns.load();}
    std::uint64_t overruns()const noexcept{return queue->overruns.load();}
    // Control thread only; no thread creation or destruction in the device callback.
    void start(Render render,void* context){
        stop();queue->reset();enabled.store(false);stopping.store(false);requested.store(0);completed.store(0);reserve.store(0);
        worker=std::thread([this,render,context]{
            platform::prioritizeAudioWorker();
            std::uint64_t produced=0;std::array<float,block*2> samples{};
            while(!stopping.load(std::memory_order_acquire)){
                if(!active()||requested.load(std::memory_order_acquire)-produced<block){
                    std::unique_lock<std::mutex> lock(mutex);
                    // Timed predicate also covers a notify racing the wait without
                    // requiring the real-time callback to acquire this mutex.
                    wake.wait_for(lock,std::chrono::microseconds(500),[&]{return stopping.load()||(active()&&requested.load()-produced>=block);});
                    continue;
                }
                auto period=render(context,samples.data(),block);
                auto next=rounded(period),previous=reserve.load();
                if(next>previous){queue->write(nullptr,next-previous);reserve.store(next);}
                queue->write(samples.data(),block);produced+=block;completed.store(produced,std::memory_order_release);
            }
        });
    }
    // The direct callback is still the sole producer here. The release store
    // transfers graph/IO ownership and the initialized queue to the worker.
    void enable(unsigned period)noexcept{
        if(active())return;
        auto count=rounded(period);queue->write(nullptr,count);reserve.store(count);
        enabled.store(true,std::memory_order_release);wake.notify_one();
    }
    void read(float* output,unsigned frames)noexcept{
        queue->read(output,frames);
        requested.fetch_add(frames,std::memory_order_release);wake.notify_one();
    }
    void stop()noexcept{
        stopping.store(true,std::memory_order_release);wake.notify_one();
        if(worker.joinable())worker.join();enabled.store(false,std::memory_order_release);
    }
};
}
