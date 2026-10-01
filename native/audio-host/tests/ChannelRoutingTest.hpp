#pragma once
#include "ChannelSharingTest.hpp"
#include "FilterMidiTest.hpp"
namespace channel_routing_tests {
using namespace easyknack;
inline void run(){
    auto check=[](bool ok){if(!ok)throw std::runtime_error("Channel routing assertion failed");};
    channel_tests::IO io; std::array<float,blockSize> left{},right{};
    for(bool swap:{false,true}){
        auto graph=AudioGraph::parse(std::string("GRAPH 4 in input 61 copy stereo @ a - split channelseparate 0 @ a a combine channelcombine ")+(swap?"257 1":"1 257")+" @ a aa out deviceout 2 78 @ a a");
        check(graph.crossChannel&&!graph.nodes[1].stereo&&graph.nodes[2].stereo);
        for(int frames:{1,17,256,35}){
            channel_tests::render(graph,io,frames,left,right);
            for(int i=0;i<frames;i++){
                check(left[i]==(swap?-.75f:.25f)&&right[i]==(swap?.25f:-.75f));
                check(io.mixed[2*i]==left[i]&&io.mixed[2*i+1]==right[i]);
            }
        }
    }
    // Stateful audio, repeated splits, and a direct fan-out must stay sample exact.
    auto reference=AudioGraph::parse("GRAPH 2 osc osc -1 440 @ a a out out 0 0 @ a a");
    auto graph=AudioGraph::parse("GRAPH 7 osc osc -1 440 @ a a split channelseparate 0 @ a a combine channelcombine 257 1 @ a aa split2 channelseparate 2 @ a a combine2 channelcombine 259 3 @ a aa out out 4 0 @ a a direct out 0 0 @ a a");
    std::array<float,blockSize> expected{};
    for(int frames:{17,256,1,35}){
        reference.process(expected.data(),frames);
        graph.process(left.data(),frames,0,nullptr);graph.process(right.data(),frames,1,nullptr);
        for(int i=0;i<frames;i++)check(left[i]==2*expected[i]&&right[i]==left[i]);
    }
    // Unconnected channels are silent, including a completely unconnected split.
    for(auto text:{"GRAPH 3 v value 2 @ c - c channelcombine 0 -1 @ a aa o out 1 0 @ a a",
                   "GRAPH 4 v value 2 @ c - s channelseparate -1 @ a a c channelcombine 0 257 @ a aa o out 2 0 @ a a"}){
        auto quiet=AudioGraph::parse(text);quiet.process(left.data(),32,0,nullptr);quiet.process(right.data(),32,1,nullptr);
        check(left[0]==2&&right[0]==0);
    }
    // MIDI events and polyphonic voices survive the different channel schedule.
    filter_midi_tests::Source source;
    source.block.append(3,filter_midi_tests::message(144,60,100));
    source.block.append(8,filter_midi_tests::message(144,72,100));
    auto poly=AudioGraph::parse("GRAPH 8 s midiin 1 @ e - n midinote 0 0 2 poly 2 @ e e math math 257 0 -1 1 multiply 0 @ e ee pan pan 2 -1 -1 @ a aa split channelseparate 3 @ a a combine channelcombine 260 4 @ a aa merge voicemerge 5 combine sum @ a a out out 6 0 @ a a");
    poly.process(left.data(),32,0,nullptr,&source);poly.process(right.data(),32,1,nullptr,&source);
    check(poly.nodes[4].voiceCount()==2&&!poly.nodes[4].stereo&&poly.nodes[5].voiceCount()==2);
    check(std::abs(left[8])<1e-5&&right[0]==0&&std::abs(right[3]-std::sqrt(2.f))<1e-5&&std::abs(right[8]-2*std::sqrt(2.f))<1e-5);
}
}
