#pragma once
#include "ParameterContracts.hpp"
namespace easyknack {
// Preserve source exports made before variable-length source lists.
inline parameters::MultipleGraphNodeParameters readMultipleGroupParameters(std::istream& in){
    auto begin=in.tellg();std::string marker;in>>marker;
    if(marker!="!be6c5b3b1b5d2183"){in.seekg(begin);return parameters::MultipleGraphNodeParameters::read(in);}
    parameters::MultipleGraphNodeParameters p;
    if(!(in>>p.count_source>>p.count_value>>p.maximum_value>>p.mode_value)||p.count_source< -1||p.count_value<0||p.count_value>256||p.maximum_value<1||p.maximum_value>256||(p.mode_value!="sum"&&p.mode_value!="mean"))throw std::runtime_error("Invalid legacy instance parameters");
    for(unsigned i=0;i<8;i++){int source;if(!(in>>source)||source< -1)throw std::runtime_error("Invalid legacy instance source");p.inputs_sources.push_back(source);}
    if(!(in>>p.program_value))throw std::runtime_error("Missing legacy instance program");return p;
}
inline parameters::SpectralGraphNodeParameters readSpectralGroupParameters(std::istream& in){
    auto begin=in.tellg();std::string marker;in>>marker;
    if(marker!="!779c8958a30145d1"){in.seekg(begin);return parameters::SpectralGraphNodeParameters::read(in);}
    parameters::SpectralGraphNodeParameters p;
    if(!(in>>p.spectrum_source)||p.spectrum_source< -1)throw std::runtime_error("Invalid legacy spectral source");
    for(unsigned i=0;i<16;i++){int source;if(!(in>>source)||source< -1)throw std::runtime_error("Invalid legacy frame source");p.inputs_sources.push_back(source);}
    if(!(in>>p.program_value))throw std::runtime_error("Missing legacy spectral program");return p;
}
}
