#include "AudioGraph.hpp"
#include <cassert>
#include <iostream>
using namespace easyknack;

static double variation(const std::vector<double>& values){
    double sum=0,squares=0;
    for(double value:values){sum+=value;squares+=value*value;}
    double mean=sum/values.size();return std::sqrt(std::max(0.,squares/values.size()-mean*mean))/mean;
}

static void narrowBand(int size,int divisor){
    constexpr int block=256,rate=48000;
    SpectrumNode fft;fft.size=size;fft.hop=size/divisor;fft.prepare(rate,block);
    // Exercise full noise: sub-cell widths now intentionally have almost no
    // random envelope left and are not a suitable gain-correction fixture.
    SpectrumBlurNode blur;blur.noiseMode=true;blur.width=1;blur.kernel="maximum";blur.prepare(rate,block);
    IFFTNode raw,corrected;raw.prepare(rate,block);corrected.prepare(rate,block);corrected.stability=1;
    std::array<float,block> in{},out{},reference{},silence{};
    const float* inputs[]{in.data(),nullptr,nullptr,nullptr};
    const DspResource* source[]{&fft.resource};const DspResource* processed[]{&blur.stream};
    ProcessContext c{inputs,4,out.data(),block,rate,silence.data(),block};
    std::vector<double> rawLevels,correctedLevels;
    double p0=0,p1=0;int samples=0;
    for(int base=0;base<rate*8;base+=block){
        for(int i=0;i<block;i++)in[i]=.1*std::sin(6.283185307179586*440*(base+i)/rate);
        c.output=out.data();c.resourceInputs=nullptr;fft.process(c);c.resourceInputs=source;blur.process(c);
        c.resourceInputs=processed;c.output=reference.data();raw.process(c);c.output=out.data();corrected.process(c);
        if(base<rate)continue;
        for(int i=0;i<block;i++){
            assert(std::isfinite(out[i]));
            if(std::abs(reference[i])>1e-9){double ratio=out[i]/reference[i];assert(ratio>=.24999&&ratio<=4.00001);}
            p0+=reference[i]*reference[i];p1+=out[i]*out[i];
            if(++samples==960){rawLevels.push_back(std::sqrt(p0/samples));correctedLevels.push_back(std::sqrt(p1/samples));samples=0;p0=p1=0;}
        }
    }
    auto before=variation(rawLevels),after=variation(correctedLevels);
    std::cerr<<"N="<<size<<" overlap="<<divisor<<" RMS variation "<<before<<" -> "<<after<<'\n';
    assert(after<before*.9);
    // Missing stream resets both overlap and gain histories, with silent output.
    c.resourceInputs=nullptr;corrected.process(c);
    for(float value:out)assert(value==0);
    assert(corrected.stabilizer.gain==1&&corrected.stabilizer.seen==0);
}

static void boundedAndContinuous(){
    EnvelopeStabilizer whole,chunked,bypass;whole.prepare(48000);chunked.prepare(48000);bypass.prepare(48000);
    for(int i=0;i<200000;i++){
        double sample=i<50000||i>150000?.1*std::sin(i*.06):0;
        assert(bypass.process(sample,0)==sample);
        auto value=whole.process(sample,1);assert(value==chunked.process(sample,1));
        assert(std::isfinite(value)&&std::abs(value)<=4*std::abs(sample)+1e-15);
        if(i==12345){IFFTNode old,next;old.prepare(48000,256);next.prepare(48000,256);old.stabilizer=chunked;next.adoptState(old);chunked=next.stabilizer;}
    }
    for(int i=0;i<20000;i++)whole.process(0,0);
    assert(whole.amount==0&&whole.process(.25,0)==.25);
    IFFTNode legacy;std::istringstream in("!0aba7785f0529c4e 3");legacy.configure(in);
    assert(legacy.sources[0]==3&&legacy.stability==0);
}
int main(){
    boundedAndContinuous();
    for(int size:{256,512,1024})for(int divisor:{2,4})narrowBand(size,divisor);
}
