#pragma once
#include "AnalysisViewerNode.hpp"
namespace easyknack {
struct SpectrumViewerNode : AnalysisViewerNode {
    void configure(std::istream& in)override{
        auto p=parameters::SpectrumViewerNodeParameters::read(in);sources=p.sources();
        selectedView=p.view_value=="auto"?-1:p.view_value=="magnitude"?0:p.view_value=="phase"?1:p.view_value=="power"?2:3;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{selectedView=static_cast<const SpectrumViewerNode&>(p).selectedView;}
};
EASYKNACK_NODE(SpectrumViewerNode,"spectrumviewer")
}
