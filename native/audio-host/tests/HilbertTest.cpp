#include "nodes/HilbertNode.hpp"
#include <cassert>
#include <sstream>
#include <iostream>
using namespace easyknack;
int main(){
    constexpr double pi=3.14159265358979323846;
    constexpr int n=16384;
    std::vector<float> input(n),direct(n),quadrature(n),zero(n);
    const float* inputs[]={input.data()};float* outputs[]={direct.data(),quadrature.data()};
    ProcessContext c{inputs,1,direct.data(),n,48000,zero.data()};c.outputs=outputs;c.outputCount=2;
    HilbertNode impulse;input[0]=1;impulse.process(c);
    for(int i=0;i<n;++i)assert(direct[i]==(i==128?1.f:0.f));
    for(int i=0;i<257;++i)assert(std::abs(quadrature[i]+quadrature[256-i])<1e-7);
    for(int i=257;i<n;++i)assert(quadrature[i]==0);
    for(double frequency:{440.,1000.,5000.,15000.,22000.}){
        HilbertNode node;
        for(int i=0;i<n;++i)input[i]=float(std::cos(2*pi*frequency*i/48000));
        node.process(c);double error=0,power=0;
        double desiredReal=0,desiredImag=0,imageReal=0,imageImag=0;
        for(int i=512;i<n;++i){
            double phase=2*pi*frequency*(i-128)/48000;
            error+=std::pow(quadrature[i]-std::sin(phase),2);power+=std::pow(std::sin(phase),2);
            assert(std::abs(direct[i]-std::cos(phase))<1e-6);
            double carrier=2*pi*100*i/48000;
            double shifted=direct[i]*std::cos(carrier)-quadrature[i]*std::sin(carrier);
            double desired=2*pi*(frequency+100)*i/48000,image=2*pi*(frequency-100)*i/48000;
            desiredReal+=shifted*std::cos(desired);desiredImag+=shifted*std::sin(desired);
            imageReal+=shifted*std::cos(image);imageImag+=shifted*std::sin(image);
        }
        assert(std::sqrt(error/power)<.02);
        assert(std::hypot(imageReal,imageImag)/std::hypot(desiredReal,desiredImag)<.02);
        // Sample-feedback calls must match a block evaluation exactly.
        HilbertNode samplewise;
        for(int i=0;i<n;++i){float d=0,q=0;const float* in[]={&input[i]};float* out[]={&d,&q};
            ProcessContext s{in,1,&d,1,48000,zero.data()};s.outputs=out;s.outputCount=2;
            samplewise.processSample(s);assert(d==direct[i]);assert(q==quadrature[i]);}
        // Graph replacement must retain the filter history.
        HilbertNode replacement;replacement.inheritState(node);
        node.process(c);auto expectedDirect=direct,expectedQuadrature=quadrature;
        replacement.process(c);assert(direct==expectedDirect);assert(quadrature==expectedQuadrature);
    }
    HilbertNode dc;std::fill(input.begin(),input.end(),1);dc.process(c);
    for(int i=257;i<n;++i)assert(std::abs(quadrature[i])<1e-7);
    HilbertNode silent;inputs[0]=nullptr;silent.process(c);
    for(int i=0;i<n;++i)assert(direct[i]==0&&quadrature[i]==0);
    std::cout<<"Hilbert impulse, phase, SSB rejection, feedback, state and silence tests passed\n";
}
