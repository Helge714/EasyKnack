#pragma once
#include <array>
#include <cstdint>
#include <cstddef>
#include <algorithm>

namespace easyknack {
enum class ValueEventKind : std::uint8_t { value, trigger };
struct ValueEvent { std::uint32_t sampleOffset; float value; ValueEventKind kind=ValueEventKind::value; };
/** One writer, borrowed per-port block view. Numeric updates retain insertion order;
 * triggers coalesce per timestamp within this stream (voices have separate streams).
 * Overflow drops newest events, retains a diagnostic counter and never allocates. */
struct ValueEventBuffer {
    static constexpr std::size_t capacity=512;
    std::array<ValueEvent,capacity> events{};
    std::uint32_t count=0, frames=0;
    std::uint64_t dropped=0;
    float initial=0;
    void clear(std::size_t blockFrames,float previous=0)noexcept {count=0;frames=static_cast<std::uint32_t>(blockFrames);initial=previous;}
    bool push(std::uint32_t offset,float value,ValueEventKind kind=ValueEventKind::value)noexcept {
        if(offset>=frames||(count&&offset<events[count-1].sampleOffset)){++dropped;return false;}
        if(kind==ValueEventKind::trigger)
            for(auto i=count;i>0&&events[i-1].sampleOffset==offset;--i)
                if(events[i-1].kind==ValueEventKind::trigger)return true;
        if(count==capacity){++dropped;return false;}
        events[count++]={offset,value,kind};return true;
    }
    const ValueEvent* begin()const noexcept{return events.data();}
    const ValueEvent* end()const noexcept{return events.data()+count;}
    float valueAt(std::uint32_t offset)const noexcept {
        float held=initial;
        for(const auto& e:*this){if(e.sampleOffset>offset)break;if(e.kind==ValueEventKind::value)held=e.value;}
        return held;
    }
    void expand(float* output,std::size_t size)const noexcept {
        float held=initial;std::size_t cursor=0;
        for(std::size_t i=0;i<size;i++) {
            float pulse=0;bool triggered=false;
            while(cursor<count&&events[cursor].sampleOffset==i){const auto& e=events[cursor++];
                if(e.kind==ValueEventKind::trigger){pulse=e.value;triggered=true;}else held=e.value;}
            output[i]=triggered?pulse:held;
        }
    }
};
}
