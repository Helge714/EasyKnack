#pragma once
#include <windows.h>
#include <mmsystem.h>
#include <chrono>
#include <memory>
#include <set>
#include <map>
#include <string>
#include <vector>
#include "MidiQueue.hpp"

namespace easyknack {
class WindowsMidiSource : public MidiSource {
public:
    struct Device{std::uint64_t key;UINT index;std::string name;};
private:
    struct Slot {
        std::atomic<std::uint64_t> key{0},generation{0};
        std::atomic<bool> enabled{false};
        MidiQueue queue,driverQueue;
        std::atomic<bool> driverEnabled{false};
        MidiBlock block;
        HMIDIIN handle=nullptr; // control thread only
        WindowsMidiSource* owner=nullptr;
    };
    std::array<Slot,64> slots;
    Slot keyboard;
    std::atomic<bool> running{false};
    bool hardware;
    std::vector<std::uint64_t> wanted,driverWanted;
    bool learningDrivers=false;
    std::string problem;
    static std::int64_t now() noexcept {return std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::steady_clock::now().time_since_epoch()).count();}
    static std::string utf8(const wchar_t* text){
        int size=WideCharToMultiByte(CP_UTF8,0,text,-1,nullptr,0,nullptr,nullptr);if(size<=1)return "MIDI Input";
        std::string result(size,'\0');WideCharToMultiByte(CP_UTF8,0,text,-1,result.data(),size,nullptr,nullptr);result.pop_back();return result;
    }
    static std::uint64_t hash(const std::string& text){std::uint64_t h=14695981039346656037ULL;for(unsigned char c:text){h^=c;h*=1099511628211ULL;}return h?h:1;}
    // No WinMM calls or allocations in the driver callback (Microsoft MidiInProc contract).
    static void CALLBACK callback(HMIDIIN,UINT message,DWORD_PTR instance,DWORD_PTR data,DWORD_PTR) noexcept {
        auto& slot=*reinterpret_cast<Slot*>(instance);
        if(message==MIM_ERROR||message==MIM_MOREDATA||message==MIM_CLOSE){if(message==MIM_CLOSE)slot.enabled.store(false,std::memory_order_release);slot.queue.panic();return;}
        if(message==MIM_DATA&&slot.enabled.load(std::memory_order_acquire)){
            auto value=static_cast<std::uint32_t>(data);auto type=value&0xf0;
            if(slot.driverEnabled.load(std::memory_order_acquire)&&(type==0x80||type==0x90||type==0xb0))slot.driverQueue.push(now(),value);
            if(slot.owner->running.load(std::memory_order_acquire))slot.queue.push(now(),value);
        }
    }
    void close(Slot& slot){
        slot.enabled.store(false,std::memory_order_release);slot.queue.panic();
        if(slot.handle){midiInStop(slot.handle);midiInReset(slot.handle);midiInClose(slot.handle);slot.handle=nullptr;}
    }
public:
    explicit WindowsMidiSource(bool useHardware=true):hardware(useHardware){for(auto& s:slots)s.owner=this;keyboard.key.store(1);keyboard.enabled.store(true);}
    ~WindowsMidiSource(){for(auto& s:slots)close(s);}
    std::vector<Device> enumerate() const {
        std::vector<Device> devices;if(!hardware)return devices;
        std::map<std::string,unsigned> occurrences;
        for(UINT i=0;i<midiInGetNumDevs();i++){
            MIDIINCAPSW caps{};if(midiInGetDevCapsW(i,&caps,sizeof caps)!=MMSYSERR_NOERROR)continue;
            auto name=utf8(caps.szPname);auto identity=name+":"+std::to_string(caps.wMid)+":"+std::to_string(caps.wPid);
            auto ordinal=occurrences[identity]++;auto key=hash(identity+":"+std::to_string(ordinal));if(key==1)key=2;
            devices.push_back({key,i,name+(ordinal?" ("+std::to_string(ordinal+1)+")":"")});
        }
        return devices;
    }
    void prepare(std::vector<std::uint64_t> devices){
        std::sort(devices.begin(),devices.end());devices.erase(std::unique(devices.begin(),devices.end()),devices.end());
        if(devices==wanted)return;wanted=std::move(devices);refresh();
    }
    void refresh(){
        auto devices=enumerate();std::set<std::uint64_t> requested(wanted.begin(),wanted.end());problem.clear();
        requested.insert(driverWanted.begin(),driverWanted.end());if(learningDrivers)for(auto& device:devices)requested.insert(device.key);
        for(auto& slot:slots)slot.driverEnabled.store(learningDrivers||std::find(driverWanted.begin(),driverWanted.end(),slot.key.load())!=driverWanted.end(),std::memory_order_release);
        for(auto& s:slots){auto key=s.key.load();if(s.handle&&(!s.enabled.load()||!requested.count(key)||std::none_of(devices.begin(),devices.end(),[&](const Device& d){return d.key==key;})))close(s);}
        for(auto key:requested){
            if(key<=1)continue;
            auto found=std::find_if(devices.begin(),devices.end(),[&](const Device& d){return d.key==key;});
            Slot* slot=nullptr;
            for(auto& s:slots)if(s.key.load()==key){slot=&s;break;}
            if(found==devices.end()){problem="MIDI input unavailable";continue;}
            if(!slot)for(auto& s:slots)if(s.key.load()==0){slot=&s;s.key.store(key,std::memory_order_release);break;}
            if(!slot){problem="MIDI session device limit reached";continue;}
            if(slot->handle)continue;
            slot->generation.fetch_add(1);slot->queue.panic();
            auto result=midiInOpen(&slot->handle,found->index,reinterpret_cast<DWORD_PTR>(&callback),reinterpret_cast<DWORD_PTR>(slot),CALLBACK_FUNCTION|MIDI_IO_STATUS);
            if(result!=MMSYSERR_NOERROR){slot->handle=nullptr;problem="MIDI input busy or unavailable: "+found->name;continue;}
            slot->driverEnabled.store(learningDrivers||std::find(driverWanted.begin(),driverWanted.end(),key)!=driverWanted.end(),std::memory_order_release);
            slot->enabled.store(true,std::memory_order_release);
            if(midiInStart(slot->handle)!=MMSYSERR_NOERROR){close(*slot);problem="MIDI input could not start: "+found->name;}
        }
    }
    void prepareDrivers(std::vector<std::uint64_t> devices,bool learning){
        std::sort(devices.begin(),devices.end());devices.erase(std::unique(devices.begin(),devices.end()),devices.end());
        if(driverWanted==devices&&learningDrivers==learning)return;driverWanted=std::move(devices);learningDrivers=learning;
        keyboard.driverEnabled.store(learning||std::find(driverWanted.begin(),driverWanted.end(),1)!=driverWanted.end());refresh();
    }
    std::vector<std::pair<std::uint64_t,std::uint32_t>> driverEvents(){
        std::vector<std::pair<std::uint64_t,std::uint32_t>> result;auto end=now()+1;
        auto drain=[&](Slot& slot){MidiBlock block;slot.driverQueue.drain(block,0,end,1);
            if(block.panic)result.emplace_back(slot.key.load(),255);
            for(std::size_t i=0;i<block.count;i++)result.emplace_back(slot.key.load(),block.events[i].message);};
        drain(keyboard);for(auto& slot:slots)if(slot.key.load())drain(slot);return result;
    }
    void setRunning(bool value) noexcept {running.store(value,std::memory_order_release);if(!value)panic();}
    void panic() noexcept {keyboard.queue.panic();for(auto& s:slots)s.queue.panic();}
    void keyboardEvent(std::uint32_t message) noexcept {
        if(message==255){keyboard.queue.panic();keyboard.driverQueue.panic();return;}
        if(keyboard.driverEnabled.load())keyboard.driverQueue.push(now(),message);
        if(running.load(std::memory_order_acquire))keyboard.queue.push(now(),message);
    }
    const std::string& status() const {return problem;}
    void beginBlock(std::size_t frames,double rate) noexcept override {
        auto end=now(),start=end-static_cast<std::int64_t>(frames*1e9/rate);
        keyboard.queue.drain(keyboard.block,start,end,frames);keyboard.block.source=1;keyboard.block.generation=1;
        if(!running.load(std::memory_order_acquire)){keyboard.block.clear();keyboard.block.panic=true;}
        for(auto& s:slots){auto key=s.key.load(std::memory_order_acquire);if(!key)continue;
            s.queue.drain(s.block,start,end,frames);s.block.source=key;s.block.generation=s.generation.load();
            if(!s.enabled.load(std::memory_order_acquire)||!running.load(std::memory_order_acquire)){s.block.clear();s.block.panic=true;}
        }
    }
    const MidiBlock* events(std::uint64_t key) const noexcept override{
        if(!key)return nullptr;if(key==1)return &keyboard.block;for(const auto& s:slots)if(s.key.load(std::memory_order_acquire)==key)return &s.block;return nullptr;
    }
};
}
