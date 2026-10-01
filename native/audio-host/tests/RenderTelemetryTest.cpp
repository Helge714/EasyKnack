#include "RenderTelemetry.hpp"
#include "AudioTiming.hpp"
#include "nodes/FFTStreamNode.hpp"
#include "audio/AudioBackend.hpp"
#include <cassert>
#include <iostream>
#include <thread>
using namespace easyknack;
int main(){
    // Sparse spectral bursts are measured over their whole hop, including
    // ordinary audio work in the intervening blocks. No time is discarded.
    for(unsigned hop:{128u,256u,512u,1024u,4096u}){
        AudioTiming timing;const unsigned cycle=std::max(256u,hop);
        for(unsigned i=0;i<cycle/256*16;i++){
            const double spectral=(i*256%cycle==0)?cycle/48000.0*.4:0;
            timing.observe(256/48000.0*.1+spectral,256,48000,hop);
        }
        auto measured=timing.telemetry.snapshot();
        assert(measured.total==16&&measured.frames==cycle);
        assert(std::abs(measured.mean-50)<1e-8&&std::abs(measured.peak-50)<1e-8);
        assert(measured.overBudget==0);
        // A genuinely slower cycle must still be visible in Peak.
        for(unsigned i=0;i<cycle/256;i++)timing.observe(cycle/48000.0*.9/(cycle/256),256,48000,hop);
        measured=timing.telemetry.snapshot();assert(std::abs(measured.peak-90)<1e-8&&measured.mean<90);
    }
    AudioTiming changed;changed.observe(.1,256,48000,1024); // incomplete old graph
    changed.observe(.001,256,48000,256);
    assert(changed.telemetry.snapshot().total==1&&changed.telemetry.snapshot().peak<20);
    RenderCadence::begin();RenderCadence::include(512);RenderCadence::include(2048);
    RenderCadence::include(128);assert(RenderCadence::period==2048);
    RenderCadence::begin();assert(RenderCadence::period==1);
    AudioTiming fragments;
    for(int i=0;i<16;i++)for(unsigned chunk:{200u,56u})fragments.observe(chunk/48000.0*.5,chunk,48000,1024);
    auto fragmented=fragments.telemetry.snapshot();
    assert(fragmented.total==4&&fragmented.frames==1024&&std::abs(fragmented.peak-50)<1e-8);
    RenderCadence::include(1487);RenderCadence::include(1493);
    assert(RenderCadence::period==RenderCadence::maxCycle);
    assert(RenderCadence::cycle(256,1487)==RenderCadence::maxCycle);
    changed.resetCycle();changed.observe(.1,256,48000,1024);
    changed.telemetry.reset();
    for(int i=0;i<4;i++)changed.observe(.001,256,48000,1024);
    assert(changed.telemetry.snapshot().total==1&&changed.telemetry.snapshot().peak<20);
    // Cadence is reported on empty FFT blocks too, not just on frame emission.
    FFTStreamNode fft;fft.size=2048;fft.hop=1024;fft.prepare(48000,256);
    float input[256]{},output[256]{};const float* inputs[]={input};
    ProcessContext context{};context.inputs=inputs;context.inputCount=1;
    context.output=output;context.silence=input;context.bufferSize=256;context.sampleRate=48000;
    for(int i=0;i<8;i++){
        RenderCadence::begin();fft.process(context);
        assert(RenderCadence::period==1024&&fft.stream.count==((i+1)%4==0?1u:0u));
    }
    RenderTelemetry t;
    for(int i=0;i<100;i++)t.observe(i==99?.01:.001,48,48000,100+i);
    auto s=t.snapshot(200);assert(s.total==100&&s.overBudget==1&&s.p99us==1000&&s.maxUs==10000&&s.peak==1000);
    assert(t.snapshot(2000000000).frames==0);auto epoch=s.epoch;t.reset();s=t.snapshot(2000000000);assert(s.epoch>epoch&&s.total==0&&s.maxUs==0);
    for(unsigned i=0;i<RenderTelemetry::capacity+7;i++)t.observe(.001,256,48000,2000000000);
    s=t.snapshot(2000000000);assert(s.dropped==7&&s.total==RenderTelemetry::capacity);
    t.reset();assert(t.snapshot().dropped==0);
    t.observe(-1,256,48000);t.observe(1,0,48000);assert(t.snapshot().total==0);
    // Producer and consumer exercise wrap-around concurrently, with no lost or duplicated accounting.
    std::atomic<bool> done{false};std::thread producer([&]{for(int i=0;i<20000;i++)t.observe(.0001,256,48000);done.store(true);});
    while(!done.load())t.snapshot();producer.join();s=t.snapshot();assert(s.total+s.dropped==20000);
    StereoRing<1024> ring;float block[512]{};ring.readAdaptive(block,256);assert(ring.priming==1&&ring.underruns==0);
    ring.write(block,256);ring.write(block,256);for(int i=0;i<8&&ring.underruns==0;i++)ring.readAdaptive(block,256);assert(ring.underruns==1);
    ring.write(block,256);ring.write(block,256);ring.write(block,256);ring.write(block,256);ring.write(block,256);assert(ring.overruns>0);
    std::cout<<"Render telemetry windows, reset, overflow, concurrent transport and priming passed\n";
}


