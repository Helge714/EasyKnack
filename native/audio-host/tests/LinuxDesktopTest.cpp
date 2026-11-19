#include "../src/platform/LinuxAudio.hpp"
#include "../src/platform/LinuxMidi.hpp"
#include <sys/wait.h>
#include <signal.h>
#include <iostream>
#include <cerrno>
using namespace easyknack;
using namespace easyknack::platform;
static void check(bool value,const char* message){if(!value)throw std::runtime_error(message);}
static std::string hex(const std::string& text){std::string result;for(unsigned char c:text){result+="0123456789abcdef"[c>>4];result+="0123456789abcdef"[c&15];}return result;}
struct NullSink {
    std::shared_ptr<PulseSession> session;std::uint32_t module=PA_INVALID_INDEX;std::string name;
    NullSink(std::shared_ptr<PulseSession> session,const std::string& suffix):session(std::move(session)),name("easyknack_test_"+std::to_string(getpid())+suffix){
        PulseSession::Guard lock(*this->session);auto arguments="sink_name="+name+" rate=48000 channels=2";
        this->session->operation(this->session->api.pa_context_load_module(this->session->context,"module-null-sink",arguments.c_str(),[](pa_context*,std::uint32_t index,void* data){*static_cast<std::uint32_t*>(data)=index;},&module));
        check(module!=PA_INVALID_INDEX,"Cannot create temporary Pulse/PipeWire test sink");
    }
    ~NullSink(){if(module!=PA_INVALID_INDEX)try{PulseSession::Guard lock(*session);session->operation(session->api.pa_context_unload_module(session->context,module,nullptr,nullptr));}catch(...) {}}
};
struct Producer {
    pid_t pid=-1;
    Producer(const char* executable,const std::string& sink){pid=fork();if(pid==0){execl(executable,executable,"--producer",sink.c_str(),static_cast<char*>(nullptr));_exit(127);}check(pid>0,"Cannot start independent audio producer");}
    ~Producer(){if(pid>0){kill(pid,SIGTERM);while(waitpid(pid,nullptr,0)<0&&errno==EINTR){}}}
};
static void midiAcceptance(){
    SharedLibrary library;if(!library.open("libasound.so.2")){std::cout<<"SKIPPED ALSA sequencer: libasound unavailable\n";return;}
    auto open=library.symbol<decltype(&snd_seq_open)>("snd_seq_open");auto close=library.symbol<decltype(&snd_seq_close)>("snd_seq_close");
    auto name=library.symbol<decltype(&snd_seq_set_client_name)>("snd_seq_set_client_name");auto port=library.symbol<decltype(&snd_seq_create_simple_port)>("snd_seq_create_simple_port");auto send=library.symbol<decltype(&snd_seq_event_output_direct)>("snd_seq_event_output_direct");
    check(open&&close&&name&&port&&send,"Incomplete ALSA sequencer API");snd_seq_t* handle=nullptr;
    if(open(&handle,"default",SND_SEQ_OPEN_OUTPUT,0)<0){std::cout<<"SKIPPED ALSA sequencer: /dev/snd/seq unavailable (physical MIDI remains unverified)\n";return;}
    struct Cleanup {snd_seq_t* handle;decltype(close) release;~Cleanup(){release(handle);}} cleanup{handle,close};
    name(handle,"EasyKnack MIDI acceptance");int source=port(handle,"Test note",SND_SEQ_PORT_CAP_READ|SND_SEQ_PORT_CAP_SUBS_READ,SND_SEQ_PORT_TYPE_MIDI_GENERIC|SND_SEQ_PORT_TYPE_APPLICATION);check(source>=0,"Cannot create MIDI test source");
    auto midi=std::make_unique<LinuxMidi>();auto devices=midi->enumerate();auto found=std::find_if(devices.begin(),devices.end(),[](auto& d){return d.name.find("EasyKnack MIDI acceptance")!=std::string::npos;});check(found!=devices.end(),"Virtual MIDI source was not discovered");
    auto key=found->key;midi->prepare({key});midi->prepareDrivers({key},false);midi->setRunning(true);midi->beginBlock(256,48000);
    snd_seq_event_t event{};snd_seq_ev_clear(&event);snd_seq_ev_set_source(&event,source);snd_seq_ev_set_subs(&event);snd_seq_ev_set_direct(&event);snd_seq_ev_set_noteon(&event,0,60,100);check(send(handle,&event)>=0,"Cannot send test MIDI note");
    bool received=false;for(int i=0;i<100&&!received;i++){std::this_thread::sleep_for(std::chrono::milliseconds(5));midi->beginBlock(256,48000);auto* block=midi->events(key);for(std::size_t j=0;j<block->count;j++)received|=block->events[j].message==0x643c90;}
    check(received,"ALSA MIDI note did not reach the DSP event block");check(!midi->driverEvents().empty(),"MIDI driver/learn tap did not receive note");midi->setRunning(false);
    std::cout<<"PASS: real ALSA virtual-port discovery, subscription, note delivery and driver tap\n";
}
int main(int argc,char** argv){
    try{
        std::shared_ptr<PulseSession> session;
        try{session=std::make_shared<PulseSession>();}catch(const std::exception& error){std::cout<<"SKIPPED Linux desktop audio: "<<error.what()<<'\n';return 77;}
        if(argc==3&&std::string(argv[1])=="--producer"){
            auto engine=std::make_unique<AudioEngine>();LinuxAudio audio(*engine,session);auto endpoint="out:pulse:"+std::string(argv[2]);audio.prepare({{endpoint,false}});
            engine->publish(AudioGraph::parse("GRAPH 2 value value 0.25 @ c - out deviceout 0 "+hex(endpoint)+" @ a a"));engine->playing.store(true);std::this_thread::sleep_for(std::chrono::seconds(30));return 0;
        }
        NullSink first(session,"_a"),second(session,"_b");auto engine=std::make_unique<AudioEngine>();
        {
            LinuxAudio audio(*engine,session);auto output="out:pulse:"+second.name;
            auto self=std::filesystem::absolute(argv[0]).string();Producer producer(self.c_str(),first.name);
            std::string application;
            for(int i=0;i<100&&application.empty();i++){
                auto state=session->snapshot(true);auto sink=std::find_if(state.sinks.begin(),state.sinks.end(),[&](auto& s){return s.name==first.name;});
                if(sink!=state.sinks.end())for(auto& app:state.applications)if(app.sink==sink->index)application="app:pulse:"+app.identity;
                if(application.empty())std::this_thread::sleep_for(std::chrono::milliseconds(25));
            }
            check(!application.empty(),"Independent application's stream was not discovered");
            auto state=session->snapshot(true);auto sink=std::find_if(state.sinks.begin(),state.sinks.end(),[&](auto& s){return s.name==first.name;});check(sink!=state.sinks.end(),"Temporary sink disappeared");
            auto monitor="in:pulse:"+sink->monitor;audio.prepare({{application,true},{monitor,true},{output,false}});
            engine->publish(AudioGraph::parse("GRAPH 2 input input "+hex(application)+" copy stereo @ a - out deviceout 0 "+hex(output)+" @ a a"));engine->playing.store(true);
            float peak=0;for(int i=0;i<100&&peak<.005f;i++){std::this_thread::sleep_for(std::chrono::milliseconds(20));peak=audio.takePeak(output);}check(peak>.005f,"Per-application capture produced no signal");
            engine->publish(AudioGraph::parse("GRAPH 2 input input "+hex(monitor)+" copy stereo @ a - out deviceout 0 "+hex(output)+" @ a a"));audio.takePeak(output);
            peak=0;for(int i=0;i<100&&peak<.005f;i++){std::this_thread::sleep_for(std::chrono::milliseconds(20));peak=audio.takePeak(output);}check(peak>.005f,"Sink/system monitor capture produced no signal");
            audio.prepare({{"out:pulse:"+first.name,false},{output,false}});
            engine->publish(AudioGraph::parse("GRAPH 3 value value 0.1 @ c - a deviceout 0 "+hex("out:pulse:"+first.name)+" @ a a b deviceout 0 "+hex(output)+" @ a a"));
            std::this_thread::sleep_for(std::chrono::milliseconds(200));check(audio.takePeak(output)>.01f&&audio.takePeak("out:pulse:"+first.name)>.01f,"Multiple outputs were not rendered");
            check(session->volume(output,.3f),"Cannot set endpoint volume");check(std::abs(session->volume(output).scalar-.3f)<.001f,"Endpoint volume readback differs");
            engine->playing.store(false);std::cout<<"PASS: real server clock, multiple sinks, system/application capture and endpoint volume\n";
        }
        midiAcceptance();return 0;
    }catch(const std::exception& error){std::cerr<<"Linux desktop acceptance failed: "<<error.what()<<'\n';return 1;}
}
