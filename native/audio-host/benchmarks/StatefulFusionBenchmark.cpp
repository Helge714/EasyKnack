// Offline prototype: no change to the production export planner.
#include "nodes/OscillatorNode.hpp"
#include "nodes/MathNode.hpp"
#include "nodes/FilterNode.hpp"
#include <chrono>
#include <cstring>
#include <fstream>
#include <iostream>
#include <sstream>
#include <memory>
using namespace easyknack;
struct Chain {
    OscillatorNode osc; MathNode gain; FilterNode filter;
    std::array<float,256> a{},b{},out{};
    Chain(){
        std::istringstream o("-1 440 wave sine -1"),m("0 0 -1 0.3 multiply 0"),f("1 -1 1000 -1 0.707 lowpass");
        osc.configure(o);gain.configure(m);filter.configure(f);
    }
};
// External names make the actual optimized machine code easy to inspect.
template<bool Inline> void block_kernel(Chain& x,int frames){
    ProcessContext o{nullptr,0,x.a.data(),std::size_t(frames),48000,nullptr};
    if constexpr(Inline){ [[clang::always_inline]] x.osc.render(x.a.data(),frames,x.osc.frequency,48000,nullptr,nullptr,true); }
    else x.osc.OscillatorNode::process(o);
    const float* mi[]={x.a.data(),nullptr};
    ProcessContext m{mi,2,x.b.data(),std::size_t(frames),48000,nullptr};
    if constexpr(Inline){
        const float smoothing=static_cast<float>(1-std::exp(-1.0/(.005*48000)));
        for(int i=0;i<frames;i++){
            if(frames==1){x.gain.currentA=x.gain.aValue;x.gain.currentB=x.gain.bValue;}
            else{x.gain.currentA+=smoothing*(x.gain.aValue-x.gain.currentA);x.gain.currentB+=smoothing*(x.gain.bValue-x.gain.currentB);}
            x.b[i]=MathNode::evaluate(x.a[i],x.gain.currentB,x.gain.operation,x.gain.clamp);
        }
    }
    else x.gain.MathNode::process(m);
    const float* fi[]={x.b.data(),nullptr,nullptr};
    ProcessContext f{fi,3,x.out.data(),std::size_t(frames),48000,nullptr};
    if constexpr(Inline){ [[clang::always_inline]] x.filter.FilterNode::process(f); }
    else x.filter.FilterNode::process(f);
}
extern "C" __attribute__((noinline)) void block_chain(Chain& x,int frames){block_kernel<false>(x,frames);}
extern "C" __attribute__((noinline)) void inline_blocks(Chain& x,int frames){block_kernel<true>(x,frames);}
template<bool Inline> void sample_chain(Chain& x,int frames){
    const float smoothing=static_cast<float>(1-std::exp(-1.0/(.005*48000)));
    for(int i=0;i<frames;i++){
        float oscillator;
        if constexpr(Inline){ [[clang::always_inline]] x.osc.render(&oscillator,1,x.osc.frequency,48000,nullptr,nullptr,true); }
        else x.osc.render(&oscillator,1,x.osc.frequency,48000,nullptr,nullptr,true);
        // Preserve MathNode's block-length-dependent smoothing (process(1) alone
        // would reset the gain every sample and would NOT be equivalent).
        if(frames==1){x.gain.currentA=x.gain.aValue;x.gain.currentB=x.gain.bValue;}
        else{x.gain.currentA+=smoothing*(x.gain.aValue-x.gain.currentA);x.gain.currentB+=smoothing*(x.gain.bValue-x.gain.currentB);}
        float value=MathNode::evaluate(oscillator,x.gain.currentB,x.gain.operation,x.gain.clamp);
        const float* input[]={&value,nullptr,nullptr};
        ProcessContext f{input,3,x.out.data()+i,1,48000,nullptr};
        if constexpr(Inline){ [[clang::always_inline]] x.filter.FilterNode::process(f); }
        else x.filter.FilterNode::process(f);
    }
}
extern "C" __attribute__((noinline)) void fused_chain(Chain& x,int frames){sample_chain<true>(x,frames);}
extern "C" __attribute__((noinline)) void sample_calls(Chain& x,int frames){sample_chain<false>(x,frames);}
void verify(){
    for(int frames:{1,17,128,256})for(unsigned wave=0;wave<4;wave++)for(int variant=0;variant<3;variant++){
        Chain a,b;a.osc.waveform=b.osc.waveform=wave;
        for(int block=0;block<180;block++){
            if(block%19==0){a.osc.frequency=b.osc.frequency=110+block*11;a.gain.bValue=b.gain.bValue=(block%5)*.17f;
                a.filter.cutoff=b.filter.cutoff=80+block*50;a.filter.resonance=b.filter.resonance=.2+block*.05;
                a.filter.mode=b.filter.mode=(block/19)%3;}
            block_chain(a,frames);if(variant==0)fused_chain(b,frames);else if(variant==1)sample_calls(b,frames);else inline_blocks(b,frames);
            if(std::memcmp(a.out.data(),b.out.data(),frames*sizeof(float))||a.osc.phase!=b.osc.phase||
               a.osc.currentFrequency!=b.osc.currentFrequency||a.gain.currentB!=b.gain.currentB||
               a.filter.ic1!=b.filter.ic1||a.filter.ic2!=b.filter.ic2||a.filter.mix!=b.filter.mix)
                throw std::runtime_error("Fusion changed samples or state");
        }
    }
    std::cout<<"48 changing-parameter waveform/block cases: bitwise equal samples and checked state\n";
}
volatile double checksum=0;
int main(int argc,char** argv){
    try{verify();std::ofstream csv(argc>1?argv[1]:"stateful-fusion.csv");
        csv<<"voices,frames,repeat,mode,mean_us,p99_us,max_us,checksum\n";
        for(int voices:{1,8,32})for(int frames:{128,256})for(int repeat=0;repeat<8;repeat++)for(int order=0;order<4;order++){
            int mode=(repeat+order)%4;std::vector<std::unique_ptr<Chain>> lanes;
            for(int v=0;v<voices;v++){lanes.push_back(std::make_unique<Chain>());lanes.back()->osc.frequency=220+v*7;}
            auto run=[&]{for(auto& lane:lanes){if(mode==1)fused_chain(*lane,frames);else if(mode==2)sample_calls(*lane,frames);else if(mode==3)inline_blocks(*lane,frames);else block_chain(*lane,frames);}};
            for(int i=0;i<100;i++)run();std::vector<double> times;double sum=0;
            for(int i=0;i<1000;i++){auto start=std::chrono::steady_clock::now();run();auto end=std::chrono::steady_clock::now();
                double us=std::chrono::duration<double,std::micro>(end-start).count();times.push_back(us);sum+=us;checksum+=lanes[0]->out[i%frames];}
            std::sort(times.begin(),times.end());csv<<voices<<','<<frames<<','<<repeat<<','<<(mode==1?"fused":mode==2?"sample-calls":mode==3?"inline-blocks":"blocks")<<','<<sum/1000<<','<<times[989]<<','<<times.back()<<','<<checksum<<'\n';
        }
    }catch(const std::exception& e){std::cerr<<e.what()<<'\n';return 1;}
}
