#include "nodes/AudioRecorderNode.hpp"
#include <cassert>
#include <fstream>
using namespace easyknack;
static std::vector<float> samples(const std::filesystem::path& path,unsigned channels){
    std::ifstream in(path,std::ios::binary);assert(in);std::array<unsigned char,44> h{};in.read(reinterpret_cast<char*>(h.data()),44);
    assert(std::string(reinterpret_cast<char*>(h.data()),4)=="RIFF"&&h[20]==3&&h[22]==channels&&h[34]==32);
    auto size=std::uint32_t(h[40])|(std::uint32_t(h[41])<<8)|(std::uint32_t(h[42])<<16)|(std::uint32_t(h[43])<<24);
    assert(std::filesystem::file_size(path)==44+size);std::vector<float> result(size/4);in.read(reinterpret_cast<char*>(result.data()),size);return result;
}
int main(int argc,char** argv){
    assert(argc==2);auto root=std::filesystem::u8path(argv[1]);std::filesystem::create_directories(root);
    auto base=root/std::filesystem::u8path("Aufnahme_ä.wav");
    auto node=std::make_unique<AudioRecorderNode>();node->path=base.u8string();node->digits=2;node->bindIdentity("wav-test");node->prepare(48000,8);
    float l[]{0,1,2,3,4,5,6,7},r[]{0,-1,-2,-3,-4,-5,-6,-7},out[8]{};
    const float* inputs[]{l,nullptr,nullptr};const float* right[]{r,nullptr,nullptr};
    ProcessContext c{inputs,3,out,8,48000,out,8};c.rightInputs=right;
    ValueEventBuffer start,end;start.clear(8);end.clear(8);start.push(2,1,ValueEventKind::trigger);end.push(6,1,ValueEventKind::trigger);
    const ValueEventBuffer* events[]{nullptr,&start,&end};c.valueEventInputs=events;node->process(c);assert(!node->active);
    // Destroy on the control thread drains accepted packets and fixes the RIFF sizes.
    node.reset();auto first=root/std::filesystem::u8path("Aufnahme_ä_01.wav");auto data=samples(first,2);assert(data.size()==8);
    for(int i=0;i<4;i++){assert(data[2*i]==i+2);assert(data[2*i+1]==-i-2);}
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
}
