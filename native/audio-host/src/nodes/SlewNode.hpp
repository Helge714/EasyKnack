#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct SlewNode : NodeProcessor {
    float input=0,rise=.03f,fall=.03f;double current=0;bool initialized=false;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::SlewNodeParameters::read(in);sources=p.sources();input=p.in_value;rise=p.rise_value;fall=p.fall_value;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const SlewNode&>(other);input=p.input;rise=p.rise;fall=p.fall;}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const SlewNode&>(other);current=p.current;initialized=p.initialized;}
    void process(ProcessContext& c)noexcept override{
        auto x=c.numericInput(0,input),up=c.numericInput(1,rise),down=c.numericInput(2,fall);
        double dt=(c.bufferSize==1&&c.blockFrames?double(c.blockFrames):1.0)/std::max(1.0,c.sampleRate);
        double lastTime=-1,coefficient=1;
        for(std::size_t i=0;i<c.bufferSize;i++){
            double target=x[i];
            if(std::isfinite(target)){
                if(!initialized){current=target;initialized=true;}
                double time=target>current?up[i]:down[i];
                time=std::isfinite(time)?std::clamp(time,0.0,60.0):0;
                if(time!=lastTime){coefficient=time>0?-std::expm1(-dt/time):1;lastTime=time;}
                current+=(target-current)*coefficient;
            }
            c.output[i]=static_cast<float>(current);
        }
    }
};
EASYKNACK_NODE(SlewNode,"slew")
}
