#include "FakePulse.hpp"
#include "../src/platform/LinuxAudio.hpp"
#include "../src/platform/LinuxVolume.hpp"
#include <iostream>
using namespace easyknack;
using namespace easyknack::platform;
static void require(bool value,int line){if(!value)throw std::runtime_error("Linux adapter assertion failed at "+std::to_string(line));}
#define CHECK(value) require((value),__LINE__)
static std::string hex(const std::string& text){std::string result;for(unsigned char c:text){result+="0123456789abcdef"[c>>4];result+="0123456789abcdef"[c&15];}return result;}
int main(){
    FakePulse server;
    auto session=std::make_shared<PulseSession>([&](PulseApi& api){server.install(api);});
    auto engine=std::make_unique<AudioEngine>();
    {
        LinuxAudio audio(*engine,session);
        CHECK(audio.enumerateOutputs().size()==3);
        auto inputs=audio.enumerateInputs();CHECK(std::count_if(inputs.begin(),inputs.end(),[](auto& p){return p.type==AudioEndpointType::Application;})==1);
        CHECK(audio.capabilities().systemCapture&&audio.capabilities().applicationCapture);
        audio.prepare({{"out:pulse:first",false},{"out:pulse:second",false},{"app:pulse:player",true},{"system",true},{"in:default",true}});
        CHECK(server.streams.size()==5);
        CHECK(std::count_if(server.streams.begin(),server.streams.end(),[](auto* s){return s->monitor==42;})==1);
        auto previous=audio.generation();bool rejected=false;
        try{audio.prepare({{"system",true,AudioCaptureMode::Route}});}catch(const std::exception&){rejected=true;}CHECK(rejected&&previous==audio.generation());
        engine->publish(AudioGraph::parse("GRAPH 4 a value 0.25 @ c - b value 0.5 @ c - x deviceout 0 "+hex("out:pulse:first")+" @ a a y deviceout 1 "+hex("out:pulse:second")+" @ a a"));
        engine->playing.store(true);for(int i=0;i<40;i++)server.tick();
        CHECK(std::abs(server.last("first")-.05f)<.001f);CHECK(std::abs(server.last("second")-.1f)<.001f);
        CHECK(audio.takePeak("out:pulse:first")>.04f&&audio.takePeak("out:pulse:second")>.09f);
        for(int i=0;i<12;i++){server.capture(.2f);server.tick();}
        CHECK(audio.statistics().capturedFrames>0&&audio.statistics().playedFrames>0);
        engine->publish(AudioGraph::parse("GRAPH 2 in input "+hex("app:pulse:player")+" copy stereo @ a - out deviceout 0 "+hex("out:pulse:first")+" @ a a"));
        for(int i=0;i<24;i++){server.capture(.2f);server.tick();}
        CHECK(std::abs(server.last("first")-.04f)<.002f);
        server.failingDevice="microphone";audio.prepare({{"in:default",true},{"out:pulse:first",false}});
        CHECK(server.streams.size()==1&&!audio.status().empty());
        server.failingDevice.clear();audio.refresh();CHECK(server.streams.size()==2&&audio.status().empty());
        // Alias selections share a physical stream and do not duplicate routing.
        audio.prepare({{"default",false},{"out:pulse:first",false}});CHECK(server.streams.size()==1);
        LinuxVolume volume(session);volume.links({"out:pulse:first","out:pulse:second"});
        server.levels["first"]=.6f;session->snapshot(true);volume.poll();
        CHECK(std::abs(server.levels["second"]-.3f)<.0001f);CHECK(volume.keysActive());CHECK(volume.keyEvent().rfind("1 ",0)==0);
        CHECK(volume.set("out:pulse:second",.4f));CHECK(std::abs(volume.get("out:pulse:second").scalar-.4f)<.0001f);
        audio.prepare({{"out:pulse:missing",false}});CHECK(!audio.status().empty());CHECK(server.streams.size()==1);
    }
    CHECK(server.streams.empty()&&server.operations==0);
    std::cout<<"Linux audio routing, process monitoring, volume, clock and lifecycle tests passed\n";
}
