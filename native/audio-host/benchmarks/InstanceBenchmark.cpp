#include "Fixture.hpp"
#include "nodes/AdditiveOscillatorNode.hpp"
#include <chrono>
#include <iostream>
#include <iomanip>
#include <algorithm>
using namespace easyknack;
volatile float checksum=0;
template<class F> void measure(const char* name,F run){
    for(int i=0;i<200;i++)run();
    std::vector<double> batches;
    for(int batch=0;batch<9;batch++){
        auto start=std::chrono::steady_clock::now();for(int block=0;block<300;block++)run();
        batches.push_back(std::chrono::duration<double,std::micro>(std::chrono::steady_clock::now()-start).count()/300);
    }
    std::sort(batches.begin(),batches.end());std::cout<<name<<","<<batches[4]<<","<<batches[4]/(256./48000*1e6)*100<<"\n";
}
int main(int argc,char** argv){
    MultipleGraphNode live;std::istringstream lp(liveParameters);live.configure(lp);live.prepare(48000,256);
    BakedInstance baked;std::istringstream bp(bakedParameters);baked.configure(bp);baked.prepare(48000,256);
    MultipleGraphNode scheduled;std::istringstream sp(liveParameters);scheduled.configure(sp);scheduled.prepare(48000,256);
    for(auto& instance:scheduled.state->instances)instance->reductionOsc=-1;
    const int count=argc>1?std::stoi(argv[1]):20;
    const std::string slope=argc>2?argv[2]:"1";
    AdditiveOscillatorNode complex;std::istringstream cp("-1 sine "+std::to_string(count)+" "+slope+" 0 -1 330 -1 0 inputs -1 -1 -1");complex.configure(cp);complex.prepare(48000,256);
    std::array<float,256> out{},silence{};const float* absent[9]{};char rates[9]{};std::fill_n(rates,9,'c');
    ProcessContext c{absent,9,out.data(),256,48000,silence.data(),256};c.sourceRates=rates;
    float requested=float(count);absent[0]=&requested;
    const float* complexAbsent[9]{};auto complexContext=c;complexContext.inputs=complexAbsent;complexContext.channelIndex=0;
    // Both paths must produce the same signal before comparing their costs.
    std::array<float,256> reference{};
    for(int block=0;block<100;block++)for(int channel=0;channel<2;channel++){
        c.channelIndex=channel;c.output=reference.data();live.process(c);c.output=out.data();baked.process(c);
        for(int sample=0;sample<256;sample++)if(std::abs(reference[sample]-out[sample])>1e-5f){std::cerr<<"Live/baked mismatch at "<<block<<":"<<sample<<"\n";return 1;}
        scheduled.process(c);
        for(int sample=0;sample<256;sample++)if(std::abs(reference[sample]-out[sample])>1e-5f){std::cerr<<"Fused/scheduled mismatch at "<<block<<":"<<sample<<"\n";return 1;}
        if(channel==0&&argc>2){complex.process(complexContext);
            for(int sample=0;sample<256;sample++)if(std::abs(reference[sample]-out[sample])>1e-4f){std::cerr<<"Instance/Complex mismatch at "<<block<<":"<<sample<<" "<<reference[sample]<<" vs "<<out[sample]<<"\n";return 1;}}
    }
    std::cout<<"case,us_per_block,deadline_percent\n";
    auto runComplex=[&]{measure("complex",[&]{complex.process(complexContext);checksum=out[255];});};
    auto runScheduled=[&]{measure("scheduled",[&]{c.channelIndex=0;scheduled.process(c);c.channelIndex=1;scheduled.process(c);checksum=out[255];});};
    auto runLive=[&]{measure("instance",[&]{c.channelIndex=0;live.process(c);c.channelIndex=1;live.process(c);checksum=out[255];});};
    auto runBaked=[&]{measure("baked",[&]{c.channelIndex=0;baked.process(c);c.channelIndex=1;baked.process(c);checksum=out[255];});};
    if(argc>3&&std::string(argv[3])=="reverse"){runBaked();runLive();runScheduled();runComplex();}
    else{runComplex();runScheduled();runLive();runBaked();}
    for(auto& n:live.state->instances[0]->runtime->graph.nodes)std::cerr<<n.id<<" rates="<<n.outputRates<<" inputs="<<n.inputRates<<" stereo="<<n.stereo<<" active="<<n.active<<"\n";
}
