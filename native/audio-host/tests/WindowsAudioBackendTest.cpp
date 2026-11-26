#define MINIAUDIO_IMPLEMENTATION
#define EASYKNACK_AUDIO_TEST
#include "audio/WindowsAudioBackend.hpp"
#include "audio/WindowsDeviceVolume.hpp"
#include <iostream>

namespace easyknack {
struct WindowsDeviceVolumeTest {
    static bool apartmentLifecycle() {
        bool valid=true;
        std::thread control([&]{
            auto noExplicitApartment=[] {
                auto result=CoInitializeEx(nullptr,COINIT_MULTITHREADED);
                if(SUCCEEDED(result))CoUninitialize();
                return result==S_OK; // An implicit process MTA is allowed; no explicit reference is.
            };
            valid &= noExplicitApartment();
            { WindowsDeviceVolume volume(false);valid &= noExplicitApartment(); }
            { WindowsDeviceVolume volume(true);valid &= volume.enumerator!=nullptr; }
            valid &= noExplicitApartment();
            for(auto mode:{COINIT_MULTITHREADED,COINIT_APARTMENTTHREADED}) {
                auto initialized=CoInitializeEx(nullptr,mode);
                if(FAILED(initialized)){valid=false;continue;}
                { WindowsDeviceVolume volume(true);valid &= volume.enumerator!=nullptr; }
                // Destroying volume preserves the caller's apartment, including STA.
                auto retained=CoInitializeEx(nullptr,mode);
                valid &= retained==S_FALSE;
                if(SUCCEEDED(retained))CoUninitialize();
                CoUninitialize();
                valid &= noExplicitApartment();
            }
        });
        control.join();return valid;
    }
    static bool run() {
        if(!apartmentLifecycle())return false;
        std::map<std::string,float> devices{{"a",.4f},{"b",.8f},{"other",.9f}};
        unsigned writes=0;
        WindowsDeviceVolume volume(false,[&](const std::string& id) {
            auto it=devices.find(id); return it==devices.end()?WindowsDeviceVolume::Value{}:WindowsDeviceVolume::Value{true,it->second,false};
        },[&](const std::string& id,float value) {
            if(!devices.count(id)) return false; devices[id]=value; ++writes; return true;
        });
        volume.links({"a","b","missing"});
        if(writes) return false; // Selecting targets must not unexpectedly change system volume.
        volume.hookReady=true; volume.poll(); volume.hookOwner=&volume;
        KBDLLHOOKSTRUCT key{}; key.vkCode=VK_VOLUME_UP;
        auto down=WindowsDeviceVolume::hook(HC_ACTION,WM_KEYDOWN,reinterpret_cast<LPARAM>(&key));
        auto up=WindowsDeviceVolume::hook(HC_ACTION,WM_KEYUP,reinterpret_cast<LPARAM>(&key));
        volume.hookOwner=nullptr;
        if(down!=1 || up!=1 || writes) return false;
        volume.poll();
        if(writes!=2 || std::abs(devices["a"]-.41f)>1e-6f || std::abs(devices["b"]-.82f)>1e-6f || devices["other"]!=.9f) return false;
        if(volume.keyEvent()!="1 0.820000 2") return false;
        volume.steps.store(-100); volume.poll();
        if(devices["a"]!=0 || devices["b"]!=0) return false;
        if(volume.keyEvent()!="2 0.000000 2") return false;
        volume.steps.store(1); volume.poll();
        if(std::abs(devices["a"]-.01f)>1e-6f || std::abs(devices["b"]-.02f)>1e-6f) return false;
        volume.steps.store(100); volume.poll();
        if(devices["a"]!=.5f || devices["b"]!=1.f) return false;
        volume.links({}); volume.steps.store(1); volume.poll();
        if(volume.keysActive() || writes!=8) return false;
        try { volume.set("a",std::numeric_limits<float>::quiet_NaN()); return false; } catch(const std::runtime_error&) { }
        return true;
    }
};
struct WindowsAudioBackendTest {
    static bool run() {
        auto engine=std::make_unique<AudioEngine>();
        WindowsAudioBackend backend(*engine,true);
        backend.setClockEnabled(false); // Deterministic manual device callbacks below.
        auto input=std::make_unique<WindowsAudioBackend::Stream>();
        input->owner=&backend; input->id="source"; input->input=true;
        std::array<float,8192> samples{};
        for (unsigned i=0;i<4096;++i) { samples[2*i]=.75f; samples[2*i+1]=-.25f; }
        input->ring.write(samples.data(),4096);
        auto output=std::make_unique<WindowsAudioBackend::Stream>();
        output->owner=&backend; output->id="sink"; output->device.pUserData=output.get();
        output->drivesDsp=true;
        backend.master=output.get();
        backend.streams.push_back(std::move(input));
        backend.streams.push_back(std::move(output));
        // Simulated devices never call ma_device_init/start/uninit.
        for (auto& stream:backend.streams) stream->initialized=true;
        backend.running=true;
        engine->publish(AudioGraph::parse("GRAPH 3 src input "+hexText("source")
            +" copy stereo @ a - a deviceout 0 "+hexText("sink")
            +" @ a a b deviceout 0 "+hexText("sink")+" @ a a"));
        engine->playing.store(true);
        bool valid=true;
        float envelope=0;
        std::uint64_t total=0;
        for (unsigned frames:{1u,127u,128u,257u,513u}) {
            std::vector<float> buffer(frames*2+2,99.f);
            WindowsAudioBackend::deviceCallback(&backend.master->device,buffer.data()+1,nullptr,frames);
            valid &= buffer.front()==99.f && buffer.back()==99.f;
            for (unsigned f=0;f<frames;++f) {
                envelope+=(.2f-envelope)*.004f;
                valid &= std::abs(buffer[1+2*f]-1.5f*envelope)<1e-6f;
                valid &= std::abs(buffer[2+2*f]+.5f*envelope)<1e-6f;
            }
            total+=frames;
        }
        valid &= backend.master->directFrames.load()==total;
        valid &= backend.master->ring.writtenFrames()==0;
        // Buffered callbacks retain routing, stereo and partial callback offsets.
        // Wait on the test side only; the device callback itself never waits.
        backend.renderAhead.start(WindowsAudioBackend::renderQueued,&backend);
        backend.renderAhead.enable(1024);
        auto reserve=backend.renderAhead.latencyFrames();
        std::array<float,512> buffered{};
        for(unsigned consumed=0;consumed<reserve;consumed+=256){
            WindowsAudioBackend::deviceCallback(&backend.master->device,buffered.data(),nullptr,256);
            for(float value:buffered)valid &= value==0;
            auto until=std::chrono::steady_clock::now()+std::chrono::seconds(5);
            while(backend.renderAhead.renderedFrames()<consumed+256&&std::chrono::steady_clock::now()<until)std::this_thread::yield();
            valid &= backend.renderAhead.renderedFrames()>=consumed+256;
        }
        WindowsAudioBackend::deviceCallback(&backend.master->device,buffered.data(),nullptr,256);
        for(unsigned f=0;f<256;f++){
            envelope+=(.2f-envelope)*.004f;
            valid &= std::abs(buffered[2*f]-1.5f*envelope)<1e-6f;
            valid &= std::abs(buffered[2*f+1]+.5f*envelope)<1e-6f;
        }
        valid &= backend.renderAhead.underruns()==0&&backend.renderAhead.overruns()==0;
        backend.renderAhead.stop();
        // Restore simulated handles before the ordinary backend destructor runs.
        for (auto& stream:backend.streams) stream->initialized=false;
        backend.master=nullptr;
        backend.running=false;
        return valid;
    }
};
}
int main() {
    if (!easyknack::WindowsAudioBackendTest::run() || !easyknack::WindowsDeviceVolumeTest::run()) {
        std::cerr<<"Direct WASAPI callback stereo/offset/summing test failed\n"; return 1;
    }
    std::cout<<"Direct WASAPI callback tests passed\n";
}
