#include "AudioGraph.hpp"
#include <cassert>
#include <iostream>
using namespace easyknack;
static void identity(std::size_t size,std::size_t divisor,std::size_t block,SpectrumWindow window,bool handover){
    FFTStreamNode fft;fft.size=size;fft.hop=size/divisor;fft.window=window;fft.prepare(48000,block);
    IFFTNode ifft;ifft.prepare(48000,block);
    std::vector<float> original(size*5+block),input(block),out(block),unused(block),silence(block);
    unsigned random=17;for(std::size_t i=0;i<original.size();i++){random=1664525*random+1013904223;original[i]=.2f*std::sin(i*.071)+.1f*(float(random>>8)/16777216.f-.5f);}original[0]=1;
    const float* inputs[]{input.data()};const DspResource* resources[]{&fft.stream};
    ProcessContext c{inputs,1,unused.data(),block,48000,silence.data(),block};
    bool adopted=false;
    for(std::size_t base=0;base<original.size();base+=block){
        c.bufferSize=c.blockFrames=std::min(block,original.size()-base);std::copy_n(original.data()+base,c.bufferSize,input.data());fft.process(c);
        auto inverse=c;inverse.output=out.data();inverse.resourceInputs=resources;ifft.process(inverse);
        for(std::size_t i=0;i<c.bufferSize;i++){double expected=base+i>=size?original[base+i-size]:0;if(std::abs(out[i]-expected)>2e-5){std::cerr<<"identity "<<size<<" "<<divisor<<" "<<block<<" at "<<base+i<<": "<<out[i]<<" vs "<<expected<<'\n';std::abort();}}
        if(handover&&!adopted&&base>=2*size){
            FFTStreamNode next;next.size=size;next.hop=size/divisor;next.window=window;next.prepare(48000,block);next.adoptState(fft);fft.adoptState(next);
            IFFTNode nextInverse;nextInverse.prepare(48000,block);nextInverse.adoptState(ifft);ifft.adoptState(nextInverse);adopted=true;
        }
    }
}
static void unifiedIdentity(std::size_t size,std::size_t divisor,std::size_t block,SpectrumWindow window,bool handover,bool padded){
    SpectrumNode fft;fft.size=size;fft.hop=size/divisor;fft.window=window;fft.padded=padded;fft.prepare(48000,block);
    IFFTNode ifft;ifft.prepare(48000,block);
    std::vector<float> original(size*5+block),input(block),out(block),unused(block),silence(block);
    unsigned random=17;for(std::size_t i=0;i<original.size();i++){random=1664525*random+1013904223;original[i]=.2f*std::sin(i*.071)+.1f*(float(random>>8)/16777216.f-.5f);}original[0]=1;
    const float* inputs[]{input.data()};const DspResource* resources[]{&fft.resource};
    ProcessContext c{inputs,1,unused.data(),block,48000,silence.data(),block};
    bool adopted=false;
    for(std::size_t base=0;base<original.size();base+=block){
        c.bufferSize=c.blockFrames=std::min(block,original.size()-base);std::copy_n(original.data()+base,c.bufferSize,input.data());fft.process(c);
        auto inverse=c;inverse.output=out.data();inverse.resourceInputs=resources;ifft.process(inverse);
        for(std::size_t i=0;i<c.bufferSize;i++){double expected=base+i>=size?original[base+i-size]:0;if(std::abs(out[i]-expected)>2e-5){std::cerr<<"identity "<<size<<" "<<divisor<<" "<<block<<" at "<<base+i<<": "<<out[i]<<" vs "<<expected<<'\n';std::abort();}}
        if(handover&&!adopted&&base>=2*size){
            SpectrumNode next;next.size=size;next.hop=size/divisor;next.window=window;next.padded=padded;next.prepare(48000,block);next.adoptState(fft);fft.adoptState(next);
            IFFTNode nextInverse;nextInverse.prepare(48000,block);nextInverse.adoptState(ifft);ifft.adoptState(nextInverse);adopted=true;
        }
    }
}
static void lateFramesAreNotReplayed(){
    SpectrumNode fft;fft.size=256;fft.hop=64;fft.window=SpectrumWindow::rectangular;fft.synchronous=false;fft.prepare(48000,256);fft.job=std::make_shared<AnalysisJob>();
    IFFTNode inverse;inverse.prepare(48000,256);
    std::array<float,256> samples{},silence{},out{};samples.fill(.5f);const float* inputs[]{samples.data()};const DspResource* resources[]{&fft.resource};
    ProcessContext c{inputs,1,out.data(),256,48000,silence.data(),256};c.resourceInputs=resources;
    for(int block=0;block<8;block++){fft.process(c);inverse.process(c);for(float x:out)assert(x==0);}
    assert(fft.job->dropped>0);fft.job->work();fft.process(c);assert(fft.resource.count==1);inverse.process(c);
    for(float x:out)assert(x==0); // Original positions are already past; never replay stale sound now.
    auto* analysis=fft.resource.analysisResult();assert(analysis&&analysis->spectrum.valid());assert(std::abs(analysis->views[0][0]-.5)<1e-7);
    assert(analysis==fft.resource.analysisResult());
}
static void largeBackgroundAnalysis(std::size_t size,bool padded){
    auto job=std::make_unique<AnalysisJob>();std::vector<float> samples(size);
    constexpr double tau=6.2831853071795864769;
    for(std::size_t i=0;i<size;i++)samples[i]=static_cast<float>(.7*std::cos(tau*37*i/size));
    assert(job->submitSpectrum(samples.data(),0,size,size/4,SpectrumWindow::rectangular,48000,size,17,padded));job->work();
    auto* result=job->acquire();assert(result&&result->spectrum.fftFrames==size*(padded?2:1));
    assert(result->binCount==result->spectrum.fftFrames/2+1);
    assert(std::abs(result->views[0][37*(padded?2:1)]-.7)<1e-6);
    AnalysisResource resource;resource.result=result;
    AnalysisViewerNode viewer;viewer.mailbox=std::make_shared<AnalysisMailbox>();
    const DspResource* resources[]{&resource};float zero=0;ProcessContext context{nullptr,1,&zero,1,48000,&zero,1};context.resourceInputs=resources;
    viewer.process(context);auto captured=std::make_unique<AnalysisFrame>();
    assert(viewer.mailbox->latest(*captured));assert(captured->valueCount==result->binCount);
    assert(std::abs(captured->span-24000)<1e-8);
    assert(job->submitCorrelation(result->spectrum));job->work();
    result=job->acquire();assert(result&&result->correlationCount==size);assert(std::abs(result->views[3][0]-1)<1e-6);
    assert(std::isfinite(result->views[3][size-1]));
    // Reusing the same worker for legacy analysis must restore the fixed lag count.
    HistoryResource history;history.prepare(48000,1,256);for(float value:samples)history.append(value);
    assert(job->submit(history,40,2000));job->work();assert(job->acquire()->correlationCount==8192);
}
int main(){
    for(auto size:{16384u,32768u,65536u})for(bool padded:{false,true}){
        unifiedIdentity(size,4,256,SpectrumWindow::hann,true,padded);
        largeBackgroundAnalysis(size,padded);
    }
    for(bool padded:{false,true})for(auto size:{256u,1024u,8192u})for(auto block:{137u,256u})for(auto window:{SpectrumWindow::hann,SpectrumWindow::rectangular})unifiedIdentity(size,4,block,window,true,padded);
    lateFramesAreNotReplayed();

    for(auto size:{256u,1024u,8192u})for(auto divisor:{2u,4u})for(auto block:{64u,137u,256u,1024u})for(auto window:{SpectrumWindow::hann,SpectrumWindow::hamming,SpectrumWindow::blackman,SpectrumWindow::rectangular})identity(size,divisor,block,window,true);
    const std::size_t size=1024,block=256;FFTStreamNode fft;fft.size=size;fft.hop=256;fft.prepare(48000,block);
    SpectrumShiftNode shift;shift.shift=3;shift.prepare(48000,block);IFFTNode inverse;inverse.prepare(48000,block);
    std::array<float,block> input{},out{},silence{};const float* inputs[]{input.data(),nullptr};const DspResource* resources[]{&fft.stream,nullptr};
    ProcessContext c{inputs,2,out.data(),block,48000,silence.data(),block};double error=0;int count=0;
    for(int base=0;base<16384;base+=block){
        for(std::size_t i=0;i<block;i++)input[i]=std::cos(6.283185307179586*10*(base+i)/size);fft.process(c);
        c.resourceInputs=resources;shift.process(c);const DspResource* shifted[]{&shift.stream};c.resourceInputs=shifted;inverse.process(c);c.resourceInputs=nullptr;
        if(base>2048)for(std::size_t i=0;i<block;i++){double expected=std::cos(6.283185307179586*13*(base+i-size)/size);error=std::max(error,std::abs(out[i]-expected));count++;}
    }assert(count&&error<1e-4);
    StreamSpectrumFrame frame;frame.bins.resize(256);frame.bins[1]={2,4};frame.bins[2]={4,8};assert(readSpectrumBin(frame,256,1.5)==std::complex<double>(3,6));assert(readSpectrumBin(frame,256,-1)==std::complex<double>{});assert(readSpectrumBin(frame,256,-1,2)==frame.bins[128]);
    std::cout<<"Streaming FFT/IFFT reconstruction, timing, handover and bin shift tests passed\n";
}
