#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <algorithm>
#include <cmath>
#include <vector>
#include <limits>
namespace easyknack {
struct LevelAnalysisNode : NodeProcessor {
    unsigned channel=0;
    double windowMs=50,attackMs=10,releaseMs=100,rate=48000;
    bool smooth=false;
    std::vector<double> squares;
    std::size_t window=1,cursor=0,count=0;
    double sum=0,peakEnvelope=0,rmsEnvelope=0;
    std::size_t outputCount()const noexcept override{return 2;}
    bool supportsControlRate()const noexcept override{return true;}
    bool inputUsesBlockData(std::size_t)const noexcept override{return true;}
    bool mergesVoices()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    void reset()noexcept{cursor=count=0;sum=peakEnvelope=rmsEnvelope=0;}
    void updateWindow()noexcept{window=std::clamp<std::size_t>(static_cast<std::size_t>(std::llround(windowMs*.001*rate)),1,std::max<std::size_t>(1,squares.size()));}
    void prepare(double sampleRate,std::size_t)override{
        rate=sampleRate;squares.resize(static_cast<std::size_t>(std::ceil(rate*2)));updateWindow();reset();
    }
    void configure(std::istream& in)override{
        auto p=parameters::LevelAnalysisNodeParameters::read(in);sources=p.sources();
        if(p.channel_value!="left"&&p.channel_value!="right"&&p.channel_value!="mix")throw std::runtime_error("Invalid level channel");
        channel=p.channel_value=="right"?1:p.channel_value=="mix"?2:0;
        windowMs=std::clamp(p.window_value,.1,2000.);attackMs=std::clamp(p.attack_value,0.,2000.);releaseMs=std::clamp(p.release_value,0.,2000.);smooth=p.smooth_value!=0;
    }
    bool supportsLiveParameters()const noexcept override{return true;}
    void applyParameters(const NodeProcessor& configuration)noexcept override{
        const auto& p=static_cast<const LevelAnalysisNode&>(configuration);
        bool changed=channel!=p.channel||windowMs!=p.windowMs;
        channel=p.channel;windowMs=p.windowMs;attackMs=p.attackMs;releaseMs=p.releaseMs;smooth=p.smooth;
        if(changed){updateWindow();reset();}
    }
    bool compatible(const LevelAnalysisNode& p)const noexcept{return channel==p.channel&&windowMs==p.windowMs&&rate==p.rate&&squares.size()==p.squares.size();}
    void copyScalars(const LevelAnalysisNode& p)noexcept{window=p.window;cursor=p.cursor;count=p.count;sum=p.sum;peakEnvelope=p.peakEnvelope;rmsEnvelope=p.rmsEnvelope;}
    void adoptState(NodeProcessor& old)noexcept override{auto& p=static_cast<LevelAnalysisNode&>(old);if(compatible(p)){squares.swap(p.squares);copyScalars(p);}}
    void inheritState(const NodeProcessor& old)noexcept override{auto& p=static_cast<const LevelAnalysisNode&>(old);if(compatible(p)){std::copy(p.squares.begin(),p.squares.end(),squares.begin());copyScalars(p);}}
    double follow(double value,double previous,std::size_t frames)const noexcept{
        double ms=value>previous?attackMs:releaseMs;
        return !smooth||ms<=0?value:previous+(value-previous)*(-std::expm1(-static_cast<double>(frames)/(rate*.001*ms)));
    }
    void process(ProcessContext& c)noexcept override{
        const auto frames=c.blockFrames?c.blockFrames:c.bufferSize;
        double peak=0;
        if(!squares.empty())for(std::size_t i=0;i<frames;i++){
            double value=0;
            for(std::size_t voice=0;voice<c.inputCount;voice++){
                auto* left=c.input(voice);auto* right=c.rightInputs?c.rightInputs[voice]:left;
                double l=left&&std::isfinite(left[i])?left[i]:0,r=right&&std::isfinite(right[i])?right[i]:0;
                value+=channel==2?(l+r)*.5:channel==1?r:l;
            }
            if(!std::isfinite(value))value=0;
            peak=std::max(peak,std::abs(value));
            if(count==window)sum-=squares[cursor];else ++count;
            squares[cursor]=value*value;sum+=squares[cursor];cursor=(cursor+1)%window;
        }
        double rms=count?std::sqrt(std::max(0.,sum)/count):0;
        peakEnvelope=follow(peak,peakEnvelope,frames);rmsEnvelope=follow(rms,rmsEnvelope,frames);
        auto bounded=[](double v){return static_cast<float>(std::clamp(v,0.,static_cast<double>(std::numeric_limits<float>::max())));};
        std::fill_n(c.output,c.bufferSize,bounded(peakEnvelope));
        if(auto* out=c.outputAt(1))std::fill_n(out,c.bufferSize,bounded(rmsEnvelope));
    }
};
EASYKNACK_NODE(LevelAnalysisNode,"levelanalysis")
}
