#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <array>
#include <utility>

namespace easyknack {
/** Direct eight-comb/four-allpass Freeverb network, independently tuned per ear.
 * Both channels render in the left pass; the right pass only copies its result. */
struct FreeverbNode final : NodeProcessor {
    struct Line { std::size_t offset=0,length=1,position=0;float lowpass=0; };
    struct Bank { std::array<Line,8> comb;std::array<Line,4> allpass; };
    struct State {
        std::array<Bank,2> banks;
        std::vector<float> memory,right;
        double rate=0;
        void prepare(double sampleRate,std::size_t frames) {
            if(rate==sampleRate&&right.size()>=frames&&!memory.empty())return;
            if(!std::isfinite(sampleRate)||sampleRate<=0||frames==0)throw std::invalid_argument("Invalid Freeverb format");
            constexpr int combs[]={1116,1188,1277,1356,1422,1491,1557,1617};
            constexpr int allpasses[]={556,441,341,225};
            std::size_t size=0;
            for(std::size_t channel=0;channel<2;channel++) {
                auto init=[&](Line& line,int tuning){line={};line.offset=size;line.length=std::max<std::size_t>(1,static_cast<std::size_t>(std::floor((tuning+23*channel)*sampleRate/44100.0)));size+=line.length;};
                for(std::size_t i=0;i<8;i++)init(banks[channel].comb[i],combs[i]);
                for(std::size_t i=0;i<4;i++)init(banks[channel].allpass[i],allpasses[i]);
            }
            memory.assign(size,0);right.assign(frames,0);rate=sampleRate;
        }
    };
    std::shared_ptr<State> state=std::make_shared<State>();
    std::array<float,5> values{.5f,.5f,1.f,1.f/3,.5f};
    bool primary=true;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return false;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override {
        auto p=parameters::FreeverbNodeParameters::read(in);sources=p.sources();
        values={static_cast<float>(p.roomSize_value),static_cast<float>(p.damp_value),static_cast<float>(p.width_value),static_cast<float>(p.wet_value),static_cast<float>(p.dry_value)};
    }
    void prepare(double rate,std::size_t frames)override{state->prepare(rate,frames);}
    void shareChannelState(NodeProcessor& other)override{state=static_cast<FreeverbNode&>(other).state;primary=false;}
    void applyParameters(const NodeProcessor& other)noexcept override{values=static_cast<const FreeverbNode&>(other).values;}
    void adoptState(NodeProcessor& previous)noexcept override {
        auto& old=static_cast<FreeverbNode&>(previous);
        if(primary&&state!=old.state&&state->rate==old.state->rate&&state->memory.size()==old.state->memory.size()) {
            state->memory.swap(old.state->memory);std::swap(state->banks,old.state->banks);
        }
    }
    void inheritState(const NodeProcessor& previous)noexcept override {
        const auto& old=static_cast<const FreeverbNode&>(previous);
        if(primary&&state!=old.state&&state->rate==old.state->rate&&state->memory.size()==old.state->memory.size()) {
            std::copy(old.state->memory.begin(),old.state->memory.end(),state->memory.begin());state->banks=old.state->banks;
        }
    }
    static float clean(float value)noexcept{return std::isfinite(value)&&std::abs(value)>=1e-25f?value:0.f;}
    static float bounded(float value,float fallback)noexcept{return std::isfinite(value)?std::clamp(value,0.f,1.f):fallback;}
    static void store(State& s,Line& line,float value)noexcept {
        s.memory[line.offset+line.position]=clean(value);if(++line.position==line.length)line.position=0;
    }
    template<std::size_t I> static float comb(State& s,Bank& b,float input,float feedback,float damp)noexcept {
        auto& line=b.comb[I];float result=s.memory[line.offset+line.position];
        line.lowpass=clean(result*(1-damp)+line.lowpass*damp);store(s,line,input+line.lowpass*feedback);return result;
    }
    template<std::size_t... I> static float sumCombs(State& s,Bank& b,float input,float feedback,float damp,std::index_sequence<I...>)noexcept {
        float result=0;((result+=comb<I>(s,b,input,feedback,damp)),...);return result;
    }
    template<std::size_t I> static float allpass(State& s,Bank& b,float input)noexcept {
        auto& line=b.allpass[I];float delayed=s.memory[line.offset+line.position];store(s,line,input+.5f*delayed);return delayed-input;
    }
    template<std::size_t... I> static float diffuse(State& s,Bank& b,float input,std::index_sequence<I...>)noexcept {
        ((input=allpass<I>(s,b,input)),...);return input;
    }
    struct Coefficients {float feedback,damp,wet1,wet2,dry;};
    static Coefficients coefficients(const std::array<float,5>& p)noexcept {
        float wet=3*p[3];return {.7f+.28f*p[0],.4f*p[1],wet*(.5f+.5f*p[2]),wet*(.5f-.5f*p[2]),2*p[4]};
    }
    void process(ProcessContext& c)noexcept override {
        auto& s=*state;
        if(s.memory.empty()||c.bufferSize>s.right.size()){std::fill_n(c.output,c.bufferSize,0.f);return;}
        if(c.channelIndex!=0){std::copy_n(s.right.data(),c.bufferSize,c.output);return;}
        std::array<ProcessContext::NumericInput,5> parameters;
        bool varying=false;
        for(std::size_t p=0;p<5;p++){
            parameters[p]=c.numericInput(p+1,values[p]);varying|=parameters[p].data&&parameters[p].stride!=0;
            varying|=c.rightInputs&&p+1<c.inputCount&&c.rightInputs[p+1]&&(!c.sourceRates||c.sourceRates[p+1]!='c');
        }
        auto input=c.numericInput(0);auto rightSample=[&](std::size_t port,std::size_t i,float fallback)noexcept {
            auto* data=c.rightInputs&&port<c.inputCount?c.rightInputs[port]:nullptr;
            return data?data[c.sourceRates&&c.sourceRates[port]=='c'?0:i]:fallback;
        };
        std::array<Coefficients,2> k{};
        for(std::size_t i=0;i<c.bufferSize;i++) {
            if(i==0||varying) {
                std::array<float,5> left{},right{};
                for(std::size_t p=0;p<5;p++){left[p]=bounded(parameters[p][i],values[p]);right[p]=bounded(rightSample(p+1,i,left[p]),values[p]);}
                k={coefficients(left),coefficients(right)};
            }
            float left=std::isfinite(input[i])?input[i]:0.f,right=rightSample(0,i,left);if(!std::isfinite(right))right=0;
            float excitation=clean((left+right)*.015f);
            float wetL=diffuse(s,s.banks[0],sumCombs(s,s.banks[0],excitation,k[0].feedback,k[0].damp,std::make_index_sequence<8>{}),std::make_index_sequence<4>{});
            float wetR=diffuse(s,s.banks[1],sumCombs(s,s.banks[1],excitation,k[1].feedback,k[1].damp,std::make_index_sequence<8>{}),std::make_index_sequence<4>{});
            c.output[i]=clean(wetL*k[0].wet1+wetR*k[0].wet2+left*k[0].dry);
            s.right[i]=clean(wetR*k[1].wet1+wetL*k[1].wet2+right*k[1].dry);
        }
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
};
EASYKNACK_NODE(FreeverbNode,"freeverb")
}
