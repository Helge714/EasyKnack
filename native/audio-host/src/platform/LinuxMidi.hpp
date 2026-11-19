#pragma once
#include "MidiPortBank.hpp"
#include "SharedLibrary.hpp"
#include <mutex>
#include <thread>
#include <map>
#include <set>
#include <cerrno>
#include <poll.h>
// Upstream sequencer headers normally arrive through asoundlib.h. Only opaque
// configuration/UMP handles and the sequencer API are needed here.
#define __ASOUNDLIB_H
typedef struct _snd_config snd_config_t;
typedef struct _snd_timer_id snd_timer_id_t;
typedef struct _snd_ump_endpoint_info snd_ump_endpoint_info_t;
typedef struct _snd_ump_block_info snd_ump_block_info_t;
#include "alsa/seq_event.h"
#include "alsa/seq.h"
#include "alsa/seqmid.h"
#undef __ASOUNDLIB_H

namespace easyknack::platform {
/** ALSA sequencer input: shared hardware/virtual ports, hotplug and driver taps. */
class LinuxMidi final : public MidiPortBank {
    struct Api {
        SharedLibrary library;
#define EK_ALSA(name) decltype(&name) name=nullptr
        EK_ALSA(snd_seq_open);EK_ALSA(snd_seq_close);EK_ALSA(snd_seq_client_id);
        EK_ALSA(snd_seq_set_client_name);EK_ALSA(snd_seq_create_simple_port);EK_ALSA(snd_seq_delete_simple_port);
        EK_ALSA(snd_seq_connect_from);EK_ALSA(snd_seq_disconnect_from);EK_ALSA(snd_seq_event_input);
        EK_ALSA(snd_seq_client_info_malloc);EK_ALSA(snd_seq_client_info_free);EK_ALSA(snd_seq_client_info_set_client);
        EK_ALSA(snd_seq_query_next_client);EK_ALSA(snd_seq_client_info_get_client);EK_ALSA(snd_seq_client_info_get_name);
        EK_ALSA(snd_seq_port_info_malloc);EK_ALSA(snd_seq_port_info_free);EK_ALSA(snd_seq_port_info_set_client);EK_ALSA(snd_seq_port_info_set_port);
        EK_ALSA(snd_seq_query_next_port);EK_ALSA(snd_seq_port_info_get_port);EK_ALSA(snd_seq_port_info_get_name);EK_ALSA(snd_seq_port_info_get_capability);
#undef EK_ALSA
        bool load(){if(!library.open("libasound.so.2"))return false;
#define EK_ALSA(name) name=library.symbol<decltype(name)>(#name);if(!name)return false
            EK_ALSA(snd_seq_open);EK_ALSA(snd_seq_close);EK_ALSA(snd_seq_client_id);EK_ALSA(snd_seq_set_client_name);
            EK_ALSA(snd_seq_create_simple_port);EK_ALSA(snd_seq_delete_simple_port);EK_ALSA(snd_seq_connect_from);EK_ALSA(snd_seq_disconnect_from);EK_ALSA(snd_seq_event_input);
            EK_ALSA(snd_seq_client_info_malloc);EK_ALSA(snd_seq_client_info_free);EK_ALSA(snd_seq_client_info_set_client);EK_ALSA(snd_seq_query_next_client);EK_ALSA(snd_seq_client_info_get_client);EK_ALSA(snd_seq_client_info_get_name);
            EK_ALSA(snd_seq_port_info_malloc);EK_ALSA(snd_seq_port_info_free);EK_ALSA(snd_seq_port_info_set_client);EK_ALSA(snd_seq_port_info_set_port);EK_ALSA(snd_seq_query_next_port);EK_ALSA(snd_seq_port_info_get_port);EK_ALSA(snd_seq_port_info_get_name);EK_ALSA(snd_seq_port_info_get_capability);
#undef EK_ALSA
            return true;
        }
    } api;
    struct DevicePort {Device device;int client,port;};
    struct Connection {Port* slot;int client,source,destination;};
    snd_seq_t* sequence=nullptr;
    mutable std::mutex mutex;
    std::map<std::uint64_t,Connection> connections;
    std::atomic<bool> stopping{false};
    std::thread reader;
    std::string problem;
    std::vector<DevicePort> discover()const{
        std::vector<DevicePort> result;if(!sequence)return result;
        snd_seq_client_info_t* client=nullptr;snd_seq_port_info_t* port=nullptr;
        if(api.snd_seq_client_info_malloc(&client)<0)return result;
        if(api.snd_seq_port_info_malloc(&port)<0){api.snd_seq_client_info_free(client);return result;}
        api.snd_seq_client_info_set_client(client,-1);std::map<std::string,unsigned> ordinals;
        while(api.snd_seq_query_next_client(sequence,client)>=0){
            int id=api.snd_seq_client_info_get_client(client);if(id==0||id==api.snd_seq_client_id(sequence))continue;
            api.snd_seq_port_info_set_client(port,id);api.snd_seq_port_info_set_port(port,-1);
            while(api.snd_seq_query_next_port(sequence,port)>=0){
                auto caps=api.snd_seq_port_info_get_capability(port);if((caps&(SND_SEQ_PORT_CAP_READ|SND_SEQ_PORT_CAP_SUBS_READ))!=(SND_SEQ_PORT_CAP_READ|SND_SEQ_PORT_CAP_SUBS_READ))continue;
                std::string name=std::string(api.snd_seq_client_info_get_name(client))+" / "+api.snd_seq_port_info_get_name(port);
                auto ordinal=ordinals[name]++;auto key=identity("alsa:"+name+":"+std::to_string(ordinal));
                result.push_back({{key,name+(ordinal?" ("+std::to_string(ordinal+1)+")":"")},id,api.snd_seq_port_info_get_port(port)});
            }
        }
        api.snd_seq_port_info_free(port);api.snd_seq_client_info_free(client);return result;
    }
    static std::uint32_t midi(const snd_seq_event_t& e)noexcept{
        auto note=[&](unsigned type){return type|e.data.note.channel|(std::uint32_t(e.data.note.note)<<8)|(std::uint32_t(e.data.note.velocity)<<16);};
        auto control=[&](unsigned type,unsigned first,unsigned second=0){return type|e.data.control.channel|(first<<8)|(second<<16);};
        switch(e.type){
            case SND_SEQ_EVENT_NOTEON:return note(0x90);case SND_SEQ_EVENT_NOTEOFF:return note(0x80);case SND_SEQ_EVENT_KEYPRESS:return note(0xa0);
            case SND_SEQ_EVENT_CONTROLLER:return control(0xb0,e.data.control.param&127,e.data.control.value&127);
            case SND_SEQ_EVENT_PGMCHANGE:return control(0xc0,e.data.control.value&127);
            case SND_SEQ_EVENT_CHANPRESS:return control(0xd0,e.data.control.value&127);
            case SND_SEQ_EVENT_PITCHBEND:{auto value=std::clamp(e.data.control.value+8192,0,16383);return control(0xe0,value&127,value>>7);}
            case SND_SEQ_EVENT_CLOCK:return 0xf8;case SND_SEQ_EVENT_START:return 0xfa;case SND_SEQ_EVENT_CONTINUE:return 0xfb;case SND_SEQ_EVENT_STOP:return 0xfc;case SND_SEQ_EVENT_SENSING:return 0xfe;
            default:return 0;
        }
    }
    void pump(){
        while(!stopping.load(std::memory_order_acquire)){
            {std::lock_guard<std::mutex> lock(mutex);snd_seq_event_t* event=nullptr;
                for(unsigned n=0;n<1024;n++){
                    int result=api.snd_seq_event_input(sequence,&event);
                    if(result<0){if(result!=-EAGAIN)for(auto& [key,c]:connections)c.slot->queue.panic();break;}
                    if(!event)continue;
                    if(event->type==SND_SEQ_EVENT_PORT_UNSUBSCRIBED){for(auto& [key,c]:connections)if(c.client==event->data.connect.sender.client&&c.source==event->data.connect.sender.port){c.slot->enabled.store(false,std::memory_order_release);c.slot->queue.panic();}continue;}
                    auto message=midi(*event);if(!message)continue;
                    for(auto& [key,c]:connections)if(c.destination==event->dest.port&&c.slot->enabled.load()){deliver(*c.slot,message);break;}
                }
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }
    }
    void disconnect(Connection& c){c.slot->enabled.store(false,std::memory_order_release);c.slot->queue.panic();api.snd_seq_disconnect_from(sequence,c.destination,c.client,c.source);api.snd_seq_delete_simple_port(sequence,c.destination);}
public:
    explicit LinuxMidi(bool hardware=true){
        if(!hardware)return;
        if(!api.load()){problem="ALSA MIDI library unavailable";return;}
        if(api.snd_seq_open(&sequence,"default",SND_SEQ_OPEN_INPUT,SND_SEQ_NONBLOCK)<0){sequence=nullptr;problem="ALSA MIDI sequencer unavailable";return;}
        api.snd_seq_set_client_name(sequence,"EasyKnack");reader=std::thread([this]{pump();});
    }
    ~LinuxMidi()override{stopping.store(true,std::memory_order_release);if(reader.joinable())reader.join();if(sequence){for(auto& [key,c]:connections)disconnect(c);api.snd_seq_close(sequence);}}
    std::vector<Device> enumerate()const override{std::lock_guard<std::mutex> lock(mutex);std::vector<Device> result;for(auto& d:discover())result.push_back(d.device);return result;}
    void refresh()override{
        std::lock_guard<std::mutex> lock(mutex);if(!sequence)return;problem.clear();auto devices=discover();
        std::set<std::uint64_t> requested(wanted.begin(),wanted.end());requested.insert(driverWanted.begin(),driverWanted.end());if(learning)for(auto& d:devices)requested.insert(d.device.key);
        for(auto it=connections.begin();it!=connections.end();){auto& c=it->second;bool present=std::any_of(devices.begin(),devices.end(),[&](auto& d){return d.device.key==it->first&&d.client==c.client&&d.port==c.source;});if(!requested.count(it->first)||!present||!c.slot->enabled.load()){disconnect(c);it=connections.erase(it);}else{c.slot->driver.store(wantsDriver(it->first));++it;}}
        for(auto key:requested){if(key<=1||connections.count(key))continue;auto found=std::find_if(devices.begin(),devices.end(),[&](auto& d){return d.device.key==key;});
            if(found==devices.end()){problem="MIDI input unavailable";continue;}auto* p=slot(key);if(!p){problem="MIDI session device limit reached";continue;}
            int destination=api.snd_seq_create_simple_port(sequence,"Input",SND_SEQ_PORT_CAP_WRITE|SND_SEQ_PORT_CAP_SUBS_WRITE,SND_SEQ_PORT_TYPE_MIDI_GENERIC|SND_SEQ_PORT_TYPE_APPLICATION);
            if(destination<0){problem="Cannot create ALSA MIDI input";continue;}
            if(api.snd_seq_connect_from(sequence,destination,found->client,found->port)<0){api.snd_seq_delete_simple_port(sequence,destination);problem="Cannot connect MIDI input: "+found->device.name;continue;}
            p->queue.panic();p->generation.fetch_add(1);p->driver.store(wantsDriver(key));p->enabled.store(true,std::memory_order_release);
            connections.emplace(key,Connection{p,found->client,found->port,destination});
        }
    }
    std::string status()const override{std::lock_guard<std::mutex> lock(mutex);return problem;}
};
}
