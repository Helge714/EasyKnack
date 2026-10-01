#pragma once
#include "ParameterContracts.hpp"
#include "../NodeProcessor.hpp"
#include <fstream>
#include <filesystem>
#include <mutex>
#include <iomanip>
#include <unordered_map>
namespace easyknack {
struct FileBufferLoadError:std::runtime_error {using std::runtime_error::runtime_error;};
/** Immutable planar PCM; weak cache avoids duplicate allocations during graph replacement. */
struct FileBufferData {
 HistoryResource channels[2]; unsigned channelCount=0;
 static std::shared_ptr<FileBufferData> load(const std::string& path,double start=0,double end=0){
  static std::mutex mutex;static std::unordered_map<std::string,std::weak_ptr<FileBufferData>> cache;
  std::lock_guard<std::mutex> guard(mutex);
  std::ostringstream keyStream;keyStream<<path<<"|"<<std::setprecision(17)<<start<<"|"<<end;auto key=keyStream.str();
  if(auto previous=cache[key].lock())return previous;
  for(auto i=cache.begin();i!=cache.end();)if(i->second.expired())i=cache.erase(i);else ++i;
  std::ifstream in(std::filesystem::u8path(path),std::ios::binary);
  char magic[8];std::uint32_t rate=0,count=0;std::uint64_t frames=0;
  in.read(magic,8);in.read(reinterpret_cast<char*>(&rate),4);in.read(reinterpret_cast<char*>(&count),4);in.read(reinterpret_cast<char*>(&frames),8);
  if(!in||std::string(magic,8)!="EKPCM001"||rate!=48000||count<1||count>2||!frames||frames>std::numeric_limits<std::size_t>::max()/count/sizeof(float)||std::filesystem::file_size(std::filesystem::u8path(path))!=24+frames*count*sizeof(float))throw std::runtime_error("Invalid file buffer PCM");
  if(!std::isfinite(start)||!std::isfinite(end)||start<0||end<0||start>=frames/double(rate)||end!=0&&end<=start)throw std::runtime_error("Invalid file buffer selection");
  auto first=static_cast<std::uint64_t>(std::min<double>(frames,std::round(start*rate)));auto last=end==0?frames:static_cast<std::uint64_t>(std::min<double>(frames,std::round(end*rate)));
  if(last<=first)throw std::runtime_error("Empty file buffer selection");
  in.seekg(static_cast<std::streamoff>(24+first*count*sizeof(float)));frames=last-first;
  auto result=std::make_shared<FileBufferData>();result->channelCount=count;
  for(unsigned c=0;c<count;c++){auto& h=result->channels[c];h.samples.resize(static_cast<std::size_t>(frames));h.sampleRate=rate;h.end=frames;h.immutable=true;}
  float chunk[8192];std::uint64_t offset=0;
  while(offset<frames){auto n=std::min<std::uint64_t>(frames-offset,8192/count);in.read(reinterpret_cast<char*>(chunk),n*count*sizeof(float));if(!in)throw std::runtime_error("Cannot read file buffer PCM");
   for(std::size_t i=0;i<n;i++)for(unsigned c=0;c<count;c++){float v=chunk[i*count+c];result->channels[c].samples[offset+i]=std::isfinite(v)?v:0;}offset+=n;
  }
  cache[key]=result;return result;
 }
};
struct FileBufferNode:NodeProcessor {
 std::shared_ptr<FileBufferData> data;unsigned channel=0;
 bool supportsControlRate()const noexcept override{return true;}
 std::size_t outputCount()const noexcept override{return 3;}
 ResourceKind outputResourceKind(std::size_t p)const noexcept override{return p==0?ResourceKind::history:ResourceKind::none;}
 DspResource* resourceOutput(std::size_t p)noexcept override{return p==0&&data?&data->channels[std::min(channel,data->channelCount-1)]:nullptr;}
 void shareChannelState(NodeProcessor& p)override{data=static_cast<FileBufferNode&>(p).data;channel=1;}
 void configure(std::istream& in)override{
  auto p=parameters::FileBufferNodeParameters::read(in);if(p.file_value=="none")return;
  auto& hex=p.file_value;if(hex.size()%2)throw std::runtime_error("Invalid file buffer path");std::string path;
  auto digit=[](char c)->unsigned{if(c>='0'&&c<='9')return c-'0';if(c>='a'&&c<='f')return c-'a'+10;throw std::runtime_error("Invalid file buffer path");};
  for(std::size_t i=0;i<hex.size();i+=2)path.push_back(static_cast<char>(digit(hex[i])*16+digit(hex[i+1])));
  try{data=FileBufferData::load(path,p.start_value,p.end_value);}catch(const std::exception& error){throw FileBufferLoadError(std::string("FILE_BUFFER_LOAD: ")+error.what());}
 }
 void process(ProcessContext& c)noexcept override{
  double frames=data?data->channels[0].samples.size():0;
  if(auto out=c.outputAt(1))std::fill_n(out,c.bufferSize,static_cast<float>(frames/48000));
  if(auto out=c.outputAt(2))std::fill_n(out,c.bufferSize,static_cast<float>(frames));
 }
};
EASYKNACK_NODE(FileBufferNode,"filebuffer")
}
