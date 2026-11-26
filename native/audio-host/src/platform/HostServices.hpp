#pragma once
#include "../audio/AudioBackend.hpp"
#include "../midi/MidiEvents.hpp"
#include <memory>
#include <set>
#include <utility>
namespace easyknack {
class AudioEngine;
/** Control-side platform services. DSP only borrows AudioIO and MidiSource. */
class HostAudioBackend : public AudioBackend {
public:
    virtual std::uint64_t generation() const=0;
    virtual float takePeak(const std::string&) noexcept=0;
    virtual std::string clockDescription() const=0;
    virtual std::string bufferDescription() const=0;
    virtual void beginPeakRead() noexcept {}
    virtual void endPeakRead() noexcept {}
    virtual void poll() {}
};
class HostMidiSource : public MidiSource {
public:
    struct Device {std::uint64_t key;std::string name;};
    virtual std::vector<Device> enumerate() const=0;
    virtual void prepare(std::vector<std::uint64_t>)=0;
    virtual void prepareDrivers(std::vector<std::uint64_t>,bool)=0;
    virtual std::vector<std::pair<std::uint64_t,std::uint32_t>> driverEvents()=0;
    virtual void refresh()=0;
    virtual void setRunning(bool) noexcept=0;
    virtual void panic() noexcept=0;
    virtual void keyboardEvent(std::uint32_t) noexcept=0;
    virtual std::string status() const=0;
    virtual void poll() {}
};
class DeviceVolumeService {
public:
    struct Value {bool available=false;float scalar=0;bool muted=false;};
    virtual ~DeviceVolumeService()=default;
    virtual Value get(const std::string&)=0;
    virtual bool set(const std::string&,float)=0;
    virtual void links(std::set<std::string>)=0;
    virtual void poll()=0;
    virtual std::string keyEvent() const=0;
    virtual bool keysActive() const=0;
};
struct HostServices {
    std::unique_ptr<HostMidiSource> midi;
    std::unique_ptr<DeviceVolumeService> volume;
    // Destroy first: device callbacks must stop before MIDI/engine state is freed.
    std::unique_ptr<HostAudioBackend> audio;
};
}
