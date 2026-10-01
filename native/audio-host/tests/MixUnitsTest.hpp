#pragma once
#include "FilterMidiTest.hpp"
namespace mix_units_tests {
using namespace easyknack;
inline void run(){
    auto check=[](bool ok){if(!ok)throw std::runtime_error("Mix / units assertion failed");};
    std::array<float,256> left{},right{};
    for(int i=0;i<=100;i++){
        float factor=i/100.f,a=MixNode::evaluate(1,0,factor,true),b=MixNode::evaluate(0,1,factor,true);
        check(std::abs(a*a+b*b-1)<1e-6);
    }
    check(MixNode::evaluate(2,10,-1,true)==2&&MixNode::evaluate(2,10,2,true)==10);
    check(std::abs(MixNode::evaluate(1,1,.5,true)-std::sqrt(2.f))<1e-6);
    MixNode live,configuration;live.current={2,3,.25f};configuration.equalPower=true;live.applyParameters(configuration);
    check(live.equalPower&&live.current[0]==2&&live.current[2]==.25f);
    float zero=0,result=0;ProcessContext ctx{nullptr,0,&result,1,48000,&zero,1};live.values={1,1,.5f};live.current=live.values;live.process(ctx);
    check(std::abs(result-std::sqrt(2.f))<1e-6);
    // Old exported source plugins must retain linear behavior without editing
    // their embedded graph or accepting arbitrary unknown contract versions.
    for(std::string rate:{"a","c","e"}){
        auto graph=AudioGraph::parse("GRAPH 2 m mix !12a4ab216b0e0133 -1 2 -1 10 -1 .25 @ "+rate+" "+rate+rate+rate+" o out 0 0 @ a a");
        graph.process(left.data(),32);check(left[0]==4&&left[31]==4);
    }
    std::istringstream old("!12a4ab216b0e0133 -1 2 -1 10 -1 .25 @");live.configure(old);
    check(!live.equalPower&&live.sources.size()==3);std::string marker;old>>marker;check(marker=="@");
    for(auto payload:{"!unknown -1 2 -1 10 -1 .25", "!12a4ab216b0e0133 -1 2 -1 10 -1 2", "!12a4ab216b0e0133 -1 2 -1"}){
        bool rejected=false;try{std::istringstream input(payload);MixNode invalid;invalid.configure(input);}catch(const std::exception&){rejected=true;}check(rejected);
    }
    for(std::string rate:{"a","c","e"}){
        auto graph=AudioGraph::parse("GRAPH 2 m mix -1 2 -1 10 -1 .25 @ "+rate+" "+rate+rate+rate+" o out 0 0 @ a a");
        graph.process(left.data(),32);check(left[0]==4&&left[31]==4);
    }
    for(auto pair: {std::pair{"0 db factor",1.f}, {"1200 cent factor",2.f},{"2 factor semitone",12.f},{"50 percent factor",.5f},{"180 degree turn",.5f},{"1000 ms second",1.f},{"0.707 q q",.707f}}){
        auto graph=AudioGraph::parse(std::string("GRAPH 2 u unitconvert -1 ")+pair.first+" @ e e o out 0 0 @ a a");
        graph.process(left.data(),32);check(std::abs(left[0]-pair.second)<1e-5&&left[0]==left[31]);
    }
    filter_midi_tests::Source source;
    auto stereo=AudioGraph::parse("GRAPH 4 s midiin 1 @ e - n midinote 0 0 2 poly 2 @ e e m voicemerge 257 mix pitch 1 1 @ a ae o out 2 0 @ a a");
    source.block.append(0,filter_midi_tests::message(144,36,100));
    stereo.process(left.data(),32,0,nullptr,&source);stereo.process(right.data(),32,1,nullptr,&source);
    check(std::abs(left[0]-std::sqrt(2.f))<1e-5&&std::abs(right[0])<1e-5);
    source.block.clear();source.block.append(0,filter_midi_tests::message(144,84,100));
    stereo.process(left.data(),32,0,nullptr,&source);stereo.process(right.data(),32,1,nullptr,&source);
    check(std::abs(left[0]-std::sqrt(2.f))<1e-5&&std::abs(right[0]-std::sqrt(2.f))<1e-5);
    auto pan=AudioGraph::parse("GRAPH 5 s midiin 1 @ e - n midinote 0 0 2 poly 2 @ e e p pan 257 -1 -1 @ a aa m voicemerge 2 combine sum @ a a o out 3 0 @ a a");
    source.block.clear();source.block.append(0,filter_midi_tests::message(144,60,100));
    pan.process(left.data(),32,0,nullptr,&source);pan.process(right.data(),32,1,nullptr,&source);
    check(pan.nodes[2].voiceCount()==2&&pan.nodes[3].voiceCount()==1);
    check(std::abs(left[0]-std::sqrt(2.f))<1e-5&&std::abs(right[0])<1e-5);
    auto perNote=AudioGraph::parse("GRAPH 6 s midiin 1 @ e - n midinote 0 0 2 poly 2 @ e e control math 1 0 -1 300 subtract 0 @ e ee p pan 257 2 0 @ a aa m voicemerge 3 combine sum @ a a o out 4 0 @ a a");
    source.block.clear();source.block.append(0,filter_midi_tests::message(144,60,100));source.block.append(8,filter_midi_tests::message(144,72,100));
    perNote.process(left.data(),32,0,nullptr,&source);perNote.process(right.data(),32,1,nullptr,&source);
    check(std::abs(left[0]-std::sqrt(2.f))<1e-5&&std::abs(right[0])<1e-5);
    check(std::abs(left[8]-std::sqrt(2.f))<1e-5&&std::abs(right[8]-std::sqrt(2.f))<1e-5);
    source.block.clear();source.block.append(0,filter_midi_tests::message(144,60,100));
    auto post=AudioGraph::parse("GRAPH 5 s midiin 1 @ e - n midinote 0 0 2 poly 2 @ e e m voicemerge 257 combine mean @ a a p pan 2 -1 1 @ a aa o out 3 0 @ a a");
    post.process(left.data(),32,0,nullptr,&source);post.process(right.data(),32,1,nullptr,&source);
    check(post.nodes[3].voiceCount()==1&&std::abs(left[0])<1e-5&&std::abs(right[0]-.5f*std::sqrt(2.f))<1e-5);
    auto mean=AudioGraph::parse("GRAPH 4 s midiin 1 @ e - n midinote 0 0 2 poly 2 @ e e m voicemerge 257 mix mean -1 1 @ a ae o out 2 0 @ a a");
    source.block.clear();source.block.append(0,filter_midi_tests::message(144,60,100));mean.process(left.data(),32,0,nullptr,&source);check(left[0]==.5f);
}
}
