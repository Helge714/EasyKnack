#pragma once
#include "ParameterContracts.hpp"
#include "../AverageMeasurement.hpp"
namespace easyknack {
struct ValueAverageNode : NodeProcessor {
    AverageMeasurement measurement;
    double mean=0,m2=0;float deviation=0,minimum=0,maximum=0,inputValue=0;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t p)const noexcept override{return p==0;}
    std::size_t outputCount()const noexcept override{return 6;}
    bool writesEventBuffer(std::size_t p)const noexcept override{return p==2;}
    void bindIdentity(const std::string& id)override{measurement.start.bind(id+":start");}
    void shareChannelState(NodeProcessor& p)override{measurement.start=static_cast<ValueAverageNode&>(p).measurement.start;}
    void shareVoiceState(NodeProcessor& p)override{shareChannelState(p);}
    void configure(std::istream& in)override{auto p=parameters::ValueAverageNodeParameters::read(in);sources=p.sources();inputValue=p.in_value;measurement.configure(p);}
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& p)noexcept override{auto& v=static_cast<const ValueAverageNode&>(p);measurement.parameters(v.measurement);inputValue=v.inputValue;}
    void inheritState(const NodeProcessor& old)noexcept override{
        auto& p=static_cast<const ValueAverageNode&>(old);auto settings=measurement;
        measurement=p.measurement;measurement.parameters(settings);mean=p.mean;m2=p.m2;
        deviation=p.deviation;minimum=p.minimum;maximum=p.maximum;
    }
    void process(ProcessContext& c)noexcept override{
        TriggerInput start(c,1,measurement.start);auto input=c.numericInput(0,inputValue);
        bool control=!input||(c.sourceRates&&c.sourceRates[0]=='c');
        AverageMeasurement::clearFinished(c);auto* count=c.outputAt(1);
        auto* deviations=c.outputAt(3);auto* minima=c.outputAt(4);auto* maxima=c.outputAt(5);
        for(std::size_t i=0;i<c.bufferSize;i++){
            bool beginning=start.at(i);if(beginning){measurement.begin(c.sampleRate);mean=m2=0;deviation=minimum=maximum=0;}
            if(measurement.active&&(!control||i==0||beginning)&&std::isfinite(input[i])){
                measurement.record();double value=input[i],delta=value-mean;
                mean+=delta/measurement.count;
                // Welford's recurrence avoids cancellation for small variations around a large mean.
                m2+=delta*(value-mean);
                deviation=static_cast<float>(std::sqrt(std::max(0.,m2/measurement.count)));
                if(measurement.count==1)minimum=maximum=input[i];
                else{minimum=std::min(minimum,input[i]);maximum=std::max(maximum,input[i]);}
            }
            if(measurement.tick())AverageMeasurement::finish(c,i);
            c.output[i]=static_cast<float>(mean);if(count)count[i]=static_cast<float>(measurement.count);
            if(deviations)deviations[i]=deviation;if(minima)minima[i]=minimum;if(maxima)maxima[i]=maximum;
        }
    }
};
EASYKNACK_NODE(ValueAverageNode,"valueaverage")
}
