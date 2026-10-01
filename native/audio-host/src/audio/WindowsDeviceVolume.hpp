#pragma once
#include "ProtocolText.hpp"
#include <windows.h>
#include <mmdeviceapi.h>
#include <endpointvolume.h>
#include <atomic>
#include <thread>
#include <future>
#include <set>
#include <sstream>
#include <cmath>
#include <algorithm>
#include <functional>
#include <map>

namespace easyknack {
/** All COM calls run on the host control thread. The keyboard hook only queues steps. */
class WindowsDeviceVolume {
public:
    struct Value { bool available=false; float scalar=0; bool muted=false; };
private:
#ifdef EASYKNACK_AUDIO_TEST
    friend struct WindowsDeviceVolumeTest;
#endif
    std::function<Value(const std::string&)> readOverride;
    std::function<bool(const std::string&,float)> writeOverride;
    IMMDeviceEnumerator* enumerator=nullptr;
    std::thread keyboard;
    DWORD keyboardThread=0;
    std::atomic<bool> enabled{false};
    std::atomic<ULONGLONG> heartbeat{0};
    std::atomic<int> steps{0};
    std::set<std::string> linked;
    std::map<std::string,float> proportions;
    bool hookReady=false;
    std::uint64_t keySequence=0;
    float keyLevel=0;
    unsigned keyTargets=0;
    inline static WindowsDeviceVolume* hookOwner=nullptr;
    static LRESULT CALLBACK hook(int code, WPARAM message, LPARAM data) {
        auto* self=hookOwner;
        if(code==HC_ACTION && self && self->enabled.load() && GetTickCount64()-self->heartbeat.load()<1500) {
            auto key=reinterpret_cast<KBDLLHOOKSTRUCT*>(data)->vkCode;
            if(key==VK_VOLUME_UP || key==VK_VOLUME_DOWN) {
                if(message==WM_KEYDOWN || message==WM_SYSKEYDOWN)
                    self->steps.fetch_add(key==VK_VOLUME_UP?1:-1);
                return 1;
            }
        }
        return CallNextHookEx(nullptr,code,message,data);
    }
    IAudioEndpointVolume* open(const std::string& id) {
        if(!enumerator) return nullptr;
        IMMDevice* device=nullptr;
        if(id=="default") enumerator->GetDefaultAudioEndpoint(eRender,eConsole,&device);
        else if(id.rfind("out:",0)==0) {
            auto text=id.substr(4);
            int n=MultiByteToWideChar(CP_UTF8,MB_ERR_INVALID_CHARS,text.data(),static_cast<int>(text.size()),nullptr,0);
            if(n<=0) return nullptr;
            std::wstring wide(n,L'\0');
            MultiByteToWideChar(CP_UTF8,MB_ERR_INVALID_CHARS,text.data(),static_cast<int>(text.size()),wide.data(),n);
            enumerator->GetDevice(wide.c_str(),&device);
        }
        if(!device) return nullptr;
        DWORD state=0; device->GetState(&state);
        IAudioEndpointVolume* volume=nullptr;
        if(state==DEVICE_STATE_ACTIVE) device->Activate(__uuidof(IAudioEndpointVolume),CLSCTX_ALL,nullptr,reinterpret_cast<void**>(&volume));
        device->Release(); return volume;
    }
public:
    explicit WindowsDeviceVolume(bool realDevices,
            std::function<Value(const std::string&)> read={},std::function<bool(const std::string&,float)> write={})
            : readOverride(std::move(read)),writeOverride(std::move(write)) {
        if(!realDevices) return;
        CoCreateInstance(__uuidof(MMDeviceEnumerator),nullptr,CLSCTX_ALL,__uuidof(IMMDeviceEnumerator),reinterpret_cast<void**>(&enumerator));
        std::promise<bool> ready; auto future=ready.get_future();
        keyboard=std::thread([this,&ready] {
            MSG msg{}; PeekMessageW(&msg,nullptr,0,0,PM_NOREMOVE);
            keyboardThread=GetCurrentThreadId(); hookOwner=this;
            HHOOK handle=SetWindowsHookExW(WH_KEYBOARD_LL,hook,GetModuleHandleW(nullptr),0);
            ready.set_value(handle!=nullptr);
            if(handle) { while(GetMessageW(&msg,nullptr,0,0)>0) { TranslateMessage(&msg); DispatchMessageW(&msg); } UnhookWindowsHookEx(handle); }
            hookOwner=nullptr;
        });
        hookReady=future.get();
    }
    ~WindowsDeviceVolume() {
        enabled.store(false);
        if(keyboard.joinable()) { if(hookReady) PostThreadMessageW(keyboardThread,WM_QUIT,0,0); keyboard.join(); }
        if(enumerator) enumerator->Release();
    }
    Value get(const std::string& id) {
        if(readOverride) return readOverride(id);
        auto* volume=open(id); if(!volume) return {};
        float scalar=0; BOOL muted=FALSE;
        bool ok=SUCCEEDED(volume->GetMasterVolumeLevelScalar(&scalar)) && SUCCEEDED(volume->GetMute(&muted));
        volume->Release(); return {ok,scalar,muted!=FALSE};
    }
    bool set(const std::string& id,float value) {
        if(!std::isfinite(value) || value<0 || value>1) throw std::runtime_error("volume outside 0..1");
        if(writeOverride) return writeOverride(id,value);
        auto* volume=open(id); if(!volume) return false;
        bool ok=SUCCEEDED(volume->SetMasterVolumeLevelScalar(value,nullptr));
        // Moving an audible volume up also recovers an externally muted endpoint.
        if(ok && value>0) ok=SUCCEEDED(volume->SetMute(FALSE,nullptr));
        volume->Release(); return ok;
    }
    void links(std::set<std::string> ids) {
        if(ids.size()>32) throw std::runtime_error("too many volume targets");
        if(ids!=linked) proportions.clear();
        linked=std::move(ids); steps.store(0); poll();
    }
    void poll() {
        heartbeat.store(GetTickCount64());
        std::map<std::string,float> levels;
        float maximum=0;
        for(const auto& id:linked) {
            auto value=get(id);
            if(value.available) { levels[id]=value.scalar; maximum=std::max(maximum,value.scalar); }
        }
        enabled.store(!levels.empty() && hookReady);
        int delta=steps.exchange(0);
        if(delta && !levels.empty()) {
            if(maximum>0) for(const auto& [id,level]:levels) proportions[id]=level/maximum;
            float next=std::clamp(maximum+delta*.02f,0.f,1.f);
            unsigned changed=0;
            // Snapshot every level before writing: default and explicit endpoint IDs may alias.
            for(const auto& [id,level]:levels) {
                auto ratio=proportions.find(id);
                if(set(id,next*(ratio==proportions.end()?1.f:ratio->second))) ++changed;
            }
            if(changed) { ++keySequence; keyLevel=next; keyTargets=changed; }
        }
    }
    std::string keyEvent() const { return std::to_string(keySequence)+" "+std::to_string(keyLevel)+" "+std::to_string(keyTargets); }
    bool keysActive() const { return enabled.load(); }
};
}
