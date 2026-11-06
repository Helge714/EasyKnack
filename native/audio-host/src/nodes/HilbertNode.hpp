#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <array>

namespace easyknack {
/** Windowed odd-symmetric FIR: positive frequencies acquire -90 degrees.
 * Direct and quadrature share the same 128-sample group delay.
 */
struct HilbertNode : NodeProcessor {
    static constexpr std::size_t taps=257, delay=(taps-1)/2;
    std::array<float,taps> history{};
    std::array<double,delay/2> coefficients{};
    std::size_t position=0;
    HilbertNode(){
        constexpr double pi=3.14159265358979323846;
        for(std::size_t j=0;j<coefficients.size();++j){
            const auto k=2*j+1;
            coefficients[j]=2./(pi*double(k))*(.54+.46*std::cos(pi*double(k)/delay));
        }
    }
    void configure(std::istream& in) override {
        auto p=parameters::HilbertNodeParameters::read(in);sources=p.sources();
    }
    std::size_t outputCount()const noexcept override{return 2;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool supportsSampleFeedback()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void inheritState(const NodeProcessor& previous)noexcept override{
        const auto& old=static_cast<const HilbertNode&>(previous);
        history=old.history;position=old.position;
    }
    void process(ProcessContext& c)noexcept override{
        const auto input=c.numericInput(0);
        auto* quadrature=c.outputAt(1);
        for(std::size_t i=0;i<c.bufferSize;++i){
            const float value=input[i];history[position]=std::isfinite(value)?value:0.f;
            const auto center=(position+taps-delay)%taps;
            c.output[i]=history[center];
            double sum=0;
            for(std::size_t j=0;j<coefficients.size();++j){
                const auto k=2*j+1;
                sum+=coefficients[j]*(double(history[(center+taps-k)%taps])-history[(center+k)%taps]);
            }
            if(quadrature)quadrature[i]=float(sum);
            position=(position+1)%taps;
        }
    }
};
EASYKNACK_NODE(HilbertNode,"hilbert")
}
