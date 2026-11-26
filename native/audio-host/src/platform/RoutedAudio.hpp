#pragma once
#include "HostServices.hpp"
#include "../AudioGraph.hpp"
#include "../audio/AudioRenderAhead.hpp"
#include <map>
#include <set>
#include <stdexcept>

namespace easyknack::platform {
struct DeviceEndpoint {
    AudioEndpoint endpoint;
    std::string device;
    bool input=false;
    unsigned channels=2,first=0;
};
class DeviceStream {
public:
    virtual ~DeviceStream()=default;
    virtual void start()=0;
    virtual void stop()noexcept=0;
    virtual bool healthy()const noexcept{return true;}
};
/** OS adapters own discovery, stream conversion and permissions. Callbacks provide
 * interleaved float PCM at the graph rate; routing/DSP never calls an OS API. */
class DeviceDriver {
public:
    using Callback=void(*)(void*,float*,const float*,unsigned)noexcept;
    virtual ~DeviceDriver()=default;
    virtual std::vector<DeviceEndpoint> endpoints()=0;
    virtual std::unique_ptr<DeviceStream> open(const DeviceEndpoint&,Callback,void*)=0;
    virtual AudioBackendCapabilities capabilities()const=0;
    virtual std::string name()const=0;
    virtual std::uint64_t generation()const noexcept{return 0;}
    virtual std::string volumeKey(const std::string&){return {};}
    virtual DeviceVolumeService::Value volume(const std::string&){return {};}
    virtual bool volume(const std::string&,float){return false;}
};

/** Device-independent multi-endpoint engine. All stream replacement occurs after
 * stopping callbacks and the single DSP worker. Secondary clocks use bounded rings. */
class RoutedAudio final : public HostAudioBackend {
    struct Stream {
        RoutedAudio* owner;
        DeviceEndpoint endpoint;
        std::vector<std::string> aliases;
        std::unique_ptr<DeviceStream> device;
        StereoRing<131072> ring;
        std::array<float,blockSize*2> block{};
        std::atomic<float> peak{0};
        std::atomic<std::uint64_t> frames{0};
        float reportedPeak=0;
        bool master=false;
        explicit Stream(RoutedAudio* owner):owner(owner){}
        bool matches(const std::string& id)const noexcept{return std::find(aliases.begin(),aliases.end(),id)!=aliases.end();}
        ~Stream(){if(device)device->stop();}
    };
    AudioEngine& engine;
    std::shared_ptr<DeviceDriver> driver;
    std::vector<std::unique_ptr<Stream>> streams;
    std::vector<AudioRoute> routes;
    AudioRenderAhead renderAhead;
    std::string topology,problem;
    bool running=false,readingPeaks=false,incomplete=false;
    std::uint64_t revision=1;
    std::uint64_t driverGeneration=0;
    float* direct=nullptr;
    std::size_t offset=0;
    static unsigned render(void* context,float* samples,unsigned frames)noexcept{
        auto& self=*static_cast<RoutedAudio*>(context);self.direct=samples;self.offset=0;
        self.engine.render(samples,frames,&self,true);self.direct=nullptr;return RenderCadence::period;
    }
    static void callback(void* context,float* output,const float* input,unsigned frames)noexcept{
        auto& stream=*static_cast<Stream*>(context);auto& self=*stream.owner;auto& endpoint=stream.endpoint;
        std::array<float,blockSize*2> stereo{};
        if(output)std::fill_n(output,std::size_t(frames)*endpoint.channels,0.f);
        for(unsigned start=0;start<frames;start+=blockSize){
            unsigned count=std::min<unsigned>(blockSize,frames-start);
            if(endpoint.input){
                for(unsigned i=0;i<count;i++)for(unsigned c=0;c<2;c++)stereo[2*i+c]=input?input[(start+i)*endpoint.channels+std::min(endpoint.first+c,endpoint.channels-1)]:0;
                stream.ring.write(stereo.data(),count);
            }else if(output){
                if(stream.master){if(self.renderAhead.active())self.renderAhead.read(stereo.data(),count);else{render(&self,stereo.data(),count);if(RenderCadence::period>1)self.renderAhead.enable(RenderCadence::period);}}
                else stream.ring.readAdaptive(stereo.data(),count);
                for(unsigned i=0;i<count;i++){
                    if(endpoint.first+1<endpoint.channels){output[(start+i)*endpoint.channels+endpoint.first]=stereo[2*i];output[(start+i)*endpoint.channels+endpoint.first+1]=stereo[2*i+1];}
                    else output[(start+i)*endpoint.channels+endpoint.first]=(stereo[2*i]+stereo[2*i+1])*.5f;
                }
            }
        }
        stream.frames.fetch_add(frames,std::memory_order_relaxed);
    }
    void stop()noexcept{for(auto& stream:streams)if(stream->device)stream->device->stop();renderAhead.stop();}
public:
    RoutedAudio(AudioEngine& engine,std::shared_ptr<DeviceDriver> driver):engine(engine),driver(std::move(driver)){prepare({});}
    ~RoutedAudio()override{stop();}
    AudioBackendCapabilities capabilities()const override{return driver->capabilities();}
    std::uint64_t generation()const override{return revision;}
    std::string clockDescription()const override{return driver->name();}
    std::string bufferDescription()const override{return "256-frame graph; device conversion and adaptive secondary clocks";}
    std::string status()const override{return problem;}
    std::vector<AudioEndpoint> enumerateInputs()override{std::vector<AudioEndpoint> result;for(auto& e:driver->endpoints())if(e.input)result.push_back(e.endpoint);return result;}
    std::vector<AudioEndpoint> enumerateOutputs()override{std::vector<AudioEndpoint> result;for(auto& e:driver->endpoints())if(!e.input)result.push_back(e.endpoint);return result;}
    void prepare(const std::vector<AudioRoute>& selected)override{
        auto observedGeneration=driver->generation();auto endpoints=driver->endpoints();std::map<std::string,std::pair<DeviceEndpoint,std::vector<std::string>>> desired;
        std::string missing;
        if(selected.size()>32)throw std::runtime_error("Maximum 32 audio routes");
        auto add=[&](const DeviceEndpoint& e,const std::string& alias){
            if(!e.channels||e.first>=e.channels||e.channels>64)throw std::runtime_error("Invalid endpoint channel map");
            auto key=std::string(e.input?"i:":"o:")+e.device+":"+std::to_string(e.first)+":"+std::to_string(e.channels);
            auto& entry=desired[key];entry.first=e;if(std::find(entry.second.begin(),entry.second.end(),alias)==entry.second.end())entry.second.push_back(alias);
        };
        for(auto& route:selected){
            if(route.mode!=AudioCaptureMode::Copy)throw std::runtime_error("Audio Route mode requires external application routing");
            auto found=std::find_if(endpoints.begin(),endpoints.end(),[&](auto& e){return e.input==route.input&&e.endpoint.id==route.endpoint&&e.endpoint.available;});
            if(found==endpoints.end()){missing+="Audio endpoint unavailable: "+route.endpoint+"; ";continue;}add(*found,route.endpoint);
        }
        if(std::none_of(desired.begin(),desired.end(),[](auto& e){return !e.second.first.input;})){
            auto found=std::find_if(endpoints.begin(),endpoints.end(),[](auto& e){return !e.input&&e.endpoint.id=="default"&&e.endpoint.available;});
            if(found==endpoints.end())throw std::runtime_error("No default audio output available");add(*found,"");
        }
        std::string key=missing;for(auto& [id,d]:desired){key+=id;for(auto& alias:d.second)key+=";"+alias;}
        if(key==topology&&!incomplete&&std::all_of(streams.begin(),streams.end(),[](auto& stream){return stream->device->healthy();})){driverGeneration=observedGeneration;return;}
        std::vector<std::unique_ptr<Stream>> next;Stream* master=nullptr;bool failed=false;
        for(auto& [id,d]:desired){
            auto stream=std::make_unique<Stream>(this);stream->endpoint=d.first;stream->aliases=d.second;
            try{stream->device=driver->open(stream->endpoint,callback,stream.get());if(!stream->device)throw std::runtime_error("Audio driver returned no stream");}
            catch(const std::exception& error){missing+=error.what();missing+="; ";failed=true;continue;}
            if(!stream->endpoint.input&&!master)master=stream.get();next.push_back(std::move(stream));
        }
        if(!master)throw std::runtime_error("Cannot open an audio output: "+missing);
        stop();streams=std::move(next);master->master=true;topology=std::move(key);problem=std::move(missing);incomplete=failed;routes=selected;++revision;
        renderAhead.start(render,this);
        try{for(auto& stream:streams)if(!stream->master)stream->device->start();master->device->start();driverGeneration=observedGeneration;}
        catch(...){stop();streams.clear();topology.clear();incomplete=true;throw;}
    }
    void refresh()override{topology.clear();prepare(routes);}
    void poll()override{if(driverGeneration!=driver->generation())try{prepare(routes);}catch(const std::exception& error){problem=error.what();}}
    void setRunning(bool value)override{if(value!=running){running=value;++revision;}}
    void beginPeakRead()noexcept override{for(auto& stream:streams)stream->reportedPeak=stream->peak.exchange(0);readingPeaks=true;}
    void endPeakRead()noexcept override{readingPeaks=false;}
    float takePeak(const std::string& id)noexcept override{for(auto& s:streams)if(!s->endpoint.input&&s->matches(id))return readingPeaks?s->reportedPeak:s->peak.exchange(0);return 0;}
    AudioIOStatistics statistics()const override{AudioIOStatistics result;for(auto& s:streams){if(s->endpoint.input){result.capturedFrames+=s->frames.load();result.captureUnder+=s->ring.underruns.load();result.captureOver+=s->ring.overruns.load();}else{result.playedFrames+=s->frames.load();result.playbackUnder+=s->ring.underruns.load();result.playbackOver+=s->ring.overruns.load();}result.priming+=s->ring.priming.load();}result.underruns=result.captureUnder+result.playbackUnder;result.overruns=result.captureOver+result.playbackOver;return result;}
    void beginBlock(std::size_t frames)noexcept override{for(auto& s:streams)if(s->endpoint.input)s->ring.readAdaptive(s->block.data(),frames);else std::fill_n(s->block.data(),frames*2,0.f);}
    float input(const std::string& id,std::size_t channel,std::size_t frame)const noexcept override{float value=0;for(auto& s:streams)if(s->endpoint.input&&s->matches(id))value+=s->block[frame*2+channel%2];return value;}
    void output(const std::string& id,std::size_t channel,const float* samples,std::size_t frames)noexcept override{for(auto& s:streams)if(!s->endpoint.input&&s->matches(id))for(std::size_t i=0;i<frames;i++)s->block[i*2+channel%2]+=samples[i];}
    void endBlock(const float* gain,std::size_t frames)noexcept override{
        for(auto& s:streams)if(!s->endpoint.input){float peak=0;for(std::size_t i=0;i<frames;i++)for(unsigned c=0;c<2;c++){auto& value=s->block[i*2+c];value=std::clamp(value*gain[i],-1.f,1.f);peak=std::max(peak,std::abs(value));}auto old=s->peak.load();while(old<peak&&!s->peak.compare_exchange_weak(old,peak)){}
            if(s->master&&direct)std::copy_n(s->block.data(),frames*2,direct+offset*2);else s->ring.write(s->block.data(),frames);
        }offset+=frames;
    }
};
}
