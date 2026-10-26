#pragma once
#include "ParameterContracts.hpp"
#include "../AverageMeasurement.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
/** Statistical analysis resource: mean power or mean correlation, never mean phase.
 * Storage is reserved during preparation. Only distinct delivered frames count. */
struct AnalysisAverageNode : NodeProcessor {
    AverageMeasurement measurement;
    AnalysisResource resource;
    std::unique_ptr<AnalysisResult> result=std::make_unique<AnalysisResult>();
    std::vector<double> mean;
    std::uint64_t seenIdentity=0,seenSequence=0,seenEnd=0,sourceIdentity=0;
    std::uint64_t seenStreamIdentity=0,seenStreamEnd=0;
    bool correlation=false,hasResult=false;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 3;}
    bool writesEventBuffer(std::size_t p)const noexcept override{return p==2;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::analysis:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::analysis:ResourceKind::none;}
    DspResource* resourceOutput(std::size_t p)noexcept override{return p==0?&resource:nullptr;}
    void bindIdentity(const std::string& id)override{measurement.start.bind(id+":start");}
    void shareChannelState(NodeProcessor& p)override{measurement.start=static_cast<AnalysisAverageNode&>(p).measurement.start;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    void prepare(double,std::size_t)override{mean.resize(AnalysisResult::count);resource.result=nullptr;}
    void configure(std::istream& in)override{auto p=parameters::AnalysisAverageNodeParameters::read(in);sources=p.sources();measurement.configure(p);}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{measurement.parameters(static_cast<const AnalysisAverageNode&>(p).measurement);}
    void adoptState(NodeProcessor& old)noexcept override{
        auto& p=static_cast<AnalysisAverageNode&>(old);auto settings=measurement;
        measurement=p.measurement;measurement.parameters(settings);mean.swap(p.mean);result.swap(p.result);
        seenIdentity=p.seenIdentity;seenSequence=p.seenSequence;seenEnd=p.seenEnd;sourceIdentity=p.sourceIdentity;
        seenStreamIdentity=p.seenStreamIdentity;seenStreamEnd=p.seenStreamEnd;
        correlation=p.correlation;hasResult=p.hasResult;resource.view=p.resource.view;resource.result=hasResult?result.get():nullptr;
    }
    void begin(double rate)noexcept{
        measurement.begin(rate);hasResult=false;resource.result=nullptr;result->sequence=0;
        result->historyIdentity=HistoryResource::nextIdentity.fetch_add(1);
        result->spectrum.fftFrames=0;
    }
    bool compatible(bool corr,std::uint64_t identity,std::size_t fft,std::size_t win,std::size_t count,
            double rate,SpectrumWindow shape,bool dc,std::size_t hop)noexcept{
        if(measurement.count&&(correlation!=corr||sourceIdentity!=identity||result->fftFrames!=fft||result->windowFrames!=win||
            result->sampleRate!=rate||result->spectrum.window!=shape||result->spectrum.dcRemoved!=dc||result->spectrum.hopFrames!=hop||
            (corr?result->correlationCount:result->binCount)!=count)){
            // A changed FFT/source is a different experiment, not another sample.
            measurement.active=false;measurement.count=0;hasResult=false;resource.result=nullptr;return false;
        }
        correlation=corr;sourceIdentity=identity;return true;
    }
    void statistics(unsigned view,std::size_t count,double spacing)noexcept{
        double peak=0,sum=0,squares=0;
        for(std::size_t k=0;k<count;k++){double v=result->views[view][k];peak=std::max(peak,std::abs(v));sum+=v;squares+=v*v;}
        result->statistics[view]={peak,count?sum/count:0,count?std::sqrt(squares/count):0,sum*spacing};
    }
    void spectrum(const std::complex<double>* bins,std::size_t fft,std::size_t win,std::size_t hop,
            double rate,std::uint64_t identity,std::uint64_t end,SpectrumWindow shape,bool dc)noexcept{
        auto count=fft/2+1;if(fft<2||fft>SpectrumFrame::capacity||!win||rate<=0)return;
        for(std::size_t k=0;k<count;k++)if(!std::isfinite(std::norm(bins[k])))return;
        if(!compatible(false,identity,fft,win,count,rate,shape,dc,hop))return;
        bool first=measurement.count==0;measurement.record();auto& f=result->spectrum;
        if(first){
            f.windowSum=f.windowSumSquares=0;
            for(std::size_t k=0;k<win;k++){double w=spectrumWindowValue(shape,k,win);f.windowSum+=w;f.windowSumSquares+=w*w;}
        }
        f.fftFrames=fft;f.windowFrames=win;f.hopFrames=hop;f.sampleRate=rate;f.window=shape;f.dcRemoved=dc;
        f.sourceIdentity=result->historyIdentity;f.endSample=end;f.startSample=end>=win?end-win:0;f.frameIndex=measurement.count;
        result->fftFrames=fft;result->windowFrames=win;result->sampleRate=rate;result->binCount=count;result->correlationCount=0;
        result->binSpacingHz=rate/fft;result->lagSpacingSeconds=0;result->availableViews=5;
        result->frequency=result->confidence=0;result->correlationTime=0;
        result->estimator="Arithmetic mean power spectrum (no phase)";
        for(std::size_t k=0;k<count;k++){
            double power=std::norm(bins[k]);mean[k]=first?power:mean[k]+(power-mean[k])/measurement.count;
            double amplitude=std::sqrt(std::max(0.,mean[k]));f.bins[k]={amplitude,0};
            if(k&&k<fft/2)f.bins[fft-k]={amplitude,0};
            double magnitude=amplitude*(k==0||k==fft/2?1:2)/std::max(f.windowSum,1e-300);
            result->views[0][k]=static_cast<float>(std::min(magnitude,double(std::numeric_limits<float>::max())));
            result->views[2][k]=static_cast<float>(std::min(magnitude*magnitude,double(std::numeric_limits<float>::max())));
        }
        resource.view=AnalysisView::magnitude;statistics(0,count,result->binSpacingHz);statistics(2,count,result->binSpacingHz);publish(end);
    }
    void correlate(const AnalysisResult& r)noexcept{
        auto count=r.correlationCount;if(!(r.availableViews&8)||!count||count>mean.size())return;
        for(std::size_t k=0;k<count;k++)if(!std::isfinite(r.views[3][k]))return;
        if(!compatible(true,r.historyIdentity,r.fftFrames,r.windowFrames,count,r.sampleRate,r.spectrum.window,r.spectrum.dcRemoved,r.spectrum.hopFrames))return;
        bool first=measurement.count==0;measurement.record();
        result->fftFrames=r.fftFrames;result->windowFrames=r.windowFrames;result->sampleRate=r.sampleRate;result->correlationCount=count;result->binCount=0;
        double power=r.correlationRms*r.correlationRms,previous=result->correlationRms*result->correlationRms;
        result->correlationRms=std::sqrt(first?power:std::max(0.,previous+(power-previous)/measurement.count));
        result->spectrum.fftFrames=0;result->spectrum.window=r.spectrum.window;result->spectrum.dcRemoved=r.spectrum.dcRemoved;result->spectrum.hopFrames=r.spectrum.hopFrames;
        result->lagSpacingSeconds=r.lagSpacingSeconds;result->binSpacingHz=0;result->availableViews=8;result->frequency=result->confidence=0;
        result->estimator="Arithmetic mean autocorrelation";result->correlationTime=0;
        bool positive=true;
        for(std::size_t k=0;k<count;k++){
            double v=r.views[3][k];mean[k]=first?v:mean[k]+(v-mean[k])/measurement.count;result->views[3][k]=static_cast<float>(mean[k]);
            if(mean[k]<=0)positive=false;if(positive)result->correlationTime+=mean[k]*r.lagSpacingSeconds;
        }
        resource.view=AnalysisView::correlation;statistics(3,count,r.lagSpacingSeconds);publish(r.endSample);
    }
    void publish(std::uint64_t end)noexcept{result->endSample=end;++result->sequence;hasResult=true;resource.result=result.get();}
    void process(ProcessContext& c)noexcept override{
        TriggerInput start(c,1,measurement.start);AverageMeasurement::clearFinished(c);auto* count=c.outputAt(1);
        auto* input=c.resource(0);auto* stream=input&&input->kind()==ResourceKind::spectrum?static_cast<const SpectrumStream*>(input):nullptr;
        auto* snapshot=input&&!stream?static_cast<const AnalysisResource*>(input)->analysisResult():nullptr;
        bool fresh=snapshot&&(snapshot->historyIdentity!=seenIdentity||snapshot->sequence!=seenSequence||snapshot->endSample!=seenEnd);
        if(snapshot){seenIdentity=snapshot->historyIdentity;seenSequence=snapshot->sequence;seenEnd=snapshot->endSample;}
        std::size_t cursor=0;
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool beginning=start.at(i);
            if(beginning){begin(c.sampleRate);fresh=false;}
            if(stream){
                while(cursor<stream->count){auto& f=stream->frames[cursor];auto end=f.start+static_cast<std::int64_t>(stream->windowFrames());
                    if(end>static_cast<std::int64_t>(stream->blockStart+i+1))break;++cursor;
                    if(f.start<0)continue;
                    bool newFrame=stream->identity!=seenStreamIdentity||static_cast<std::uint64_t>(end)>seenStreamEnd;
                    if(!newFrame)continue;
                    seenStreamIdentity=stream->identity;seenStreamEnd=static_cast<std::uint64_t>(end);
                    // Async FFT uses the same stream type, but delivers old capture timestamps.
                    // Accept these at delivery; a stale frame present at Start is only a baseline.
                    bool delayed=end<=static_cast<std::int64_t>(stream->blockStart);
                    if(measurement.active&&!(beginning&&delayed))
                        spectrum(f.bins.data(),stream->size,stream->windowFrames(),stream->hop,stream->sampleRate,stream->identity,static_cast<std::uint64_t>(end),stream->window,stream->dcRemoved);
                }
            }else if(i==0&&fresh&&measurement.active){
                if(snapshot->spectrum.valid()){auto& f=snapshot->spectrum;spectrum(f.bins.data(),f.fftFrames,f.windowFrames,f.hopFrames,f.sampleRate,snapshot->historyIdentity,f.endSample,f.window,f.dcRemoved);}
                else correlate(*snapshot);
            }
            if(measurement.tick())AverageMeasurement::finish(c,i);
            if(c.output)c.output[i]=0;if(count)count[i]=static_cast<float>(measurement.count);
        }
    }
};
EASYKNACK_NODE(AnalysisAverageNode,"analysisaverage")
}
