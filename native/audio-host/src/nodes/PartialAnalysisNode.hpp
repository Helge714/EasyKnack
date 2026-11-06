#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Partials.hpp"
namespace easyknack {
struct PartialAnalysisNode : NodeProcessor {
    PartialsResource partials;SpectrumStream residual;
    double threshold=-60,minimum=20,maximum=20000,tolerance=100;std::size_t limit=64;
    struct Track {bool active=false,used=false;std::uint64_t id=0;double frequency=0,age=0;};
    std::array<Track,PartialsResource::capacity> tracks{};std::uint64_t identity=0,nextId=1;std::int64_t lastStart=0;
    struct Peak {double frequency,amplitude,phase;};std::vector<Peak> peaks;
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    std::size_t outputCount()const noexcept override{return 2;}
    ResourceKind outputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::partials:ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t port)noexcept override{return port==0?static_cast<DspResource*>(&partials):&residual;}
    void configure(std::istream& in)override{auto p=parameters::PartialAnalysisNodeParameters::read(in);sources=p.sources();threshold=p.threshold_value;minimum=p.minimum_value;maximum=p.maximum_value;tolerance=p.tolerance_value;limit=std::clamp<std::size_t>(std::size_t(p.maximum_partials_value),1,PartialsResource::capacity);}
    void prepare(double,std::size_t block)override{partials.prepare(block);residual.prepare(block);peaks.resize(SpectrumStream::maxSize/2+1);}
    void inheritState(const NodeProcessor& p)noexcept override{const auto& old=static_cast<const PartialAnalysisNode&>(p);tracks=old.tracks;identity=old.identity;nextId=old.nextId;lastStart=old.lastStart;}
    void process(ProcessContext& c)noexcept override{
        const auto* input=static_cast<const SpectrumStream*>(c.resource(0));
        if(!input||!input->size){partials.size=partials.count=residual.size=residual.count=0;identity=0;return;}
        partials.metadata(*input);residual.copy(*input);
        if(identity!=input->identity){tracks={};identity=input->identity;nextId=1;lastStart=0;}
        const auto length=input->windowFrames();
        for(std::size_t f=0;f<partials.count;f++){
            const auto& source=input->frames[f];auto& frame=partials.frames[f];frame.start=source.start;frame.count=0;
            if(source.start<0)continue;
            const double dt=lastStart<source.start?double(source.start-lastStart)/input->sampleRate:0;lastStart=source.start;
            std::size_t found=0;
            for(std::size_t k=1;k<input->size/2;k++){
                const double a=std::abs(source.bins[k-1]),b=std::abs(source.bins[k]),d=std::abs(source.bins[k+1]);
                if(b<=a||b<d||b<1e-20)continue;
                const double la=std::log(std::max(a,1e-30)),lb=std::log(b),ld=std::log(std::max(d,1e-30)),den=la-2*lb+ld;
                const double offset=std::abs(den)>1e-15?std::clamp(.5*(la-ld)/den,-.5,.5):0;
                const double frequency=(k+offset)*input->sampleRate/input->size;
                if(frequency<minimum||frequency>maximum)continue;
                auto response=partialWindow(partialTau*(double(k)/input->size-frequency/input->sampleRate),length,input->window);
                if(std::abs(response)<1e-20)continue;
                auto phasor=source.bins[k]/response;double amplitude=2*std::abs(phasor);
                if(20*std::log10(std::max(amplitude,1e-30))<threshold)continue;
                peaks[found++]={frequency,amplitude,std::arg(phasor)};
            }
            std::sort(peaks.begin(),peaks.begin()+found,[](const Peak& a,const Peak& b){return a.amplitude>b.amplitude;});found=std::min(found,limit);
            for(auto& t:tracks)t.used=false;
            // Match all old tracks before reusing slots, so births cannot steal later matches.
            std::array<int,PartialsResource::capacity> assigned;assigned.fill(-1);
            for(std::size_t i=0;i<found;i++){
                double best=tolerance;int slot=-1;
                for(std::size_t j=0;j<tracks.size();j++)if(tracks[j].active&&!tracks[j].used){double distance=std::abs(1200*std::log2(peaks[i].frequency/tracks[j].frequency));if(distance<=best){best=distance;slot=int(j);}}
                if(slot>=0){assigned[i]=slot;tracks[slot].used=true;}
            }
            for(std::size_t i=0;i<found;i++){
                bool birth=assigned[i]<0;int slot=assigned[i];
                if(birth){for(std::size_t j=0;j<tracks.size();j++)if(!tracks[j].used){slot=int(j);break;}if(slot<0)continue;tracks[slot]={true,true,nextId++,peaks[i].frequency,0};}
                auto& t=tracks[slot];t.active=true;t.used=true;t.frequency=peaks[i].frequency;if(!birth)t.age+=dt;
                auto& p=frame.values[frame.count++];p={t.id,std::size_t(slot),peaks[i].frequency,peaks[i].amplitude,peaks[i].phase,t.age,peaks[i].frequency,peaks[i].amplitude,peaks[i].phase};
                addPartial(residual.frames[f],input->size,length,input->sampleRate,input->window,p.frequency,p.amplitude,p.phase,-1);
            }
            for(auto& t:tracks)if(!t.used)t.active=false;
            SpectrumStream::realSymmetry(residual.frames[f],input->size);
        }
    }
};
EASYKNACK_NODE(PartialAnalysisNode,"partialanalysis")
}
