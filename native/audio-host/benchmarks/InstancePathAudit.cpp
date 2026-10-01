#include "AuditFixture.hpp"
#include <chrono>
#include <iostream>
#include <algorithm>
using namespace easyknack;
volatile float resultSink=0;
template<class Run> double timeBlock(Run run){
    for(int i=0;i<200;i++)run();
    std::vector<double> times;
    for(int batch=0;batch<9;batch++){
        auto start=std::chrono::steady_clock::now();
        for(int i=0;i<300;i++)run();
        times.push_back(std::chrono::duration<double,std::micro>(std::chrono::steady_clock::now()-start).count()/300);
    }
    std::sort(times.begin(),times.end());return times[4];
}
int main(){
    std::cout<<"inputs,instance_us,baked_us,complex_us,instance_fast,baked_fast\n";
    for(int mode=0;mode<5;mode++){
        MultipleGraphNode live;BakedInstance baked;
        std::istringstream lp(liveParameters),bp(bakedParameters);live.configure(lp);baked.configure(bp);
        live.prepare(48000,256);baked.prepare(48000,256);
        AdditiveOscillatorNode complex,rightComplex;
        for(auto* osc:{&complex,&rightComplex}){std::istringstream p("-1 sine 20 0 0 -1 330 -1 0 inputs -1 -1 -1");osc->configure(p);osc->prepare(48000,256);}
        std::array<float,256> frequency,falloff{},rightFrequency,out{},reference{},silence{};frequency.fill(330);rightFrequency.fill(330);
        float count=20;
        const float* inputs[9]{&count,frequency.data(),falloff.data()};
        const float* rightInputs[9]{&count,rightFrequency.data(),falloff.data()};
        char rates[9]{'c','c','c','c','c','c','c','c','c'};
        if(mode==1||mode>=3)rates[1]='a';if(mode>=2)rates[2]='a';
        ProcessContext c{inputs,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;
        if(mode==4)c.rightInputs=rightInputs;
        const float* complexInputs[6]{nullptr,frequency.data(),nullptr,nullptr,falloff.data(),nullptr};
        auto cc=c;cc.inputs=complexInputs;cc.inputCount=6;cc.sourceRates=nullptr;cc.rightInputs=nullptr;
        for(int block=0;block<30;block++)for(int channel=0;channel<2;channel++){
            c.channelIndex=channel;c.output=reference.data();live.process(c);c.output=out.data();baked.process(c);
            for(int i=0;i<256;i++)if(std::abs(out[i]-reference[i])>1e-5){std::cerr<<"Mismatch "<<mode<<"\n";return 1;}
            cc.channelIndex=0;(channel?rightComplex:complex).process(cc);
            for(int i=0;i<256;i++)if(std::abs(out[i]-reference[i])>1e-4){std::cerr<<"Complex mismatch "<<mode<<"\n";return 1;}
        }
        auto l=timeBlock([&]{c.channelIndex=0;live.process(c);c.channelIndex=1;live.process(c);resultSink=out[255];});
        auto b=timeBlock([&]{c.channelIndex=0;baked.process(c);c.channelIndex=1;baked.process(c);resultSink=out[255];});
        auto a=timeBlock([&]{complex.process(cc);if(mode==4)rightComplex.process(cc);resultSink=out[255];});
        const char* names[]{"control","audio-frequency","audio-falloff","audio-both","audio-stereo"};
        std::cout<<names[mode]<<","<<l<<","<<b<<","<<a<<","<<live.state->instances[0]->reductionReady()<<","<<baked.state->instances[0]->reductionReady()<<"\n";
    }
}
