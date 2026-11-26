// Apple-only OS adapter, compiled separately from DSP node translation units.
#if defined(__APPLE__)
#define MA_NO_FLAC
#define MA_NO_ENCODING
#define MA_NO_RESOURCE_MANAGER
#define MA_NO_NODE_GRAPH
#define MA_NO_ENGINE
#define MA_NO_GENERATION
#define MINIAUDIO_IMPLEMENTATION
#include "miniaudio.h"
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <CoreAudio/CoreAudio.h>
#import <CoreAudio/AudioHardwareTapping.h>
#import <CoreAudio/CATapDescription.h>
#import <CoreMIDI/CoreMIDI.h>
#include <libproc.h>
#include <unistd.h>
#include <map>
#include "MacHost.hpp"
#include "DefaultAudio.hpp"
#include "KeyboardMidi.hpp"
#include "RoutedAudio.hpp"
#include "RoutedVolume.hpp"
#include "MidiPortBank.hpp"
#include "MidiByteDecoder.hpp"

namespace easyknack::platform {
namespace {
std::string text(CFStringRef value){if(!value)return {};char bytes[4096]{};if(CFStringGetCString(value,bytes,sizeof bytes,kCFStringEncodingUTF8))return bytes;return {};}
template<class T> bool property(AudioObjectID object,AudioObjectPropertySelector selector,T& value,AudioObjectPropertyScope scope=kAudioObjectPropertyScopeGlobal,AudioObjectPropertyElement element=kAudioObjectPropertyElementMain){AudioObjectPropertyAddress address{selector,scope,element};UInt32 size=sizeof value;return AudioObjectGetPropertyData(object,&address,0,nullptr,&size,&value)==noErr;}
template<class T> std::vector<T> list(AudioObjectID object,AudioObjectPropertySelector selector){AudioObjectPropertyAddress address{selector,kAudioObjectPropertyScopeGlobal,kAudioObjectPropertyElementMain};UInt32 size=0;if(AudioObjectGetPropertyDataSize(object,&address,0,nullptr,&size)!=noErr)return {};std::vector<T> result(size/sizeof(T));if(size&&AudioObjectGetPropertyData(object,&address,0,nullptr,&size,result.data())!=noErr)return {};result.resize(size/sizeof(T));return result;}
std::string stringProperty(AudioObjectID object,AudioObjectPropertySelector selector){CFStringRef value=nullptr;if(!property(object,selector,value))return {};auto result=text(value);if(value)CFRelease(value);return result;}
std::string encode(const std::string& value){std::string result;for(unsigned char c:value){result+="0123456789abcdef"[c>>4];result+="0123456789abcdef"[c&15];}return result;}
std::string decode(const std::string& value){std::string result;if(value.size()%2)return {};for(std::size_t i=0;i<value.size();i+=2){auto a=std::string("0123456789abcdef").find(value[i]),b=std::string("0123456789abcdef").find(value[i+1]);if(a>15||b>15)return {};result+=char(a*16+b);}return result;}
AudioObjectID defaultOutput(){AudioObjectID result=0;property(kAudioObjectSystemObject,kAudioHardwarePropertyDefaultOutputDevice,result);return result;}
AudioObjectID deviceForUID(const std::string& uid){for(auto id:list<AudioObjectID>(kAudioObjectSystemObject,kAudioHardwarePropertyDevices))if(stringProperty(id,kAudioDevicePropertyDeviceUID)==uid)return id;return 0;}
unsigned channelCount(AudioObjectID device,bool input){AudioObjectPropertyAddress a{kAudioDevicePropertyStreamConfiguration,input?kAudioObjectPropertyScopeInput:kAudioObjectPropertyScopeOutput,kAudioObjectPropertyElementMain};UInt32 size=0;if(AudioObjectGetPropertyDataSize(device,&a,0,nullptr,&size)!=noErr||size<sizeof(AudioBufferList))return 0;std::vector<unsigned char> data(size);if(AudioObjectGetPropertyData(device,&a,0,nullptr,&size,data.data())!=noErr)return 0;auto* buffers=reinterpret_cast<AudioBufferList*>(data.data());unsigned count=0;for(UInt32 i=0;i<buffers->mNumberBuffers;i++)count+=buffers->mBuffers[i].mNumberChannels;return count;}
void check(OSStatus status,const char* operation){if(status!=noErr)throw std::runtime_error(std::string(operation)+" (Core Audio status "+std::to_string(status)+")");}
void microphonePermission(){
    auto status=[AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeAudio];
    if(status==AVAuthorizationStatusNotDetermined){[AVCaptureDevice requestAccessForMediaType:AVMediaTypeAudio completionHandler:^(BOOL){}];throw std::runtime_error("Microphone permission pending; grant access in System Settings, then refresh audio devices");}
    if(status!=AVAuthorizationStatusAuthorized)throw std::runtime_error("Microphone permission denied; enable EasyKnack in System Settings > Privacy & Security > Microphone");
}

struct Process {AudioObjectID object;pid_t pid;std::string key,label;};
std::vector<Process> processes(){
    std::vector<Process> result;
    if(@available(macOS 14.2,*))for(auto id:list<AudioObjectID>(kAudioObjectSystemObject,kAudioHardwarePropertyProcessObjectList)){
        pid_t pid=0;if(!property(id,kAudioProcessPropertyPID,pid)||pid==getpid())continue;
        auto bundle=stringProperty(id,kAudioProcessPropertyBundleID);char path[PROC_PIDPATHINFO_MAXSIZE]{};proc_pidpath(pid,path,sizeof path);
        std::string identity=bundle.empty()?std::string(path):bundle;if(identity.empty())identity="pid:"+std::to_string(pid);
        char label[256]{};proc_name(pid,label,sizeof label);result.push_back({id,pid,encode(identity),label[0]?label:identity});
    }return result;
}

class MacStream final : public DeviceStream {
    ma_device device{};bool initialized=false,started=false;
    AudioObjectID tap=0,aggregate=0;
    DeviceDriver::Callback callback;void* context;
    std::atomic<std::uint64_t>& revision;
    static void data(ma_device* device,void* output,const void* input,ma_uint32 frames){auto& self=*static_cast<MacStream*>(device->pUserData);self.callback(self.context,static_cast<float*>(output),static_cast<const float*>(input),frames);}
    static void notification(const ma_device_notification* event){if(event->type==ma_device_notification_type_stopped||event->type==ma_device_notification_type_rerouted)static_cast<MacStream*>(event->pDevice->pUserData)->revision.fetch_add(1,std::memory_order_relaxed);}
    void close()noexcept{stop();if(initialized){ma_device_uninit(&device);initialized=false;}if(aggregate){AudioHardwareDestroyAggregateDevice(aggregate);aggregate=0;}if(tap){if(@available(macOS 14.2,*))AudioHardwareDestroyProcessTap(tap);tap=0;}}
public:
    MacStream(ma_context& audio,const DeviceEndpoint& endpoint,DeviceDriver::Callback callback,void* context,std::atomic<std::uint64_t>& revision):callback(callback),context(context),revision(revision){
        try{@autoreleasepool{
            std::string uid=endpoint.device;
            if(uid.rfind("hardware:",0)==0){uid.erase(0,9);if(endpoint.input)microphonePermission();}
            else if(uid=="system"||uid.rfind("app:",0)==0){
                if(@available(macOS 14.2,*)){
                    NSMutableArray<NSNumber*>* ids=[NSMutableArray array];
                    if(uid=="system"){
                        // Exclude all process objects belonging to this audio host.
                        for(auto id:list<AudioObjectID>(kAudioObjectSystemObject,kAudioHardwarePropertyProcessObjectList)){pid_t pid=0;if(property(id,kAudioProcessPropertyPID,pid)&&pid==getpid())[ids addObject:@(id)];}
                    }else for(auto& p:processes())if(p.key==uid.substr(4))[ids addObject:@(p.object)];
                    if(uid!="system"&&!ids.count)throw std::runtime_error("Application is no longer available for audio capture");
                    CATapDescription* description=uid=="system"?[[CATapDescription alloc] initStereoGlobalTapButExcludeProcesses:ids]:[[CATapDescription alloc] initStereoMixdownOfProcesses:ids];
                    description.name=@"EasyKnack audio capture";description.privateTap=YES;description.muteBehavior=CATapUnmuted;
                    check(AudioHardwareCreateProcessTap(description,&tap),"Create system/application audio tap; check Audio Capture permission");
                    NSString* aggregateUID=[[NSUUID UUID] UUIDString];
                    NSDictionary* config=@{@kAudioAggregateDeviceNameKey:@"EasyKnack private audio capture",@kAudioAggregateDeviceUIDKey:aggregateUID,@kAudioAggregateDeviceIsPrivateKey:@YES,@kAudioAggregateDeviceTapAutoStartKey:@YES,@kAudioAggregateDeviceTapListKey:@[@{@kAudioSubTapUIDKey:description.UUID.UUIDString,@kAudioSubTapDriftCompensationKey:@YES}]};
                    check(AudioHardwareCreateAggregateDevice((__bridge CFDictionaryRef)config,&aggregate),"Create private capture aggregate");uid=aggregateUID.UTF8String;
                }else throw std::runtime_error("System/application capture requires macOS 14.2 or newer");
            }else throw std::runtime_error("Unknown Core Audio endpoint token");
            ma_device_id id{};if(uid.size()>=sizeof id.coreaudio)throw std::runtime_error("Core Audio device UID too long");std::copy(uid.begin(),uid.end(),id.coreaudio);
            auto config=ma_device_config_init(endpoint.input?ma_device_type_capture:ma_device_type_playback);config.sampleRate=sampleRate;config.periodSizeInFrames=blockSize;config.dataCallback=data;config.pUserData=this;
            config.playback.channelMixMode=ma_channel_mix_mode_simple;config.capture.channelMixMode=ma_channel_mix_mode_simple;
            config.notificationCallback=notification;
            if(endpoint.input){config.capture.pDeviceID=&id;config.capture.format=ma_format_f32;config.capture.channels=endpoint.channels;}
            else{config.playback.pDeviceID=&id;config.playback.format=ma_format_f32;config.playback.channels=endpoint.channels;}
            auto status=ma_device_init(&audio,&config,&device);if(status!=MA_SUCCESS)throw std::runtime_error(std::string("Core Audio device: ")+ma_result_description(status));initialized=true;
        }}catch(...){close();throw;}
    }
    ~MacStream()override{close();}
    void start()override{if(started)return;auto result=ma_device_start(&device);if(result!=MA_SUCCESS)throw std::runtime_error(std::string("Core Audio start; check Microphone/Audio Capture permission: ")+ma_result_description(result));started=true;}
    void stop()noexcept override{if(started){ma_device_stop(&device);started=false;}}
    bool healthy()const noexcept override{return initialized&&ma_device_is_started(&device);}
};

class MacDriver final : public DeviceDriver {
    ma_context audio{};bool ready=false;
    std::atomic<std::uint64_t> revision{1};
    static OSStatus changed(AudioObjectID,UInt32,const AudioObjectPropertyAddress*,void* context){static_cast<MacDriver*>(context)->revision.fetch_add(1,std::memory_order_relaxed);return noErr;}
    void listeners(bool add){for(auto selector:std::array<AudioObjectPropertySelector,3>{kAudioHardwarePropertyDevices,kAudioHardwarePropertyDefaultInputDevice,kAudioHardwarePropertyDefaultOutputDevice}){AudioObjectPropertyAddress a{selector,kAudioObjectPropertyScopeGlobal,kAudioObjectPropertyElementMain};if(add)AudioObjectAddPropertyListener(kAudioObjectSystemObject,&a,changed,this);else AudioObjectRemovePropertyListener(kAudioObjectSystemObject,&a,changed,this);}}
    static std::string uidFromKey(const std::string& id){if(id.rfind("out:coreaudio:",0)!=0)return {};auto text=id.substr(14);auto suffix=text.find(":channels:");return decode(text.substr(0,suffix));}
    static DeviceVolumeService::Value readVolume(AudioObjectID device){
        if(!device)return {};Float32 scalar=0;UInt32 mute=0;property(device,kAudioDevicePropertyMute,mute,kAudioObjectPropertyScopeOutput);
        if(property(device,kAudioDevicePropertyVolumeScalar,scalar,kAudioObjectPropertyScopeOutput))return {true,std::clamp(scalar,0.f,1.f),mute!=0};
        float sum=0;unsigned count=0;for(unsigned c=1;c<=channelCount(device,false);c++)if(property(device,kAudioDevicePropertyVolumeScalar,scalar,kAudioObjectPropertyScopeOutput,c)){sum+=scalar;++count;}
        return count?DeviceVolumeService::Value{true,std::clamp(sum/count,0.f,1.f),mute!=0}:DeviceVolumeService::Value{};
    }
public:
    MacDriver(){ma_backend backend=ma_backend_coreaudio;auto status=ma_context_init(&backend,1,nullptr,&audio);if(status!=MA_SUCCESS)throw std::runtime_error(std::string("Core Audio context: ")+ma_result_description(status));ready=true;listeners(true);}
    ~MacDriver()override{listeners(false);if(ready)ma_context_uninit(&audio);}
    std::uint64_t generation()const noexcept override{return revision.load(std::memory_order_relaxed);}
    std::string name()const override{return "Core Audio / independent device clocks";}
    AudioBackendCapabilities capabilities()const override{if(@available(macOS 14.2,*))return {true,true,false,false,false};return {};}
    std::vector<DeviceEndpoint> endpoints()override{
        std::vector<DeviceEndpoint> result;AudioObjectID defaults[2]{defaultOutput(),0};property(kAudioObjectSystemObject,kAudioHardwarePropertyDefaultInputDevice,defaults[1]);
        for(auto device:list<AudioObjectID>(kAudioObjectSystemObject,kAudioHardwarePropertyDevices)){
            auto uid=stringProperty(device,kAudioDevicePropertyDeviceUID),label=stringProperty(device,kAudioObjectPropertyName);if(uid.empty()||label=="EasyKnack private audio capture")continue;
            UInt32 alive=0;property(device,kAudioDevicePropertyDeviceIsAlive,alive);
            for(unsigned input=0;input<2;input++){
                auto count=channelCount(device,input);if(!count||count>64)continue;
                auto base=std::string(input?"in:":"out:")+"coreaudio:"+encode(uid);auto type=input?AudioEndpointType::HardwareInput:AudioEndpointType::HardwareOutput;
                if(device==defaults[input])result.push_back({{input?"in:default":"default","System Default",type,alive!=0,false},"hardware:"+uid,input!=0,count,0});
                for(unsigned first=0;first<count;first+=2){auto id=base+(first?":channels:"+std::to_string(first):"");auto name=label+(count>2?" ["+std::to_string(first+1)+(first+1<count?"/"+std::to_string(first+2):"")+"]":"");result.push_back({{id,name,type,alive!=0,false},"hardware:"+uid,input!=0,count,first});}
            }
        }
        if(@available(macOS 14.2,*)){
            result.push_back({{"system","System Audio",AudioEndpointType::SystemMix,true,false},"system",true,2,0});
            std::set<std::string> seen;for(auto& p:processes())if(seen.insert(p.key).second)result.push_back({{"app:coreaudio:"+p.key,p.label,AudioEndpointType::Application,true,false},"app:"+p.key,true,2,0});
        }return result;
    }
    std::unique_ptr<DeviceStream> open(const DeviceEndpoint& endpoint,Callback callback,void* context)override{return std::make_unique<MacStream>(audio,endpoint,callback,context,revision);}
    std::string volumeKey(const std::string& id)override{auto uid=id=="default"?stringProperty(defaultOutput(),kAudioDevicePropertyDeviceUID):uidFromKey(id);return uid.empty()?"":"out:coreaudio:"+encode(uid);}
    DeviceVolumeService::Value volume(const std::string& id)override{return readVolume(deviceForUID(uidFromKey(id)));}
    bool volume(const std::string& id,float scalar)override{
        auto device=deviceForUID(uidFromKey(id));if(!device)return false;Float32 value=std::clamp(scalar,0.f,1.f);
        auto set=[&](AudioObjectPropertySelector selector,AudioObjectPropertyElement element,const void* data,UInt32 size){AudioObjectPropertyAddress a{selector,kAudioObjectPropertyScopeOutput,element};Boolean writable=false;return AudioObjectIsPropertySettable(device,&a,&writable)==noErr&&writable&&AudioObjectSetPropertyData(device,&a,0,nullptr,size,data)==noErr;};
        bool result=set(kAudioDevicePropertyVolumeScalar,kAudioObjectPropertyElementMain,&value,sizeof value);
        if(!result)for(unsigned c=1;c<=channelCount(device,false);c++)result=set(kAudioDevicePropertyVolumeScalar,c,&value,sizeof value)||result;
        if(result){UInt32 mute=0;set(kAudioDevicePropertyMute,kAudioObjectPropertyElementMain,&mute,sizeof mute);}return result;
    }
};

class MacMidi final : public MidiPortBank {
    MIDIClientRef client=0;MIDIPortRef input=0;
    struct Source {Device device;MIDIEndpointRef endpoint;};
    std::map<std::uint64_t,MIDIEndpointRef> connected;
    std::array<MidiByteDecoder,64> decoders;
    std::array<std::uint64_t,64> decoderGeneration{};
    std::string problem;
    std::atomic<bool> dirty{false};
    static void changed(const MIDINotification*,void* context){static_cast<MacMidi*>(context)->dirty.store(true,std::memory_order_relaxed);}
    static void receive(const MIDIPacketList* packets,void* context,void* source){
        auto& self=*static_cast<MacMidi*>(context);auto* port=static_cast<Port*>(source);if(!port||!port->enabled.load(std::memory_order_acquire))return;
        auto index=std::size_t(port-self.ports.data());if(index>=self.ports.size())return;
        auto generation=port->generation.load();if(self.decoderGeneration[index]!=generation){self.decoders[index]=MidiByteDecoder{};self.decoderGeneration[index]=generation;}
        auto* packet=&packets->packet[0];for(UInt32 i=0;i<packets->numPackets;i++){self.decoders[index].read(packet->data,packet->length,[&](std::uint32_t message){self.deliver(*port,message);});packet=MIDIPacketNext(packet);}
    }
    std::vector<Source> discover()const{
        std::vector<Source> result;if(!client)return result;
        for(ItemCount i=0;i<MIDIGetNumberOfSources();i++){
            auto endpoint=MIDIGetSource(i);SInt32 uid=0;if(!endpoint||MIDIObjectGetIntegerProperty(endpoint,kMIDIPropertyUniqueID,&uid)!=noErr)continue;
            CFStringRef name=nullptr;MIDIObjectGetStringProperty(endpoint,kMIDIPropertyDisplayName,&name);auto label=text(name);if(name)CFRelease(name);
            result.push_back({{identity("coremidi:"+std::to_string(uid)),label.empty()?"MIDI "+std::to_string(uid):label},endpoint});
        }return result;
    }
public:
    explicit MacMidi(bool hardware){if(!hardware)return;if(MIDIClientCreate(CFSTR("EasyKnack"),changed,this,&client)!=noErr){problem="CoreMIDI client unavailable";return;}if(MIDIInputPortCreate(client,CFSTR("EasyKnack Input"),receive,this,&input)!=noErr){problem="CoreMIDI input port unavailable";MIDIClientDispose(client);client=0;}}
    ~MacMidi()override{for(auto& p:ports)p.enabled.store(false,std::memory_order_release);if(input)MIDIPortDispose(input);if(client)MIDIClientDispose(client);}
    std::vector<Device> enumerate()const override{std::vector<Device> result;for(auto& source:discover())result.push_back(source.device);return result;}
    void refresh()override{
        if(!input)return;problem.clear();auto sources=discover();std::set<std::uint64_t> requested(wanted.begin(),wanted.end());requested.insert(driverWanted.begin(),driverWanted.end());if(learning)for(auto& source:sources)requested.insert(source.device.key);
        for(auto it=connected.begin();it!=connected.end();){auto source=std::find_if(sources.begin(),sources.end(),[&](auto& d){return d.device.key==it->first&&d.endpoint==it->second;});auto* p=slot(it->first);
            if(!requested.count(it->first)||source==sources.end()){if(p){p->enabled.store(false);p->queue.panic();p->driverQueue.panic();p->generation.fetch_add(1);}MIDIPortDisconnectSource(input,it->second);it=connected.erase(it);}else{if(p)p->driver.store(wantsDriver(it->first));++it;}}
        for(auto key:requested){if(key<=1||connected.count(key))continue;auto found=std::find_if(sources.begin(),sources.end(),[&](auto& s){return s.device.key==key;});if(found==sources.end()){problem="MIDI device disconnected";continue;}auto* p=slot(key);if(!p){problem="Maximum 64 MIDI devices";continue;}
            p->queue.panic();p->generation.fetch_add(1);p->driver.store(wantsDriver(key));p->enabled.store(true,std::memory_order_release);
            if(MIDIPortConnectSource(input,found->endpoint,p)!=noErr){p->enabled.store(false);problem="Cannot connect MIDI source: "+found->device.name;continue;}connected.emplace(key,found->endpoint);
        }
    }
    std::string status()const override{return problem;}
    void poll()override{
        // MIDIClientCreate delivers setup notifications on its creating run loop.
        // The host runs a socket control loop rather than NSApplicationMain.
        if(client)for(unsigned i=0;i<8;i++)if(CFRunLoopRunInMode(kCFRunLoopDefaultMode,0,true)!=kCFRunLoopRunHandledSource)break;
        if(dirty.exchange(false,std::memory_order_relaxed))refresh();
    }
};
}
HostServices createMacHostServices(AudioEngine& engine,bool nullDevice){
    HostServices result;result.midi=createMacMidiSource(!nullDevice);engine.midiSource=result.midi.get();
    if(nullDevice){result.volume=std::make_unique<UnavailableVolume>();result.audio=std::make_unique<DefaultAudio>(engine,true);}
    else{auto driver=std::make_shared<MacDriver>();result.volume=std::make_unique<RoutedVolume>(driver);result.audio=std::make_unique<RoutedAudio>(engine,driver);}return result;
}
std::unique_ptr<HostMidiSource> createMacMidiSource(bool hardware){return std::make_unique<MacMidi>(hardware);}
}
#endif
