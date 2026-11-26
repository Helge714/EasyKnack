#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../dsp/StereoDynamics.hpp"
namespace easyknack {
// Stereo-linked sample-peak limiter. A monotonic deque bounds the entire lookahead
// window in amortized O(1); no callback allocation and no claim of true-peak limiting.
struct LimiterNode final:NodeProcessor {
    struct Entry{std::uint64_t index=0;double peak=0;};
    struct State:dynamics::StereoState {
        std::vector<float> leftDelay,rightDelay;std::vector<Entry> maxima;
        std::size_t position=0,head=0,tail=0,count=0,delay=0;std::uint64_t index=0;double gain=1;
        dynamics::TimeCoefficient release;
        void reset(std::size_t d)noexcept{position=head=tail=count=0;index=0;gain=1;delay=d;std::fill(leftDelay.begin(),leftDelay.end(),0.f);std::fill(rightDelay.begin(),rightDelay.end(),0.f);}
    };
    std::shared_ptr<State> state=std::make_shared<State>();double ceiling=.98,release=100,lookahead=5;bool primary=true;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return false;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void shareChannelState(NodeProcessor& other)override{state=static_cast<LimiterNode&>(other).state;primary=false;}
    void prepare(double fs,std::size_t frames)override{
        auto& s=*state;
        if(s.rate!=fs||s.leftDelay.empty()){s.rate=fs;auto size=static_cast<std::size_t>(std::ceil(fs*.02))+2;s.leftDelay.assign(size,0);s.rightDelay.assign(size,0);s.maxima.resize(size+1);s.reset(static_cast<std::size_t>(std::round(lookahead*fs*.001)));}
        s.right.resize(frames);
    }
    void configure(std::istream& in)override{auto p=parameters::LimiterNodeParameters::read(in);sources=p.sources();ceiling=p.ceiling_value;release=p.release_value;lookahead=p.lookahead_value;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const LimiterNode&>(other);ceiling=p.ceiling;release=p.release;lookahead=p.lookahead;}
    void inheritState(const NodeProcessor& other)noexcept override{
        auto& p=static_cast<const LimiterNode&>(other);auto& a=*state;auto& b=*p.state;
        if(primary&&a.rate==b.rate&&lookahead==p.lookahead&&a.leftDelay.size()==b.leftDelay.size()){
            std::copy(b.leftDelay.begin(),b.leftDelay.end(),a.leftDelay.begin());std::copy(b.rightDelay.begin(),b.rightDelay.end(),a.rightDelay.begin());std::copy(b.maxima.begin(),b.maxima.end(),a.maxima.begin());
            a.position=b.position;a.head=b.head;a.tail=b.tail;a.count=b.count;a.delay=b.delay;a.index=b.index;a.gain=b.gain;
        }
    }
    void adoptState(NodeProcessor& other)noexcept override{
        auto& p=static_cast<LimiterNode&>(other);auto& a=*state;auto& b=*p.state;
        if(primary&&a.rate==b.rate&&lookahead==p.lookahead&&a.leftDelay.size()==b.leftDelay.size()){
            a.leftDelay.swap(b.leftDelay);a.rightDelay.swap(b.rightDelay);a.maxima.swap(b.maxima);
            std::swap(a.position,b.position);std::swap(a.head,b.head);std::swap(a.tail,b.tail);std::swap(a.count,b.count);std::swap(a.delay,b.delay);std::swap(a.index,b.index);std::swap(a.gain,b.gain);
        }
    }
    void process(ProcessContext& c)noexcept override{
        auto& s=*state;if(s.leftDelay.empty()||c.bufferSize>s.right.size()){std::fill_n(c.output,c.bufferSize,0.f);return;}
        if(c.channelIndex!=0){std::copy_n(s.right.data(),c.bufferSize,c.output);return;}
        auto delay=static_cast<std::size_t>(std::round(dynamics::bound(lookahead,0,20,5)*s.rate*.001));if(delay!=s.delay)s.reset(delay);
        auto input=c.numericInput(0),level=c.numericInput(1,ceiling),rel=c.numericInput(2,release);
        auto capacity=s.maxima.size();
        for(std::size_t i=0;i<c.bufferSize;i++,s.index++){
            float l=dynamics::sample(input[i]),r=dynamics::sample(dynamics::rightSample(c,0,i,l));double peak=std::max(std::abs(double(l)),std::abs(double(r)));
            while(s.count&&s.maxima[(s.tail+capacity-1)%capacity].peak<=peak){s.tail=(s.tail+capacity-1)%capacity;--s.count;}
            s.maxima[s.tail]={s.index,peak};s.tail=(s.tail+1)%capacity;++s.count;
            while(s.count&&s.maxima[s.head].index+s.delay<s.index){s.head=(s.head+1)%capacity;--s.count;}
            double limit=dynamics::bound(level[i],0,1,.98),maximum=s.maxima[s.head].peak;
            double target=maximum>limit?limit/maximum:1,coefficient=s.release.get(rel[i],c.sampleRate);
            s.gain=std::min(target,coefficient*s.gain+(1-coefficient)*target);
            auto read=s.position>=s.delay?s.position-s.delay:s.position+s.leftDelay.size()-s.delay;
            float delayedL=s.delay?s.leftDelay[read]:l,delayedR=s.delay?s.rightDelay[read]:r;
            s.leftDelay[s.position]=l;s.rightDelay[s.position]=r;if(++s.position==s.leftDelay.size())s.position=0;
            c.output[i]=static_cast<float>(std::clamp(delayedL*s.gain,-limit,limit));s.right[i]=static_cast<float>(std::clamp(delayedR*s.gain,-limit,limit));
        }
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
};
EASYKNACK_NODE(LimiterNode,"limiter")
}
