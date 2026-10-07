#include "nodes/VectorNode.hpp"
#include "nodes/VectorCombineNode.hpp"
#include "nodes/VectorSeparateNode.hpp"
#include "nodes/VectorMathNode.hpp"
#include "nodes/VectorDistanceNode.hpp"
#include "nodes/VectorLengthNode.hpp"
#include "nodes/VectorDotNode.hpp"
#include "nodes/SpatialReceiverNode.hpp"
#include "nodes/UnitConverterNode.hpp"
#include <cassert>
using namespace easyknack;
int main(){
    float x[]{1,2,3,4},y[]{2,999,999,999},zero[4]{},out[4]{},second[4]{},third[4]{};float* outputs[]{out,second,third};
    const float* inputs[]{x,y,nullptr,nullptr,nullptr};char rates[]{'a','c','c','c','c'};
    ProcessContext c{inputs,5,out,4,48000,zero,4};c.outputs=outputs;c.outputCount=3;c.sourceRates=rates;
    VectorCombineNode combine;combine.prepare(48000,4);combine.process(c);assert(combine.result.at(3)[0]==4&&combine.result.at(3)[1]==2);
    const DspResource* resources[]{&combine.result,nullptr,nullptr,nullptr,nullptr};c.resourceInputs=resources;
    VectorSeparateNode separate;separate.process(c);assert(out[3]==4&&second[3]==2&&third[3]==0);
    rates[0]='c';separate.process(c);assert(out[3]==1);rates[0]='a';
    VectorDistanceNode distance;distance.process(c);assert(std::abs(out[3]-std::sqrt(20.f))<1e-5);
    VectorMathNode math;math.prepare(48000,4);math.operation=5;math.process(c);assert(std::abs(length(math.result.at(3))-1)<1e-12);
    resources[0]=nullptr;math.process(c);assert(length(math.result.at(0))==0); // Normalizing zero is defined.
    VectorDotNode dot;dot.a={1,2,3};dot.b={4,5,6};dot.process(c);assert(out[0]==32);
    assert(unitToBase(100,UnitFormat::centimetre)==1&&unitFromBase(1,UnitFormat::millimetre)==1000);
    SpatialReceiverNode left,right;right.shareChannelState(left);left.prepare(48000,4);right.prepare(48000,4);
    left.source=right.source={1,0,0};left.spacing=right.spacing=.2;left.attenuation=right.attenuation=false;
    const float* audio[]{x,nullptr,nullptr,nullptr,nullptr};c.inputs=audio;c.channelIndex=0;left.process(c);
    assert(out[0]==0&&std::abs(second[0]-1.1)<1e-6);c.channelIndex=1;right.process(c);assert(out[0]==1&&std::abs(second[0]-.9)<1e-6);
    left.stereo=false;c.channelIndex=0;left.process(c);assert(out[0]==1&&second[0]==1);
    const float* rightAudio[]{y,nullptr,nullptr,nullptr,nullptr};c.rightInputs=rightAudio;left.process(c);assert(out[0]==1.5f);
    c.rightInputs=nullptr;left.propagation=true;left.source={343.0/48000,0,0};left.delay.assign(left.delay.size(),0);left.write=0;left.process(c);assert(out[0]==0&&std::abs(out[1]-1)<1e-5);
    left.source={1e6,0,0};left.process(c);assert(out[0]==0); // Outside bounded travel-time storage.
}
