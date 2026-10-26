#pragma once
#include "ParameterContracts.hpp"
#include "../AverageMeasurement.hpp"
namespace easyknack {
struct ValueAverageNode : NodeProcessor {
    AverageMeasurement measurement;
    double mean=0;float inputValue=0;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t p)const noexcept override{return p==0;}
    std::size_t outputCount()const noexcept override{return 3;}
    bool writesEventBuffer(std::size_t p)const noexcept override{return p==2;}
    void bindIdentity(const std::string& id)override{measurement.start.bind(id+":start");}
    void shareChannelState(NodeProcessor& p)override{measurement.start=static_cast<ValueAverageNode&>(p).measurement.start;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    void configure(std::istream& in)override{auto p=parameters::ValueAverageNodeParameters::read(in);sources=p.sources();inputValue=p.in_value;measurement.configure(p);}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{auto& v=static_cast<const ValueAverageNode&>(p);measurement.parameters(v.measurement);inputValue=v.inputValue;}
    void inheritState(const NodeProcessor& old)noexcept override{
        auto& p=static_cast<const ValueAverageNode&>(old);auto settings=measurement;
        measurement=p.measurement;measurement.parameters(settings);mean=p.mean;
    }
    void process(ProcessContext& c)noexcept override{
        TriggerInput start(c,1,measurement.start);auto input=c.numericInput(0,inputValue);
        bool control=!input||(c.sourceRates&&c.sourceRates[0]=='c');
        AverageMeasurement::clearFinished(c);auto* count=c.outputAt(1);
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool beginning=start.at(i);if(beginning){measurement.begin(c.sampleRate);mean=0;}
            if(measurement.active&&(!control||i==0||beginning)&&std::isfinite(input[i])){
                measurement.record();mean+=(static_cast<double>(input[i])-mean)/measurement.count;
            }
            if(measurement.tick())AverageMeasurement::finish(c,i);
            c.output[i]=static_cast<float>(mean);if(count)count[i]=static_cast<float>(measurement.count);
        }
    }
};
EASYKNACK_NODE(ValueAverageNode,"valueaverage")
}
