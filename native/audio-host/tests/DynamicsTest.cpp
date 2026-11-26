#include "nodes/AllpassNode.hpp"
#include "nodes/DelayAllpassNode.hpp"
#include "nodes/EnvelopeFollowerNode.hpp"
#include "nodes/DynamicsCurveNode.hpp"
#include "nodes/CompressorNode.hpp"
#include "nodes/LimiterNode.hpp"
#include <cassert>
#include <cstdio>
#include <cstdlib>
#include <new>
static bool counting=false;static std::size_t allocations=0;
void* operator new(std::size_t n){if(counting)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void* operator new[](std::size_t n){return ::operator new(n);}void operator delete(void* p)noexcept{std::free(p);}void operator delete[](void* p)noexcept{std::free(p);}
void operator delete(void* p,std::size_t)noexcept{std::free(p);}void operator delete[](void* p,std::size_t)noexcept{std::free(p);}
using namespace easyknack;
struct Block {
 std::array<float,256> left{},right{},output{},outputRight{},silence{},modulation{};
 const float* inputs[10]{left.data()},*rights[10]{right.data()};char rates[10]{'a','c','c','c','c','c','c','c','c','c'};
 void run(NodeProcessor& node,std::size_t frames=256,NodeProcessor* other=nullptr,double fs=48000){
  ProcessContext c{inputs,10,output.data(),frames,fs,silence.data(),frames};c.rightInputs=rights;c.sourceRates=rates;
  counting=true;node.process(c);if(other){c.output=outputRight.data();c.channelIndex=1;other->process(c);}counting=false;assert(allocations==0);
 }
};
int main(){
 for(int order:{1,2})for(double hz:{80.,1000.,12000.}){
  AllpassNode n;n.order=order;Block b;double inputEnergy=0,outputEnergy=0;
  for(int block=0;block<160;block++){
   for(int i=0;i<256;i++)b.left[i]=std::sin(6.283185307179586*hz*(block*256+i)/48000);
   b.run(n);if(block>80)for(int i=0;i<256;i++){inputEnergy+=double(b.left[i])*b.left[i];outputEnergy+=double(b.output[i])*b.output[i];}
  }
  assert(std::abs(outputEnergy/inputEnergy-1)<.002);
 }
 {DelayAllpassNode n;n.time=17./48000;n.prepare(48000,256);Block b;double energy=0;b.left[0]=1;
  b.run(n);assert(std::abs(b.output[0]+.5)<1e-7);assert(std::abs(b.output[17]-.75)<1e-7);assert(std::abs(b.output[34]-.375)<1e-7);
  for(float x:b.output)energy+=double(x)*x;b.left.fill(0);
  for(int block=0;block<30;block++){b.run(n);for(float x:b.output)energy+=double(x)*x;}assert(std::abs(energy-1)<1e-6);
  DelayAllpassNode replacement;replacement.prepare(48000,256);replacement.inheritState(n);Block a;b.run(n);a.run(replacement);assert(a.output==b.output);
 }
 for(bool rms:{false,true}){
  EnvelopeFollowerNode n;n.attack=10;n.release=100;n.rms=rms;Block b;b.left.fill(1);
  b.run(n,256);b.run(n,224);double expected=1-std::exp(-1.);if(rms)expected=std::sqrt(expected);assert(std::abs(b.output[223]-expected)<1e-6);
  b.left.fill(0);for(int i=0;i<18;i++)b.run(n);b.run(n,192);double tail=(1-std::exp(-1.))*std::exp(-1.);if(rms)tail=std::sqrt(tail);assert(std::abs(b.output[191]-tail)<1e-6);
 }
 {DynamicsCurveNode n;n.knee=0;Block b;b.left.fill(std::pow(10.,-4./20));b.run(n);assert(std::abs(b.output[0]-std::pow(10.,-6./20))<1e-6);n.ratio=1;b.run(n);assert(b.output[0]==1);}
 {CompressorNode n,r;r.shareChannelState(n);n.prepare(48000,256);r.prepare(48000,256);n.values={-12,4,0,0,100,0};Block b;b.left.fill(.25);b.right.fill(1);b.run(n,256,&r);double gain=std::pow(10.,-9./20);assert(std::abs(b.output[0]-.25*gain)<1e-6);assert(std::abs(b.outputRight[0]-gain)<1e-6);
  n.link=false;b.run(n,256,&r);assert(b.output[0]>.08&&b.output[0]<.26);
  float side=1;b.inputs[1]=&side;b.rights[1]=&side;b.rates[1]='c';b.left.fill(.01);b.right.fill(.01);n.link=true;b.run(n,256,&r);assert(std::abs(b.output[0]-.01*gain)<1e-6);
  CompressorNode copy,copyRight;copyRight.shareChannelState(copy);copy.prepare(48000,256);copy.values=n.values;copy.inheritState(n);assert(copy.state->gain==n.state->gain);
  n.state->gain={-1e-300,-1e-300};b.inputs[1]=b.rights[1]=nullptr;b.left.fill(0);b.right.fill(0);b.run(n,256,&r);assert(n.state->gain[0]==0&&n.state->gain[1]==0);
 }
 for(double fs:{44100.,48000.,96000.})for(double ms:{0.,1.,5.,20.}){
  LimiterNode n,r;n.lookahead=ms;r.shareChannelState(n);n.prepare(fs,256);r.prepare(fs,256);n.ceiling=.5;n.release=30;Block b;
  std::size_t sample=0,delay=std::size_t(std::round(ms*fs*.001));bool heard=false;
  for(int block=0;block<80;block++){
   auto frames=block%3==0?1:block%3==1?17:256;
   for(int i=0;i<frames;i++){auto k=sample+i;b.left[i]=k==0?2.f:float(std::sin(k*.31)*3);b.right[i]=b.left[i]*.25f;}
   b.run(n,frames,&r,fs);
   for(int i=0;i<frames;i++){assert(std::abs(b.output[i])<=.500001);assert(std::abs(b.outputRight[i])<=.500001);assert(std::abs(b.outputRight[i]-.25*b.output[i])<1e-6);if(sample+i<delay)assert(b.output[i]==0);heard|=b.output[i]!=0;}
   sample+=frames;
  }assert(heard);
  LimiterNode copy,cr;copy.lookahead=ms;copy.ceiling=n.ceiling;copy.release=n.release;cr.shareChannelState(copy);copy.prepare(fs,256);copy.inheritState(n);Block a;b.left.fill(0);b.right.fill(0);b.run(n,256,&r,fs);a.run(copy,256,&cr,fs);assert(a.output==b.output);assert(a.outputRight==b.outputRight);
 }
 {LimiterNode n,r;r.shareChannelState(n);n.prepare(48000,256);Block b;b.left[0]=std::numeric_limits<float>::quiet_NaN();b.right[1]=std::numeric_limits<float>::infinity();b.run(n,256,&r);for(float x:b.output)assert(std::isfinite(x));}
 std::puts("Dynamics/allpass tests passed: unity allpass response, envelope timing, curve, stereo/sidechain, limiter ceiling/latency/state, zero callback allocations");
}
