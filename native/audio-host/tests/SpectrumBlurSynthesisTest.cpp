#include "AudioGraph.hpp"
#include <cassert>
#include <iostream>
using namespace easyknack;

static void synthesizedBand(int size,int divisor,SpectrumWindow window,bool padded,const char* kernel){
    constexpr int block=256;
    const int capture=size*16;
    SpectrumNode fft;fft.size=size;fft.hop=size/divisor;fft.window=window;fft.padded=padded;fft.prepare(48000,block);
    SpectrumBlurNode blur;blur.kernel=kernel;blur.logarithmic=true;blur.width=1;blur.prepare(48000,block);
    IFFTNode inverse;inverse.prepare(48000,block);
    std::array<float,block> in{},out{},silence{};
    const float* inputs[]{in.data(),nullptr,nullptr,nullptr};
    const DspResource* spectrum[]{&fft.resource};const DspResource* blurred[]{&blur.stream};
    ProcessContext c{inputs,4,out.data(),block,48000,silence.data(),block};
    std::vector<std::complex<double>> result(capture);
    for(int base=0;base<4*size+capture;base+=block){
        for(int i=0;i<block;i++)in[i]=.1*std::sin(6.283185307179586*440*(base+i)/48000.);
        c.resourceInputs=nullptr;fft.process(c);c.resourceInputs=spectrum;blur.process(c);c.resourceInputs=blurred;inverse.process(c);
        if(base>=4*size)for(int i=0;i<block;i++){assert(std::isfinite(out[i]));result[base-4*size+i]=out[i];}
    }
    spectrumTransform(result.data(),capture);
    int n=padded?2*size:size;double low=1e30,high=0;int count=0;
    for(int k=1;k<n/2;k++){
        double hz=k*48000./n;if(hz<250||hz>800)continue;
        double amplitude=std::abs(result[k*capture/n])*2/capture;
        low=std::min(low,amplitude);high=std::max(high,amplitude);count++;
    }
    // The entire interior must survive resynthesis, rather than only the source/edges.
    if(!(count>5&&low>1e-5)){
        std::cerr<<size<<" / "<<divisor<<" padded="<<padded<<" kernel="<<kernel<<" minimum="<<low<<" maximum="<<high<<'\n';std::abort();
    }
    if(std::string(kernel)=="maximum"&&divisor>=4&&!padded)assert(low/high>.95);
}

static void phaseTimingAndBypass(){
    SpectrumBlurNode blur;blur.kernel="maximum";blur.logarithmic=true;blur.width=1;blur.prepare(48000,256);
    SpectrumStream input;input.prepare(256);input.size=input.windowSize=1024;input.hop=256;input.sampleRate=48000;input.count=1;
    input.frames[0].bins[32]={2,1};SpectrumStream::realSymmetry(input.frames[0],1024);
    const float* inputs[]{nullptr,nullptr,nullptr,nullptr};const DspResource* resources[]{&input};float out=0,silence=0;
    ProcessContext c{inputs,4,&out,1,48000,&silence};c.resourceInputs=resources;
    for(auto start:{std::int64_t(-1024),std::int64_t(0),std::int64_t(1)<<54}){
        input.frames[0].start=start;blur.process(c);auto old=blur.stream.frames[0].bins[23];
        input.frames[0].start=start+256;blur.process(c);
        auto expected=old*std::polar(1.,6.283185307179586*23*256/1024);
        assert(std::abs(expected-blur.stream.frames[0].bins[23])<1e-12);
    }
    blur.preservePhase=true;blur.process(c);
    assert(std::abs(std::arg(blur.stream.frames[0].bins[32])-std::arg(input.frames[0].bins[32]))<1e-12);
    for(bool preserve:{false,true}){
        blur.preservePhase=preserve;blur.width=0;blur.process(c);
        for(int k=0;k<1024;k++)assert(blur.stream.frames[0].bins[k]==input.frames[0].bins[k]);
    }
}

int main(){
    phaseTimingAndBypass();
    for(int size:{1024,2048})for(int divisor:{2,4,8})
        for(auto window:{SpectrumWindow::hann,SpectrumWindow::rectangular})
            synthesizedBand(size,divisor,window,false,"maximum");
    for(auto kernel:{"maximum","box","gaussian"})for(bool padded:{false,true})
        synthesizedBand(2048,4,SpectrumWindow::hann,padded,kernel);
    std::cout<<"Blur/IFFT band coverage, phase timing and bypass passed\n";
}
