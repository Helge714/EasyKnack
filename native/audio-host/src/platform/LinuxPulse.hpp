#pragma once
#include "SharedLibrary.hpp"
#include "HostServices.hpp"
#include "pulse/thread-mainloop.h"
#include "pulse/context.h"
#include "pulse/introspect.h"
#include "pulse/stream.h"
#include <unistd.h>
#include <chrono>
#include <thread>
#include <map>
#include <cmath>
#include <functional>

namespace easyknack::platform {
#define EK_PULSE_FUNCTIONS(X) \
 X(pa_threaded_mainloop_new) X(pa_threaded_mainloop_free) X(pa_threaded_mainloop_start) X(pa_threaded_mainloop_stop) \
 X(pa_threaded_mainloop_get_api) X(pa_threaded_mainloop_lock) X(pa_threaded_mainloop_unlock) \
 X(pa_context_new) X(pa_context_unref) X(pa_context_connect) X(pa_context_disconnect) X(pa_context_get_state) \
 X(pa_operation_get_state) X(pa_operation_cancel) X(pa_operation_unref) \
 X(pa_context_get_server_info) X(pa_context_get_sink_info_list) X(pa_context_get_source_info_list) X(pa_context_get_sink_input_info_list) \
 X(pa_context_load_module) X(pa_context_unload_module) \
 X(pa_context_set_sink_volume_by_name) X(pa_context_set_sink_mute_by_name) X(pa_proplist_gets) \
 X(pa_stream_new) X(pa_stream_unref) X(pa_stream_disconnect) X(pa_stream_get_state) \
 X(pa_stream_set_write_callback) X(pa_stream_set_read_callback) X(pa_stream_connect_playback) X(pa_stream_connect_record) \
 X(pa_stream_set_monitor_stream) X(pa_stream_write) X(pa_stream_peek) X(pa_stream_drop) X(pa_stream_writable_size)
struct PulseApi {
    SharedLibrary library;
#define EK_PULSE_DECLARE(name) decltype(&::name) name=nullptr;
    EK_PULSE_FUNCTIONS(EK_PULSE_DECLARE)
#undef EK_PULSE_DECLARE
    explicit PulseApi(bool load=true){
        if(!load)return;
        if(!library.open("libpulse.so.0"))throw std::runtime_error("PulseAudio client library unavailable (libpulse.so.0)");
#define EK_PULSE_LOAD(name) name=library.symbol<decltype(name)>(#name);if(!name)throw std::runtime_error("Missing PulseAudio API: " #name);
        EK_PULSE_FUNCTIONS(EK_PULSE_LOAD)
#undef EK_PULSE_LOAD
    }
};
#undef EK_PULSE_FUNCTIONS

/** Control-side PulseAudio protocol connection; also works with PipeWire-Pulse.
 * All waits are bounded and release the event-loop lock. Audio callbacks never wait. */
class PulseSession {
public:
    struct Sink {std::uint32_t index;std::string name,label,monitor;pa_cvolume volume;bool muted,hardware;pa_channel_map map;};
    struct Source {std::uint32_t index;std::string name,label;bool monitor,hardware;pa_channel_map map;};
    struct Application {std::uint32_t index,sink;std::string identity,name;};
    struct Snapshot {std::string defaultSink,defaultSource;std::vector<Sink> sinks;std::vector<Source> sources;std::vector<Application> applications;};
    PulseApi api;
    pa_threaded_mainloop* loop=nullptr;
    pa_context* context=nullptr;
    struct Guard {PulseSession& session;explicit Guard(PulseSession& s):session(s){s.api.pa_threaded_mainloop_lock(s.loop);}~Guard(){session.api.pa_threaded_mainloop_unlock(session.loop);}};
private:
    Snapshot cached;
    std::chrono::steady_clock::time_point refreshed{};
    bool started=false;
    template<class Condition> void until(Condition ready){
        auto deadline=std::chrono::steady_clock::now()+std::chrono::seconds(3);
        while(!ready()){
            if(std::chrono::steady_clock::now()>=deadline)throw std::runtime_error("PulseAudio operation timed out");
            auto state=api.pa_context_get_state(context);if(state==PA_CONTEXT_FAILED||state==PA_CONTEXT_TERMINATED)throw std::runtime_error("PulseAudio connection lost");
            api.pa_threaded_mainloop_unlock(loop);std::this_thread::sleep_for(std::chrono::milliseconds(2));api.pa_threaded_mainloop_lock(loop);
        }
    }
    void cleanup()noexcept{if(started)api.pa_threaded_mainloop_stop(loop);if(context){api.pa_context_disconnect(context);api.pa_context_unref(context);}if(loop)api.pa_threaded_mainloop_free(loop);context=nullptr;loop=nullptr;started=false;}
    void connect(){
        loop=api.pa_threaded_mainloop_new();if(!loop)throw std::runtime_error("Cannot create PulseAudio event loop");
        try{
            context=api.pa_context_new(api.pa_threaded_mainloop_get_api(loop),"EasyKnack");if(!context)throw std::runtime_error("Cannot create PulseAudio context");
            if(api.pa_context_connect(context,nullptr,PA_CONTEXT_NOAUTOSPAWN,nullptr)<0)throw std::runtime_error("PulseAudio/PipeWire-Pulse server unavailable");
            if(api.pa_threaded_mainloop_start(loop)<0)throw std::runtime_error("Cannot start PulseAudio event loop");started=true;
            Guard lock(*this);until([&]{return api.pa_context_get_state(context)==PA_CONTEXT_READY;});
        }catch(...){cleanup();throw;}
    }
public:
    PulseSession(){connect();}
    explicit PulseSession(const std::function<void(PulseApi&)>& configure):api(false){configure(api);connect();}
    ~PulseSession(){cleanup();}
    void operation(pa_operation* operation){
        if(!operation)throw std::runtime_error("PulseAudio request rejected");
        try{until([&]{return api.pa_operation_get_state(operation)!=PA_OPERATION_RUNNING;});}
        catch(...){api.pa_operation_cancel(operation);api.pa_operation_unref(operation);throw;}
        auto state=api.pa_operation_get_state(operation);api.pa_operation_unref(operation);
        if(state!=PA_OPERATION_DONE)throw std::runtime_error("PulseAudio request cancelled");
    }
    void ready(pa_stream* stream){until([&]{auto state=api.pa_stream_get_state(stream);if(state==PA_STREAM_FAILED||state==PA_STREAM_TERMINATED)throw std::runtime_error("PulseAudio stream unavailable");return state==PA_STREAM_READY;});}
    Snapshot snapshot(bool force=false){
        Guard lock(*this);auto now=std::chrono::steady_clock::now();if(!force&&now-refreshed<std::chrono::milliseconds(100))return cached;
        Snapshot next;
        operation(api.pa_context_get_server_info(context,[](pa_context*,const pa_server_info* info,void* data){if(info){auto& s=*static_cast<Snapshot*>(data);if(info->default_sink_name)s.defaultSink=info->default_sink_name;if(info->default_source_name)s.defaultSource=info->default_source_name;}},&next));
        operation(api.pa_context_get_sink_info_list(context,[](pa_context*,const pa_sink_info* info,int,void* data){if(info)static_cast<Snapshot*>(data)->sinks.push_back({info->index,info->name?info->name:"",info->description?info->description:info->name,info->monitor_source_name?info->monitor_source_name:"",info->volume,bool(info->mute),bool(info->flags&PA_SINK_HARDWARE),info->channel_map});},&next));
        operation(api.pa_context_get_source_info_list(context,[](pa_context*,const pa_source_info* info,int,void* data){if(info)static_cast<Snapshot*>(data)->sources.push_back({info->index,info->name?info->name:"",info->description?info->description:info->name,info->monitor_of_sink!=PA_INVALID_INDEX,bool(info->flags&PA_SOURCE_HARDWARE),info->channel_map});},&next));
        struct ApplicationQuery {PulseSession* session;Snapshot* snapshot;} query{this,&next};
        operation(api.pa_context_get_sink_input_info_list(context,[](pa_context*,const pa_sink_input_info* info,int,void* data){
            if(!info)return;auto& q=*static_cast<ApplicationQuery*>(data);
            auto property=[&](const char* key){auto* text=q.session->api.pa_proplist_gets(info->proplist,key);return text?std::string(text):std::string{};};
            if(property(PA_PROP_APPLICATION_PROCESS_ID)==std::to_string(getpid()))return;
            auto binary=property(PA_PROP_APPLICATION_PROCESS_BINARY),name=property(PA_PROP_APPLICATION_NAME);
            if(name.empty())name=info->name?info->name:"Application";if(binary.empty())binary=name;
            q.snapshot->applications.push_back({info->index,info->sink,binary,name});
        },&query));
        cached=std::move(next);refreshed=now;return cached;
    }
    template<class Endpoints> static std::pair<std::string,unsigned> selection(const Endpoints& endpoints,const std::string& body){
        for(auto& endpoint:endpoints)if(endpoint.name==body)return {body,0};
        auto split=body.rfind(":channels:");if(split==std::string::npos)return {};
        auto name=body.substr(0,split),number=body.substr(split+10);if(number.empty())return {};
        unsigned first=0;for(char c:number){if(c<'0'||c>'9'||first>=PA_CHANNELS_MAX)return {};first=first*10+unsigned(c-'0');}
        if(first%2||first>=PA_CHANNELS_MAX)return {};
        for(auto& endpoint:endpoints)if(endpoint.name==name)return {name,first};return {};
    }
    static std::pair<std::string,unsigned> sinkSelection(const Snapshot& snapshot,const std::string& id){return id=="default"?std::make_pair(snapshot.defaultSink,0u):id.rfind("out:pulse:",0)==0?selection(snapshot.sinks,id.substr(10)):std::pair<std::string,unsigned>{};}
    static std::pair<std::string,unsigned> sourceSelection(const Snapshot& snapshot,const std::string& id){return id=="in:default"?std::make_pair(snapshot.defaultSource,0u):id.rfind("in:pulse:",0)==0?selection(snapshot.sources,id.substr(9)):std::pair<std::string,unsigned>{};}
    static std::string sinkName(const Snapshot& snapshot,const std::string& id){return sinkSelection(snapshot,id).first;}
    static pa_channel_map channels(pa_channel_map native,unsigned first=0){
        if(!native.channels){native.channels=2;native.map[0]=PA_CHANNEL_POSITION_FRONT_LEFT;native.map[1]=PA_CHANNEL_POSITION_FRONT_RIGHT;}
        pa_channel_map result{};if(first>=native.channels)return result;
        result.channels=static_cast<std::uint8_t>(std::min<unsigned>(2,native.channels-first));
        for(unsigned i=0;i<result.channels;i++)result.map[i]=native.map[first+i];return result;
    }
    DeviceVolumeService::Value volume(const std::string& id){
        auto state=snapshot();auto name=sinkName(state,id);for(auto& sink:state.sinks)if(sink.name==name){double sum=0;for(unsigned i=0;i<sink.volume.channels;i++)sum+=sink.volume.values[i];return {true,sink.volume.channels?float(sum/sink.volume.channels/PA_VOLUME_NORM):0,sink.muted};}return {};
    }
    bool volume(const std::string& id,float scalar){
        if(!std::isfinite(scalar)||scalar<0||scalar>1)throw std::runtime_error("Volume outside 0..1");
        auto state=snapshot();auto name=sinkName(state,id);auto found=std::find_if(state.sinks.begin(),state.sinks.end(),[&](auto& s){return s.name==name;});if(found==state.sinks.end())return false;
        auto volume=found->volume;for(unsigned i=0;i<volume.channels;i++)volume.values[i]=static_cast<pa_volume_t>(std::llround(scalar*PA_VOLUME_NORM));
        Guard lock(*this);int success=0;
        operation(api.pa_context_set_sink_volume_by_name(context,name.c_str(),&volume,[](pa_context*,int value,void* data){*static_cast<int*>(data)=value;},&success));
        if(success&&scalar>0)operation(api.pa_context_set_sink_mute_by_name(context,name.c_str(),0,nullptr,nullptr));
        refreshed={};return success!=0;
    }
};
}
