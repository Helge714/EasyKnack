#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
namespace easyknack {
struct AnalysisStatisticsNode : NodeProcessor {
    unsigned mode=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    void configure(std::istream& in)override{auto p=parameters::AnalysisStatisticsNodeParameters::read(in);sources=p.sources();mode=p.statistic_value=="peak"?0:p.statistic_value=="mean"?1:p.statistic_value=="rms"?2:p.statistic_value=="integral"?3:p.statistic_value=="frequency"?4:p.statistic_value=="periodicity"?5:6;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{mode=static_cast<const AnalysisStatisticsNode&>(p).mode;}
    void process(ProcessContext& c)noexcept override{
        auto* resource=static_cast<const AnalysisResource*>(c.resource(0));auto* result=resource?resource->analysisResult():nullptr;double value=0;
        if(result)value=mode<4?result->statistics[static_cast<unsigned>(resource->view)][mode]:mode==4?result->frequency:mode==5?result->confidence:result->correlationTime;
        std::fill_n(c.output,c.bufferSize,static_cast<float>(std::clamp(value,-static_cast<double>(std::numeric_limits<float>::max()),static_cast<double>(std::numeric_limits<float>::max()))));
    }
};
EASYKNACK_NODE(AnalysisStatisticsNode,"analysisstatistics")
}
