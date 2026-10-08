#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct MultipleSpectrumInputNode:BakedInput {
 void configure(std::istream& in)override{auto p=parameters::MultipleSpectrumInputNodeParameters::read(in);port=p.slot_value;resourceKind=ResourceKind::spectrum;sources.clear();}
};
EASYKNACK_NODE(MultipleSpectrumInputNode,"multiplespectruminput")
}
