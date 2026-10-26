#pragma once
#include "ParameterContracts.hpp"
#include "../TriggerInput.hpp"
#include "../Analysis.hpp"
#include <chrono>
namespace easyknack {
/** Retain every captured row until Clear; UI polling never consumes or overwrites rows. */
struct MeasurementRecorderNode : NodeProcessor {
    static constexpr std::size_t maxRows=4096,header=12;
    ManualTriggerReceiver record;
    std::size_t count=1,stored=0,submitted=0;
    std::uint64_t generation=0,session=0,samples=0;
    std::array<float,8> fallback{};
    std::vector<float> rows;
    std::shared_ptr<AnalysisMailbox> mailbox;
    std::unique_ptr<AnalysisFrame> packet=std::make_unique<AnalysisFrame>();
    bool isObserver()const noexcept override{return true;}
    bool observesWithoutInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    char executionRate()const noexcept override{return 'a';}
    void bindIdentity(const std::string& id)override{record.bind(id+":record");mailbox=analysisMailbox(id);}
    void configure(std::istream& in)override{
        auto p=parameters::MeasurementRecorderNodeParameters::read(in);sources=p.sources();
        count=std::clamp<std::size_t>(p.inputs_value,1,8);generation=std::stoull(p.generation_value,nullptr,16);
        fallback={static_cast<float>(p.value_0_value),static_cast<float>(p.value_1_value),static_cast<float>(p.value_2_value),static_cast<float>(p.value_3_value),static_cast<float>(p.value_4_value),static_cast<float>(p.value_5_value),static_cast<float>(p.value_6_value),static_cast<float>(p.value_7_value)};
    }
    void prepare(double,std::size_t)override{
        static std::atomic<std::uint64_t> identity{static_cast<std::uint64_t>(std::chrono::steady_clock::now().time_since_epoch().count())};
        session=++identity;rows.resize(maxRows*(5+count));
    }
    void adoptState(NodeProcessor& old)noexcept override{
        auto& p=static_cast<MeasurementRecorderNode&>(old);if(count!=p.count||generation!=p.generation)return;
        rows.swap(p.rows);stored=p.stored;samples=p.samples;session=p.session;record=p.record;
    }
    static void words(float* target,std::uint64_t value)noexcept{for(unsigned i=0;i<4;i++)target[i]=static_cast<float>((value>>(16*i))&65535);}
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);if(c.channelIndex!=0)return;
        TriggerInput trigger(c,0,record);
        for(std::size_t i=0;i<c.bufferSize;i++)if(trigger.at(i)&&stored<maxRows){
            auto* row=rows.data()+stored*(5+count);words(row,samples+i);unsigned valid=0;
            for(std::size_t p=0;p<count;p++){auto value=c.numericInput(p+1,fallback[p])[i];if(std::isfinite(value)){valid|=1u<<p;row[5+p]=value;}else row[5+p]=0;}
            row[4]=static_cast<float>(valid);++stored;
        }
        samples+=c.bufferSize;
        if(stored>submitted&&mailbox){
            // Retry a full mailbox on a later block, including when no more triggers arrive.
            auto write=mailbox->write.load(std::memory_order_relaxed);
            if((write+1)%mailbox->frames.size()==mailbox->read.load(std::memory_order_acquire))return;
            auto& f=*packet;f.confidence=static_cast<float>(c.sampleRate);f.values[0]=-9137;f.values[1]=1;f.values[2]=static_cast<float>(count);f.values[3]=static_cast<float>(stored);
            words(f.values.data()+4,generation);words(f.values.data()+8,session);
            std::copy_n(rows.data(),stored*(5+count),f.values.data()+header);f.valueCount=header+stored*(5+count);mailbox->publish(f);submitted=stored;
        }
    }
};
EASYKNACK_NODE(MeasurementRecorderNode,"measurementrecorder")
}
