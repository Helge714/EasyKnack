#pragma once
#include "ParameterContracts.hpp"
#include "../AnalysisResources.hpp"
#include "../WaveformResource.hpp"
namespace easyknack {
struct WavetableViewerNode : NodeProcessor {
    std::shared_ptr<AnalysisMailbox> mailbox;
    std::array<float,WaveformResource::size> previous{};
    bool published=false;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool isObserver()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::waveform;}
    void configure(std::istream& in)override{sources=parameters::WavetableViewerNodeParameters::read(in).sources();}
    void bindIdentity(const std::string& id)override{mailbox=analysisMailbox(id);}
    void process(ProcessContext& c)noexcept override{
        if(c.channelIndex||!mailbox)return;auto* wave=waveformResource(c.resource(0));
        if(wave&&published&&previous==wave->samples)return;
        if(!wave&&!published)return;
        if((mailbox->write.load(std::memory_order_relaxed)+1)%mailbox->frames.size()==mailbox->read.load(std::memory_order_acquire))return;
        AnalysisFrame frame;frame.span=1;frame.valueCount=wave?WaveformResource::size:0;
        if(wave){previous=wave->samples;std::copy(previous.begin(),previous.end(),frame.values.begin());}
        mailbox->publish(frame);published=wave!=nullptr;
    }
};
EASYKNACK_NODE(WavetableViewerNode,"waveformviewer")
}
