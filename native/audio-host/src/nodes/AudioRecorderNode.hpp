#pragma once
#include "ParameterContracts.hpp"
#include "../TriggerInput.hpp"
#include "../AudioRecording.hpp"
#include <optional>
namespace easyknack {
struct AudioRecorderNode : NodeProcessor {
    std::string path,id;
    unsigned channels=2,digits=4;bool overwrite=false,active=false,secondary=false;
    std::uint64_t epoch=0;
    ManualTriggerReceiver start,stop;
    std::shared_ptr<AudioRecording> recording;
    std::vector<float> block;
    std::vector<std::optional<TriggerInput>> starts,stops;
    bool isObserver()const noexcept override{return true;}
    bool observesWithoutInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}
    bool mergesVoices()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    char executionRate()const noexcept override{return 'a';}
    void bindIdentity(const std::string& value)override{id=value;start.bind(id+":start");stop.bind(id+":stop");}
    void configure(std::istream& in)override{
        auto p=parameters::AudioRecorderNodeParameters::read(in);sources=p.sources();
        path.clear();if(p.file_value!="none"){
            if(p.file_value.size()%2)throw std::runtime_error("Invalid recording path");
            auto digit=[](char c)->unsigned{if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;throw std::runtime_error("Invalid recording path");};
            for(std::size_t i=0;i<p.file_value.size();i+=2)path.push_back(static_cast<char>(digit(p.file_value[i])*16+digit(p.file_value[i+1])));
        }
        channels=static_cast<unsigned>(std::stoul(p.channels_value));digits=std::clamp(p.digits_value,1,12);overwrite=p.overwrite_value!=0;
        if(channels<1||channels>2)throw std::runtime_error("Invalid recording channels");
    }
    void prepare(double rate,std::size_t frames)override{if(secondary)return;block.resize(frames*channels);starts.resize(2048);stops.resize(2048);recording=AudioRecording::create(path,overwrite,digits,channels,static_cast<unsigned>(std::lround(rate)),frames,id);}
    void shareChannelState(NodeProcessor&)override{secondary=true;}
    void adoptState(NodeProcessor& old)noexcept override{
        auto& p=static_cast<AudioRecorderNode&>(old);if(path!=p.path||channels!=p.channels||digits!=p.digits||overwrite!=p.overwrite)return;
        recording.swap(p.recording);active=p.active;epoch=p.epoch;start.seen=p.start.seen;stop.seen=p.stop.seen;
    }
    void process(ProcessContext& c)noexcept override{
        std::fill_n(c.output,c.bufferSize,0.f);if(c.channelIndex||!recording)return;
        if(epoch!=recording->epoch()){epoch=recording->epoch();active=false;}
        if(recording->failed())active=false;
        auto voices=std::max<std::size_t>(1,c.inputCount/3);
        // Trigger cursors are bounded by the host's global voice budget; no audio-thread allocation.
        voices=std::min(voices,starts.size());for(std::size_t v=0;v<voices;v++){starts[v].emplace(c,voices+v,start);stops[v].emplace(c,2*voices+v,stop);}
        std::size_t used=0;auto flush=[&]{if(used){if(!recording->push(2,block.data(),used))active=false;used=0;}};
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool begin=false,end=false;for(std::size_t v=0;v<voices;v++){begin|=starts[v]->at(i);end|=stops[v]->at(i);}
            if(end&&active){flush();recording->push(3);active=false;}
            if(begin&&!end&&!active)active=recording->push(1);
            if(!active)continue;
            float left=0,right=0;
            for(std::size_t v=0;v<voices;v++){
                auto a=c.numericInput(v)[i];left+=std::isfinite(a)?a:0;
                auto* r=c.rightInputs?c.rightInputs[v]:nullptr;auto b=r?r[c.sourceRates&&c.sourceRates[v]=='c'?0:i]:a;right+=std::isfinite(b)?b:0;
            }
            if(used+channels>block.size())flush();if(!active)continue;
            block[used++]=channels==1?.5f*(left+right):left;if(channels==2)block[used++]=right;
        }
        flush();
    }
};
EASYKNACK_NODE(AudioRecorderNode,"audiorecorder")
}
