#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Partials.hpp"
namespace easyknack {
struct PartialAnalysisNode : NodeProcessor {
    PartialsResource partials;SpectrumStream residual;
    double threshold=-60,minimum=20,maximum=20000,tolerance=100;
    double sinusoidality=.8,prominence=3,assessment=30,confidence=.7,duration=20,gap=20;
    double fundamental=0,harmonicTolerance=30,minimumF0=40,maximumF0=2000;bool harmonic=false;std::size_t limit=64;
    struct Track {bool active=false,used=false;std::uint64_t id=0;double frequency=0,velocity=0,phase=0,amplitude=0,quality=0;std::int64_t born=0,seen=0;};
    struct Peak {double frequency,amplitude,phase,shape;};
    std::vector<Track> tracks;std::vector<Peak> peaks;std::vector<int> assigned,trackOrder,freeSlots;
    std::uint64_t identity=0,nextId=1;
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    std::string parameterStructure()const override{return std::to_string(limit);}
    std::size_t resourceCapacityHint(ResourceKind k)const noexcept override{return k==ResourceKind::partials?limit:0;}
    void applyParameters(const NodeProcessor& p)noexcept override{
        const auto& o=static_cast<const PartialAnalysisNode&>(p);threshold=o.threshold;minimum=o.minimum;maximum=o.maximum;tolerance=o.tolerance;
        sinusoidality=o.sinusoidality;prominence=o.prominence;assessment=o.assessment;confidence=o.confidence;duration=o.duration;gap=o.gap;
        harmonic=o.harmonic;fundamental=o.fundamental;harmonicTolerance=o.harmonicTolerance;minimumF0=o.minimumF0;maximumF0=o.maximumF0;
    }
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::spectrum:ResourceKind::none;}
    std::size_t outputCount()const noexcept override{return 2;}
    ResourceKind outputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::partials:ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t p)noexcept override{return p==0?static_cast<DspResource*>(&partials):&residual;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!fa4a30260a8836d4"){
            sources={readInput(in)};threshold=readNumber(in);double count=readNumber(in);if(count<1||count>32768)throw std::runtime_error("Invalid partial capacity");limit=std::size_t(count);
            minimum=readNumber(in);maximum=readNumber(in);tolerance=readNumber(in);sinusoidality=prominence=assessment=confidence=duration=gap=0;return;
        }
        in.seekg(start);auto p=parameters::PartialAnalysisNodeParameters::read(in);sources=p.sources();threshold=p.threshold_value;limit=std::size_t(p.maximum_partials_value);minimum=p.minimum_value;maximum=p.maximum_value;tolerance=p.tolerance_value;
        sinusoidality=p.sinusoidality_value;prominence=p.prominence_value;assessment=p.assessment_value;confidence=p.confidence_value;duration=p.duration_value;gap=p.gap_value;
        harmonic=p.harmonic_value;fundamental=p.fundamental_value;harmonicTolerance=p.harmonic_tolerance_value;minimumF0=p.minimum_f0_value;maximumF0=p.maximum_f0_value;
    }
    void prepare(double,std::size_t block)override{
        partials.prepare(block,limit);residual.prepare(block);tracks.assign(limit,Track{});trackOrder.resize(limit);freeSlots.resize(limit);
        peaks.resize(SpectrumStream::maxSize/2+1);assigned.resize(peaks.size());
    }
    void inheritState(const NodeProcessor& p)noexcept override{const auto& o=static_cast<const PartialAnalysisNode&>(p);std::copy_n(o.tracks.begin(),std::min(tracks.size(),o.tracks.size()),tracks.begin());identity=o.identity;nextId=o.nextId;}
    static double cents(double a,double b)noexcept{return 1200*std::abs(std::log2(a/b));}
    static double prediction(const Track& t,std::int64_t start,double rate)noexcept{return std::max(1e-9,t.frequency+t.velocity*double(start-t.seen)/rate);}
    double estimateFundamental(std::size_t count)const noexcept{
        double best=0,score=0;auto n=std::min<std::size_t>(count,32);
        for(std::size_t i=0;i<std::min<std::size_t>(count,16);i++)for(unsigned divisor=1;divisor<=8;divisor++){
            double f=peaks[i].frequency/divisor;if(f<minimumF0||f>maximumF0)continue;double value=0;
            for(std::size_t j=0;j<n;j++){double h=std::round(peaks[j].frequency/f);if(h>=1&&h<=128&&cents(peaks[j].frequency,h*f)<=std::max(5.,harmonicTolerance))value+=peaks[j].amplitude*peaks[j].shape/std::sqrt(h);}
            if(value>score){score=value;best=f;}
        }
        return best;
    }
    void process(ProcessContext& c)noexcept override{
        const auto* input=static_cast<const SpectrumStream*>(c.resource(0));
        if(!input||!input->size){partials.size=partials.count=residual.size=residual.count=0;identity=0;return;}
        partials.metadata(*input);residual.copy(*input);
        if(identity!=input->identity){std::fill(tracks.begin(),tracks.end(),Track{});identity=input->identity;nextId=1;}
        const auto length=input->windowFrames();const double rate=input->sampleRate;
        for(std::size_t f=0;f<partials.count;f++){
            const auto& source=input->frames[f];auto& frame=partials.frames[f];frame.start=source.start;frame.count=0;if(source.start<0)continue;
            std::size_t found=0;const int padding=int(input->size/length),last=int(input->size/2);
            for(int k=1;k<last;k++){
                const double a=std::abs(source.bins[k-1]),b=std::abs(source.bins[k]),d=std::abs(source.bins[k+1]);if(b<=a||b<d||b<1e-20)continue;
                double la=std::log(std::max(a,1e-30)),lb=std::log(b),ld=std::log(std::max(d,1e-30)),den=la-2*lb+ld;
                double offset=std::abs(den)>1e-15?std::clamp(.5*(la-ld)/den,-.5,.5):0;
                double hz=(k+offset)*rate/input->size;if(hz<minimum||hz>maximum)continue;
                auto response=partialWindow(partialTau*(double(k)/input->size-hz/rate),length,input->window);if(std::abs(response)<1e-20)continue;
                auto phasor=source.bins[k]/response;double amplitude=2*std::abs(phasor);if(20*std::log10(std::max(amplitude,1e-30))<threshold)continue;
                double shape=1;
                if(sinusoidality>0||confidence>0){
                    std::complex<double> cross{};double observed=0,ideal=0;
                    for(int j=std::max(0,k-4*padding);j<=std::min(last,k+4*padding);j++){
                        double angle=partialTau*j/input->size,omega=partialTau*hz/rate;
                        auto model=phasor*partialWindow(angle-omega,length,input->window)+std::conj(phasor)*partialWindow(angle+omega,length,input->window);
                        cross+=std::conj(model)*source.bins[j];observed+=std::norm(source.bins[j]);ideal+=std::norm(model);
                    }
                    shape=observed>0&&ideal>0?std::clamp(std::norm(cross)/(observed*ideal),0.,1.):0;
                }
                if(shape<sinusoidality)continue;
                if(prominence>0){
                    std::array<double,64> noise{};std::size_t count=0;
                    for(int j=std::max(1,k-12*padding);j<=std::min(last-1,k+12*padding);j++)if(std::abs(j-k)>3*padding&&count<noise.size())noise[count++]=std::abs(source.bins[j]);
                    if(count){std::nth_element(noise.begin(),noise.begin()+count/2,noise.begin()+count);if(20*std::log10(b/std::max(noise[count/2],1e-30))<prominence)continue;}
                }
                peaks[found++]={hz,amplitude,std::arg(phasor),shape};
            }
            std::sort(peaks.begin(),peaks.begin()+found,[](const Peak& a,const Peak& b){return a.amplitude*a.shape>b.amplitude*b.shape;});
            if(harmonic){
                double f0=c.numericInput(1,float(fundamental))[0];if(!(f0>0&&std::isfinite(f0)))f0=estimateFundamental(found);
                auto end=std::remove_if(peaks.begin(),peaks.begin()+found,[&](const Peak& p){double h=f0>0?std::round(p.frequency/f0):0;return h<1||cents(p.frequency,h*f0)>harmonicTolerance;});found=std::size_t(end-peaks.begin());
            }
            std::size_t active=0,free=0;for(std::size_t j=0;j<tracks.size();j++){
                auto& t=tracks[j];t.used=false;if(t.active&&double(source.start-t.seen)/rate>gap*.001+double(input->hop)/rate+1e-9)t.active=false;
                if(t.active)trackOrder[active++]=int(j);else freeSlots[free++]=int(j);
            }
            std::sort(trackOrder.begin(),trackOrder.begin()+active,[&](int a,int b){return prediction(tracks[a],source.start,rate)<prediction(tracks[b],source.start,rate);});
            std::fill_n(assigned.data(),found,-1);
            for(std::size_t i=0;i<found;i++){
                const auto& peak=peaks[i];double bound=std::exp2(tolerance/1200),low=peak.frequency/bound,high=peak.frequency*bound,best=1.5;int slot=-1;
                auto begin=std::lower_bound(trackOrder.begin(),trackOrder.begin()+active,low,[&](int j,double value){return prediction(tracks[j],source.start,rate)<value;});
                for(auto j=begin;j!=trackOrder.begin()+active;j++){
                    auto& t=tracks[*j];double predicted=prediction(t,source.start,rate);if(predicted>high)break;if(t.used)continue;
                    double elapsed=double(source.start-t.seen)/rate;
                    double phaseError=std::abs(std::remainder(peak.phase-t.phase-partialTau*.5*(t.frequency+peak.frequency)*elapsed,partialTau))/3.141592653589793;
                    double cost=cents(peak.frequency,predicted)/std::max(1.,tolerance)+.15*std::min(4.,std::abs(std::log(std::max(peak.amplitude,1e-30)/std::max(t.amplitude,1e-30))))+.2*phaseError;
                    if(cost<best){best=cost;slot=*j;}
                }
                if(slot>=0){assigned[i]=slot;tracks[slot].used=true;}
            }
            std::size_t nextFree=0;
            for(std::size_t i=0;i<found;i++){
                auto& peak=peaks[i];int slot=assigned[i];bool birth=slot<0;if(birth){if(nextFree>=free)continue;slot=freeSlots[nextFree++];tracks[slot]={};tracks[slot].id=nextId++;tracks[slot].born=source.start;}
                auto& t=tracks[slot];double elapsed=birth?0:double(source.start-t.seen)/rate;
                double phaseError=birth?0:std::abs(std::remainder(peak.phase-t.phase-partialTau*.5*(t.frequency+peak.frequency)*elapsed,partialTau));
                double target=peak.shape*(.5+.5*std::cos(std::min(phaseError,3.141592653589793)*.5));
                double alpha=assessment>0?-std::expm1(-elapsed/(assessment*.001)):1;
                t.quality=birth?peak.shape:t.quality+alpha*(target-t.quality);
                t.velocity=birth||elapsed<=0?0:.75*t.velocity+.25*(peak.frequency-t.frequency)/elapsed;
                t.active=t.used=true;t.frequency=peak.frequency;t.phase=peak.phase;t.amplitude=peak.amplitude;t.seen=source.start;
                double age=double(source.start-t.born)/rate;
                if(t.quality<confidence||age+1e-9<duration*.001)continue;
                auto& p=frame.values[frame.count++];p={t.id,std::size_t(slot),peak.frequency,peak.amplitude,peak.phase,age,peak.frequency,peak.amplitude,peak.phase,t.quality};
                addPartial(residual.frames[f],input->size,length,rate,input->window,p.frequency,p.amplitude,p.phase,-1);
            }
            std::sort(frame.values.begin(),frame.values.begin()+frame.count,[](const Partial& a,const Partial& b){return a.amplitude>b.amplitude;});
            SpectrumStream::realSymmetry(residual.frames[f],input->size);
        }
    }
};
EASYKNACK_NODE(PartialAnalysisNode,"partialanalysis")
}
