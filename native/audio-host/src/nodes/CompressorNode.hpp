#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../dsp/StereoDynamics.hpp"
namespace easyknack {
// Feed-forward peak compressor: soft-knee static curve, log-domain gain smoothing.
struct CompressorNode final:NodeProcessor {
    struct State:dynamics::StereoState {std::array<double,2> gain{};std::array<dynamics::TimeCoefficient,2> attack,release;};
    std::shared_ptr<State> state=std::make_shared<State>();
    std::array<double,6> values{-12,4,6,10,100,0};bool link=true,primary=true;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool separatesChannels()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return false;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void shareChannelState(NodeProcessor& other)override{state=static_cast<CompressorNode&>(other).state;primary=false;}
    void prepare(double fs,std::size_t frames)override{if(state->rate!=fs){state->gain={};state->rate=fs;}state->right.resize(frames);}
    void configure(std::istream& in)override{auto p=parameters::CompressorNodeParameters::read(in);sources=p.sources();values={p.threshold_value,p.ratio_value,p.knee_value,p.attack_value,p.release_value,p.makeup_value};link=p.link_value;}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const CompressorNode&>(other);values=p.values;link=p.link;}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const CompressorNode&>(other);if(primary)state->gain=p.state->gain;}
    void adoptState(NodeProcessor& other)noexcept override{inheritState(other);}
    void process(ProcessContext& c)noexcept override{
        auto& s=*state;if(c.bufferSize>s.right.size()){std::fill_n(c.output,c.bufferSize,0.f);return;}
        if(c.channelIndex!=0){std::copy_n(s.right.data(),c.bufferSize,c.output);return;}
        auto input=c.numericInput(0),side=c.numericInput(1);std::array<ProcessContext::NumericInput,6> parameters;
        for(std::size_t k=0;k<6;k++)parameters[k]=c.numericInput(k+2,values[k]);
        for(std::size_t i=0;i<c.bufferSize;i++){
            float l=dynamics::sample(input[i]),r=dynamics::sample(dynamics::rightSample(c,0,i,l));
            double sl=side?dynamics::finite(side[i]):l,sr=dynamics::finite(dynamics::rightSample(c,1,i,side?static_cast<float>(sl):r));
            // A mono sidechain is shared by the host; absent sidechains use each main channel.
            std::array<double,2> detector{std::abs(sl),std::abs(sr)},gains{};
            if(link)detector[0]=std::max(detector[0],detector[1]);
            for(std::size_t ch=0;ch<(link?1u:2u);ch++){
                double p[6];for(std::size_t k=0;k<6;k++)p[k]=ch?dynamics::rightSample(c,k+2,i,parameters[k][i]):parameters[k][i];
                double target=dynamics::reduction(dynamics::decibels(detector[ch]),dynamics::bound(p[0],-120,24,-12),dynamics::bound(p[1],1,100,4),dynamics::bound(p[2],0,48,6));
                double coefficient=target<s.gain[ch]?s.attack[ch].get(dynamics::bound(p[3],0,2000,10),c.sampleRate):s.release[ch].get(p[4],c.sampleRate);
                s.gain[ch]=coefficient*s.gain[ch]+(1-coefficient)*target;
                if(std::abs(s.gain[ch])<1e-12)s.gain[ch]=0; // Avoid denormals during long release tails.
                gains[ch]=dynamics::amplitude(s.gain[ch]+dynamics::bound(p[5],-60,24));
            }
            if(link){s.gain[1]=s.gain[0];gains[1]=gains[0];}
            c.output[i]=dynamics::sample(l*gains[0]);s.right[i]=dynamics::sample(r*gains[1]);
        }
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
};
EASYKNACK_NODE(CompressorNode,"compressor")
}
