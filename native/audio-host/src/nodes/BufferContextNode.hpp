#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
namespace easyknack {
struct BufferContextNode : NodeProcessor {
    bool canShareChannels()const noexcept override{return true;}
    char executionRate()const noexcept override{return 'a';}
    std::size_t outputCount()const noexcept override{return 2;}
    ResourceKind inputResourceKind(std::size_t port)const noexcept override{return port==0?ResourceKind::history:ResourceKind::none;}
    void configure(std::istream& in)override{auto p=parameters::BufferContextNodeParameters::read(in);sources=p.sources();}
    void process(ProcessContext& c)noexcept override{
        const auto* h=static_cast<const HistoryResource*>(c.resource(0));auto* position=c.outputAt(1);
        const auto size=h?h->samples.size():0;
        std::fill_n(c.output,c.bufferSize,static_cast<float>(size));
        if(position)for(std::size_t i=0;i<c.bufferSize;++i)
            position[i]=!size?0.f:h->immutable?1.f:std::min(float(double(h->writeHeadAt(i))/size),std::nextafter(1.f,0.f));
    }
};
EASYKNACK_NODE(BufferContextNode,"buffercontext")
}
