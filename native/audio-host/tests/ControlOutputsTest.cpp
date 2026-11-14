#include "AudioGraph.hpp"
#include "FilterMidiTest.hpp"
#include <cassert>
#include <cmath>
using namespace easyknack;
static void neutralNoise(){
    DigitalNoiseNode node;node.muteBypass=true;node.frequencyValue=777;node.strengthValue=1;node.seedValue=17;
    float factor[32]{},trigger[32]{},frequency[32]{},silence[32]{};float* outputs[]{factor,trigger,frequency};
    ProcessContext context{nullptr,0,factor,32,48000,silence,32};context.outputs=outputs;context.outputCount=3;
    auto random=node.rngState;node.process(context);for(int i=0;i<32;i++)assert(factor[i]==1&&trigger[i]==0&&frequency[i]==777);assert(node.rngState==random);
    float values[32];for(int i=0;i<32;i++)values[i]=(i-8)*123.5f;const float* inputs[]{values,nullptr,nullptr};context.inputs=inputs;context.inputCount=3;context.sourceRates="acc";
    node.process(context);for(int i=0;i<32;i++)assert(factor[i]==1&&trigger[i]==0&&frequency[i]==values[i]);
    float scalar=-123.5f;inputs[0]=&scalar;context.sourceRates="ccc";node.process(context);for(float value:frequency)assert(value==scalar);
    inputs[0]=nullptr;DigitalNoiseNode active;active.seedValue=17;active.strengthValue=1;active.frequencyValue=777;node.applyParameters(active);node.process(context);assert(node.initialized&&!node.muteBypass);for(int i=0;i<32;i++)assert(std::abs(frequency[i]-777*factor[i])<.001);
    DigitalNoiseNode old;std::istringstream payload("!37322412def6ce9b -1 440 -1 1 -1 17 gaussian");old.configure(payload);assert(!old.muteBypass&&old.gaussian);
}
static void voicesAndPitchWheel(){
    filter_midi_tests::Source source;float out[32]{};using filter_midi_tests::message;
    auto voices=AudioGraph::parse("GRAPH 3 source midiin 1 @ c - note midinote 0 0 2 poly 2 @ a c sink out 1025 0 @ a a");
    source.block.append(0,message(144,60,100));source.block.append(2,message(144,64,127));voices.process(out,32,0,nullptr,&source);
    assert(voices.nodes[1].voiceCount()==2);for(int i=0;i<32;i++)assert(voices.nodes[1].extraBuffers[3][i]==0&&voices.nodes[1].voice(1).extraBuffers[3][i]==1);
    source.block.clear();source.block.append(0,message(128,60));source.block.append(0,message(128,64));voices.process(out,32,0,nullptr,&source);
    for(int i=0;i<32;i++)assert(voices.nodes[1].extraBuffers[3][i]==0&&voices.nodes[1].voice(1).extraBuffers[3][i]==1);
    auto patch=[](int range){return "GRAPH 3 source midiin 1 @ c - note midinote 0 0 "+std::to_string(range)+" poly 1 @ a c sink out 1 0 @ a a";};
    auto mono=AudioGraph::parse(patch(2));source.block.clear();source.block.append(0,message(144,69,127));source.block.append(8,message(224,0,96));mono.process(out,32,0,nullptr,&source);
    assert(std::abs(out[0]-440)<.001&&std::abs(out[8]-440*std::exp2(1./12))<.002);
    auto wider=AudioGraph::parse(patch(12));wider.inheritState(mono);source.block.clear();wider.process(out,32,0,nullptr,&source);assert(std::abs(out[0]-440*std::exp2(6./12))<.002);
    auto disabled=AudioGraph::parse(patch(0));disabled.inheritState(wider);disabled.process(out,32,0,nullptr,&source);assert(std::abs(out[0]-440)<.001);
    source.block.append(0,message(224,0,64));mono.process(out,32,0,nullptr,&source);assert(std::abs(out[0]-440)<.001);
}
static void releasedNoteValues(){
    using filter_midi_tests::message;filter_midi_tests::Source source;float out[32]{};
    auto patch=[](int voices){return "GRAPH 5 source midiin 1 @ c - note midinote 0 0 2 poly "+std::to_string(voices)+" @ a c f out 1 0 @ a a v out 769 0 @ a a g out 257 0 @ a a";};
    auto mono=AudioGraph::parse(patch(1));source.block.append(0,message(144,69,64));source.block.append(4,message(128,69));mono.process(out,32,0,nullptr,&source);
    auto& note=mono.nodes[1];for(int i=0;i<32;i++){assert(note.buffer[i]==440);assert(std::abs(note.extraBuffers[2][i]-64.f/127)<1e-6);assert(note.extraBuffers[0][i]==(i<4?1:0));}
    EnvelopeNode envelope;envelope.legacy=false;envelope.duration=envelope.decay=0;envelope.release=10;
    std::istringstream shape("4 0 0 0 0 0 0 0 .3333333333333333 1 .3333333333333333 1 .3333333333333333 1 0 .6666666666666666 1 .6666666666666666 1 .6666666666666666 1 0 1 0 1 0 1 0 0");envelope.curve.read(shape);
    const float* inputs[]{note.extraBuffers[0].data(),nullptr,nullptr,nullptr,nullptr};float env[32]{},silence[32]{};ProcessContext context{inputs,5,env,32,1000,silence,32};context.sourceRates="aaccc";envelope.process(context);
    assert(env[4]==1&&env[5]*note.extraBuffers[2][5]>0&&env[13]*note.extraBuffers[2][13]>0&&env[14]==0);
    source.block.clear();source.block.append(0,message(224,0,96));mono.process(out,32,0,nullptr,&source);assert(note.buffer[0]==440);assert(std::abs(note.extraBuffers[2][0]-64.f/127)<1e-6);
    auto rebuilt=AudioGraph::parse(patch(1));rebuilt.inheritState(mono);source.block.clear();rebuilt.process(out,32,0,nullptr,&source);assert(rebuilt.nodes[1].buffer[0]==440&&std::abs(rebuilt.nodes[1].extraBuffers[2][0]-64.f/127)<1e-6);
    auto poly=AudioGraph::parse(patch(2));source.block.append(0,message(144,60,40));source.block.append(1,message(144,64,100));source.block.append(4,message(128,60));source.block.append(6,message(128,64));poly.process(out,32,0,nullptr,&source);
    auto& a=poly.nodes[1];auto& b=a.voice(1);assert(std::abs(a.extraBuffers[2][31]-40.f/127)<1e-6&&std::abs(b.extraBuffers[2][31]-100.f/127)<1e-6);
    auto fa=a.buffer[31],fb=b.buffer[31];source.block.clear();source.block.append(0,message(224,0,96));poly.process(out,32,0,nullptr,&source);assert(a.buffer[0]==fa&&b.buffer[0]==fb);
    source.block.clear();source.block.append(0,message(144,67,90));poly.process(out,32,0,nullptr,&source);assert(std::abs(a.extraBuffers[2][0]-90.f/127)<1e-6);assert(std::abs(b.extraBuffers[2][0]-100.f/127)<1e-6);
    source.block.clear();source.block.panic=true;poly.process(out,32,0,nullptr,&source);assert(a.extraBuffers[2][0]==0&&b.extraBuffers[2][0]==0);
}
int main(){neutralNoise();voicesAndPitchWheel();releasedNoteValues();}
