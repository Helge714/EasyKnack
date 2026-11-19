#pragma once
#include "SharedLibrary.hpp"
#include "lame/include/lame.h"
#include <filesystem>
#include <vector>
#include <algorithm>
#include <mutex>
#include <cmath>
#include <cstdio>
#include <cerrno>
#include <fcntl.h>
#ifdef _WIN32
#include <io.h>
#include <sys/stat.h>
#else
#include <unistd.h>
#endif
namespace easyknack::platform {
/** Portable MP3 encoder, called only by the recording worker. Retains LAME's
 * gapless tag and never overwrites an existing file in numbered-take mode. */
class LameMp3Recording {
    struct Api {
        SharedLibrary library;
#define EK_LAME(name) decltype(&::name) name=nullptr
        EK_LAME(lame_init);EK_LAME(lame_close);EK_LAME(lame_set_num_channels);EK_LAME(lame_set_in_samplerate);
        EK_LAME(lame_set_brate);EK_LAME(lame_set_quality);EK_LAME(lame_set_bWriteVbrTag);EK_LAME(lame_set_write_id3tag_automatic);
        EK_LAME(lame_init_params);EK_LAME(lame_encode_buffer_ieee_float);EK_LAME(lame_encode_buffer_interleaved_ieee_float);
        EK_LAME(lame_encode_flush);EK_LAME(lame_get_lametag_frame);
#undef EK_LAME
        bool load(){
#ifdef _WIN32
            if(!library.open("libmp3lame.dll"))return false;
#elif defined(__APPLE__)
            if(!library.open("libmp3lame.dylib"))return false;
#else
            if(!library.open("libmp3lame.so.0"))return false;
#endif
#define EK_LAME(name) name=library.symbol<decltype(name)>(#name);if(!name)return false
            EK_LAME(lame_init);EK_LAME(lame_close);EK_LAME(lame_set_num_channels);EK_LAME(lame_set_in_samplerate);
            EK_LAME(lame_set_brate);EK_LAME(lame_set_quality);EK_LAME(lame_set_bWriteVbrTag);EK_LAME(lame_set_write_id3tag_automatic);
            EK_LAME(lame_init_params);EK_LAME(lame_encode_buffer_ieee_float);EK_LAME(lame_encode_buffer_interleaved_ieee_float);
            EK_LAME(lame_encode_flush);EK_LAME(lame_get_lametag_frame);
#undef EK_LAME
            return true;
        }
    } api;
    lame_t encoder=nullptr;
    FILE* file=nullptr;
    unsigned channels=0;
    bool finished=false;
    std::vector<unsigned char> bytes;
    std::vector<float> clipped;
    // Upstream initializes shared quantization tables per encoder. Serialize
    // codec calls on recording workers; the audio callback only queues samples.
    static std::mutex& codecMutex(){static std::mutex mutex;return mutex;}
    bool append(int count){return count>=0&&std::fwrite(bytes.data(),1,static_cast<std::size_t>(count),file)==static_cast<std::size_t>(count);}
public:
    bool exists=false,codecFailure=false;
    bool open(const std::filesystem::path& path,bool overwrite,unsigned count,unsigned rate){
        std::lock_guard<std::mutex> lock(codecMutex());
        if(file||encoder||count<1||count>2)return false;
        if(!api.load()||!(encoder=api.lame_init())){codecFailure=true;return false;}
        if(api.lame_set_num_channels(encoder,count)<0||api.lame_set_in_samplerate(encoder,rate)<0||api.lame_set_brate(encoder,count==1?128:192)<0||api.lame_set_quality(encoder,2)<0){codecFailure=true;return false;}
        api.lame_set_bWriteVbrTag(encoder,1);api.lame_set_write_id3tag_automatic(encoder,0);
        if(api.lame_init_params(encoder)<0){codecFailure=true;return false;}
#ifdef _WIN32
        int descriptor=_wopen(path.c_str(),_O_WRONLY|_O_CREAT|_O_BINARY|(overwrite?_O_TRUNC:_O_EXCL),_S_IREAD|_S_IWRITE);
        if(descriptor>=0){file=_fdopen(descriptor,"wb");if(!file)_close(descriptor);}
#else
        int descriptor=::open(path.c_str(),O_WRONLY|O_CREAT|(overwrite?O_TRUNC:O_EXCL),0666);
        if(descriptor>=0){file=fdopen(descriptor,"wb");if(!file)::close(descriptor);}
#endif
        if(!file){exists=!overwrite&&descriptor<0&&errno==EEXIST;return false;}
        channels=count;bytes.resize(16384);return true;
    }
    bool write(const float* samples,std::size_t count){
        std::lock_guard<std::mutex> lock(codecMutex());
        if(!file||finished||count%channels)return false;
        auto frames=count/channels;if(frames>1000000)return false;
        bytes.resize(std::max<std::size_t>(16384,frames*5/4+7200));
        clipped.resize(count);for(std::size_t i=0;i<count;i++)clipped[i]=std::isnan(samples[i])?0:std::clamp(samples[i],-1.f,1.f);
        int encoded=channels==2?api.lame_encode_buffer_interleaved_ieee_float(encoder,clipped.data(),static_cast<int>(frames),bytes.data(),static_cast<int>(bytes.size()))
            :api.lame_encode_buffer_ieee_float(encoder,clipped.data(),nullptr,static_cast<int>(frames),bytes.data(),static_cast<int>(bytes.size()));
        return append(encoded);
    }
    bool finish(){
        std::lock_guard<std::mutex> lock(codecMutex());
        if(!file||finished)return true;finished=true;bytes.resize(16384);
        bool ok=append(api.lame_encode_flush(encoder,bytes.data(),static_cast<int>(bytes.size())));
        auto tag=api.lame_get_lametag_frame(encoder,bytes.data(),bytes.size());
        if(tag>bytes.size())ok=false;
        else if(tag){if(std::fseek(file,0,SEEK_SET)!=0||std::fwrite(bytes.data(),1,tag,file)!=tag)ok=false;}
        if(std::fflush(file)!=0)ok=false;if(std::fclose(file)!=0)ok=false;file=nullptr;return ok;
    }
    ~LameMp3Recording(){finish();std::lock_guard<std::mutex> lock(codecMutex());if(encoder)api.lame_close(encoder);}
};
}
