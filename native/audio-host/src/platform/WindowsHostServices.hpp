#pragma once
#include "HostServices.hpp"
#include "../audio/RoutingAudioBackend.hpp"
#include "../audio/WindowsDeviceVolume.hpp"
#include "../midi/WindowsMidiSource.hpp"
namespace easyknack::platform {
class WindowsAudio final : public HostAudioBackend {
    RoutingAudioBackend backend;
public:
    WindowsAudio(AudioEngine& engine,bool nullDevice):backend(engine,nullDevice){}
    std::uint64_t generation()const override{return backend.generation();}
    float takePeak(const std::string& id)noexcept override{return backend.takePeak(id);}
    std::string clockDescription()const override{return backend.clockDescription();}
    std::string bufferDescription()const override{return backend.bufferDescription();}
    AudioBackendCapabilities capabilities()const override{return backend.capabilities();}
    std::vector<AudioEndpoint> enumerateInputs()override{return backend.enumerateInputs();}
    std::vector<AudioEndpoint> enumerateOutputs()override{return backend.enumerateOutputs();}
    void prepare(const std::vector<AudioRoute>& routes)override{backend.prepare(routes);}
    void setRunning(bool value)override{backend.setRunning(value);}
    void refresh()override{backend.refresh();}
    std::string status()const override{return backend.status();}
    AudioIOStatistics statistics()const override{return backend.statistics();}
    void beginBlock(std::size_t n)noexcept override{backend.beginBlock(n);}
    float input(const std::string& id,std::size_t c,std::size_t f)const noexcept override{return backend.input(id,c,f);}
    void output(const std::string& id,std::size_t c,const float* data,std::size_t n)noexcept override{backend.output(id,c,data,n);}
    void endBlock(const float* gain,std::size_t n)noexcept override{backend.endBlock(gain,n);}
};
class WindowsMidi final : public HostMidiSource {
    WindowsMidiSource source;
public:
    explicit WindowsMidi(bool hardware):source(hardware){}
    std::vector<Device> enumerate()const override{std::vector<Device> result;for(auto& d:source.enumerate())result.push_back({d.key,d.name});return result;}
    void prepare(std::vector<std::uint64_t> d)override{source.prepare(std::move(d));}
    void prepareDrivers(std::vector<std::uint64_t> d,bool learn)override{source.prepareDrivers(std::move(d),learn);}
    std::vector<std::pair<std::uint64_t,std::uint32_t>> driverEvents()override{return source.driverEvents();}
    void refresh()override{source.refresh();}
    void setRunning(bool v)noexcept override{source.setRunning(v);}
    void panic()noexcept override{source.panic();}
    void keyboardEvent(std::uint32_t m)noexcept override{source.keyboardEvent(m);}
    std::string status()const override{return source.status();}
    void beginBlock(std::size_t n,double rate)noexcept override{source.beginBlock(n,rate);}
    const MidiBlock* events(std::uint64_t key)const noexcept override{return source.events(key);}
};
class WindowsVolume final : public DeviceVolumeService {
    WindowsDeviceVolume volume;
public:
    explicit WindowsVolume(bool hardware):volume(hardware){}
    Value get(const std::string& id)override{auto v=volume.get(id);return {v.available,v.scalar,v.muted};}
    bool set(const std::string& id,float v)override{return volume.set(id,v);}
    void links(std::set<std::string> ids)override{volume.links(std::move(ids));}
    void poll()override{volume.poll();}
    std::string keyEvent()const override{return volume.keyEvent();}
    bool keysActive()const override{return volume.keysActive();}
};
inline HostServices createHostServices(AudioEngine& engine,bool nullDevice){
    HostServices result;
    result.midi=std::make_unique<WindowsMidi>(!nullDevice);
    result.volume=std::make_unique<WindowsVolume>(!nullDevice);
    engine.midiSource=result.midi.get();
    result.audio=std::make_unique<WindowsAudio>(engine,nullDevice);
    return result;
}
}
