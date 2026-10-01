#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct BinDiscardNode : NodeProcessor {
    float value=0;bool discarded=false;
    bool supportsScalarInputs()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::BinDiscardNodeParameters::read(in);sources=p.sources();value=p.discard_value;}
    void process(ProcessContext& c)noexcept override{auto v=c.numericInput(0,value)[0];discarded=!std::isfinite(v)||v!=0;}
};
EASYKNACK_NODE(BinDiscardNode,"bindiscard")
}
