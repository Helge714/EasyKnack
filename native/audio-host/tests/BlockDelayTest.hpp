#pragma once
#include "FilterMidiTest.hpp"
namespace block_delay_tests {
using namespace easyknack;
inline void check(bool v,int line){if(!v)throw std::runtime_error("Block delay assertion at "+std::to_string(line));}
#define BD_CHECK(...) check((__VA_ARGS__),__LINE__)
inline void run(){
    std::array<float,256> left{},right{};
    auto feed=AudioGraph::parse("GRAPH 3 d blockdelay 1 0 @ a a v value .25 @ c - o out 0 0 @ a a");
    feed.process(left.data(),128);BD_CHECK(left[0]==0);feed.process(left.data(),128);BD_CHECK(left[127]==0);
    feed.process(left.data(),128);BD_CHECK(left[0]==.25f&&left[127]==.25f);
    const std::string patch="GRAPH 4 d blockdelay 2 0 @ a a gain math 0 0 -1 .5 multiply 0 @ a aa sum math 1 0 -1 1 add 0 @ a aa o out 0 0 @ a a";
    auto feedback=AudioGraph::parse(patch);
    for(float expected:{0.f,1.f,1.5f,1.75f}){feedback.process(left.data(),256);for(float value:left)BD_CHECK(value==expected);}
    auto replacement=AudioGraph::parse(patch);replacement.inheritState(feedback);
    feedback.process(left.data(),256);replacement.process(right.data(),256);BD_CHECK(left==right);
    auto self=AudioGraph::parse("GRAPH 2 d blockdelay 0 0 @ a a o out 0 0 @ a a");self.process(left.data(),256);BD_CHECK(left[0]==0);
    auto dead=AudioGraph::parse("GRAPH 3 d blockdelay 0 0 @ a a v value .5 @ c - o out 1 0 @ a a");BD_CHECK(!dead.nodes[0].active&&dead.deferred.empty());
    bool rejected=false;try{AudioGraph::parse("GRAPH 2 m math 0 0 -1 1 add 0 @ a aa o out 0 0 @ a a");}catch(const std::runtime_error&){rejected=true;}BD_CHECK(rejected);
    // Channel-specific input must propagate around a loop even when Delay is scheduled first.
    auto stereo=AudioGraph::parse("GRAPH 4 d blockdelay 2 0 @ a a c context @ c - m math 0 0 1 0 add 0 @ a aa o out 0 0 @ a a");
    stereo.process(left.data(),256,0,nullptr);stereo.process(right.data(),256,1,nullptr);BD_CHECK(left[0]==0&&right[0]==0);
    stereo.process(left.data(),256,0,nullptr);stereo.process(right.data(),256,1,nullptr);BD_CHECK(left[0]==-1&&right[0]==1);
    // Polyphonic gate enters feedback after the deferred node in the schedule.
    filter_midi_tests::Source source;source.block.append(0,filter_midi_tests::message(144,60,100));source.block.append(0,filter_midi_tests::message(144,64,100));
    auto poly=AudioGraph::parse("GRAPH 5 d blockdelay 3 0 @ a a s midiin 1 @ c - n midinote 1 0 2 poly 2 @ a c m math 0 0 258 0 add 0 @ a aa o out 0 0 @ a a");
    BD_CHECK(poly.nodes[0].voiceCount()==2&&poly.nodes[3].voiceCount()==2);
    poly.process(left.data(),256,0,nullptr,&source);BD_CHECK(left[0]==0);source.block.clear();poly.process(left.data(),256,0,nullptr,&source);BD_CHECK(left[0]==2);
    std::cout<<"Block delay feedback, state, partial blocks, stereo and voice tests passed\n";
}
#undef BD_CHECK
}
