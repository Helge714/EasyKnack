#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct MultipleVectorInputNode:BakedInput {
 void configure(std::istream& in)override{auto p=parameters::MultipleVectorInputNodeParameters::read(in);port=p.slot_value;resourceKind=ResourceKind::vector;sources.clear();}
};
EASYKNACK_NODE(MultipleVectorInputNode,"multiplevectorinput")
}
