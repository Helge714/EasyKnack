#pragma once
#include "HostServices.hpp"
#include "../midi/MidiQueue.hpp"
#include <chrono>
#include <array>
#include <algorithm>

namespace easyknack::platform {
/** Stable port slots shared by OS MIDI adapters. Only queues/atomics cross into DSP. */
class MidiPortBank : public HostMidiSource {
protected:
    struct Port {
        std::atomic<std::uint64_t> key{0},generation{0};
        std::atomic<bool> enabled{false},driver{false};
        MidiQueue queue,driverQueue;
        MidiBlock block;
    };
    std::array<Port,64> ports;
    Port keyboard;
    std::atomic<bool> running{false};
    std::vector<std::uint64_t> wanted,driverWanted;
    bool learning=false;
    static std::int64_t now()noexcept{return std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::steady_clock::now().time_since_epoch()).count();}
    static std::uint64_t identity(const std::string& text){std::uint64_t h=14695981039346656037ULL;for(unsigned char c:text){h^=c;h*=1099511628211ULL;}return h<=1?h+2:h;}
    static void canonical(std::vector<std::uint64_t>& keys){std::sort(keys.begin(),keys.end());keys.erase(std::unique(keys.begin(),keys.end()),keys.end());}
    bool wantsDriver(std::uint64_t key)const{return learning||std::find(driverWanted.begin(),driverWanted.end(),key)!=driverWanted.end();}
    Port* slot(std::uint64_t key){for(auto& p:ports)if(p.key.load()==key)return &p;for(auto& p:ports)if(!p.key.load()){p.key.store(key,std::memory_order_release);return &p;}return nullptr;}
    void deliver(Port& port,std::uint32_t message)noexcept{
        auto type=message&0xf0;
        if(port.driver.load(std::memory_order_acquire)&&(type==0x80||type==0x90||type==0xb0))port.driverQueue.push(now(),message);
        if(running.load(std::memory_order_acquire))port.queue.push(now(),message);
    }
public:
    MidiPortBank(){keyboard.key.store(1);keyboard.enabled.store(true);}
    void prepare(std::vector<std::uint64_t> keys)override{canonical(keys);if(keys!=wanted){wanted=std::move(keys);refresh();}}
    void prepareDrivers(std::vector<std::uint64_t> keys,bool learn)override{canonical(keys);if(keys!=driverWanted||learning!=learn){driverWanted=std::move(keys);learning=learn;keyboard.driver.store(wantsDriver(1));refresh();}}
    void setRunning(bool value)noexcept override{running.store(value,std::memory_order_release);if(!value)panic();}
    void panic()noexcept override{keyboard.queue.panic();for(auto& p:ports)p.queue.panic();}
    void keyboardEvent(std::uint32_t message)noexcept override{if(message==255){keyboard.queue.panic();keyboard.driverQueue.panic();return;}deliver(keyboard,message);}
    std::vector<std::pair<std::uint64_t,std::uint32_t>> driverEvents()override{
        std::vector<std::pair<std::uint64_t,std::uint32_t>> result;
        auto drain=[&](Port& p){MidiBlock b;p.driverQueue.drain(b,0,now()+1,1);if(b.panic)result.emplace_back(p.key.load(),255);for(std::size_t i=0;i<b.count;i++)result.emplace_back(p.key.load(),b.events[i].message);};
        drain(keyboard);for(auto& p:ports)if(p.key.load())drain(p);return result;
    }
    void beginBlock(std::size_t frames,double rate)noexcept override{
        auto end=now(),start=end-static_cast<std::int64_t>(frames*1e9/rate);
        auto drain=[&](Port& p){p.queue.drain(p.block,start,end,frames);p.block.source=p.key.load();p.block.generation=p.generation.load();if(!p.enabled.load(std::memory_order_acquire)||!running.load()){p.block.clear();p.block.panic=true;}};
        drain(keyboard);keyboard.block.generation=1;for(auto& p:ports)if(p.key.load(std::memory_order_acquire))drain(p);
    }
    const MidiBlock* events(std::uint64_t key)const noexcept override{if(key==1)return &keyboard.block;for(auto& p:ports)if(key&&p.key.load(std::memory_order_acquire)==key)return &p.block;return nullptr;}
};
}
