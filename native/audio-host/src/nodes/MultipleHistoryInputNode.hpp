#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct MultipleHistoryInputNode:BakedInput {
 void configure(std::istream& in)override{auto p=parameters::MultipleHistoryInputNodeParameters::read(in);port=p.slot_value;resourceKind=ResourceKind::history;sources.clear();}
};
EASYKNACK_NODE(MultipleHistoryInputNode,"multiplehistoryinput")
}
