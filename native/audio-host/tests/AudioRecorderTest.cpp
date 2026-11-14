#include "nodes/AudioRecorderNode.hpp"
#include <cassert>
#include <fstream>
#define DR_MP3_IMPLEMENTATION
#include "../third_party/dr_mp3.h"
using namespace easyknack;
static std::vector<float> samples(const std::filesystem::path& path,unsigned channels){
    std::ifstream in(path,std::ios::binary);assert(in);std::array<unsigned char,44> h{};in.read(reinterpret_cast<char*>(h.data()),44);
    assert(std::string(reinterpret_cast<char*>(h.data()),4)=="RIFF"&&h[20]==3&&h[22]==channels&&h[34]==32);
    auto size=std::uint32_t(h[40])|(std::uint32_t(h[41])<<8)|(std::uint32_t(h[42])<<16)|(std::uint32_t(h[43])<<24);
    assert(std::filesystem::file_size(path)==44+size);std::vector<float> result(size/4);in.read(reinterpret_cast<char*>(result.data()),size);return result;
}
int main(int argc,char** argv){
    assert(argc==2);auto root=std::filesystem::u8path(argv[1]);std::filesystem::create_directories(root);
    {auto unused=AudioRecording::create((root/"unused.wav").u8string(),false,2,2,48000,8,"unused-recording");unused.reset();auto report=std::make_unique<AnalysisFrame>();assert(!analysisMailbox("unused-recording")->latest(*report));}
    auto base=root/std::filesystem::u8path("Aufnahme_ä.wav");
    auto node=std::make_unique<AudioRecorderNode>();node->path=base.u8string();node->digits=2;node->bindIdentity("wav-test");node->prepare(48000,8);
    float l[]{0,1,2,3,4,5,6,7},r[]{0,-1,-2,-3,-4,-5,-6,-7},out[8]{};
    const float* inputs[]{l,nullptr,nullptr};const float* right[]{r,nullptr,nullptr};
    ProcessContext c{inputs,3,out,8,48000,out,8};c.rightInputs=right;
    ValueEventBuffer start,end;start.clear(8);end.clear(8);start.push(2,1,ValueEventKind::trigger);end.push(6,1,ValueEventKind::trigger);
    const ValueEventBuffer* events[]{nullptr,&start,&end};c.valueEventInputs=events;node->process(c);assert(!node->active);
    // Destroy on the control thread drains accepted packets and fixes the RIFF sizes.
    node.reset();auto first=base;auto data=samples(first,2);assert(data.size()==8);
    for(int i=0;i<4;i++){assert(data[2*i]==i+2);assert(data[2*i+1]==-i-2);}
    // The first take is unnumbered; a collision moves it aside and continues the series.
    float secondSamples[]{.25f,.5f};
    auto second=AudioRecording::create(base.u8string(),false,2,1,48000,8,"");second->push(1);second->push(2,secondSamples,2);second->push(3);second.reset();
    assert(!std::filesystem::exists(base));
    assert(samples(root/std::filesystem::u8path("Aufnahme_ä_01.wav"),2)==data);
    assert(samples(root/std::filesystem::u8path("Aufnahme_ä_02.wav"),1).size()==2);
    second=AudioRecording::create(base.u8string(),false,4,1,48000,8,"");second->push(1);second->push(2,secondSamples,2);second.reset();
    assert(samples(root/std::filesystem::u8path("Aufnahme_ä_0003.wav"),1).size()==2);
    // An active unnumbered take is renamed only after its writer has closed.
    auto overlap=root/"Overlap.wav";
    auto a=AudioRecording::create(overlap.u8string(),false,4,1,48000,8,"");a->push(1);a->push(2,secondSamples,2);
    for(int i=0;i<2000&&!std::filesystem::exists(overlap);i++)std::this_thread::sleep_for(std::chrono::milliseconds(1));
    assert(std::filesystem::exists(overlap));
    auto b=AudioRecording::create(overlap.u8string(),false,4,1,48000,8,"");b->push(1);b->push(2,secondSamples,2);b.reset();
    assert(std::filesystem::exists(overlap));assert(samples(root/"Overlap_0002.wav",1).size()==2);
    a.reset();assert(!std::filesystem::exists(overlap));assert(samples(root/"Overlap_0001.wav",1).size()==2);
    // Existing numbered files are never overwritten, even if the numeric width grows.
    for(int i=1;i<=99;i++){std::ofstream f(root/("Growth_"+std::to_string(i)+".wav"));}
    auto session=AudioRecording::create((root/"Growth.wav").u8string(),false,1,1,48000,8,"");float x[]{.25f,.5f};session->push(1);session->push(2,x,2);session->push(3);session.reset();
    assert(samples(root/"Growth_100.wav",1).size()==2);
    // Explicit overwrite replaces the target; mono stores arithmetic channel means.
    node=std::make_unique<AudioRecorderNode>();node->path=base.u8string();node->channels=1;node->overwrite=true;node->bindIdentity("wav-mono");node->prepare(48000,8);
    start.clear(8);end.clear(8);start.push(0,1,ValueEventKind::trigger);node->process(c);assert(node->active);
    AudioRecording::stopAll();std::this_thread::sleep_for(std::chrono::milliseconds(30));start.clear(8);node->process(c);assert(!node->active);node.reset();
    data=samples(base,1);assert(data.size()==8);for(float v:data)assert(v==0);
    session=AudioRecording::create(base.u8string(),true,1,1,48000,8,"");session->push(1);session->push(2,x,2);session.reset();assert(samples(base,1).size()==2);
    // Two polyphonic voices are folded into the same stereo recording.
    node=std::make_unique<AudioRecorderNode>();node->path=(root/"voices.wav").u8string();node->overwrite=true;node->prepare(48000,8);
    const float* poly[]{l,l,nullptr,nullptr,nullptr,nullptr};c.inputs=poly;c.inputCount=6;c.rightInputs=nullptr;
    start.push(0,1,ValueEventKind::trigger);const ValueEventBuffer* pe[]{nullptr,nullptr,&start,nullptr,&end,nullptr};c.valueEventInputs=pe;node->process(c);node.reset();
    data=samples(root/"voices.wav",2);assert(data.size()==16&&data[2]==2&&data[3]==2);
    // Native Windows encoding is optional at runtime; on this test machine it must
    // produce decodable mono/stereo MP3 and preserve an existing numbered take.
#ifdef _WIN32
    for(unsigned channels:{1u,2u}){
        auto target=root/std::filesystem::u8path("MP3_ä_"+std::to_string(channels)+".mp3");
        auto take=AudioRecording::create(target.u8string(),false,2,channels,48000,256,"");assert(take->push(1));
        std::array<float,512> wave{};for(int b=0;b<200;b++){
            for(int i=0;i<256;i++)for(unsigned ch=0;ch<channels;ch++)wave[i*channels+ch]=.5f*std::sin(6.283185307179586*440*(b*256+i)/48000);
            assert(take->push(2,wave.data(),256*channels));
        }assert(take->push(3));take.reset();
        auto path=target;drmp3 decoder{};assert(drmp3_init_file_w(&decoder,path.c_str(),nullptr));assert(decoder.channels==channels&&decoder.sampleRate==48000);
        std::vector<float> decoded(60000*channels);auto frames=drmp3_read_pcm_frames_f32(&decoder,60000,decoded.data());drmp3_uninit(&decoder);if(frames<51200||frames>=55000)std::fprintf(stderr,"MP3 channels=%u decoded=%llu\n",channels,static_cast<unsigned long long>(frames));assert(frames>=51200&&frames<55000);
        double power=0;for(std::size_t i=0;i<frames*channels;i++){assert(std::isfinite(decoded[i]));power+=decoded[i]*decoded[i];}assert(std::sqrt(power/(frames*channels))>.2);
        auto bytes=std::filesystem::file_size(path);take=AudioRecording::create(target.u8string(),false,2,channels,48000,256,"");take->push(1);take->push(2,wave.data(),256*channels);take->push(3);take.reset();assert(!std::filesystem::exists(target));assert(std::filesystem::file_size(root/std::filesystem::u8path(target.stem().u8string()+"_01.mp3"))==bytes);assert(std::filesystem::exists(root/std::filesystem::u8path(target.stem().u8string()+"_02.mp3")));
    }
#endif
}
