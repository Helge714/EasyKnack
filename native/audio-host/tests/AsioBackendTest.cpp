#include "audio/AsioAudioBackend.hpp"
#include <iostream>
#include <limits>

using namespace easyknack;
static void require(bool value,const char* message) { if (!value) throw std::runtime_error(message); }
struct Fixture;
struct FakeDriver final : IASIO {
    Fixture& fixture;
    ASIOCallbacks* callbacks=nullptr;
    std::vector<ASIOBufferInfo> infos;
    std::vector<std::array<std::vector<float>,2>> memory;
    explicit FakeDriver(Fixture& f):fixture(f){}
    HRESULT STDMETHODCALLTYPE QueryInterface(REFIID,void**) override { return E_NOINTERFACE; }
    ULONG STDMETHODCALLTYPE AddRef() override { return 1; }
    ULONG STDMETHODCALLTYPE Release() override;
    ASIOBool init(void*) override { return ASIOTrue; }
    void getDriverName(char* p) override { std::strcpy(p,"Test"); }
    long getDriverVersion() override { return 1; }
    void getErrorMessage(char* p) override { p[0]=0; }
    ASIOError start() override;
    ASIOError stop() override;
    ASIOError getChannels(long* i,long* o) override { *i=3; *o=4; return ASE_OK; }
    ASIOError getLatencies(long* i,long* o) override { *i=513; *o=513; return ASE_OK; }
    ASIOError getBufferSize(long* lo,long* hi,long* preferred,long* granularity) override { *lo=1; *hi=8192; *preferred=513; *granularity=1; return ASE_OK; }
    ASIOError canSampleRate(ASIOSampleRate r) override { return r==48000?ASE_OK:ASE_NoClock; }
    ASIOError getSampleRate(ASIOSampleRate* r) override { *r=48000; return ASE_OK; }
    ASIOError setSampleRate(ASIOSampleRate r) override { return canSampleRate(r); }
    ASIOError getClockSources(ASIOClockSource*,long*) override { return ASE_NotPresent; }
    ASIOError setClockSource(long) override { return ASE_NotPresent; }
    ASIOError getSamplePosition(ASIOSamples*,ASIOTimeStamp*) override { return ASE_NotPresent; }
    ASIOError getChannelInfo(ASIOChannelInfo* i) override { i->type=ASIOSTFloat32LSB; std::strcpy(i->name,"Test channel"); return ASE_OK; }
    ASIOError createBuffers(ASIOBufferInfo* b,long channels,long frames,ASIOCallbacks* c) override;
    ASIOError disposeBuffers() override;
    ASIOError controlPanel() override { return ASE_OK; }
    ASIOError future(long,void*) override { return ASE_NotPresent; }
    ASIOError outputReady() override { return ASE_OK; }
    void pump(long index) {
        for (std::size_t c=0;c<infos.size();++c) if (infos[c].isInput)
            for (int f=0;f<513;++f) memory[c][index][f]=(infos[c].channelNum==0?1.f:-1.f)*(0.25f+float(f)/4096);
        callbacks->bufferSwitch(index,ASIOTrue);
    }
};
struct Fixture {
    FakeDriver* live=nullptr;
    int opened=0,released=0,starts=0,stops=0,creates=0,disposes=0;
    bool failOpen=false,failBuffers=false;
    AsioDrivers source() {
        return {[]{return std::vector<AsioDriverEntry>{{"{TEST}","Test device"},{"{OTHER}","Other device"}};},
            [this](const std::string&) -> AsioHandle {
                if (failOpen) return {};
                ++opened; live=new FakeDriver(*this); return AsioHandle(live);
            }};
    }
};
ULONG FakeDriver::Release() { ++fixture.released; if (fixture.live==this) fixture.live=nullptr; delete this; return 0; }
ASIOError FakeDriver::start() { ++fixture.starts; return ASE_OK; }
ASIOError FakeDriver::stop() { ++fixture.stops; return ASE_OK; }
ASIOError FakeDriver::createBuffers(ASIOBufferInfo* b,long n,long frames,ASIOCallbacks* c) {
    if (fixture.failBuffers) return ASE_NoMemory;
    ++fixture.creates; callbacks=c; memory.resize(n);
    for (long i=0;i<n;++i) for (int side=0;side<2;++side) { memory[i][side].resize(frames,999.f); b[i].buffers[side]=memory[i][side].data(); }
    infos.assign(b,b+n); return ASE_OK;
}
ASIOError FakeDriver::disposeBuffers() { ++fixture.disposes; memory.clear(); infos.clear(); return ASE_OK; }
struct ForwardIO : AudioIO {
    AsioAudioBackend* backend=nullptr;
    void beginBlock(std::size_t n) noexcept override { backend->beginBlock(n); }
    float input(const std::string& id,std::size_t c,std::size_t f) const noexcept override { return backend->input(id,c,f); }
    void output(const std::string& id,std::size_t c,const float* data,std::size_t n) noexcept override { backend->output(id,c,data,n); }
    void endBlock(const float* gain,std::size_t n) noexcept override { backend->endBlock(gain,n); }
};
static void sampleFormats() {
    for (auto type:{ASIOSTInt16LSB,ASIOSTInt24LSB,ASIOSTInt32LSB,ASIOSTFloat32LSB,ASIOSTFloat64LSB,
            ASIOSTInt16MSB,ASIOSTInt24MSB,ASIOSTInt32MSB,ASIOSTFloat32MSB,ASIOSTFloat64MSB,
            ASIOSTInt32LSB16,ASIOSTInt32LSB18,ASIOSTInt32LSB20,ASIOSTInt32LSB24,
            ASIOSTInt32MSB16,ASIOSTInt32MSB18,ASIOSTInt32MSB20,ASIOSTInt32MSB24}) {
        auto format=AsioSampleFormat::from(type);
        std::array<unsigned char,80> data{}; data.fill(0xAA);
        for (int i=0;i<5;++i) {
            float value=-1.f+i*.5f;
            format.write(data.data()+1,i,value);
            require(std::abs(format.read(data.data()+1,i)-value)<0.00004f,"PCM roundtrip");
        }
        require(data[0]==0xAA && data[1+5*format.bytes]==0xAA,"PCM buffer boundary");
        format.write(data.data()+1,0,std::numeric_limits<float>::quiet_NaN());
        require(format.read(data.data()+1,0)==0,"NaN must become silence");
    }
    unsigned char negative24[]={0,0,0x80};
    require(AsioSampleFormat::from(ASIOSTInt24LSB).read(negative24,0)==-1,"24 bit sign extension");
    unsigned char halfBig[]={0x40,0,0,0};
    require(AsioSampleFormat::from(ASIOSTInt32MSB).read(halfBig,0)==.5f,"big endian reference");
    double huge=1e200;
    require(AsioSampleFormat::from(ASIOSTFloat64LSB).read(&huge,0)==0,"float conversion overflow must become silence");
    bool rejected=false; try { AsioSampleFormat::from(ASIOSTDSDInt8LSB1); } catch (...) { rejected=true; }
    require(rejected,"DSD is not PCM");
}
static void adaptiveClocks() {
    for (int deviation:{-1,1}) {
    StereoRing<> ring;
    std::array<float,514> input{}; std::array<float,512> output{};
    for (unsigned i=0;i<257;++i) { input[2*i]=.75f; input[2*i+1]=-.25f; }
    ring.write(input.data(),256); ring.write(input.data(),256);
    for (int block=0;block<30000;++block) {
        // Producer differs by 0.1%; fixed-rate buffers eventually under/overflow.
        ring.write(input.data(),block%4==0?256+deviation:256);
        ring.readAdaptive(output.data(),256,512);
        require(std::abs(output[100]-.75f)<1e-6f && std::abs(output[101]+.25f)<1e-6f,"adaptive stereo continuity");
    }
    require(ring.overruns.load()==0 && ring.underruns.load()==0,"clock drift must not exhaust ring");
    require(ring.available()<2048,"bounded drift compensation");
    }
}
static void burstyClocks() {
    StereoRing<> ring;
    std::array<float,2048> input{};
    std::array<float,512> output{};
    for (unsigned i=0;i<1024;++i) { input[2*i]=.75f; input[2*i+1]=-.25f; }
    ring.write(input.data(),1024); ring.write(input.data(),1024);
    // Equal average clocks, but producer callbacks arrive in 21 ms batches.
    // Check samples too: silently re-priming must not hide dropouts.
    for (int block=0;block<30000;++block) {
        if (block%4==0) ring.write(input.data(),1024);
        ring.readAdaptive(output.data(),256);
        for (unsigned i=0;i<256;++i)
            require(std::abs(output[2*i]-.75f)<1e-6f && std::abs(output[2*i+1]+.25f)<1e-6f,
                    "bursty callback scheduling must preserve continuous stereo audio");
    }
    require(ring.underruns.load()==0 && ring.overruns.load()==0,"scheduler jitter must not exhaust ring");
}
int main() {
    try {
        sampleFormats(); adaptiveClocks(); burstyClocks();
        Fixture fixture; auto engine=std::make_unique<AudioEngine>(); ForwardIO io;
        {
            AsioAudioBackend backend(*engine,io,fixture.source()); io.backend=&backend;
            backend.discover(); require(backend.endpoints(true).size()==4,"stereo pairs and odd mono channel discovery");
            auto opened=fixture.opened; backend.discover(); require(opened==fixture.opened,"discovery cache");
            std::string input="asio:{TEST}:i:0",output="asio:{TEST}:o:2";
            std::vector<AudioRoute> routes{{input,true},{output,false},{input,true}};
            backend.prepare(routes); require(backend.ready(),"driver prepared");
            require(fixture.live->infos.size()==4,"duplicate routes share buffers");
            require(fixture.live->infos[0].channelNum==2,"selected output channel pair");
            engine->publish(AudioGraph::parse("GRAPH 2 src input "+hexText(input)+" copy stereo @ a - sink deviceout 0 "+hexText(output)+" @ a a"));
            engine->playing.store(true); backend.start();
            for (int i=0;i<30;++i) fixture.live->pump(i%2);
            for (std::size_t c=0;c<fixture.live->infos.size();++c) if (!fixture.live->infos[c].isInput)
                for (int f=0;f<513;++f) {
                    float expected=(fixture.live->infos[c].channelNum==2?1.f:-1.f)*(.25f+float(f)/4096)*.2f;
                    require(std::abs(fixture.live->memory[c][1][f]-expected)<1e-5f,"same callback duplex data, channel mapping and block offsets");
                }
            require(backend.statistics().capturedFrames==30*513,"frame statistics");
            fixture.live->callbacks->asioMessage(kAsioResetRequest,0,nullptr,nullptr);
            require(backend.needsReset(),"reset request deferred");
            fixture.live->pump(0);
            require(fixture.live->memory[0][0][0]==0,"silence while resetting");
            backend.prepare(routes); backend.start(); require(!backend.needsReset(),"rebuild clears reset");
            bool rejected=false; try { backend.validate({{input,true},{"asio:{OTHER}:o:0",false}}); } catch (...) { rejected=true; }
            require(rejected && backend.ready(),"multiple clock domains rejected before teardown");
            backend.stop(); backend.stop(); require(fixture.stops==2,"idempotent stop");
            fixture.failBuffers=true; backend.prepare(routes);
            require(!backend.ready() && !backend.status().empty(),"buffer creation failure reported");
            fixture.failBuffers=false; backend.prepare(routes); require(backend.ready(),"recover after failure");
            fixture.failOpen=true; backend.prepare(routes); require(!backend.ready(),"missing driver closes old stream");
            backend.prepare({}); require(backend.status().empty(),"empty graph clears diagnostic");
        }
        require(fixture.opened==fixture.released && fixture.creates==fixture.disposes,"driver and buffer lifetimes balanced");
        std::cout<<"ASIO backend and independent-clock tests passed\n";
    } catch (const std::exception& e) { std::cerr<<e.what()<<'\n'; return 1; }
}
