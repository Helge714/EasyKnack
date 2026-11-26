#pragma once
#include "LinuxPulse.hpp"
#include <set>
#include <map>
namespace easyknack::platform {
/** Linux desktops own media-key bindings. Follow their default-sink volume
 * changes while linking is explicitly enabled, preserving selected proportions. */
class LinuxVolume final : public DeviceVolumeService {
    std::shared_ptr<PulseSession> session;
    std::set<std::string> linked;
    std::map<std::string,float> proportions;
    Value previous;
    std::string defaultName;
    std::uint64_t sequence=0;
    float keyLevel=0;
    unsigned keyTargets=0;
    bool active=false;
    void baseline(){auto state=session->snapshot();defaultName=state.defaultSink;previous=session->volume("default");}
public:
    explicit LinuxVolume(std::shared_ptr<PulseSession> session):session(std::move(session)){baseline();}
    Value get(const std::string& id)override{try{auto value=session->volume(id);value.scalar=std::clamp(value.scalar,0.f,1.f);return value;}catch(...){return {};}}
    bool set(const std::string& id,float value)override{try{bool result=session->volume(id,value);baseline();return result;}catch(...){return false;}}
    void links(std::set<std::string> ids)override{if(ids.size()>32)throw std::runtime_error("Too many volume targets");if(ids!=linked){linked=std::move(ids);proportions.clear();baseline();}active=!linked.empty();}
    void poll()override{
        try{
            auto state=session->snapshot();auto current=session->volume("default");
            if(!current.available||!previous.available||state.defaultSink!=defaultName||linked.empty()){previous=current;defaultName=state.defaultSink;active=false;return;}
            std::map<std::string,float> levels;float maximum=0;
            for(auto& id:linked){auto name=PulseSession::sinkName(state,id);if(name.empty())continue;auto value=session->volume(id);if(value.available){float before=name==defaultName?previous.scalar:value.scalar;levels[name]=before;maximum=std::max(maximum,before);}}
            active=!levels.empty();float delta=current.scalar-previous.scalar;
            if(active&&std::abs(delta)>0.0001f){
                if(maximum>0)for(auto& [name,value]:levels)proportions[name]=value/maximum;
                float next=std::clamp(maximum+delta,0.f,1.f);unsigned changed=0;
                for(auto& [name,value]:levels){auto ratio=proportions.find(name);if(session->volume("out:pulse:"+name,next*(ratio==proportions.end()?1.f:ratio->second)))++changed;}
                if(!levels.count(defaultName))session->volume("default",std::clamp(previous.scalar,0.f,1.f));
                if(changed){++sequence;keyLevel=next;keyTargets=changed;}
                current=session->volume("default");
            }
            previous=current;defaultName=state.defaultSink;
        }catch(...){active=false;}
    }
    std::string keyEvent()const override{return std::to_string(sequence)+" "+std::to_string(keyLevel)+" "+std::to_string(keyTargets);}
    bool keysActive()const override{return active;}
};
}
