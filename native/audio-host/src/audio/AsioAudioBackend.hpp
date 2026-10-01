#pragma once
#include "AudioBackend.hpp"
#include "AsioDrivers.hpp"
#include "AsioSamples.hpp"
#include "../AudioGraph.hpp"
#include <map>
#include <set>
#include <chrono>

namespace easyknack {
/** One full-duplex driver clocks the graph. No COM, locks or allocation in callbacks. */
class AsioAudioBackend final : public AudioIO {
    struct Port { AudioEndpoint endpoint; std::string driver; long first, count; };
    struct Channel { AsioSampleFormat format; };
    struct Pair { std::string id; bool input; int left, right; std::array<float,blockSize*2> block{}; };
    AsioDrivers drivers;
    AudioEngine& engine;
    AudioIO& graphIO;
    std::vector<Port> ports;
    std::set<std::string> probed;
    std::set<std::string> selectedIds;
    AsioHandle driver;
    std::vector<ASIOBufferInfo> buffers;
    std::vector<Channel> channels;
    std::vector<Pair> pairs;
    std::vector<float> scratch;
    long frames=0, index=0;
    std::size_t offset=0;
    bool created=false, started=false, outputReady=false;
    std::string error;
    std::atomic<bool> resetRequested{false}, enabled{false};
    std::atomic<std::uint64_t> captured{0}, played{0}, overloads{0};
    inline static std::atomic<AsioAudioBackend*> active{nullptr};
    ASIOCallbacks callbacks{bufferSwitch,sampleRateChanged,message,timeSwitch};
    static void check(ASIOError code, const char* operation) {
        if (code!=ASE_OK && code!=ASE_SUCCESS) throw std::runtime_error(std::string("ASIO ")+operation+" ("+std::to_string(code)+")");
    }
    static void bufferSwitch(long i, ASIOBool) {
        if (auto* self=active.load(std::memory_order_acquire)) self->process(i);
    }
    static ASIOTime* timeSwitch(ASIOTime* time,long i,ASIOBool direct) { bufferSwitch(i,direct); return time; }
    static void sampleRateChanged(ASIOSampleRate rate) {
        if (auto* self=active.load()) if (rate!=sampleRate) self->resetRequested.store(true);
    }
    static long message(long selector,long value,void*,double*) {
        auto supported=[](long s) { return s==kAsioEngineVersion || s==kAsioResetRequest || s==kAsioResyncRequest
                || s==kAsioLatenciesChanged || s==kAsioOverload || s==kAsioSupportsTimeInfo; };
        if (selector==kAsioSelectorSupported) return supported(value)?1:0;
        if (selector==kAsioEngineVersion) return 2;
        if (selector==kAsioSupportsTimeInfo) return 1;
        if (auto* self=active.load()) {
            if (selector==kAsioResetRequest || selector==kAsioResyncRequest) { self->resetRequested.store(true); return 1; }
            if (selector==kAsioOverload) { self->overloads.fetch_add(1); return 1; }
            if (selector==kAsioLatenciesChanged) return 1;
        }
        return 0;
    }
    void process(long i) noexcept {
        if (i!=0 && i!=1) return;
        index=i; offset=0;
        // Drivers may callback during start/stop. Always deliver initialized silence then.
        for (std::size_t c=0;c<buffers.size();++c) if (!buffers[c].isInput)
            std::memset(buffers[c].buffers[i],0,static_cast<std::size_t>(frames)*channels[c].format.bytes);
        if (enabled.load(std::memory_order_acquire) && !resetRequested.load(std::memory_order_relaxed)) {
            engine.render(scratch.data(),static_cast<unsigned>(frames),&graphIO,true);
        }
        if (outputReady) driver->outputReady();
    }
    const Port* find(const AudioRoute& route) const {
        for (const auto& port:ports) if (port.endpoint.id==route.endpoint && route.input==(port.endpoint.type==AudioEndpointType::AsioInput)) return &port;
        return nullptr;
    }
public:
    AsioAudioBackend(AudioEngine& e,AudioIO& io,AsioDrivers source=windowsAsioDrivers()) : drivers(std::move(source)),engine(e),graphIO(io) { }
    ~AsioAudioBackend() override { close(); }
    static bool owns(const std::string& id) { return id.rfind("asio:",0)==0; }
    void discover() {
        // Probe once, while no ASIO device is open. Never instantiate another driver during playback.
        if (driver) return;
        for (const auto& entry:drivers.enumerate()) {
            if (!probed.insert(entry.id).second) continue;
            auto probe=drivers.open(entry.id); long in=0,out=0;
            if (!probe || probe->getChannels(&in,&out)!=ASE_OK || in<0 || out<0 || in>256 || out>256) {
                // Preserve a selectable diagnostic endpoint when a registered driver cannot initialize.
                for (bool input:{true,false}) ports.push_back({{"asio:"+entry.id+(input?":i:0":":o:0"),
                    entry.name+" (Treiber nicht verfügbar)",input?AudioEndpointType::AsioInput:AudioEndpointType::AsioOutput,false},entry.id,0,2});
                continue;
            }
            for (bool input:{true,false}) for (long c=0;c<(input?in:out);c+=2) {
                auto count=std::min<long>(2,(input?in:out)-c);
                ASIOChannelInfo info{}; info.channel=c; info.isInput=input?ASIOTrue:ASIOFalse;
                std::string label=entry.name+" | "+std::to_string(c+1)+(count==2?"–"+std::to_string(c+2):"");
                if (probe->getChannelInfo(&info)==ASE_OK) { info.name[sizeof(info.name)-1]=0; if (info.name[0]) label+=" · "+std::string(info.name); }
                ports.push_back({{"asio:"+entry.id+(input?":i:":":o:")+std::to_string(c),label,
                    input?AudioEndpointType::AsioInput:AudioEndpointType::AsioOutput},entry.id,c,count});
            }
        }
    }
    std::vector<AudioEndpoint> endpoints(bool input) const {
        std::vector<AudioEndpoint> result;
        for (const auto& p:ports) if (input==(p.endpoint.type==AudioEndpointType::AsioInput)) {
            auto e=p.endpoint;
            if (created && selectedIds.count(e.id)) e.available=true;
            if (!error.empty() && selectedIds.count(e.id)) e.available=false;
            result.push_back(std::move(e));
        }
        return result;
    }
    void validate(const std::vector<AudioRoute>& routes) const {
        std::set<std::string> selected;
        for (const auto& route:routes) {
            if (route.mode!=AudioCaptureMode::Copy) throw std::runtime_error("ASIO does not configure external routing automatically");
            if (auto p=find(route)) selected.insert(p->driver);
            else if (owns(route.endpoint)) {
                auto pos=route.endpoint.find(':',5);
                if (pos==std::string::npos) throw std::runtime_error("Invalid ASIO endpoint");
                selected.insert(route.endpoint.substr(5,pos-5));
            }
        }
        if (selected.size()>1) throw std::runtime_error("Only one ASIO driver can clock a graph; select channel pairs from the same driver");
    }
    void close() noexcept {
        stop();
        if (created) { driver->disposeBuffers(); created=false; }
        if (active.load()==this) active.store(nullptr,std::memory_order_release);
        driver.reset(); pairs.clear(); channels.clear(); buffers.clear(); scratch.clear(); frames=0;
    }
    void prepare(const std::vector<AudioRoute>& routes) {
        validate(routes); close(); error.clear(); resetRequested.store(false);
        selectedIds.clear(); for (const auto& route:routes) selectedIds.insert(route.endpoint);
        if (routes.empty()) return;
        try {
            std::map<std::pair<bool,std::string>,const Port*> unique;
            for (const auto& route:routes) {
                auto p=find(route);
                if (!p) throw std::runtime_error("ASIO endpoint unavailable: "+route.endpoint);
                unique[{route.input,route.endpoint}]=p;
            }
            if (unique.size()>32) throw std::runtime_error("ASIO: maximum 32 channel pairs");
            driver=drivers.open(unique.begin()->second->driver);
            if (!driver) throw std::runtime_error("ASIO driver cannot be opened (64-bit driver required)");
            ASIOSampleRate rate=0; check(driver->getSampleRate(&rate),"getSampleRate");
            if (rate!=sampleRate) { check(driver->canSampleRate(sampleRate),"requires 48000 Hz"); check(driver->setSampleRate(sampleRate),"setSampleRate"); }
            check(driver->getSampleRate(&rate),"getSampleRate");
            if (rate!=sampleRate) throw std::runtime_error("ASIO: driver did not accept 48000 Hz");
            long minimum=0,maximum=0,granularity=0;
            check(driver->getBufferSize(&minimum,&maximum,&frames,&granularity),"getBufferSize");
            if (frames<1 || frames>8192 || frames<minimum || frames>maximum) throw std::runtime_error("ASIO: unsupported buffer size (1..8192 frames)");
            long in=0,out=0; check(driver->getChannels(&in,&out),"getChannels");
            for (const auto& item:unique) {
                const auto& p=*item.second; bool input=item.first.first;
                if (p.first<0 || p.first+p.count>(input?in:out)) throw std::runtime_error("ASIO: channel no longer available");
                int first=static_cast<int>(buffers.size());
                for (long c=p.first;c<p.first+p.count;++c) {
                    ASIOChannelInfo info{}; info.channel=c; info.isInput=input?ASIOTrue:ASIOFalse;
                    check(driver->getChannelInfo(&info),"getChannelInfo");
                    channels.push_back({AsioSampleFormat::from(info.type)});
                    buffers.push_back({static_cast<ASIOBool>(input?ASIOTrue:ASIOFalse),c,{nullptr,nullptr}});
                }
                pairs.push_back({p.endpoint.id,input,first,p.count==2?first+1:first,{}});
            }
            scratch.resize(static_cast<std::size_t>(frames)*2);
            AsioAudioBackend* expected=nullptr;
            if (!active.compare_exchange_strong(expected,this)) throw std::runtime_error("ASIO driver already active");
            check(driver->createBuffers(buffers.data(),static_cast<long>(buffers.size()),frames,&callbacks),"createBuffers");
            created=true;
            for (std::size_t c=0;c<buffers.size();++c) for (int i=0;i<2;++i) {
                if (!buffers[c].buffers[i]) throw std::runtime_error("ASIO: driver returned null buffer");
                if (!buffers[c].isInput) std::memset(buffers[c].buffers[i],0,static_cast<std::size_t>(frames)*channels[c].format.bytes);
            }
            outputReady=driver->outputReady()==ASE_OK;
        } catch (const std::exception& e) { error=e.what(); close(); }
    }
    void start() {
        if (!created || started) return;
        enabled.store(true,std::memory_order_release);
        auto code=driver->start();
        if (code!=ASE_OK) { enabled.store(false); driver->stop(); error="ASIO start failed ("+std::to_string(code)+")"; }
        else { started=true; error.clear(); }
    }
    void stop() noexcept {
        enabled.store(false,std::memory_order_release);
        if (started) { driver->stop(); started=false; }
    }
    bool needsReset() const { return resetRequested.load() || !error.empty(); }
    bool ready() const { return created; }
    std::string status() const { return resetRequested.load()?"ASIO: Treiber fordert Neustart der Audiobuffer an":error; }
    AudioIOStatistics statistics() const { AudioIOStatistics s{captured.load(),played.load(),overloads.load(),0};s.driverOverload=overloads.load();return s; }
    void beginBlock(std::size_t count) noexcept override {
        for (auto& pair:pairs) {
            std::fill_n(pair.block.data(),count*2,0.f);
            if (pair.input) for (std::size_t f=0;f<count;++f) {
                pair.block[2*f]=channels[pair.left].format.read(buffers[pair.left].buffers[index],offset+f);
                pair.block[2*f+1]=channels[pair.right].format.read(buffers[pair.right].buffers[index],offset+f);
            }
        }
    }
    float input(const std::string& id,std::size_t channel,std::size_t frame) const noexcept override {
        for (const auto& pair:pairs) if (pair.input && pair.id==id) return pair.block[2*frame+channel];
        return 0;
    }
    void output(const std::string& id,std::size_t channel,const float* samples,std::size_t count) noexcept override {
        for (auto& pair:pairs) if (!pair.input && pair.id==id)
            for (std::size_t f=0;f<count;++f) pair.block[2*f+channel]+=std::isfinite(samples[f])?samples[f]:0.f;
    }
    void endBlock(const float* gain,std::size_t count) noexcept override {
        bool hasInput=false,hasOutput=false;
        for (const auto& pair:pairs) {
            if (pair.input) { hasInput=true; continue; }
            hasOutput=true;
            for (std::size_t f=0;f<count;++f) {
                auto l=pair.block[2*f]*gain[f],r=pair.block[2*f+1]*gain[f];
                if (pair.left==pair.right) channels[pair.left].format.write(buffers[pair.left].buffers[index],offset+f,(l+r)*.5f);
                else {
                    channels[pair.left].format.write(buffers[pair.left].buffers[index],offset+f,l);
                    channels[pair.right].format.write(buffers[pair.right].buffers[index],offset+f,r);
                }
            }
        }
        offset+=count;
        if (hasInput) captured.fetch_add(count,std::memory_order_relaxed);
        if (hasOutput) played.fetch_add(count,std::memory_order_relaxed);
    }
};
}
