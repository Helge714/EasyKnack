// Offline experiment only: this barrier deliberately does NOT run in the audio callback.
#include "AudioGraph.hpp"
#include <algorithm>
#include <chrono>
#include <condition_variable>
#include <fstream>
#include <iostream>
#include <mutex>
#include <thread>
using namespace easyknack;
using Clock=std::chrono::steady_clock;
struct Work {
    std::array<AdditiveOscillatorNode,64> nodes;
    std::array<std::array<float,256>,64> outputs{};
    std::array<float,256> silence{};
    int voices=1,frames=256,channels=1;
    Work(){int lane=0;for(auto& n:nodes){std::istringstream p("-1 sine 64 1 0 -1 "+std::to_string(220+7*lane++)+" -1 0");n.configure(p);}}
    void run(int worker,int workers){for(int v=worker;v<voices*channels;v+=workers){
        ProcessContext c{nullptr,0,outputs[v].data(),static_cast<std::size_t>(frames),48000,silence.data(),static_cast<std::size_t>(frames)};nodes[v].process(c);
    }}
};
struct Workers {
    Work& work;std::mutex mutex;std::condition_variable start,finished;std::vector<std::thread> threads;
    unsigned epoch=0;int pending=0;bool stop=false;
    explicit Workers(Work& w):work(w){for(int id=0;id<4;id++)threads.emplace_back([this,id]{unsigned seen=0;std::unique_lock<std::mutex> lock(mutex);for(;;){
        start.wait(lock,[&]{return stop||epoch!=seen;});if(stop)return;seen=epoch;lock.unlock();work.run(id,4);lock.lock();if(--pending==0)finished.notify_one();
    }});}
    void run(){std::unique_lock<std::mutex> lock(mutex);pending=4;epoch++;start.notify_all();finished.wait(lock,[&]{return pending==0;});}
    ~Workers(){{std::lock_guard<std::mutex> lock(mutex);stop=true;}start.notify_all();for(auto& t:threads)t.join();}
};
template<class F>void measure(std::ostream& out,const char* mode,int voices,int frames,int channels,F run){
    for(int i=0;i<20;i++)run();std::vector<double> times;times.reserve(300);double sum=0;int misses=0;
    for(int i=0;i<300;i++){auto begin=Clock::now();run();double us=std::chrono::duration<double,std::micro>(Clock::now()-begin).count();times.push_back(us);sum+=us;if(us>frames*1e6/48000)misses++;}
    std::sort(times.begin(),times.end());out<<mode<<','<<voices<<','<<frames<<','<<channels<<','<<sum/times.size()<<','<<times[296]<<','<<times.back()<<','<<misses<<'\n';
}
int main(int argc,char** argv){
    std::ofstream file(argc>1?argv[1]:"dsp-benchmark.csv");file<<"mode,voices,frames,channels,mean_us,p99_us,max_us,deadline_misses_of_300\n";
    auto serialStorage=std::make_unique<Work>(),parallelStorage=std::make_unique<Work>();auto& serial=*serialStorage;auto& parallel=*parallelStorage;Workers workers(parallel);
    for(int voices:{1,8,32})for(int frames:{128,256})for(int channels:{1,2}){
        serial.voices=parallel.voices=voices;serial.frames=parallel.frames=frames;serial.channels=parallel.channels=channels;
        // Separate equal states for correctness, then warm-up and timing.
        for(int v=0;v<64;v++)parallel.nodes[v].inheritState(serial.nodes[v]);
        serial.run(0,1);workers.run();
        for(int v=0;v<voices*channels;v++)for(int i=0;i<frames;i++)if(serial.outputs[v][i]!=parallel.outputs[v][i])return 2;
        measure(file,"serial",voices,frames,channels,[&]{serial.run(0,1);});
        measure(file,"four_workers",voices,frames,channels,[&]{workers.run();});file.flush();
    }
    // Prevent dead-code elimination and make the rendered workload observable.
    double checksum=0;for(auto& channel:serial.outputs)for(float value:channel)checksum+=value;
    std::cout<<"checksum="<<checksum<<"; hardware threads="<<std::thread::hardware_concurrency()<<'\n';
}
