#pragma once
#include <chrono>
#include <cstring>
#include <fstream>
#include <iostream>
#include <optional>
// Included by the generated test after ExportChain, LegacyChain and BlockChain.
namespace easyknack {
// A manually written one-node implementation, with no internal graph at render
// time. Configuration borrows the generated parser, not its processing kernel.
struct HandwrittenChain:NodeProcessor {
    std::optional<OscillatorNode> osc;
    std::optional<MathNode> gain;
    std::optional<FilterNode> filter;
    std::array<float,256> a{},b{};
    void configure(std::istream& input)override{
        ExportChain definition;definition.configure(input);
        auto& nodes=definition.runtime->graph.nodes;
        osc.emplace(static_cast<OscillatorNode&>(*nodes[0].processor));
        gain.emplace(static_cast<MathNode&>(*nodes[1].processor));
        filter.emplace(static_cast<FilterNode&>(*nodes[2].processor));
    }
    bool canShareChannels()const noexcept override{return true;}
    void inheritState(const NodeProcessor& old)noexcept override{
        const auto& previous=static_cast<const HandwrittenChain&>(old);
        osc->inheritState(*previous.osc);gain->inheritState(*previous.gain);filter->inheritState(*previous.filter);
    }
    void process(ProcessContext& c)noexcept override{
#if defined(__clang__)
        [[clang::always_inline]]
#endif
        osc->render(a.data(),c.bufferSize,osc->frequency,c.sampleRate,nullptr,nullptr,true);
        const float smoothing=static_cast<float>(1-std::exp(-1.0/(.005*c.sampleRate)));
        for(std::size_t i=0;i<c.bufferSize;i++){
            gain->currentB+=smoothing*(gain->bValue-gain->currentB);
            b[i]=MathNode::evaluate(a[i],gain->currentB,MathOperation::multiply,false);
        }
        const float* inputs[]={b.data(),nullptr,nullptr};
        ProcessContext f{inputs,3,c.output,c.bufferSize,c.sampleRate,nullptr};
#if defined(__clang__)
        [[clang::always_inline]]
#endif
        filter->FilterNode::process(f);
    }
};
// Same hand-written reference, additionally fusing the loop with block-prepared
// coefficients. This keeps the comparison fair after the exporter gains fusion.
struct HandwrittenFused:HandwrittenChain {
    void process(ProcessContext& c)noexcept override{
        const double prepared=1-std::exp(-1.0/(.005*c.sampleRate));
        const float smoothing=static_cast<float>(prepared);
        for(std::size_t i=0;i<c.bufferSize;i++){
            float value;
#if defined(__clang__)
            [[clang::always_inline]]
#endif
            osc->renderPrepared(&value,1,osc->frequency,c.sampleRate,nullptr,nullptr,true,prepared);
            gain->currentB+=smoothing*(gain->bValue-gain->currentB);
            value=MathNode::evaluate(value,gain->currentB,MathOperation::multiply,false);
            const float* inputs[]={&value,nullptr,nullptr};
            ProcessContext f{inputs,3,c.output+i,1,c.sampleRate,nullptr};
#if defined(__clang__)
            [[clang::always_inline]]
#endif
            filter->processPrepared(f,prepared);
        }
    }
};
inline void changeChain(AudioGraph& graph,int step){
    for(auto& n:graph.nodes){
        if(auto* baked=dynamic_cast<BakedGraphProcessor*>(n.processor.get()))changeChain(baked->runtime->graph,step);
        if(auto* hand=dynamic_cast<HandwrittenChain*>(n.processor.get())){
            hand->osc->frequency=110+step*23;hand->osc->waveform=step%4;
            hand->gain->bValue=.13f*(step%7);hand->filter->cutoff=100+step*71;hand->filter->mode=step%3;
        }
        if(auto* osc=dynamic_cast<OscillatorNode*>(n.processor.get())){osc->frequency=110+step*23;osc->waveform=step%4;}
        if(auto* gain=dynamic_cast<MathNode*>(n.processor.get());gain&&n.processor->sources[0]>=0){
            // Only the internal gain, not the common root monitor.
            if(graph.nodes[gain->sources[0]%maxNodes].processorId=="osc")gain->bValue=.13f*(step%7);
        }
        if(auto* filter=dynamic_cast<FilterNode*>(n.processor.get())){filter->cutoff=100+step*71;filter->mode=step%3;}
    }
}
inline void chainRender(AudioGraph& graph,int frames,std::array<float,512>& output){
    graph.process(output.data(),frames,0,nullptr);graph.process(output.data()+256,frames,1,nullptr);
}
inline void compareSpecializedChains(const std::string& original,const std::string& plugin,const std::string& id,const std::string& path){
    auto make=[&](int mode){return mode==0?AudioGraph::parse(original):AudioGraph::compile(plugin,[&](const std::string& type)->std::unique_ptr<NodeProcessor>{
        if(type!=id)return createNodeProcessor(type);
        if(mode==1)return std::make_unique<LegacyChain>();
        if(mode==2)return std::make_unique<BlockChain>();
        if(mode==3)return std::make_unique<ExportChain>();
        if(mode==4)return std::make_unique<HandwrittenChain>();
        return std::make_unique<HandwrittenFused>();
    });};
    // A runtime mismatch must select the ordinary plan before touching state.
    for(int variant=0;variant<2;variant++){
        auto a=make(1),b=make(3);
        for(auto* graph:{&a,&b})for(auto& n:graph->nodes)
            if(auto* chain=dynamic_cast<BakedGraphProcessor*>(n.processor.get())){
                auto& gain=static_cast<MathNode&>(*chain->runtime->graph.nodes[1].processor);
                if(variant==0)gain.clamp=true;else gain.operation=MathOperation::add;
                if(auto* specialized=dynamic_cast<ExportChain*>(n.processor.get()))
                    if(specialized->renderSpecializedChain(17))throw std::runtime_error("Missing chain fallback");
            }
        std::array<float,512> x{},y{};
        for(int i=0;i<50;i++){
            chainRender(a,256,x);chainRender(b,256,y);
            if(std::memcmp(x.data(),y.data(),sizeof(x)))throw std::runtime_error("Chain fallback changed output");
        }
    }
    for(int frames:{1,17,128,256}){
        std::vector<AudioGraph> graphs;for(int mode=0;mode<6;mode++)graphs.push_back(make(mode));
        std::array<float,512> reference{},actual{};
        for(int block=0;block<160;block++){
            if(block%13==0)for(auto& graph:graphs)changeChain(graph,block/13);
            if(block==70)for(int mode=0;mode<6;mode++){auto replacement=make(mode);replacement.inheritState(graphs[mode]);graphs[mode]=std::move(replacement);}
            chainRender(graphs[0],frames,reference);
            for(int mode=1;mode<6;mode++){
                chainRender(graphs[mode],frames,actual);
                for(int channel=0;channel<2;channel++)for(int i=0;i<frames;i++){
                    int sample=channel*256+i;
                    if(!std::isfinite(actual[sample])||std::memcmp(&reference[sample],&actual[sample],sizeof(float)))
                        throw std::runtime_error("Chain mismatch mode="+std::to_string(mode)+" frames="+std::to_string(frames)+" block="+std::to_string(block));
                }
            }
        }
    }
    std::cout<<"20 full-graph comparisons bitwise equal, with parameter and state changes\n";
    std::ofstream out(path);out<<"frames,repeat,mode,mean_us,p99_us,max_us\n";
    const char* names[]={"original","previous-export","specialized-blocks","specialized-fused","handwritten","handwritten-fused"};
    volatile double checksum=0;
    for(int frames:{128,256})for(int repeat=0;repeat<12;repeat++)for(int order=0;order<6;order++){
        int mode=(repeat+order)%6;auto graph=make(mode);std::array<float,512> samples{};
        for(int i=0;i<100;i++)chainRender(graph,frames,samples);
        std::vector<double> times;double sum=0;
        for(int i=0;i<1500;i++){
            auto begin=std::chrono::steady_clock::now();chainRender(graph,frames,samples);auto end=std::chrono::steady_clock::now();
            double us=std::chrono::duration<double,std::micro>(end-begin).count();times.push_back(us);sum+=us;checksum+=samples[i%frames];
        }
        std::sort(times.begin(),times.end());out<<frames<<','<<repeat<<','<<names[mode]<<','<<sum/1500<<','<<times[1484]<<','<<times.back()<<'\n';
    }
}
}
