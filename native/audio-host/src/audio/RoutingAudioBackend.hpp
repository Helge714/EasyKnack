#pragma once
#include "WindowsAudioBackend.hpp"
#include "AsioAudioBackend.hpp"

namespace easyknack {
/** Standard endpoints, not vendor integrations. External routing remains user controlled. */
class RoutingAudioBackend final : public AudioBackend {
    WindowsAudioBackend wasapi;
    AsioAudioBackend asio;
    bool running=false, usesAsio=false;
    std::uint64_t diagnosticGeneration=1;
    std::vector<AudioRoute> current;
    static bool same(const std::vector<AudioRoute>& a,const std::vector<AudioRoute>& b) {
        if (a.size()!=b.size()) return false;
        for (std::size_t i=0;i<a.size();++i)
            if (a[i].endpoint!=b[i].endpoint || a[i].input!=b[i].input || a[i].mode!=b[i].mode) return false;
        return true;
    }
    void configure(const std::vector<AudioRoute>& routes) {
        ++diagnosticGeneration;
        std::vector<AudioRoute> w,a;
        for (const auto& route:routes) (AsioAudioBackend::owns(route.endpoint)?a:w).push_back(route);
        asio.validate(a); // Invalid selections must not tear down the previous configuration.
        wasapi.setClockEnabled(false);
        asio.stop();
        wasapi.setRunning(false);
        asio.prepare(a);
        usesAsio=!a.empty();
        wasapi.prepare(w);
        current=routes;
        wasapi.setRunning(running);
        if (usesAsio) { if (running) asio.start(); }
        else wasapi.setClockEnabled(true);
    }
public:
    std::uint64_t generation()const{return diagnosticGeneration;}
    float takePeak(const std::string& id) noexcept { return wasapi.takePeak(id); }
    std::string clockDescription() const { return usesAsio?"ASIO":wasapi.clockDescription(); }
    std::string bufferDescription() const { return wasapi.bufferDescription(); }
    RoutingAudioBackend(AudioEngine& engine,bool nullDevice) : wasapi(engine,nullDevice),
        asio(engine,*this,nullDevice?AsioDrivers{[]{return std::vector<AsioDriverEntry>{};},[](const std::string&){return AsioHandle{};}}:windowsAsioDrivers()) {
        asio.discover();
    }
    ~RoutingAudioBackend() override { asio.stop(); wasapi.setClockEnabled(false); }
    AudioBackendCapabilities capabilities() const override { return wasapi.capabilities(); }
    std::vector<AudioEndpoint> enumerateInputs() override {
        auto result=wasapi.enumerateInputs(); asio.discover();
        auto extra=asio.endpoints(true); result.insert(result.end(),extra.begin(),extra.end()); return result;
    }
    std::vector<AudioEndpoint> enumerateOutputs() override {
        auto result=wasapi.enumerateOutputs(); auto extra=asio.endpoints(false);
        result.insert(result.end(),extra.begin(),extra.end()); return result;
    }
    void prepare(const std::vector<AudioRoute>& routes) override { if (!same(current,routes)) configure(routes); }
    void setRunning(bool value) override {
        if(value!=running)++diagnosticGeneration;
        asio.stop(); wasapi.setRunning(value); running=value;
        if (value && usesAsio) asio.start();
    }
    void refresh() override {
        if (!wasapi.needsRefresh() && (!usesAsio || (asio.ready() && !asio.needsReset()))) return;
        ++diagnosticGeneration;
        // Stop ASIO too: WASAPI refresh may replace ring buffers referenced by the DSP callback.
        asio.stop(); wasapi.refresh();
        if (usesAsio && (asio.needsReset() || !asio.ready())) { configure(current); return; }
        if (running && usesAsio) asio.start();
    }
    std::string status() const override {
        auto a=asio.status(),w=wasapi.status(); return a.empty()?w:w.empty()?a:w+"; "+a;
    }
    AudioIOStatistics statistics() const override {
        auto a=asio.statistics(),w=wasapi.statistics();
        AudioIOStatistics s{a.capturedFrames+w.capturedFrames,a.playedFrames+w.playedFrames,a.underruns+w.underruns,a.overruns+w.overruns};
        s.captureUnder=w.captureUnder;s.captureOver=w.captureOver;s.playbackUnder=w.playbackUnder;s.playbackOver=w.playbackOver;s.driverOverload=a.driverOverload;s.priming=w.priming;return s;
    }
    void beginBlock(std::size_t n) noexcept override { wasapi.beginBlock(n); asio.beginBlock(n); }
    float input(const std::string& id,std::size_t c,std::size_t f) const noexcept override {
        return AsioAudioBackend::owns(id)?asio.input(id,c,f):wasapi.input(id,c,f);
    }
    void output(const std::string& id,std::size_t c,const float* data,std::size_t n) noexcept override {
        if (AsioAudioBackend::owns(id)) asio.output(id,c,data,n); else wasapi.output(id,c,data,n);
    }
    void endBlock(const float* gain,std::size_t n) noexcept override { wasapi.endBlock(gain,n); asio.endBlock(gain,n); }
};
}
