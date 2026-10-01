#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
#include <sstream>
namespace easyknack {
enum class UnitFormat { factor,percent,db,cent,semitone,octave,hz,khz,second,ms,turn,degree,radian,q,sample,bpm };
inline UnitFormat readUnit(std::istream& in){
    std::string name;in>>name;
    const char* names[]={"factor","percent","db","cent","semitone","octave","hz","khz","second","ms","turn","degree","radian","q","sample","bpm"};
    for(int i=0;i<16;i++)if(name==names[i])return static_cast<UnitFormat>(i);
    throw std::runtime_error("Unknown unit");
}
inline int unitDimension(UnitFormat u){if(u==UnitFormat::bpm)return 1;if(u==UnitFormat::sample)return 2;int i=static_cast<int>(u);return i<6?0:i<8?1:i<10?2:i<13?3:4;}
inline double unitToBase(double v,UnitFormat u,double rate=48000) noexcept {
    switch(u){
        case UnitFormat::percent:return v/100;
        case UnitFormat::db:return v<=-160?0:std::pow(10.,std::clamp(v/20.,-100.,100.));
        case UnitFormat::cent:return std::exp2(std::clamp(v/1200.,-100.,100.));
        case UnitFormat::semitone:return std::exp2(std::clamp(v/12.,-100.,100.));
        case UnitFormat::octave:return std::exp2(std::clamp(v,-100.,100.));
        case UnitFormat::khz:return v*1000; case UnitFormat::bpm:return v/60;
        case UnitFormat::ms:return v/1000;
        case UnitFormat::sample:return v/rate;
        case UnitFormat::degree:return v/360;
        case UnitFormat::radian:return v/(2*3.141592653589793);
        default:return v;
    }
}
inline double unitFromBase(double v,UnitFormat u,double rate=48000) noexcept {
    switch(u){
        case UnitFormat::percent:return v*100;
        case UnitFormat::db:return std::max(-160.,20*std::log10(std::max(v,1e-8)));
        case UnitFormat::cent:return 1200*std::log2(std::max(v,1e-8));
        case UnitFormat::semitone:return 12*std::log2(std::max(v,1e-8));
        case UnitFormat::octave:return std::log2(std::max(v,1e-8));
        case UnitFormat::khz:return v/1000; case UnitFormat::bpm:return v*60;
        case UnitFormat::ms:return v*1000;
        case UnitFormat::sample:return v*rate;
        case UnitFormat::degree:return v*360;
        case UnitFormat::radian:return v*(2*3.141592653589793);
        default:return v;
    }
}
struct UnitConverterNode:NodeProcessor {
    bool supportsSampleFeedback()const noexcept override{return true;}

    UnitFormat from=UnitFormat::factor,to=UnitFormat::percent;
    float value=0;
    bool canShareChannels()const noexcept override{return true;}
    bool supportsControlRate()const noexcept override{return true;}
    bool supportsEventRate()const noexcept override{return true;}
    bool evaluatesOnEvents()const noexcept override{return true;}
    bool supportsLiveParameters() const noexcept override { return true; }
    void applyParameters(const NodeProcessor& configuration) noexcept override {
        const auto& p=static_cast<const UnitConverterNode&>(configuration);
        value=p.value;from=p.from;to=p.to;
    }
    void configure(std::istream& in)override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::UnitConverterNodeParameters::read(in);
        sources=p.sources();value=p.value_value;std::istringstream a(p.from_value),b(p.to_value);from=readUnit(a);to=readUnit(b);if(unitDimension(from)!=unitDimension(to))throw std::runtime_error("Incompatible units");
        return;}

        sources={readInput(in)};value=readNumber(in);from=readUnit(in);to=readUnit(in);
        if(unitDimension(from)!=unitDimension(to))throw std::runtime_error("Incompatible units");
    }
    bool supportsScalarInputs()const noexcept override{return true;}
    void process(ProcessContext& c)noexcept override{
        const auto input=c.numericInput(0,value);
        for(std::size_t i=0;i<c.bufferSize;i++){
            c.output[i]=evaluate(input[i],from,to,c.sampleRate);
        }
    }
    static float evaluate(double v,UnitFormat from,UnitFormat to,double rate=48000) noexcept {
        double result=from==to?v:unitFromBase(unitToBase(v,from,rate),to,rate);
        return std::isfinite(result)?static_cast<float>(std::clamp(result,-1e30,1e30)):0;
    }
};
EASYKNACK_NODE(UnitConverterNode,"unitconvert")
}
