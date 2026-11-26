#include "../src/platform/MidiPortBank.hpp"
#include <thread>
#include <iostream>
#include <stdexcept>
using namespace easyknack::platform;
struct FakeMidi final : MidiPortBank {
    std::vector<Device> enumerate()const override{return {{42,"Test MIDI"}};}
    void refresh()override{auto* p=slot(42);p->generation.store(7);p->driver.store(wantsDriver(42));p->enabled.store(std::find(wanted.begin(),wanted.end(),42)!=wanted.end()||wantsDriver(42));}
    std::string status()const override{return {};}
    void event(std::uint32_t message){deliver(*slot(42),message);}
};
static void require(bool value){if(!value)throw std::runtime_error("MIDI port bank assertion failed");}
int main(){
    auto storage=std::make_unique<FakeMidi>();auto& midi=*storage;
    midi.prepare({42,42,1});midi.prepareDrivers({42},false);midi.setRunning(true);
    midi.event(0x643c90);midi.keyboardEvent(0x403d90);
    std::this_thread::sleep_for(std::chrono::milliseconds(1));midi.beginBlock(256,48000);
    require(midi.events(42)->count==1&&midi.events(42)->events[0].message==0x643c90&&midi.events(42)->generation==7);
    require(midi.events(1)->count==1&&midi.events(1)->events[0].message==0x403d90);
    auto driver=midi.driverEvents();require(driver.size()==1&&driver[0].first==42&&driver[0].second==0x643c90);
    midi.event(0x4020e0);require(midi.driverEvents().empty());
    midi.setRunning(false);midi.beginBlock(256,48000);require(midi.events(42)->panic&&midi.events(42)->count==0);
    midi.prepareDrivers({},true);midi.event(0x643c80);midi.keyboardEvent(0x403d80);require(midi.driverEvents().size()==2);
    midi.keyboardEvent(255);require(midi.driverEvents().at(0).second==255);
    require(midi.events(999)==nullptr);std::cout<<"MIDI port bank tests passed\n";
}
