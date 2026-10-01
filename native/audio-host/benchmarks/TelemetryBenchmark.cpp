#include "AudioTiming.hpp"
#include "AudioGraph.hpp"
#include <fstream>
#include <iostream>
#include <vector>
using namespace easyknack;
// Historical timing path, retained here solely as a measurement reference.
struct LegacyTiming {
    std::atomic<std::uint32_t> average{0},peak{0},overBudget{0};double smoothed=0;std::uint32_t peakValue=0,overruns=0;bool initialized=false;
    void observe(double seconds,unsigned frames,double rate){
        double load=seconds*rate/frames*10000,alpha=std::min(1.0,frames/(rate*.5));
        smoothed=initialized?smoothed+alpha*(load-smoothed):load;initialized=true;
        auto encode=[](double x){return static_cast<std::uint32_t>(std::min(x,double(UINT32_MAX)));};
        peakValue=std::max(peakValue,encode(load));if(load>10000&&overruns<UINT32_MAX)++overruns;
        average.store(encode(smoothed),std::memory_order_relaxed);peak.store(peakValue,std::memory_order_relaxed);overBudget.store(overruns,std::memory_order_relaxed);
    }
};
void graphComparison(){
    std::ofstream report("target/telemetry-graph.csv");report<<"repeat,frames,telemetry,mean_us,p99_us,max_us,deadline_misses_of_300\n";
    for(int repeat=0;repeat<3;repeat++)for(unsigned frames:{128u,256u}){
        std::array<float,256> reference{};
        for(int pass=0;pass<2;pass++){int mode=(pass+repeat)%2;
            auto graph=AudioGraph::parse("GRAPH 2 osc additive -1 sine 64 1 0 -1 220 -1 0 @ a aaa out out 0 0 @ a a");
            std::array<float,256> left{},right{};AudioTiming timing;LegacyTiming legacy;
            for(int i=0;i<20;i++)graph.process(left.data(),frames);
            std::vector<double> times;times.reserve(300);double total=0;int missed=0;auto nextPoll=std::chrono::steady_clock::now()+std::chrono::milliseconds(250);
            for(int i=0;i<300;i++){
                auto start=std::chrono::steady_clock::now();graph.process(left.data(),frames);
                auto elapsed=std::chrono::duration<double>(std::chrono::steady_clock::now()-start).count();
                if(mode)timing.observe(elapsed,frames,48000);else legacy.observe(elapsed,frames,48000);
                double us=std::chrono::duration<double,std::micro>(std::chrono::steady_clock::now()-start).count();times.push_back(us);total+=us;if(us>frames*1e6/48000)++missed;
                if(mode&&std::chrono::steady_clock::now()>=nextPoll){timing.telemetry.snapshot();nextPoll+=std::chrono::milliseconds(250);}
            }
            if(pass==0)reference=left;else for(unsigned i=0;i<frames;i++)if(left[i]!=reference[i])throw std::runtime_error("Telemetry changed output");
            std::sort(times.begin(),times.end());report<<repeat<<','<<frames<<','<<mode<<','<<total/300<<','<<times[296]<<','<<times.back()<<','<<missed<<'\n';
        }
    }
}
int main(){
    using Clock=std::chrono::steady_clock;
    std::cout<<"repeat,frames,legacy_ns_per_observe,telemetry_ns_per_observe\n";
    for(int repeat=0;repeat<3;repeat++)for(unsigned frames:{128u,256u}){
        LegacyTiming legacy;AudioTiming measured;double total[2]{};
        for(int batch=0;batch<200;batch++){
            auto begin=Clock::now();for(int i=0;i<512;i++)legacy.observe(.0005+i*1e-9,frames,48000);
            total[0]+=std::chrono::duration<double,std::nano>(Clock::now()-begin).count();
            begin=Clock::now();for(int i=0;i<512;i++)measured.observe(.0005+i*1e-9,frames,48000);
            total[1]+=std::chrono::duration<double,std::nano>(Clock::now()-begin).count();
            measured.telemetry.reset(); // Control work deliberately outside writer timing.
        }
        std::cerr<<"timing checksum "<<legacy.average.load()<<" "<<measured.average.load()<<"\n";
        std::cout<<repeat<<','<<frames<<','<<total[0]/102400<<','<<total[1]/102400<<'\n';
    }
    graphComparison();
}




