#pragma once
#include "RoutedAudio.hpp"
#include <map>
namespace easyknack::platform {
/** Follow explicit desktop default-volume changes; no global keyboard interception. */
class RoutedVolume final : public DeviceVolumeService {
    std::shared_ptr<DeviceDriver> driver;
    std::set<std::string> linked;
    std::map<std::string,float> proportions;
    Value previous;
    std::string defaultKey;
    std::uint64_t sequence=0;
    float keyLevel=0;unsigned targets=0;bool active=false;
    void baseline(){defaultKey=driver->volumeKey("default");previous=driver->volume(defaultKey);}
public:
    explicit RoutedVolume(std::shared_ptr<DeviceDriver> driver):driver(std::move(driver)){baseline();}
    Value get(const std::string& id)override{try{return driver->volume(driver->volumeKey(id));}catch(...){return {};}}
    bool set(const std::string& id,float level)override{try{bool result=driver->volume(driver->volumeKey(id),std::clamp(level,0.f,1.f));baseline();return result;}catch(...){return false;}}
    void links(std::set<std::string> ids)override{if(ids.size()>32)throw std::runtime_error("Too many volume targets");if(ids!=linked){linked=std::move(ids);proportions.clear();baseline();}active=false;}
    void poll()override{
        try{auto key=driver->volumeKey("default");auto current=driver->volume(key);
            if(linked.empty()||key!=defaultKey||!previous.available||!current.available){previous=current;defaultKey=key;active=false;return;}
            std::map<std::string,float> levels;float maximum=0;
            for(auto& id:linked){auto canonical=driver->volumeKey(id);if(canonical.empty())continue;auto value=driver->volume(canonical);if(value.available){float before=canonical==key?previous.scalar:value.scalar;levels[canonical]=before;maximum=std::max(maximum,before);}}
            active=!levels.empty();float delta=current.scalar-previous.scalar;
            if(active&&std::abs(delta)>.0001f){
                if(maximum>0)for(auto& [id,value]:levels)proportions[id]=value/maximum;
                float next=std::clamp(maximum+delta,0.f,1.f);unsigned changed=0;
                for(auto& [id,value]:levels){auto ratio=proportions.find(id);if(driver->volume(id,next*(ratio==proportions.end()?1.f:ratio->second)))++changed;}
                if(!levels.count(key))driver->volume(key,previous.scalar);
                if(changed){++sequence;keyLevel=next;targets=changed;}current=driver->volume(key);
            }previous=current;
        }catch(...){active=false;}
    }
    std::string keyEvent()const override{return std::to_string(sequence)+" "+std::to_string(keyLevel)+" "+std::to_string(targets);}
    bool keysActive()const override{return active;}
};
}
