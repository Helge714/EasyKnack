#pragma once
#include "DspResources.hpp"
#include "Spectrum.hpp"
namespace easyknack {
enum class AnalysisView { magnitude, phase, power, correlation };
struct AnalysisResult {
    static constexpr std::size_t count=SpectrumFrame::capacity/2+1; // Padded 64k spectrum, including Nyquist; also holds all 64k positive lags.
    std::uint64_t sequence=0,endSample=0,historyIdentity=0;
    double sampleRate=0,lagSpacingSeconds=0,binSpacingHz=0;
    std::size_t windowFrames=8192,fftFrames=16384;
    float frequency=0,confidence=0;
    // One shared FFT, with cached derived views. Correlation has all positive lags.
    OwnedArray<std::array<float,count>,4> views{};
    std::array<std::array<double,4>,4> statistics{}; // peak, mean, RMS, integral
    double correlationTime=0;
    const char* estimator="DC-centered, unbiased linear autocorrelation";
    SpectrumFrame spectrum; // New FFT snapshots preserve raw complex coefficients.
    std::size_t binCount=8192,correlationCount=8192;
    unsigned availableViews=15; // magnitude, phase, power, correlation
};
struct AnalysisResource : DspResource {
    mutable const AnalysisResult* result=nullptr;AnalysisView view=AnalysisView::magnitude;
    virtual const AnalysisResult* analysisResult()const noexcept{return result;}
    ResourceKind kind()const noexcept override{return ResourceKind::analysis;}
};

}
