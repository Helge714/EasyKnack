#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"

namespace easyknack {
/** Windowed odd-symmetric FIR: positive frequencies acquire -90 degrees.
 * Direct and quadrature share the same (taps-1)/2 sample group delay.
 */
struct HilbertNode : NodeProcessor {
    std::size_t taps=257, delay=128;
    std::vector<float> history;
    std::vector<double> coefficients;
    std::size_t position=0;
    HilbertNode(){setLength(257);}
    void setLength(std::size_t length){
        if(length!=61&&length!=129&&length!=257&&length!=513&&length!=1025&&length!=2049)
            throw std::runtime_error("Invalid Hilbert filter length");
        taps=length;delay=(taps-1)/2;position=0;
        history.assign(taps,0.f);coefficients.resize(delay/2);
        constexpr double pi=3.14159265358979323846;
        for(std::size_t j=0;j<coefficients.size();++j){
            const auto k=2*j+1;
            coefficients[j]=2./(pi*double(k))*(.54+.46*std::cos(pi*double(k)/delay));
        }
    }
    void configure(std::istream& in) override {
        auto p=parameters::HilbertNodeParameters::read(in);sources=p.sources();setLength(std::stoul(p.length_value));
    }
    std::size_t outputCount()const noexcept override{return 2;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    std::string parameterStructure()const override{return std::to_string(taps);}
    void inheritState(const NodeProcessor& previous)noexcept override{
        const auto& old=static_cast<const HilbertNode&>(previous);
        if(taps==old.taps){std::copy(old.history.begin(),old.history.end(),history.begin());position=old.position;}
    }
    void process(ProcessContext& c)noexcept override{
        const auto input=c.numericInput(0);
        auto* quadrature=c.outputAt(1);
        for(std::size_t i=0;i<c.bufferSize;++i){
            const float value=input[i];history[position]=std::isfinite(value)?value:0.f;
            const auto center=position>=delay?position-delay:position+taps-delay;
            c.output[i]=history[center];
            double sum=0;
            for(std::size_t j=0;j<coefficients.size();++j){
                const auto k=2*j+1;
                const auto older=center>=k?center-k:center+taps-k;
                const auto newer=center+k<taps?center+k:center+k-taps;
                sum+=coefficients[j]*(double(history[older])-history[newer]);
            }
            if(quadrature)quadrature[i]=float(sum);
            if(++position==taps)position=0;
        }
    }
};
EASYKNACK_NODE(HilbertNode,"hilbert")
}
