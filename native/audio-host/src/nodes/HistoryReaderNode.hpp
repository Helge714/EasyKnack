#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../BufferRead.hpp"
namespace easyknack {
struct HistoryReaderNode : NodeProcessor {
    bool wrapping=true;
    float speed=1,offset=0;double cursor=0;bool initialized=false;std::uint64_t historyIdentity=0;
    ManualTriggerReceiver reset;
    float previousReset=0;
    bool legacyOutputLayout=false; // Old compiled plans retain a silent former trigger slot.
    void bindIdentity(const std::string& id)override{reset.bind(id+":reset");}
    void shareChannelState(NodeProcessor& p)override{reset=static_cast<HistoryReaderNode&>(p).reset;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    bool canShareChannels()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 3;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::history:ResourceKind::none;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{auto& v=static_cast<const HistoryReaderNode&>(p);if(wrapping!=v.wrapping)initialized=false;wrapping=v.wrapping;speed=v.speed;offset=v.offset;legacyOutputLayout=v.legacyOutputLayout;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!e0e39780cef2821c"){
            int history,speedInput,offsetInput,resetInput;
            if(!(in>>marker)||marker!="normalized-offset-v2"||!(in>>history>>speedInput>>speed>>offsetInput>>offset>>resetInput))throw std::runtime_error("Invalid legacy reader parameters");
            sources={history,speedInput,offsetInput,resetInput};
            for(auto source:sources)if(source< -1||source>=2048)throw std::runtime_error("Invalid reader source");
            if(!std::isfinite(speed)||speed< -8||speed>8||!std::isfinite(offset)||offset<0||offset>1)throw std::runtime_error("Invalid reader values");
            legacyOutputLayout=true;return;
        }
        if(marker=="!b99cd9f79b33ffd5"){
            int h,s,o,r;if(!(in>>marker)||marker!="normalized-position-v3"||!(in>>h>>s>>speed>>o>>offset>>r))throw std::runtime_error("Invalid legacy reader parameters");
            sources={h,s,o,r};for(int v:sources)if(v< -1||v>=2048)throw std::runtime_error("Invalid reader source");
            if(!std::isfinite(speed)||speed< -8||speed>8||!std::isfinite(offset)||offset<0||offset>1)throw std::runtime_error("Invalid reader values");wrapping=true;return;
        }
        in.seekg(start);auto p=parameters::HistoryReaderNodeParameters::read(in);wrapping=p.wrap_value!=0;sources=p.sources();speed=p.speed_value;offset=p.offset_value;legacyOutputLayout=false;
    }
    void inheritState(const NodeProcessor& p)noexcept override{auto& old=static_cast<const HistoryReaderNode&>(p);cursor=old.cursor;initialized=old.initialized&&wrapping==old.wrapping;historyIdentity=old.historyIdentity;reset.seen=old.reset.seen;previousReset=old.previousReset;}
    void process(ProcessContext& c)noexcept override{
        auto* history=static_cast<const HistoryResource*>(c.resource(0));
        auto* distance=c.outputAt(1);
        auto* readPosition=c.outputAt(2);
        if(readPosition)std::fill_n(readPosition,c.bufferSize,0.f);
        const auto manual=reset.take();auto* events=c.events(3);std::size_t eventCursor=0;
        if(!history||history->samples.empty()){
            std::fill_n(c.output,c.bufferSize,0.f);
            if(distance)std::fill_n(distance,c.bufferSize,0.f);
            initialized=false;
            return;
        }
        const auto size=history->samples.size();const double length=static_cast<double>(size);
        if(!initialized||historyIdentity!=history->identity){historyIdentity=history->identity;cursor=wrapping?wrap(static_cast<double>(history->end%size)-static_cast<double>(history->blockFrames),length):(history->immutable?0:static_cast<double>(history->end)-history->blockFrames);initialized=true;}
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool resetting=i==0&&manual&&!c.input(3)&&!events;
            if(events){
                if(i==0){resetting|=previousReset<=.5f&&events->initial>.5f;previousReset=events->initial;}
                while(eventCursor<events->count&&events->events[eventCursor].sampleOffset==i){
                    auto& event=events->events[eventCursor++];
                    if(event.kind==ValueEventKind::trigger)resetting=true;
                    else{resetting|=previousReset<=.5f&&event.value>.5f;previousReset=event.value;}
                }
            }else{float value=c.valueAt(3,static_cast<std::uint32_t>(i));resetting|=previousReset<=.5f&&value>.5f;previousReset=value;}
            const auto write=history->writeHeadAt(i);
            if(resetting)cursor=history->immutable?0:wrapping?wrap(static_cast<double>(write)-1,length):std::max(0.,bufferHeadAt(*history,i)-1);
            float rate=c.input(1)?c.input(1)[i]:speed,delay=c.input(2)?c.input(2)[i]:offset;
            double normalizedDelay=std::isfinite(delay)?std::clamp(delay,0.f,1.f):0;
            double base=cursor;
            double position=wrapping?wrap(base-normalizedDelay*length,length):clampBufferTimeline(*history,base-normalizedDelay*length,i);
            double step=std::isfinite(rate)?std::clamp(rate,-8.f,8.f):0;
            auto value=wrapping?readBufferPosition(*history,position,i):readBufferClamped(*history,position,i);
            c.output[i]=value.audio;if(distance)distance[i]=value.distance;
            if(readPosition&&!legacyOutputLayout)readPosition[i]=value.position;
            double increment=step*(history->immutable?history->sampleRate/c.sampleRate:1);
            cursor=wrapping?wrap(base+increment,length):position+normalizedDelay*length+increment;
        }
    }
    static double wrap(double position,double length)noexcept{
        return wrapBufferPosition(position,length);
    }
};
EASYKNACK_NODE(HistoryReaderNode,"historyreader")
}
