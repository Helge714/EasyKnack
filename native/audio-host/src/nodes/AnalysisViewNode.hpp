#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
namespace easyknack {
struct AnalysisViewNode : NodeProcessor {
    AnalysisResource resource;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &resource;}
    void configure(std::istream& in)override{auto p=parameters::AnalysisViewNodeParameters::read(in);sources=p.sources();resource.view=p.view_value=="magnitude"?AnalysisView::magnitude:p.view_value=="phase"?AnalysisView::phase:p.view_value=="power"?AnalysisView::power:AnalysisView::correlation;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{resource.view=static_cast<const AnalysisViewNode&>(p).resource.view;}
    void process(ProcessContext& c)noexcept override{auto* input=static_cast<const AnalysisResource*>(c.resource(0));resource.result=input?input->analysisResult():nullptr;}
};
EASYKNACK_NODE(AnalysisViewNode,"analysisview")
}
