#pragma once
#include "FilterMidiTest.hpp"
namespace value_event_tests {
using namespace easyknack;
using filter_midi_tests::Source;
using filter_midi_tests::message;
inline void check(bool value,int line){if(!value)throw std::runtime_error("Value event assertion at "+std::to_string(line));}
#define VE_CHECK(...) check((__VA_ARGS__),__LINE__)
inline void run(){
    Source source;std::array<float,256> out{},right{};
    const std::string patch="GRAPH 5 s midiin 1 @ e - cc midicc 0 0 1 @ e e m math 1 0 -1 2 multiply 0 @ e ee "
        "map maprange 2 0 -1 0 -1 2 -1 -1 -1 1 0 @ e eeeee o out 3 0 @ a a";
    auto graph=AudioGraph::parse(patch);
    source.block.append(4,message(176,1,64));source.block.append(12,message(176,1,127));
    graph.process(out.data(),16,0,nullptr,&source);
    VE_CHECK(out[0]==-1&&std::abs(out[4]-(128.f/127-1))<1e-6&&out[12]==1);
    VE_CHECK(graph.nodes[2].eventEvaluations[0]==3&&graph.nodes[3].eventEvaluations[0]==3);
    VE_CHECK(graph.nodes[2].eventOffsets[0].count()==3&&graph.nodes[2].eventOffsets[0].test(4));
    source.block.clear();graph.process(out.data(),16,0,nullptr,&source);
    VE_CHECK(out[0]==1&&out[15]==1&&graph.nodes[2].eventEvaluations[0]==3&&graph.nodes[2].eventOffsets[0].none());
    auto copy=AudioGraph::parse(patch);copy.inheritState(graph);copy.process(out.data(),16,0,nullptr,&source);
    VE_CHECK(copy.nodes[2].eventEvaluations[0]==3&&out[0]==1); // unrelated graph publication does not retrigger
    source.block.panic=true;copy.process(out.data(),16,0,nullptr,&source);VE_CHECK(out[0]==-1&&copy.nodes[2].eventEvaluations[0]==4);
    // Two input changes at the same sample form one evaluation, not intermediate outputs.
    auto simultaneous=AudioGraph::parse("GRAPH 5 s midiin 1 @ e - x midicc 0 0 1 @ e e y midicc 0 0 2 @ e e m math 1 0 2 0 subtract 0 @ e ee o out 3 0 @ a a");
    source.block.clear();source.block.append(5,message(176,1,127));source.block.append(5,message(176,2,127));
    simultaneous.process(out.data(),16,0,nullptr,&source);
    VE_CHECK(simultaneous.nodes[3].eventEvaluations[0]==2&&simultaneous.nodes[3].eventOffsets[0].count()==1);
    VE_CHECK(std::all_of(out.begin(),out.begin()+16,[](float value){return value==0;}));
    // Control values cause updates only when changed, including edits to the source.
    auto control=AudioGraph::parse("GRAPH 3 v value 1 @ c - m math 0 0 -1 2 multiply 0 @ e ee o out 1 0 @ a a");
    control.process(out.data(),16,0,nullptr);control.process(out.data(),16,0,nullptr);
    VE_CHECK(out[0]==2&&control.nodes[1].eventEvaluations[0]==1);
    auto edited=AudioGraph::parse("GRAPH 3 v value 3 @ c - m math 0 0 -1 2 multiply 0 @ e ee o out 1 0 @ a a");
    edited.inheritState(control);edited.process(out.data(),16,0,nullptr);VE_CHECK(out[0]==6&&edited.nodes[1].eventEvaluations[0]==2);
    auto parameter=AudioGraph::parse("GRAPH 3 v value 3 @ c - m math 0 0 -1 4 multiply 0 @ e ee o out 1 0 @ a a");
    parameter.inheritState(edited);parameter.process(out.data(),16,0,nullptr);VE_CHECK(out[0]==12&&parameter.nodes[1].eventEvaluations[0]==1);
    struct Stereo : AudioIO {
        float base=1;
        void beginBlock(std::size_t) noexcept override{}
        float input(const std::string&,std::size_t ch,std::size_t frame)const noexcept override{return base+100*ch+frame;}
        void output(const std::string&,std::size_t,const float*,std::size_t) noexcept override{}
        void endBlock(const float*,std::size_t) noexcept override{}
    } audio;
    auto mixed=AudioGraph::parse("GRAPH 5 s midiin 1 @ e - cc midicc 0 0 1 @ e e a input 61 copy stereo @ a - m math 2 0 1 0 add 0 @ e ee o out 3 0 @ a a");
    source.block.clear();source.block.append(7,message(176,1,127));
    mixed.process(out.data(),16,0,&audio,&source);mixed.process(right.data(),16,1,&audio,&source);
    VE_CHECK(out[0]==1&&out[6]==1&&out[7]==9&&out[15]==9&&right[0]==101&&right[7]==109);
    source.block.clear();audio.base=20;mixed.process(out.data(),16,0,&audio,&source);mixed.process(right.data(),16,1,&audio,&source);
    VE_CHECK(out[0]==9&&right[0]==109&&mixed.nodes[3].eventEvaluations[0]==2&&mixed.nodes[3].eventEvaluations[1]==2);
    // Audio mode sees every sample; control mode sees only the block's first sample.
    for(char rate:{'a','c'}){
        auto sampled=AudioGraph::parse(std::string("GRAPH 4 s midiin 1 @ e - cc midicc 0 0 1 @ e e m math 1 0 -1 2 multiply 0 @ ")+rate+" "+rate+rate+" o out 2 0 @ a a");
        source.block.clear();source.block.append(5,message(176,1,127));sampled.process(out.data(),16,0,nullptr,&source);
        VE_CHECK(out[0]==0&&out[5]==(rate=='a'?2:0));source.block.clear();sampled.process(out.data(),16,0,nullptr,&source);VE_CHECK(out[0]==2);
    }
    // Trigger falling edge is an event even if the pulse ends in the next block.
    auto pulse=AudioGraph::parse("GRAPH 4 s midiin 1 @ e - n midinote 0 0 2 poly 1 @ e e m math 513 0 -1 1 multiply 0 @ e ee o out 2 0 @ a a");
    source.block.clear();source.block.append(15,message(144,69,127));pulse.process(out.data(),16,0,nullptr,&source);
    VE_CHECK(out[14]==0&&out[15]==1);source.block.clear();pulse.process(out.data(),16,0,nullptr,&source);VE_CHECK(out[0]==0&&pulse.nodes[2].eventOffsets[0].test(0));
    auto poly=AudioGraph::parse("GRAPH 5 s midiin 1 @ e - n midinote 0 0 2 poly 2 @ e e m math 257 0 -1 1 multiply 0 @ e ee merge voicemerge 2 @ a a o out 3 0 @ a a");
    source.block.clear();source.block.append(2,message(144,60,127));source.block.append(6,message(144,64,127));poly.process(out.data(),16,0,nullptr,&source);
    VE_CHECK(out[1]==0&&out[2]==1&&out[6]==2&&poly.nodes[2].eventEvaluations[0]==2&&poly.nodes[2].voice(1).eventEvaluations[0]==2);
    bool rejected=false;try{AudioGraph::parse("GRAPH 1 o osc -1 440 @ e a");}catch(...){rejected=true;}VE_CHECK(rejected);
    std::cout<<"Value event scheduling, sampling, stereo and polyphony tests passed\n";
}
#undef VE_CHECK
}
