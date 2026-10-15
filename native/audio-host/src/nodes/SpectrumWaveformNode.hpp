#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include "../WaveformCapture.hpp"
#include "../AnalysisData.hpp"
#include "../Analysis.hpp"
#include "../TriggerInput.hpp"
#include <cstring>
namespace easyknack {
struct SpectrumWaveformNode:NodeProcessor {
    std::unique_ptr<WaveformResource> wave=std::make_unique<WaveformResource>();
    std::unique_ptr<WaveformCapture> job;
    std::shared_ptr<AnalysisMailbox> mailbox;
    ManualTriggerReceiver capture;
    std::string saved;
    double frequency=440;bool normalize=true,pending=false,publish=false;
    bool supportsControlRate()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsScalarInputs()const noexcept override{return true;}
    bool isObserver()const noexcept override{return true;}
    ResourceKind inputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::spectrum:ResourceKind::none;}
    ResourceKind outputResourceKind(std::size_t)const noexcept override{return ResourceKind::waveform;}
    DspResource* resourceOutput(std::size_t)noexcept override{return wave.get();}
    void bindIdentity(const std::string& id)override{capture.bind(id+":capture");mailbox=analysisMailbox(id);}
    void prepare(double,std::size_t)override{job=std::make_unique<WaveformCapture>();}
    void configure(std::istream& in)override{
        auto p=parameters::SpectrumWaveformNodeParameters::read(in);sources=p.sources();frequency=p.frequency_value;normalize=p.normalize_value;saved=p.saved_value;
        if(p.saved_value!="empty"){
            if(p.saved_value.size()!=WaveformResource::size*8)throw std::runtime_error("Invalid waveform samples");
            std::array<float,WaveformResource::size> samples{};
            for(std::size_t i=0;i<samples.size();i++){auto text=p.saved_value.substr(i*8,8);std::size_t consumed=0;auto bits=static_cast<std::uint32_t>(std::stoul(text,&consumed,16));if(consumed!=8)throw std::runtime_error("Invalid waveform sample");std::memcpy(&samples[i],&bits,4);if(!std::isfinite(samples[i]))throw std::runtime_error("Non-finite waveform sample");}
            wave->fromSamples(samples);
        }
    }
    void adoptState(NodeProcessor& previous)noexcept override{
        auto& old=static_cast<SpectrumWaveformNode&>(previous);if(saved==old.saved){wave.swap(old.wave);job.swap(old.job);pending=old.pending;publish=old.publish;}capture.seen=old.capture.seen;
    }
    void process(ProcessContext& c)noexcept override{
        if(c.channelIndex||!job)return;
        if(job->state.load(std::memory_order_acquire)==2){wave.swap(job->result);job->state.store(0,std::memory_order_release);publish=true;}
        TriggerInput trigger(c,2,capture);
        for(std::size_t i=0;i<(c.blockFrames?c.blockFrames:c.bufferSize);i++)if(trigger.at(i))pending=true;
        auto* resource=c.resource(0);auto* input=resource&&(resource->kind()==ResourceKind::spectrum||resource->kind()==ResourceKind::analysis)?static_cast<const AnalysisResource*>(resource):nullptr;
        if(pending&&input){auto* result=input->analysisResult();if(result&&result->spectrum.valid())pending=!job->submit(result->spectrum,c.numericInput(1,float(frequency))[0],normalize);}
        if(publish&&mailbox){AnalysisFrame frame;frame.span=1;frame.valueCount=WaveformResource::size;std::copy(wave->samples.begin(),wave->samples.end(),frame.values.begin());mailbox->publish(frame);publish=false;}
    }
};
EASYKNACK_NODE(SpectrumWaveformNode,"spectrumwaveform")
}
