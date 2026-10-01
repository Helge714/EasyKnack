#pragma once
#include "BakedGraph.hpp"
#include <chrono>
#include <cstring>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <iomanip>
#include <stdexcept>
#include <vector>

namespace easyknack {
// The sequence is indexed in samples, independently of callback size and measured speed.
struct ReferenceMidi final:MidiSource {
    MidiBlock block;
    ReferenceMidi(){block.source=1;block.generation=1;}
    void beginBlock(std::size_t,double)noexcept override{}
    const MidiBlock* events(std::uint64_t)const noexcept override{return &block;}
    void at(std::uint64_t start,unsigned frames){
        block.clear();
        auto emit=[&](std::uint64_t at,unsigned message){if(at>=start&&at<start+frames)block.append(unsigned(at-start),message);};
        // 32 simultaneous held notes, staggered within a block; voice stealing in 1/8-voice cases.
        for(unsigned note=0;note<32;note++){
            emit(note*7,144|((48+note)<<8)|((80+note)<<16));
            emit(18000+note*11,128|((48+note)<<8));
            emit(24000+note*7,144|((48+note)<<8)|(100<<16));
        }
        emit(10000,224|(80<<16)); // Pitch bend while notes are held.
        emit(32000,176|(123<<8));
    }
};

class ReferenceHarness {
    std::ofstream equality,performance;
    using Clock=std::chrono::steady_clock;
    static constexpr float tolerance=1e-5f; // Declared before measurement; NaNs never pass.
    static const Node* find(const AudioGraph& graph,const std::string& id){
        for(const auto& n:graph.nodes){
            if(n.processorId==id)return &n;
            if(auto* baked=dynamic_cast<const BakedGraphProcessor*>(n.processor.get()))
                if(auto* found=find(baked->runtime->graph,id))return found;
        }
        return nullptr;
    }
    static void sameMidi(const AudioGraph& a,const AudioGraph& b){
        auto* x=find(a,"mididelay");auto* y=find(b,"mididelay");
        if(!x&&!y)return;
        if(!x||!y)throw std::runtime_error("Missing MIDI delay");
        const auto& first=x->midi;const auto& second=y->midi;
        if(first.count!=second.count||first.panic!=second.panic||first.source!=second.source||first.generation!=second.generation)
            throw std::runtime_error("MIDI block metadata differs");
        for(std::size_t i=0;i<first.count;i++)if(first.events[i].offset!=second.events[i].offset||first.events[i].message!=second.events[i].message||first.events[i].transpose!=second.events[i].transpose)
            throw std::runtime_error("MIDI event order, offset or value differs");
    }
    // Counts owned float sample buffers, including voices/nested runtimes, deduplicating sharing.
    // This deliberately excludes processor state, allocator overhead, event/MIDI and executable memory.
    static std::size_t sampleBytes(const AudioGraph& graph){
        std::unordered_set<const void*> buffers,graphs;
        auto visit=[&](auto&& self,const AudioGraph& g)->void{
            if(!graphs.insert(&g).second)return;
            for(const auto& n:g.nodes)for(std::size_t v=0;v<n.voiceCount();v++){
                const auto& voice=n.voice(v);buffers.insert(voice.buffer.data());
                for(const auto& b:voice.heldInputs)buffers.insert(b.data());
                for(const auto& b:voice.extraBuffers)buffers.insert(b.data());
                for(const auto& c:voice.channelCache)for(const auto& b:c.outputs)buffers.insert(b.data());
                for(auto* processor:{voice.processor.get(),voice.rightProcessor.get()})
                    if(auto* baked=dynamic_cast<const BakedGraphProcessor*>(processor))self(self,baked->runtime->graph);
            }
        };
        visit(visit,graph);return buffers.size()*sizeof(float)*blockSize;
    }
    static void render(AudioGraph& graph,unsigned frames,ReferenceMidi& midi,std::array<float,512>& output){
        graph.process(output.data(),frames,0,nullptr,&midi);
        graph.process(output.data()+256,frames,1,nullptr,&midi);
    }
public:
    explicit ReferenceHarness(const std::filesystem::path& directory):equality(directory/"equivalence.csv"),performance(directory/"offline.csv"){
        if(!equality||!performance)throw std::runtime_error("Cannot create reference reports");
        equality<<"scenario,frames,samples_per_channel,bit_differences,max_abs_error,nonzero_samples,state_transfers\n";
        performance<<"scenario,frames,repeat,mode,blocks,mean_us,p99_us,max_us,over_budget,sample_buffer_bytes\n";
        performance<<std::setprecision(10);
    }
    template<class Factory>void run(const std::string& name,const std::string& original,const std::string& plugin,const std::vector<std::pair<std::string,std::string>>& changes,Factory factory){
        auto make=[&](int mode,int change=-1){
            const auto& first=change<0?original:changes[change].first;
            const auto& second=change<0?plugin:changes[change].second;
            auto graph=mode==0?AudioGraph::parse(first):AudioGraph::compile(second,[&](const std::string& id){return factory(id,mode==2);});
            if(mode==3)for(auto& node:graph.nodes)
                if(auto* baked=dynamic_cast<BakedGraphProcessor*>(node.processor.get()))baked->runtime->graph.directChannelOutputs=false;
            return graph;
        };
        for(unsigned frames:{1u,17u,128u,256u}){
            auto a=make(0),b=make(1),c=make(2),copy=make(3);ReferenceMidi midi;std::array<float,512> x{},y{},z{},copied{};
            for(auto* graph:{&b,&c})for(const auto& node:graph->nodes)
                if(auto* baked=dynamic_cast<const BakedGraphProcessor*>(node.processor.get())){
                    if(node.stereo!=(name=="stereo"))throw std::runtime_error(name+": wrong exported channel layout");
                    for(const auto& inner:baked->runtime->graph.nodes)
                        if(name.rfind("synth-",0)==0 && inner.active && inner.stereo)
                            throw std::runtime_error(name+": mono synth unnecessarily evaluates both channels: "+inner.processorId);
                }
            std::uint64_t differences=0,nonzero=0;float error=0;unsigned swaps=0;
            for(unsigned position=0;position<36000;){
                unsigned count=std::min(frames,36000-position);midi.at(position,count);
                if(swaps<3&&position>=8192*(swaps+1)){
                    int change=swaps==2?-1:static_cast<int>(swaps);
                    auto aa=make(0,change),bb=make(1,change),cc=make(2,change),dd=make(3,change);
                    aa.inheritState(a);bb.inheritState(b);cc.inheritState(c);dd.inheritState(copy);a=std::move(aa);b=std::move(bb);c=std::move(cc);copy=std::move(dd);swaps++;
                }
                render(a,count,midi,x);render(b,count,midi,y);render(c,count,midi,z);
                render(copy,count,midi,copied);sameMidi(a,copy);
                sameMidi(a,b);sameMidi(a,c);
                for(unsigned channel=0;channel<2;channel++)for(unsigned sample=0;sample<count;sample++){
                    auto i=channel*256+sample;
                    if(!std::isfinite(x[i])||!std::isfinite(y[i])||!std::isfinite(z[i])||!std::isfinite(copied[i]))throw std::runtime_error(name+": nonfinite sample");
                    error=std::max(error,std::max(std::abs(x[i]-y[i]),std::abs(x[i]-z[i])));
                    error=std::max(error,std::abs(x[i]-copied[i]));
                    differences+=std::memcmp(&x[i],&y[i],sizeof(float))!=0||std::memcmp(&x[i],&z[i],sizeof(float))!=0||std::memcmp(&x[i],&copied[i],sizeof(float))!=0;
                    nonzero+=std::abs(x[i])>1e-6f;
                    if(error>tolerance)throw std::runtime_error(name+": mismatch at sample "+std::to_string(position+sample)+", channel "+std::to_string(channel)+", frames "+std::to_string(frames)+", error "+std::to_string(error));
                }
                position+=count;
            }
            if(!nonzero)throw std::runtime_error(name+": silent fixture");
            equality<<name<<','<<frames<<",36000,"<<differences<<','<<error<<','<<nonzero<<','<<swaps<<'\n';equality.flush();
        }
        for(unsigned frames:{128u,256u})for(int repeat=0;repeat<4;repeat++)for(int order=0;order<4;order++){
            int mode=(order+repeat)%4;auto graph=make(mode);ReferenceMidi midi;std::array<float,512> output{};
            // Keep all voices held throughout the timing run. Warm-up is excluded.
            midi.at(0,256);render(graph,256,midi,output);midi.block.clear();
            for(int warm=0;warm<80;warm++)render(graph,frames,midi,output);
            std::vector<double> samples;samples.reserve(400);double sum=0;unsigned over=0;
            for(int block=0;block<400;block++){
                auto start=Clock::now();render(graph,frames,midi,output);auto end=Clock::now();
                double us=std::chrono::duration<double,std::micro>(end-start).count();samples.push_back(us);sum+=us;over+=us>frames*1e6/48000.;
            }
            std::sort(samples.begin(),samples.end());
            performance<<name<<','<<frames<<','<<repeat<<','<<(mode==0?"original":mode==1?"export":mode==2?"scheduled-export":"copy-export")<<",400,"<<sum/400<<','<<samples[395]<<','<<samples.back()<<','<<over<<','<<sampleBytes(graph)<<'\n';performance.flush();
        }
        std::cout<<name<<": equivalence and timing complete\n";
    }
};
}
