#include "AudioGraph.hpp"
#include <cassert>
#include <iostream>
using namespace easyknack;

static void reconstructNoise(int size,int divisor,bool padded,SpectrumWindow window){
    constexpr int block=256;
    SpectrumNode fft;fft.size=size;fft.hop=size/divisor;fft.window=window;fft.padded=padded;fft.prepare(48000,block);
    SpectrumBlurNode blur;blur.noiseMode=true;blur.width=padded?2:1;blur.kernel="box";blur.prepare(48000,block);
    IFFTNode inverse;inverse.prepare(48000,block);
    double energy=0;for(int i=0;i<size;i++){double w=spectrumWindowValue(window,i,size);energy+=w*w;}
    std::array<float,block> in{},out{},silence{};const float* inputs[]{in.data(),nullptr,nullptr,nullptr};
    const DspResource* source[]{&fft.resource};const DspResource* processed[]{&blur.stream};
    ProcessContext c{inputs,4,out.data(),block,48000,silence.data(),block};
    for(int base=0;base<size*12;base+=block){
        c.resourceInputs=nullptr;fft.process(c);
        for(std::size_t f=0;f<fft.resource.count;f++){
            auto& frame=fft.resource.frames[f];
            for(std::size_t k=0;k<=fft.resource.size/2;k++)frame.bins[k]={std::sqrt(energy),0};
            SpectrumStream::realSymmetry(frame,fft.resource.size);
        }
        c.resourceInputs=source;blur.process(c);c.resourceInputs=processed;inverse.process(c);
        if(base>=size*4)for(int i=0;i<block;i++)
            assert(std::abs(out[i]-SpectrumNoise::sample(base+i-size))<2e-6);
    }
}

static void bandCoverage(int size){
    constexpr int block=256,capture=65536;
    SpectrumNode fft;fft.size=size;fft.hop=size/4;fft.prepare(48000,block);
    SpectrumBlurNode blur;blur.noiseMode=true;blur.logarithmic=blur.octaveMode=true;blur.octaves=1;blur.kernel="maximum";blur.prepare(48000,block);
    IFFTNode inverse;inverse.prepare(48000,block);
    std::array<float,block> in{},out{},silence{};const float* inputs[]{in.data(),nullptr,nullptr,nullptr};
    const DspResource* source[]{&fft.resource};const DspResource* processed[]{&blur.stream};
    ProcessContext c{inputs,4,out.data(),block,48000,silence.data(),block};
    std::vector<std::complex<double>> result(capture);
    for(int base=0;base<4*size+capture;base+=block){
        for(int i=0;i<block;i++)in[i]=.1*std::sin(6.283185307179586*3000*(base+i)/48000.);
        c.resourceInputs=nullptr;fft.process(c);c.resourceInputs=source;blur.process(c);c.resourceInputs=processed;inverse.process(c);
        if(base>=4*size)for(int i=0;i<block;i++){assert(std::isfinite(out[i]));result[base-4*size+i]=out[i];}
    }
    spectrumTransform(result.data(),capture);
    double quarter[4]{},lower=0,upper=0;
    for(int k=1;k<capture/2;k++){
        double hz=k*48000./capture,power=std::norm(result[k]);
        if(hz>=1600&&hz<3000)lower+=power;
        if(hz>=3000&&hz<5625)upper+=power;
    }
    // Equal-width slices within FFT cells must all carry energy, not just centres.
    // Use fractional bin positions (the long FFT resolves 64+ points per short bin).
    for(int k=1;k<capture/2;k++){
        double hz=k*48000./capture;
        if(hz>1800&&hz<5000)quarter[((k*size)%capture)*4/capture]+=std::norm(result[k]);
    }
    auto bounds=std::minmax_element(quarter,quarter+4);
    assert(*bounds.first/ *bounds.second>.65);
    // Symmetric log-frequency intervals have approximately equal power.
    assert(lower/upper>.65&&lower/upper<1.55);
}

