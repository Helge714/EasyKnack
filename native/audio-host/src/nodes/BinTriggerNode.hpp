#pragma once
#include "BinUniformNode.hpp"
namespace easyknack {
struct BinTriggerNode : BinUniformNode {
    BinTriggerNode(){trigger=true;}
    void configure(std::istream& in)override{auto p=parameters::BinTriggerNodeParameters::read(in);sources=p.sources();slot=p.slot_value;}
};
EASYKNACK_NODE(BinTriggerNode,"bintrigger")
}
