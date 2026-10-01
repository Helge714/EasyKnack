#pragma once
#include "FilterMidiTest.hpp"
namespace poly_midi_tests {
using namespace easyknack;
using filter_midi_tests::Source;
using filter_midi_tests::message;
inline void check(bool condition,int line){if(!condition)throw std::runtime_error("Poly MIDI assertion at "+std::to_string(line));}
#define PM_CHECK(...) check((__VA_ARGS__),__LINE__)
inline void run(){
    Source source;std::array<float,256> out{},copyOut{};
    const std::string patch="GRAPH 6 src midiin 1 @ c - note midinote 0 0 2 poly 2 @ a c "
        "add math 257 0 -1 .5 add 0 @ a aa merge voicemerge 2 @ a a filter filter 3 -1 1000 -1 .707 lowpass @ a aaa out out 4 0 @ a a";
    auto graph=AudioGraph::parse(patch);
    PM_CHECK(graph.nodes[1].voiceCount()==2&&graph.nodes[2].voiceCount()==2&&graph.nodes[3].voiceCount()==1&&graph.nodes[4].voiceCount()==1);
    source.block.append(2,message(144,60,100));source.block.append(4,message(144,64,127));
    graph.process(out.data(),16,0,nullptr,&source);
    auto& notes=graph.nodes[1];
    PM_CHECK(notes.extraBuffers[0][1]==0&&notes.extraBuffers[0][2]==1&&notes.voice(1).extraBuffers[0][3]==0&&notes.voice(1).extraBuffers[0][4]==1);
    PM_CHECK(std::abs(notes.buffer[4]-261.625565)<.001&&std::abs(notes.voice(1).buffer[4]-329.627557)<.001);
    PM_CHECK(graph.nodes[3].buffer[1]==1&&graph.nodes[3].buffer[2]==2&&graph.nodes[3].buffer[4]==3);
    // A released lane is reused before stealing a held note; triggers stay lane-local.
    source.block.clear();source.block.append(1,message(128,60));source.block.append(3,message(144,67,127));
    graph.process(out.data(),16,0,nullptr,&source);
    PM_CHECK(notes.extraBuffers[0][1]==0&&notes.extraBuffers[0][3]==1&&notes.extraBuffers[1][3]==1&&notes.voice(1).extraBuffers[1][3]==0);
    source.block.clear();source.block.append(0,message(144,69,127));graph.process(out.data(),16,0,nullptr,&source);
    PM_CHECK(notes.voice(1).buffer[0]==440&&notes.voice(1).extraBuffers[1][0]==1); // oldest held lane stolen
    // Sustain and panic must affect every voice, including across live recompilation.
    auto replacement=AudioGraph::parse(patch);replacement.inheritState(graph);
    source.block.clear();source.block.append(0,message(176,64,127));source.block.append(1,message(128,69));
    graph.process(out.data(),16,0,nullptr,&source);replacement.process(copyOut.data(),16,0,nullptr,&source);PM_CHECK(out==copyOut);
    PM_CHECK(notes.voice(1).extraBuffers[0][15]==1);
    source.block.clear();source.block.append(0,message(176,64,0));graph.process(out.data(),16,0,nullptr,&source);PM_CHECK(notes.voice(1).extraBuffers[0][0]==0);
    source.block.clear();source.block.panic=true;graph.process(out.data(),16,0,nullptr,&source);PM_CHECK(notes.extraBuffers[0][0]==0);
    // Mono control modulation broadcasts; independent voice domains cannot silently zip.
    for(const std::string& bad:{
        "GRAPH 6 s midiin 1 @ c - a midinote 0 0 2 poly 2 @ a c b midinote 0 0 2 poly 3 @ a c m math 257 0 258 0 add 0 @ a aa sum voicemerge 3 @ a a o out 4 0 @ a a"}){
        bool rejected=false;try{AudioGraph::parse(bad);}catch(const std::runtime_error&){rejected=true;}PM_CHECK(rejected);
    }
    auto automatic=AudioGraph::parse("GRAPH 3 s midiin 1 @ c - n midinote 0 0 2 poly 2 @ a c o out 257 0 @ a a");
    source.block.clear();source.block.append(0,message(144,60,100));source.block.append(0,message(144,64,100));
    automatic.process(out.data(),16,0,nullptr,&source);
    PM_CHECK(automatic.nodes[2].voiceCount()==1&&out[0]==2&&out[15]==2);
    auto separate=AudioGraph::parse("GRAPH 7 s midiin 1 @ c - a midinote 0 0 2 poly 2 @ a c b midinote 0 0 2 poly 3 @ a c x voicemerge 257 @ a a y voicemerge 258 @ a a m math 3 0 4 0 add 0 @ a aa o out 5 0 @ a a");
    PM_CHECK(separate.nodes[5].voiceCount()==1);
    // Fractional pitch shifts do not alter note identities or lose NoteOff.
    auto transpose=AudioGraph::parse("GRAPH 4 s midiin 1 @ c - t miditranspose 0 -12.5 @ c c n midinote 1 0 2 @ a c o out 2 0 @ a a");
    source.block.clear();source.block.append(2,message(144,69,127));source.block.append(8,message(128,69));
    transpose.process(out.data(),16,0,nullptr,&source);PM_CHECK(std::abs(out[2]-440*std::exp2(-12.5/12))<.001&&transpose.nodes[2].extraBuffers[0][8]==0);
    // Delay spans blocks, preserves offsets and metadata, flushes immediately on panic.
    const std::string delayPatch="GRAPH 5 s midiin 1 @ c - t miditranspose 0 12 @ c c d mididelay 1 1 @ c c n midinote 2 0 2 @ a c o out 259 0 @ a a";
    auto delay=AudioGraph::parse(delayPatch);
    source.block.clear();source.block.append(4,message(144,69,127));source.block.append(12,message(128,69));
    delay.process(out.data(),32,0,nullptr,&source);PM_CHECK(out[4]==0);
    source.block.clear();delay.process(out.data(),32,0,nullptr,&source);
    PM_CHECK(out[19]==0&&out[20]==1&&out[27]==1&&out[28]==0&&delay.nodes[3].buffer[20]==880);
    source.block.append(0,message(144,60,127));delay.process(out.data(),16,0,nullptr,&source);
    source.block.clear();source.block.panic=true;delay.process(out.data(),128,0,nullptr,&source);PM_CHECK(std::all_of(out.begin(),out.begin()+128,[](float v){return v==0;}));
    // Reducing delay keeps NoteOff behind already queued NoteOn.
    auto d=AudioGraph::parse(delayPatch);source.block.clear();source.block.append(0,message(144,60,127));d.process(out.data(),16,0,nullptr,&source);
    auto reduced=AudioGraph::parse("GRAPH 5 s midiin 1 @ c - t miditranspose 0 12 @ c c d mididelay 1 0 @ c c n midinote 2 0 2 @ a c o out 259 0 @ a a");reduced.inheritState(d);
    source.block.clear();source.block.append(0,message(128,60));reduced.process(out.data(),64,0,nullptr,&source);PM_CHECK(out[63]==0);
    // Exhausting the bounded queue releases voices rather than losing a NoteOff.
    auto overflow=AudioGraph::parse("GRAPH 4 s midiin 1 @ c - d mididelay 0 10000 @ c c n midinote 1 0 2 @ a c o out 258 0 @ a a");
    for(int block=0;block<17;block++){source.block.clear();for(int i=0;i<256;i++)source.block.append(0,message(144,60,127));overflow.process(out.data(),16,0,nullptr,&source);}
    PM_CHECK(overflow.nodes[1].midi.panic);
    std::cout<<"Polyphonic voice, merge, transpose and MIDI delay tests passed\n";
}
#undef PM_CHECK
}
