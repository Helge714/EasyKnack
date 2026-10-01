#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
struct IFFTNode : NodeProcessor {
    std::vector<double> sum,normalization;
    std::vector<std::complex<double>> work;
    std::uint64_t identity=0,nextBlock=0;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    void configure(std::istream& in)override{sources=parameters::IFFTNodeParameters::read(in).sources();}
    void prepare(double,std::size_t block)override{sum.assign(2*(SpectrumStream::maxSize+block+1),0);normalization.assign(sum.size(),0);work.resize(SpectrumStream::maxSize);identity=0;}
    void adoptState(NodeProcessor& previous)noexcept override{auto& old=static_cast<IFFTNode&>(previous);if(sum.size()==old.sum.size()){sum.swap(old.sum);normalization.swap(old.normalization);identity=old.identity;nextBlock=old.nextBlock;}}
    void process(ProcessContext& c)noexcept override{
        auto* stream=static_cast<const SpectrumStream*>(c.resource(0));
        if(!stream||!stream->size){std::fill_n(c.output,c.bufferSize,0);identity=0;return;}
        RenderCadence::include(stream->hop);
        if(identity!=stream->identity||nextBlock!=stream->blockStart){std::fill(sum.begin(),sum.end(),0);std::fill(normalization.begin(),normalization.end(),0);identity=stream->identity;}
        nextBlock=stream->blockStart+c.bufferSize;const auto n=stream->size;const auto length=stream->windowFrames();
        for(std::size_t f=0;f<stream->count;f++){
            const auto& frame=stream->frames[f];std::copy_n(frame.bins.data(),n,work.data());spectrumTransform(work.data(),n,true);
            for(std::size_t i=0;i<length;i++){
                const auto time=frame.start+static_cast<std::int64_t>(length+i);if(time<static_cast<std::int64_t>(stream->blockStart))continue;
                auto slot=static_cast<std::size_t>(time)%sum.size();double window=spectrumWindowValue(stream->window,i,length);
                sum[slot]+=work[i].real()*window;normalization[slot]+=window*window;
            }
        }
        for(std::size_t i=0;i<c.bufferSize;i++){
            // The initial partial overlap contains only zero-padded history. Do
            // not amplify spectral rounding error at the first window's edge.
            auto slot=(stream->blockStart+i)%sum.size();double value=stream->blockStart+i>=length&&normalization[slot]>1e-12?sum[slot]/normalization[slot]:0;
            c.output[i]=std::isfinite(value)?static_cast<float>(value):0;sum[slot]=0;normalization[slot]=0;
        }
    }
};
EASYKNACK_NODE(IFFTNode,"ifft")
}
