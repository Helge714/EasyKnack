#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../Partials.hpp"
namespace easyknack {
struct PartialsSpectrumNode : NodeProcessor {
    SpectrumStream stream;std::uint64_t identity=0;
    struct State{std::uint64_t id=0;std::int64_t start=0;double delta=0,correction=0;};std::array<State,PartialsResource::capacity> phases{};
    char executionRate()const noexcept override{return 'a';}bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::partials;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{sources=parameters::PartialsSpectrumNodeParameters::read(in).sources();}
    void prepare(double,std::size_t block)override{stream.prepare(block);}
    void inheritState(const NodeProcessor& p)noexcept override{const auto& old=static_cast<const PartialsSpectrumNode&>(p);phases=old.phases;identity=old.identity;}
    void process(ProcessContext& c)noexcept override{
        const auto* in=static_cast<const PartialsResource*>(c.resource(0));if(!in||!in->size){stream.size=stream.count=0;identity=0;return;}
        in->spectrumMetadata(stream);RenderCadence::include(in->hop);
        if(identity!=in->identity){phases={};identity=in->identity;}
        for(std::size_t f=0;f<in->count;f++){
            const auto& frame=in->frames[f];auto& out=stream.frames[f];out.start=frame.start;std::fill_n(out.bins.data(),in->size,std::complex<double>{});
            for(std::size_t i=0;i<frame.count;i++){
                const auto& p=frame.values[i];if(p.slot>=phases.size())continue;auto& state=phases[p.slot];double delta=std::isfinite(p.frequency)?p.frequency-p.originalFrequency:0;
                if(state.id!=p.id)state={p.id,frame.start,delta,0};
                else if(frame.start>state.start)state.correction=std::remainder(state.correction+partialTau*.5*(delta+state.delta)*double(frame.start-state.start)/in->sampleRate,partialTau);
                state.start=frame.start;state.delta=delta;
                addPartial(out,in->size,in->windowSize?in->windowSize:in->size,in->sampleRate,in->window,p.frequency,p.amplitude,p.phase+state.correction);
            }
            SpectrumStream::realSymmetry(out,in->size);
        }
    }
};
EASYKNACK_NODE(PartialsSpectrumNode,"partialsspectrum")
}
