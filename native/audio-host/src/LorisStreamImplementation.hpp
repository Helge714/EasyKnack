// Compiled once by nodes/LorisLibrary.cpp, after the original Loris sources.
#include "LorisStream.hpp"
#include <unordered_map>
#include <unordered_set>
namespace Loris {
/** The sequence in Analyzer::analyze, with a persistent, bounded PartialBuilder.
 * No peak-selection, thinning, reassignment or matching formula is replaced. */
class EasyKnackStreamingAnalyzer {
    Analyzer analyzer;
    PartialBuilder builder;
    ReassignedSpectrum spectrum;
    SpectralPeakSelector selector;
    struct Track {std::uint64_t id;std::size_t slot;double born;};
    std::unordered_map<const Partial*,Track> tracks;
    std::vector<std::size_t> freeSlots;
    std::uint64_t& nextId;
    std::unique_ptr<AssociateBandwidth> association;
    static std::vector<double> window(std::size_t length,bool derivative) {
        std::vector<double> values(length);
        auto shape=KaiserWindow::computeShape(90);
        if(derivative)KaiserWindow::buildTimeDerivativeWindow(values,shape);
        else KaiserWindow::buildWindow(values,shape);
        return values;
    }
public:
    EasyKnackStreamingAnalyzer(std::size_t length,double rate,const easyknack::LorisStream::Settings& s,std::size_t capacity,std::uint64_t& ids):
        analyzer(s.resolution>0?s.resolution:2*rate/(length+1)),
        builder(s.drift>0?s.drift:analyzer.freqResolution()*.5),
        spectrum(window(length,false),window(length,true)),
        selector(rate,(length-1)/(2*std::sqrt(1+std::pow(KaiserWindow::computeShape(90)/M_PI,2))*rate)),nextId(ids) {
        analyzer.setAmpFloor(s.threshold);analyzer.setFreqFloor(s.minimum);
        if(s.bandwidth==1){analyzer.storeResidueBandwidth(s.region);association=std::make_unique<AssociateBandwidth>(s.region,rate);}
        else if(s.bandwidth==2)analyzer.storeConvergenceBandwidth(s.convergence);
        else analyzer.storeNoBandwidth();
        for(std::size_t i=capacity;i>0;--i)freeSlots.push_back(i-1);
    }
    void analyze(const double* begin,const double* middle,const double* end,double time,double rate,
                 const easyknack::LorisStream::Settings& settings,easyknack::PartialFrame& out) {
        spectrum.transform(begin,middle,end);
        auto peaks=selector.selectPeaks(spectrum,settings.minimum);
        auto rejected=analyzer.thinPeaks(peaks,time);
        analyzer.fixBandwidth(peaks);
        if(association)association->associateBandwidth(peaks.begin(),rejected,peaks.end());
        peaks.erase(rejected,peaks.end());
        peaks.erase(std::remove_if(peaks.begin(),peaks.end(),[&](const auto& p){return !std::isfinite(p.frequency())||p.frequency()>settings.maximum||p.frequency()>=rate*.5;}),peaks.end());
        if(peaks.size()>out.values.size())peaks.erase(peaks.begin()+out.values.size(),peaks.end()); // upstream thinning is amplitude-sorted
        builder.buildPartials(peaks,time);
        std::unordered_set<const Partial*> active(builder.mEligiblePartials.begin(),builder.mEligiblePartials.end());
        for(auto it=builder.mCollectedPartials.begin();it!=builder.mCollectedPartials.end();) {
            if(!active.count(&*it)){
                auto found=tracks.find(&*it);if(found!=tracks.end()){freeSlots.push_back(found->second.slot);tracks.erase(found);}
                it=builder.mCollectedPartials.erase(it);
            }else ++it;
        }
        out.count=0;
        for(auto* p:builder.mEligiblePartials) {
            auto found=tracks.find(p);
            if(found==tracks.end()){
                // All dead tracks were reclaimed before assigning new stable slots.
                if(freeSlots.empty())continue;
                found=tracks.emplace(p,Track{nextId++,freeSlots.back(),p->startTime()}).first;freeSlots.pop_back();
            }
            const auto info=found->second;
            double age=std::max(0.,time-info.born);
            if(age+1e-9>=settings.duration*.001&&out.count<out.values.size()){
                double frequency=p->frequencyAt(time),phase=p->phaseAt(time);
                double tonal=std::clamp(1-p->bandwidthAt(time),0.,1.);
                double amplitude=p->last().amplitude()*std::sqrt(tonal);
                // Loris phase is referenced to the reassigned breakpoint time;
                // the resource convention is phase at the original window start.
                phase=std::remainder(phase-easyknack::partialTau*frequency*(middle-begin+1)/rate,easyknack::partialTau);
                if(std::isfinite(amplitude)&&std::isfinite(phase)&&std::isfinite(frequency))
                    out.values[out.count++]={info.id,info.slot,frequency,amplitude,phase,age,frequency,amplitude,phase,tonal};
            }
            // Matching uses only the final breakpoint. Keep two for interpolation,
            // without allowing an indefinitely running analyzer to grow its history.
            while(p->numBreakpoints()>2)p->erase(p->begin());
        }
    }
};
}
namespace easyknack {
struct LorisStream::Impl {
    std::unique_ptr<Loris::EasyKnackStreamingAnalyzer> analyzer;
    std::vector<std::complex<double>> work;
    std::vector<double> samples;
    Settings settings{};std::size_t size=0,window=0,capacity=0;double rate=0;
    std::int64_t last=-1;std::uint64_t nextId=1;
    static bool same(const Settings& a,const Settings& b) {
        return a.resolution==b.resolution&&a.drift==b.drift&&a.threshold==b.threshold&&a.minimum==b.minimum&&a.maximum==b.maximum&&
               a.duration==b.duration&&a.convergence==b.convergence&&a.region==b.region&&a.bandwidth==b.bandwidth;
    }
};
LorisStream::LorisStream():impl(std::make_unique<Impl>()){}
LorisStream::~LorisStream()=default;
void LorisStream::reset(){impl->analyzer.reset();impl->last=-1;}
void LorisStream::analyze(const SpectrumStream& input,const StreamSpectrumFrame& frame,const Settings& settings,PartialFrame& out){
    auto& p=*impl;const auto length=input.windowFrames();
    if(length<4||input.size<length||input.sampleRate<=0||settings.minimum>=input.sampleRate*.5||settings.maximum<settings.minimum){out.count=0;return;}
    if(!p.analyzer||p.size!=input.size||p.window!=length||p.rate!=input.sampleRate||p.capacity!=out.values.size()||!Impl::same(p.settings,settings)||frame.start<=p.last){
        p.analyzer=std::make_unique<Loris::EasyKnackStreamingAnalyzer>(length-1,input.sampleRate,settings,out.values.size(),p.nextId);
        p.work.resize(input.size);p.samples.resize(length);p.size=input.size;p.window=length;p.rate=input.sampleRate;p.capacity=out.values.size();p.settings=settings;
    }
    std::copy_n(frame.bins.data(),input.size,p.work.data());spectrumTransform(p.work.data(),input.size,true);
    // Recover the unwindowed section for Loris' original Kaiser/reassignment stage.
    // The single zero endpoint of Hann/Blackman is excluded from the odd window.
    for(std::size_t i=1;i<length;i++){
        double w=spectrumWindowValue(input.window,i,length);
        p.samples[i]=std::abs(w)>1e-12?p.work[i].real()/w:0;
    }
    auto middle=p.samples.data()+length/2;
    p.analyzer->analyze(p.samples.data()+1,middle,p.samples.data()+length,(frame.start+length/2.)/input.sampleRate,input.sampleRate,settings,out);
    p.last=frame.start;
}
}
