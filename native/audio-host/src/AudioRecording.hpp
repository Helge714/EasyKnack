#pragma once
#include "Analysis.hpp"
#include "platform/Mp3Recording.hpp"
#include <thread>
#include <chrono>
#include <filesystem>
#include <cstdio>
#include <cerrno>
#include <mutex>
#include <map>
#include <limits>
#include <fcntl.h>
#ifdef _WIN32
#include <io.h>
#include <sys/stat.h>
#else
#include <unistd.h>
#endif

namespace easyknack {
/** Single audio producer, single disk consumer. Storage is allocated before publication. */
class AudioRecording {
    struct Packet { unsigned kind=0; std::size_t samples=0;std::vector<float> data; };
    static constexpr unsigned capacity=256;
    std::array<Packet,capacity> queue;
    std::atomic<unsigned> read{0},write{0};
    std::atomic<bool> closing{false};
    std::atomic<std::uint64_t> stopEpoch{0};
    std::atomic<unsigned> failure{0};
    std::thread worker;
    std::string target,identity;
    bool overwrite; unsigned digits,channels,sampleRate;
    std::shared_ptr<AnalysisMailbox> mailbox;
    FILE* file=nullptr;
    std::unique_ptr<platform::Mp3Recording> mp3;
    std::uint64_t frames=0,pendingIndex=0;
    std::filesystem::path pendingRename;
    unsigned state=0,error=0;
    std::string actualPath;
    static auto& registry(){static std::vector<std::weak_ptr<AudioRecording>> value;return value;}
    static auto& registryMutex(){static std::mutex value;return value;}
    static auto& fileMutex(){static std::mutex value;return value;}
    static auto& activeFiles(){static std::map<std::string,AudioRecording*> value;return value;}
    static std::string lower(std::string value){for(auto& c:value)c=static_cast<char>(std::tolower(static_cast<unsigned char>(c)));return value;}
    std::filesystem::path indexed(const std::filesystem::path& base,std::uint64_t number)const{
        auto suffix=std::to_string(number);if(suffix.size()<digits)suffix.insert(0,digits-suffix.size(),'0');
        return base.parent_path()/std::filesystem::u8path(base.stem().u8string()+"_"+suffix+base.extension().u8string());
    }
    static std::uint64_t next(std::uint64_t& number){
        if(number==std::numeric_limits<std::uint64_t>::max())throw std::runtime_error("Recording index exhausted");
        return ++number;
    }
    // Atomic no-replace moves: never overwrite an existing numbered take.
    static bool moveAside(const std::filesystem::path& from,const std::filesystem::path& to){
#ifdef _WIN32
        if(MoveFileExW(from.c_str(),to.c_str(),0))return true;
        auto error=GetLastError();if(error==ERROR_ALREADY_EXISTS||error==ERROR_FILE_EXISTS)return false;
        throw std::filesystem::filesystem_error("Cannot number recording",from,to,std::error_code(error,std::system_category()));
#else
        if(::link(from.c_str(),to.c_str())){if(errno==EEXIST)return false;throw std::system_error(errno,std::generic_category(),"Cannot number recording");}
        if(::unlink(from.c_str())){int error=errno;::unlink(to.c_str());throw std::system_error(error,std::generic_category(),"Cannot move recording");}
        return true;
#endif
    }
    std::filesystem::path chooseTarget(const std::filesystem::path& base){
        auto parent=base.parent_path().empty()?std::filesystem::path("."):base.parent_path();
        auto prefix=base.stem().u8string()+"_";auto extension=lower(base.extension().u8string());
        std::uint64_t maximum=0;
        for(const auto& entry:std::filesystem::directory_iterator(parent)){
            auto path=entry.path();auto stem=path.stem().u8string();
            if(lower(path.extension().u8string())!=extension||stem.compare(0,prefix.size(),prefix)!=0)continue;
            auto suffix=stem.substr(prefix.size());if(suffix.empty()||!std::all_of(suffix.begin(),suffix.end(),[](unsigned char c){return c>='0'&&c<='9';}))continue;
            try{maximum=std::max(maximum,static_cast<std::uint64_t>(std::stoull(suffix)));}catch(const std::out_of_range&){}
        }
        if(std::filesystem::exists(base)){
            if(!std::filesystem::is_regular_file(base))throw std::runtime_error("Recording target is not a file");
            auto active=activeFiles().find(base.u8string());
            if(active!=activeFiles().end()){
                auto previous=active->second;
                if(previous->pendingRename.empty()){
                    previous->pendingIndex=next(maximum);previous->pendingRename=indexed(base,maximum);
                }
                maximum=std::max(maximum,previous->pendingIndex);
            }else{
                while(!moveAside(base,indexed(base,next(maximum)))){}
            }
        }
        return maximum?indexed(base,next(maximum)):base;
    }
    static void word(FILE* f,std::uint32_t n,unsigned bytes){for(unsigned i=0;i<bytes;i++)std::fputc((n>>(8*i))&255,f);}
    void header(){
        std::rewind(file);std::fwrite("RIFF",1,4,file);word(file,36+static_cast<std::uint32_t>(frames*channels*4),4);
        std::fwrite("WAVEfmt ",1,8,file);word(file,16,4);word(file,3,2);word(file,channels,2);word(file,sampleRate,4);
        word(file,sampleRate*channels*4,4);word(file,channels*4,2);word(file,32,2);std::fwrite("data",1,4,file);word(file,static_cast<std::uint32_t>(frames*channels*4),4);
    }
    bool writing()const noexcept{return file||bool(mp3);}
    void finish(){
        bool completed=writing();
        if(mp3){if(!mp3->finish())error=3;mp3.reset();state=error?3:2;}
        if(file){header();if(std::ferror(file)||std::fflush(file))error=3;if(std::fclose(file))error=3;file=nullptr;state=error?3:2;}
        if(completed){
            std::lock_guard<std::mutex> lock(fileMutex());
            auto active=activeFiles().find(actualPath);if(active!=activeFiles().end()&&active->second==this)activeFiles().erase(active);
            if(!pendingRename.empty()){
                try{if(!moveAside(std::filesystem::u8path(actualPath),pendingRename))throw std::runtime_error("Numbered recording already exists");actualPath=pendingRename.u8string();}
                catch(...){error=1;state=3;failure.store(error,std::memory_order_release);}
                pendingRename.clear();pendingIndex=0;
            }
        }
    }
    void begin(){
        finish();frames=0;error=0;state=1;
        try{
            if(target.empty())throw std::runtime_error("No recording path");
            auto base=std::filesystem::absolute(std::filesystem::u8path(target)).lexically_normal();
            auto extension=base.extension().u8string();std::transform(extension.begin(),extension.end(),extension.begin(),[](unsigned char c){return static_cast<char>(std::tolower(c));});bool compressed=extension==".mp3";
            if(!base.parent_path().empty())std::filesystem::create_directories(base.parent_path());
            std::lock_guard<std::mutex> lock(fileMutex());
            for(;;){
                auto path=overwrite?base:chooseTarget(base);
                if(compressed){
                    auto encoder=std::make_unique<platform::Mp3Recording>();
                    if(encoder->open(path,overwrite,channels,sampleRate)){mp3=std::move(encoder);actualPath=path.u8string();activeFiles()[actualPath]=this;break;}
                    if(encoder->exists)continue;if(encoder->codecFailure)error=5;throw std::runtime_error("Cannot create MP3");
                }
                int fd;
#ifdef _WIN32
                fd=_wopen(path.c_str(),_O_WRONLY|_O_CREAT|_O_BINARY|(overwrite?_O_TRUNC:_O_EXCL),_S_IREAD|_S_IWRITE);
                if(fd>=0){file=_fdopen(fd,"wb");if(!file)_close(fd);}
#else
                fd=::open(path.c_str(),O_WRONLY|O_CREAT|(overwrite?O_TRUNC:O_EXCL),0666);
                if(fd>=0){file=fdopen(fd,"wb");if(!file)::close(fd);}
#endif
                if(file){actualPath=path.u8string();activeFiles()[actualPath]=this;header();break;}
                if(!overwrite&&fd<0&&errno==EEXIST)continue;
                throw std::runtime_error("Cannot create WAV");
            }
        }catch(...){if(!error)error=1;state=3;failure.store(error,std::memory_order_release);}
    }
    bool report(){
        // Prepared-but-unused sessions are retired during graph handover. They
        // must not overwrite the active/completed take's telemetry with Ready.
        if(!mailbox||state==0)return true;
        if((mailbox->write.load(std::memory_order_relaxed)+1)%mailbox->frames.size()==mailbox->read.load(std::memory_order_acquire))return false;
        auto frame=std::make_unique<AnalysisFrame>();frame->frequency=static_cast<float>(double(frames)/sampleRate);frame->values[0]=-9151;
        frame->values[1]=static_cast<float>(state);frame->values[2]=static_cast<float>(error);
        auto size=std::min(actualPath.size(),AnalysisFrame::maxValues-4);frame->values[3]=static_cast<float>(size);
        for(std::size_t i=0;i<size;i++)frame->values[4+i]=static_cast<unsigned char>(actualPath[i]);frame->valueCount=4+size;mailbox->publish(*frame);return true;
    }
    void run()noexcept{
        auto last=std::chrono::steady_clock::now();auto emptySince=last;std::uint64_t stopped=0;bool changed=true;
        while(true){
            auto r=read.load(std::memory_order_relaxed),w=write.load(std::memory_order_acquire);
            if(r!=w){
                auto& p=queue[r];
                if(p.kind==1)begin();
                else if(p.kind==2&&writing()){
                    if(mp3){if(mp3->write(p.data.data(),p.samples))frames+=p.samples/channels;else{error=3;failure.store(3,std::memory_order_release);finish();}}
                    else if(frames*channels*4+p.samples*4>0xffffffffULL-36){error=4;failure.store(4,std::memory_order_release);finish();}
                    else {auto written=std::fwrite(p.data.data(),sizeof(float),p.samples,file);frames+=written/channels;if(written!=p.samples){error=3;failure.store(3,std::memory_order_release);finish();}}
                }else if(p.kind==3)finish();
                read.store((r+1)%capacity,std::memory_order_release);emptySince=std::chrono::steady_clock::now();changed=true;
            }else{
                if(auto failed=failure.load(std::memory_order_acquire);failed&&writing()){error=failed;finish();changed=true;}
                auto epoch=stopEpoch.load(std::memory_order_acquire);
                if(epoch!=stopped&&std::chrono::steady_clock::now()-emptySince>std::chrono::milliseconds(15)){finish();stopped=epoch;changed=true;}
                if(closing.load(std::memory_order_acquire)){finish();report();break;}
                std::this_thread::sleep_for(std::chrono::milliseconds(1));
            }
            if(std::chrono::steady_clock::now()-last>std::chrono::milliseconds(100)){
                if(changed||writing())changed=!report();last=std::chrono::steady_clock::now();
            }
        }
    }
public:
    static std::shared_ptr<AudioRecording> create(std::string path,bool replace,unsigned width,unsigned channels,unsigned rate,std::size_t block,std::string id){
        auto result=std::shared_ptr<AudioRecording>(new AudioRecording(std::move(path),replace,width,channels,rate,block,std::move(id)));
        std::lock_guard<std::mutex> lock(registryMutex());auto& all=registry();all.erase(std::remove_if(all.begin(),all.end(),[](auto& p){return p.expired();}),all.end());all.push_back(result);return result;
    }
    AudioRecording(std::string path,bool replace,unsigned width,unsigned ch,unsigned rate,std::size_t block,std::string id):target(std::move(path)),identity(std::move(id)),overwrite(replace),digits(std::clamp(width,1u,12u)),channels(ch),sampleRate(rate){
        if(channels<1||channels>2||!sampleRate||!block)throw std::invalid_argument("Invalid recording format");
        for(auto& p:queue)p.data.resize(block*channels);if(!identity.empty())mailbox=analysisMailbox(identity);
        worker=std::thread([this]{run();});
    }
    ~AudioRecording(){closing.store(true,std::memory_order_release);if(worker.joinable())worker.join();}
    std::uint64_t epoch()const noexcept{return stopEpoch.load(std::memory_order_acquire);}
    static void stopAll(){std::lock_guard<std::mutex> lock(registryMutex());for(auto& weak:registry())if(auto p=weak.lock())p->stopEpoch.fetch_add(1,std::memory_order_release);}
    bool push(unsigned kind,const float* data=nullptr,std::size_t samples=0)noexcept{
        auto w=write.load(std::memory_order_relaxed);auto next=(w+1)%capacity;
        if(next==read.load(std::memory_order_acquire)||samples>queue[w].data.size()){failure.store(2,std::memory_order_release);return false;}
        if(kind==1)failure.store(0,std::memory_order_release);
        auto& p=queue[w];p.kind=kind;p.samples=samples;if(samples)std::copy_n(data,samples,p.data.data());write.store(next,std::memory_order_release);return true;
    }
    bool failed()const noexcept{return failure.load(std::memory_order_acquire)!=0;}
};
}
