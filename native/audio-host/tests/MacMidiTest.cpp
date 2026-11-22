#include "../src/platform/MacHost.hpp"
#include <CoreMIDI/CoreMIDI.h>
#include <chrono>
#include <thread>
#include <iostream>
#include <unistd.h>
using namespace easyknack;
int main(){
    struct Session {MIDIClientRef client=0;MIDIEndpointRef source=0;~Session(){if(source)MIDIEndpointDispose(source);if(client)MIDIClientDispose(client);}} session;
    if(MIDIClientCreate(CFSTR("EasyKnack test producer"),nullptr,nullptr,&session.client)!=noErr){std::cout<<"SKIP: CoreMIDI server unavailable\n";return 77;}
    auto name=std::string("EasyKnack acceptance ")+std::to_string(getpid());
    auto label=CFStringCreateWithCString(nullptr,name.c_str(),kCFStringEncodingUTF8);
    auto status=MIDISourceCreate(session.client,label,&session.source);CFRelease(label);
    if(status!=noErr){std::cout<<"SKIP: CoreMIDI virtual source unavailable\n";return 77;}
    auto midi=platform::createMacMidiSource(true);std::uint64_t key=0;
    for(unsigned retry=0;retry<100&&!key;retry++){midi->poll();for(auto& device:midi->enumerate())if(device.name.find(name)!=std::string::npos)key=device.key;if(!key)std::this_thread::sleep_for(std::chrono::milliseconds(10));}
    if(!key)throw std::runtime_error("CoreMIDI virtual source was not discovered");
    midi->prepare({key});midi->prepareDrivers({key},false);midi->setRunning(true);midi->beginBlock(256,48000);
    alignas(MIDIPacketList) unsigned char bytes[1024]{};auto* packets=reinterpret_cast<MIDIPacketList*>(bytes);
    auto* packet=MIDIPacketListInit(packets);const Byte note[]{0x90,60,100};
    if(!MIDIPacketListAdd(packets,sizeof bytes,packet,0,sizeof note,note)||MIDIReceived(session.source,packets)!=noErr)throw std::runtime_error("CoreMIDI test send failed");
    bool found=false;for(unsigned retry=0;retry<100&&!found;retry++){std::this_thread::sleep_for(std::chrono::milliseconds(5));midi->beginBlock(256,48000);if(auto* block=midi->events(key))for(std::size_t i=0;i<block->count;i++)found|=block->events[i].message==0x643c90;}
    if(!found)throw std::runtime_error("CoreMIDI note did not reach the host MIDI queue");
    auto driver=midi->driverEvents();if(std::none_of(driver.begin(),driver.end(),[&](auto& event){return event.first==key&&event.second==0x643c90;}))throw std::runtime_error("CoreMIDI Learn event missing");
    midi->setRunning(false);midi->beginBlock(256,48000);if(!midi->events(key)->panic)throw std::runtime_error("CoreMIDI stop panic missing");
    std::cout<<"CoreMIDI virtual source, note queue, Learn and stop panic passed\n";
}
