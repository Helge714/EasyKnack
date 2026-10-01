#pragma once
#include "AudioBackend.hpp"
#include <windows.h>
#include <mmdeviceapi.h>
#include <audioclient.h>
#include <propidl.h>
#include <thread>

namespace easyknack {
/** WASAPI process loopback, scoped to Windows. No OS object reaches the graph.
 * Uses ActivateAudioInterfaceAsync (Windows build 20348+), including an agile completion handler.
 * Our bundled miniaudio's desktop path cannot activate the virtual process-loopback interface. */
class WindowsProcessCapture {
    struct ActivationParams { int type = 1; DWORD process; int mode; }; // AUDIOCLIENT_ACTIVATION_PARAMS ABI
    class Activation final : public IActivateAudioInterfaceCompletionHandler, public IAgileObject {
        std::atomic<ULONG> refs{1};
    public:
        HANDLE completed = CreateEventW(nullptr, TRUE, FALSE, nullptr);
        ActivationParams parameters;
        PROPVARIANT property{};
        HRESULT result = E_PENDING;
        IAudioClient* client = nullptr;
        Activation(DWORD pid, bool exclude) : parameters{1, pid, exclude ? 1 : 0} {
            property.vt = VT_BLOB; property.blob.cbSize = sizeof(parameters);
            property.blob.pBlobData = reinterpret_cast<BYTE*>(&parameters);
        }
        ~Activation() { if (client) client->Release(); if (completed) CloseHandle(completed); }
        HRESULT STDMETHODCALLTYPE QueryInterface(REFIID iid, void** object) override {
            if (!object) return E_POINTER;
            *object = nullptr;
            if (iid == __uuidof(IUnknown) || iid == __uuidof(IActivateAudioInterfaceCompletionHandler))
                *object = static_cast<IActivateAudioInterfaceCompletionHandler*>(this);
            else if (iid == __uuidof(IAgileObject)) *object = static_cast<IAgileObject*>(this);
            else return E_NOINTERFACE;
            AddRef(); return S_OK;
        }
        ULONG STDMETHODCALLTYPE AddRef() override { return ++refs; }
        ULONG STDMETHODCALLTYPE Release() override { auto remaining = --refs; if (!remaining) delete this; return remaining; }
        HRESULT STDMETHODCALLTYPE ActivateCompleted(IActivateAudioInterfaceAsyncOperation* operation) override {
            IUnknown* activated = nullptr; HRESULT activationResult = E_FAIL;
            result = operation->GetActivateResult(&activationResult, &activated);
            if (SUCCEEDED(result)) result = activationResult;
            if (SUCCEEDED(result) && activated) result = activated->QueryInterface(__uuidof(IAudioClient), reinterpret_cast<void**>(&client));
            if (activated) activated->Release();
            SetEvent(completed); return S_OK;
        }
    };
    StereoRing<>& ring;
    std::atomic<bool>& failed;
    IAudioClient* client = nullptr;
    IAudioCaptureClient* capture = nullptr;
    HANDLE event = nullptr;
    std::atomic<bool> stopping{true};
    std::thread worker;
    void pump() noexcept {
        bool com = SUCCEEDED(CoInitializeEx(nullptr, COINIT_MULTITHREADED));
        while (!stopping.load(std::memory_order_relaxed)) {
            WaitForSingleObject(event, 50);
            if (stopping.load(std::memory_order_relaxed)) break;
            UINT32 frames = 0;
            HRESULT hr = capture->GetNextPacketSize(&frames);
            while (SUCCEEDED(hr) && frames && !stopping.load(std::memory_order_relaxed)) {
                BYTE* data = nullptr; DWORD flags = 0;
                hr = capture->GetBuffer(&data, &frames, &flags, nullptr, nullptr);
                if (FAILED(hr)) break;
                ring.write(flags & AUDCLNT_BUFFERFLAGS_SILENT ? nullptr : reinterpret_cast<const float*>(data), frames);
                hr = capture->ReleaseBuffer(frames);
                if (SUCCEEDED(hr)) hr = capture->GetNextPacketSize(&frames);
            }
            if (FAILED(hr)) { failed.store(true); break; }
        }
        if (com) CoUninitialize();
    }
public:
    WindowsProcessCapture(StereoRing<>& target, std::atomic<bool>& status) : ring(target), failed(status) { }
    ~WindowsProcessCapture() {
        stop();
        if (capture) capture->Release(); if (client) client->Release(); if (event) CloseHandle(event);
    }
    HRESULT open(DWORD pid, bool exclude) {
        using Activate = HRESULT(WINAPI*)(LPCWSTR, REFIID, PROPVARIANT*, IActivateAudioInterfaceCompletionHandler*, IActivateAudioInterfaceAsyncOperation**);
        auto activate = reinterpret_cast<Activate>(GetProcAddress(GetModuleHandleW(L"mmdevapi.dll"), "ActivateAudioInterfaceAsync"));
        if (!activate) return E_NOTIMPL;
        auto* completion = new Activation(pid, exclude);
        if (!completion->completed) { completion->Release(); return E_OUTOFMEMORY; }
        IActivateAudioInterfaceAsyncOperation* operation = nullptr;
        HRESULT result = activate(L"VAD\\Process_Loopback", __uuidof(IAudioClient), &completion->property, completion, &operation);
        if (SUCCEEDED(result)) {
            if (WaitForSingleObject(completion->completed, 4000) == WAIT_OBJECT_0) {
                result = completion->result;
                if (SUCCEEDED(result) && completion->client) { client = completion->client; client->AddRef(); }
                else if (SUCCEEDED(result)) result = E_NOINTERFACE;
            } else result = HRESULT_FROM_WIN32(ERROR_TIMEOUT);
        }
        if (operation) operation->Release();
        completion->Release(); // The async operation owns its callback until completion, including on timeout.
        if (FAILED(result)) return result;
        WAVEFORMATEX format{};
        format.wFormatTag = WAVE_FORMAT_IEEE_FLOAT; format.nChannels = 2; format.nSamplesPerSec = 48000;
        format.wBitsPerSample = 32; format.nBlockAlign = 8; format.nAvgBytesPerSec = 48000*8;
        result = client->Initialize(AUDCLNT_SHAREMODE_SHARED, AUDCLNT_STREAMFLAGS_LOOPBACK | AUDCLNT_STREAMFLAGS_EVENTCALLBACK,
                1000000, 0, &format, nullptr);
        if (FAILED(result)) return result;
        event = CreateEventW(nullptr, FALSE, FALSE, nullptr);
        if (!event) return E_OUTOFMEMORY;
        result = client->SetEventHandle(event);
        if (FAILED(result)) return result;
        return client->GetService(__uuidof(IAudioCaptureClient), reinterpret_cast<void**>(&capture));
    }
    bool start() {
        if (worker.joinable()) return !failed.load();
        if (!capture || FAILED(client->Start())) return false;
        stopping.store(false); worker = std::thread([this] { pump(); }); return true;
    }
    void stop() noexcept {
        stopping.store(true); if (event) SetEvent(event);
        if (worker.joinable()) worker.join();
        if (client) client->Stop();
    }
};
}
