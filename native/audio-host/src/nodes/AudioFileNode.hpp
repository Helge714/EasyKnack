#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../AudioFileStream.hpp"
#include "../Analysis.hpp"

namespace easyknack {
struct AudioFileNode : NodeProcessor {
    std::shared_ptr<AudioFileStream> stream;
    std::string path;
    double playValue=0,seekValue=0,speedValue=1,start=0,end=1,position=0,lastSeek=0;
    bool loop=false,wasPlaying=false;std::uint64_t underruns=0;
    ManualTriggerReceiver reset;
    std::shared_ptr<AnalysisMailbox> mailbox;
    std::unique_ptr<AnalysisFrame> report=std::make_unique<AnalysisFrame>();
    unsigned reportCountdown=0;
    bool supportsScalarInputs()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 4;}
    void bindIdentity(const std::string& id)override{reset.bind(id+":reset");mailbox=analysisMailbox(id);}
    void shareChannelState(NodeProcessor& other)override{auto& p=static_cast<AudioFileNode&>(other);stream=p.stream;reset=p.reset;}
    void shareVoiceState(NodeProcessor& other)override{reset=static_cast<AudioFileNode&>(other).reset;}
    static std::string decodePath(const std::string& hex){
        if(hex=="none")return {};
        if(hex.size()%2)throw std::runtime_error("Invalid audio path encoding");std::string result;
        auto digit=[](char c)->unsigned{if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;throw std::runtime_error("Invalid audio path encoding");};
        for(std::size_t i=0;i<hex.size();i+=2)result.push_back(static_cast<char>(digit(hex[i])*16+digit(hex[i+1])));return result;
    }
    void configure(std::istream& in)override{
        auto p=parameters::AudioFileNodeParameters::read(in);sources=p.sources();path=decodePath(p.file_value);
        playValue=p.play_value;seekValue=p.position_value;speedValue=p.speed_value;loop=p.loop_value!=0;start=p.loop_start_value;end=p.loop_end_value;
        if(end<=start)end=std::min(1.,start+1./48000);
        if(!path.empty())stream=std::make_shared<AudioFileStream>(path);
        position=stream?seekValue*stream->frames:0;lastSeek=seekValue;
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& p=static_cast<AudioFileNode&>(previous);if(path!=p.path)return;
        stream.swap(p.stream);position=p.position;lastSeek=p.lastSeek;wasPlaying=p.wasPlaying;underruns=p.underruns;reset.seen=p.reset.seen;
    }
    void inheritState(const NodeProcessor& previous)noexcept override{
        auto& p=static_cast<const AudioFileNode&>(previous);if(path==p.path){position=p.position;lastSeek=p.lastSeek;wasPlaying=p.wasPlaying;underruns=p.underruns;reset.seen=p.reset.seen;}
    }
    void process(ProcessContext& c)noexcept override{
        bool playing=c.valueAt(0,0,static_cast<float>(playValue))>=.5f;
        double speed=c.valueAt(3,0,static_cast<float>(speedValue));speed=std::isfinite(speed)?std::clamp(speed,0.,4.):0;
        double seek=std::clamp<double>(c.valueAt(2,0,static_cast<float>(seekValue)),0,1);
        auto clicks=reset.take();auto* events=c.events(1);std::size_t event=0;
        if(stream&&std::isfinite(seek)&&seek!=lastSeek){position=seek*stream->frames;lastSeek=seek;}
        double low=stream?std::min<double>(stream->frames-1,start*stream->frames):0;
        double high=stream?std::max(low+1,std::min<double>(stream->frames,end*stream->frames)):0;
        if(stream&&playing&&!wasPlaying&&position>=stream->frames)position=loop?low:0;
        wasPlaying=playing;bool missed=false;
        if(stream)stream->request(static_cast<std::uint64_t>(position),static_cast<std::uint64_t>(low));
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool restart=i==0&&clicks&&!events&&!c.input(1);
            if(events)while(event<events->count&&events->events[event].sampleOffset<=i){if(events->events[event].sampleOffset==i)restart=true;++event;}
            if(restart&&stream){position=loop?low:0;stream->request(static_cast<std::uint64_t>(position),static_cast<std::uint64_t>(low));}
            float value=0;
            if(stream&&playing&&speed>0){
                if(loop&&(position>=high||position<low))position=low+std::fmod(std::max(0.,position-low),high-low);
                if(position<stream->frames){
                    auto frame=static_cast<std::uint64_t>(position);auto next=frame+1;
                    if(loop&&next>=high)next=static_cast<std::uint64_t>(low);
                    float a=0,b=0;bool ready=stream->sample(frame,c.channelIndex,a)&&stream->sample(next,c.channelIndex,b);
                    if(ready){value=static_cast<float>(a+(b-a)*(position-frame));position+=speed*stream->rate/c.sampleRate;}
                    else{missed=true;stream->request(frame,static_cast<std::uint64_t>(low));}
                }
            }
            c.output[i]=std::isfinite(value)?value:0;
        }
        if(missed)++underruns;
        if(auto* out=c.outputAt(1))std::fill(out,out+c.bufferSize,stream?static_cast<float>(std::min(1.,position/stream->frames)):0);
        if(auto* out=c.outputAt(2))std::fill(out,out+c.bufferSize,stream?static_cast<float>(stream->frames/double(stream->rate)):0);
        if(auto* out=c.outputAt(3))std::fill(out,out+c.bufferSize,static_cast<float>(underruns+(stream&&stream->failed.load(std::memory_order_relaxed)?1:0)));
        if(mailbox&&c.channelIndex==0&&reportCountdown--==0){
            report->frequency=stream?std::min(1.,position/stream->frames):0;report->valueCount=1;report->values[0]=static_cast<float>(report->frequency);mailbox->publish(*report);reportCountdown=7;
        }
    }
};
EASYKNACK_NODE(AudioFileNode,"audiofile")
}
