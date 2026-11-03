#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
/** One FFT resource for analysis and resynthesis. Worker results retain their timestamps. */
struct SpectrumNode : NodeProcessor {
    SpectrumStream resource;
    std::shared_ptr<AnalysisJob> job;
    std::vector<float> ring;
    std::vector<double> weights;
    std::size_t size=2048,hop=512,cursor=0;
    SpectrumWindow window=SpectrumWindow::hann;
    bool mix=false,right=false,all=true,removeDc=false,padded=false,synchronous=true;
    double rate=0,intervalMs=0;
    std::uint64_t lastSubmission=0;
    std::size_t effectiveHop()const noexcept{return synchronous?hop:std::max(hop,static_cast<std::size_t>(std::ceil(rate*intervalMs/1000.)));}
    std::uint64_t end=0,received=0,identity=HistoryResource::nextIdentity.fetch_add(1);
    bool canShareChannels()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return !all;}
    bool mergesVoices()const noexcept override{return !all;}
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    char executionRate()const noexcept override{return 'a';}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &resource;}
    static std::size_t hopFor(std::size_t size,double overlap)noexcept{
        return std::clamp<std::size_t>(static_cast<std::size_t>(std::llround(size*(1-std::clamp(overlap,0.,1.)))),1,size);
    }
    void configure(std::istream& in)override{
        auto position=in.tellg();std::string marker;in>>marker;
        if(marker=="!df5db079f2749a23"||marker=="!5c7d4aae074fd80b"||marker=="!a701056f3c7961e7"||marker=="!d4eac824a0d8961f"){
            int source;std::string length,shape,overlap,channel;
            if(!(in>>source>>length>>shape>>overlap>>channel)||source< -1||source>=2048)throw std::runtime_error("Invalid legacy FFT parameters");
            sources={source};size=std::stoul(length);window=spectrumWindow(shape);right=channel=="right";mix=channel=="mix";all=false;synchronous=false;
            double amount=marker!="!df5db079f2749a23"?std::stod(overlap):overlap=="none"?0:overlap=="half"?.5:.75;
            if(!std::isfinite(amount)||amount<0||amount>1)throw std::runtime_error("Invalid FFT overlap");hop=hopFor(size,amount);
            if(marker=="!a701056f3c7961e7"||marker=="!d4eac824a0d8961f"){std::string padding;if(!(in>>padding)||(padding!="none"&&padding!="double"))throw std::runtime_error("Invalid FFT padding");padded=padding=="double";}
            if(marker=="!d4eac824a0d8961f"){int dc;if(!(in>>dc)||(dc!=0&&dc!=1))throw std::runtime_error("Invalid FFT DC option");removeDc=dc!=0;}
        }else{
            in.seekg(position);auto p=parameters::SpectrumNodeParameters::read(in);sources=p.sources();size=std::stoul(p.size_value);
            window=spectrumWindow(p.window_value);hop=hopFor(size,p.overlap_value);right=p.channel_value=="right";mix=p.channel_value=="mix";all=p.channel_value=="all";
            padded=p.padding_value=="double";removeDc=p.remove_dc_value;synchronous=p.synchronous_value;intervalMs=p.interval_ms_value;
        }
        if(!std::isfinite(intervalMs)||intervalMs<0||intervalMs>60000)throw std::runtime_error("Invalid FFT frame interval");
        if(size<64||size>SpectrumFrame::maxWindowSize||(size&(size-1)))throw std::runtime_error("Invalid FFT size");
        if(synchronous)hop=std::max(hop,SpectrumStream::minimumHop(size));
    }
    void prepare(double sampleRate,std::size_t block)override{
        rate=sampleRate;resource.prepare(block);ring.assign(SpectrumFrame::maxWindowSize,0);weights.resize(SpectrumFrame::maxWindowSize);
        for(std::size_t i=0;i<size;i++)weights[i]=spectrumWindowValue(window,i,size);
        job=AnalysisWorker::instance().create();
        resource.size=size*(padded?2:1);resource.windowSize=size;resource.hop=effectiveHop();resource.window=window;resource.dcRemoved=removeDc;resource.sampleRate=rate;
        resource.identity=identity;cursor=0;end=received=lastSubmission=0;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& configuration)noexcept override{
        const auto& p=static_cast<const SpectrumNode&>(configuration);
        if(size==p.size&&hop==p.hop&&window==p.window&&all==p.all&&right==p.right&&mix==p.mix&&padded==p.padded&&removeDc==p.removeDc&&synchronous==p.synchronous&&intervalMs==p.intervalMs)return;
        if(size!=p.size||window!=p.window||all!=p.all||right!=p.right||mix!=p.mix||synchronous!=p.synchronous){cursor=0;end=0;std::fill(ring.begin(),ring.end(),0);}
        size=p.size;hop=p.hop;window=p.window;all=p.all;right=p.right;mix=p.mix;padded=p.padded;removeDc=p.removeDc;synchronous=p.synchronous;intervalMs=p.intervalMs;lastSubmission=0;
        for(std::size_t i=0;i<size;i++)weights[i]=spectrumWindowValue(window,i,size);
        identity=HistoryResource::nextIdentity.fetch_add(1);received=0;resource.result=nullptr;resource.count=0;
        resource.identity=identity;resource.size=size*(padded?2:1);resource.windowSize=size;resource.hop=effectiveHop();resource.window=window;resource.dcRemoved=removeDc;
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<SpectrumNode&>(previous);
        if(size==p.size&&hop==p.hop&&window==p.window&&all==p.all&&right==p.right&&mix==p.mix&&padded==p.padded&&removeDc==p.removeDc&&synchronous==p.synchronous&&intervalMs==p.intervalMs&&rate==p.rate){
            ring.swap(p.ring);job.swap(p.job);cursor=p.cursor;end=p.end;received=p.received;lastSubmission=p.lastSubmission;identity=p.identity;resource.identity=identity;
            resource.analysisCache.swap(p.resource.analysisCache);std::swap(resource.result,p.resource.result);
        }
    }
    void process(ProcessContext& c)noexcept override{
        RenderCadence::include(hop);
        if(!all&&c.channelIndex!=0)return;
        resource.count=0;resource.blockStart=end;resource.blockFrames=c.bufferSize;
        for(std::size_t i=0;i<c.bufferSize;i++){
            double value=0;
            if(all)value=c.numericInput(0)[i];
            else for(std::size_t voice=0;voice<c.inputCount;voice++){
                const float* left=c.input(voice);const float* rightInput=c.rightInputs?c.rightInputs[voice]:left;
                double l=left&&std::isfinite(left[i])?left[i]:0,r=rightInput&&std::isfinite(rightInput[i])?rightInput[i]:0;
                value+=mix?(l+r)*.5:right?r:l;
            }
            ring[cursor]=std::isfinite(value)?static_cast<float>(std::clamp(value,-double(std::numeric_limits<float>::max()),double(std::numeric_limits<float>::max()))):0;
            cursor=(cursor+1)%size;++end;
            if(synchronous){
                if(end%hop||resource.count>=resource.frames.size())continue;
                auto& frame=resource.frames[resource.count++];frame.start=static_cast<std::int64_t>(end)-static_cast<std::int64_t>(size);
                double mean=0;if(removeDc){for(std::size_t j=0;j<size;j++)mean+=ring[j];mean/=size;}
                for(std::size_t j=0;j<size;j++)frame.bins[j]={(ring[(cursor+j)%size]-mean)*weights[j],0};
                std::fill(frame.bins.begin()+size,frame.bins.begin()+resource.size,std::complex<double>{});
                spectrumTransform(frame.bins.data(),resource.size);
            }else if(job&&end>=size&&(!lastSubmission||end-lastSubmission>=effectiveHop())){
                job->submitSpectrum(ring.data(),cursor,size,effectiveHop(),window,rate,end,identity,padded,removeDc);
                lastSubmission=end; // A busy worker skips this frame; never retry at every sample.
            }
        }
        if(!synchronous&&job){
            auto* ready=job->acquire();
            if(ready&&ready->historyIdentity==identity&&ready->sequence!=received){
                received=ready->sequence;resource.result=ready;resource.count=1;auto& frame=resource.frames[0];frame.start=ready->spectrum.startSample;
                std::copy_n(ready->spectrum.bins.data(),resource.size,frame.bins.data());
            }
        }
    }
};
EASYKNACK_NODE(SpectrumNode,"spectrum")
}
