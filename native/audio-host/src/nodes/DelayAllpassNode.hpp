#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../dsp/Dynamics.hpp"
namespace easyknack {
struct DelayAllpassNode final:NodeProcessor {
    double time=.01,feedback=.5,rate=0;std::vector<float> memory;std::size_t position=0;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::DelayAllpassNodeParameters::read(in);sources=p.sources();time=p.time_value;feedback=p.feedback_value;}
    void prepare(double fs,std::size_t)override{if(fs!=rate||memory.empty()){rate=fs;memory.assign(static_cast<std::size_t>(std::ceil(fs))+1,0);position=0;}}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const DelayAllpassNode&>(other);time=p.time;feedback=p.feedback;}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const DelayAllpassNode&>(other);if(rate==p.rate&&memory.size()==p.memory.size()){std::copy(p.memory.begin(),p.memory.end(),memory.begin());position=p.position;}}
    void adoptState(NodeProcessor& other)noexcept override{auto& p=static_cast<DelayAllpassNode&>(other);if(rate==p.rate&&memory.size()==p.memory.size()){memory.swap(p.memory);std::swap(position,p.position);}}
    void process(ProcessContext& c)noexcept override{
        if(memory.empty()){std::fill_n(c.output,c.bufferSize,0.f);return;}
        auto input=c.numericInput(0),t=c.numericInput(1,time),g=c.numericInput(2,feedback);
        auto delay=static_cast<std::size_t>(std::clamp(std::round(dynamics::bound(t[0],0,1,time)*rate),1.,double(memory.size()-1)));
        for(std::size_t i=0;i<c.bufferSize;i++){
            auto read=position>=delay?position-delay:position+memory.size()-delay;
            double gain=dynamics::bound(g[i],-.99,.99,.5),x=dynamics::finite(input[i]),y=memory[read]-gain*x;
            memory[position]=dynamics::sample(x+gain*y);if(++position==memory.size())position=0;c.output[i]=dynamics::sample(y);
        }
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
};
EASYKNACK_NODE(DelayAllpassNode,"delayallpass")
}
