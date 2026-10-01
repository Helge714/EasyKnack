#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
namespace easyknack {
struct SpectrumStatisticsNode : NodeProcessor {
    unsigned view=0,statistic=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::analysis;}
    void configure(std::istream& in)override{
        auto p=parameters::SpectrumStatisticsNodeParameters::read(in);sources=p.sources();
        view=p.view_value=="magnitude"?0:p.view_value=="phase"?1:2;
        statistic=p.statistic_value=="peak"?0:p.statistic_value=="mean"?1:p.statistic_value=="rms"?2:3;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{
        auto& other=static_cast<const SpectrumStatisticsNode&>(p);view=other.view;statistic=other.statistic;
    }
    void process(ProcessContext& c)noexcept override{
        auto* input=static_cast<const AnalysisResource*>(c.resource(0));auto* result=input?input->analysisResult():nullptr;
        double value=result?result->statistics[view][statistic]:0;
        std::fill_n(c.output,c.bufferSize,static_cast<float>(std::clamp(value,-static_cast<double>(std::numeric_limits<float>::max()),static_cast<double>(std::numeric_limits<float>::max()))));
    }
};
EASYKNACK_NODE(SpectrumStatisticsNode,"spectrumstatistics")
}
