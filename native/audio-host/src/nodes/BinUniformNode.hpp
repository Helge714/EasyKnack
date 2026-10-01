#pragma once
#include "BinContextNode.hpp"
namespace easyknack {
struct BinUniformNode : BinAccess {
    std::size_t slot=0;float held=0;
    bool canShareChannels()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::BinUniformNodeParameters::read(in);sources=p.sources();slot=p.slot_value;}
    void process(ProcessContext& c)noexcept override{c.output[0]=held;}
};
EASYKNACK_NODE(BinUniformNode,"binuniform")
}
