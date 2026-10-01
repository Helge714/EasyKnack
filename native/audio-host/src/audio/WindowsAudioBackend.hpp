#pragma once
// Only this backend knows WASAPI, Windows process IDs and miniaudio devices.
#include "AudioBackend.hpp"
#include "ProtocolText.hpp"
#include "WindowsProcessCapture.hpp"
#include "../AudioGraph.hpp"
#include "miniaudio.h"
#include <audiopolicy.h>
#include <mmdeviceapi.h>
#include <tlhelp32.h>
#include <map>
#include <set>
#include <chrono>

namespace easyknack {
template<class T> struct ComPtr {
    T* ptr = nullptr;
    ~ComPtr() { if (ptr) ptr->Release(); }
    T** address() { return &ptr; }
    T* operator->() const { return ptr; }
};
inline std::string utf8(const wchar_t* text) {
    if (!text || !*text) return {};
    int size = WideCharToMultiByte(CP_UTF8, 0, text, -1, nullptr, 0, nullptr, nullptr);
    std::string result(size, 0);
    WideCharToMultiByte(CP_UTF8, 0, text, -1, result.data(), size, nullptr, nullptr);
    result.pop_back(); return result;
}
class WindowsAudioBackend final : public AudioBackend {
#ifdef EASYKNACK_AUDIO_TEST
    friend struct WindowsAudioBackendTest;
#endif
    struct DeviceInfo { AudioEndpoint endpoint; ma_device_id id{}; DWORD process = 0; std::vector<DWORD> processes; };
    struct Stream {
        WindowsAudioBackend* owner = nullptr;
        std::string id, name;
        bool input = false, initialized = false, started = false, drivesDsp = false;
        ma_result openResult = MA_SUCCESS;
        ma_device device{};
        StereoRing<> ring;
        std::array<float, blockSize*2> block{};
        std::atomic<bool> failed{false};
        std::atomic<std::uint64_t> directFrames{0};
        std::atomic<unsigned> maxCapturePacket{0};
        std::atomic<float> peak{0};
        static_assert(std::atomic<float>::is_always_lock_free);
        std::unique_ptr<WindowsProcessCapture> processCapture;
        HRESULT processResult = S_OK;
        ~Stream() { if (initialized && !processCapture) ma_device_uninit(&device); }
        bool start() { started = processCapture ? processCapture->start() : ma_device_start(&device) == MA_SUCCESS; return started; }
        void stop() {
            if (!started) return;
            if (processCapture) processCapture->stop(); else ma_device_stop(&device);
            started=false;
        }
    };
    AudioEngine& engine;
    ma_context context{}, clockContext{};
    ma_device clock{};
    bool contextReady = false, clockContextReady = false, clockReady = false, comReady = false;
    bool nullBackend = false, running = false, processCapture = false;
    bool clockEnabled = true;
    Stream* master = nullptr; // Changed only while every callback is stopped.
    float* directOutput = nullptr; // DSP thread only, valid for one master callback.
    std::size_t directOffset = 0;
    static constexpr std::size_t inputReserve = 256;
    static std::size_t inputTarget(const Stream& stream) noexcept {
        // Two actual capture packets bridge differently sized device periods.
        // Process loopback has its own packet delivery and retains its larger reserve.
        return stream.processCapture ? StereoRing<>::adaptiveTargetFrames
            : std::min<std::size_t>(4096, std::max<std::size_t>(inputReserve,
                    stream.maxCapturePacket.load(std::memory_order_relaxed)*2));
    }
    std::vector<DeviceInfo> inputs, outputs;
    std::vector<std::unique_ptr<Stream>> streams;
    std::vector<AudioRoute> selectedRoutes;
    std::string preparedKey;
    static void deviceCallback(ma_device* device, void* output, const void* input, ma_uint32 frames) {
        auto& stream = *static_cast<Stream*>(device->pUserData);
        if (stream.input) {
            stream.maxCapturePacket.store(std::max(stream.maxCapturePacket.load(std::memory_order_relaxed), frames), std::memory_order_relaxed);
            stream.ring.write(static_cast<const float*>(input), frames);
        }
        else if (stream.drivesDsp) {
            auto& self = *stream.owner;
            self.directOutput = static_cast<float*>(output);
            self.directOffset = 0;
            self.engine.render(self.directOutput, frames, &self, true);
            self.directOutput = nullptr;
            stream.directFrames.fetch_add(frames, std::memory_order_relaxed);
        }
        else stream.ring.readAdaptive(static_cast<float*>(output), frames);
    }
    static void notification(const ma_device_notification* event) {
        if (event->type == ma_device_notification_type_stopped)
            static_cast<Stream*>(event->pDevice->pUserData)->failed.store(true, std::memory_order_relaxed);
    }
    static void clockCallback(ma_device* device, void* output, const void*, ma_uint32 frames) {
        auto& self = *static_cast<WindowsAudioBackend*>(device->pUserData);
        self.engine.render(static_cast<float*>(output), frames, &self, true);
    }
    static bool supportsProcessCapture() {
        using VersionFunction = LONG (WINAPI*)(OSVERSIONINFOW*);
        auto function = reinterpret_cast<VersionFunction>(GetProcAddress(GetModuleHandleW(L"ntdll.dll"), "RtlGetVersion"));
        OSVERSIONINFOW version{}; version.dwOSVersionInfoSize = sizeof(version);
        return function && function(&version) == 0 && version.dwBuildNumber >= 20348;
    }
    void discoverApplications() {
        if (!processCapture) return;
        ComPtr<IMMDeviceEnumerator> enumerator;
        if (FAILED(CoCreateInstance(__uuidof(MMDeviceEnumerator), nullptr, CLSCTX_ALL,
                __uuidof(IMMDeviceEnumerator), reinterpret_cast<void**>(enumerator.address())))) return;
        ComPtr<IMMDeviceCollection> devices;
        if (FAILED(enumerator->EnumAudioEndpoints(eRender, DEVICE_STATE_ACTIVE, devices.address()))) return;
        UINT count = 0; devices->GetCount(&count);
        std::map<std::string, std::set<DWORD>> applications;
        for (UINT d=0; d<count; ++d) {
            ComPtr<IMMDevice> device; if (FAILED(devices->Item(d, device.address()))) continue;
            ComPtr<IAudioSessionManager2> manager;
            if (FAILED(device->Activate(__uuidof(IAudioSessionManager2), CLSCTX_ALL, nullptr, reinterpret_cast<void**>(manager.address())))) continue;
            ComPtr<IAudioSessionEnumerator> sessions;
            if (FAILED(manager->GetSessionEnumerator(sessions.address()))) continue;
            int sessionCount = 0; sessions->GetCount(&sessionCount);
            for (int s=0; s<sessionCount; ++s) {
                ComPtr<IAudioSessionControl> control;
                if (FAILED(sessions->GetSession(s, control.address()))) continue;
                AudioSessionState state;
                if (FAILED(control->GetState(&state)) || state == AudioSessionStateExpired) continue;
                ComPtr<IAudioSessionControl2> details;
                if (FAILED(control->QueryInterface(__uuidof(IAudioSessionControl2), reinterpret_cast<void**>(details.address())))) continue;
                DWORD pid = 0; if (FAILED(details->GetProcessId(&pid)) || !pid || pid == GetCurrentProcessId()) continue;
                HANDLE process = OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION, FALSE, pid);
                if (!process) continue;
                wchar_t path[32768]; DWORD size = 32768;
                bool found = QueryFullProcessImageNameW(process, 0, path, &size); CloseHandle(process);
                if (!found) continue;
                CharLowerBuffW(path, size);
                auto fullPath = utf8(path), id = "app:" + fullPath;
                applications[id].insert(pid);
            }
        }
        std::map<DWORD, DWORD> parents;
        HANDLE snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
        if (snapshot != INVALID_HANDLE_VALUE) {
            PROCESSENTRY32 entry{}; entry.dwSize = sizeof(entry);
            if (Process32First(snapshot, &entry)) do { parents[entry.th32ProcessID] = entry.th32ParentProcessID; } while (Process32Next(snapshot, &entry));
            CloseHandle(snapshot);
        }
        for (const auto& app : applications) {
            DeviceInfo info{{app.first, app.first.substr(app.first.find_last_of("\\/") + 1), AudioEndpointType::Application}};
            // Capture each independent process tree once. Multiple browser sessions must not double the same samples.
            for (DWORD pid : app.second) {
                DWORD ancestor = pid; bool covered = false;
                for (unsigned depth=0; depth<128; ++depth) {
                    auto parent = parents.find(ancestor);
                    if (parent == parents.end() || !parent->second || parent->second == ancestor) break;
                    ancestor = parent->second;
                    if (app.second.count(ancestor)) { covered = true; break; }
                }
                if (!covered) info.processes.push_back(pid);
            }
            inputs.push_back(std::move(info));
        }
    }
    void discover() {
        inputs.clear(); outputs.clear();
        // Stable logical default follows the OS; explicit hardware IDs never fall back.
        outputs.push_back({{"default", "System Default", AudioEndpointType::HardwareOutput}});
        if (processCapture) inputs.push_back({{"system", "System Audio (ohne EasyKnack)", AudioEndpointType::SystemMix}, {}, GetCurrentProcessId()});
        ma_device_info *playback = nullptr, *capture = nullptr;
        ma_uint32 playbackCount = 0, captureCount = 0;
        if (ma_context_get_devices(&context, &playback, &playbackCount, &capture, &captureCount) == MA_SUCCESS) {
            for (ma_uint32 i=0; i<playbackCount; ++i)
                outputs.push_back({{"out:" + (nullBackend ? std::to_string(i) : utf8(playback[i].id.wasapi)), playback[i].name, AudioEndpointType::HardwareOutput}, playback[i].id});
            for (ma_uint32 i=0; i<captureCount; ++i)
                inputs.push_back({{"in:" + (nullBackend ? std::to_string(i) : utf8(capture[i].id.wasapi)), capture[i].name, AudioEndpointType::HardwareInput}, capture[i].id});
        }
        discoverApplications();
    }
    const DeviceInfo* find(const std::string& id, bool input) const {
        const auto& list = input ? inputs : outputs;
        for (const auto& item : list) if (item.endpoint.id == id) return &item;
        return nullptr;
    }
    std::string routeKey(const std::vector<AudioRoute>& routes) const {
        std::map<std::pair<bool,std::string>,AudioRoute> unique;
        for (const auto& route:routes) unique[{route.input,route.endpoint}]=route;
        std::string key;
        for (const auto& entry:unique) {
            const auto& route=entry.second; auto info=find(route.endpoint,route.input);
            key+=(route.input?"i":"o")+hexText(route.endpoint)+":"+(info?std::to_string(info->process):"missing")+";";
            if (info) for (auto pid:info->processes) key+=std::to_string(pid)+",";
        }
        return key;
    }
    static void prime(Stream& stream) {
        stream.ring.reset();
        stream.directFrames.store(0);
        if (!stream.input) stream.ring.write(nullptr, StereoRing<>::adaptiveTargetFrames);
    }
    void pauseStreams() {
        ma_device_stop(&clock);
        // The DSP producer must finish before any consumer/buffer is reset or destroyed.
        if (master) master->stop();
        for (auto& stream : streams) if (stream->initialized && stream.get()!=master) stream->stop();
        master = nullptr;
    }
    void resumeStreams() {
        if (running) {
            if (clockEnabled) for (auto& stream : streams)
                if (!stream->input && stream->initialized) { master=stream.get(); break; }
            for (auto& stream : streams) {
                stream->drivesDsp = stream.get()==master;
                prime(*stream);
                stream->failed.store(false);
            }
            // Capture and secondary sinks are ready before the master starts the DSP.
            for (auto& stream : streams) if (stream->initialized && stream.get()!=master)
                stream->failed.store(!stream->start());
            if (master && !master->start()) { master->failed.store(true); master=nullptr; }
        }
        if (clockEnabled && !master && ma_device_start(&clock)!=MA_SUCCESS)
            throw std::runtime_error("audio fallback clock start failed");
    }
    AudioEndpoint withAvailability(AudioEndpoint endpoint) const {
        for (const auto& stream : streams) if (stream->id == endpoint.id && stream->input ==
                (endpoint.type == AudioEndpointType::HardwareInput || endpoint.type == AudioEndpointType::Application || endpoint.type == AudioEndpointType::SystemMix))
            if (!stream->initialized || stream->failed.load()) endpoint.available = false;
        return endpoint;
    }
    void release() noexcept {
        if (clockReady) pauseStreams();
        if (clockReady) { ma_device_uninit(&clock); clockReady = false; }
        streams.clear();
        if (clockContextReady) { ma_context_uninit(&clockContext); clockContextReady = false; }
        if (contextReady) { ma_context_uninit(&context); contextReady = false; }
        if (comReady) { CoUninitialize(); comReady = false; }
    }
public:
    float takePeak(const std::string& id) noexcept {
        float peak=0;
        for(auto& stream:streams) if(!stream->input && stream->id==id) peak=std::max(peak,stream->peak.exchange(0));
        return peak;
    }
    std::string bufferDescription() const {
        std::string result;
        for (const auto& stream:streams) if (stream->input) {
            if (!result.empty()) result += " ";
            result += hexText(stream->id)+" "+std::to_string(stream->maxCapturePacket.load())
                +" "+std::to_string(stream->ring.available())+" "+std::to_string(inputTarget(*stream));
        }
        return result.empty()?"-":result;
    }
    std::string clockDescription() const {
        if (!clockEnabled) return "external";
        if (!master) return "timer";
        return "output " + hexText(master->id) + " "
            + std::to_string(master->device.playback.internalPeriodSizeInFrames) + " "
            + std::to_string(master->device.playback.internalSampleRate) + " "
            + std::to_string(inputReserve);
    }
    bool needsRefresh() const {
        return routeKey(selectedRoutes)!=preparedKey || std::any_of(streams.begin(),streams.end(),[this](const auto& s) {
            return find(s->id,s->input) && (!s->initialized || s->failed.load());
        });
    }
    // Control thread only. False gives sole DSP ownership to ASIO.
    void setClockEnabled(bool value) {
        if (clockEnabled == value) return;
        pauseStreams();
        clockEnabled = value;
        resumeStreams();
    }
    explicit WindowsAudioBackend(AudioEngine& target, bool useNull) : engine(target), nullBackend(useNull) {
        try {
            comReady = SUCCEEDED(CoInitializeEx(nullptr, COINIT_MULTITHREADED));
            processCapture = !useNull && supportsProcessCapture();
            ma_backend backend = useNull ? ma_backend_null : ma_backend_wasapi;
            if (ma_context_init(&backend, 1, nullptr, &context) != MA_SUCCESS) throw std::runtime_error("WASAPI initialization failed");
            contextReady = true;
            ma_backend timerBackend = ma_backend_null;
            if (ma_context_init(&timerBackend, 1, nullptr, &clockContext) != MA_SUCCESS) throw std::runtime_error("audio clock initialization failed");
            clockContextReady = true;
            auto config = ma_device_config_init(ma_device_type_playback);
            config.playback.format = ma_format_f32; config.playback.channels = 2;
            config.sampleRate = sampleRate; config.periodSizeInFrames = blockSize;
            config.dataCallback = clockCallback; config.pUserData = this;
            if (ma_device_init(&clockContext, &config, &clock) != MA_SUCCESS) throw std::runtime_error("audio clock failed");
            clockReady = true;
            discover();
            if (ma_device_start(&clock) != MA_SUCCESS) throw std::runtime_error("audio clock start failed");
        } catch (...) { release(); throw; }
    }
    ~WindowsAudioBackend() override { release(); }
    AudioBackendCapabilities capabilities() const override { return {processCapture, processCapture, false, false, false}; }
    std::vector<AudioEndpoint> enumerateInputs() override {
        discover(); std::vector<AudioEndpoint> result;
        for (const auto& input : inputs) result.push_back(withAvailability(input.endpoint));
        return result;
    }
    std::vector<AudioEndpoint> enumerateOutputs() override {
        std::vector<AudioEndpoint> result; for (const auto& output : outputs) result.push_back(withAvailability(output.endpoint)); return result;
    }
    void prepare(const std::vector<AudioRoute>& routes) override {
        std::map<std::pair<bool, std::string>, AudioRoute> unique;
        for (const auto& route : routes) {
            if (route.mode != AudioCaptureMode::Copy) throw std::runtime_error("ROUTE requires a routing backend");
            unique[{route.input, route.endpoint}] = route;
        }
        if (unique.size() > 32) throw std::runtime_error("Maximum 32 simultaneous audio endpoints");
        std::string key=routeKey(routes);
        std::size_t streamCount = 0;
        for (const auto& entry : unique) {
            auto info = find(entry.second.endpoint, entry.second.input);
            streamCount += info ? std::max<std::size_t>(1, info->processes.size()) : 1;
        }
        if (streamCount > 64) throw std::runtime_error("Maximum 64 simultaneous audio streams");
        if (key == preparedKey) return;
        pauseStreams(); // No callback sees a half-open topology.
        streams.clear();
        selectedRoutes = routes; preparedKey = key;
        for (const auto& entry : unique) {
            const auto& route = entry.second;
            auto info = find(route.endpoint, route.input);
            std::vector<DWORD> processes = info && !info->processes.empty() ? info->processes : std::vector<DWORD>{info ? info->process : 0};
            for (DWORD pid : processes) {
            auto stream = std::make_unique<Stream>(); stream->owner=this; stream->id = route.endpoint; stream->input = route.input;
            stream->name = info ? info->endpoint.name : route.endpoint;
            if (info) {
                bool loopback = info->endpoint.type == AudioEndpointType::SystemMix || info->endpoint.type == AudioEndpointType::Application;
                if (loopback) {
                    stream->processCapture = std::make_unique<WindowsProcessCapture>(stream->ring, stream->failed);
                    stream->processResult = stream->processCapture->open(pid, info->endpoint.type == AudioEndpointType::SystemMix);
                    stream->initialized = SUCCEEDED(stream->processResult);
                } else {
                auto config = ma_device_config_init(loopback ? ma_device_type_loopback : route.input ? ma_device_type_capture : ma_device_type_playback);
                config.sampleRate = sampleRate; config.periodSizeInFrames = 128;
                config.noFixedSizedCallback = MA_TRUE;
                config.wasapi.usage = ma_wasapi_usage_pro_audio;
                config.wasapi.noAutoConvertSRC = MA_TRUE;
                config.dataCallback = deviceCallback; config.notificationCallback = notification; config.pUserData = stream.get();
                config.capture.format = ma_format_f32; config.capture.channels = 2;
                config.playback.format = ma_format_f32; config.playback.channels = 2;
                config.wasapi.noAutoStreamRouting = route.endpoint != "default";
                if (loopback) {
                    config.wasapi.loopbackProcessID = pid;
                    config.wasapi.loopbackProcessExclude = info->endpoint.type == AudioEndpointType::SystemMix;
                } else if (route.input) config.capture.pDeviceID = &info->id;
                else if (route.endpoint != "default") config.playback.pDeviceID = &info->id;
                // miniaudio converts native channel layouts and rates to stereo float/48 kHz here.
                stream->openResult = ma_device_init(&context, &config, &stream->device);
                stream->initialized = stream->openResult == MA_SUCCESS;
                }
            }
            streams.push_back(std::move(stream));
            }
        }
        resumeStreams();
    }
    void refresh() override {
        // Called after enumeration. Missing devices/PIDs change the key. Failed opens retry.
        if (std::any_of(streams.begin(), streams.end(), [this](const auto& s) {
                return find(s->id, s->input) && (!s->initialized || s->failed.load()); })) preparedKey.clear();
        prepare(selectedRoutes);
    }
    void setRunning(bool value) override {
        if (running == value) return;
        pauseStreams();
        running = value;
        if (!value) for (auto& stream : streams) { stream->ring.reset(); stream->failed.store(false); }
        resumeStreams();
    }
    std::string status() const override {
        std::string result;
        for (const auto& stream : streams) if (!stream->initialized || stream->failed.load()) {
            if (!result.empty()) result += "; "; result += "Nicht verfügbar: " + stream->name;
            if (stream->openResult != MA_SUCCESS) result += " (" + std::string(ma_result_description(stream->openResult)) + ")";
            if (FAILED(stream->processResult)) result += " (WASAPI " + std::to_string(static_cast<unsigned long>(stream->processResult)) + ")";
        }
        return result;
    }
    AudioIOStatistics statistics() const override {
        AudioIOStatistics result;
        for (const auto& stream : streams) {
            if (stream->input) result.capturedFrames += stream->ring.writtenFrames();
            else result.playedFrames += stream.get()==master ? stream->directFrames.load() : stream->ring.readFrames();
            result.underruns += stream->ring.underruns.load(); result.overruns += stream->ring.overruns.load();
            if(stream->input){result.captureUnder+=stream->ring.underruns.load();result.captureOver+=stream->ring.overruns.load();}
            else{result.playbackUnder+=stream->ring.underruns.load();result.playbackOver+=stream->ring.overruns.load();}
            result.priming+=stream->ring.priming.load();
        }
        return result;
    }
    void beginBlock(std::size_t frames) noexcept override {
        for (auto& stream : streams) {
            std::fill_n(stream->block.data(), frames*2, 0.f);
            if (stream->input && running && stream->initialized && !stream->failed.load(std::memory_order_relaxed)) {
                stream->ring.readAdaptive(stream->block.data(), frames, inputTarget(*stream));
            }
        }
    }
    float input(const std::string& id, std::size_t channel, std::size_t frame) const noexcept override {
        float value = 0;
        for (const auto& stream : streams) if (stream->input && stream->id == id) value += stream->block[2*frame+channel];
        return value;
    }
    void output(const std::string& id, std::size_t channel, const float* samples, std::size_t frames) noexcept override {
        for (auto& stream : streams) if (!stream->input && stream->id == id)
            for (std::size_t i=0; i<frames; ++i) stream->block[2*i+channel] += std::isfinite(samples[i]) ? samples[i] : 0;
    }
    void endBlock(const float* gain, std::size_t frames) noexcept override {
        if (!running) return;
        for (auto& stream : streams) if (!stream->input && stream->initialized && !stream->failed.load(std::memory_order_relaxed)) {
            float peak=0;
            for (std::size_t i=0; i<frames; ++i) for (unsigned channel=0; channel<2; ++channel)
                stream->block[2*i+channel] = std::clamp(stream->block[2*i+channel] * gain[i], -1.f, 1.f);
            for(std::size_t i=0;i<frames*2;++i) peak=std::max(peak,std::abs(stream->block[i]));
            stream->peak.store(std::max(peak,stream->peak.load(std::memory_order_relaxed)),std::memory_order_relaxed);
            if (stream.get()==master && directOutput)
                std::copy_n(stream->block.data(), frames*2, directOutput+directOffset*2);
            else stream->ring.write(stream->block.data(), frames);
        }
        directOffset += frames;
    }
};
}
