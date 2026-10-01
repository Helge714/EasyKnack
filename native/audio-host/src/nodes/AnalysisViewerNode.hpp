#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
namespace easyknack {
struct AnalysisViewerNode : NodeProcessor {
    std::shared_ptr<AnalysisMailbox> mailbox;std::uint64_t sequence=0,previousIdentity=0;AnalysisView previousView=AnalysisView::magnitude;
    int selectedView=-1; // Legacy nodes inherit the upstream view; new viewers select directly.
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool isObserver()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    void configure(std::istream& in)override{auto p=parameters::AnalysisViewerNodeParameters::read(in);sources=p.sources();}
    void bindIdentity(const std::string& id)override{mailbox=analysisMailbox(id);}
    void process(ProcessContext& c)noexcept override{
        if(c.channelIndex!=0)return;auto* input=static_cast<const AnalysisResource*>(c.resource(0));
        auto* result=input?input->analysisResult():nullptr;if(!mailbox||!result)return;
        auto view=selectedView<0?input->view:static_cast<AnalysisView>(selectedView);
        if(!(result->availableViews&(1u<<static_cast<unsigned>(view))))view=input->view;
        if(!(result->availableViews&(1u<<static_cast<unsigned>(view))))view=AnalysisView::magnitude;
        if(sequence==result->sequence&&previousIdentity==result->historyIdentity&&previousView==view)return;
        AnalysisFrame frame;frame.valueCount=view==AnalysisView::correlation?result->correlationCount:result->binCount;frame.frequency=result->frequency;frame.confidence=result->confidence;
        frame.symmetric=view==AnalysisView::correlation;
        frame.span=(frame.valueCount-1)*(frame.symmetric?result->lagSpacingSeconds:result->binSpacingHz);
        frame.values=result->views[static_cast<unsigned>(view)];mailbox->publish(frame);sequence=result->sequence;previousView=view;previousIdentity=result->historyIdentity;
    }
};
EASYKNACK_NODE(AnalysisViewerNode,"analysisviewer")
}
