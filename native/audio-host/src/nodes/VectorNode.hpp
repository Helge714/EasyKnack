#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
struct VectorNode:VectorProcessor {
    Vector3 value{};
    void configure(std::istream& in)override{auto p=parameters::VectorNodeParameters::read(in);value={vectorParameter(p.x_value),vectorParameter(p.y_value),vectorParameter(p.z_value)};}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{value=static_cast<const VectorNode&>(other).value;}
    void process(ProcessContext& c)noexcept override{result.count=1;result.samples[0]=value;c.output[0]=0;}
};
EASYKNACK_NODE(VectorNode,"vector")
}
