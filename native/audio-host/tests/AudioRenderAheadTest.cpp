#include "audio/AudioRenderAhead.hpp"
#include <cassert>
#include <functional>
#include <iostream>
using namespace easyknack;

struct Renderer {
    std::atomic<unsigned> frames{0};
    std::atomic<bool> entered{false},blocked{true};
    unsigned period=2048;
    static unsigned render(void* data,float* output,unsigned count)noexcept {
        auto& self=*static_cast<Renderer*>(data);self.entered.store(true);
        while(self.blocked.load())std::this_thread::yield();
        auto base=self.frames.load();
        for(unsigned i=0;i<count;i++){output[2*i]=float(base+i+1);output[2*i+1]=-float(base+i+1);}
        self.frames.fetch_add(count);return self.period;
    }
};
static void await(const std::function<bool()>& predicate){
    auto until=std::chrono::steady_clock::now()+std::chrono::seconds(5);
    while(!predicate()&&std::chrono::steady_clock::now()<until)std::this_thread::yield();
    assert(predicate());
}
int main(){
    AudioRenderAhead pump;Renderer render;
    pump.start(Renderer::render,&render);assert(!pump.active());
    pump.enable(render.period);auto reserve=pump.latencyFrames();assert(reserve>=render.period+512);
    std::array<float,512> output{};
    pump.read(output.data(),256);await([&]{return render.entered.load();});
    // Render is still blocked. The hardware-facing read must return independently.
    for(unsigned i=256;i<reserve;i+=256){pump.read(output.data(),256);for(float x:output)assert(x==0);}
    assert(pump.underruns()==0&&render.frames.load()==0);
    render.blocked.store(false);await([&]{return render.frames.load()>=reserve;});
    unsigned received=0;
    for(unsigned count:{1u,127u,128u,255u,17u,256u}){
        pump.read(output.data(),count);
        for(unsigned i=0;i<count;i++){assert(output[2*i]==float(received+i+1));assert(output[2*i+1]==-float(received+i+1));}
        received+=count;
    }
    assert(pump.underruns()==0&&pump.overruns()==0);pump.stop();
    // Restart discards old samples/demand, but preserves diagnostic counters.
    render.frames.store(0);render.entered.store(false);render.blocked.store(true);
    pump.start(Renderer::render,&render);pump.enable(1024);reserve=pump.latencyFrames();
    for(unsigned i=0;i<reserve/256;i++)pump.read(output.data(),256);
    pump.read(output.data(),256);assert(pump.underruns()==1);
    render.blocked.store(false);pump.stop();assert(!pump.active());
    std::cout<<"Render-ahead deadline isolation, order, fragments, restart and underruns passed\n";
}
