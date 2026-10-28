#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
namespace easyknack {
struct ValueNode : NodeProcessor {
    bool supportsSampleFeedback()const noexcept override{return true;}

    bool canShareChannels() const noexcept override { return true; }
    float value = 0,current=0;bool sliding=false;
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const ValueNode&>(configuration);
        value=p.value;sliding=true;
    }
    void configure(std::istream& in) override {
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::ValueNodeParameters::read(in);
        value=current=p.value_value;sliding=false;
        return;}
 value=current=readNumber(in);sliding=false; }
    void inheritState(const NodeProcessor& previous)noexcept override{
        auto& old=static_cast<const ValueNode&>(previous);
        if(value==old.value){current=old.current;sliding=old.sliding;}
    }
    bool supportsControlRate() const noexcept override { return true; }
    void process(ProcessContext& context) noexcept override {
        if(context.controls){auto values=context.numericInput(0,value);for(std::size_t i=0;i<context.bufferSize;i++)context.output[i]=values[i];current=context.output[context.bufferSize-1];sliding=false;return;}
        if(sliding){
            auto frames=context.blockFrames?context.blockFrames:context.bufferSize;
            current+=static_cast<float>(1-std::exp(-double(frames)/(.005*context.sampleRate)))*(value-current);
            if(std::abs(current-value)<1e-6f*std::max(1.f,std::abs(value))){current=value;sliding=false;}
        }else current=value;
        std::fill_n(context.output, context.bufferSize, current);
    }
};
EASYKNACK_NODE(ValueNode, "value")
}

