#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Analysis.hpp"
namespace easyknack {
struct PitchMonitorNode : NodeProcessor {
    std::shared_ptr<AnalysisMailbox> mailbox;
    std::unique_ptr<AnalysisFrame> frame=std::make_unique<AnalysisFrame>();
    std::size_t untilPublish=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{sources=parameters::PitchMonitorNodeParameters::read(in).sources();}
    void bindIdentity(const std::string& id)override{mailbox=analysisMailbox(id);}
    void process(ProcessContext& c)noexcept override{
        auto target=c.numericInput(1);
        for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=target[i];
        if(c.channelIndex!=0||!mailbox||!c.bufferSize)return;
        auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        if(untilPublish>frames){untilPublish-=frames;return;}
        untilPublish=static_cast<std::size_t>(c.sampleRate/20);
        auto finite=[](float v){return std::isfinite(v)?v:0.f;};
        auto last=c.bufferSize-1;
        frame->frequency=std::max(0.f,finite(c.numericInput(0)[last]));
        frame->confidence=std::clamp(finite(c.numericInput(2)[last]),0.f,1.f);
        frame->valueCount=1;frame->values[0]=std::max(0.f,finite(target[last]));
        mailbox->publish(*frame);
    }
};
EASYKNACK_NODE(PitchMonitorNode,"pitchmonitor")
}
