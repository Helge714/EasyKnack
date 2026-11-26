#include "../src/platform/RoutedAudio.hpp"
#include "../src/platform/RoutedVolume.hpp"
#include "../src/platform/MidiByteDecoder.hpp"
#include <iostream>
using namespace easyknack;
using namespace easyknack::platform;
static void require(bool value,int line){if(!value)throw std::runtime_error("Routed audio assertion at "+std::to_string(line));}
#define CHECK(value) require((value),__LINE__)
struct FakeDriver final : DeviceDriver {
    struct Stream final : DeviceStream {
        FakeDriver& owner;DeviceEndpoint endpoint;Callback callback;void* context;bool active=false;
        std::vector<float> output;
        Stream(FakeDriver& owner,const DeviceEndpoint& endpoint,Callback callback,void* context):owner(owner),endpoint(endpoint),callback(callback),context(context){owner.streams.push_back(this);}
        ~Stream()override{stop();owner.streams.erase(std::remove(owner.streams.begin(),owner.streams.end(),this),owner.streams.end());}
        void start()override{active=true;}
        void stop()noexcept override{active=false;}
    };
    std::vector<Stream*> streams;
    std::vector<DeviceEndpoint> devices{
        {{"default","Default",AudioEndpointType::HardwareOutput},"first",false,4,0},
        {{"out:first","First 1/2",AudioEndpointType::HardwareOutput},"first",false,4,0},
        {{"out:pair","First 3/4",AudioEndpointType::HardwareOutput},"first",false,4,2},
        {{"out:mono","Mono",AudioEndpointType::HardwareOutput},"mono",false,1,0},
        {{"in:pair","Capture 3/4",AudioEndpointType::HardwareInput},"input",true,4,2}};
    std::map<std::string,float> levels{{"first",.5f},{"mono",.25f}};
    std::string failing;
    std::uint64_t revision=1;
    std::uint64_t generation()const noexcept override{return revision;}
    std::vector<DeviceEndpoint> endpoints()override{return devices;}
    std::unique_ptr<DeviceStream> open(const DeviceEndpoint& e,Callback c,void* p)override{if(e.endpoint.id==failing)throw std::runtime_error("Simulated unavailable input");return std::make_unique<Stream>(*this,e,c,p);}
    AudioBackendCapabilities capabilities()const override{return {};}
    std::string name()const override{return "Fake hardware driver";}
    std::string volumeKey(const std::string& id)override{return id=="default"||id=="out:first"||id=="out:pair"?"first":id=="out:mono"?"mono":levels.count(id)?id:"";}
    DeviceVolumeService::Value volume(const std::string& id)override{auto i=levels.find(id);return i==levels.end()?DeviceVolumeService::Value{}:DeviceVolumeService::Value{true,i->second,false};}
    bool volume(const std::string& id,float value)override{if(!levels.count(id))return false;levels[id]=value;return true;}
    void tick(){for(bool input:{true,false})for(auto* stream:streams)if(stream->active&&stream->endpoint.input==input){std::vector<float> data(256*stream->endpoint.channels);if(input)for(unsigned i=0;i<256;i++){data[i*4+2]=.25f;data[i*4+3]=.5f;}stream->callback(stream->context,input?nullptr:data.data(),input?data.data():nullptr,256);if(!input)stream->output=std::move(data);}}
};
static std::string hex(const std::string& text){std::string result;for(unsigned char c:text){result+="0123456789abcdef"[c>>4];result+="0123456789abcdef"[c&15];}return result;}
int main(){
    MidiByteDecoder decoder;std::vector<std::uint32_t> messages;
    const std::uint8_t bytes[]{0x90,60,0xf8,100,61,90,0xc0,4,5,0xf0,1,2,0xf8,0xf7,64,90,0x80,60,0};
    decoder.read(bytes,sizeof bytes,[&](auto value){messages.push_back(value);});
    CHECK((messages==std::vector<std::uint32_t>{0xf8,0x643c90,0x5a3d90,0x4c0,0x5c0,0xf8,0x3c80}));
    auto driver=std::make_shared<FakeDriver>();auto engine=std::make_unique<AudioEngine>();
    {
        RoutedAudio audio(*engine,driver);CHECK(driver->streams.size()==1);
        audio.prepare({{"default",false},{"out:first",false},{"out:pair",false},{"out:mono",false}});CHECK(driver->streams.size()==3);
        auto generation=audio.generation();audio.prepare({{"default",false},{"out:first",false},{"out:pair",false},{"out:mono",false}});CHECK(audio.generation()==generation);
        bool rejected=false;try{audio.prepare({{"default",false,AudioCaptureMode::Route}});}catch(...){rejected=true;}CHECK(rejected&&audio.generation()==generation);
        engine->publish(AudioGraph::parse("GRAPH 4 a value 0.25 @ c - x deviceout 0 "+hex("default")+" @ a a y deviceout 0 "+hex("out:pair")+" @ a a z deviceout 0 "+hex("out:mono")+" @ a a"));engine->playing.store(true);
        for(int i=0;i<40;i++)driver->tick();
        for(auto* stream:driver->streams){CHECK(!stream->output.empty());if(stream->endpoint.channels==4){auto first=stream->endpoint.first;CHECK(std::abs(stream->output[first]-.05f)<.001f);CHECK(stream->output[(first+2)%4]==0);}else CHECK(std::abs(stream->output[0]-.05f)<.001f);}
        audio.beginPeakRead();CHECK(audio.takePeak("default")>0);CHECK(audio.takePeak("default")==audio.takePeak("out:first"));audio.endPeakRead();
        driver->failing="in:pair";audio.prepare({{"in:pair",true},{"default",false}});CHECK(!audio.status().empty());driver->failing.clear();audio.prepare({{"in:pair",true},{"default",false}});CHECK(audio.status().empty());
        engine->publish(AudioGraph::parse("GRAPH 2 in input "+hex("in:pair")+" copy stereo @ a - out deviceout 0 "+hex("default")+" @ a a"));
        for(int i=0;i<45;i++)driver->tick();
        auto out=std::find_if(driver->streams.begin(),driver->streams.end(),[](auto* s){return !s->endpoint.input;});CHECK(out!=driver->streams.end());CHECK(std::abs((*out)->output[0]-.05f)<.002f);CHECK(std::abs((*out)->output[1]-.1f)<.002f);
        RoutedVolume volume(driver);volume.links({"out:mono"});volume.poll();driver->levels["first"]=.6f;volume.poll();CHECK(std::abs(driver->levels["mono"]-.35f)<.0001f);CHECK(std::abs(driver->levels["first"]-.5f)<.0001f);CHECK(volume.keysActive());
        generation=audio.generation();++driver->revision;audio.poll();CHECK(audio.generation()==generation);
        driver->devices[0].device="new-default";++driver->revision;audio.poll();CHECK(audio.generation()>generation);
        CHECK(std::any_of(driver->streams.begin(),driver->streams.end(),[](auto* stream){return stream->endpoint.device=="new-default";}));
    }CHECK(driver->streams.empty());std::cout<<"Routed audio, channel maps, recovery, volume, teardown and MIDI decoding passed\n";
}
