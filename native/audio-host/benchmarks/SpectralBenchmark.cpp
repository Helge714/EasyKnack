// Feed an encoded GRAPH command to the real scheduler, with deterministic input.
// clang++ -std=c++17 -O2 -static -I native/audio-host/src -I target/native/generated
//   native/audio-host/benchmarks/SpectralBenchmark.cpp -o target/spectral-benchmark.exe
#include "AudioGraph.hpp"
#include <algorithm>
#include <chrono>
#include <fstream>
#include <iostream>

using namespace easyknack;
struct BenchmarkInput : InputNode {
    std::uint64_t time=0;
    void process(ProcessContext& c)noexcept override{
        for(std::size_t i=0;i<c.bufferSize;i++)
            c.output[i]=float(.2*std::sin((time+i)*6.283185307179586*(c.channelIndex?660:440)/c.sampleRate));
        time+=c.bufferSize;
    }
};
int main(int argc,char** argv){
    if(argc!=2){std::cerr<<"Usage: spectral-benchmark encoded-graph.txt\n";return 2;}
    std::ifstream file(argv[1]);if(!file){std::cerr<<"Cannot open graph\n";return 2;}
    try{
        std::string text((std::istreambuf_iterator<char>(file)),{});
        auto graph=AudioGraph::compile(text,[](const std::string& type)->std::unique_ptr<NodeProcessor>{
            if(type=="input")return std::make_unique<BenchmarkInput>();
            return createNodeProcessor(type);
        });
        graph.prepare(48000,256);float left[256]{},right[256]{};
        auto run=[&]{graph.process(left,256,0,nullptr);graph.process(right,256,1,nullptr);};
        for(int i=0;i<100;i++)run();std::vector<double> times;
        for(int batch=0;batch<9;batch++){
            auto start=std::chrono::steady_clock::now();for(int i=0;i<200;i++)run();
            times.push_back(std::chrono::duration<double,std::micro>(std::chrono::steady_clock::now()-start).count()/200);
        }
        std::sort(times.begin(),times.end());
        std::cout<<"median_us="<<times[4]<<" deadline_percent="<<times[4]/(256./48000*1e6)*100<<" sample="<<left[200]<<'\n';
    }catch(const std::exception& error){std::cerr<<error.what()<<'\n';return 1;}
}
