#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
#include <array>
namespace easyknack {
struct SpectralFeaturesNode : NodeProcessor {
    bool power=true,normalized=true,ready=false;
    double fraction=.85;
    std::vector<double> weights,previous;
    std::array<double,8> values{};
    std::uint64_t identity=0,end=0;
    std::size_t size=0,windowSize=0,hop=0;
    double rate=0,amplitudeScale=1;
    SpectrumWindow window=SpectrumWindow::hann;bool dc=false;
    std::size_t outputCount()const noexcept override{return 8;}
    bool supportsControlRate()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    void prepare(double,std::size_t)override{weights.resize(SpectrumFrame::capacity/2+1);previous.resize(weights.size());}
    void configure(std::istream& in)override{
        auto p=parameters::SpectralFeaturesNodeParameters::read(in);sources=p.sources();
        if(p.weighting_value!="power"&&p.weighting_value!="magnitude")throw std::runtime_error("Invalid spectral weighting");
        power=p.weighting_value=="power";fraction=std::clamp(p.rolloff_fraction_value,0.,1.);normalized=p.normalize_flux_value!=0;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& node)noexcept override{
        auto& p=static_cast<const SpectralFeaturesNode&>(node);
        if(power!=p.power||normalized!=p.normalized||fraction!=p.fraction)ready=false;
        power=p.power;normalized=p.normalized;fraction=p.fraction;
    }
    void adoptState(NodeProcessor& old)noexcept override{
        auto& p=static_cast<SpectralFeaturesNode&>(old);
        if(power!=p.power||normalized!=p.normalized||fraction!=p.fraction)return;
        previous.swap(p.previous);values=p.values;ready=p.ready;identity=p.identity;end=p.end;size=p.size;
        windowSize=p.windowSize;hop=p.hop;rate=p.rate;window=p.window;dc=p.dc;amplitudeScale=p.amplitudeScale;
    }
    void evaluate(const std::complex<double>* bins,std::size_t fft,std::size_t win,std::size_t step,
            double sr,std::uint64_t source,std::uint64_t position,SpectrumWindow shape,bool removed)noexcept{
        if(fft<2||fft>SpectrumFrame::capacity||sr<=0)return;
        bool compatible=ready&&identity==source&&size==fft&&windowSize==win&&hop==step&&rate==sr&&window==shape&&dc==removed&&position>end;
        if(ready&&identity==source&&size==fft&&windowSize==win&&hop==step&&rate==sr&&window==shape&&dc==removed&&position==end)return;
        std::size_t count=fft/2+1;double total=0,first=0,logSum=0,spacing=sr/fft;
        if(!ready||windowSize!=win||window!=shape){double sum=0;for(std::size_t i=0;i<win;i++)sum+=spectrumWindowValue(shape,i,win);amplitudeScale=sum>0?1/sum:1;}
        double scale=amplitudeScale;
        for(std::size_t k=0;k<count;k++){
            double a=std::abs(bins[k])*scale,multiplier=k==0||k==fft/2?1:2;
            double w=(power?a*a:a)*multiplier;
            if(!std::isfinite(w)||w<0)w=0;
            weights[k]=w;total+=w;first+=w*k*spacing;
        }
        values.fill(0);
        if(total>1e-30){
            double center=first/total,m2=0,m3=0,m4=0,flux=0,cumulative=0;
            double floor=std::max(1e-300,total/count*1e-12),arithmetic=0;
            bool rolled=false;
            for(std::size_t k=0;k<count;k++){
                double p=weights[k]/total,d=k*spacing-center,d2=d*d;
                m2+=p*d2;m3+=p*d2*d;m4+=p*d2*d2;
                double current=normalized?p:weights[k],difference=current-(compatible?previous[k]:current);
                flux+=difference*difference;previous[k]=current;
                cumulative+=weights[k];
                if(!rolled&&cumulative>=fraction*total){values[5]=k*spacing;rolled=true;}
                double clamped=std::max(weights[k],floor);logSum+=std::log(clamped);arithmetic+=clamped;
            }
            values[0]=center;values[1]=std::sqrt(std::max(0.,m2));
            if(m2>1e-20){values[2]=m3/(m2*std::sqrt(m2));values[3]=m4/(m2*m2);}
            values[4]=std::sqrt(flux);
            double middle=spacing*(count-1)*.5;
            double denominator=spacing*spacing*count*(static_cast<double>(count)*count-1)/12;
            values[6]=denominator>0?(center-middle)/denominator:0;
            values[7]=std::clamp(std::exp(logSum/count)/(arithmetic/count),0.,1.);
        }else{
            double flux=0;for(std::size_t k=0;k<count;k++){if(compatible)flux+=previous[k]*previous[k];previous[k]=0;}values[4]=std::sqrt(flux);
        }
        ready=true;identity=source;size=fft;windowSize=win;hop=step;rate=sr;window=shape;dc=removed;end=position;
    }
    void process(ProcessContext& c)noexcept override{
        auto* resource=c.resource(0);
        if(resource&&resource->kind()==ResourceKind::spectrum){
            auto& stream=*static_cast<const SpectrumStream*>(resource);
            for(std::size_t f=0;f<stream.count;f++){auto& frame=stream.frames[f];if(frame.start>=0)
                evaluate(frame.bins.data(),stream.size,stream.windowFrames(),stream.hop,stream.sampleRate,stream.identity,frame.start+stream.windowFrames(),stream.window,stream.dcRemoved);}
        }else if(resource){
            auto* result=static_cast<const AnalysisResource*>(resource)->analysisResult();
            if(result&&result->spectrum.valid()){auto& f=result->spectrum;evaluate(f.bins.data(),f.fftFrames,f.windowFrames,f.hopFrames,f.sampleRate,f.sourceIdentity,f.endSample,f.window,f.dcRemoved);}
        }else{ready=false;values.fill(0);}
        for(std::size_t p=0;p<8;p++)if(auto* out=c.outputAt(p)){
            double value=values[p];if(!std::isfinite(value))value=0;
            std::fill_n(out,c.bufferSize,static_cast<float>(std::clamp(value,-static_cast<double>(std::numeric_limits<float>::max()),static_cast<double>(std::numeric_limits<float>::max()))));
        }
    }
};
EASYKNACK_NODE(SpectralFeaturesNode,"spectralfeatures")
}
