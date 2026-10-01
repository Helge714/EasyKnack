#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../FunctionCurve.hpp"
namespace easyknack {
struct CurveNode : NodeProcessor {
    bool supportsSampleFeedback() const noexcept override { return true; }
    bool supportsScalarInputs()const noexcept override{return true;}
    FunctionCurve curve;
    float fallback=0,fromMin=0,fromMax=1,toMin=0,toMax=1;
    bool extend=false;
    bool canShareChannels() const noexcept override{return true;}
    bool supportsControlRate() const noexcept override{return true;}
    bool supportsEventRate() const noexcept override{return true;}
    bool evaluatesOnEvents() const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    std::shared_ptr<const void> takeParameterResource()noexcept override{return std::move(curve.storage);}
    void applyParameters(const NodeProcessor& configuration)noexcept override{
        const auto& p=static_cast<const CurveNode&>(configuration);
        curve.storage=p.curve.storage;fallback=p.fallback;fromMin=p.fromMin;fromMax=p.fromMax;toMin=p.toMin;toMax=p.toMax;extend=p.extend;
    }
    void configure(std::istream& in) override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::CurveNodeParameters::read(in);
        sources=p.sources();fallback=p.audio_in_value;fromMin=p.from_min_value;fromMax=p.from_max_value;toMin=p.to_min_value;toMax=p.to_max_value;extend=p.outside_value=="1";curve=std::move(p.curve_value);
        return;}

        sources={readInput(in)};fallback=readNumber(in,-1000000,1000000);
        fromMin=readNumber(in,-1000000,1000000);fromMax=readNumber(in,-1000000,1000000);
        toMin=readNumber(in,-1000000,1000000);toMax=readNumber(in,-1000000,1000000);
        int mode;if(!(in>>mode)||(mode!=0&&mode!=1))throw std::runtime_error("invalid curve range mode");extend=mode==1;curve.read(in);
    }
    void process(ProcessContext& c) noexcept override{
        const auto input=c.numericInput(0);double range=static_cast<double>(fromMax)-fromMin;
        for(std::size_t i=0;i<c.bufferSize;i++){
            double x=input?input[i]:fallback;
            double y=std::abs(range)<1e-12?toMin:toMin+(static_cast<double>(toMax)-toMin)*curve.sample((x-fromMin)/range,extend);
            c.output[i]=std::isfinite(y)?static_cast<float>(std::clamp(y,-1e12,1e12)):0;
        }
    }
};
EASYKNACK_NODE(CurveNode,"curve")
}
