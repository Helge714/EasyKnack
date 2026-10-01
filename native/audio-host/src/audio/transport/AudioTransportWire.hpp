#pragma once
#include <cstddef>
#include <cstdint>
#include <type_traits>

namespace easyknack::transport {
// Experimental user-mode ABI. A kernel adapter must validate/map it separately.
// No pointers, size_t, STL containers or std::atomic objects in shared storage.
constexpr std::uint32_t abiVersion = 1, maxChannels = 8, maxFrames = 8192;
constexpr std::uint64_t magic = 0x454b415544494f31ULL;
enum class Side : unsigned { Device = 0, Engine = 1 };
enum class State : std::uint64_t { Empty = 0, Online = 1, Closed = 2, Expired = 3 };
struct Format { std::uint32_t sampleRate = 48000, channels = 2, capacityFrames = 1024; };
struct SessionId { std::uint64_t low, high; };
struct alignas(64) Header {
    std::uint64_t signature;
    std::uint32_t version, bytes;
    SessionId session;
    std::uint32_t sampleRate, channels, capacityFrames, sampleFormat; // 1 = interleaved IEEE float32 LE
    std::uint64_t ready;
    std::uint64_t reserved;
};
struct alignas(64) Peer {
    std::uint64_t claimed, state, heartbeatMilliseconds;
    std::uint64_t reserved[5];
};
struct alignas(64) Cursor {
    std::uint64_t position, xruns;
    std::uint64_t reserved[6];
};
struct alignas(64) Ring {
    Cursor producer, consumer; // Separate cache lines; each position has exactly one writer.
    float samples[maxFrames * maxChannels];
};
struct alignas(64) Region {
    Header header;
    Peer peers[2];
    Ring rings[2]; // 0 device->engine; 1 engine->device.
};
static_assert(sizeof(float) == 4 && sizeof(Header) == 64 && sizeof(Peer) == 64 && sizeof(Cursor) == 64);
static_assert(offsetof(Header, ready) == 48 && offsetof(Region, rings) == 192);
static_assert(sizeof(Ring) == 262272 && sizeof(Region) == 524736);
static_assert(std::is_standard_layout_v<Region> && std::is_trivially_copyable_v<Region>);
inline bool valid(Format format) noexcept {
    return format.sampleRate >= 8000 && format.sampleRate <= 384000 && format.channels >= 1 && format.channels <= maxChannels
        && format.capacityFrames >= 64 && format.capacityFrames <= maxFrames
        && (format.capacityFrames & (format.capacityFrames - 1)) == 0;
}
}
