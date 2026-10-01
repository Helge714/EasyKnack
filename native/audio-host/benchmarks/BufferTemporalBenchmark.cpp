#include "nodes/HistoryBufferNode.hpp"
#include "nodes/HistoryReaderNode.hpp"
#include <array>
#include <chrono>
#include <iostream>
using namespace easyknack;
// Exact old block-snapshot reader, kept only as the benchmark baseline.
void legacy(HistoryReaderNode& r,ProcessContext& c){
    auto* h=static_cast<const HistoryResource*>(c.resource(0));auto size=h->samples.size();double length=static_cast<double>(size),write=h->end%size;
    if(!r.initialized||r.historyIdentity!=h->identity){r.historyIdentity=h->identity;r.cursor=HistoryReaderNode::wrap(write-h->blockFrames,length);r.initialized=true;}
    for(std::size_t i=0;i<c.bufferSize;i++){
        float rate=c.input(1)?c.input(1)[i]:r.speed,delay=c.input(2)?c.input(2)[i]:r.offset;
        double p=HistoryReaderNode::wrap(r.cursor-(std::isfinite(delay)?std::clamp(delay,0.f,1.f):0)*length,length);auto a=static_cast<std::size_t>(p),b=(a+1)%size;
        c.output[i]=h->samples[a]+(h->samples[b]-h->samples[a])*static_cast<float>(p-a);
        double d=std::abs(p-write);c.outputAt(1)[i]=static_cast<float>(2*std::min(d,length-d)/length);
        r.cursor=HistoryReaderNode::wrap(r.cursor+(std::isfinite(rate)?std::clamp(rate,-8.f,8.f):0),length);
    }
}
volatile float observed=0;
int main(){
    using Clock=std::chrono::steady_clock;
    constexpr unsigned frames=256,blocks=1000;
    std::cout<<"capacity,readers_per_channel,channels,repeat,mode,mean_us,p99_us,max_us\n";
    for(unsigned capacity:{256u,4096u,96000u})for(unsigned count:{1u,8u})for(unsigned repeat=0;repeat<3;repeat++)for(unsigned order=0;order<2;order++){
        bool temporal=(order+repeat)%2;std::array<HistoryBufferNode,2> writers;std::array<std::array<HistoryReaderNode,8>,2> readers;
        std::array<float,frames> input{},output{},distance{},silence{};float* outputs[]{output.data(),distance.data()};const float* inputs[]{nullptr,nullptr,nullptr};
        for(unsigned ch=0;ch<2;ch++){writers[ch].seconds=capacity/48000.;writers[ch].prepare(48000,frames);for(unsigned r=0;r<count;r++){readers[ch][r].speed=r%2?-.75f:1.5f;readers[ch][r].offset=.27f;}}
        std::array<double,blocks> timings{};
        for(unsigned block=0;block<blocks+200;block++){
            for(unsigned i=0;i<frames;i++)input[i]=static_cast<float>((block+i)%37)/37;
            auto start=Clock::now();
            for(unsigned ch=0;ch<2;ch++){
                auto& history=writers[ch].history;
                if(temporal)history.beginBlock(frames);else history.blockFrames=frames;
                for(float value:input)history.append(value);
                const DspResource* resources[]{&history,nullptr,nullptr};
                ProcessContext c{inputs,3,output.data(),frames,48000,silence.data(),frames};c.outputs=outputs;c.outputCount=2;c.resourceInputs=resources;
                for(unsigned r=0;r<count;r++){if(temporal)readers[ch][r].process(c);else legacy(readers[ch][r],c);observed=output[127]+distance[127];}
            }
            if(block>=200)timings[block-200]=std::chrono::duration<double,std::micro>(Clock::now()-start).count();
        }
        double sum=0;for(auto t:timings)sum+=t;std::sort(timings.begin(),timings.end());
        std::cout<<capacity<<','<<count<<",2,"<<repeat<<','<<(temporal?"sample-accurate":"previous")<<','<<sum/blocks<<','<<timings[989]<<','<<timings.back()<<'\n';
    }
}
