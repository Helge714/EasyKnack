#pragma once
#include "BinContextNode.hpp"
namespace easyknack {
struct BinHoldNode : BinAccess {
    float value=0;
    std::vector<float> held;
    std::vector<unsigned char> captured;
    ManualTriggerReceiver manual;
    bool pending=false,captureFrame=false;
    std::uint64_t pendingTime=0,identity=0;
    std::size_t bins=0;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    void bindIdentity(const std::string& id)override{manual.bind(id+":trigger");}
    void configure(std::istream& in)override{auto p=parameters::BinHoldNodeParameters::read(in);sources=p.sources();value=p.in_value;}
    void prepare(double,std::size_t)override{held.assign(SpectrumStream::maxSize/2+1,0);captured.assign(held.size(),0);bins=0;pending=captureFrame=false;}
    void beginBlock(const SpectrumStream& source)noexcept{
        if(identity!=source.identity||bins!=source.size){std::fill(captured.begin(),captured.end(),0);identity=source.identity;bins=source.size;pending=false;}
        if(manual.take()){pending=true;pendingTime=source.blockStart;}
    }
    void beginFrame(std::int64_t end)noexcept{
        captureFrame=pending&&end>0&&static_cast<std::uint64_t>(end)>pendingTime;
        if(captureFrame)pending=false;
    }
    void process(ProcessContext& c)noexcept override{
        if(!frame||bin>=held.size()){c.output[0]=0;return;}
        float input=c.numericInput(0,value)[0];if(!std::isfinite(input))input=0;
        if(captureFrame||c.numericInput(1)[0]>0){held[bin]=input;captured[bin]=1;}
        c.output[0]=captured[bin]?held[bin]:input;
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& old=static_cast<BinHoldNode&>(previous);held.swap(old.held);captured.swap(old.captured);
        manual.seen=old.manual.seen;pending=old.pending;pendingTime=old.pendingTime;identity=old.identity;bins=old.bins;captureFrame=false;
    }
};
EASYKNACK_NODE(BinHoldNode,"binhold")
}
