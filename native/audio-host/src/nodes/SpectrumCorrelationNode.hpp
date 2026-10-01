#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
namespace easyknack {
struct SpectrumCorrelationNode : NodeProcessor {
    AnalysisResource resource;
    std::shared_ptr<AnalysisJob> job;
    std::uint64_t sequence=0,identity=0,generation=HistoryResource::nextIdentity.fetch_add(1);
    bool unbiased=false;
    double smoothing=1;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &resource;}
    void configure(std::istream& in)override{
        auto position=in.tellg();std::string marker;in>>marker;
        if(marker=="!cda992b32640bb2e"||marker=="!998d05d9c008fe0e"){
            int source;if(!(in>>source)||source< -1||source>=2048)throw std::runtime_error("Invalid correlation source");sources={source};unbiased=false;smoothing=1;
            if(marker=="!998d05d9c008fe0e"){std::string mode;if(!(in>>mode)||(mode!="biased"&&mode!="unbiased"))throw std::runtime_error("Invalid correlation normalization");unbiased=mode=="unbiased";}return;
        }
        in.seekg(position);auto p=parameters::SpectrumCorrelationNodeParameters::read(in);sources=p.sources();unbiased=p.normalization_value=="unbiased";smoothing=p.smoothing_value;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{
        auto& p=static_cast<const SpectrumCorrelationNode&>(other);if(unbiased==p.unbiased&&smoothing==p.smoothing)return;
        unbiased=p.unbiased;smoothing=p.smoothing;sequence=identity=0;resource.result=nullptr;
        generation=HistoryResource::nextIdentity.fetch_add(1);
    }
    void prepare(double,std::size_t)override{if(!job)job=AnalysisWorker::instance().create();resource.view=AnalysisView::correlation;}
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<SpectrumCorrelationNode&>(previous);if(unbiased!=p.unbiased||smoothing!=p.smoothing)return;generation=p.generation;job.swap(p.job);std::swap(resource.result,p.resource.result);sequence=p.sequence;identity=p.identity;
    }
    void process(ProcessContext& c)noexcept override{
        if(c.channelIndex||!job)return;
        auto* input=static_cast<const AnalysisResource*>(c.resource(0));auto* result=input?input->analysisResult():nullptr;
        if(!result||!result->spectrum.valid()){resource.result=nullptr;sequence=identity=0;return;}
        if(identity!=result->historyIdentity){resource.result=nullptr;sequence=0;identity=result->historyIdentity;generation=HistoryResource::nextIdentity.fetch_add(1);}
        if(sequence!=result->sequence&&job->submitCorrelation(result->spectrum,unbiased,generation,smoothing))sequence=result->sequence;
        auto* ready=job->acquire();resource.result=ready&&ready->historyIdentity==generation?ready:nullptr;
    }
};
EASYKNACK_NODE(SpectrumCorrelationNode,"spectrumcorrelation")
}
