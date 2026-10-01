#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Analysis.hpp"
#include <algorithm>
namespace easyknack {
struct OscilloscopeNode:NodeProcessor {
    std::shared_ptr<AnalysisMailbox> mailbox;
    std::unique_ptr<AnalysisFrame> frame=std::make_unique<AnalysisFrame>();
    float low=-.1f,high=.1f,milliseconds=20;
    unsigned cursor=0,skip=0,elapsed=0;
    bool armed=false,capturing=false,right=false,mix=false;
    bool mergesVoices()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    bool isObserver()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    void bindIdentity(const std::string& id)override{mailbox=analysisMailbox(id);}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const OscilloscopeNode&>(configuration);
        if(right!=p.right||mix!=p.mix){cursor=skip=elapsed=0;armed=capturing=false;}
        low=p.low;high=p.high;milliseconds=p.milliseconds;right=p.right;mix=p.mix;
    }
    void configure(std::istream& in)override{
        auto p=parameters::OscilloscopeNodeParameters::read(in);sources=p.sources();right=p.channel_value=="right";mix=p.channel_value=="mix";
        low=std::min(p.low_value,p.high_value);high=std::max(p.low_value,p.high_value);milliseconds=p.window_value;
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<OscilloscopeNode&>(previous);if(right!=p.right||mix!=p.mix)return;
        frame.swap(p.frame);cursor=p.cursor;skip=p.skip;elapsed=p.elapsed;armed=p.armed;capturing=p.capturing;
    }
    void inheritState(const NodeProcessor& old)noexcept override{
        const auto& p=static_cast<const OscilloscopeNode&>(old);
        if(right!=p.right||mix!=p.mix)return;
        *frame=*p.frame;cursor=p.cursor;skip=p.skip;elapsed=p.elapsed;armed=p.armed;capturing=p.capturing;
    }
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);
        if(!mailbox||c.channelIndex!=0)return;
        unsigned stride=std::max(1u,static_cast<unsigned>(milliseconds*.001*c.sampleRate/256));
        frame->span=256*stride/c.sampleRate;
        for(std::size_t i=0;i<c.bufferSize;i++){
            float v=0;
            for(std::size_t voice=0;voice<c.inputCount;++voice){
                const float* left=c.input(voice);const float* rhs=c.rightInputs?c.rightInputs[voice]:left;
                double l=left&&std::isfinite(left[i])?left[i]:0,r=rhs&&std::isfinite(rhs[i])?rhs[i]:0;
                v+=static_cast<float>(mix?(l+r)*.5:right?r:l);
            }
            if(!std::isfinite(v))v=0;
            if(v<=low)armed=true;
            if(!capturing&&((armed&&v>=high)||++elapsed>=stride*512)){
                capturing=true;armed=false;cursor=0;skip=0;elapsed=0;
            }
            if(capturing&&skip++%stride==0){frame->values[cursor++]=v;
                if(cursor==frame->valueCount){mailbox->publish(*frame);capturing=false;cursor=0;}}
        }
    }
};
EASYKNACK_NODE(OscilloscopeNode,"scope")
}
