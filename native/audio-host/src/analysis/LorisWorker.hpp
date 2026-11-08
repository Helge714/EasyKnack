#pragma once
#include <condition_variable>
#include <mutex>
#include <thread>
namespace easyknack {
/** One bounded rendezvous, no audio-thread container allocation or job queue.
 * The caller waits for this frame: worker execution is part of the DSP deadline. */
class LorisWorker {
    std::mutex mutex;
    std::condition_variable ready,done;
    bool pending=false,stopping=false;
    void (*function)(void*)noexcept=nullptr;void* argument=nullptr;
    std::thread thread;
    void loop(){
        std::unique_lock<std::mutex> lock(mutex);
        for(;;){
            ready.wait(lock,[&]{return pending||stopping;});if(stopping)return;
            auto run=function;auto data=argument;lock.unlock();run(data);lock.lock();
            pending=false;done.notify_one();
        }
    }
public:
    LorisWorker():thread([this]{loop();}){}
    ~LorisWorker(){ {std::lock_guard<std::mutex> lock(mutex);stopping=true;}ready.notify_one();thread.join();}
    LorisWorker(const LorisWorker&)=delete;
    void execute(void (*run)(void*)noexcept,void* data)noexcept{
        std::unique_lock<std::mutex> lock(mutex);
        function=run;argument=data;pending=true;ready.notify_one();
        done.wait(lock,[&]{return !pending;});
    }
};
}
