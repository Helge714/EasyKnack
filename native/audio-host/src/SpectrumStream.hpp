#pragma once
#include "AnalysisData.hpp"
#include <memory>
#include <limits>
#include "Spectrum.hpp"
#include "RenderCadence.hpp"
namespace easyknack {
/** Complete audio-thread STFT batches. All storage is reserved before playback. */
struct StreamSpectrumFrame {
    std::int64_t start=0;
    std::vector<std::complex<double>> bins;
};
struct SpectrumStream final : AnalysisResource {
    static constexpr std::size_t maxSize=16384,minHop=64;
    std::size_t size=0,windowSize=0,hop=0,count=0,blockFrames=0;
    bool dcRemoved=false;
    mutable std::unique_ptr<AnalysisResult> analysisCache;
    std::uint64_t identity=0,blockStart=0;
    double sampleRate=0;
    SpectrumWindow window=SpectrumWindow::hann;
    std::vector<StreamSpectrumFrame> frames;
    ResourceKind kind()const noexcept override{return ResourceKind::spectrum;}
    void prepare(std::size_t maximumBlock){
        analysisCache=std::make_unique<AnalysisResult>();result=nullptr;
        frames.resize((maximumBlock+minHop-1)/minHop+1);
        for(auto& frame:frames)frame.bins.resize(maxSize);
        count=0;
    }
    void metadata(const SpectrumStream& other)noexcept{
        RenderCadence::include(other.hop);
        size=other.size;windowSize=other.windowSize;dcRemoved=other.dcRemoved;hop=other.hop;count=other.count;blockFrames=other.blockFrames;identity=other.identity;blockStart=other.blockStart;sampleRate=other.sampleRate;window=other.window;
    }
    void copy(const SpectrumStream& other)noexcept{
        metadata(other);count=std::min(count,frames.size());
        for(std::size_t f=0;f<count;f++){frames[f].start=other.frames[f].start;std::copy_n(other.frames[f].bins.data(),size,frames[f].bins.data());}
    }
    std::size_t windowFrames()const noexcept{return windowSize?windowSize:size;}
    const AnalysisResult* analysisResult()const noexcept override{
        if(!size||!analysisCache)return nullptr;
        if(!count)return result&&result->historyIdentity==identity?result:nullptr;
        const auto& latest=frames[count-1];if(latest.start<0)return nullptr;
        auto end=static_cast<std::uint64_t>(latest.start)+windowFrames();
        if(result&&result->historyIdentity==identity&&result->endSample==end)return result;
        auto& value=*analysisCache;auto& frame=value.spectrum;
        frame.fftFrames=size;frame.windowFrames=windowFrames();frame.hopFrames=hop;frame.window=window;
        frame.startSample=latest.start;frame.endSample=end;frame.frameIndex=hop?end/hop:0;
        frame.sampleRate=sampleRate;frame.sourceIdentity=identity;frame.dcRemoved=dcRemoved;
        frame.windowSum=frame.windowSumSquares=0;
        for(std::size_t i=0;i<frame.windowFrames;i++){double w=spectrumWindowValue(window,i,frame.windowFrames);frame.windowSum+=w;frame.windowSumSquares+=w*w;}
        std::copy_n(latest.bins.data(),size,frame.bins.data());
        value.sequence=end;value.endSample=end;value.historyIdentity=identity;value.sampleRate=sampleRate;
        value.windowFrames=frame.windowFrames;value.fftFrames=size;value.binCount=size/2+1;value.correlationCount=0;value.availableViews=7;
        value.binSpacingHz=sampleRate/size;value.lagSpacingSeconds=0;value.frequency=value.confidence=0;value.correlationTime=0;
        value.estimator="Complex FFT (no pitch estimation)";
        for(std::size_t i=0;i<value.binCount;i++){
            double scale=(i==0||i==size/2?1:2)/frame.windowSum;
            double magnitude=std::abs(frame.bins[i])*scale;
            value.views[0][i]=static_cast<float>(std::min(magnitude,static_cast<double>(std::numeric_limits<float>::max())));value.views[1][i]=static_cast<float>(std::arg(frame.bins[i]));value.views[2][i]=static_cast<float>(std::min(magnitude*magnitude,static_cast<double>(std::numeric_limits<float>::max())));
        }
        for(unsigned v=0;v<3;v++){
            double peak=0,sum=0,squares=0;
            for(std::size_t i=0;i<value.binCount;i++){double x=value.views[v][i];peak=std::max(peak,std::abs(x));sum+=x;squares+=x*x;}
            value.statistics[v]={peak,sum/value.binCount,std::sqrt(squares/value.binCount),sum*value.binSpacingHz};
        }
        result=&value;return result;
    }
    static void realSymmetry(StreamSpectrumFrame& frame,std::size_t size)noexcept{
        frame.bins[0]={frame.bins[0].real(),0};frame.bins[size/2]={frame.bins[size/2].real(),0};
        for(std::size_t k=1;k<size/2;k++)frame.bins[size-k]=std::conj(frame.bins[k]);
    }
};
}
