#pragma once
#include "LinuxPulse.hpp"
#include "../AudioGraph.hpp"
#include "../audio/AudioRenderAhead.hpp"
#include <map>
#include <set>

namespace easyknack::platform {
/** Linux devices and loopback share the PulseAudio/PipeWire server clock.
 * Secondary device clocks use bounded adaptive rings, as on Windows. */
class LinuxAudio final : public HostAudioBackend {
    struct Stream {
        LinuxAudio* owner;
        std::vector<std::string> aliases;
        std::string device;
        bool input=false,master=false;
        pa_stream* handle=nullptr;
        StereoRing<131072> ring;
        std::array<float,blockSize*2> block{};
        std::atomic<float> peak{0};
        std::atomic<std::uint64_t> frames{0};
        std::atomic<unsigned> packet{0};
        explicit Stream(LinuxAudio& owner):owner(&owner){}
        ~Stream(){if(handle){auto& api=owner->session->api;api.pa_stream_set_write_callback(handle,nullptr,nullptr);api.pa_stream_set_read_callback(handle,nullptr,nullptr);api.pa_stream_disconnect(handle);api.pa_stream_unref(handle);}}
        bool matches(const std::string& id)const noexcept{for(auto& alias:aliases)if(id==alias)return true;return false;}
    };
    AudioEngine& engine;
    std::shared_ptr<PulseSession> session;
    std::vector<std::unique_ptr<Stream>> streams;
    std::vector<AudioRoute> routes;
    Stream* master=nullptr;
    std::string topology,problem;
    std::uint64_t revision=1;
    bool running=false;
    bool incomplete=false;
    AudioRenderAhead renderAhead;
    float* directOutput=nullptr;
    std::size_t directOffset=0;
    static void capture(pa_stream* handle,std::size_t,void* data){
        auto& stream=*static_cast<Stream*>(data);auto& api=stream.owner->session->api;
        for(;;){const void* samples=nullptr;std::size_t bytes=0;if(api.pa_stream_peek(handle,&samples,&bytes)<0||!bytes)break;
            auto frames=bytes/(2*sizeof(float));stream.ring.write(static_cast<const float*>(samples),frames);
            stream.frames.fetch_add(frames,std::memory_order_relaxed);stream.packet.store(std::max(stream.packet.load(),static_cast<unsigned>(frames)));
            if(api.pa_stream_drop(handle)<0)break;
        }
    }
    static unsigned renderQueued(void* context,float* output,unsigned frames)noexcept{
        auto& self=*static_cast<LinuxAudio*>(context);self.directOutput=output;self.directOffset=0;
        self.engine.render(output,frames,&self,true);self.directOutput=nullptr;return RenderCadence::period;
    }
    static void playback(pa_stream* handle,std::size_t bytes,void* data){
        auto& stream=*static_cast<Stream*>(data);auto& self=*stream.owner;
        std::array<float,blockSize*2> output{};
        while(bytes>=2*sizeof(float)){
            unsigned frames=static_cast<unsigned>(std::min<std::size_t>(blockSize,bytes/(2*sizeof(float))));
            if(stream.master){
                if(self.renderAhead.active())self.renderAhead.read(output.data(),frames);
                else{renderQueued(&self,output.data(),frames);if(RenderCadence::period>1)self.renderAhead.enable(RenderCadence::period);}
            }else stream.ring.readAdaptive(output.data(),frames);
            if(self.session->api.pa_stream_write(handle,output.data(),frames*2*sizeof(float),nullptr,0,PA_SEEK_RELATIVE)<0)break;
            stream.frames.fetch_add(frames,std::memory_order_relaxed);bytes-=frames*2*sizeof(float);
        }
    }
    std::unique_ptr<Stream> open(bool input,const std::string& device,std::uint32_t monitor=PA_INVALID_INDEX){
        auto stream=std::make_unique<Stream>(*this);stream->input=input;stream->device=device;
        pa_sample_spec format{PA_SAMPLE_FLOAT32LE,sampleRate,2};auto& api=session->api;
        stream->handle=api.pa_stream_new(session->context,input?"EasyKnack capture":"EasyKnack output",&format,nullptr);
        if(!stream->handle)throw std::runtime_error("Cannot create PulseAudio stream");
        pa_buffer_attr buffer{static_cast<std::uint32_t>(-1),blockSize*2*sizeof(float)*3,static_cast<std::uint32_t>(-1),blockSize*2*sizeof(float),blockSize*2*sizeof(float)};
        auto flags=static_cast<pa_stream_flags_t>(PA_STREAM_ADJUST_LATENCY|PA_STREAM_AUTO_TIMING_UPDATE|PA_STREAM_INTERPOLATE_TIMING);
        int result;
        if(input){
            if(monitor!=PA_INVALID_INDEX&&api.pa_stream_set_monitor_stream(stream->handle,monitor)<0)throw std::runtime_error("Application monitoring unavailable");
            api.pa_stream_set_read_callback(stream->handle,capture,stream.get());result=api.pa_stream_connect_record(stream->handle,device.c_str(),&buffer,flags);
        }else{
            api.pa_stream_set_write_callback(stream->handle,playback,stream.get());result=api.pa_stream_connect_playback(stream->handle,device.c_str(),&buffer,flags,nullptr,nullptr);
        }
        if(result<0)throw std::runtime_error("Cannot connect audio endpoint: "+device);
        session->ready(stream->handle);return stream;
    }
public:
    LinuxAudio(AudioEngine& engine,std::shared_ptr<PulseSession> session):engine(engine),session(std::move(session)){prepare({});}
    ~LinuxAudio()override{PulseSession::Guard lock(*session);renderAhead.stop();streams.clear();}
    std::uint64_t generation()const override{return revision;}
    AudioBackendCapabilities capabilities()const override{return {true,true,false,false,false};}
    std::string clockDescription()const override{return "PulseAudio / PipeWire-Pulse playback";}
    std::string bufferDescription()const override{return "256-frame graph blocks; server-negotiated device latency";}
    std::vector<AudioEndpoint> enumerateOutputs()override{
        auto state=session->snapshot();std::vector<AudioEndpoint> result{{"default","System Default",AudioEndpointType::HardwareOutput,!state.defaultSink.empty(),false}};
        for(auto& sink:state.sinks)result.push_back({"out:pulse:"+sink.name,sink.label,sink.hardware?AudioEndpointType::HardwareOutput:AudioEndpointType::VirtualOutput,true,false});return result;
    }
    std::vector<AudioEndpoint> enumerateInputs()override{
        auto state=session->snapshot();std::vector<AudioEndpoint> result{{"in:default","System Default",AudioEndpointType::HardwareInput,!state.defaultSource.empty(),false},{"system","System Audio",AudioEndpointType::SystemMix,!state.defaultSink.empty(),false}};
        for(auto& source:state.sources)result.push_back({"in:pulse:"+source.name,source.label,source.monitor?AudioEndpointType::SystemMix:source.hardware?AudioEndpointType::HardwareInput:AudioEndpointType::VirtualInput,true,false});
        std::set<std::string> seen;for(auto& app:state.applications)if(seen.insert(app.identity).second)result.push_back({"app:pulse:"+app.identity,app.name,AudioEndpointType::Application,true,false});return result;
    }
    void prepare(const std::vector<AudioRoute>& selected)override{
        auto state=session->snapshot();std::map<std::pair<bool,std::string>,AudioRoute> unique;
        for(auto& route:selected){if(route.mode!=AudioCaptureMode::Copy)throw std::runtime_error("ROUTE requires an external routing backend");unique[{route.input,route.endpoint}]=route;}
        if(unique.size()>32)throw std::runtime_error("Maximum 32 simultaneous audio endpoints");
        struct Desired {bool input;std::string device;std::uint32_t monitor;std::vector<std::string> aliases;};
        std::map<std::string,Desired> desired;std::string missing;
        auto add=[&](bool input,const std::string& device,std::uint32_t monitor,const std::string& alias){
            if(device.empty()){missing="Audio endpoint unavailable: "+alias;return;}
            auto key=(input?"i:":"o:")+device+":"+std::to_string(monitor);
            auto& entry=desired[key];entry.input=input;entry.device=device;entry.monitor=monitor;entry.aliases.push_back(alias);
        };
        for(auto& [key,route]:unique){
            if(!route.input){auto name=PulseSession::sinkName(state,route.endpoint);auto found=std::find_if(state.sinks.begin(),state.sinks.end(),[&](auto& sink){return sink.name==name;});add(false,found==state.sinks.end()?"":name,PA_INVALID_INDEX,route.endpoint);}
            else if(route.endpoint.rfind("app:pulse:",0)==0){
                bool found=false;for(auto& app:state.applications)if(app.identity==route.endpoint.substr(10)){
                    auto sink=std::find_if(state.sinks.begin(),state.sinks.end(),[&](auto& sink){return sink.index==app.sink;});
                    if(sink!=state.sinks.end()){add(true,sink->monitor,app.index,route.endpoint);found=true;}
                }if(!found)missing="Application audio unavailable: "+route.endpoint;
            }else if(route.endpoint=="system"){
                auto sink=std::find_if(state.sinks.begin(),state.sinks.end(),[&](auto& sink){return sink.name==state.defaultSink;});add(true,sink==state.sinks.end()?"":sink->monitor,PA_INVALID_INDEX,route.endpoint);
            }else{
                auto name=route.endpoint=="in:default"?state.defaultSource:route.endpoint.rfind("in:pulse:",0)==0?route.endpoint.substr(9):std::string{};
                auto source=std::find_if(state.sources.begin(),state.sources.end(),[&](auto& source){return source.name==name;});add(true,source==state.sources.end()?"":name,PA_INVALID_INDEX,route.endpoint);
            }
        }
        bool hasOutput=std::any_of(desired.begin(),desired.end(),[](auto& entry){return !entry.second.input;});
        if(!hasOutput)add(false,state.defaultSink,PA_INVALID_INDEX,"");
        if(desired.size()>64)throw std::runtime_error("Maximum 64 simultaneous audio streams");
        std::string key;for(auto& [id,d]:desired){key+=id+";";for(auto& alias:d.aliases)key+=alias+";";}key+="|"+missing;
        PulseSession::Guard lock(*session);
        bool healthy=std::all_of(streams.begin(),streams.end(),[&](auto& s){return session->api.pa_stream_get_state(s->handle)==PA_STREAM_READY;});
        if(key==topology&&healthy&&!incomplete)return;
        std::vector<std::unique_ptr<Stream>> next;Stream* primary=nullptr;
        bool failed=false;
        for(auto& [id,d]:desired){
            try{auto stream=open(d.input,d.device,d.monitor);stream->aliases=d.aliases;if(!d.input&&!primary)primary=stream.get();next.push_back(std::move(stream));}
            catch(const std::runtime_error& error){failed=true;if(!missing.empty())missing+=" | ";missing+=error.what();}
        }
        if(!primary){
            if(state.defaultSink.empty())throw std::runtime_error("No Linux audio output available");
            auto clock=open(false,state.defaultSink);clock->aliases={""};primary=clock.get();next.push_back(std::move(clock));
        }
        renderAhead.stop();streams.clear();streams=std::move(next);master=primary;master->master=true;
        topology=std::move(key);problem=std::move(missing);routes=selected;incomplete=failed;++revision;renderAhead.start(renderQueued,this);
    }
    void refresh()override{session->snapshot(true);prepare(routes);}
    void setRunning(bool value)override{if(value!=running){running=value;++revision;}}
    std::string status()const override{return problem;}
    float takePeak(const std::string& id)noexcept override{for(auto& stream:streams)if(!stream->input&&stream->matches(id))return stream->peak.exchange(0);return 0;}
    AudioIOStatistics statistics()const override{
        AudioIOStatistics result;for(auto& stream:streams){if(stream->input){result.capturedFrames+=stream->frames.load();result.captureUnder+=stream->ring.underruns.load();result.captureOver+=stream->ring.overruns.load();}else{result.playedFrames+=stream->frames.load();result.playbackUnder+=stream->ring.underruns.load();result.playbackOver+=stream->ring.overruns.load();}result.priming+=stream->ring.priming.load();}result.underruns=result.captureUnder+result.playbackUnder;result.overruns=result.captureOver+result.playbackOver;return result;
    }
    void beginBlock(std::size_t frames)noexcept override{
        for(auto& stream:streams)if(stream->input)stream->ring.readAdaptive(stream->block.data(),frames,std::min<std::size_t>(4096,std::max<unsigned>(256,stream->packet.load()*2)));else std::fill_n(stream->block.data(),frames*2,0.f);
    }
    float input(const std::string& id,std::size_t channel,std::size_t frame)const noexcept override{float result=0;for(auto& stream:streams)if(stream->input&&stream->matches(id))result+=stream->block[frame*2+channel%2];return result;}
    void output(const std::string& id,std::size_t channel,const float* samples,std::size_t frames)noexcept override{for(auto& stream:streams)if(!stream->input&&stream->matches(id))for(std::size_t i=0;i<frames;i++)stream->block[i*2+channel%2]+=samples[i];}
    void endBlock(const float* gain,std::size_t frames)noexcept override{
        for(auto& stream:streams)if(!stream->input){float peak=0;for(std::size_t i=0;i<frames;i++)for(unsigned c=0;c<2;c++){auto& value=stream->block[i*2+c];value=std::clamp(value*gain[i],-1.f,1.f);peak=std::max(peak,std::abs(value));}auto old=stream->peak.load();while(old<peak&&!stream->peak.compare_exchange_weak(old,peak)){}
            if(stream.get()==master&&directOutput)std::copy_n(stream->block.data(),frames*2,directOutput+directOffset*2);else stream->ring.write(stream->block.data(),frames);
        }
        directOffset+=frames;
    }
};
}
