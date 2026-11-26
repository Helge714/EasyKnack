#include "nodes/FreeverbNode.hpp"
#include <cassert>
#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <new>

static bool counting=false;static std::size_t allocations=0;
void* operator new(std::size_t n){if(counting)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void* operator new[](std::size_t n){return ::operator new(n);}
void operator delete(void* p)noexcept{std::free(p);}void operator delete[](void* p)noexcept{std::free(p);}
void operator delete(void* p,std::size_t)noexcept{std::free(p);}void operator delete[](void* p,std::size_t)noexcept{std::free(p);}

// Deliberately separate scalar reference: conventional individual delay lines,
// matching the eight combs, damping, allpasses and gain laws in Reverb.knack.
struct Reference {
    struct Delay{std::vector<float> data;std::size_t pos=0;float lp=0;};
    std::array<std::array<Delay,8>,2> comb;std::array<std::array<Delay,4>,2> ap;
    explicit Reference(double rate){int cs[]={1116,1188,1277,1356,1422,1491,1557,1617},as[]={556,441,341,225};
        for(int ch=0;ch<2;ch++){for(int i=0;i<8;i++)comb[ch][i].data.resize(std::max(1,int((cs[i]+23*ch)*rate/44100)));
            for(int i=0;i<4;i++)ap[ch][i].data.resize(std::max(1,int((as[i]+23*ch)*rate/44100)));}}
    std::array<float,2> tick(float l,float r,const std::array<float,5>& p){
        float wet[2]{},feedback=.7f+.28f*p[0],damp=.4f*p[1],input=.015f*(l+r);
        for(int ch=0;ch<2;ch++){
            for(auto& line:comb[ch]){float y=line.data[line.pos];line.lp=y*(1-damp)+line.lp*damp;line.data[line.pos]=input+line.lp*feedback;line.pos=(line.pos+1)%line.data.size();wet[ch]+=y;}
            for(auto& line:ap[ch]){float y=line.data[line.pos];line.data[line.pos]=wet[ch]+.5f*y;line.pos=(line.pos+1)%line.data.size();wet[ch]=y-wet[ch];}
        }
        float w1=3*p[3]*(.5f+.5f*p[2]),w2=3*p[3]*(.5f-.5f*p[2]),dry=2*p[4];
        return {wet[0]*w1+wet[1]*w2+l*dry,wet[1]*w1+wet[0]*w2+r*dry};
    }
};
struct Engine {
    easyknack::FreeverbNode left,right;
    std::array<float,256> inL{},inR{},outL{},outR{},silence{};
    const float* inputs[6]{inL.data()},*rightInputs[6]{inR.data()};
    char rates[6]{'a','c','c','c','c','c'};
    double rate;
    explicit Engine(double sampleRate):rate(sampleRate){right.shareChannelState(left);left.prepare(rate,256);right.prepare(rate,256);}
    void process(std::size_t frames){
        easyknack::ProcessContext c{inputs,6,outL.data(),frames,rate,silence.data(),frames};c.rightInputs=rightInputs;c.sourceRates=rates;
        left.process(c);c.output=outR.data();c.channelIndex=1;right.process(c);
    }
};
int main(){
    for(double rate:{44100.,48000.,96000.}){
        Engine e(rate);Reference reference(rate);std::size_t sample=0;
        for(int block=0;block<1600;block++){
            std::size_t frames=block%3==0?1:block%3==1?17:256;
            for(std::size_t i=0;i<frames;i++){e.inL[i]=sample+i==0?1.f:sample+i<4000?.01f*std::sin(float(sample+i)*.03f):0.f;e.inR[i]=sample+i==0?-.2f:0.f;}
            counting=true;e.process(frames);counting=false;assert(allocations==0);
            for(std::size_t i=0;i<frames;i++){auto expected=reference.tick(e.inL[i],e.inR[i],e.left.values);assert(std::abs(e.outL[i]-expected[0])<1e-5f);assert(std::abs(e.outR[i]-expected[1])<1e-5f);}sample+=frames;
        }
        assert(e.left.state==e.right.state);assert(e.left.state->memory.size()*sizeof(float)<rate*3);
    }
    {Engine e(48000);e.left.values={.5f,.5f,1,0,.5f};for(int i=0;i<256;i++){e.inL[i]=.2f*std::sin(i*.1f);e.inR[i]=-.3f*std::cos(i*.11f);}e.process(256);
        for(int i=0;i<256;i++){assert(e.outL[i]==e.inL[i]);assert(e.outR[i]==e.inR[i]);}}
    {Engine e(48000);e.left.values={.5f,.5f,0,1.f/3,0};e.inL[0]=1;bool tail=false;
        for(int b=0;b<60;b++){e.process(256);for(int i=0;i<256;i++){assert(e.outL[i]==e.outR[i]);tail|=e.outL[i]!=0;}e.inL.fill(0);}assert(tail);}
    {Engine original(48000),expected(48000),replacement(48000);original.inL[0]=1;
        for(int b=0;b<40;b++){original.process(256);original.inL.fill(0);}
        expected.left.inheritState(original.left);
        counting=true;replacement.left.adoptState(original.left);replacement.right.adoptState(original.right);counting=false;assert(allocations==0);
        for(int b=0;b<20;b++){expected.process(256);replacement.process(256);assert(expected.outL==replacement.outL);assert(expected.outR==replacement.outR);}}
    {Engine e(48000);Reference reference(48000);std::array<float,256> room{},wet{};float dry=.25f;
        e.inputs[1]=room.data();e.inputs[4]=wet.data();e.inputs[5]=&dry;e.rates[1]='a';e.rates[4]='a';
        for(int b=0;b<60;b++){
            for(int i=0;i<256;i++){room[i]=float(i)/255;wet[i]=float(255-i)/255;e.inL[i]=b==0&&i==0?1.f:0.f;}
            counting=true;e.process(256);counting=false;assert(allocations==0);
            for(int i=0;i<256;i++){auto p=e.left.values;p[0]=room[i];p[3]=wet[i];p[4]=dry;auto expected=reference.tick(e.inL[i],0,p);
                assert(std::abs(e.outL[i]-expected[0])<1e-5f);assert(std::abs(e.outR[i]-expected[1])<1e-5f);}
        }}
    {Engine e(48000);easyknack::FreeverbNode configuration;configuration.values={1,1,1,1,1};e.left.applyParameters(configuration);
        e.inL[0]=std::numeric_limits<float>::quiet_NaN();e.inR[1]=std::numeric_limits<float>::infinity();
        for(int b=0;b<30;b++){e.process(256);for(float y:e.outL)assert(std::isfinite(y));for(float y:e.outR)assert(std::isfinite(y));e.inL.fill(0);e.inR.fill(0);}}
    {Engine e(48000);for(int i=0;i<256;i++)e.inL[i]=.05f*std::sin(i*.1f);
        for(int b=0;b<200;b++)e.process(256);auto start=std::chrono::steady_clock::now();
        for(int b=0;b<4000;b++)e.process(256);
        double ns=std::chrono::duration<double,std::nano>(std::chrono::steady_clock::now()-start).count()/(4000*256);
        std::printf("Freeverb: %.1f ns/stereo-sample, %.3f %% of 48 kHz budget, %zu delay bytes, zero callback allocations\n",ns,ns*48000/1e7,e.left.state->memory.size()*sizeof(float));
    }
}
