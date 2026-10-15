#include "AudioGraph.hpp"
#include "WaveformCapture.hpp"
#include <cassert>
using namespace easyknack;
int main(){
    auto wave=std::make_unique<WaveformResource>();std::array<float,2048> samples{};
    for(std::size_t i=0;i<samples.size();i++)samples[i]=std::sin(Wavetables::tau*i/samples.size());wave->fromSamples(samples);
    for(int i=0;i<100;i++)assert(std::abs(wave->sample(i/100.,440./48000)-std::sin(Wavetables::tau*i/100.))<1e-5);
    assert(wave->sample(.25,.6)==0);
    auto spectrum=std::make_unique<SpectrumFrame>();spectrum->fftFrames=spectrum->windowFrames=1024;spectrum->windowSum=1024;spectrum->sampleRate=48000;
    spectrum->bins[10]={0,-512};spectrum->bins[1014]={0,512};
    auto captured=std::make_unique<WaveformResource>();WaveformCapture::extract(*spectrum,468.75,false,*captured);
    for(int i=0;i<100;i++)assert(std::abs(captured->sample(i/100.,468.75/48000)-std::sin(Wavetables::tau*i/100.))<1e-5);
    WaveformCapture job;assert(job.submit(*spectrum,468.75,true));assert(!job.submit(*spectrum,440,true));
    for(int n=0;n<500&&job.state.load()!=2;n++)std::this_thread::sleep_for(std::chrono::milliseconds(2));assert(job.state.load()==2);
    std::array<float,256> output{},silence{};const float* inputs[7]{};const DspResource* resources[7]{};
    ProcessContext c{inputs,7,output.data(),256,48000,silence.data(),256};c.resourceInputs=resources;
    OscillatorNode osc;std::istringstream config("-1 440 wave sine -1");osc.configure(config);osc.prepare(48000,256);resources[1]=wave.get();osc.process(c);
    for(int i=0;i<256;i++)assert(std::abs(output[i]-std::sin(Wavetables::tau*440*i/48000))<1e-5);
    AdditiveOscillatorNode additive;std::istringstream ac("-1 sine 1 1 0 -1 440 -1 0");additive.configure(ac);additive.prepare(48000,256);resources[1]=nullptr;resources[0]=wave.get();additive.process(c);
    for(int i=0;i<256;i++)assert(std::abs(output[i]-std::sin(Wavetables::tau*440*i/48000))<1e-5);
    // Missing resource at a group boundary must retain the oscillator's builtin choice.
    resources[0]=resources[1]=nullptr;inputs[1]=silence.data();osc.resetPhase();osc.process(c);
    for(int i=0;i<256;i++)assert(std::abs(output[i]-std::sin(Wavetables::tau*440*i/48000))<1e-5);
    inputs[1]=nullptr;
    auto analyzed=std::make_unique<AnalysisResult>();analyzed->spectrum=*spectrum;
    AnalysisResource inputResource;inputResource.result=analyzed.get();resources[0]=&inputResource;
    ValueEventBuffer triggers;triggers.clear(256);triggers.push(127,1,ValueEventKind::trigger);
    const ValueEventBuffer* events[7]{};events[2]=&triggers;c.valueEventInputs=events;c.bufferSize=1;
    SpectrumWaveformNode capture;capture.frequency=468.75;capture.prepare(48000,256);capture.process(c);
    assert(!capture.pending&&capture.job->state.load()!=0);
    for(int n=0;n<500&&capture.job->state.load()!=2;n++)std::this_thread::sleep_for(std::chrono::milliseconds(2));assert(capture.job->state.load()==2);
    triggers.clear(256);capture.process(c);assert(std::abs(capture.wave->sample(.25,.01)-1)<1e-5);
    c.valueEventInputs=nullptr;c.bufferSize=256;
    // Full resource samples retain details that the playback mip levels intentionally omit.
    std::array<float,WaveformResource::size> raw{};raw[7]=.75f;raw[513]=-.42f;
    wave->fromSamples(raw);assert(wave->samples==raw);
    auto viewer=std::make_unique<WavetableViewerNode>();viewer->bindIdentity("waveform-viewer-test");resources[0]=wave.get();viewer->process(c);
    AnalysisFrame viewed;assert(viewer->mailbox->latest(viewed));assert(viewed.valueCount==raw.size());
    for(std::size_t i=0;i<raw.size();i++)assert(viewed.values[i]==raw[i]);
    viewer->process(c);assert(!viewer->mailbox->latest(viewed));
    // Background request cadence is independent of window duration, and synchronous cadence stays unchanged.
    auto fft=std::make_unique<SpectrumNode>();fft->size=256;fft->hop=128;fft->synchronous=false;fft->intervalMs=100;fft->prepare(48000,256);
    assert(fft->effectiveHop()==4800);c.resourceInputs=nullptr;
    for(int block=0;block<20;block++)fft->process(c);
    assert(fft->job->submitted==2);assert(fft->lastSubmission==5056);
    auto same=std::make_unique<SpectrumNode>();same->size=256;same->hop=128;same->synchronous=false;same->intervalMs=100;same->prepare(48000,256);same->adoptState(*fft);
    assert(same->lastSubmission==5056);same->synchronous=true;assert(same->effectiveHop()==128);
    // GRAPH_TEST
    BakedInput boundary;std::istringstream bc("0 7 0");boundary.configure(bc);assert(boundary.outputResourceKind(0)==ResourceKind::waveform);
}
