#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisData.hpp"
namespace easyknack {
struct PitchDetectionNode : NodeProcessor {
    double minimum=40,maximum=2000;float frequency=0,confidence=0;
    std::uint64_t sequence=0,identity=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 2;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    void configure(std::istream& in)override{auto p=parameters::PitchDetectionNodeParameters::read(in);sources=p.sources();minimum=p.minimum_value;maximum=p.maximum_value;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const PitchDetectionNode&>(other);minimum=p.minimum;maximum=p.maximum;sequence=identity=0;}
    void detect(const AnalysisResult& r)noexcept{
        frequency=confidence=0;
        if(!(r.availableViews&8)||r.correlationCount<3||minimum>=maximum||r.lagSpacingSeconds<=0)return;
        auto& values=r.views[3];auto count=r.correlationCount;
        auto first=static_cast<std::size_t>(std::clamp(std::ceil(1/(maximum*r.lagSpacingSeconds)),1.,static_cast<double>(count-2)));
        auto last=static_cast<std::size_t>(std::clamp(std::floor(1/(minimum*r.lagSpacingSeconds)),1.,static_cast<double>(count-2)));
        // Ignore the initial zero-lag lobe, then choose the earliest strong local peak.
        std::size_t trough=1;while(trough+1<count&&values[trough]>=values[trough+1])++trough;
        first=std::max(first,trough+1);float best=0;
        for(auto i=first;i<=last;i++)if(values[i]>values[i-1]&&values[i]>=values[i+1])best=std::max(best,values[i]);
        if(best<.1f)return;
        for(auto i=first;i<=last;i++)if(values[i]>values[i-1]&&values[i]>=values[i+1]&&values[i]>=best*.9f){
            double a=values[i-1],b=values[i],d=values[i+1],den=a-2*b+d;
            double offset=den!=0?std::clamp(.5*(a-d)/den,-.5,.5):0;
            double hz=1/((i+offset)*r.lagSpacingSeconds);
            if(hz>=minimum&&hz<=maximum){frequency=static_cast<float>(hz);confidence=std::clamp(values[i],0.f,1.f);}return;
        }
    }
    void process(ProcessContext& c)noexcept override{
        auto* input=static_cast<const AnalysisResource*>(c.resource(0));auto* r=input?input->analysisResult():nullptr;
        if(!r){frequency=confidence=0;sequence=identity=0;}
        else if(sequence!=r->sequence||identity!=r->historyIdentity){detect(*r);sequence=r->sequence;identity=r->historyIdentity;}
        if(auto* out=c.outputAt(0))std::fill_n(out,c.bufferSize,frequency);
        if(auto* out=c.outputAt(1))std::fill_n(out,c.bufferSize,confidence);
    }
};
EASYKNACK_NODE(PitchDetectionNode,"pitchdetection")
}
