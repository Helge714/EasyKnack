#pragma once
#include "../NodeProcessor.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
// Inside an instance this emits weighted signal and weight. MultipleGraph performs the reduction.
struct InstanceCombineNode : NodeProcessor {
    float value=0,weight=1;std::string mode="mean";
    bool isSink()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 2;}
    void configure(std::istream& in)override{auto p=parameters::InstanceCombineNodeParameters::read(in);sources=p.sources();value=p.in_value;weight=p.weight_value;mode=p.mode_value;}
    void process(ProcessContext& c)noexcept override{
        auto signal=c.numericInput(0,value),weights=c.numericInput(1,weight);
        for(std::size_t i=0;i<c.bufferSize;i++){
            float w=weights[i];w=std::isfinite(w)?std::max(0.f,w):0.f;
            if(auto* out=c.outputAt(0))out[i]=signal[i]*w;
            if(auto* out=c.outputAt(1))out[i]=w;
        }
    }
};
EASYKNACK_NODE(InstanceCombineNode,"instancecombine")
}
