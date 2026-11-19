#pragma once
#include "HostServices.hpp"
#include "../midi/MidiQueue.hpp"
#include <chrono>
#include <stdexcept>
namespace easyknack::platform {
/** Portable MIDI keyboard; hardware discovery is deliberately not advertised. */
class KeyboardMidi final : public HostMidiSource {
    MidiQueue queue,driverQueue;
    MidiBlock block;
    std::atomic<bool> running{false},driver{false};
    static std::int64_t now()noexcept{return std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::steady_clock::now().time_since_epoch()).count();}
    static void check(const std::vector<std::uint64_t>& devices){for(auto id:devices)if(id>1)throw std::runtime_error("Hardware MIDI backend is not implemented on this platform");}
public:
    std::vector<Device> enumerate()const override{return {};}
    void prepare(std::vector<std::uint64_t> devices)override{check(devices);}
    void prepareDrivers(std::vector<std::uint64_t> devices,bool learning)override{check(devices);driver.store(learning||std::find(devices.begin(),devices.end(),1)!=devices.end());}
    std::vector<std::pair<std::uint64_t,std::uint32_t>> driverEvents()override{
        MidiBlock events;driverQueue.drain(events,0,now()+1,1);std::vector<std::pair<std::uint64_t,std::uint32_t>> result;
        if(events.panic)result.emplace_back(1,255);for(std::size_t i=0;i<events.count;i++)result.emplace_back(1,events.events[i].message);return result;
    }
    void refresh()override{}
    void setRunning(bool v)noexcept override{running.store(v);if(!v)panic();}
    void panic()noexcept override{queue.panic();}
    void keyboardEvent(std::uint32_t m)noexcept override{if(m==255){queue.panic();driverQueue.panic();return;}auto t=now();if(driver.load())driverQueue.push(t,m);if(running.load())queue.push(t,m);}
    std::string status()const override{return {};}
    void beginBlock(std::size_t n,double rate)noexcept override{
        auto end=now();queue.drain(block,end-static_cast<std::int64_t>(n*1e9/rate),end,n);block.source=1;block.generation=1;
        if(!running.load()){block.clear();block.panic=true;}
    }
    const MidiBlock* events(std::uint64_t key)const noexcept override{return key==1?&block:nullptr;}
};
class UnavailableVolume final : public DeviceVolumeService {
public:
    Value get(const std::string&)override{return {};}
    bool set(const std::string&,float)override{return false;}
    void links(std::set<std::string>)override{}
    void poll()override{}
    std::string keyEvent()const override{return "0 0 0";}
    bool keysActive()const override{return false;}
};
}
