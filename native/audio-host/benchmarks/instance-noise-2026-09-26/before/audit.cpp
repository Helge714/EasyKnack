#include "Fixture.hpp"
#include "nodes/OutputNode.hpp"
#include <chrono>
#include <iostream>
#include <algorithm>
using namespace easyknack;
volatile float auditSink=0;
std::unique_ptr<NodeProcessor> factory(const std::string& type){
    if(type=="live")return std::make_unique<MultipleGraphNode>();
    if(type=="baked")return std::make_unique<BakedInstance>();
    if(type=="complex")return std::make_unique<AdditiveOscillatorNode>();
    if(type=="gain")return std::make_unique<GainNode>();
    if(type=="out")return std::make_unique<OutputNode>();
    throw std::runtime_error(type);
}
int main(int argc,char** argv){
    // Compare live/baked settled samples independently of timing.
    auto check=AudioGraph::compile(std::string("GRAPH 3 src live ")+liveParameters+" @ a ccccccccc gain gain 0 -12 @ a a out out 1 0 @ a a",factory);
    auto compiled=AudioGraph::compile(std::string("GRAPH 3 src baked ")+bakedParameters+" @ a cccc gain gain 0 -12 @ a a out out 1 0 @ a a",factory);
    std::array<float,256> expected{},actual{};
    for(int block=0;block<320;block++){
        check.process(expected.data(),256);compiled.process(actual.data(),256);
        for(int sample=0;sample<256;sample++)if(std::abs(expected[sample]-actual[sample])>1e-5f)
            throw std::runtime_error("live/baked output mismatch");
    }
    std::cout<<"case,us_per_stereo_block,stereo_source,cross_channel\n";
    std::vector<std::string> order{"live","baked"};
    if(argc>1&&std::string(argv[1])=="reverse")std::reverse(order.begin(),order.end());
    for(const std::string& kind:order){
        std::string parameters=kind=="live"?liveParameters:kind=="baked"?bakedParameters:"-1 sine 20 0 0 -1 440 -1 0 inputs -1 -1 -1";
        std::string rates=kind=="live"?"ccccccccc":kind=="baked"?"cccc":"cccccc";
        auto graph=AudioGraph::compile("GRAPH 3 src "+kind+" "+parameters+" @ a "+rates+" gain gain 0 -12 @ a a out out 1 0 @ a a",factory);
        std::array<float,256> out{};
        auto run=[&]{graph.process(out.data(),256);auditSink=out[255];};
        for(int i=0;i<300;i++)run();
        std::vector<double> times;
        for(int batch=0;batch<9;batch++){
            auto start=std::chrono::steady_clock::now();
            for(int i=0;i<500;i++)run();
            times.push_back(std::chrono::duration<double,std::micro>(std::chrono::steady_clock::now()-start).count()/500);
        }
        std::sort(times.begin(),times.end());
        std::cout<<kind<<","<<times[4]<<","<<graph.nodes[0].stereo<<","<<graph.crossChannel<<"\n";
    }
}

