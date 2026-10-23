#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Analysis.hpp"
namespace easyknack {
/** Rolling telemetry packets recover intermediate blocks even when UI polling is slower.
 * No allocation, locks, strings or UI work in process(). Header words preserve the
 * 64-bit sample clock exactly through the existing float analysis transport. */
struct ValueViewerNode : NodeProcessor {
    static constexpr std::size_t maxInputs=8,header=8;
    std::size_t count=1,stride=480,partial=0,stored=0,cursor=0,capacity=128,publishSamples=0;
    unsigned channel=0;bool raw=false;std::uint64_t samples=0;
    std::array<float,maxInputs> low{},high{},last{};
    std::vector<float> ring;
    std::shared_ptr<AnalysisMailbox> mailbox;
    std::unique_ptr<AnalysisFrame> packet=std::make_unique<AnalysisFrame>();
    bool isObserver()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    char executionRate()const noexcept override{return 'a';}
    void bindIdentity(const std::string& id)override{mailbox=analysisMailbox(id);}
    void configure(std::istream& in)override{
        auto p=parameters::ValueViewerNodeParameters::read(in);sources=p.sources();
        count=std::clamp<std::size_t>(p.inputs_value,1,maxInputs);raw=p.record_samples_value!=0;
        if(p.channel_value!="left"&&p.channel_value!="right"&&p.channel_value!="mix")throw std::runtime_error("Invalid viewer channel");
        channel=p.channel_value=="right"?1:p.channel_value=="mix"?2:0;
    }
    void prepare(double rate,std::size_t)override{
        stride=raw?1:std::max<std::size_t>(1,std::llround(rate*.01));
        capacity=std::min<std::size_t>(raw?4096:128,(AnalysisFrame::maxValues-header)/(3*count));
        ring.resize(capacity*count*3);packet->confidence=static_cast<float>(rate);packet->frequency=static_cast<float>(count);
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<ValueViewerNode&>(previous);
        if(count!=p.count||channel!=p.channel||stride!=p.stride||capacity!=p.capacity)return;
        ring.swap(p.ring);samples=p.samples;partial=p.partial;stored=p.stored;cursor=p.cursor;
        publishSamples=p.publishSamples;low=p.low;high=p.high;last=p.last;
    }
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);if(!mailbox||c.channelIndex!=0||ring.empty())return;
        for(std::size_t i=0;i<c.bufferSize;i++){
            for(std::size_t p=0;p<count;p++){
                auto input=c.numericInput(p);double value=input[i];
                if(channel&&c.rightInputs&&p<c.inputCount&&c.rightInputs[p]){
                    double right=c.rightInputs[p][c.sourceRates&&c.sourceRates[p]=='c'?0:i];value=channel==2?(value+right)*.5:right;
                }
                float v=std::isfinite(value)?static_cast<float>(value):0;
                if(!partial)low[p]=high[p]=v;else{low[p]=std::min(low[p],v);high[p]=std::max(high[p],v);}last[p]=v;
            }
            ++samples;
            if(++partial==stride){
                for(std::size_t p=0;p<count;p++){auto index=(cursor*count+p)*3;ring[index]=low[p];ring[index+1]=high[p];ring[index+2]=last[p];}
                cursor=(cursor+1)%capacity;stored=std::min(capacity,stored+1);partial=0;
            }
        }
        publishSamples+=c.bufferSize;
        // 50 Hz telemetry, independent of audio block size. The rolling packet
        // includes older buckets, so the UI deduplicates rather than losing blocks.
        if(!stored||publishSamples<static_cast<std::size_t>(c.sampleRate/50))return;
        publishSamples=0;auto& f=*packet;f.span=0;f.values[0]=1;f.values[1]=static_cast<float>(stride);f.values[2]=static_cast<float>(stored);f.values[3]=0;
        auto completed=samples-partial;for(unsigned j=0;j<4;j++)f.values[4+j]=static_cast<float>((completed>>(16*j))&65535);
        for(std::size_t b=0;b<stored;b++)for(std::size_t p=0;p<count*3;p++)f.values[header+b*count*3+p]=ring[((cursor+capacity-stored+b)%capacity)*count*3+p];
        f.valueCount=header+stored*count*3;mailbox->publish(f);
    }
};
EASYKNACK_NODE(ValueViewerNode,"valueviewer")
}
