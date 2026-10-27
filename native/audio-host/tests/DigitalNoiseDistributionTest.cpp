#include "AudioGraph.hpp"
#include <cassert>
#include <cmath>
#include <iostream>
using namespace easyknack;

static void distribution(bool gaussian,float frequency){
    DigitalNoiseNode node;node.frequencyValue=frequency;node.strengthValue=1;node.seedValue=781;node.gaussian=gaussian;
    std::array<float,256> factors{},triggers{},hz{},silence{};
    float* outputs[]{factors.data(),triggers.data(),hz.data()};
    ProcessContext c{nullptr,0,factors.data(),256,48000,silence.data(),256};c.outputs=outputs;c.outputCount=3;
    constexpr std::size_t samples=1u<<22;
    double sum=0,squares=0,third=0;std::array<std::size_t,6> histogram{};
    for(std::size_t b=0;b<samples/256;b++){
        node.process(c);
        for(unsigned i=0;i<256;i++){
            double x=std::log2(factors[i]);assert(std::isfinite(x)&&x>=-1.000001&&x<=1.000001);
            sum+=x;squares+=x*x;third+=x*x*x;
            ++histogram[std::min(5,std::max(0,int((x+1)*3)))];
            assert(hz[i]==frequency*factors[i]);
            if(i&&factors[i]!=factors[i-1])assert(triggers[i]==1);
        }
    }
    double mean=sum/samples,variance=squares/samples-mean*mean;
    double phi3=std::exp(-4.5)/std::sqrt(2*3.141592653589793),mass=std::erf(3/std::sqrt(2.));
    double expected=gaussian?(1-6*phi3/mass)/9:1./3;
    std::cout<<(gaussian?"Gaussian":"Uniform")<<" @ "<<frequency<<" Hz: mean oct="<<mean<<", variance="<<variance<<std::endl;
    assert(std::abs(mean)<.006);assert(std::abs(variance-expected)<.004);assert(std::abs(third/samples)<.006);
    for(unsigned k=0;k<6;k++){
        double expectedMass=gaussian?(.5*std::erf((k-2.)/std::sqrt(2.))-.5*std::erf((k-3.)/std::sqrt(2.)))/mass:1./6;
        assert(std::abs(double(histogram[k])/samples-expectedMass)<.006);
    }
}

int main(){
    DigitalNoiseNode legacy;std::istringstream old("!5bc07ddcba438cf0 -1 440 -1 .5 -1 123");legacy.configure(old);
    assert(legacy.sources.size()==3&&legacy.seedValue==123&&!legacy.gaussian&&legacy.strengthValue==.5f);
    for(bool gaussian:{false,true})for(float frequency:{4000.f,16000.f})distribution(gaussian,frequency);
    // Block and sample-feedback paths generate exactly the same sequence.
    DigitalNoiseNode block,sample;block.seedValue=sample.seedValue=17;block.gaussian=sample.gaussian=true;
    block.strengthValue=sample.strengthValue=1;block.frequencyValue=sample.frequencyValue=440;
    std::array<float,256> a{},b{},ta{},tb{},ha{},hb{},silence{};
    float* oa[]{a.data(),ta.data(),ha.data()};float* ob[]{b.data(),tb.data(),hb.data()};
    ProcessContext c{nullptr,0,a.data(),256,48000,silence.data(),256};c.outputs=oa;c.outputCount=3;
    for(unsigned n=0;n<16;n++){
        block.process(c);
        for(unsigned i=0;i<256;i++){
            float* one[]{b.data()+i,tb.data()+i,hb.data()+i};auto s=c;s.output=b.data()+i;s.outputs=one;s.bufferSize=1;s.samplewise=true;
            sample.process(s);
        }
        assert(a==b&&ta==tb&&ha==hb&&block.rngState==sample.rngState&&block.phase==sample.phase);
    }
    // Zero strength immediately bypasses modulation, without a new random draw.
    auto state=block.rngState;block.strengthValue=0;block.process(c);
    for(unsigned i=0;i<256;i++)assert(a[i]==1&&ha[i]==440);assert(block.rngState==state);
    // Sharing may reuse identical distributions, but not a different distribution.
    DigitalNoiseNode first,second;first.seedValue=second.seedValue=3;first.strengthValue=second.strengthValue=1;
    first.instanceCache=second.instanceCache=std::make_shared<NoiseBlockCache>();
    first.process(c);second.process(c);assert(first.instanceCache->hits==1);
    first.gaussian=true;first.process(c);second.process(c);assert(first.instanceCache->renders==3);
}