static void tinyWidth(const char* kernel,bool padded,int divisor){
    constexpr int block=256,size=1024;
    for(float width:{0.f,.00001f,.0001f,.001f}){
        SpectrumNode fft;fft.size=size;fft.hop=size/divisor;fft.padded=padded;fft.prepare(48000,block);
        SpectrumBlurNode blur;blur.noiseMode=true;blur.logarithmic=blur.octaveMode=true;blur.octaves=width;blur.kernel=kernel;blur.prepare(48000,block);
        IFFTNode inverse;inverse.prepare(48000,block);
        std::array<float,block> in{},out{},silence{};const float* inputs[]{in.data(),nullptr,nullptr,nullptr};
        const DspResource* source[]{&fft.resource};const DspResource* processed[]{&blur.stream};
        ProcessContext c{inputs,4,out.data(),block,48000,silence.data(),block};
        double error=0,power=0;
        for(int base=0;base<65536;base+=block){
            for(int i=0;i<block;i++)in[i]=.1*std::sin(6.283185307179586*440*(base+i)/48000.);
            c.resourceInputs=nullptr;fft.process(c);c.resourceInputs=source;blur.process(c);c.resourceInputs=processed;inverse.process(c);
            if(base>=4*size)for(int i=0;i<block;i++){
                double expected=.1*std::sin(6.283185307179586*440*(base+i-size)/48000.);
                assert(std::isfinite(out[i]));error+=(out[i]-expected)*(out[i]-expected);power+=expected*expected;
            }
        }
        double relative=std::sqrt(error/power);
        std::cerr<<"Tiny width "<<width<<" "<<kernel<<" padded="<<padded<<" overlap="<<divisor<<" relative error="<<relative<<'\n';
        assert(relative<(width<=.00001f?1e-5:width<=.0001f?1e-4:1e-3));
    }
}

static void boundedPhaseAndState(){
    SpectrumStream input;input.prepare(256);input.size=input.windowSize=1024;input.hop=256;input.count=1;input.identity=9;
    SpectrumBlurNode node,reference;for(auto* p:{&node,&reference}){p->noiseMode=true;p->width=.2f;p->kernel="maximum";p->prepare(48000,256);}
    float out=0,silence=0;const float* inputs[]{nullptr,nullptr,nullptr,nullptr};const DspResource* resources[]{&input};
    ProcessContext c{inputs,4,&out,1,48000,&silence};c.resourceInputs=resources;
    double previous=0;const double span=double(node.width),depth=span*span*(3-2*span);
    for(int f=0;f<100;f++){
        input.frames[0].start=f*256;input.frames[0].bins[32]=std::polar(2.,f*.3);SpectrumStream::realSymmetry(input.frames[0],1024);
        node.process(c);reference.process(c);
        assert(node.stream.frames[0].bins[32]==reference.stream.frames[0].bins[32]);
        double offset=std::arg(node.stream.frames[0].bins[32]/input.frames[0].bins[32]);
        assert(std::abs(offset)<=3.141592653589793*depth+1e-12);
        assert(std::abs(offset-previous)<=depth*6.283185307179586*span*.25/(1-depth)+1e-12);previous=offset;
        if(f==40){SpectrumBlurNode replacement;replacement.prepare(48000,256);replacement.adoptState(node);node.adoptState(replacement);}
    }
    node.width=0;node.process(c);for(int k=0;k<1024;k++)assert(node.stream.frames[0].bins[k]==input.frames[0].bins[k]);
    node.width=.2f;node.process(c);assert(std::isfinite(std::abs(node.stream.frames[0].bins[32])));
}

int main(){
    boundedPhaseAndState();
    for(int size:{256,1024})for(int divisor:{2,4})for(bool padded:{false,true})
        for(auto window:{SpectrumWindow::hann,SpectrumWindow::rectangular})reconstructNoise(size,divisor,padded,window);
    for(int size:{256,512,1024})bandCoverage(size);
    for(auto kernel:{"maximum","box","gaussian"})for(bool padded:{false,true})for(int divisor:{2,4})tinyWidth(kernel,padded,divisor);
    std::cout<<"Noise WOLA identity, sub-bin coverage and octave power passed\n";
}
