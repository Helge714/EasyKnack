#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../AnalysisData.hpp"
namespace easyknack {
/** One linear peak scan per new correlation frame, constant storage, no allocations. */
struct CorrelationFeaturesNode : NodeProcessor {
    double minimum=20,maximum=20000,threshold=.3,silence=1e-6;
    std::array<double,5> values{0,0,0,-120,0};
    std::uint64_t identity=0,sequence=0,end=0;
    bool ready=false;
    std::size_t outputCount()const noexcept override{return 5;}
    bool supportsControlRate()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    void configure(std::istream& in)override{
        auto p=parameters::CorrelationFeaturesNodeParameters::read(in);sources=p.sources();
        minimum=p.minimum_value;maximum=p.maximum_value;threshold=p.threshold_value;silence=p.silence_value;
        if(!std::isfinite(minimum)||!std::isfinite(maximum)||minimum<=0||maximum<=0||!std::isfinite(threshold)||threshold<0||threshold>1||!std::isfinite(silence)||silence<0)
            throw std::invalid_argument("Invalid correlation feature settings");
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{
        auto& p=static_cast<const CorrelationFeaturesNode&>(other);
        if(minimum!=p.minimum||maximum!=p.maximum||threshold!=p.threshold||silence!=p.silence)ready=false;
        minimum=p.minimum;maximum=p.maximum;threshold=p.threshold;silence=p.silence;
    }
    void inheritState(const NodeProcessor& other)noexcept override{
        auto& p=static_cast<const CorrelationFeaturesNode&>(other);
        if(minimum!=p.minimum||maximum!=p.maximum||threshold!=p.threshold||silence!=p.silence)return;
        values=p.values;identity=p.identity;sequence=p.sequence;end=p.end;ready=p.ready;
    }
    void clear()noexcept{values={0,0,0,-120,0};}
    void evaluate(const AnalysisResult& r)noexcept{
        clear();auto count=r.correlationCount;double dt=r.lagSpacingSeconds;
        if(!(r.availableViews&8)||count<4||count>AnalysisResult::count||!std::isfinite(dt)||dt<=0||minimum>maximum||
            !std::isfinite(r.correlationRms)||r.correlationRms<=silence)return;
        double zero=r.views[3][0];if(!std::isfinite(zero)||zero<=1e-12)return;
        double lower=std::max(2.,std::ceil(1/(maximum*dt))),upper=std::min(static_cast<double>((count-1)/3),std::floor(1/(minimum*dt)));
        if(lower>upper)return;
        double best=-1,selected=0,selectedPeak=0;
        // Two passes prefer the shortest period among numerically equal maxima.
        for(unsigned pass=0;pass<2;pass++)for(std::size_t k=static_cast<std::size_t>(lower);k<=static_cast<std::size_t>(upper);k++){
            double a=r.views[3][k-1]/zero,b=r.views[3][k]/zero,c=r.views[3][k+1]/zero;
            if(!std::isfinite(a)||!std::isfinite(b)||!std::isfinite(c)||b<=0||b<=a||b<c)continue;
            double curvature=a-2*b+c,shift=curvature<0?std::clamp(.5*(a-c)/curvature,-.5,.5):0;
            double period=(k+shift)*dt,frequency=1/period;
            if(frequency<minimum||frequency>maximum)continue;
            double peak=std::clamp(b-.25*(a-c)*shift,0.,1.);
            if(pass==0)best=std::max(best,peak);
            else if(peak>=best-1e-5){selected=period;selectedPeak=peak;break;}
        }
        if(best<0||selected<=0)return;
        values[0]=selectedPeak;double p=std::clamp(selectedPeak,1e-12,1-1e-12);
        values[3]=std::clamp(10*std::log10(p/(1-p)),-120.,120.);
        if(selectedPeak>=threshold){values[1]=selected;values[2]=1/selected;values[4]=1;}
    }
    void process(ProcessContext& c)noexcept override{
        auto* resource=c.resource(0);auto* r=resource&&resource->kind()==ResourceKind::analysis?static_cast<const AnalysisResource*>(resource)->analysisResult():nullptr;
        if(!r){ready=false;clear();}
        else if(!ready||r->historyIdentity!=identity||r->sequence!=sequence||r->endSample!=end){
            evaluate(*r);identity=r->historyIdentity;sequence=r->sequence;end=r->endSample;ready=true;
        }
        for(std::size_t p=0;p<values.size();p++)if(auto* output=c.outputAt(p))std::fill_n(output,c.bufferSize,static_cast<float>(values[p]));
    }
};
EASYKNACK_NODE(CorrelationFeaturesNode,"correlationfeatures")
}
