#pragma once
// Compatibility for saved generated graphs before oscillator Reset inputs.
#include <istream>
#include <string>
#include <vector>
#include <cmath>
#include <stdexcept>
namespace easyknack { namespace parameters {
struct LegacyOscillatorNodeParameters {
    int frequency_source{};
    double frequency_value{};
    std::string waveform_value{};
    int waveform_source{};
    int phase_source{};
    double phase_value{};
    std::vector<int> sources() const { return {frequency_source,waveform_source,phase_source}; }
    static LegacyOscillatorNodeParameters read(std::istream& in) {
        LegacyOscillatorNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!e5d6fa7b1b0ad545") throw std::runtime_error("Incompatible parameter contract: osc; rebuild Java and audio host together");
        if(!(in>>p.frequency_source)) throw std::runtime_error("Missing parameter: frequency");
        if(p.frequency_source < -1 || p.frequency_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.frequency_value)) throw std::runtime_error("Missing parameter: frequency");
        if(!std::isfinite(p.frequency_value)) throw std::runtime_error("Non-finite parameter");
        if(p.frequency_value<1.0E-5) throw std::runtime_error("Parameter below minimum");
        if(p.frequency_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>marker)||marker!="wave") throw std::runtime_error("Invalid parameter marker");
        if(!(in>>p.waveform_value)) throw std::runtime_error("Missing parameter: waveform");
        if(p.waveform_value!="sine" && p.waveform_value!="triangle" && p.waveform_value!="saw" && p.waveform_value!="square") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.waveform_source)) throw std::runtime_error("Missing parameter: waveform");
        if(p.waveform_source < -1 || p.waveform_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.phase_source)) throw std::runtime_error("Missing parameter: phase");
        if(p.phase_source < -1 || p.phase_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.phase_value)) throw std::runtime_error("Missing parameter: phase");
        if(!std::isfinite(p.phase_value)) throw std::runtime_error("Non-finite parameter");
        return p;
    }
};
struct LegacyAdditiveOscillatorNodeParameters {
    int waveform_source{};
    std::string waveform_value{};
    int harmonics_value{};
    double falloff_value{};
    double spread_value{};
    int frequency_source{};
    double frequency_value{};
    int phase_source{};
    double phase_value{};
    int harmonics_source{};
    int falloff_source{};
    int spread_source{};
    std::vector<int> sources() const { return {waveform_source,frequency_source,phase_source,harmonics_source,falloff_source,spread_source}; }
    static LegacyAdditiveOscillatorNodeParameters read(std::istream& in) {
        LegacyAdditiveOscillatorNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!51e01ae3a60f12dc") throw std::runtime_error("Incompatible parameter contract: additive; rebuild Java and audio host together");
        if(!(in>>p.waveform_source)) throw std::runtime_error("Missing parameter: waveform");
        if(p.waveform_source < -1 || p.waveform_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.waveform_value)) throw std::runtime_error("Missing parameter: waveform");
        if(p.waveform_value!="sine" && p.waveform_value!="triangle" && p.waveform_value!="saw" && p.waveform_value!="square") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.harmonics_value)) throw std::runtime_error("Missing parameter: harmonics");
        if(p.harmonics_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.harmonics_value>144.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.falloff_value)) throw std::runtime_error("Missing parameter: falloff");
        if(!std::isfinite(p.falloff_value)) throw std::runtime_error("Non-finite parameter");
        if(p.falloff_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.falloff_value>4.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.spread_value)) throw std::runtime_error("Missing parameter: spread");
        if(!std::isfinite(p.spread_value)) throw std::runtime_error("Non-finite parameter");
        if(p.spread_value<-2400.0) throw std::runtime_error("Parameter below minimum");
        if(p.spread_value>2400.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.frequency_source)) throw std::runtime_error("Missing parameter: frequency");
        if(p.frequency_source < -1 || p.frequency_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.frequency_value)) throw std::runtime_error("Missing parameter: frequency");
        if(!std::isfinite(p.frequency_value)) throw std::runtime_error("Non-finite parameter");
        if(p.frequency_value<1.0E-5) throw std::runtime_error("Parameter below minimum");
        if(p.frequency_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.phase_source)) throw std::runtime_error("Missing parameter: phase");
        if(p.phase_source < -1 || p.phase_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.phase_value)) throw std::runtime_error("Missing parameter: phase");
        if(!std::isfinite(p.phase_value)) throw std::runtime_error("Non-finite parameter");
        if(p.phase_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.phase_value>360.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>marker)||marker!="inputs") throw std::runtime_error("Invalid parameter marker");
        if(!(in>>p.harmonics_source)) throw std::runtime_error("Missing parameter: harmonics");
        if(p.harmonics_source < -1 || p.harmonics_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.falloff_source)) throw std::runtime_error("Missing parameter: falloff");
        if(p.falloff_source < -1 || p.falloff_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.spread_source)) throw std::runtime_error("Missing parameter: spread");
        if(p.spread_source < -1 || p.spread_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};

}}
