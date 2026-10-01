#pragma once
#include "AudioTransportWire.hpp"
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#include <sddl.h>
#include <algorithm>
#include <cmath>
#include <cstring>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>

namespace easyknack::transport {
inline std::uint64_t load(std::uint64_t& value) noexcept {
    return static_cast<std::uint64_t>(InterlockedCompareExchange64(reinterpret_cast<volatile LONG64*>(&value), 0, 0));
}
inline void store(std::uint64_t& value, std::uint64_t next) noexcept {
    InterlockedExchange64(reinterpret_cast<volatile LONG64*>(&value), static_cast<LONG64>(next));
}
inline bool claim(std::uint64_t& value, std::uint64_t expected, std::uint64_t next) noexcept {
    return static_cast<std::uint64_t>(InterlockedCompareExchange64(reinterpret_cast<volatile LONG64*>(&value),
            static_cast<LONG64>(next), static_cast<LONG64>(expected))) == expected;
}
inline void increment(std::uint64_t& value) noexcept {
    InterlockedIncrement64(reinterpret_cast<volatile LONG64*>(&value));
}
inline std::uint64_t uptime() noexcept { return GetTickCount64(); }

/** Mapping handles/views are acquired and released on the control thread. Peers retain
 * independent references: terminating either process cannot unmap the other's view. */
class WindowsSharedAudio {
    HANDLE handle = nullptr;
    Region* view = nullptr;
    std::wstring objectName;
    SessionId identity{};
    WindowsSharedAudio() = default;
    void map() {
        view = static_cast<Region*>(MapViewOfFile(handle, FILE_MAP_READ | FILE_MAP_WRITE, 0, 0, sizeof(Region)));
        if (!view) throw std::runtime_error("Cannot map audio transport");
    }
public:
    ~WindowsSharedAudio() { if (view) UnmapViewOfFile(view); if (handle) CloseHandle(handle); }
    WindowsSharedAudio(const WindowsSharedAudio&) = delete;
    WindowsSharedAudio& operator=(const WindowsSharedAudio&) = delete;
    static std::unique_ptr<WindowsSharedAudio> create(Format format) {
        if (!valid(format)) throw std::invalid_argument("Invalid transport format");
        auto result = std::unique_ptr<WindowsSharedAudio>(new WindowsSharedAudio());
        GUID guid{}; if (FAILED(CoCreateGuid(&guid))) throw std::runtime_error("Cannot create transport identity");
        std::memcpy(&result->identity, &guid, sizeof(guid));
        result->objectName = L"Local\\EasyKnack.Audio." + std::to_wstring(result->identity.low) + L"." + std::to_wstring(result->identity.high);
        // Only this Windows account and SYSTEM can attach. Never use a world-writable named section.
        HANDLE token = nullptr;
        if (!OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &token)) throw std::runtime_error("Cannot read transport owner");
        DWORD bytes = 0; GetTokenInformation(token, TokenUser, nullptr, 0, &bytes);
        std::vector<unsigned char> user(bytes);
        bool queried = GetTokenInformation(token, TokenUser, user.data(), bytes, &bytes) != FALSE;
        CloseHandle(token);
        if (!queried) throw std::runtime_error("Cannot read transport owner SID");
        LPWSTR sid = nullptr;
        if (!ConvertSidToStringSidW(reinterpret_cast<TOKEN_USER*>(user.data())->User.Sid, &sid)) throw std::runtime_error("Invalid transport owner SID");
        std::wstring acl = L"D:P(A;;GA;;;SY)(A;;GA;;;" + std::wstring(sid) + L")"; LocalFree(sid);
        PSECURITY_DESCRIPTOR descriptor = nullptr;
        if (!ConvertStringSecurityDescriptorToSecurityDescriptorW(acl.c_str(), SDDL_REVISION_1, &descriptor, nullptr)) throw std::runtime_error("Invalid transport ACL");
        SECURITY_ATTRIBUTES attributes{sizeof(attributes), descriptor, FALSE};
        result->handle = CreateFileMappingW(INVALID_HANDLE_VALUE, &attributes, PAGE_READWRITE, 0, sizeof(Region), result->objectName.c_str());
        DWORD error = GetLastError(); LocalFree(descriptor);
        if (!result->handle || error == ERROR_ALREADY_EXISTS) throw std::runtime_error("Cannot create unique audio transport");
        result->map();
        std::memset(result->view, 0, sizeof(Region)); // Touch pages on the control thread, before callbacks start.
        auto& header = result->view->header;
        header.signature = magic; header.version = abiVersion; header.bytes = sizeof(Region);
        header.session = result->identity; header.sampleRate = format.sampleRate;
        header.channels = format.channels; header.capacityFrames = format.capacityFrames; header.sampleFormat = 1;
        store(header.ready, 1);
        return result;
    }
    static std::unique_ptr<WindowsSharedAudio> open(const std::wstring& name, SessionId expected) {
        auto result = std::unique_ptr<WindowsSharedAudio>(new WindowsSharedAudio());
        result->objectName = name; result->identity = expected;
        result->handle = OpenFileMappingW(FILE_MAP_READ | FILE_MAP_WRITE, FALSE, name.c_str());
        if (!result->handle) throw std::runtime_error("Audio transport unavailable");
        result->map(); result->format(); return result;
    }
    Format format() const {
        if (load(view->header.ready) != 1) throw std::runtime_error("Audio transport not ready");
        const auto& header = view->header;
        Format format{header.sampleRate, header.channels, header.capacityFrames};
        if (header.signature != magic || header.version != abiVersion
                || header.bytes != sizeof(Region) || header.sampleFormat != 1 || !valid(format)
                || header.session.low != identity.low || header.session.high != identity.high)
            throw std::runtime_error("Incompatible or stale audio transport");
        return format;
    }
    Region& region() noexcept { return *view; }
    const std::wstring& name() const noexcept { return objectName; }
    SessionId session() const noexcept { return identity; }
};

