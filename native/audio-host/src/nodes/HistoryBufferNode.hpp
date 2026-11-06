#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct HistoryBufferNode : NodeProcessor {
    HistoryResource history;double seconds=2;
    bool canShareChannels()const noexcept override{return true;}
    bool mergesVoices()const noexcept override{return true;}
    char executionRate()const noexcept override{return 'a';}
    ResourceKind outputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::history:ResourceKind::none;}
    DspResource* resourceOutput(std::size_t port)noexcept override{return port==0?&history:nullptr;}
    void configure(std::istream& in)override{auto p=parameters::HistoryBufferNodeParameters::read(in);sources=p.sources();seconds=p.seconds_value;}
    void prepare(double rate,std::size_t frames)override{history.prepare(rate,seconds,frames);}
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<HistoryBufferNode&>(previous);
        if(history.sampleRate==p.history.sampleRate&&history.samples.size()==p.history.samples.size()){
            history.samples.swap(p.history.samples);std::swap(history.end,p.history.end);std::swap(history.identity,p.history.identity);
            if(history.overwritten.size()==p.history.overwritten.size()){
                history.overwritten.swap(p.history.overwritten);std::swap(history.savedFrames,p.history.savedFrames);
                std::swap(history.writeStart,p.history.writeStart);std::swap(history.blockFrames,p.history.blockFrames);
            }
        }
    }
    void process(ProcessContext& c)noexcept override{
        if(history.samples.empty())return;
        history.beginBlock(c.bufferSize);
        for(std::size_t i=0;i<c.bufferSize;i++){
            float sum=0;for(std::size_t p=0;p<c.inputCount;p++)if(c.input(p))sum+=c.input(p)[i];history.append(sum);
        }
    }
};
EASYKNACK_NODE(HistoryBufferNode,"history")
}
