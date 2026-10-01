#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct MultipleInputNode : BakedInput {
    void configure(std::istream& in)override{auto p=parameters::MultipleInputNodeParameters::read(in);port=p.slot_value;sources.clear();}
};
EASYKNACK_NODE(MultipleInputNode,"multipleinput")
}
