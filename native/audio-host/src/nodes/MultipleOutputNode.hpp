#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct MultipleOutputNode : BakedOutput {
    void configure(std::istream& in)override{auto p=parameters::MultipleOutputNodeParameters::read(in);sources=p.sources();}
};
EASYKNACK_NODE(MultipleOutputNode,"multipleoutput")
}
