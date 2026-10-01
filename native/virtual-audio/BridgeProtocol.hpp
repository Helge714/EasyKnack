#pragma once
#if !defined(_KERNEL_MODE)
#include <stdint.h>
#endif

namespace easyknack::driver {
#if defined(_KERNEL_MODE)
using uint32_t = unsigned int;
using int16_t = short;
static_assert(sizeof(uint32_t) == 4 && sizeof(int16_t) == 2, "Unexpected kernel ABI");
#endif
constexpr uint32_t Version = 1;
constexpr uint32_t SampleRate = 48000;
constexpr uint32_t Channels = 2;
constexpr uint32_t PacketFrames = 256;
constexpr uint32_t FrameBytes = Channels * sizeof(int16_t);
constexpr uint32_t DeviceType = 0x8001;
// CTL_CODE(0x8001, 0x800, METHOD_BUFFERED, FILE_READ_DATA | FILE_WRITE_DATA).
constexpr uint32_t ExchangeIoctl = (DeviceType << 16) | (3u << 14) | (0x800u << 2);
constexpr uint32_t RenderRunning = 1;
constexpr uint32_t CaptureRunning = 2;
struct Packet {
    uint32_t version;
    uint32_t writeFrames; // Host -> virtual microphone; request.
    uint32_t readFrames;  // Virtual speaker -> host; request.
    uint32_t written;     // Reply: accepted frames.
    uint32_t read;        // Reply: available frames (remaining output is silent).
    uint32_t flags;       // Reply: stream state.
    uint32_t reserved[2];
    int16_t toCapture[PacketFrames * Channels];
    int16_t fromRender[PacketFrames * Channels];
};
static_assert(sizeof(Packet) == 2080, "Driver ABI changed");
inline bool validRequest(const Packet& packet) {
    return packet.version == Version && packet.writeFrames <= PacketFrames &&
        packet.readFrames <= PacketFrames && !packet.reserved[0] && !packet.reserved[1];
}
}
