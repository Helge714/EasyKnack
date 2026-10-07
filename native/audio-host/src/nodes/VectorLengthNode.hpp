#pragma once
#include "ParameterContracts.hpp"
#include "VectorSeparateNode.hpp"
namespace easyknack {
struct VectorLengthNode:VectorSeparateNode {
    std::size_t outputCount()const noexcept override{return 1;}
    void configure(std::istream& in)override{auto p=parameters::VectorLengthNodeParameters::read(in);sources=p.sources();value={vectorParameter(p.vector_x_value),vectorParameter(p.vector_y_value),vectorParameter(p.vector_z_value)};}
    void process(ProcessContext& c)noexcept override{for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=static_cast<float>(length(vectorInput(c,0,i,value)));}
};
EASYKNACK_NODE(VectorLengthNode,"vectorlength")
}
