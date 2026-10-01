#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../SpectrumStream.hpp"
namespace easyknack {
struct FFTStreamNode : NodeProcessor {
    SpectrumStream stream;
    std::vector<float> ring;
    std::vector<double> weights;
    std::size_t size=2048,hop=512,cursor=0;std::uint64_t end=0;
    SpectrumWindow window=SpectrumWindow::hann;
    char executionRate()const noexcept override{return 'a';}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::spectrum;}
    DspResource* resourceOutput(std::size_t)noexcept override{return &stream;}
    void configure(std::istream& in)override{
        auto p=parameters::FFTStreamNodeParameters::read(in);sources=p.sources();size=std::stoul(p.size_value);window=spectrumWindow(p.window_value);hop=size/std::stoul(p.overlap_value);
        if(size<256||size>SpectrumStream::maxSize||(size&(size-1))||hop<SpectrumStream::minHop)throw std::runtime_error("Invalid streaming FFT size/hop");
    }
    void prepare(double rate,std::size_t block)override{
        stream.prepare(block);ring.assign(size,0);weights.resize(size);for(std::size_t i=0;i<size;i++)weights[i]=spectrumWindowValue(window,i,size);
        stream.size=size;stream.hop=hop;stream.window=window;stream.sampleRate=rate;stream.identity=HistoryResource::nextIdentity.fetch_add(1);cursor=0;end=0;
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& old=static_cast<FFTStreamNode&>(previous);
        if(size==old.size&&hop==old.hop&&window==old.window&&stream.sampleRate==old.stream.sampleRate){ring.swap(old.ring);cursor=old.cursor;end=old.end;stream.identity=old.stream.identity;}
    }
    void process(ProcessContext& c)noexcept override{
        RenderCadence::include(hop);
        stream.count=0;stream.blockStart=end;stream.blockFrames=c.bufferSize;auto input=c.numericInput(0);
        for(std::size_t i=0;i<c.bufferSize;i++){
            ring[cursor]=std::isfinite(input[i])?input[i]:0;cursor=(cursor+1)%size;++end;
            if(end%hop||stream.count>=stream.frames.size())continue;
            auto& frame=stream.frames[stream.count++];frame.start=static_cast<std::int64_t>(end)-static_cast<std::int64_t>(size);
            for(std::size_t j=0;j<size;j++)frame.bins[j]={ring[(cursor+j)%size]*weights[j],0};
            spectrumTransform(frame.bins.data(),size);
        }
    }
};
EASYKNACK_NODE(FFTStreamNode,"fftstream")
}
