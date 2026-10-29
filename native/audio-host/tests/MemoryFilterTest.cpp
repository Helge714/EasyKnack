#include "AudioGraph.hpp"
#include <cassert>
using namespace easyknack;
static double level(int stages,int mode,double frequency,bool compensation){
    FilterNode node;node.stages=stages;node.mode=mode;node.mix={0,0,0};node.mix[mode]=1;node.compensate=compensation;node.prepare(48000,256);
    float input[256]{},output[256]{},zero[256]{};const float* inputs[]{input,nullptr,nullptr};ProcessContext c{inputs,3,output,256,48000,zero,256};
    double power=0;int count=0;
    for(int base=0;base<96000;base+=256){
        for(int i=0;i<256;i++)input[i]=float(.1*std::sin(6.283185307179586*frequency*(base+i)/48000.));
        node.process(c);for(int i=0;i<256;i++){assert(std::isfinite(output[i]));if(base>=48000){power+=output[i]*output[i];++count;}}
    }
    return std::sqrt(power/count);
}
static void filters(){
    assert(level(4,0,3000,false)<level(1,0,3000,false)*.02);
    assert(level(4,1,100,false)<level(1,1,100,false)*.002);
    assert(std::abs(level(4,0,100,false)-.1/std::sqrt(2.))<.001);
    double dry=level(1,0,1500,false),wet=level(1,0,1500,true);
    assert(wet>dry*1.8&&std::abs(wet-.1/std::sqrt(2.))<.001);
    assert(level(4,0,10000,true)<=level(4,0,10000,false)*4.001);
    FilterNode legacy,raw;std::istringstream old("!cd23044f7c58d242 -1 -1 1000 -1 0.707 lowpass"),plain("-1 -1 1000 -1 0.707 lowpass");legacy.configure(old);raw.configure(plain);
    float in=.2f,a=0,b=0;const float* inputs[]{&in,nullptr,nullptr};ProcessContext c{inputs,3,&a,1,48000,nullptr,1};
    legacy.prepare(48000,1);raw.prepare(48000,1);
    for(int i=0;i<500;i++){legacy.processSample(c);c.output=&b;raw.process(c);assert(a==b);c.output=&a;}
    FilterNode copy;copy.prepare(48000,1);copy.inheritState(raw);c.output=&a;copy.process(c);c.output=&b;raw.process(c);assert(a==b);
    FilterNode extreme;extreme.stages=4;extreme.resonance=30;extreme.compensate=true;extreme.prepare(48000,1);
    for(int i=0;i<20000;i++){extreme.cutoff=i%2?5:20000;extreme.mode=i%3;extreme.processSample(c);assert(std::isfinite(b));}
}
static void memory(){
    BinMemoryNode node;node.prepare(48000,256);SpectrumStream source;source.prepare(256);source.size=1024;source.identity=1;node.beginBlock(source);node.frame=&source.frames[0];
    float input=4,update=0,output=0;const float* inputs[]{&input,&update};ProcessContext c{inputs,2,&output,1,48000,nullptr,1};
    node.bin=2;node.process(c);assert(output==4);input=8;node.process(c);assert(output==4);
    update=.25;node.process(c);assert(output==5);node.process(c);assert(output==5.75);
    node.bin=3;input=100;node.process(c);assert(output==100);
    node.bin=2;update=0;node.process(c);assert(output==5.75);
    BinMemoryNode copy;copy.prepare(48000,256);copy.adoptState(node);copy.frame=node.frame;copy.bin=2;copy.process(c);assert(output==5.75);
    update=2;copy.process(c);assert(output==100);update=-1;input=0;copy.process(c);assert(output==100);
    source.identity=2;copy.beginBlock(source);copy.process(c);assert(output==0);
}
int main(){memory();filters();}
