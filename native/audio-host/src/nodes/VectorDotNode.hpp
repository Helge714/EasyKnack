#pragma once
#include "ParameterContracts.hpp"
#include "VectorDistanceNode.hpp"
namespace easyknack {
struct VectorDotNode:VectorDistanceNode {
    void configure(std::istream& in)override{auto p=parameters::VectorDotNodeParameters::read(in);sources=p.sources();a={vectorParameter(p.a_x_value),vectorParameter(p.a_y_value),vectorParameter(p.a_z_value)};b={vectorParameter(p.b_x_value),vectorParameter(p.b_y_value),vectorParameter(p.b_z_value)};}
    void process(ProcessContext& c)noexcept override{for(std::size_t i=0;i<c.bufferSize;i++){auto x=vectorInput(c,0,i,a),y=vectorInput(c,1,i,b);c.output[i]=static_cast<float>(x[0]*y[0]+x[1]*y[1]+x[2]*y[2]);}}
};
EASYKNACK_NODE(VectorDotNode,"vectordot")
}
