#pragma once
#include "Analysis.hpp"
#include <thread>
#include <chrono>
#include <filesystem>
#include <cstdio>
#include <cerrno>
#include <mutex>
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
    std::uint64_t frames=0,nextIndex=1;
    unsigned state=0,error=0;
    std::string actualPath;
    static auto& registry(){static std::vector<std::weak_ptr<AudioRecording>> value;return value;}
    static auto& registryMutex(){static std::mutex value;return value;}
    static void word(FILE* f,std::uint32_t n,unsigned bytes){for(unsigned i=0;i<bytes;i++)std::fputc((n>>(8*i))&255,f);}
    void header(){
        std::rewind(file);std::fwrite("RIFF",1,4,file);word(file,36+static_cast<std::uint32_t>(frames*channels*4),4);
        std::fwrite("WAVEfmt ",1,8,file);word(file,16,4);word(file,3,2);word(file,channels,2);word(file,sampleRate,4);
        word(file,sampleRate*channels*4,4);word(file,channels*4,2);word(file,32,2);std::fwrite("data",1,4,file);word(file,static_cast<std::uint32_t>(frames*channels*4),4);
    }
    void finish(){if(file){header();if(std::ferror(file)||std::fflush(file))error=3;if(std::fclose(file))error=3;file=nullptr;state=error?3:2;}}
    void begin(){
        finish();frames=0;error=0;state=1;
        try{
            if(target.empty())throw std::runtime_error("No recording path");
            auto base=std::filesystem::u8path(target);
            if(!base.parent_path().empty())std::filesystem::create_directories(base.parent_path());
            for(;;){
                auto path=base;
                if(!overwrite){auto index=std::to_string(nextIndex++);if(index.size()<digits)index.insert(0,digits-index.size(),'0');path=base.parent_path()/std::filesystem::u8path(base.stem().u8string()+"_"+index+".wav");}
                int fd;
#ifdef _WIN32
                fd=_wopen(path.c_str(),_O_WRONLY|_O_CREAT|_O_BINARY|(overwrite?_O_TRUNC:_O_EXCL),_S_IREAD|_S_IWRITE);
                if(fd>=0){file=_fdopen(fd,"wb");if(!file)_close(fd);}
#else
                fd=::open(path.c_str(),O_WRONLY|O_CREAT|(overwrite?O_TRUNC:O_EXCL),0666);
                if(fd>=0){file=fdopen(fd,"wb");if(!file)::close(fd);}
#endif
                if(file){actualPath=path.u8string();header();break;}
                if(!overwrite&&fd<0&&errno==EEXIST)continue;
                throw std::runtime_error("Cannot create WAV");
            }
        }catch(...){error=1;state=3;failure.store(1,std::memory_order_release);}
    }
    void report(){
        if(!mailbox)return;
        auto frame=std::make_unique<AnalysisFrame>();frame->frequency=static_cast<float>(double(frames)/sampleRate);frame->values[0]=-9151;
        frame->values[1]=static_cast<float>(state);frame->values[2]=static_cast<float>(error);
        auto size=std::min(actualPath.size(),AnalysisFrame::maxValues-4);frame->values[3]=static_cast<float>(size);
        for(std::size_t i=0;i<size;i++)frame->values[4+i]=static_cast<unsigned char>(actualPath[i]);frame->valueCount=4+size;mailbox->publish(*frame);
    }
    void run()noexcept{
        auto last=std::chrono::steady_clock::now();auto emptySince=last;std::uint64_t stopped=0;bool changed=true;
        while(true){
            auto r=read.load(std::memory_order_relaxed),w=write.load(std::memory_order_acquire);
            if(r!=w){
                auto& p=queue[r];
                if(p.kind==1)begin();
                else if(p.kind==2&&file){
                    if(frames*channels*4+p.samples*4>0xffffffffULL-36){error=4;failure.store(4,std::memory_order_release);finish();}
                    else {auto written=std::fwrite(p.data.data(),sizeof(float),p.samples,file);frames+=written/channels;if(written!=p.samples){error=3;failure.store(3,std::memory_order_release);finish();}}
                }else if(p.kind==3)finish();
                read.store((r+1)%capacity,std::memory_order_release);emptySince=std::chrono::steady_clock::now();changed=true;
            }else{
                if(auto failed=failure.load(std::memory_order_acquire);failed&&file){error=failed;finish();changed=true;}
                auto epoch=stopEpoch.load(std::memory_order_acquire);
                if(epoch!=stopped&&std::chrono::steady_clock::now()-emptySince>std::chrono::milliseconds(15)){finish();stopped=epoch;changed=true;}
                if(closing.load(std::memory_order_acquire)){finish();report();break;}
                std::this_thread::sleep_for(std::chrono::milliseconds(1));
            }
            if(std::chrono::steady_clock::now()-last>std::chrono::milliseconds(100)){
                if(changed||file){report();changed=false;}last=std::chrono::steady_clock::now();
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
