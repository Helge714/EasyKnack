#pragma once
#include "../NodeProcessor.hpp"
#include "ParameterContracts.hpp"
namespace easyknack {
struct SpectrumRelayNode:NodeProcessor {
 DspResource* resource=nullptr;
 bool supportsControlRate()const noexcept override{return true;}
 void configure(std::istream& in)override{auto p=parameters::SpectrumRelayNodeParameters::read(in);sources=p.sources();}
 ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
 ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
 DspResource* resourceOutput(std::size_t)noexcept override{return resource;}
 void process(ProcessContext& c)noexcept override{resource=const_cast<DspResource*>(c.resource(0));}
};
EASYKNACK_NODE(SpectrumRelayNode,"spectrumrelay")
}
