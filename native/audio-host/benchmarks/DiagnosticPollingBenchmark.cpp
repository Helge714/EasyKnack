#include "AudioGraph.hpp"
#include "RenderTelemetry.hpp"
#include <fstream>
#include <iostream>
using namespace easyknack;
int main(){
    using Clock=std::chrono::steady_clock;
    std::cout<<"repeat,frames,mode,mean_us,p99_us,max_us,deadline_misses,snapshot_max_us,telemetry_drops\n";
    for(int repeat=0;repeat<2;repeat++)for(unsigned frames:{128u,256u})for(int pass=0;pass<3;pass++){
        int mode=(pass+repeat)%3;RenderTelemetry telemetry;std::atomic<bool> done{false};double pollMax=0;
        std::thread reader([&]{while(!done.load()){if(mode==2){auto before=Clock::now();telemetry.snapshot();pollMax=std::max(pollMax,std::chrono::duration<double,std::micro>(Clock::now()-before).count());}std::this_thread::sleep_for(std::chrono::milliseconds(50));}});
        auto graph=AudioGraph::parse("GRAPH 2 osc additive -1 sine 64 1 0 -1 220 -1 0 @ a aaa out out 0 0 @ a a");
        std::array<float,256> buffer{};std::vector<double> times;times.reserve(360);unsigned missed=0;double sum=0;
        auto next=Clock::now();
        for(int i=0;i<360;i++){
            auto start=Clock::now();graph.process(buffer.data(),frames);double elapsed=std::chrono::duration<double>(Clock::now()-start).count();
            if(mode)telemetry.observe(elapsed,frames,48000);
            double us=std::chrono::duration<double,std::micro>(Clock::now()-start).count();times.push_back(us);sum+=us;if(us>frames*1e6/48000)++missed;
            next+=std::chrono::nanoseconds(static_cast<long long>(frames*1e9/48000));std::this_thread::sleep_until(next);
        }
        done.store(true);reader.join();std::sort(times.begin(),times.end());
        std::cout<<repeat<<','<<frames<<','<<mode<<','<<sum/times.size()<<','<<times[356]<<','<<times.back()<<','<<missed<<','<<pollMax<<','<<telemetry.dropped.load()<<'\n';
    }
}
