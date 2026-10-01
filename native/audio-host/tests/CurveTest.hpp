#pragma once
#include "../src/nodes/CurveNode.hpp"
#include "../src/nodes/EnvelopeNode.hpp"

namespace curve_tests {
using namespace easyknack;
inline void require(bool value){if(!value)throw std::runtime_error("Curve/envelope test failed");}
inline void run(){
    const std::string identity="2 0 0 0 0 0 0 0 1 1 1 1 1 1 0";
    CurveNode mapping;std::istringstream parameters("-1 0 -1 1 -2 2 0 "+identity);mapping.configure(parameters);
    std::array<float,8> input{-2,-1,-.5f,0,.5f,1,2,3},output{},silence{};const float* inputs[]={input.data()};
    ProcessContext c{inputs,1,output.data(),8,48000,silence.data()};mapping.process(c);
    require(output==std::array<float,8>{-2,-2,-1,0,1,2,2,2});
    std::istringstream extended("-1 0 -1 1 -2 2 1 "+identity);mapping.configure(extended);mapping.process(c);require(output[0]==-4&&output[7]==6);
    std::istringstream reversed("-1 0 1 -1 0 1 0 "+identity);mapping.configure(reversed);mapping.process(c);require(output[1]==1&&output[5]==0);
    std::istringstream degenerate("-1 0 1 1 -2 2 0 "+identity);mapping.configure(degenerate);mapping.process(c);require(output[7]==-2);
    FunctionCurve bezier;std::istringstream shape("2 0 0 0 0 0.3333333333333333 0 2 1 1 0.6666666666666666 0 1 1 0");bezier.read(shape);
    for(int i=0;i<=1000;i++){double x=i/1000.0;require(std::abs(bezier.sample(x)-x*x*x)<.00002);}
    FunctionCurve step;std::istringstream stairs("3 0 0 0 0 0 0 1 0.5 1 0.5 1 0.5 1 1 1 0 1 0 1 0 0");step.read(stairs);
    require(step.sample(.499999)==0&&step.sample(.5)==1&&step.sample(.999999)==1&&step.sample(1)==0);
    bool rejected=false;try{std::istringstream bad("2 0 0 0 0 .9 0 2 1 1 .1 1 1 1 0");step.read(bad);}catch(...){rejected=true;}require(rejected);
    const std::string adsr="4 0 0 0 0 0 0 0 .2 1 .2 1 .2 1 0 .5 .65 .5 .65 .5 .65 0 1 0 1 0 1 0 0";
    EnvelopeNode env;std::istringstream envParams("-1 0 -1 0 10 10 .5 "+adsr);env.configure(envParams);
    std::array<float,64> gates{},triggers{},result{},zero{};const float* signals[]={gates.data(),triggers.data()};
    ProcessContext ec{signals,2,result.data(),64,1000,zero.data()};
    for(int i=3;i<20;i++)gates[i]=1;env.process(ec);
    require(result[0]==0&&result[2]==0&&result[3]==0&&result[4]>0&&std::abs(result[13]-.65)<1e-6);
    require(std::abs(result[20]-result[19])<1e-6&&result[21]<result[20]&&result[30]==0&&result[63]==0);
    // Early note-off starts from the actual attack level; no jump to the sustain level.
    EnvelopeNode early;std::istringstream ep("-1 0 -1 0 10 10 .5 "+adsr);early.configure(ep);
    gates.fill(0);gates[0]=gates[1]=1;early.process(ec);require(result[1]>0&&result[2]==result[1]&&result[3]<result[2]);
    // Trigger alone runs the envelope once and does not require a whole control block.
    gates.fill(0);triggers[7]=1;early.process(ec);require(result[6]==0&&result[8]>0&&result[63]==0);
    // Graph updates preserve held-gate and envelope progress rather than retriggering.
    gates.fill(1);triggers.fill(0);ec.bufferSize=4;early.process(ec);
    EnvelopeNode updated;std::istringstream up("-1 0 -1 0 10 10 .5 "+adsr);updated.configure(up);updated.inheritState(early);
    std::array<float,64> expected{};early.process(ec);expected=result;updated.process(ec);require(result==expected);
    // Repeated retriggers at a high level never overshoot the normalized 0..1 range.
    ec.bufferSize=64;for(int i=0;i<64;i++)triggers[i]=i%3==0?1.f:0.f;
    updated.process(ec);for(float v:result)require(v>=0&&v<=1);
}
}
