#include "AudioGraph.hpp"
#include "SimdMath.hpp"
#include <cassert>
#include <cstring>
#include <limits>
#include <iostream>
using namespace easyknack;
int main(){
#if defined(__SSE2__)
    const float values[]={0.f,-0.f,1.f,-1.f,1e-13f,1e-12f,-1e-12f,1e30f,-1e30f,
        std::numeric_limits<float>::infinity(),-std::numeric_limits<float>::infinity(),std::numeric_limits<float>::quiet_NaN()};
    for(auto op:{MathOperation::add,MathOperation::subtract,MathOperation::multiply,MathOperation::divide,
                 MathOperation::minimum,MathOperation::maximum,MathOperation::absolute,MathOperation::negate})for(bool clamp:{false,true})for(float a:values)for(float b:values){
        float output[4];simd::store(output,simd::evaluate(simd::splat(a),simd::splat(b),op,clamp));
        float expected=MathNode::evaluate(a,b,op,clamp);
        for(float actual:output)assert(std::memcmp(&actual,&expected,sizeof(float))==0);
    }
#endif
    AdditiveOscillatorNode spectrum;
    for(unsigned count:{1u,144u,5u,72u,1u})for(float slope:{1.f,0.f,2.3f,4.f})for(float exponent:{0.f,.5f,1.f,1.4f,2.f,4.f}){
        spectrum.updateSpectrum(slope,exponent,count);
        for(unsigned h=0;h<144;h++){
            double ratio=std::pow(h+1,static_cast<double>(exponent));
            double weight=h<count?std::pow(h+1,-slope):0;
            assert(std::abs(spectrum.ratios[h]-ratio)<=ratio*1e-12&&spectrum.weights[h]==weight);
        }
    }
    // Multiple outputs, fan-out, stereo, short buffers and changing oscillator state.
    const char* message="GRAPH 7 osc osc -1 440 @ a a split channelseparate 0 @ a a combine channelcombine 257 1 @ a aa split2 channelseparate 2 @ a a combine2 channelcombine 259 3 @ a aa out out 4 0 @ a a direct out 0 0 @ a a";
    auto pooled=AudioGraph::parse(message),reference=AudioGraph::parse(message);
    for(int index:reference.order){auto& node=reference.nodes[index];for(std::size_t v=0;v<node.voiceCount();v++)node.voice(v).allocateChannels(node.stereo);}
    assert(pooled.cacheBuffersAfter<pooled.cacheBuffersBefore);
    std::array<float,256> a{},b{};
    for(int block=0;block<100;block++)for(std::size_t ch=0;ch<2;ch++){
        int frames=block%3==0?1:block%3==1?17:256;
        pooled.process(a.data(),frames,ch,nullptr);reference.process(b.data(),frames,ch,nullptr);
        assert(std::memcmp(a.data(),b.data(),frames*sizeof(float))==0);
    }
    std::cout<<"Channel cache buffers: "<<pooled.cacheBuffersBefore<<" -> "<<pooled.cacheBuffersAfter<<" (1024 bytes each)\n";
}
