#pragma once
#include "AnalysisData.hpp"
#include "nodes/AutocorrelationNode.hpp"
#include "Spectrum.hpp"
#include <mutex>
#include <thread>
#include <chrono>
namespace easyknack {
struct AnalysisJob {
    struct Request {
        std::atomic<int> state{0}; // free, ready, working
        std::array<float,8192> values{};
        double rate=0;std::uint64_t end=0,identity=0,ordinal=0;float minimum=20,maximum=20000;
        bool spectral=false,correlate=false,padded=false,removeDc=false,unbiased=false;std::size_t size=8192,hop=4096;
        SpectrumFrame inputSpectrum;
        double smoothing=1;
        SpectrumWindow window=SpectrumWindow::hann;
    };
    struct Result {std::atomic<int> state{0};AnalysisResult value;}; // free, writing, ready, borrowed
    std::array<Request,3> requests;
    std::array<Result,3> results;
    AutocorrelationNode analyzer;
    std::array<std::complex<double>,16384> spectrum{};
    std::atomic<std::uint64_t> dropped{0};
    std::uint64_t sequence=0,lastOrdinal=0,submitted=0;
    // Worker-owned temporal average; never shared mutably with the audio thread.
    std::array<double,8192> smoothed{};
    bool smoothingReady=false,smoothedUnbiased=false,smoothedDc=false;
    std::uint64_t smoothedIdentity=0,smoothedEnd=0;
    std::size_t smoothedSize=0,smoothedFftSize=0;
    SpectrumWindow smoothedWindow=SpectrumWindow::hann;
    double smoothedRate=0;
    int borrowed=-1; // audio thread only
    bool submit(const HistoryResource& history,float minimum,float maximum)noexcept {
        if(history.end<8192||history.samples.size()<8192)return false;
        for(auto& r:requests)if(r.state.load(std::memory_order_acquire)==0){
            for(unsigned i=0;i<8192;i++)r.values[i]=history.sample(static_cast<double>(history.end-8192+i));
            r.rate=history.sampleRate;r.end=history.end;r.identity=history.identity;r.ordinal=++submitted;r.minimum=minimum;r.maximum=maximum;
            r.spectral=false;r.correlate=false;
            r.state.store(1,std::memory_order_release);return true;
        }
        dropped.fetch_add(1,std::memory_order_relaxed);return false;
    }
    bool submitSpectrum(const float* ring,std::size_t cursor,std::size_t size,std::size_t hop,
            SpectrumWindow window,double rate,std::uint64_t end,std::uint64_t identity,bool padded=false,bool removeDc=false)noexcept{
        if(!ring||size<2||size>8192||(size&(size-1))||!hop||end<size||!std::isfinite(rate)||rate<=0)return false;
        for(auto& r:requests)if(r.state.load(std::memory_order_acquire)==0){
            for(std::size_t i=0;i<size;i++)r.values[i]=ring[(cursor+i)%size];
            r.spectral=true;r.correlate=false;r.padded=padded;r.removeDc=removeDc;r.size=size;r.hop=hop;r.window=window;r.rate=rate;r.end=end;r.identity=identity;r.ordinal=++submitted;
            r.state.store(1,std::memory_order_release);return true;
        }
        dropped.fetch_add(1,std::memory_order_relaxed);return false;
    }
    bool submitCorrelation(const SpectrumFrame& frame,bool unbiased=false,std::uint64_t identity=0,double smoothing=1)noexcept{
        if(!frame.valid()||frame.fftFrames>SpectrumFrame::capacity||(frame.fftFrames&(frame.fftFrames-1))
            ||!frame.windowFrames||frame.windowFrames>8192||frame.windowFrames>frame.fftFrames
            ||!std::isfinite(frame.sampleRate)||frame.sampleRate<=0||!std::isfinite(smoothing)||smoothing<0||smoothing>1)return false;
        for(auto& r:requests)if(r.state.load(std::memory_order_acquire)==0){
            r.inputSpectrum=frame;r.smoothing=smoothing;r.unbiased=unbiased;r.correlate=true;r.spectral=false;r.rate=frame.sampleRate;
            r.end=frame.endSample;r.identity=identity?identity:frame.sourceIdentity;r.ordinal=++submitted;
            r.state.store(1,std::memory_order_release);return true;
        }
        dropped.fetch_add(1,std::memory_order_relaxed);return false;
    }
    void analyzeCorrelation(const Request& request,AnalysisResult& result)noexcept{
        const auto& frame=request.inputSpectrum;
        for(std::size_t i=0;i<frame.fftFrames;i++)spectrum[i]=std::norm(frame.bins[i]);
        spectrumTransform(spectrum.data(),frame.fftFrames,true);
        result.availableViews=8;result.spectrum.fftFrames=0;
        result.windowFrames=frame.windowFrames;result.fftFrames=frame.fftFrames;
        result.correlationCount=frame.windowFrames;result.binCount=0;
        result.lagSpacingSeconds=1/frame.sampleRate;result.binSpacingHz=0;
        result.frequency=result.confidence=0;result.correlationTime=0;
        result.estimator=frame.fftFrames>=2*frame.windowFrames-1?"Windowed linear autocorrelation (lag-zero normalized)":"Windowed circular autocorrelation (lag-zero normalized)";
        if(request.unbiased&&frame.fftFrames>=2*frame.windowFrames-1)result.estimator="Windowed linear autocorrelation (unbiased, clamped)";
        for(auto& view:result.views)view.fill(0);
        for(auto& stats:result.statistics)stats.fill(0);
        double energy=spectrum[0].real(),peak=0,sum=0,squares=0;
        bool reset=!smoothingReady||smoothedIdentity!=frame.sourceIdentity||smoothedSize!=frame.windowFrames
            ||smoothedFftSize!=frame.fftFrames||smoothedWindow!=frame.window||smoothedDc!=frame.dcRemoved
            ||smoothedRate!=frame.sampleRate||smoothedUnbiased!=request.unbiased||frame.endSample<smoothedEnd;
        for(std::size_t i=0;i<result.correlationCount;i++){
            double correction=request.unbiased&&frame.fftFrames>=2*frame.windowFrames-1?static_cast<double>(frame.windowFrames)/(frame.windowFrames-i):1;
            double value=energy>1e-30?std::clamp(spectrum[i].real()/energy*correction,-1.,1.):0;
            if(!reset&&request.smoothing<1)value=frame.endSample==smoothedEnd?smoothed[i]:smoothed[i]*(1-request.smoothing)+value*request.smoothing;
            smoothed[i]=value;
            result.views[3][i]=static_cast<float>(value);peak=std::max(peak,std::abs(value));sum+=value;squares+=value*value;
        }
        smoothingReady=true;smoothedIdentity=frame.sourceIdentity;smoothedEnd=frame.endSample;
        smoothedSize=frame.windowFrames;smoothedFftSize=frame.fftFrames;smoothedWindow=frame.window;
        smoothedDc=frame.dcRemoved;smoothedRate=frame.sampleRate;smoothedUnbiased=request.unbiased;
        auto count=result.correlationCount;
        result.statistics[3]={peak,sum/count,std::sqrt(squares/count),sum/frame.sampleRate};
        for(std::size_t i=0;i<count&&result.views[3][i]>0;i++)result.correlationTime+=result.views[3][i]/frame.sampleRate;
    }
    static void analyzeSpectrum(const Request& request,AnalysisResult& result)noexcept{
        auto& frame=result.spectrum;result.availableViews=7;
        frame.windowFrames=request.size;frame.fftFrames=request.size*(request.padded?2:1);frame.hopFrames=request.hop;frame.window=request.window;
        frame.sampleRate=request.rate;frame.startSample=request.end-request.size;frame.endSample=request.end;
        frame.frameIndex=frame.startSample/request.hop;frame.sourceIdentity=request.identity;
        frame.windowSum=frame.windowSumSquares=0;frame.dcRemoved=request.removeDc;
        double mean=0;if(request.removeDc){for(std::size_t i=0;i<request.size;i++)mean+=request.values[i];mean/=request.size;}
        for(std::size_t i=0;i<request.size;i++){
            double weight=spectrumWindowValue(request.window,i,request.size);
            frame.windowSum+=weight;frame.windowSumSquares+=weight*weight;
            frame.bins[i]=(static_cast<double>(request.values[i])-mean)*weight;
        }
        std::fill(frame.bins.begin()+request.size,frame.bins.begin()+frame.fftFrames,std::complex<double>{});
        spectrumTransform(frame.bins.data(),frame.fftFrames);
        result.windowFrames=request.size;result.fftFrames=frame.fftFrames;result.binCount=frame.fftFrames/2+1;
        result.binSpacingHz=request.rate/frame.fftFrames;result.lagSpacingSeconds=0;
        result.frequency=result.confidence=0;result.correlationTime=0;result.estimator="Complex FFT (no pitch estimation)";
        for(auto& view:result.views)view.fill(0);
        for(std::size_t i=0;i<result.binCount;i++){
            double scale=(i==0||i==frame.fftFrames/2?1:2)/frame.windowSum;
            double magnitude=std::abs(frame.bins[i])*scale;
            result.views[0][i]=static_cast<float>(std::min(magnitude,static_cast<double>(std::numeric_limits<float>::max())));
            result.views[1][i]=static_cast<float>(std::arg(frame.bins[i]));
            result.views[2][i]=static_cast<float>(std::min(magnitude*magnitude,static_cast<double>(std::numeric_limits<float>::max())));
        }
        for(unsigned view=0;view<4;view++){
            double peak=0,sum=0,squares=0;
            for(std::size_t i=0;i<result.binCount;i++){double value=result.views[view][i];peak=std::max(peak,std::abs(value));sum+=value;squares+=value*value;}
            result.statistics[view]={peak,sum/result.binCount,std::sqrt(squares/result.binCount),sum*result.binSpacingHz};
        }
    }
    const AnalysisResult* acquire()noexcept{
        int latest=-1;std::uint64_t seq=borrowed<0?0:results[borrowed].value.sequence;
        for(int i=0;i<3;i++)if(results[i].state.load(std::memory_order_acquire)==2&&results[i].value.sequence>seq){latest=i;seq=results[i].value.sequence;}
        if(latest>=0){results[latest].state.store(3,std::memory_order_release);if(borrowed>=0)results[borrowed].state.store(0,std::memory_order_release);borrowed=latest;}
        for(int i=0;i<3;i++)if(i!=borrowed){int ready=2;results[i].state.compare_exchange_strong(ready,0);}
        return borrowed<0?nullptr:&results[borrowed].value;
    }
    void work(){ // reusable worker only
        for(auto& request:requests){int ready=1;if(!request.state.compare_exchange_strong(ready,2))continue;
            if(request.ordinal<=lastOrdinal){request.state.store(0,std::memory_order_release);continue;}
            Result* destination=nullptr;
            for(auto& result:results){int free=0;if(result.state.compare_exchange_strong(free,1)){destination=&result;break;}}
            if(!destination){dropped.fetch_add(1);request.state.store(0,std::memory_order_release);continue;}
            if(request.spectral||request.correlate){
                auto& result=destination->value;
                if(request.correlate)analyzeCorrelation(request,result);else analyzeSpectrum(request,result);
                lastOrdinal=request.ordinal;
                result.sequence=++sequence;result.endSample=request.end;result.historyIdentity=request.identity;result.sampleRate=request.rate;
                destination->state.store(2,std::memory_order_release);request.state.store(0,std::memory_order_release);continue;
            }
            analyzer.samples=request.values;analyzer.writeCursor=0;analyzer.filledSamples=8192;
            analyzer.minimum=request.minimum;analyzer.maximum=request.maximum;analyzer.spectralSnapshot=spectrum.data();
            spectrum.fill({});analyzer.analyze(request.rate);lastOrdinal=request.ordinal;
            auto& result=destination->value;result.sequence=++sequence;result.endSample=request.end;result.historyIdentity=request.identity;result.sampleRate=request.rate;result.lagSpacingSeconds=1/request.rate;result.binSpacingHz=request.rate/16384;
            result.availableViews=15;result.spectrum.fftFrames=0;result.binCount=8192;result.windowFrames=8192;result.fftFrames=16384;
            result.frequency=analyzer.frequency;result.confidence=analyzer.confidence;
            for(std::size_t i=0;i<8192;i++){
                double power=std::norm(spectrum[i]);
                result.views[0][i]=static_cast<float>(std::sqrt(power)/8192);
                result.views[1][i]=static_cast<float>(std::arg(spectrum[i]));
                result.views[2][i]=static_cast<float>(std::min(power/(8192.*8192.),static_cast<double>(std::numeric_limits<float>::max())));
                result.views[3][i]=analyzer.correlation[i];
            }
            for(unsigned view=0;view<4;view++){
                double peak=0,sum=0,squares=0;
                for(std::size_t i=0;i<8192;i++){float value=result.views[view][i];peak=std::max(peak,std::abs(static_cast<double>(value)));sum+=value;squares+=static_cast<double>(value)*value;}
                double spacing=view==3?1/request.rate:request.rate/16384;
                result.statistics[view]={peak,sum/8192,std::sqrt(squares/8192),sum*spacing};
            }
            result.correlationTime=0;
            for(float value:result.views[3]){if(value<=0)break;result.correlationTime+=value/request.rate;}
            destination->state.store(2,std::memory_order_release);request.state.store(0,std::memory_order_release);
        }
    }
};
/** Registration and reclamation are control/worker operations. Audio only accesses
 * its preallocated job slots and never takes this mutex or waits for the worker. */
class AnalysisWorker {
    std::mutex mutex;std::vector<std::weak_ptr<AnalysisJob>> jobs;
    std::atomic<bool> stop{false};std::thread thread;
    AnalysisWorker():thread([this]{
        std::vector<std::shared_ptr<AnalysisJob>> active;
        while(!stop.load()){
            active.clear();{std::lock_guard<std::mutex> lock(mutex);
                jobs.erase(std::remove_if(jobs.begin(),jobs.end(),[](auto& j){return j.expired();}),jobs.end());
                for(auto& j:jobs)if(auto p=j.lock())active.push_back(std::move(p));}
            for(auto& job:active)job->work();
            std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }
    }){}
public:
    ~AnalysisWorker(){stop.store(true);thread.join();}
    static AnalysisWorker& instance(){static AnalysisWorker worker;return worker;}
    std::shared_ptr<AnalysisJob> create(){auto job=std::make_shared<AnalysisJob>();std::lock_guard<std::mutex> lock(mutex);jobs.push_back(job);return job;}
};
}
