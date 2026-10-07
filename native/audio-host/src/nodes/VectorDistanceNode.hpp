#pragma once
#include "ParameterContracts.hpp"
#include "../VectorResource.hpp"
namespace easyknack {
struct VectorDistanceNode:NodeProcessor {
    Vector3 a{},b{};
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::vector;}
    void configure(std::istream& in)override{auto p=parameters::VectorDistanceNodeParameters::read(in);sources=p.sources();a={vectorParameter(p.a_x_value),vectorParameter(p.a_y_value),vectorParameter(p.a_z_value)};b={vectorParameter(p.b_x_value),vectorParameter(p.b_y_value),vectorParameter(p.b_z_value)};}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{const auto& v=static_cast<const VectorDistanceNode&>(other);a=v.a;b=v.b;}
    void process(ProcessContext& c)noexcept override{for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=static_cast<float>(length(subtract(vectorInput(c,0,i,a),vectorInput(c,1,i,b))));}
};
EASYKNACK_NODE(VectorDistanceNode,"vectordistance")
}
