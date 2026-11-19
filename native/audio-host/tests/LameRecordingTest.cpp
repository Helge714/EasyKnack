#define MA_NO_DEVICE_IO
#define MA_NO_ENCODING
#define MA_NO_RESOURCE_MANAGER
#define MA_NO_NODE_GRAPH
#define MA_NO_ENGINE
#define MA_NO_GENERATION
#define MA_NO_FLAC
#define MINIAUDIO_IMPLEMENTATION
#include "miniaudio.h"
#include "../src/platform/LameMp3Recording.hpp"
#include "../src/audio/AudioFileDecode.hpp"
#include <chrono>
#include <iostream>
static void require(bool value){if(!value)throw std::runtime_error("Portable MP3 recording assertion failed");}
static std::string hex(const std::string& value){std::string result;for(unsigned char c:value){result+="0123456789abcdef"[c>>4];result+="0123456789abcdef"[c&15];}return result;}
int main(){
    using namespace easyknack::platform;
    auto folder=std::filesystem::temp_directory_path()/("easyknack-codec-"+std::to_string(std::chrono::steady_clock::now().time_since_epoch().count()));
    require(std::filesystem::create_directory(folder));
    for(unsigned channels:{1u,2u}){
        auto path=folder/std::filesystem::u8path("recording-ä-"+std::to_string(channels)+".mp3");
        constexpr unsigned frames=48000;std::vector<float> original(frames*channels);
        for(unsigned i=0;i<frames;i++)for(unsigned c=0;c<channels;c++)original[i*channels+c]=.25f*std::sin(6.283185307179586*(c?660:440)*i/48000.);
        {LameMp3Recording encoder;require(encoder.open(path,false,channels,48000));
            for(unsigned i=0;i<frames;i+=257)require(encoder.write(original.data()+i*channels,std::min(257u,frames-i)*channels));
            require(encoder.finish());require(encoder.finish());}
        auto size=std::filesystem::file_size(path);require(size>2000);
        {LameMp3Recording encoder;require(!encoder.open(path,false,channels,48000)&&encoder.exists);}
        require(std::filesystem::file_size(path)==size);
        drmp3 decoder{};
#ifdef _WIN32
        require(drmp3_init_file_w(&decoder,path.c_str(),nullptr));
#else
        require(drmp3_init_file(&decoder,path.c_str(),nullptr));
#endif
        require(decoder.channels==channels&&decoder.sampleRate==48000);
        std::vector<float> decoded((frames+4096)*channels);auto count=drmp3_read_pcm_frames_f32(&decoder,frames+4096,decoded.data());drmp3_uninit(&decoder);
        require(count==frames);double error=0;for(unsigned i=0;i<frames*channels;i++){auto difference=original[i]-decoded[i];error+=difference*difference;}require(std::sqrt(error/(frames*channels))<.025);
        auto pcm=folder/("decoded-"+std::to_string(channels)+".pcm");easyknack::decodeAudioFile(hex(path.u8string()),hex(pcm.u8string()));
        require(std::filesystem::file_size(pcm)==24+frames*channels*sizeof(float));
        std::filesystem::remove(pcm);std::filesystem::remove(path);
    }
    std::filesystem::remove(folder);std::cout<<"Portable MP3 encode, gapless decode, import and collision tests passed\n";
}
