#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
namespace easyknack {
struct FFTAnalysisNode : NodeProcessor {
    AnalysisResource resource;std::shared_ptr<AnalysisJob> job;
    std::uint64_t submitted=0,historyIdentity=0;float minimum=20,maximum=20000;double rate=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::history;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &resource;}
    void configure(std::istream& in)override{auto p=parameters::FFTAnalysisNodeParameters::read(in);sources=p.sources();minimum=p.minimum_value;maximum=p.maximum_value;if(minimum>=maximum)throw std::runtime_error("Invalid analysis frequency range");}
    void prepare(double sampleRate,std::size_t)override{if(!job||rate!=sampleRate){job=AnalysisWorker::instance().create();rate=sampleRate;submitted=0;resource.result=nullptr;}}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{auto& n=static_cast<const FFTAnalysisNode&>(p);minimum=n.minimum;maximum=n.maximum;}
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<FFTAnalysisNode&>(previous);if(rate==p.rate){job.swap(p.job);std::swap(submitted,p.submitted);std::swap(historyIdentity,p.historyIdentity);std::swap(resource.result,p.resource.result);}
    }
    void process(ProcessContext& c)noexcept override{
        auto* history=static_cast<const HistoryResource*>(c.resource(0));if(!job||!history)return;
        if(historyIdentity!=history->identity){historyIdentity=history->identity;submitted=0;}
        auto* next=job->acquire();resource.result=next&&next->historyIdentity==historyIdentity?next:nullptr;
        if(history->end>=submitted+4096&&job->submit(*history,minimum,maximum))submitted=history->end;
    }
};
EASYKNACK_NODE(FFTAnalysisNode,"fftanalysis")
}
