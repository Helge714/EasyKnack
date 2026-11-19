#pragma once
#include "../src/platform/LinuxPulse.hpp"
#include <cstring>
/** Deterministic server substitute: tests the real Linux adapter on any host.
 * It does not replace target-side PulseAudio/PipeWire acceptance. */
struct FakePulse {
    using Api=easyknack::platform::PulseApi;
    struct Stream {
        FakePulse* owner;std::string device;bool input=false;std::uint32_t monitor=PA_INVALID_INDEX;
        unsigned channels=2;pa_channel_map map{};
        pa_stream_request_cb_t write=nullptr,read=nullptr;void* writeData=nullptr;void* readData=nullptr;
        std::vector<float> pending,played;
    };
    struct Properties {std::map<std::string,std::string> values;};
    inline static FakePulse* current=nullptr;
    std::vector<Stream*> streams;
    std::map<std::string,float> levels{{"first",.5f},{"second",.25f}};
    std::string defaultSink="first";
    std::string failingDevice;
    std::map<std::string,unsigned> channelCounts;
    unsigned operations=0;
    static Stream& stream(pa_stream* s){return *reinterpret_cast<Stream*>(s);}
    static FakePulse& server(pa_context* c){return *reinterpret_cast<FakePulse*>(c);}
    pa_operation* done(){++operations;return reinterpret_cast<pa_operation*>(new FakePulse*(this));}
    pa_channel_map channelMap(const std::string& name){
        pa_channel_map map{};auto found=channelCounts.find(name);map.channels=static_cast<std::uint8_t>(found==channelCounts.end()?2:found->second);
        constexpr pa_channel_position_t positions[]{PA_CHANNEL_POSITION_FRONT_LEFT,PA_CHANNEL_POSITION_FRONT_RIGHT,PA_CHANNEL_POSITION_REAR_LEFT,PA_CHANNEL_POSITION_REAR_RIGHT};
        for(unsigned i=0;i<map.channels;i++)map.map[i]=map.channels==1?PA_CHANNEL_POSITION_MONO:positions[i%4];return map;
    }
    void install(Api& api){
        current=this;
        api.pa_threaded_mainloop_new=[](){return reinterpret_cast<pa_threaded_mainloop*>(current);};
        api.pa_threaded_mainloop_free=[](pa_threaded_mainloop*){};
        api.pa_threaded_mainloop_start=[](pa_threaded_mainloop*){return 0;};
        api.pa_threaded_mainloop_stop=[](pa_threaded_mainloop*){};
        api.pa_threaded_mainloop_lock=[](pa_threaded_mainloop*){};
        api.pa_threaded_mainloop_unlock=[](pa_threaded_mainloop*){};
        api.pa_threaded_mainloop_get_api=[](pa_threaded_mainloop* p){return reinterpret_cast<pa_mainloop_api*>(p);};
        api.pa_context_new=[](pa_mainloop_api* p,const char*){return reinterpret_cast<pa_context*>(p);};
        api.pa_context_unref=[](pa_context*){};api.pa_context_disconnect=[](pa_context*){};
        api.pa_context_connect=[](pa_context*,const char*,pa_context_flags_t,const pa_spawn_api*){return 0;};
        api.pa_context_get_state=[](const pa_context*){return PA_CONTEXT_READY;};
        api.pa_operation_get_state=[](const pa_operation*){return PA_OPERATION_DONE;};api.pa_operation_cancel=[](pa_operation*){};
        api.pa_operation_unref=[](pa_operation* p){auto** owner=reinterpret_cast<FakePulse**>(p);--(*owner)->operations;delete owner;};
        api.pa_context_get_server_info=[](pa_context* c,pa_server_info_cb_t cb,void* data){pa_server_info info{};info.default_sink_name=server(c).defaultSink.c_str();info.default_source_name="microphone";cb(c,&info,data);return server(c).done();};
        api.pa_context_get_sink_info_list=[](pa_context* c,pa_sink_info_cb_t cb,void* data){unsigned index=1;for(auto& [name,level]:server(c).levels){pa_sink_info info{};info.index=index++;info.name=name.c_str();info.description=name.c_str();auto monitor=name+".monitor";info.monitor_source_name=monitor.c_str();info.flags=PA_SINK_HARDWARE;info.channel_map=server(c).channelMap(name);info.volume.channels=info.channel_map.channels;for(unsigned i=0;i<info.volume.channels;i++)info.volume.values[i]=static_cast<pa_volume_t>(std::llround(level*PA_VOLUME_NORM));cb(c,&info,0,data);}cb(c,nullptr,1,data);return server(c).done();};
        api.pa_context_get_source_info_list=[](pa_context* c,pa_source_info_cb_t cb,void* data){pa_source_info info{};info.index=9;info.name="microphone";info.description="Microphone";info.monitor_of_sink=PA_INVALID_INDEX;info.flags=PA_SOURCE_HARDWARE;info.channel_map=server(c).channelMap("microphone");cb(c,&info,0,data);for(auto& [name,level]:server(c).levels){auto monitor=name+".monitor";info.name=monitor.c_str();info.description=monitor.c_str();info.monitor_of_sink=1;info.channel_map=server(c).channelMap(name);cb(c,&info,0,data);}cb(c,nullptr,1,data);return server(c).done();};
        api.pa_proplist_gets=[](const pa_proplist* p,const char* key)->const char*{auto& values=reinterpret_cast<const Properties*>(p)->values;auto found=values.find(key);return found==values.end()?nullptr:found->second.c_str();};
        api.pa_context_get_sink_input_info_list=[](pa_context* c,pa_sink_input_info_cb_t cb,void* data){
            Properties properties{{{PA_PROP_APPLICATION_PROCESS_ID,"987654"},{PA_PROP_APPLICATION_PROCESS_BINARY,"player"},{PA_PROP_APPLICATION_NAME,"Player"}}};
            pa_sink_input_info info{};info.index=42;info.sink=1;info.name="Player";info.channel_map=server(c).channelMap("player");info.proplist=reinterpret_cast<pa_proplist*>(&properties);cb(c,&info,0,data);
            properties.values[PA_PROP_APPLICATION_PROCESS_ID]=std::to_string(getpid());info.index=43;cb(c,&info,0,data);cb(c,nullptr,1,data);return server(c).done();
        };
        api.pa_context_set_sink_volume_by_name=[](pa_context* c,const char* name,const pa_cvolume* v,pa_context_success_cb_t cb,void* data){auto found=server(c).levels.find(name);bool ok=found!=server(c).levels.end();if(ok)found->second=float(v->values[0])/PA_VOLUME_NORM;if(cb)cb(c,ok,data);return server(c).done();};
        api.pa_context_set_sink_mute_by_name=[](pa_context* c,const char*,int,pa_context_success_cb_t cb,void* data){if(cb)cb(c,1,data);return server(c).done();};
        api.pa_stream_new=[](pa_context* c,const char*,const pa_sample_spec* format,const pa_channel_map* map){auto* s=new Stream{&server(c)};s->channels=format->channels;if(map)s->map=*map;server(c).streams.push_back(s);return reinterpret_cast<pa_stream*>(s);};
        api.pa_stream_unref=[](pa_stream* p){auto& s=stream(p);auto& all=s.owner->streams;all.erase(std::remove(all.begin(),all.end(),&s),all.end());delete &s;};
        api.pa_stream_disconnect=[](pa_stream*){return 0;};api.pa_stream_get_state=[](const pa_stream*){return PA_STREAM_READY;};
        api.pa_stream_set_write_callback=[](pa_stream* p,pa_stream_request_cb_t cb,void* data){stream(p).write=cb;stream(p).writeData=data;};
        api.pa_stream_set_read_callback=[](pa_stream* p,pa_stream_request_cb_t cb,void* data){stream(p).read=cb;stream(p).readData=data;};
        api.pa_stream_connect_playback=[](pa_stream* p,const char* name,const pa_buffer_attr*,pa_stream_flags_t,const pa_cvolume*,pa_stream*){stream(p).device=name;return 0;};
        api.pa_stream_connect_record=[](pa_stream* p,const char* name,const pa_buffer_attr*,pa_stream_flags_t){stream(p).device=name;stream(p).input=true;return stream(p).owner->failingDevice==name?-1:0;};
        api.pa_stream_set_monitor_stream=[](pa_stream* p,std::uint32_t index){stream(p).monitor=index;return 0;};
        api.pa_stream_writable_size=[](const pa_stream*)->std::size_t{return 2048;};
        api.pa_stream_write=[](pa_stream* p,const void* data,std::size_t bytes,pa_free_cb_t,std::int64_t,pa_seek_mode_t){auto& s=stream(p);auto* samples=static_cast<const float*>(data);s.played.insert(s.played.end(),samples,samples+bytes/sizeof(float));return 0;};
        api.pa_stream_peek=[](pa_stream* p,const void** data,std::size_t* bytes){auto& s=stream(p);*data=s.pending.empty()?nullptr:s.pending.data();*bytes=s.pending.size()*sizeof(float);return 0;};
        api.pa_stream_drop=[](pa_stream* p){stream(p).pending.clear();return 0;};
    }
    void tick(unsigned frames=256){auto copy=streams;for(auto* s:copy)if(s->write)s->write(reinterpret_cast<pa_stream*>(s),frames*s->channels*sizeof(float),s->writeData);}
    void capture(float value){for(auto* s:streams)if(s->input&&s->read){s->pending.assign(256*s->channels,value);s->read(reinterpret_cast<pa_stream*>(s),s->pending.size()*sizeof(float),s->readData);}}
    float last(const std::string& device){for(auto* s:streams)if(!s->input&&s->device==device&&!s->played.empty())return s->played.back();return 0;}
};
