#pragma once
#include "BridgeProtocol.hpp"

namespace easyknack::driver {
// Caller holds the kernel spin lock. No allocation, pointers from userspace,
// floating point, or unbounded cursor arithmetic in this storage.
struct PcmRing {
    static constexpr uint32_t Capacity = 2048;
    int16_t samples[Capacity * Channels]{};
    uint32_t head = 0;
    uint32_t count = 0;
    void reset() { head = count = 0; }
    uint32_t write(const int16_t* source, uint32_t frames) {
        const auto accepted = frames < Capacity - count ? frames : Capacity - count;
        for (uint32_t i = 0; i < accepted; ++i)
            for (uint32_t c = 0; c < Channels; ++c)
                samples[((head + count + i) % Capacity) * Channels + c] = source[i * Channels + c];
        count += accepted;
        return accepted;
    }
    uint32_t read(int16_t* destination, uint32_t frames) {
        const auto available = frames < count ? frames : count;
        for (uint32_t i = 0; i < frames; ++i)
            for (uint32_t c = 0; c < Channels; ++c)
                destination[i * Channels + c] = i < available ? samples[((head + i) % Capacity) * Channels + c] : 0;
        head = (head + available) % Capacity;
        count -= available;
        return available;
    }
};
}
