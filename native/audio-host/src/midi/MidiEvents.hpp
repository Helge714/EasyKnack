#pragma once
#include <array>
#include <cstdint>
#include <cstddef>

namespace easyknack {
struct MidiEvent { std::uint32_t offset=0,message=0; float transpose=0; };
struct MidiBlock {
    static constexpr std::size_t capacity=256;
    std::array<MidiEvent,capacity> events{};
    std::size_t count=0;
    bool panic=false;
    std::uint64_t source=0,generation=0;
    void clear() noexcept {count=0;panic=false;}
    void append(std::uint32_t offset,std::uint32_t message) noexcept {
        if(count<capacity)events[count++]={offset,message};else{count=0;panic=true;}
    }
};
/** Hardware-independent event source, borrowed for the lifetime of the audio engine. */
struct MidiSource {
    virtual ~MidiSource()=default;
    virtual void beginBlock(std::size_t frames,double sampleRate) noexcept=0;
    virtual const MidiBlock* events(std::uint64_t device) const noexcept=0;
};
}
