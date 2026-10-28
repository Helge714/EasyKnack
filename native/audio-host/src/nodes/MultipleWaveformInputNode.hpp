#pragma once
#include "../BakedGraph.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct MultipleWaveformInputNode:BakedInput {
 void configure(std::istream& in)override{auto p=parameters::MultipleWaveformInputNodeParameters::read(in);port=p.slot_value;resourceKind=ResourceKind::waveform;sources.clear();}
};
EASYKNACK_NODE(MultipleWaveformInputNode,"multiplewaveforminput")
}
