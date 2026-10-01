#pragma once
#include "ParameterContracts.hpp"
#include "../TriggerInput.hpp"
#include <array>
namespace easyknack {
struct TriggerMergeNode : NodeProcessor {
    std::array<ManualTriggerReceiver,16> buttons;
    unsigned count=2;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    bool processesEventBlocks()const noexcept override{return true;}
    bool writesEventBuffers()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsTriggerFeedback()const noexcept override{return true;}
    char resolveAutoRate(const char*,std::size_t)noexcept override{return 'e';}
    void bindIdentity(const std::string& id)override{for(unsigned i=0;i<buttons.size();++i)buttons[i].bind(id+":input-"+std::to_string(i));}
    void shareChannelState(NodeProcessor& p)override{buttons=static_cast<TriggerMergeNode&>(p).buttons;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{count=static_cast<const TriggerMergeNode&>(p).count;}
    void configure(std::istream& in)override{auto p=parameters::TriggerMergeNodeParameters::read(in);sources=p.sources();count=p.inputs_value;}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const TriggerMergeNode&>(other);for(unsigned i=0;i<buttons.size();++i)buttons[i].seen=p.buttons[i].seen;}
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);
        for(unsigned p=0;p<count;++p){TriggerInput in(c,p,buttons[p]);for(std::size_t i=0;i<c.bufferSize;++i)if(in.at(i))c.output[i]=1;}
        if(auto* out=c.eventOutput()){out->initial=0;for(std::size_t i=0;i<c.bufferSize;++i)if(c.output[i])out->push(static_cast<std::uint32_t>(c.sampleOffset+i),1,ValueEventKind::trigger);}
    }
    void processEventBlock(ProcessContext& c)noexcept override{process(c);}
};
EASYKNACK_NODE(TriggerMergeNode,"triggermerge")
}
