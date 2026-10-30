#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct MultipleSpectrumOutputNode:BakedOutput {
    void configure(std::istream& in)override{auto p=parameters::MultipleSpectrumOutputNodeParameters::read(in);sources=p.sources();resourceKind=ResourceKind::spectrum;}
};
EASYKNACK_NODE(MultipleSpectrumOutputNode,"multiplespectrumoutput")
}
