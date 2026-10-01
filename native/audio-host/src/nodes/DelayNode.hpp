#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
#include <cmath>
#include <vector>
namespace easyknack {
// One implementation for feed-forward and feedback delays. All storage is prepared
// off the audio thread; feedback reads a whole chunk before committing any inputs.
struct DelayNode : NodeProcessor {
    static constexpr double maxDelaySeconds=10.0;
    double preparedRate=0,latchedSamples=0;
    float inputValue=0,timeValue=.25f;
    std::vector<float> buffer;
    std::size_t writeIndex=0;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return false;}
    bool defersInputs()const noexcept override{return true;}
    bool defersInput(std::size_t port)const noexcept override{return port==0;}
    bool sampleFeedbackBoundary()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{
        in>>std::ws;if(in.peek()=='!'){auto p=parameters::DelayNodeParameters::read(in);sources=p.sources();inputValue=p.audio_in_value;timeValue=p.time_value;}
        else{sources={readInput(in)};inputValue=readNumber(in,-1,1);sources.push_back(readInput(in));timeValue=readNumber(in,0,maxDelaySeconds);}
    }
    void prepare(double rate,std::size_t frames)override{
        if(!std::isfinite(rate)||rate<=0||!frames)throw std::invalid_argument("Invalid delay format");
        if(preparedRate==rate&&!buffer.empty())return;
        buffer.assign(static_cast<std::size_t>(std::ceil(rate*maxDelaySeconds))+2,0.f);preparedRate=rate;writeIndex=0;
    }
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const DelayNode&>(other);inputValue=p.inputValue;timeValue=p.timeValue;}
    void adoptState(NodeProcessor& other)noexcept override{auto& old=static_cast<DelayNode&>(other);if(preparedRate==old.preparedRate&&buffer.size()==old.buffer.size()){buffer.swap(old.buffer);std::swap(writeIndex,old.writeIndex);}}
    void inheritState(const NodeProcessor& other)noexcept override{auto& old=static_cast<const DelayNode&>(other);if(preparedRate==old.preparedRate&&buffer.size()==old.buffer.size()){std::copy(old.buffer.begin(),old.buffer.end(),buffer.begin());writeIndex=old.writeIndex;}}
    double delaySamples(ProcessContext& c)const noexcept{
        double time=c.numericInput(1,timeValue)[0];if(!std::isfinite(time))time=0;
        double samples=std::clamp(time,0.0,maxDelaySeconds)*preparedRate;
        // Seconds are transmitted as floats; preserve exact integer sample settings.
        double nearest=std::round(samples);return std::abs(samples-nearest)<.0001?nearest:samples;
    }
    std::size_t minimumFeedbackFrames(double rate,std::size_t)const noexcept override{
        return sources.size()>1&&sources[1]>=0?1:static_cast<std::size_t>(std::max(1.0,std::floor(timeValue*rate+.0001)));
    }
    std::size_t beginFeedbackBlock(ProcessContext& c)noexcept override{
        latchedSamples=std::max(1.0,delaySamples(c));return static_cast<std::size_t>(std::floor(latchedSamples));
    }
    float read(double delay,std::size_t offset=0)const noexcept{
        double position=static_cast<double>(writeIndex+offset)-delay;
        if(position<0)position+=buffer.size();if(position>=buffer.size())position-=buffer.size();
        auto a=static_cast<std::size_t>(position);auto b=(a+1)%buffer.size();float f=static_cast<float>(position-a);
        return buffer[a]+(buffer[b]-buffer[a])*f;
    }
    void store(float value)noexcept{buffer[writeIndex]=std::isfinite(value)?value:0.f;if(++writeIndex==buffer.size())writeIndex=0;}
    void process(ProcessContext& c)noexcept override{
        if(buffer.empty()){std::fill_n(c.output,c.bufferSize,0.f);return;}
        const double delay=delaySamples(c);auto input=c.numericInput(0,inputValue);
        for(std::size_t i=0;i<c.bufferSize;i++){
            float value=std::isfinite(input[i])?input[i]:0.f;buffer[writeIndex]=value;
            c.output[i]=delay<=0?value:read(delay);store(value);
        }
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
    void processSample(ProcessContext& c)noexcept override{for(std::size_t i=0;i<c.bufferSize;i++)c.output[i]=read(latchedSamples,i);}
    void commitSample(ProcessContext& c)noexcept override{auto input=c.numericInput(0,inputValue);for(std::size_t i=0;i<c.bufferSize;i++)store(input[i]);}
};
EASYKNACK_NODE(DelayNode,"delay")
}
