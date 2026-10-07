#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
struct VectorCombineNode:VectorProcessor {
    Vector3 value{};
    void configure(std::istream& in)override{auto p=parameters::VectorCombineNodeParameters::read(in);sources=p.sources();value={p.x_value,p.y_value,p.z_value};}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{value=static_cast<const VectorCombineNode&>(other).value;}
    void process(ProcessContext& c)noexcept override{result.count=c.bufferSize;for(std::size_t i=0;i<c.bufferSize;i++){for(std::size_t axis=0;axis<3;axis++){double v=c.numericInput(axis,static_cast<float>(value[axis]))[i];result.samples[i][axis]=std::isfinite(v)?v:0;}c.output[i]=0;}}
};
EASYKNACK_NODE(VectorCombineNode,"vectorcombine")
}
