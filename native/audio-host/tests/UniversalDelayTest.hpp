#pragma once
namespace universal_delay_tests {
using namespace easyknack;
inline void check(bool value,int line){if(!value)throw std::runtime_error("Universal delay assertion at "+std::to_string(line));}
#define UD_CHECK(...) check((__VA_ARGS__),__LINE__)
inline std::string seconds(int samples){std::ostringstream s;s.precision(12);s<<samples/48000.0;return s.str();}
inline void run(){
    std::array<float,256> a{},b{};
    for(int delay:{0,1,4,17,255,256,480,12000}){
        auto graph=AudioGraph::parse("GRAPH 3 d delay 1 0 -1 "+seconds(delay)+" @ a ac m math 0 0 -1 1 add 0 @ a aa o out 0 0 @ a a");
        UD_CHECK(graph.hasSampleSteps);int elapsed=0;
        for(int frames:{256,13,256,7,256}){graph.process(a.data(),frames);UD_CHECK(graph.lastFeedbackChunk==static_cast<unsigned>(std::min(frames,std::max(1,delay))));for(int i=0;i<frames;i++)UD_CHECK(a[i]==(elapsed+i)/std::max(1,delay));elapsed+=frames;}
    }
    // Ordinary feed-forward delay retains zero bypass and fractional interpolation.
    for(int delay:{0,1,17,256}){
        auto graph=AudioGraph::parse("GRAPH 3 v value .25 @ c - d delay 0 0 -1 "+seconds(delay)+" @ a ac o out 1 0 @ a a");
        UD_CHECK(!graph.hasSampleSteps);graph.process(a.data(),256);for(int i=0;i<256;i++)UD_CHECK(a[i]==(i<delay?0:.25f));
    }
    auto dynamic=AudioGraph::parse("GRAPH 4 time value "+seconds(256)+" @ c - d delay 2 0 0 0 @ a ac m math 1 0 -1 1 add 0 @ a aa o out 1 0 @ a a");
    dynamic.process(a.data(),256);UD_CHECK(dynamic.lastFeedbackChunk==256);
    std::istringstream vp(seconds(4)),vr(seconds(4));dynamic.nodes[0].processor->configure(vp);dynamic.nodes[0].rightProcessor->configure(vr);
    dynamic.process(a.data(),256);UD_CHECK(dynamic.lastFeedbackChunk==4);UD_CHECK(a[0]==1&&a[4]==2&&a[252]==64);
    // A single shared island respects both delay lengths without duplicate execution.
    auto overlap=AudioGraph::parse("GRAPH 4 d delay 2 0 -1 "+seconds(4)+" @ a ac e delay 2 0 -1 "+seconds(17)+" @ a ac m math 0 0 1 0 add 0 @ a aa o out 2 0 @ a a");overlap.process(a.data(),256);UD_CHECK(overlap.lastFeedbackChunk==4&&overlap.steps[0].nodes.size()==3);
    // Full-block wavetable readers cannot be synthesized from their own feedback.
    bool rejected=false;try{AudioGraph::parse("GRAPH 3 d delay 1 0 1 .01 @ a ac m math 0 0 -1 1 add 0 @ a aa o out 0 0 @ a a");}catch(const std::runtime_error&){rejected=true;}UD_CHECK(rejected);
    auto stereo=AudioGraph::parse("GRAPH 4 c context @ c - d delay 2 0 -1 "+seconds(4)+" @ a ac m math 1 0 0 0 add 0 @ a aa o out 1 0 @ a a");stereo.process(a.data(),256,0,nullptr);stereo.process(b.data(),256,1,nullptr);for(int i=0;i<256;i++)UD_CHECK(a[i]==-(i/4)&&b[i]==i/4);
    auto copied=AudioGraph::parse("GRAPH 4 c context @ c - d delay 2 0 -1 "+seconds(4)+" @ a ac m math 1 0 0 0 add 0 @ a aa o out 1 0 @ a a");copied.adoptState(stereo);copied.process(a.data(),256);UD_CHECK(a[0]==-64);
    const auto blockPatch="GRAPH 4 d delay 2 0 -1 "+seconds(256)+" @ a ac c channelcombine 0 0 @ a aa m math 1 0 -1 1 add 0 @ a aa o out 0 0 @ a a";
    auto whole=AudioGraph::parse(blockPatch);UD_CHECK(whole.steps[0].blockOnly);whole.process(a.data(),256);UD_CHECK(a[0]==0&&a[255]==0);whole.process(a.data(),256);UD_CHECK(a[0]==1&&a[255]==1);
    auto unsafe=blockPatch;unsafe.replace(unsafe.find(seconds(256)),seconds(256).size(),seconds(4));bool shortRejected=false;try{AudioGraph::parse(unsafe);}catch(const std::runtime_error&){shortRejected=true;}UD_CHECK(shortRejected);
    auto configuration=AudioGraph::compile(unsafe,createNodeProcessor,true);UD_CHECK(configuration.parameterShape()!=whole.parameterShape());
    std::cout<<"Universal delay: adaptive steps, modulation, migration-compatible timings, stereo, overlapping loops and state passed\n";
}
#undef UD_CHECK
}
