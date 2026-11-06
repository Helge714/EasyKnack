#include "nodes/SpectrumMathNode.hpp"
#include "nodes/SpectrumMixNode.hpp"
#include "SpectrumArithmeticParameters.hpp"
#include <cassert>
#include <cstdlib>
#include <new>
#include <iostream>
using namespace easyknack;
static thread_local bool watching=false;static unsigned allocations=0;
void* operator new(std::size_t n){if(watching)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void operator delete(void* p)noexcept{std::free(p);}void operator delete(void* p,std::size_t)noexcept{std::free(p);}
static void close(std::complex<double> a,std::complex<double> b){assert(std::abs(a-b)<1e-10);}
int main(){
    using Op=SpectrumMathNode::Operation;
    close(SpectrumMathNode::evaluate({1,2},{3,-4},Op::add),{4,-2});
    close(SpectrumMathNode::evaluate({1,2},{3,-4},Op::subtract),{-2,6});
    close(SpectrumMathNode::evaluate({1,2},{3,-4},Op::multiply),{11,2});
    close(SpectrumMathNode::evaluate({1,2},{3,-4},Op::divide),{-.2,.4});
    close(SpectrumMathNode::evaluate({1,2},{},Op::divide),{});
    close(SpectrumMathNode::evaluate({1e308,1e308},{1e308,1e308},Op::multiply),{});
    SpectrumStream a,b;a.prepare(256);b.prepare(256);a.size=b.size=a.windowSize=b.windowSize=128;a.hop=b.hop=64;a.sampleRate=b.sampleRate=48000;a.count=b.count=2;a.blockFrames=b.blockFrames=256;
    for(unsigned f=0;f<2;f++){
        a.frames[f].start=b.frames[f].start=f*64;
        std::fill_n(a.frames[f].bins.data(),128,std::complex<double>{});std::fill_n(b.frames[f].bins.data(),128,std::complex<double>{});
        a.frames[f].bins[3]={1,2};b.frames[f].bins[3]={3,-4};SpectrumStream::realSymmetry(a.frames[f],128);SpectrumStream::realSymmetry(b.frames[f],128);
    }
    SpectrumMathNode math;std::istringstream mp(mathParameters);math.configure(mp);math.prepare(48000,256);
    SpectrumMixNode mix;std::istringstream xp(mixParameters);mix.configure(xp);mix.prepare(48000,256);
    SpectrumMixNode legacy;std::istringstream lp("!1317e59a28722f80 -1 -1 2 3");legacy.configure(lp);legacy.prepare(48000,256);
    float silence[256]{},output[256]{},factor=.5f;const float* numeric[]={nullptr,nullptr,&factor};char rates[]={'c','c','c'};const DspResource* resources[]={&a,&b,nullptr};
    ProcessContext c{numeric,3,output,256,48000,silence};c.resourceInputs=resources;c.sourceRates=rates;
    watching=true;
    for(auto op:{Op::add,Op::subtract,Op::multiply,Op::divide}){
        math.operation=op;math.process(c);assert(math.stream.count==2);
        close(math.stream.frames[0].bins[3],SpectrumMathNode::evaluate({1,2},{3,-4},op));
        close(math.stream.frames[0].bins[125],std::conj(math.stream.frames[0].bins[3]));
    }
    mix.process(c);close(mix.stream.frames[0].bins[3],{2,-1});
    factor=0;mix.process(c);close(mix.stream.frames[0].bins[3],a.frames[0].bins[3]);factor=1;mix.process(c);close(mix.stream.frames[0].bins[3],b.frames[0].bins[3]);
    mix.equalPower=true;factor=.5;mix.process(c);close(mix.stream.frames[0].bins[3],std::complex<double>(4,-2)/std::sqrt(2.));
    legacy.process(c);close(legacy.stream.frames[0].bins[3],{11,-8});
    resources[1]=nullptr;math.operation=Op::add;math.process(c);close(math.stream.frames[0].bins[3],{1,2});math.operation=Op::multiply;math.process(c);close(math.stream.frames[0].bins[3],{});
    resources[1]=&b;
    // Reject incompatible batches in full, including a late mismatching frame.
    b.frames[1].start++;math.process(c);mix.process(c);assert(!math.stream.size&&!mix.stream.size);b.frames[1].start--;
    b.hop++;math.process(c);assert(!math.stream.size);b.hop--;
    b.count--;mix.process(c);assert(!mix.stream.size);b.count++;
    b.sampleRate++;mix.process(c);assert(!mix.stream.size);b.sampleRate--;
    b.size=64;math.process(c);assert(!math.stream.size);b.size=128;
    b.window=SpectrumWindow::hamming;math.process(c);assert(!math.stream.size);b.window=a.window;
    factor=std::numeric_limits<float>::quiet_NaN();mix.process(c);close(mix.stream.frames[0].bins[3],{1,2});
    resources[0]=resources[1]=nullptr;mix.process(c);assert(!mix.stream.size&&!mix.stream.count);
    watching=false;assert(!allocations);
    std::cout<<"Spectrum arithmetic, crossfade, legacy plugins, frame alignment and allocation checks passed\n";
}
