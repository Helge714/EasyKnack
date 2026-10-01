#pragma once
#include <algorithm>
#include <array>
#include <atomic>
#include <cstddef>
#include <cstdint>
#include <memory>
#include <string>
#include <vector>

namespace easyknack {
enum class AudioEndpointType { SystemMix, Application, HardwareInput, HardwareOutput, VirtualInput, VirtualOutput, AsioInput, AsioOutput };
enum class AudioCaptureMode { Copy, Route };
struct AudioEndpoint {
    std::string id, name;
    AudioEndpointType type;
    bool available = true, canRoute = false;
};
struct AudioBackendCapabilities {
    bool systemCapture = false, applicationCapture = false, applicationRouting = false,
         virtualInputs = false, virtualOutputs = false;
};
struct AudioBufferView {
    float* const* channels;
    std::size_t channelCount, frameCount;
};
struct AudioRoute { std::string endpoint; bool input; AudioCaptureMode mode = AudioCaptureMode::Copy; };
struct AudioIOStatistics {
    std::uint64_t capturedFrames=0,playedFrames=0,underruns=0,overruns=0;
    std::uint64_t captureUnder=0,captureOver=0,playbackUnder=0,playbackOver=0,driverOverload=0,priming=0;
};

/** One producer, one consumer. Full buffers drop incoming frames; empty buffers read silence.
 * Ownership of cursors is never stolen by the other thread. Reset only while both are stopped. */
template<std::size_t Capacity = 8192> class StereoRing {
    std::array<std::array<float, 2>, Capacity> data{};
    std::atomic<std::uint64_t> readPosition{0}, writePosition{0};
    // Consumer-owned adaptive resampling state; reset only after both endpoints stop.
    double fraction = 0, ratio = 1;
    bool adaptivePrimed = false;
public:
    // 42.7 ms at 48 kHz: tolerate batched Windows timer/device callbacks.
    // DSP block size stays 256; this reserve is only for independent clocks.
    static constexpr std::size_t adaptiveTargetFrames = 2048;
    std::atomic<unsigned> underruns{0}, overruns{0};
    std::atomic<std::uint64_t> priming{0};
    static_assert(std::atomic<std::uint64_t>::is_always_lock_free);
    void reset() noexcept { readPosition.store(0); writePosition.store(0); fraction=0; ratio=1; adaptivePrimed=false; }
    std::size_t available() const noexcept {
        return static_cast<std::size_t>(writePosition.load(std::memory_order_acquire) - readPosition.load(std::memory_order_relaxed));
    }
    std::uint64_t writtenFrames() const noexcept { return writePosition.load(std::memory_order_relaxed); }
    std::uint64_t readFrames() const noexcept { return readPosition.load(std::memory_order_relaxed); }
    void write(const float* input, std::size_t frames) noexcept {
        auto w = writePosition.load(std::memory_order_relaxed);
        auto r = readPosition.load(std::memory_order_acquire);
        auto count = std::min<std::uint64_t>(frames, Capacity - (w - r));
        for (std::size_t i = 0; i < count; ++i) data[(w + i) % Capacity] = {input ? input[2*i] : 0, input ? input[2*i+1] : 0};
        writePosition.store(w + count, std::memory_order_release);
        if (count < frames) overruns.fetch_add(1, std::memory_order_relaxed);
    }
    void read(float* output, std::size_t frames) noexcept {
        auto r = readPosition.load(std::memory_order_relaxed);
        auto w = writePosition.load(std::memory_order_acquire);
        auto count = std::min<std::uint64_t>(frames, w - r);
        for (std::size_t i = 0; i < count; ++i) {
            const auto& frame = data[(r + i) % Capacity]; output[2*i] = frame[0]; output[2*i+1] = frame[1];
        }
        std::fill(output + 2*count, output + 2*frames, 0.f);
        readPosition.store(r + count, std::memory_order_release);
        if (count < frames) underruns.fetch_add(1, std::memory_order_relaxed);
    }
    /** Bridge independently clocked WASAPI devices. Bounded linear interpolation with
     * slow fill-level feedback; never used in a synchronous ASIO insert. */
    void readAdaptive(float* output, std::size_t frames, std::size_t target=adaptiveTargetFrames) noexcept {
        auto r=readPosition.load(std::memory_order_relaxed);
        auto w=writePosition.load(std::memory_order_acquire);
        auto available=w-r;
        target=std::min<std::size_t>(Capacity/2,std::max(target,frames*2));
        if (!adaptivePrimed) {
            if (available<target) { priming.fetch_add(1,std::memory_order_relaxed);std::fill_n(output,frames*2,0.f); return; }
            adaptivePrimed=true;
        }
        double desired=1+std::clamp((double(available)-double(target))/double(target)*0.002,-0.01,0.01);
        ratio+=(desired-ratio)*0.02;
        std::size_t f=0,consumed=0;
        for (;f<frames;++f) {
            auto position=static_cast<std::size_t>(fraction);
            if (position+1>=available) break;
            const auto& a=data[(r+position)%Capacity]; const auto& b=data[(r+position+1)%Capacity];
            float t=static_cast<float>(fraction-position);
            for (unsigned c=0;c<2;++c) output[2*f+c]=a[c]+(b[c]-a[c])*t;
            fraction+=ratio;
        }
        consumed=std::min<std::size_t>(static_cast<std::size_t>(fraction),available);
        fraction-=consumed;
        readPosition.store(r+consumed,std::memory_order_release);
        if (f<frames) {
            std::fill(output+2*f,output+2*frames,0.f);
            underruns.fetch_add(1,std::memory_order_relaxed); adaptivePrimed=false; fraction=0;
        }
    }
};

/** Called only from the DSP thread. Endpoint lookup and buffers must already be prepared. */
class AudioIO {
public:
    virtual ~AudioIO() = default;
    virtual void beginBlock(std::size_t frames) noexcept = 0;
    virtual float input(const std::string& id, std::size_t channel, std::size_t frame) const noexcept = 0;
    virtual void output(const std::string& id, std::size_t channel, const float* samples, std::size_t frames) noexcept = 0;
    virtual void endBlock(const float* gain, std::size_t frames) noexcept = 0;
};
/** Control-thread API; never called by nodes or callbacks. */
class AudioBackend : public AudioIO {
public:
    virtual AudioBackendCapabilities capabilities() const = 0;
    virtual std::vector<AudioEndpoint> enumerateInputs() = 0;
    virtual std::vector<AudioEndpoint> enumerateOutputs() = 0;
    virtual void prepare(const std::vector<AudioRoute>& routes) = 0;
    virtual void setRunning(bool running) = 0;
    virtual void refresh() = 0;
    virtual std::string status() const = 0;
    virtual AudioIOStatistics statistics() const = 0;
};
}
