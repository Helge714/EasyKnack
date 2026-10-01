#pragma once
#include <array>
#include <atomic>
#include <chrono>
#include <filesystem>
#include <fstream>
#include <thread>
#include <stdexcept>
#include <vector>
#include <algorithm>
#include <cstdint>

namespace easyknack {
// One bounded read-ahead cache. Only the worker performs file I/O after construction.
class AudioFileStream {
    static constexpr std::uint64_t pageFrames=8192;
    struct Page {std::atomic<unsigned> state{0};std::uint64_t index=~std::uint64_t{};std::array<float,pageFrames*2> data{};};
    std::array<Page,12> pages;
    std::ifstream file;
    std::atomic<bool> stopping{false};
    std::atomic<std::uint64_t> requested{0},loopPage{0};
    std::thread worker;
    static std::uint64_t readInteger(std::istream& in,int bytes){std::uint64_t value=0;for(int i=0;i<bytes;i++){int c=in.get();if(c<0)throw std::runtime_error("Truncated audio cache");value|=std::uint64_t(c)<<(8*i);}return value;}
    void load(Page& page,std::uint64_t index){
        page.data.fill(0);file.clear();file.seekg(24+index*pageFrames*channels*4);
        auto count=std::min(pageFrames,frames-index*pageFrames)*channels;
        file.read(reinterpret_cast<char*>(page.data.data()),static_cast<std::streamsize>(count*4));
        if(file.gcount()!=static_cast<std::streamsize>(count*4))failed.store(true,std::memory_order_relaxed);
        page.index=index;page.state.store(2,std::memory_order_release);
    }
    void run(){
        while(!stopping.load(std::memory_order_relaxed)){
            auto head=std::min(requested.load(std::memory_order_relaxed),frames-1)/pageFrames;
            auto total=(frames+pageFrames-1)/pageFrames;
            std::array<std::uint64_t,8> wanted{};for(int i=0;i<7;i++)wanted[i]=(head+i)%total;wanted[7]=std::min(loopPage.load(std::memory_order_relaxed),total-1);
            for(auto index:wanted){
                bool found=false;for(auto& p:pages)if(p.state.load(std::memory_order_acquire)>=2&&p.index==index){found=true;break;}
                if(found)continue;
                for(auto& p:pages){
                    unsigned state=p.state.load(std::memory_order_acquire);
                    if(state!=0&&state!=2)continue;
                    if(state==2&&std::find(wanted.begin(),wanted.end(),p.index)!=wanted.end())continue;
                    if(p.state.compare_exchange_strong(state,1,std::memory_order_acq_rel)){load(p,index);break;}
                }
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(2));
        }
    }
public:
    std::uint32_t channels=0,rate=0;std::uint64_t frames=0;
    std::atomic<bool> failed{false};
    explicit AudioFileStream(const std::string& path):file(std::filesystem::u8path(path),std::ios::binary){
        char magic[8]{};file.read(magic,8);if(std::string(magic,8)!="EKPCM001")throw std::runtime_error("Invalid audio cache");
        rate=static_cast<std::uint32_t>(readInteger(file,4));channels=static_cast<std::uint32_t>(readInteger(file,4));frames=readInteger(file,8);
        if(rate!=48000||channels<1||channels>2||frames==0||frames>(std::uint64_t{1}<<40))throw std::runtime_error("Invalid audio cache dimensions");
        file.seekg(0,std::ios::end);if(static_cast<std::uint64_t>(file.tellg())!=24+frames*channels*4)throw std::runtime_error("Incomplete audio cache");
        auto count=std::min<std::uint64_t>(pages.size(),(frames+pageFrames-1)/pageFrames);
        for(std::uint64_t i=0;i<count;i++)load(pages[i],i);
        worker=std::thread([this]{run();});
    }
    ~AudioFileStream(){stopping.store(true,std::memory_order_relaxed);if(worker.joinable())worker.join();}
    void request(std::uint64_t frame,std::uint64_t loop)noexcept{requested.store(frame,std::memory_order_relaxed);loopPage.store(loop/pageFrames,std::memory_order_relaxed);}
    bool sample(std::uint64_t frame,std::size_t channel,float& value)noexcept{
        if(frame>=frames){value=0;return true;}
        const auto index=frame/pageFrames;
        for(auto& p:pages){
            unsigned state=p.state.load(std::memory_order_acquire);
            if(state<2)continue;
            // Bounded attempt; a concurrent reader or writer never makes audio wait.
            if(!p.state.compare_exchange_strong(state,state+1,std::memory_order_acquire))continue;
            bool match=p.index==index;
            if(match)value=p.data[(frame%pageFrames)*channels+std::min<std::size_t>(channel,channels-1)];
            p.state.fetch_sub(1,std::memory_order_release);if(match)return true;
        }
        return false;
    }
};
}
