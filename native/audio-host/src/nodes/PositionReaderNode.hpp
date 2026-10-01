#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../BufferRead.hpp"
namespace easyknack {
struct PositionReaderNode : NodeProcessor {
    bool wrapping=true;
    float position=0,offset=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    std::size_t outputCount()const noexcept override{return 3;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::history:ResourceKind::none;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const PositionReaderNode&>(other);position=p.position;offset=p.offset;wrapping=p.wrapping;}
    void configure(std::istream& in)override{
        auto start=in.tellg();std::string marker;in>>marker;
        if(marker=="!5eedf3ee01ec24b1"){
            int h,p,o;if(!(in>>h>>p>>position>>o>>offset))throw std::runtime_error("Invalid legacy position reader parameters");sources={h,p,o};
            for(int v:sources)if(v< -1||v>=2048)throw std::runtime_error("Invalid reader source");
            if(!std::isfinite(position)||position<0||position>1||!std::isfinite(offset)||offset<0||offset>1)throw std::runtime_error("Invalid reader values");wrapping=true;return;
        }
        in.seekg(start);auto p=parameters::PositionReaderNodeParameters::read(in);sources=p.sources();position=p.position_value;offset=p.offset_value;wrapping=p.wrap_value!=0;}
    void process(ProcessContext& c)noexcept override{
        auto* history=static_cast<const HistoryResource*>(c.resource(0));auto* distance=c.outputAt(1);auto* readPosition=c.outputAt(2);
        if(!history||history->samples.empty()){
            std::fill_n(c.output,c.bufferSize,0.f);if(distance)std::fill_n(distance,c.bufferSize,0.f);if(readPosition)std::fill_n(readPosition,c.bufferSize,0.f);return;
        }
        auto p=c.numericInput(1,position),o=c.numericInput(2,offset);
        for(std::size_t i=0;i<c.bufferSize;i++){
            double location=std::isfinite(p[i])?p[i]:0,shift=std::isfinite(o[i])?o[i]:0;
            auto value=wrapping?readBufferPosition(*history,(wrapBufferPosition(location,1)-wrapBufferPosition(shift,1))*history->samples.size(),i):readBufferClamped(*history,positionBufferTimeline(*history,location-shift,i),i);
            c.output[i]=value.audio;if(distance)distance[i]=value.distance;if(readPosition)readPosition[i]=value.position;
        }
    }
};
EASYKNACK_NODE(PositionReaderNode,"positionreader")
}
