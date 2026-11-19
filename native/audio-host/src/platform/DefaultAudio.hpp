#pragma once
#include "HostServices.hpp"
#include "../AudioGraph.hpp"
#include "miniaudio.h"
#include <stdexcept>
#include <atomic>
namespace easyknack::platform {
/** Initial Linux/macOS device boundary: one default stereo output and optional
 * default capture. No process capture, ASIO, virtual routing or callback allocation. */
class DefaultAudio final : public HostAudioBackend {
    AudioEngine& engine;
    ma_context context{};ma_device device{};
    bool contextReady=false,deviceReady=false,capture=false,nullDevice=false,running=false;
    std::uint64_t revision=1;
    std::atomic<std::uint64_t> captured{0},played{0};
    std::atomic<float> peak{0};
    const float* inputData=nullptr;std::size_t cursor=0;
    static void callback(ma_device* device,void* output,const void* input,ma_uint32 frames){
        auto& self=*static_cast<DefaultAudio*>(device->pUserData);
        self.inputData=static_cast<const float*>(input);self.cursor=0;
        self.engine.render(static_cast<float*>(output),frames,&self,true);
        float high=0;auto* data=static_cast<float*>(output);
        for(std::size_t i=0;i<std::size_t(frames)*2;i++)high=std::max(high,std::abs(data[i]));
        auto previous=self.peak.load();while(previous<high&&!self.peak.compare_exchange_weak(previous,high)){}
        if(input)self.captured.fetch_add(frames);self.played.fetch_add(frames);self.inputData=nullptr;
    }
    void open(bool useCapture){
        if(deviceReady){ma_device_uninit(&device);deviceReady=false;}
        capture=useCapture;
        auto config=ma_device_config_init(capture?ma_device_type_duplex:ma_device_type_playback);
        config.playback.format=ma_format_f32;config.playback.channels=2;
        if(capture){config.capture.format=ma_format_f32;config.capture.channels=2;}
        config.sampleRate=sampleRate;config.periodSizeInFrames=blockSize;
        config.dataCallback=callback;config.pUserData=this;
        auto result=ma_device_init(&context,&config,&device);
        if(result!=MA_SUCCESS)throw std::runtime_error(std::string("Default audio device: ")+ma_result_description(result));
        deviceReady=true;
        result=ma_device_start(&device);
        if(result!=MA_SUCCESS){ma_device_uninit(&device);deviceReady=false;throw std::runtime_error(std::string("Audio start: ")+ma_result_description(result));}
    }
public:
    DefaultAudio(AudioEngine& engine,bool useNull):engine(engine),nullDevice(useNull){
        ma_backend backend=ma_backend_null;
        auto result=ma_context_init(useNull?&backend:nullptr,useNull?1:0,nullptr,&context);
        if(result!=MA_SUCCESS)throw std::runtime_error(std::string("Audio context: ")+ma_result_description(result));
        contextReady=true;
        try{open(false);}catch(...){ma_context_uninit(&context);contextReady=false;throw;}
    }
    ~DefaultAudio()override{if(deviceReady)ma_device_uninit(&device);if(contextReady)ma_context_uninit(&context);}
    std::uint64_t generation()const override{return revision;}
    float takePeak(const std::string& id)noexcept override{return id=="default"?peak.exchange(0):0;}
    std::string clockDescription()const override{return std::string(ma_get_backend_name(context.backend))+" default output";}
    std::string bufferDescription()const override{return deviceReady?std::to_string(device.playback.internalPeriodSizeInFrames)+" frames":"unavailable";}
    AudioBackendCapabilities capabilities()const override{return {};}
    std::vector<AudioEndpoint> enumerateInputs()override{
        ma_device_info* info=nullptr;ma_uint32 count=0;bool available=nullDevice;
        if(!nullDevice&&ma_context_get_devices(&context,nullptr,nullptr,&info,&count)==MA_SUCCESS)available=count>0;
        return {{"in:default","Default audio input",AudioEndpointType::HardwareInput,available,false}};
    }
    std::vector<AudioEndpoint> enumerateOutputs()override{return {{"default","Default audio output",AudioEndpointType::HardwareOutput,deviceReady,false}};}
    void prepare(const std::vector<AudioRoute>& routes)override{
        bool wantsCapture=false;
        for(auto& r:routes){
            if(r.mode!=AudioCaptureMode::Copy||(!r.input&&r.endpoint!="default"&&!r.endpoint.empty())||(r.input&&r.endpoint!="in:default"))
                throw std::runtime_error("Endpoint/routing unavailable in the portable default-device backend: "+r.endpoint);
            wantsCapture|=r.input;
        }
        if(wantsCapture!=capture||!deviceReady){++revision;open(wantsCapture);}
    }
    void setRunning(bool value)override{if(value!=running){running=value;++revision;}}
    void refresh()override{}
    std::string status()const override{return deviceReady?"":"Default audio device unavailable";}
    AudioIOStatistics statistics()const override{AudioIOStatistics s;s.capturedFrames=captured.load();s.playedFrames=played.load();return s;}
    void beginBlock(std::size_t)noexcept override{}
    float input(const std::string& id,std::size_t channel,std::size_t frame)const noexcept override{return inputData&&id=="in:default"?inputData[2*(cursor+frame)+(channel%2)]:0;}
    void output(const std::string&,std::size_t,const float*,std::size_t)noexcept override{} // Engine mixes the validated default sinks.
    void endBlock(const float*,std::size_t frames)noexcept override{cursor+=frames;}
};
}
