#pragma once
#include <atomic>
#include <condition_variable>
#include <mutex>
#include <thread>
#include <memory>
#if defined(_WIN32)
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#endif
namespace easyknack {
/** One persistent helper shared by all spectral groups. Contention falls back to serial.
 * Construct/destruct outside rendering; a successful submit must be joined before
 * releasing its borrowed frame. No work queue, callback allocation or detached jobs. */
class SpectralWorker {
    std::mutex mutex;
    std::condition_variable ready;
    std::atomic<bool> occupied{false},finished{true};
    bool pending=false,stopping=false;
    void (*function)(void*)noexcept=nullptr;void* argument=nullptr;
    std::thread thread;
    void loop(){
#if defined(_WIN32)
        SetThreadPriority(GetCurrentThread(),THREAD_PRIORITY_HIGHEST);
#endif
        std::unique_lock<std::mutex> lock(mutex);
        for(;;){
            ready.wait(lock,[&]{return pending||stopping;});if(stopping)return;
            auto run=function;auto data=argument;pending=false;lock.unlock();run(data);
            finished.store(true,std::memory_order_release);lock.lock();
        }
    }
public:
    SpectralWorker():thread([this]{loop();}){}
    ~SpectralWorker(){ {std::lock_guard<std::mutex> lock(mutex);stopping=true;}ready.notify_one();thread.join();}
    SpectralWorker(const SpectralWorker&)=delete;
    static std::shared_ptr<SpectralWorker> shared(){
        // Called from graph preparation only. Keep one worker until process shutdown.
        static auto worker=std::make_shared<SpectralWorker>();return worker;
    }
    bool submit(void (*run)(void*)noexcept,void* data)noexcept{
        std::unique_lock<std::mutex> lock(mutex,std::try_to_lock);
        if(!lock.owns_lock()||occupied.exchange(true,std::memory_order_acquire))return false;
        function=run;argument=data;pending=true;finished.store(false,std::memory_order_relaxed);
        ready.notify_one();return true;
    }
    void join()noexcept{
        while(!finished.load(std::memory_order_acquire))std::this_thread::yield();
        occupied.store(false,std::memory_order_release);
    }
};
}
