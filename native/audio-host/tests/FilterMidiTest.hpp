#pragma once
#include "../src/AudioGraph.hpp"
#include "../src/midi/MidiQueue.hpp"
#include <iostream>

namespace filter_midi_tests {
using namespace easyknack;
inline void check(bool value,int line){if(!value)throw std::runtime_error("Filter/MIDI assertion at "+std::to_string(line));}
#define FM_CHECK(...) check((__VA_ARGS__),__LINE__)
struct Source : MidiSource {
    MidiBlock block;
    Source(){block.source=1;block.generation=1;}
    void beginBlock(std::size_t,double) noexcept override{}
    const MidiBlock* events(std::uint64_t key) const noexcept override{return key==1?&block:nullptr;}
};
inline std::uint32_t message(int status,int a=0,int b=0){return status|(a<<8)|(b<<16);}
inline double response(const std::string& mode,double frequency){
    FilterNode filter;std::istringstream settings("-1 -1 1000 -1 .707 "+mode);filter.configure(settings);
    std::array<float,256> input{},output{},silence{};const float* ins[]={input.data(),nullptr,nullptr};
    ProcessContext context{ins,3,output.data(),256,48000,silence.data()};double energy=0,reference=0;
    for(int block=0;block<80;block++){
        for(int i=0;i<256;i++)input[i]=static_cast<float>(std::sin((block*256+i)*2*3.141592653589793*frequency/48000));
        filter.process(context);if(block>20)for(int i=0;i<256;i++){energy+=output[i]*output[i];reference+=input[i]*input[i];}
    }
    return std::sqrt(energy/reference);
}
inline void run(){
    FM_CHECK(response("lowpass",100)>.98&&response("lowpass",10000)<.02);
    FM_CHECK(response("highpass",100)<.02&&response("highpass",10000)>.98);
    FM_CHECK(response("bandpass",1000)>.69&&response("bandpass",10000)<.15);
    // State continuity under live graph publication and independent stereo filter memory.
    auto graph=AudioGraph::parse("GRAPH 3 osc osc -1 440 @ a a filter filter 0 -1 1000 -1 10 lowpass @ a aaa out out 1 0 @ a a");
    std::array<float,256> left{},expected{},right{};graph.process(left.data(),256);
    auto copy=AudioGraph::parse("GRAPH 3 osc osc -1 440 @ a a filter filter 0 -1 1000 -1 10 lowpass @ a aaa out out 1 0 @ a a");
    copy.inheritState(graph);graph.process(expected.data(),256);copy.process(left.data(),256);FM_CHECK(left==expected);
    struct Stereo : AudioIO {
        void beginBlock(std::size_t) noexcept override{}
        float input(const std::string&,std::size_t channel,std::size_t frame) const noexcept override{return channel==0&&frame==0?1.f:0.f;}
        void output(const std::string&,std::size_t,const float*,std::size_t) noexcept override{}
        void endBlock(const float*,std::size_t) noexcept override{}
    } io;
    auto stereo=AudioGraph::parse("GRAPH 3 cap input 61 copy stereo @ a - filter filter 0 -1 1000 -1 30 lowpass @ a aaa out out 1 0 @ a a");
    stereo.process(left.data(),256,0,&io);stereo.process(right.data(),256,1,&io);
    FM_CHECK(std::any_of(left.begin(),left.end(),[](float v){return v!=0;})&&std::all_of(right.begin(),right.end(),[](float v){return v==0;}));
    MidiQueue queue;MidiBlock block;queue.push(250,message(144,69,127));queue.push(750,message(128,69));queue.drain(block,0,1000,100);
    FM_CHECK(block.count==2&&block.events[0].offset==25&&block.events[1].offset==75);
    for(int i=0;i<1025;i++)queue.push(250,message(144,69,127));queue.drain(block,0,1000,100);FM_CHECK(block.panic&&block.count==0);
    queue.push(1250,message(144,69,127));queue.drain(block,1000,2000,100);FM_CHECK(!block.panic&&block.count==1&&block.events[0].offset==25);
    Source source;
    auto midi=AudioGraph::parse("GRAPH 5 src midiin 1 @ c - note midinote 0 0 2 @ a c freq out 1 0 @ a a gate out 257 0 @ a a vel out 769 0 @ a a");
    source.block.append(2,message(144,69,100));source.block.append(5,message(144,72,127));source.block.append(9,message(128,72));source.block.append(12,message(144,69,0));
    midi.process(left.data(),16,0,nullptr,&source);
    const auto& note=midi.nodes[1];FM_CHECK(note.buffer[2]==440&&note.buffer[5]>523&&note.buffer[9]==440);
    FM_CHECK(note.extraBuffers[0][1]==0&&note.extraBuffers[0][2]==1&&note.extraBuffers[0][11]==1&&note.extraBuffers[0][12]==0);
    FM_CHECK(note.extraBuffers[1][2]==1&&note.extraBuffers[1][3]==0&&note.extraBuffers[1][5]==1);
    FM_CHECK(std::abs(note.extraBuffers[2][2]-100.f/127)<1e-6&&note.extraBuffers[2][5]==1);
    // Sustain, all-notes-off and panic release held keys instead of leaving stuck gates.
    source.block.clear();source.block.append(0,message(144,60,127));source.block.append(1,message(176,64,127));source.block.append(2,message(128,60));
    midi.process(left.data(),16,0,nullptr,&source);FM_CHECK(note.extraBuffers[0][15]==1);
    source.block.clear();source.block.append(3,message(176,64,0));midi.process(left.data(),16,0,nullptr,&source);FM_CHECK(note.extraBuffers[0][2]==1&&note.extraBuffers[0][3]==0);
    source.block.clear();source.block.append(0,message(144,60,127));source.block.append(1,message(176,123));midi.process(left.data(),16,0,nullptr,&source);FM_CHECK(note.extraBuffers[0][0]==1&&note.extraBuffers[0][1]==0);
    source.block.clear();source.block.append(0,message(144,60,127));midi.process(left.data(),16,0,nullptr,&source);
    source.block.clear();source.block.panic=true;midi.process(left.data(),16,0,nullptr,&source);FM_CHECK(note.extraBuffers[0][0]==0);
    // All four outputs are routable, including the trigger and velocity channels.
    auto chain=AudioGraph::parse("GRAPH 5 src midiin 1 @ c - note midinote 0 1 2 @ a c cc midicc 0 1 1 @ a c sum math 513 0 2 0 add 0 @ a aa out out 3 0 @ a a");
    source.block.clear();source.block.append(1,message(145,60,127));source.block.append(3,message(144,69,127));source.block.append(6,message(176,1,64));
    chain.process(left.data(),16,0,nullptr,&source);FM_CHECK(left[1]==0&&left[3]==1&&std::abs(left[6]-64.f/127)<1e-6);
    auto next=AudioGraph::parse("GRAPH 5 src midiin 1 @ c - note midinote 0 1 2 @ a c cc midicc 0 1 1 @ a c sum math 513 0 2 0 add 0 @ a aa out out 3 0 @ a a");
    next.inheritState(chain);source.block.clear();next.process(left.data(),16,0,nullptr,&source);FM_CHECK(next.nodes[1].extraBuffers[0][0]==1);
    source.block.generation++;next.process(left.data(),16,0,nullptr,&source);FM_CHECK(next.nodes[1].extraBuffers[0][0]==0&&next.nodes[2].buffer[0]==0);
    auto envelope=AudioGraph::parse("GRAPH 4 src midiin 1 @ c - note midinote 0 0 2 @ a c env envelope 257 0 513 0 1 1 .5 4 0 0 0 0 0 0 0 .2 1 .2 1 .2 1 0 .5 .65 .5 .65 .5 .65 0 1 0 1 0 1 0 0 @ a aa out out 2 0 @ a a");
    source.block.clear();source.block.append(4,message(144,69,127));source.block.append(12,message(128,69));
    envelope.process(left.data(),16,0,nullptr,&source);
    FM_CHECK(left[3]==0&&left[4]==0&&left[5]>0&&left[12]==left[11]&&left[13]<left[12]);
    FM_CHECK(envelope.midiDevices()==std::vector<std::uint64_t>{1});
    auto disconnected=AudioGraph::parse("GRAPH 2 src midiin 1 @ c - note midinote 0 0 2 @ a c");FM_CHECK(disconnected.order.empty()&&disconnected.midiDevices().empty());
    for(const std::string& bad:{"GRAPH 2 src midiin 1 @ c - out out 0 0 @ a a","GRAPH 2 src value 1 @ c - note midinote 0 0 2 @ a c","GRAPH 2 src value 1 @ c - out out 256 0 @ a a"}){
        bool rejected=false;try{AudioGraph::parse(bad);}catch(...){rejected=true;}FM_CHECK(rejected);
    }
}
#undef FM_CHECK
}
