// Generated from Java ParameterContract. Do not edit.
#pragma once
#include "FunctionCurve.hpp"
#include <string>
#include <vector>
#include <cmath>
namespace easyknack { namespace parameters {
struct AutocorrelationNodeParameters {
    int audio_in_source{};
    double minimum_value{};
    double maximum_value{};
    std::string channel_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static AutocorrelationNodeParameters read(std::istream& in) {
        AutocorrelationNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7f47dacb5c301937") throw std::runtime_error("Incompatible parameter contract: autocorrelation; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.minimum_value)) throw std::runtime_error("Missing parameter: minimum");
        if(!std::isfinite(p.minimum_value)) throw std::runtime_error("Non-finite parameter");
        if(p.minimum_value<30.0) throw std::runtime_error("Parameter below minimum");
        if(p.minimum_value>1000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.maximum_value)) throw std::runtime_error("Missing parameter: maximum");
        if(!std::isfinite(p.maximum_value)) throw std::runtime_error("Non-finite parameter");
        if(p.maximum_value<100.0) throw std::runtime_error("Parameter below minimum");
        if(p.maximum_value>10000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.channel_value)) throw std::runtime_error("Missing parameter: channel");
        if(p.channel_value!="left" && p.channel_value!="right") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct SpectrumCorrelationNodeParameters {
    int spectrum_source{};
    std::string normalization_value{};
    double smoothing_value{};
    std::vector<int> sources() const { return {spectrum_source}; }
    static SpectrumCorrelationNodeParameters read(std::istream& in) {
        SpectrumCorrelationNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!e106ab54634a4960") throw std::runtime_error("Incompatible parameter contract: spectrumcorrelation; rebuild Java and audio host together");
        if(!(in>>p.spectrum_source)) throw std::runtime_error("Missing parameter: spectrum");
        if(p.spectrum_source < -1 || p.spectrum_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.normalization_value)) throw std::runtime_error("Missing parameter: normalization");
        if(p.normalization_value!="biased" && p.normalization_value!="unbiased") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.smoothing_value)) throw std::runtime_error("Missing parameter: smoothing");
        if(!std::isfinite(p.smoothing_value)) throw std::runtime_error("Non-finite parameter");
        if(p.smoothing_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.smoothing_value>1.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct FFTAnalysisNodeParameters {
    int history_source{};
    double minimum_value{};
    double maximum_value{};
    std::vector<int> sources() const { return {history_source}; }
    static FFTAnalysisNodeParameters read(std::istream& in) {
        FFTAnalysisNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!1022082243bdad37") throw std::runtime_error("Incompatible parameter contract: fftanalysis; rebuild Java and audio host together");
        if(!(in>>p.history_source)) throw std::runtime_error("Missing parameter: history");
        if(p.history_source < -1 || p.history_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.minimum_value)) throw std::runtime_error("Missing parameter: minimum");
        if(!std::isfinite(p.minimum_value)) throw std::runtime_error("Non-finite parameter");
        if(p.minimum_value<20.0) throw std::runtime_error("Parameter below minimum");
        if(p.minimum_value>1000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.maximum_value)) throw std::runtime_error("Missing parameter: maximum");
        if(!std::isfinite(p.maximum_value)) throw std::runtime_error("Non-finite parameter");
        if(p.maximum_value<1001.0) throw std::runtime_error("Parameter below minimum");
        if(p.maximum_value>20000.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct OscilloscopeNodeParameters {
    int audio_in_source{};
    double low_value{};
    double high_value{};
    double window_value{};
    std::string channel_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static OscilloscopeNodeParameters read(std::istream& in) {
        OscilloscopeNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!b1e724b051579b12") throw std::runtime_error("Incompatible parameter contract: scope; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.low_value)) throw std::runtime_error("Missing parameter: low");
        if(!std::isfinite(p.low_value)) throw std::runtime_error("Non-finite parameter");
        if(p.low_value<-1.0) throw std::runtime_error("Parameter below minimum");
        if(p.low_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.high_value)) throw std::runtime_error("Missing parameter: high");
        if(!std::isfinite(p.high_value)) throw std::runtime_error("Non-finite parameter");
        if(p.high_value<-1.0) throw std::runtime_error("Parameter below minimum");
        if(p.high_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.window_value)) throw std::runtime_error("Missing parameter: window");
        if(!std::isfinite(p.window_value)) throw std::runtime_error("Non-finite parameter");
        if(p.window_value<6.0) throw std::runtime_error("Parameter below minimum");
        if(p.window_value>200.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.channel_value)) throw std::runtime_error("Missing parameter: channel");
        if(p.channel_value!="left" && p.channel_value!="right" && p.channel_value!="mix") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct PitchDetectionNodeParameters {
    int analysis_source{};
    double minimum_value{};
    double maximum_value{};
    std::vector<int> sources() const { return {analysis_source}; }
    static PitchDetectionNodeParameters read(std::istream& in) {
        PitchDetectionNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!d8f0b003ca90f629") throw std::runtime_error("Incompatible parameter contract: pitchdetection; rebuild Java and audio host together");
        if(!(in>>p.analysis_source)) throw std::runtime_error("Missing parameter: analysis");
        if(p.analysis_source < -1 || p.analysis_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.minimum_value)) throw std::runtime_error("Missing parameter: minimum");
        if(!std::isfinite(p.minimum_value)) throw std::runtime_error("Non-finite parameter");
        if(p.minimum_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.minimum_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.maximum_value)) throw std::runtime_error("Missing parameter: maximum");
        if(!std::isfinite(p.maximum_value)) throw std::runtime_error("Non-finite parameter");
        if(p.maximum_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.maximum_value>20000.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct PitchHoldNodeParameters {
    int frequency_source{};
    double frequency_value{};
    int confidence_source{};
    double confidence_value{};
    int threshold_source{};
    double threshold_value{};
    double initial_value{};
    std::vector<int> sources() const { return {frequency_source,confidence_source,threshold_source}; }
    static PitchHoldNodeParameters read(std::istream& in) {
        PitchHoldNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!4ac0270e6d628169") throw std::runtime_error("Incompatible parameter contract: pitchhold; rebuild Java and audio host together");
        if(!(in>>p.frequency_source)) throw std::runtime_error("Missing parameter: frequency");
        if(p.frequency_source < -1 || p.frequency_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.frequency_value)) throw std::runtime_error("Missing parameter: frequency");
        if(!std::isfinite(p.frequency_value)) throw std::runtime_error("Non-finite parameter");
        if(p.frequency_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.frequency_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.confidence_source)) throw std::runtime_error("Missing parameter: confidence");
        if(p.confidence_source < -1 || p.confidence_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.confidence_value)) throw std::runtime_error("Missing parameter: confidence");
        if(!std::isfinite(p.confidence_value)) throw std::runtime_error("Non-finite parameter");
        if(p.confidence_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.confidence_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.threshold_source)) throw std::runtime_error("Missing parameter: threshold");
        if(p.threshold_source < -1 || p.threshold_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.threshold_value)) throw std::runtime_error("Missing parameter: threshold");
        if(!std::isfinite(p.threshold_value)) throw std::runtime_error("Non-finite parameter");
        if(p.threshold_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.threshold_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.initial_value)) throw std::runtime_error("Missing parameter: initial");
        if(!std::isfinite(p.initial_value)) throw std::runtime_error("Non-finite parameter");
        if(p.initial_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.initial_value>20000.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct PitchMonitorNodeParameters {
    int frequency_source{};
    int target_source{};
    int confidence_source{};
    std::vector<int> sources() const { return {frequency_source,target_source,confidence_source}; }
    static PitchMonitorNodeParameters read(std::istream& in) {
        PitchMonitorNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!1aa7a8f99638eaf6") throw std::runtime_error("Incompatible parameter contract: pitchmonitor; rebuild Java and audio host together");
        if(!(in>>p.frequency_source)) throw std::runtime_error("Missing parameter: frequency");
        if(p.frequency_source < -1 || p.frequency_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.target_source)) throw std::runtime_error("Missing parameter: target");
        if(p.target_source < -1 || p.target_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.confidence_source)) throw std::runtime_error("Missing parameter: confidence");
        if(p.confidence_source < -1 || p.confidence_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct SpectrumNodeParameters {
    int audio_in_source{};
    std::string size_value{};
    std::string window_value{};
    double overlap_value{};
    std::string channel_value{};
    std::string padding_value{};
    int remove_dc_value{};
    int synchronous_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static SpectrumNodeParameters read(std::istream& in) {
        SpectrumNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!3158441c7adcae12") throw std::runtime_error("Incompatible parameter contract: spectrum; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.size_value)) throw std::runtime_error("Missing parameter: size");
        if(p.size_value!="256" && p.size_value!="512" && p.size_value!="1024" && p.size_value!="2048" && p.size_value!="4096" && p.size_value!="8192") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.window_value)) throw std::runtime_error("Missing parameter: window");
        if(p.window_value!="hann" && p.window_value!="hamming" && p.window_value!="blackman" && p.window_value!="rectangular") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.overlap_value)) throw std::runtime_error("Missing parameter: overlap");
        if(!std::isfinite(p.overlap_value)) throw std::runtime_error("Non-finite parameter");
        if(p.overlap_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.overlap_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.channel_value)) throw std::runtime_error("Missing parameter: channel");
        if(p.channel_value!="all" && p.channel_value!="left" && p.channel_value!="right" && p.channel_value!="mix") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.padding_value)) throw std::runtime_error("Missing parameter: padding");
        if(p.padding_value!="none" && p.padding_value!="double") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.remove_dc_value)) throw std::runtime_error("Missing parameter: remove-dc");
        if(p.remove_dc_value!=0 && p.remove_dc_value!=1) throw std::runtime_error("Boolean must be 0 or 1");
        if(!(in>>p.synchronous_value)) throw std::runtime_error("Missing parameter: synchronous");
        if(p.synchronous_value!=0 && p.synchronous_value!=1) throw std::runtime_error("Boolean must be 0 or 1");
        return p;
    }
};
struct SpectrumStatisticsNodeParameters {
    int analysis_source{};
    std::string view_value{};
    std::string statistic_value{};
    std::vector<int> sources() const { return {analysis_source}; }
    static SpectrumStatisticsNodeParameters read(std::istream& in) {
        SpectrumStatisticsNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!3d57841af4651298") throw std::runtime_error("Incompatible parameter contract: spectrumstatistics; rebuild Java and audio host together");
        if(!(in>>p.analysis_source)) throw std::runtime_error("Missing parameter: analysis");
        if(p.analysis_source < -1 || p.analysis_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.view_value)) throw std::runtime_error("Missing parameter: view");
        if(p.view_value!="magnitude" && p.view_value!="phase" && p.view_value!="power") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.statistic_value)) throw std::runtime_error("Missing parameter: statistic");
        if(p.statistic_value!="peak" && p.statistic_value!="mean" && p.statistic_value!="rms" && p.statistic_value!="integral") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct SpectrumViewerNodeParameters {
    int analysis_source{};
    std::string view_value{};
    std::vector<int> sources() const { return {analysis_source}; }
    static SpectrumViewerNodeParameters read(std::istream& in) {
        SpectrumViewerNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!f0b4ec016889dbe3") throw std::runtime_error("Incompatible parameter contract: spectrumviewer; rebuild Java and audio host together");
        if(!(in>>p.analysis_source)) throw std::runtime_error("Missing parameter: analysis");
        if(p.analysis_source < -1 || p.analysis_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.view_value)) throw std::runtime_error("Missing parameter: view");
        if(p.view_value!="auto" && p.view_value!="magnitude" && p.view_value!="phase" && p.view_value!="power" && p.view_value!="correlation") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct AnalysisStatisticsNodeParameters {
    int analysis_source{};
    std::string statistic_value{};
    std::vector<int> sources() const { return {analysis_source}; }
    static AnalysisStatisticsNodeParameters read(std::istream& in) {
        AnalysisStatisticsNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!502dd8eefa16b9dc") throw std::runtime_error("Incompatible parameter contract: analysisstatistics; rebuild Java and audio host together");
        if(!(in>>p.analysis_source)) throw std::runtime_error("Missing parameter: analysis");
        if(p.analysis_source < -1 || p.analysis_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.statistic_value)) throw std::runtime_error("Missing parameter: statistic");
        if(p.statistic_value!="peak" && p.statistic_value!="mean" && p.statistic_value!="rms" && p.statistic_value!="integral" && p.statistic_value!="frequency" && p.statistic_value!="periodicity" && p.statistic_value!="correlationtime") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct AnalysisViewNodeParameters {
    int analysis_in_source{};
    std::string view_value{};
    std::vector<int> sources() const { return {analysis_in_source}; }
    static AnalysisViewNodeParameters read(std::istream& in) {
        AnalysisViewNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!1ae3e37af4861941") throw std::runtime_error("Incompatible parameter contract: analysisview; rebuild Java and audio host together");
        if(!(in>>p.analysis_in_source)) throw std::runtime_error("Missing parameter: analysis-in");
        if(p.analysis_in_source < -1 || p.analysis_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.view_value)) throw std::runtime_error("Missing parameter: view");
        if(p.view_value!="magnitude" && p.view_value!="phase" && p.view_value!="power" && p.view_value!="correlation") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct AnalysisViewerNodeParameters {
    int analysis_source{};
    std::vector<int> sources() const { return {analysis_source}; }
    static AnalysisViewerNodeParameters read(std::istream& in) {
        AnalysisViewerNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!9d0952fca297181f") throw std::runtime_error("Incompatible parameter contract: analysisviewer; rebuild Java and audio host together");
        if(!(in>>p.analysis_source)) throw std::runtime_error("Missing parameter: analysis");
        if(p.analysis_source < -1 || p.analysis_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct AdditiveOscillatorNodeParameters {
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
    static AdditiveOscillatorNodeParameters read(std::istream& in) {
        AdditiveOscillatorNodeParameters p; std::string marker;
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
struct BitcrusherNodeParameters {
    int audio_in_source{};
    double audio_in_value{};
    int bits_source{};
    double bits_value{};
    int downsample_source{};
    double downsample_value{};
    std::vector<int> sources() const { return {audio_in_source,bits_source,downsample_source}; }
    static BitcrusherNodeParameters read(std::istream& in) {
        BitcrusherNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!5dad6ac9d5d2b366") throw std::runtime_error("Incompatible parameter contract: bitcrusher; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.audio_in_value)) throw std::runtime_error("Missing parameter: audio-in");
        if(!std::isfinite(p.audio_in_value)) throw std::runtime_error("Non-finite parameter");
        if(p.audio_in_value<-1.0) throw std::runtime_error("Parameter below minimum");
        if(p.audio_in_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.bits_source)) throw std::runtime_error("Missing parameter: bits");
        if(p.bits_source < -1 || p.bits_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.bits_value)) throw std::runtime_error("Missing parameter: bits");
        if(!std::isfinite(p.bits_value)) throw std::runtime_error("Non-finite parameter");
        if(p.bits_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.bits_value>24.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.downsample_source)) throw std::runtime_error("Missing parameter: downsample");
        if(p.downsample_source < -1 || p.downsample_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.downsample_value)) throw std::runtime_error("Missing parameter: downsample");
        if(!std::isfinite(p.downsample_value)) throw std::runtime_error("Non-finite parameter");
        if(p.downsample_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.downsample_value>1024.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct BlockDelayNodeParameters {
    int in_source{};
    double in_value{};
    std::vector<int> sources() const { return {in_source}; }
    static BlockDelayNodeParameters read(std::istream& in) {
        BlockDelayNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!445bf1af28d451b0") throw std::runtime_error("Incompatible parameter contract: blockdelay; rebuild Java and audio host together");
        if(!(in>>p.in_source)) throw std::runtime_error("Missing parameter: in");
        if(p.in_source < -1 || p.in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.in_value)) throw std::runtime_error("Missing parameter: in");
        if(!std::isfinite(p.in_value)) throw std::runtime_error("Non-finite parameter");
        return p;
    }
};
struct ChannelCombineNodeParameters {
    int left_source{};
    int right_source{};
    std::vector<int> sources() const { return {left_source,right_source}; }
    static ChannelCombineNodeParameters read(std::istream& in) {
        ChannelCombineNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!dfde9a3a98fa5473") throw std::runtime_error("Incompatible parameter contract: channelcombine; rebuild Java and audio host together");
        if(!(in>>p.left_source)) throw std::runtime_error("Missing parameter: left");
        if(p.left_source < -1 || p.left_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.right_source)) throw std::runtime_error("Missing parameter: right");
        if(p.right_source < -1 || p.right_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct ChannelSeparateNodeParameters {
    int audio_in_source{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static ChannelSeparateNodeParameters read(std::istream& in) {
        ChannelSeparateNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!825597bd1e0c7703") throw std::runtime_error("Incompatible parameter contract: channelseparate; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct ClampNodeParameters {
    int audio_in_source{};
    double audio_in_value{};
    std::string mode_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static ClampNodeParameters read(std::istream& in) {
        ClampNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!f20157c0ac42a9d8") throw std::runtime_error("Incompatible parameter contract: clamp; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.audio_in_value)) throw std::runtime_error("Missing parameter: audio-in");
        if(!std::isfinite(p.audio_in_value)) throw std::runtime_error("Non-finite parameter");
        if(p.audio_in_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.audio_in_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="clip" && p.mode_value!="foldback" && p.mode_value!="wrap" && p.mode_value!="soft_clip") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct ClockNodeParameters {
    int frequency_source{};
    double frequency_value{};
    int sync_source{};
    double sync_value{};
    std::vector<int> sources() const { return {frequency_source,sync_source}; }
    static ClockNodeParameters read(std::istream& in) {
        ClockNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!efed79e3f629d123") throw std::runtime_error("Incompatible parameter contract: clock; rebuild Java and audio host together");
        if(!(in>>p.frequency_source)) throw std::runtime_error("Missing parameter: frequency");
        if(p.frequency_source < -1 || p.frequency_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.frequency_value)) throw std::runtime_error("Missing parameter: frequency");
        if(!std::isfinite(p.frequency_value)) throw std::runtime_error("Non-finite parameter");
        if(p.frequency_value<0.001) throw std::runtime_error("Parameter below minimum");
        if(p.frequency_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.sync_source)) throw std::runtime_error("Missing parameter: sync");
        if(p.sync_source < -1 || p.sync_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.sync_value)) throw std::runtime_error("Missing parameter: sync");
        if(!std::isfinite(p.sync_value)) throw std::runtime_error("Non-finite parameter");
        return p;
    }
};
struct ComparatorNodeParameters {
    int a_source{};
    double a_value{};
    int b_source{};
    double b_value{};
    std::string operation_value{};
    std::vector<int> sources() const { return {a_source,b_source}; }
    static ComparatorNodeParameters read(std::istream& in) {
        ComparatorNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!a9cf470c93432d98") throw std::runtime_error("Incompatible parameter contract: comparator; rebuild Java and audio host together");
        if(!(in>>p.a_source)) throw std::runtime_error("Missing parameter: a");
        if(p.a_source < -1 || p.a_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.a_value)) throw std::runtime_error("Missing parameter: a");
        if(!std::isfinite(p.a_value)) throw std::runtime_error("Non-finite parameter");
        if(p.a_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.a_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.b_source)) throw std::runtime_error("Missing parameter: b");
        if(p.b_source < -1 || p.b_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.b_value)) throw std::runtime_error("Missing parameter: b");
        if(!std::isfinite(p.b_value)) throw std::runtime_error("Non-finite parameter");
        if(p.b_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.b_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.operation_value)) throw std::runtime_error("Missing parameter: operation");
        if(p.operation_value!="less" && p.operation_value!="less_equal" && p.operation_value!="greater" && p.operation_value!="greater_equal" && p.operation_value!="equal" && p.operation_value!="not_equal") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct CurveNodeParameters {
    int audio_in_source{};
    double audio_in_value{};
    double from_min_value{};
    double from_max_value{};
    double to_min_value{};
    double to_max_value{};
    std::string outside_value{};
    FunctionCurve curve_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static CurveNodeParameters read(std::istream& in) {
        CurveNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!c9493da779cb305c") throw std::runtime_error("Incompatible parameter contract: curve; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.audio_in_value)) throw std::runtime_error("Missing parameter: audio-in");
        if(!std::isfinite(p.audio_in_value)) throw std::runtime_error("Non-finite parameter");
        if(p.audio_in_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.audio_in_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.from_min_value)) throw std::runtime_error("Missing parameter: from-min");
        if(!std::isfinite(p.from_min_value)) throw std::runtime_error("Non-finite parameter");
        if(p.from_min_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.from_min_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.from_max_value)) throw std::runtime_error("Missing parameter: from-max");
        if(!std::isfinite(p.from_max_value)) throw std::runtime_error("Non-finite parameter");
        if(p.from_max_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.from_max_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.to_min_value)) throw std::runtime_error("Missing parameter: to-min");
        if(!std::isfinite(p.to_min_value)) throw std::runtime_error("Non-finite parameter");
        if(p.to_min_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.to_min_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.to_max_value)) throw std::runtime_error("Missing parameter: to-max");
        if(!std::isfinite(p.to_max_value)) throw std::runtime_error("Non-finite parameter");
        if(p.to_max_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.to_max_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.outside_value)) throw std::runtime_error("Missing parameter: outside");
        p.curve_value.read(in);
        return p;
    }
};
struct DelayNodeParameters {
    int audio_in_source{};
    double audio_in_value{};
    int time_source{};
    double time_value{};
    std::vector<int> sources() const { return {audio_in_source,time_source}; }
    static DelayNodeParameters read(std::istream& in) {
        DelayNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!367c27885dede8f4") throw std::runtime_error("Incompatible parameter contract: delay; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.audio_in_value)) throw std::runtime_error("Missing parameter: audio-in");
        if(!std::isfinite(p.audio_in_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.time_source)) throw std::runtime_error("Missing parameter: time");
        if(p.time_source < -1 || p.time_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.time_value)) throw std::runtime_error("Missing parameter: time");
        if(!std::isfinite(p.time_value)) throw std::runtime_error("Non-finite parameter");
        if(p.time_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.time_value>10.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct DigitalNoiseNodeParameters {
    int frequency_source{};
    double frequency_value{};
    int strength_source{};
    double strength_value{};
    int seed_source{};
    int seed_value{};
    std::vector<int> sources() const { return {frequency_source,strength_source,seed_source}; }
    static DigitalNoiseNodeParameters read(std::istream& in) {
        DigitalNoiseNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!5bc07ddcba438cf0") throw std::runtime_error("Incompatible parameter contract: digitalnoise; rebuild Java and audio host together");
        if(!(in>>p.frequency_source)) throw std::runtime_error("Missing parameter: frequency");
        if(p.frequency_source < -1 || p.frequency_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.frequency_value)) throw std::runtime_error("Missing parameter: frequency");
        if(!std::isfinite(p.frequency_value)) throw std::runtime_error("Non-finite parameter");
        if(p.frequency_value<1.0E-5) throw std::runtime_error("Parameter below minimum");
        if(p.frequency_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.strength_source)) throw std::runtime_error("Missing parameter: strength");
        if(p.strength_source < -1 || p.strength_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.strength_value)) throw std::runtime_error("Missing parameter: strength");
        if(!std::isfinite(p.strength_value)) throw std::runtime_error("Non-finite parameter");
        if(p.strength_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.strength_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.seed_source)) throw std::runtime_error("Missing parameter: seed");
        if(p.seed_source < -1 || p.seed_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.seed_value)) throw std::runtime_error("Missing parameter: seed");
        if(p.seed_value<-1.0) throw std::runtime_error("Parameter below minimum");
        if(p.seed_value>1.6777215E7) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct EdgeDetectorNodeParameters {
    int in_source{};
    double in_value{};
    int threshold_source{};
    double threshold_value{};
    std::string mode_value{};
    std::vector<int> sources() const { return {in_source,threshold_source}; }
    static EdgeDetectorNodeParameters read(std::istream& in) {
        EdgeDetectorNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!27b361d7882d3250") throw std::runtime_error("Incompatible parameter contract: edgedetector; rebuild Java and audio host together");
        if(!(in>>p.in_source)) throw std::runtime_error("Missing parameter: in");
        if(p.in_source < -1 || p.in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.in_value)) throw std::runtime_error("Missing parameter: in");
        if(!std::isfinite(p.in_value)) throw std::runtime_error("Non-finite parameter");
        if(p.in_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.in_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.threshold_source)) throw std::runtime_error("Missing parameter: threshold");
        if(p.threshold_source < -1 || p.threshold_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.threshold_value)) throw std::runtime_error("Missing parameter: threshold");
        if(!std::isfinite(p.threshold_value)) throw std::runtime_error("Non-finite parameter");
        if(p.threshold_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.threshold_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="rising" && p.mode_value!="falling" && p.mode_value!="both") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct EnvelopeNodeParameters {
    int gate_source{};
    double gate_value{};
    int trigger_source{};
    double trigger_value{};
    double duration_value{};
    double release_value{};
    double sustain_position_value{};
    FunctionCurve curve_value{};
    std::vector<int> sources() const { return {gate_source,trigger_source}; }
    static EnvelopeNodeParameters read(std::istream& in) {
        EnvelopeNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!a11bbab0677e0e68") throw std::runtime_error("Incompatible parameter contract: envelope; rebuild Java and audio host together");
        if(!(in>>p.gate_source)) throw std::runtime_error("Missing parameter: gate");
        if(p.gate_source < -1 || p.gate_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.gate_value)) throw std::runtime_error("Missing parameter: gate");
        if(!std::isfinite(p.gate_value)) throw std::runtime_error("Non-finite parameter");
        if(p.gate_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.gate_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.trigger_source)) throw std::runtime_error("Missing parameter: trigger");
        if(p.trigger_source < -1 || p.trigger_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.trigger_value)) throw std::runtime_error("Missing parameter: trigger");
        if(!std::isfinite(p.trigger_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.duration_value)) throw std::runtime_error("Missing parameter: duration");
        if(!std::isfinite(p.duration_value)) throw std::runtime_error("Non-finite parameter");
        if(p.duration_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.duration_value>60000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.release_value)) throw std::runtime_error("Missing parameter: release");
        if(!std::isfinite(p.release_value)) throw std::runtime_error("Non-finite parameter");
        if(p.release_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.release_value>60000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.sustain_position_value)) throw std::runtime_error("Missing parameter: sustain-position");
        if(!std::isfinite(p.sustain_position_value)) throw std::runtime_error("Non-finite parameter");
        if(p.sustain_position_value<0.01) throw std::runtime_error("Parameter below minimum");
        if(p.sustain_position_value>0.99) throw std::runtime_error("Parameter above maximum");
        p.curve_value.read(in);
        return p;
    }
};
struct FilterNodeParameters {
    int audio_in_source{};
    int cutoff_source{};
    double cutoff_value{};
    int resonance_source{};
    double resonance_value{};
    std::string mode_value{};
    std::vector<int> sources() const { return {audio_in_source,cutoff_source,resonance_source}; }
    static FilterNodeParameters read(std::istream& in) {
        FilterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!cd23044f7c58d242") throw std::runtime_error("Incompatible parameter contract: filter; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.cutoff_source)) throw std::runtime_error("Missing parameter: cutoff");
        if(p.cutoff_source < -1 || p.cutoff_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.cutoff_value)) throw std::runtime_error("Missing parameter: cutoff");
        if(!std::isfinite(p.cutoff_value)) throw std::runtime_error("Non-finite parameter");
        if(p.cutoff_value<5.0) throw std::runtime_error("Parameter below minimum");
        if(p.cutoff_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.resonance_source)) throw std::runtime_error("Missing parameter: resonance");
        if(p.resonance_source < -1 || p.resonance_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.resonance_value)) throw std::runtime_error("Missing parameter: resonance");
        if(!std::isfinite(p.resonance_value)) throw std::runtime_error("Non-finite parameter");
        if(p.resonance_value<0.1) throw std::runtime_error("Parameter below minimum");
        if(p.resonance_value>30.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="lowpass" && p.mode_value!="highpass" && p.mode_value!="bandpass") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct GainNodeParameters {
    int audio_in_source{};
    double gainDb_value{};
    int gainDb_source{};
    std::vector<int> sources() const { return {audio_in_source,gainDb_source}; }
    static GainNodeParameters read(std::istream& in) {
        GainNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!ce179d9f5f5839c0") throw std::runtime_error("Incompatible parameter contract: gain; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.gainDb_value)) throw std::runtime_error("Missing parameter: gainDb");
        if(!std::isfinite(p.gainDb_value)) throw std::runtime_error("Non-finite parameter");
        if(p.gainDb_value<-160.0) throw std::runtime_error("Parameter below minimum");
        if(p.gainDb_value>6.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.gainDb_source)) throw std::runtime_error("Missing parameter: gainDb");
        if(p.gainDb_source < -1 || p.gainDb_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct InputNodeParameters {
    std::string endpoint_value{};
    std::string capture_mode_value{};
    std::string channels_value{};
    std::vector<int> sources() const { return {}; }
    static InputNodeParameters read(std::istream& in) {
        InputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!9355034c5b36a167") throw std::runtime_error("Incompatible parameter contract: input; rebuild Java and audio host together");
        if(!(in>>p.endpoint_value)) throw std::runtime_error("Missing parameter: endpoint");
        if(!(in>>p.capture_mode_value)) throw std::runtime_error("Missing parameter: capture-mode");
        if(p.capture_mode_value!="copy") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.channels_value)) throw std::runtime_error("Missing parameter: channels");
        if(p.channels_value!="mono" && p.channels_value!="stereo") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct LegacyVoiceMergeNodeParameters {
    int audio_in_source{};
    std::string mode_value{};
    int pitch_source{};
    double width_value{};
    std::vector<int> sources() const { return {audio_in_source,pitch_source}; }
    static LegacyVoiceMergeNodeParameters read(std::istream& in) {
        LegacyVoiceMergeNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!3f0c7d77e2cea323") throw std::runtime_error("Incompatible parameter contract: voicemerge; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>marker)||marker!="mix") throw std::runtime_error("Invalid parameter marker");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="sum" && p.mode_value!="mean" && p.mode_value!="spread" && p.mode_value!="pitch") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.pitch_source)) throw std::runtime_error("Missing parameter: pitch");
        if(p.pitch_source < -1 || p.pitch_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.width_value)) throw std::runtime_error("Missing parameter: width");
        if(!std::isfinite(p.width_value)) throw std::runtime_error("Non-finite parameter");
        if(p.width_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.width_value>1.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct MapRangeNodeParameters {
    int audio_in_source{};
    double audio_in_value{};
    int from_min_source{};
    double from_min_value{};
    int from_max_source{};
    double from_max_value{};
    int to_min_source{};
    double to_min_value{};
    int to_max_source{};
    double to_max_value{};
    int clamp_value{};
    std::vector<int> sources() const { return {audio_in_source,from_min_source,from_max_source,to_min_source,to_max_source}; }
    static MapRangeNodeParameters read(std::istream& in) {
        MapRangeNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!98c23c5e5bb8a10b") throw std::runtime_error("Incompatible parameter contract: maprange; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.audio_in_value)) throw std::runtime_error("Missing parameter: audio-in");
        if(!std::isfinite(p.audio_in_value)) throw std::runtime_error("Non-finite parameter");
        if(p.audio_in_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.audio_in_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.from_min_source)) throw std::runtime_error("Missing parameter: from-min");
        if(p.from_min_source < -1 || p.from_min_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.from_min_value)) throw std::runtime_error("Missing parameter: from-min");
        if(!std::isfinite(p.from_min_value)) throw std::runtime_error("Non-finite parameter");
        if(p.from_min_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.from_min_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.from_max_source)) throw std::runtime_error("Missing parameter: from-max");
        if(p.from_max_source < -1 || p.from_max_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.from_max_value)) throw std::runtime_error("Missing parameter: from-max");
        if(!std::isfinite(p.from_max_value)) throw std::runtime_error("Non-finite parameter");
        if(p.from_max_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.from_max_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.to_min_source)) throw std::runtime_error("Missing parameter: to-min");
        if(p.to_min_source < -1 || p.to_min_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.to_min_value)) throw std::runtime_error("Missing parameter: to-min");
        if(!std::isfinite(p.to_min_value)) throw std::runtime_error("Non-finite parameter");
        if(p.to_min_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.to_min_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.to_max_source)) throw std::runtime_error("Missing parameter: to-max");
        if(p.to_max_source < -1 || p.to_max_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.to_max_value)) throw std::runtime_error("Missing parameter: to-max");
        if(!std::isfinite(p.to_max_value)) throw std::runtime_error("Non-finite parameter");
        if(p.to_max_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.to_max_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.clamp_value)) throw std::runtime_error("Missing parameter: clamp");
        if(p.clamp_value!=0 && p.clamp_value!=1) throw std::runtime_error("Boolean must be 0 or 1");
        return p;
    }
};
struct MathNodeParameters {
    int a_source{};
    double a_value{};
    int b_source{};
    double b_value{};
    std::string operation_value{};
    int clamp_value{};
    std::vector<int> sources() const { return {a_source,b_source}; }
    static MathNodeParameters read(std::istream& in) {
        MathNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!f2c12009349832d2") throw std::runtime_error("Incompatible parameter contract: math; rebuild Java and audio host together");
        if(!(in>>p.a_source)) throw std::runtime_error("Missing parameter: a");
        if(p.a_source < -1 || p.a_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.a_value)) throw std::runtime_error("Missing parameter: a");
        if(!std::isfinite(p.a_value)) throw std::runtime_error("Non-finite parameter");
        if(p.a_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.a_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.b_source)) throw std::runtime_error("Missing parameter: b");
        if(p.b_source < -1 || p.b_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.b_value)) throw std::runtime_error("Missing parameter: b");
        if(!std::isfinite(p.b_value)) throw std::runtime_error("Non-finite parameter");
        if(p.b_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.b_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.operation_value)) throw std::runtime_error("Missing parameter: operation");
        if(p.operation_value!="add" && p.operation_value!="subtract" && p.operation_value!="multiply" && p.operation_value!="divide" && p.operation_value!="power" && p.operation_value!="remainder" && p.operation_value!="modulo" && p.operation_value!="minimum" && p.operation_value!="maximum" && p.operation_value!="floor" && p.operation_value!="ceil" && p.operation_value!="round" && p.operation_value!="fract" && p.operation_value!="sqrt" && p.operation_value!="root" && p.operation_value!="exp" && p.operation_value!="log" && p.operation_value!="log_base" && p.operation_value!="absolute" && p.operation_value!="negate") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.clamp_value)) throw std::runtime_error("Missing parameter: clamp");
        if(p.clamp_value!=0 && p.clamp_value!=1) throw std::runtime_error("Boolean must be 0 or 1");
        return p;
    }
};
struct MixNodeParameters {
    int a_source{};
    double a_value{};
    int b_source{};
    double b_value{};
    int factor_source{};
    double factor_value{};
    std::string mode_value{};
    std::vector<int> sources() const { return {a_source,b_source,factor_source}; }
    static MixNodeParameters read(std::istream& in) {
        MixNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!35ad97674505797a") throw std::runtime_error("Incompatible parameter contract: mix; rebuild Java and audio host together");
        if(!(in>>p.a_source)) throw std::runtime_error("Missing parameter: a");
        if(p.a_source < -1 || p.a_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.a_value)) throw std::runtime_error("Missing parameter: a");
        if(!std::isfinite(p.a_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.b_source)) throw std::runtime_error("Missing parameter: b");
        if(p.b_source < -1 || p.b_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.b_value)) throw std::runtime_error("Missing parameter: b");
        if(!std::isfinite(p.b_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.factor_source)) throw std::runtime_error("Missing parameter: factor");
        if(p.factor_source < -1 || p.factor_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.factor_value)) throw std::runtime_error("Missing parameter: factor");
        if(!std::isfinite(p.factor_value)) throw std::runtime_error("Non-finite parameter");
        if(p.factor_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.factor_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="linear" && p.mode_value!="equalpower") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct NoiseNodeParameters {
    std::string mode_value{};
    std::vector<int> sources() const { return {}; }
    static NoiseNodeParameters read(std::istream& in) {
        NoiseNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!742852c54323d505") throw std::runtime_error("Incompatible parameter contract: noise; rebuild Java and audio host together");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="white" && p.mode_value!="pink" && p.mode_value!="brown" && p.mode_value!="blue") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct OscillatorNodeParameters {
    int frequency_source{};
    double frequency_value{};
    std::string waveform_value{};
    int waveform_source{};
    std::vector<int> sources() const { return {frequency_source,waveform_source}; }
    static OscillatorNodeParameters read(std::istream& in) {
        OscillatorNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!6f1c0f366ccb47fd") throw std::runtime_error("Incompatible parameter contract: osc; rebuild Java and audio host together");
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
        return p;
    }
};
struct OutputNodeParameters {
    int audio_in_source{};
    std::string endpoint_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static OutputNodeParameters read(std::istream& in) {
        OutputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!193e8d8659eae5a6") throw std::runtime_error("Incompatible parameter contract: deviceout; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.endpoint_value)) throw std::runtime_error("Missing parameter: endpoint");
        return p;
    }
};
struct PanNodeParameters {
    int audio_in_source{};
    int pan_source{};
    double pan_value{};
    std::vector<int> sources() const { return {audio_in_source,pan_source}; }
    static PanNodeParameters read(std::istream& in) {
        PanNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!893d8ec4b8a35fd2") throw std::runtime_error("Incompatible parameter contract: pan; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.pan_source)) throw std::runtime_error("Missing parameter: pan");
        if(p.pan_source < -1 || p.pan_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.pan_value)) throw std::runtime_error("Missing parameter: pan");
        if(!std::isfinite(p.pan_value)) throw std::runtime_error("Non-finite parameter");
        if(p.pan_value<-1.0) throw std::runtime_error("Parameter below minimum");
        if(p.pan_value>1.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct SampleDelayNodeParameters {
    int in_source{};
    double in_value{};
    std::vector<int> sources() const { return {in_source}; }
    static SampleDelayNodeParameters read(std::istream& in) {
        SampleDelayNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!9ea2c8a578e18319") throw std::runtime_error("Incompatible parameter contract: sampledelay; rebuild Java and audio host together");
        if(!(in>>p.in_source)) throw std::runtime_error("Missing parameter: in");
        if(p.in_source < -1 || p.in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.in_value)) throw std::runtime_error("Missing parameter: in");
        if(!std::isfinite(p.in_value)) throw std::runtime_error("Non-finite parameter");
        return p;
    }
};
struct SampleHoldNodeParameters {
    int audio_in_source{};
    double audio_in_value{};
    int trigger_source{};
    double trigger_value{};
    std::vector<int> sources() const { return {audio_in_source,trigger_source}; }
    static SampleHoldNodeParameters read(std::istream& in) {
        SampleHoldNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!2a041693d952bf76") throw std::runtime_error("Incompatible parameter contract: samplehold; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.audio_in_value)) throw std::runtime_error("Missing parameter: audio-in");
        if(!std::isfinite(p.audio_in_value)) throw std::runtime_error("Non-finite parameter");
        if(p.audio_in_value<-1000000.0) throw std::runtime_error("Parameter below minimum");
        if(p.audio_in_value>1000000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.trigger_source)) throw std::runtime_error("Missing parameter: trigger");
        if(p.trigger_source < -1 || p.trigger_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.trigger_value)) throw std::runtime_error("Missing parameter: trigger");
        if(!std::isfinite(p.trigger_value)) throw std::runtime_error("Non-finite parameter");
        return p;
    }
};
struct ValueNodeParameters {
    double value_value{};
    std::vector<int> sources() const { return {}; }
    static ValueNodeParameters read(std::istream& in) {
        ValueNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!5e91de4eb658efcf") throw std::runtime_error("Incompatible parameter contract: value; rebuild Java and audio host together");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        return p;
    }
};
struct VoiceMergeNodeParameters {
    int audio_in_source{};
    std::string mode_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static VoiceMergeNodeParameters read(std::istream& in) {
        VoiceMergeNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!752f0d0bdeb2ca8d") throw std::runtime_error("Incompatible parameter contract: voicemerge; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>marker)||marker!="combine") throw std::runtime_error("Invalid parameter marker");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="sum" && p.mode_value!="mean") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct ContextInputNodeParameters {
    std::vector<int> sources() const { return {}; }
    static ContextInputNodeParameters read(std::istream& in) {
        ContextInputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7133a4d4393cd14d") throw std::runtime_error("Incompatible parameter contract: context; rebuild Java and audio host together");
        return p;
    }
};
struct RateConverterNodeParameters {
    int value_source{};
    double value_value{};
    std::string rate_value{};
    std::string reduction_value{};
    std::vector<int> sources() const { return {value_source}; }
    static RateConverterNodeParameters read(std::istream& in) {
        RateConverterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7059f3ef8aad91d2") throw std::runtime_error("Incompatible parameter contract: rateconvert; rebuild Java and audio host together");
        if(!(in>>p.value_source)) throw std::runtime_error("Missing parameter: value");
        if(p.value_source < -1 || p.value_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.rate_value)) throw std::runtime_error("Missing parameter: rate");
        if(p.rate_value!="control" && p.rate_value!="audio" && p.rate_value!="event") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.reduction_value)) throw std::runtime_error("Missing parameter: reduction");
        if(p.reduction_value!="first" && p.reduction_value!="last" && p.reduction_value!="mean" && p.reduction_value!="rms" && p.reduction_value!="minimum" && p.reduction_value!="maximum" && p.reduction_value!="peak") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct TriggerPulseNodeParameters {
    int trigger_source{};
    std::vector<int> sources() const { return {trigger_source}; }
    static TriggerPulseNodeParameters read(std::istream& in) {
        TriggerPulseNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!8da02bb3ba57f238") throw std::runtime_error("Incompatible parameter contract: triggerpulse; rebuild Java and audio host together");
        if(!(in>>p.trigger_source)) throw std::runtime_error("Missing parameter: trigger");
        if(p.trigger_source < -1 || p.trigger_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct TriggerInputNodeParameters {
    std::vector<int> sources() const { return {}; }
    static TriggerInputNodeParameters read(std::istream& in) {
        TriggerInputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!35ffb2770c63d50f") throw std::runtime_error("Incompatible parameter contract: triggerinput; rebuild Java and audio host together");
        if(!(in>>marker)||marker!="trigger-v1") throw std::runtime_error("Invalid parameter marker");
        return p;
    }
};
struct FrequencyConverterNodeParameters {
    int value_source{};
    double value_value{};
    std::string from_value{};
    std::string to_value{};
    std::vector<int> sources() const { return {value_source}; }
    static FrequencyConverterNodeParameters read(std::istream& in) {
        FrequencyConverterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7f7bc15f2eb92337") throw std::runtime_error("Incompatible parameter contract: unitconvert; rebuild Java and audio host together");
        if(!(in>>p.value_source)) throw std::runtime_error("Missing parameter: value");
        if(p.value_source < -1 || p.value_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.from_value)) throw std::runtime_error("Missing parameter: from");
        if(!(in>>p.to_value)) throw std::runtime_error("Missing parameter: to");
        return p;
    }
};
struct GainConverterNodeParameters {
    int value_source{};
    double value_value{};
    std::string from_value{};
    std::string to_value{};
    std::vector<int> sources() const { return {value_source}; }
    static GainConverterNodeParameters read(std::istream& in) {
        GainConverterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7f7bc15f2eb92337") throw std::runtime_error("Incompatible parameter contract: unitconvert; rebuild Java and audio host together");
        if(!(in>>p.value_source)) throw std::runtime_error("Missing parameter: value");
        if(p.value_source < -1 || p.value_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.from_value)) throw std::runtime_error("Missing parameter: from");
        if(!(in>>p.to_value)) throw std::runtime_error("Missing parameter: to");
        return p;
    }
};
struct PhaseConverterNodeParameters {
    int value_source{};
    double value_value{};
    std::string from_value{};
    std::string to_value{};
    std::vector<int> sources() const { return {value_source}; }
    static PhaseConverterNodeParameters read(std::istream& in) {
        PhaseConverterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7f7bc15f2eb92337") throw std::runtime_error("Incompatible parameter contract: unitconvert; rebuild Java and audio host together");
        if(!(in>>p.value_source)) throw std::runtime_error("Missing parameter: value");
        if(p.value_source < -1 || p.value_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.from_value)) throw std::runtime_error("Missing parameter: from");
        if(!(in>>p.to_value)) throw std::runtime_error("Missing parameter: to");
        return p;
    }
};
struct PitchConverterNodeParameters {
    int value_source{};
    double value_value{};
    std::string from_value{};
    std::string to_value{};
    std::vector<int> sources() const { return {value_source}; }
    static PitchConverterNodeParameters read(std::istream& in) {
        PitchConverterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7f7bc15f2eb92337") throw std::runtime_error("Incompatible parameter contract: unitconvert; rebuild Java and audio host together");
        if(!(in>>p.value_source)) throw std::runtime_error("Missing parameter: value");
        if(p.value_source < -1 || p.value_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.from_value)) throw std::runtime_error("Missing parameter: from");
        if(!(in>>p.to_value)) throw std::runtime_error("Missing parameter: to");
        return p;
    }
};
struct TimeConverterNodeParameters {
    int value_source{};
    double value_value{};
    std::string from_value{};
    std::string to_value{};
    std::vector<int> sources() const { return {value_source}; }
    static TimeConverterNodeParameters read(std::istream& in) {
        TimeConverterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7f7bc15f2eb92337") throw std::runtime_error("Incompatible parameter contract: unitconvert; rebuild Java and audio host together");
        if(!(in>>p.value_source)) throw std::runtime_error("Missing parameter: value");
        if(p.value_source < -1 || p.value_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.from_value)) throw std::runtime_error("Missing parameter: from");
        if(!(in>>p.to_value)) throw std::runtime_error("Missing parameter: to");
        return p;
    }
};
struct UnitConverterNodeParameters {
    int value_source{};
    double value_value{};
    std::string from_value{};
    std::string to_value{};
    std::vector<int> sources() const { return {value_source}; }
    static UnitConverterNodeParameters read(std::istream& in) {
        UnitConverterNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!7f7bc15f2eb92337") throw std::runtime_error("Incompatible parameter contract: unitconvert; rebuild Java and audio host together");
        if(!(in>>p.value_source)) throw std::runtime_error("Missing parameter: value");
        if(p.value_source < -1 || p.value_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.value_value)) throw std::runtime_error("Missing parameter: value");
        if(!std::isfinite(p.value_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.from_value)) throw std::runtime_error("Missing parameter: from");
        if(!(in>>p.to_value)) throw std::runtime_error("Missing parameter: to");
        return p;
    }
};
struct MidiCCNodeParameters {
    int midi_in_source{};
    int channel_value{};
    int controller_value{};
    std::vector<int> sources() const { return {midi_in_source}; }
    static MidiCCNodeParameters read(std::istream& in) {
        MidiCCNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!38f012bd1dbdecd3") throw std::runtime_error("Incompatible parameter contract: midicc; rebuild Java and audio host together");
        if(!(in>>p.midi_in_source)) throw std::runtime_error("Missing parameter: midi-in");
        if(p.midi_in_source < -1 || p.midi_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.channel_value)) throw std::runtime_error("Missing parameter: channel");
        if(p.channel_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.channel_value>16.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.controller_value)) throw std::runtime_error("Missing parameter: controller");
        if(p.controller_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.controller_value>127.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct MidiDelayNodeParameters {
    int midi_in_source{};
    double delay_value{};
    std::vector<int> sources() const { return {midi_in_source}; }
    static MidiDelayNodeParameters read(std::istream& in) {
        MidiDelayNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!bb50876dd2f75f54") throw std::runtime_error("Incompatible parameter contract: mididelay; rebuild Java and audio host together");
        if(!(in>>p.midi_in_source)) throw std::runtime_error("Missing parameter: midi-in");
        if(p.midi_in_source < -1 || p.midi_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.delay_value)) throw std::runtime_error("Missing parameter: delay");
        if(!std::isfinite(p.delay_value)) throw std::runtime_error("Non-finite parameter");
        if(p.delay_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.delay_value>10000.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct MidiInputNodeParameters {
    std::string device_value{};
    std::vector<int> sources() const { return {}; }
    static MidiInputNodeParameters read(std::istream& in) {
        MidiInputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!d9cb8c8ade22c7ac") throw std::runtime_error("Incompatible parameter contract: midiin; rebuild Java and audio host together");
        if(!(in>>p.device_value)) throw std::runtime_error("Missing parameter: device");
        return p;
    }
};
struct MidiNoteNodeParameters {
    int midi_in_source{};
    int channel_value{};
    double bend_range_value{};
    int voices_value{};
    std::vector<int> sources() const { return {midi_in_source}; }
    static MidiNoteNodeParameters read(std::istream& in) {
        MidiNoteNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!d6a775d538011745") throw std::runtime_error("Incompatible parameter contract: midinote; rebuild Java and audio host together");
        if(!(in>>p.midi_in_source)) throw std::runtime_error("Missing parameter: midi-in");
        if(p.midi_in_source < -1 || p.midi_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.channel_value)) throw std::runtime_error("Missing parameter: channel");
        if(p.channel_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.channel_value>16.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.bend_range_value)) throw std::runtime_error("Missing parameter: bend-range");
        if(!std::isfinite(p.bend_range_value)) throw std::runtime_error("Non-finite parameter");
        if(p.bend_range_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.bend_range_value>48.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>marker)||marker!="poly") throw std::runtime_error("Invalid parameter marker");
        if(!(in>>p.voices_value)) throw std::runtime_error("Missing parameter: voices");
        if(p.voices_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.voices_value>32.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct MidiTransposeNodeParameters {
    int midi_in_source{};
    double transpose_value{};
    std::vector<int> sources() const { return {midi_in_source}; }
    static MidiTransposeNodeParameters read(std::istream& in) {
        MidiTransposeNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!293f06894121c90f") throw std::runtime_error("Incompatible parameter contract: miditranspose; rebuild Java and audio host together");
        if(!(in>>p.midi_in_source)) throw std::runtime_error("Missing parameter: midi-in");
        if(p.midi_in_source < -1 || p.midi_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.transpose_value)) throw std::runtime_error("Missing parameter: transpose");
        if(!std::isfinite(p.transpose_value)) throw std::runtime_error("Non-finite parameter");
        if(p.transpose_value<-48.0) throw std::runtime_error("Parameter below minimum");
        if(p.transpose_value>48.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct SlewNodeParameters {
    int in_source{};
    double in_value{};
    int rise_source{};
    double rise_value{};
    int fall_source{};
    double fall_value{};
    std::vector<int> sources() const { return {in_source,rise_source,fall_source}; }
    static SlewNodeParameters read(std::istream& in) {
        SlewNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!50e66eaa73d290b1") throw std::runtime_error("Incompatible parameter contract: slew; rebuild Java and audio host together");
        if(!(in>>p.in_source)) throw std::runtime_error("Missing parameter: in");
        if(p.in_source < -1 || p.in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.in_value)) throw std::runtime_error("Missing parameter: in");
        if(!std::isfinite(p.in_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.rise_source)) throw std::runtime_error("Missing parameter: rise");
        if(p.rise_source < -1 || p.rise_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.rise_value)) throw std::runtime_error("Missing parameter: rise");
        if(!std::isfinite(p.rise_value)) throw std::runtime_error("Non-finite parameter");
        if(p.rise_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.rise_value>60.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.fall_source)) throw std::runtime_error("Missing parameter: fall");
        if(p.fall_source < -1 || p.fall_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.fall_value)) throw std::runtime_error("Missing parameter: fall");
        if(!std::isfinite(p.fall_value)) throw std::runtime_error("Non-finite parameter");
        if(p.fall_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.fall_value>60.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct InstanceCombineNodeParameters {
    int in_source{};
    double in_value{};
    int weight_source{};
    double weight_value{};
    std::string mode_value{};
    std::vector<int> sources() const { return {in_source,weight_source}; }
    static InstanceCombineNodeParameters read(std::istream& in) {
        InstanceCombineNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!b3a7649629004692") throw std::runtime_error("Incompatible parameter contract: instancecombine; rebuild Java and audio host together");
        if(!(in>>p.in_source)) throw std::runtime_error("Missing parameter: in");
        if(p.in_source < -1 || p.in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.in_value)) throw std::runtime_error("Missing parameter: in");
        if(!std::isfinite(p.in_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.weight_source)) throw std::runtime_error("Missing parameter: weight");
        if(p.weight_source < -1 || p.weight_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.weight_value)) throw std::runtime_error("Missing parameter: weight");
        if(!std::isfinite(p.weight_value)) throw std::runtime_error("Non-finite parameter");
        if(p.weight_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.weight_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="sum" && p.mode_value!="mean" && p.mode_value!="weighted") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct MultipleGraphNodeParameters {
    int count_source{};
    int count_value{};
    int maximum_value{};
    std::string mode_value{};
    int input0_source{};
    int input1_source{};
    int input2_source{};
    int input3_source{};
    int input4_source{};
    int input5_source{};
    int input6_source{};
    int input7_source{};
    std::string program_value{};
    std::vector<int> sources() const { return {count_source,input0_source,input1_source,input2_source,input3_source,input4_source,input5_source,input6_source,input7_source}; }
    static MultipleGraphNodeParameters read(std::istream& in) {
        MultipleGraphNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!be6c5b3b1b5d2183") throw std::runtime_error("Incompatible parameter contract: multiplegraph; rebuild Java and audio host together");
        if(!(in>>p.count_source)) throw std::runtime_error("Missing parameter: count");
        if(p.count_source < -1 || p.count_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.count_value)) throw std::runtime_error("Missing parameter: count");
        if(p.count_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.count_value>256.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.maximum_value)) throw std::runtime_error("Missing parameter: maximum");
        if(p.maximum_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.maximum_value>256.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="sum" && p.mode_value!="mean") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.input0_source)) throw std::runtime_error("Missing parameter: input0");
        if(p.input0_source < -1 || p.input0_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input1_source)) throw std::runtime_error("Missing parameter: input1");
        if(p.input1_source < -1 || p.input1_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input2_source)) throw std::runtime_error("Missing parameter: input2");
        if(p.input2_source < -1 || p.input2_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input3_source)) throw std::runtime_error("Missing parameter: input3");
        if(p.input3_source < -1 || p.input3_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input4_source)) throw std::runtime_error("Missing parameter: input4");
        if(p.input4_source < -1 || p.input4_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input5_source)) throw std::runtime_error("Missing parameter: input5");
        if(p.input5_source < -1 || p.input5_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input6_source)) throw std::runtime_error("Missing parameter: input6");
        if(p.input6_source < -1 || p.input6_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input7_source)) throw std::runtime_error("Missing parameter: input7");
        if(p.input7_source < -1 || p.input7_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.program_value)) throw std::runtime_error("Missing parameter: program");
        return p;
    }
};
struct InstanceContextNodeParameters {
    std::vector<int> sources() const { return {}; }
    static InstanceContextNodeParameters read(std::istream& in) {
        InstanceContextNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!1b19a3bdaa57339f") throw std::runtime_error("Incompatible parameter contract: instancecontext; rebuild Java and audio host together");
        return p;
    }
};
struct MultipleInputNodeParameters {
    int slot_value{};
    std::vector<int> sources() const { return {}; }
    static MultipleInputNodeParameters read(std::istream& in) {
        MultipleInputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!95697de8a15532f4") throw std::runtime_error("Incompatible parameter contract: multipleinput; rebuild Java and audio host together");
        if(!(in>>p.slot_value)) throw std::runtime_error("Missing parameter: slot");
        if(p.slot_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.slot_value>8.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct MultipleOutputNodeParameters {
    int in_source{};
    std::vector<int> sources() const { return {in_source}; }
    static MultipleOutputNodeParameters read(std::istream& in) {
        MultipleOutputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!8038c0daf6c58441") throw std::runtime_error("Incompatible parameter contract: multipleoutput; rebuild Java and audio host together");
        if(!(in>>p.in_source)) throw std::runtime_error("Missing parameter: in");
        if(p.in_source < -1 || p.in_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct ScaleQuantizerNodeParameters {
    int frequency_source{};
    double frequency_value{};
    int target_source{};
    double target_value{};
    std::string mode_value{};
    std::string root_value{};
    std::string scale_value{};
    double tuning_value{};
    int hysteresis_source{};
    double hysteresis_value{};
    std::vector<int> sources() const { return {frequency_source,target_source,hysteresis_source}; }
    static ScaleQuantizerNodeParameters read(std::istream& in) {
        ScaleQuantizerNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!9db8fc1c4fa3c906") throw std::runtime_error("Incompatible parameter contract: scalequantizer; rebuild Java and audio host together");
        if(!(in>>p.frequency_source)) throw std::runtime_error("Missing parameter: frequency");
        if(p.frequency_source < -1 || p.frequency_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.frequency_value)) throw std::runtime_error("Missing parameter: frequency");
        if(!std::isfinite(p.frequency_value)) throw std::runtime_error("Non-finite parameter");
        if(p.frequency_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.frequency_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.target_source)) throw std::runtime_error("Missing parameter: target");
        if(p.target_source < -1 || p.target_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.target_value)) throw std::runtime_error("Missing parameter: target");
        if(!std::isfinite(p.target_value)) throw std::runtime_error("Non-finite parameter");
        if(p.target_value<1.0) throw std::runtime_error("Parameter below minimum");
        if(p.target_value>20000.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="scale" && p.mode_value!="fixed") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.root_value)) throw std::runtime_error("Missing parameter: root");
        if(p.root_value!="0" && p.root_value!="1" && p.root_value!="2" && p.root_value!="3" && p.root_value!="4" && p.root_value!="5" && p.root_value!="6" && p.root_value!="7" && p.root_value!="8" && p.root_value!="9" && p.root_value!="10" && p.root_value!="11") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.scale_value)) throw std::runtime_error("Missing parameter: scale");
        if(p.scale_value!="chromatic" && p.scale_value!="major" && p.scale_value!="minor" && p.scale_value!="major_pentatonic" && p.scale_value!="minor_pentatonic") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.tuning_value)) throw std::runtime_error("Missing parameter: tuning");
        if(!std::isfinite(p.tuning_value)) throw std::runtime_error("Non-finite parameter");
        if(p.tuning_value<400.0) throw std::runtime_error("Parameter below minimum");
        if(p.tuning_value>480.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.hysteresis_source)) throw std::runtime_error("Missing parameter: hysteresis");
        if(p.hysteresis_source < -1 || p.hysteresis_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.hysteresis_value)) throw std::runtime_error("Missing parameter: hysteresis");
        if(!std::isfinite(p.hysteresis_value)) throw std::runtime_error("Non-finite parameter");
        if(p.hysteresis_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.hysteresis_value>12.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct HistoryBufferNodeParameters {
    int audio_in_source{};
    double seconds_value{};
    std::vector<int> sources() const { return {audio_in_source}; }
    static HistoryBufferNodeParameters read(std::istream& in) {
        HistoryBufferNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!4fe231812e443cc4") throw std::runtime_error("Incompatible parameter contract: history; rebuild Java and audio host together");
        if(!(in>>p.audio_in_source)) throw std::runtime_error("Missing parameter: audio-in");
        if(p.audio_in_source < -1 || p.audio_in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.seconds_value)) throw std::runtime_error("Missing parameter: seconds");
        if(!std::isfinite(p.seconds_value)) throw std::runtime_error("Non-finite parameter");
        if(p.seconds_value<0.005333333333333333) throw std::runtime_error("Parameter below minimum");
        if(p.seconds_value>30.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct HistoryReaderNodeParameters {
    int history_source{};
    int speed_source{};
    double speed_value{};
    int offset_source{};
    double offset_value{};
    int reset_source{};
    std::vector<int> sources() const { return {history_source,speed_source,offset_source,reset_source}; }
    static HistoryReaderNodeParameters read(std::istream& in) {
        HistoryReaderNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!b99cd9f79b33ffd5") throw std::runtime_error("Incompatible parameter contract: historyreader; rebuild Java and audio host together");
        if(!(in>>marker)||marker!="normalized-position-v3") throw std::runtime_error("Invalid parameter marker");
        if(!(in>>p.history_source)) throw std::runtime_error("Missing parameter: history");
        if(p.history_source < -1 || p.history_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.speed_source)) throw std::runtime_error("Missing parameter: speed");
        if(p.speed_source < -1 || p.speed_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.speed_value)) throw std::runtime_error("Missing parameter: speed");
        if(!std::isfinite(p.speed_value)) throw std::runtime_error("Non-finite parameter");
        if(p.speed_value<-8.0) throw std::runtime_error("Parameter below minimum");
        if(p.speed_value>8.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.offset_source)) throw std::runtime_error("Missing parameter: offset");
        if(p.offset_source < -1 || p.offset_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.offset_value)) throw std::runtime_error("Missing parameter: offset");
        if(!std::isfinite(p.offset_value)) throw std::runtime_error("Non-finite parameter");
        if(p.offset_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.offset_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.reset_source)) throw std::runtime_error("Missing parameter: reset");
        if(p.reset_source < -1 || p.reset_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct PositionReaderNodeParameters {
    int history_source{};
    int position_source{};
    double position_value{};
    int offset_source{};
    double offset_value{};
    std::vector<int> sources() const { return {history_source,position_source,offset_source}; }
    static PositionReaderNodeParameters read(std::istream& in) {
        PositionReaderNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!5eedf3ee01ec24b1") throw std::runtime_error("Incompatible parameter contract: positionreader; rebuild Java and audio host together");
        if(!(in>>p.history_source)) throw std::runtime_error("Missing parameter: history");
        if(p.history_source < -1 || p.history_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.position_source)) throw std::runtime_error("Missing parameter: position");
        if(p.position_source < -1 || p.position_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.position_value)) throw std::runtime_error("Missing parameter: position");
        if(!std::isfinite(p.position_value)) throw std::runtime_error("Non-finite parameter");
        if(p.position_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.position_value>1.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.offset_source)) throw std::runtime_error("Missing parameter: offset");
        if(p.offset_source < -1 || p.offset_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.offset_value)) throw std::runtime_error("Missing parameter: offset");
        if(!std::isfinite(p.offset_value)) throw std::runtime_error("Non-finite parameter");
        if(p.offset_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.offset_value>1.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct BinContextNodeParameters {
    int spectrum_source{};
    std::vector<int> sources() const { return {spectrum_source}; }
    static BinContextNodeParameters read(std::istream& in) {
        BinContextNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!f4337bf854f879bc") throw std::runtime_error("Incompatible parameter contract: bincontext; rebuild Java and audio host together");
        if(!(in>>p.spectrum_source)) throw std::runtime_error("Missing parameter: spectrum");
        if(p.spectrum_source < -1 || p.spectrum_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct BinOutputNodeParameters {
    int a_source{};
    double a_value{};
    int b_source{};
    double b_value{};
    std::string mode_value{};
    std::vector<int> sources() const { return {a_source,b_source}; }
    static BinOutputNodeParameters read(std::istream& in) {
        BinOutputNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!d457095baba5ab43") throw std::runtime_error("Incompatible parameter contract: binoutput; rebuild Java and audio host together");
        if(!(in>>p.a_source)) throw std::runtime_error("Missing parameter: a");
        if(p.a_source < -1 || p.a_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.a_value)) throw std::runtime_error("Missing parameter: a");
        if(!std::isfinite(p.a_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.b_source)) throw std::runtime_error("Missing parameter: b");
        if(p.b_source < -1 || p.b_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.b_value)) throw std::runtime_error("Missing parameter: b");
        if(!std::isfinite(p.b_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="cartesian" && p.mode_value!="polar") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct BinRandomNodeParameters {
    int seed_value{};
    std::string mode_value{};
    std::vector<int> sources() const { return {}; }
    static BinRandomNodeParameters read(std::istream& in) {
        BinRandomNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!4d4b486be2759e95") throw std::runtime_error("Incompatible parameter contract: binrandom; rebuild Java and audio host together");
        if(!(in>>p.seed_value)) throw std::runtime_error("Missing parameter: seed");
        if(p.seed_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.seed_value>2.147483647E9) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.mode_value)) throw std::runtime_error("Missing parameter: mode");
        if(p.mode_value!="fixed" && p.mode_value!="frame") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct BinReadNodeParameters {
    int index_source{};
    double index_value{};
    std::string boundary_value{};
    int spectrum_source{};
    std::string spectrum_bound_value{};
    std::vector<int> sources() const { return {index_source,spectrum_source}; }
    static BinReadNodeParameters read(std::istream& in) {
        BinReadNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!5305da1eed15ce74") throw std::runtime_error("Incompatible parameter contract: binread; rebuild Java and audio host together");
        if(!(in>>p.index_source)) throw std::runtime_error("Missing parameter: index");
        if(p.index_source < -1 || p.index_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.index_value)) throw std::runtime_error("Missing parameter: index");
        if(!std::isfinite(p.index_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.boundary_value)) throw std::runtime_error("Missing parameter: boundary");
        if(p.boundary_value!="zero" && p.boundary_value!="clamp" && p.boundary_value!="wrap") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.spectrum_source)) throw std::runtime_error("Missing parameter: spectrum");
        if(p.spectrum_source < -1 || p.spectrum_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.spectrum_bound_value)) throw std::runtime_error("Missing parameter: spectrum-bound");
        return p;
    }
};
struct SpectralGraphNodeParameters {
    int spectrum_source{};
    int external0_source{};
    int external1_source{};
    int external2_source{};
    int external3_source{};
    int external4_source{};
    int external5_source{};
    int external6_source{};
    int external7_source{};
    int external8_source{};
    int external9_source{};
    int external10_source{};
    int external11_source{};
    int external12_source{};
    int external13_source{};
    int external14_source{};
    int external15_source{};
    std::string program_value{};
    std::vector<int> sources() const { return {spectrum_source,external0_source,external1_source,external2_source,external3_source,external4_source,external5_source,external6_source,external7_source,external8_source,external9_source,external10_source,external11_source,external12_source,external13_source,external14_source,external15_source}; }
    static SpectralGraphNodeParameters read(std::istream& in) {
        SpectralGraphNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!779c8958a30145d1") throw std::runtime_error("Incompatible parameter contract: spectralgraph; rebuild Java and audio host together");
        if(!(in>>p.spectrum_source)) throw std::runtime_error("Missing parameter: spectrum");
        if(p.spectrum_source < -1 || p.spectrum_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external0_source)) throw std::runtime_error("Missing parameter: external0");
        if(p.external0_source < -1 || p.external0_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external1_source)) throw std::runtime_error("Missing parameter: external1");
        if(p.external1_source < -1 || p.external1_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external2_source)) throw std::runtime_error("Missing parameter: external2");
        if(p.external2_source < -1 || p.external2_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external3_source)) throw std::runtime_error("Missing parameter: external3");
        if(p.external3_source < -1 || p.external3_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external4_source)) throw std::runtime_error("Missing parameter: external4");
        if(p.external4_source < -1 || p.external4_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external5_source)) throw std::runtime_error("Missing parameter: external5");
        if(p.external5_source < -1 || p.external5_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external6_source)) throw std::runtime_error("Missing parameter: external6");
        if(p.external6_source < -1 || p.external6_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external7_source)) throw std::runtime_error("Missing parameter: external7");
        if(p.external7_source < -1 || p.external7_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external8_source)) throw std::runtime_error("Missing parameter: external8");
        if(p.external8_source < -1 || p.external8_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external9_source)) throw std::runtime_error("Missing parameter: external9");
        if(p.external9_source < -1 || p.external9_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external10_source)) throw std::runtime_error("Missing parameter: external10");
        if(p.external10_source < -1 || p.external10_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external11_source)) throw std::runtime_error("Missing parameter: external11");
        if(p.external11_source < -1 || p.external11_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external12_source)) throw std::runtime_error("Missing parameter: external12");
        if(p.external12_source < -1 || p.external12_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external13_source)) throw std::runtime_error("Missing parameter: external13");
        if(p.external13_source < -1 || p.external13_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external14_source)) throw std::runtime_error("Missing parameter: external14");
        if(p.external14_source < -1 || p.external14_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.external15_source)) throw std::runtime_error("Missing parameter: external15");
        if(p.external15_source < -1 || p.external15_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.program_value)) throw std::runtime_error("Missing parameter: program");
        return p;
    }
};
struct FFTStreamNodeParameters {
    int in_source{};
    std::string size_value{};
    std::string window_value{};
    std::string overlap_value{};
    std::vector<int> sources() const { return {in_source}; }
    static FFTStreamNodeParameters read(std::istream& in) {
        FFTStreamNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!a82e48b117c06543") throw std::runtime_error("Incompatible parameter contract: fftstream; rebuild Java and audio host together");
        if(!(in>>p.in_source)) throw std::runtime_error("Missing parameter: in");
        if(p.in_source < -1 || p.in_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.size_value)) throw std::runtime_error("Missing parameter: size");
        if(p.size_value!="256" && p.size_value!="512" && p.size_value!="1024" && p.size_value!="2048" && p.size_value!="4096" && p.size_value!="8192") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.window_value)) throw std::runtime_error("Missing parameter: window");
        if(p.window_value!="hann" && p.window_value!="hamming" && p.window_value!="blackman" && p.window_value!="rectangular") throw std::runtime_error("Unknown enum parameter");
        if(!(in>>p.overlap_value)) throw std::runtime_error("Missing parameter: overlap");
        if(p.overlap_value!="2" && p.overlap_value!="4") throw std::runtime_error("Unknown enum parameter");
        return p;
    }
};
struct IFFTNodeParameters {
    int spectrum_source{};
    std::vector<int> sources() const { return {spectrum_source}; }
    static IFFTNodeParameters read(std::istream& in) {
        IFFTNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!0aba7785f0529c4e") throw std::runtime_error("Incompatible parameter contract: ifft; rebuild Java and audio host together");
        if(!(in>>p.spectrum_source)) throw std::runtime_error("Missing parameter: spectrum");
        if(p.spectrum_source < -1 || p.spectrum_source >= 2048) throw std::runtime_error("Invalid source reference");
        return p;
    }
};
struct SpectrumShiftNodeParameters {
    int spectrum_source{};
    int shift_source{};
    double shift_value{};
    int compensate_value{};
    std::vector<int> sources() const { return {spectrum_source,shift_source}; }
    static SpectrumShiftNodeParameters read(std::istream& in) {
        SpectrumShiftNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!3fca4d6e3d3dfc84") throw std::runtime_error("Incompatible parameter contract: spectrumshift; rebuild Java and audio host together");
        if(!(in>>p.spectrum_source)) throw std::runtime_error("Missing parameter: spectrum");
        if(p.spectrum_source < -1 || p.spectrum_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.shift_source)) throw std::runtime_error("Missing parameter: shift");
        if(p.shift_source < -1 || p.shift_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.shift_value)) throw std::runtime_error("Missing parameter: shift");
        if(!std::isfinite(p.shift_value)) throw std::runtime_error("Non-finite parameter");
        if(p.shift_value<-8192.0) throw std::runtime_error("Parameter below minimum");
        if(p.shift_value>8192.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.compensate_value)) throw std::runtime_error("Missing parameter: compensate");
        if(p.compensate_value!=0 && p.compensate_value!=1) throw std::runtime_error("Boolean must be 0 or 1");
        return p;
    }
};
struct BinUniformNodeParameters {
    int slot_value{};
    std::vector<int> sources() const { return {}; }
    static BinUniformNodeParameters read(std::istream& in) {
        BinUniformNodeParameters p; std::string marker;
        if(!(in>>marker)||marker!="!86b54844d189242e") throw std::runtime_error("Incompatible parameter contract: binuniform; rebuild Java and audio host together");
        if(!(in>>p.slot_value)) throw std::runtime_error("Missing parameter: slot");
        if(p.slot_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.slot_value>15.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
struct BakedGroup6920b561e015Parameters {
    int input_0_source{};
    double input_0_value{};
    int input_1_source{};
    double input_1_value{};
    int input_2_source{};
    double input_2_value{};
    std::vector<int> sources() const { return {input_0_source,input_1_source,input_2_source}; }
    static BakedGroup6920b561e015Parameters read(std::istream& in) {
        BakedGroup6920b561e015Parameters p; std::string marker;
        if(!(in>>marker)||marker!="!0863179485ffe43d") throw std::runtime_error("Incompatible parameter contract: baked_6920b561e015; rebuild Java and audio host together");
        if(!(in>>p.input_0_source)) throw std::runtime_error("Missing parameter: input_0");
        if(p.input_0_source < -1 || p.input_0_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input_0_value)) throw std::runtime_error("Missing parameter: input_0");
        if(!std::isfinite(p.input_0_value)) throw std::runtime_error("Non-finite parameter");
        if(p.input_0_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.input_0_value>256.0) throw std::runtime_error("Parameter above maximum");
        if(!(in>>p.input_1_source)) throw std::runtime_error("Missing parameter: input_1");
        if(p.input_1_source < -1 || p.input_1_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input_1_value)) throw std::runtime_error("Missing parameter: input_1");
        if(!std::isfinite(p.input_1_value)) throw std::runtime_error("Non-finite parameter");
        if(!(in>>p.input_2_source)) throw std::runtime_error("Missing parameter: input_2");
        if(p.input_2_source < -1 || p.input_2_source >= 2048) throw std::runtime_error("Invalid source reference");
        if(!(in>>p.input_2_value)) throw std::runtime_error("Missing parameter: input_2");
        if(!std::isfinite(p.input_2_value)) throw std::runtime_error("Non-finite parameter");
        if(p.input_2_value<0.0) throw std::runtime_error("Parameter below minimum");
        if(p.input_2_value>4.0) throw std::runtime_error("Parameter above maximum");
        return p;
    }
};
}}