enum class TransferStatus { Ok, Underrun, Overrun, PeerUnavailable, InvalidCursor };
struct Transfer { std::size_t frames; TransferStatus status; };

/** One client per side; read and write belong to one audio thread. Shared storage is
 * accessed only through Windows Interlocked operations (full fences), not volatile or
 * implementation-dependent cross-process std::atomic. Destroy client before its mapping.
 * A disconnected/expired session is terminal: reconnect with a fresh mapping/session ID. */
class SharedAudioClient {
    Region& region;
    Format cachedFormat;
    unsigned side;
    std::uint64_t (*clock)();
    bool attached = false;
    static constexpr std::uint64_t leaseMilliseconds = 2000;
    bool peerAvailable() noexcept {
        auto& own = region.peers[side]; auto& peer = region.peers[1-side];
        if (!attached || load(own.state) != static_cast<std::uint64_t>(State::Online)) return false;
        auto now = clock(); store(own.heartbeatMilliseconds, now);
        if (load(peer.state) != static_cast<std::uint64_t>(State::Online)) return false;
        auto heartbeat = load(peer.heartbeatMilliseconds);
        if (now >= heartbeat && now - heartbeat > leaseMilliseconds) {
            claim(peer.state, static_cast<std::uint64_t>(State::Online), static_cast<std::uint64_t>(State::Expired));
            return false;
        }
        return true;
    }
public:
    SharedAudioClient(WindowsSharedAudio& mapping, Side role, std::uint64_t (*time)() = uptime)
        : region(mapping.region()), cachedFormat(mapping.format()), side(static_cast<unsigned>(role)), clock(time) {
        if (side > 1 || !time) throw std::invalid_argument("Invalid transport role/clock");
        auto& own = region.peers[side];
        if (!claim(own.claimed, 0, 1)) throw std::runtime_error("Audio transport role already claimed; create a new session");
        store(own.heartbeatMilliseconds, clock()); store(own.state, static_cast<std::uint64_t>(State::Online)); attached = true;
    }
    ~SharedAudioClient() { close(); }
    SharedAudioClient(const SharedAudioClient&) = delete;
    SharedAudioClient& operator=(const SharedAudioClient&) = delete;
    void close() noexcept { if (attached) store(region.peers[side].state, static_cast<std::uint64_t>(State::Closed)); attached = false; }
    Format format() const noexcept { return cachedFormat; }
    bool connected() noexcept { return peerAvailable(); }
    Transfer write(const float* samples, std::size_t frames) noexcept {
        if (!peerAvailable()) return {0, TransferStatus::PeerUnavailable};
        auto& ring = region.rings[side];
        auto w = load(ring.producer.position), r = load(ring.consumer.position);
        auto capacity = cachedFormat.capacityFrames, channels = cachedFormat.channels;
        if (w-r > capacity) return {0, TransferStatus::InvalidCursor};
        auto count = std::min<std::uint64_t>(frames, capacity-(w-r));
        for (std::size_t i=0; i<count; ++i) for (unsigned c=0; c<channels; ++c) {
            float sample = samples ? samples[i*channels+c] : 0;
            ring.samples[((w+i)&(capacity-1))*channels+c] = std::isfinite(sample) ? sample : 0;
        }
        store(ring.producer.position, w+count);
        if (count < frames) increment(ring.producer.xruns);
        return {static_cast<std::size_t>(count), count == frames ? TransferStatus::Ok : TransferStatus::Overrun};
    }
    Transfer read(float* samples, std::size_t frames) noexcept {
        auto channels = cachedFormat.channels, capacity = cachedFormat.capacityFrames;
        std::fill_n(samples, frames*channels, 0.f);
        if (!peerAvailable()) return {0, TransferStatus::PeerUnavailable};
        auto& ring = region.rings[1-side];
        auto r = load(ring.consumer.position), w = load(ring.producer.position);
        if (w-r > capacity) return {0, TransferStatus::InvalidCursor};
        auto count = std::min<std::uint64_t>(frames, w-r);
        for (std::size_t i=0; i<count; ++i) for (unsigned c=0; c<channels; ++c) {
            auto sample = ring.samples[((r+i)&(capacity-1))*channels+c];
            samples[i*channels+c] = std::isfinite(sample) ? sample : 0;
        }
        store(ring.consumer.position, r+count);
        if (count < frames) increment(ring.consumer.xruns);
        return {static_cast<std::size_t>(count), count == frames ? TransferStatus::Ok : TransferStatus::Underrun};
    }
};
}
