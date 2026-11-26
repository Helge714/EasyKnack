#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../dsp/Dynamics.hpp"
namespace easyknack {
struct EnvelopeFollowerNode final:NodeProcessor {
    double attack=5,release=100,envelope=0;bool rms=false;
    dynamics::TimeCoefficient attackCoefficient,releaseCoefficient;
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{auto p=parameters::EnvelopeFollowerNodeParameters::read(in);sources=p.sources();attack=p.attack_value;release=p.release_value;rms=p.mode_value=="rms";}
    void applyParameters(const NodeProcessor& other)noexcept override{auto& p=static_cast<const EnvelopeFollowerNode&>(other);attack=p.attack;release=p.release;if(rms!=p.rms){envelope=rms?std::sqrt(envelope):envelope*envelope;rms=p.rms;}}
    void inheritState(const NodeProcessor& other)noexcept override{auto& p=static_cast<const EnvelopeFollowerNode&>(other);envelope=rms==p.rms?p.envelope:rms?p.envelope*p.envelope:std::sqrt(p.envelope);}
    void process(ProcessContext& c)noexcept override{
        auto input=c.numericInput(0),a=c.numericInput(1,attack),r=c.numericInput(2,release);
        for(std::size_t i=0;i<c.bufferSize;i++){
            double target=std::abs(dynamics::finite(input[i]));if(rms)target*=target;
            double coefficient=target>envelope?attackCoefficient.get(dynamics::bound(a[i],0,2000,5),c.sampleRate):releaseCoefficient.get(r[i],c.sampleRate);
            envelope=coefficient*envelope+(1-coefficient)*target;if(envelope<1e-50)envelope=0;
            c.output[i]=dynamics::sample(rms?std::sqrt(envelope):envelope);
        }
    }
    void processExport(ProcessContext& c)noexcept{process(c);}
};
EASYKNACK_NODE(EnvelopeFollowerNode,"envelopefollower")
}
