#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
struct VectorSeparateNode:NodeProcessor {
    Vector3 value{};
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 3;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::vector;}
    void configure(std::istream& in)override{auto p=parameters::VectorSeparateNodeParameters::read(in);sources=p.sources();value={vectorParameter(p.vector_x_value),vectorParameter(p.vector_y_value),vectorParameter(p.vector_z_value)};}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{value=static_cast<const VectorSeparateNode&>(other).value;}
    void process(ProcessContext& c)noexcept override{for(std::size_t i=0;i<c.bufferSize;i++){auto v=vectorInput(c,0,i,value);for(std::size_t axis=0;axis<3;axis++)if(auto* out=c.outputAt(axis))out[i]=static_cast<float>(v[axis]);}}
};
EASYKNACK_NODE(VectorSeparateNode,"vectorseparate")
}
