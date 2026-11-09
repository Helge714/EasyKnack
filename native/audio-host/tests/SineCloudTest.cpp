#include "nodes/SineCloudNode.hpp"
#include <cassert>
#include <cstdlib>
#include <new>
#include <sstream>
using namespace easyknack;
thread_local bool rendering=false;
void* operator new(std::size_t n){assert(!rendering);if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void* operator new[](std::size_t n){return ::operator new(n);}
void operator delete(void* p)noexcept{assert(!rendering);std::free(p);}
void operator delete[](void* p)noexcept{::operator delete(p);}
void operator delete(void* p,std::size_t)noexcept{::operator delete(p);}
void operator delete[](void* p,std::size_t)noexcept{::operator delete(p);}
int main(){
    SineCloudTable a,b;
    a.build({7,42,100,700},65536);b.build({7,42,100,700},65536);
    assert(a.count==7&&a.samples==b.samples&&a.samples.size()==65536);
    b.build({7,43,100,700},65536);assert(a.samples!=b.samples);
    std::vector<std::complex<double>> bins(a.samples.begin(),a.samples.end());
    spectrumTransform(bins.data(),bins.size());
    double magnitude=std::abs(bins[100]);assert(magnitude>1);
    for(unsigned k=1;k<bins.size()/2;k++){
        if(k<=700&&k%100==0)assert(std::abs(std::abs(bins[k])/magnitude-1)<1e-5);
        else assert(std::abs(bins[k])<magnitude*1e-5);
    }
    float peak=0;for(float x:a.samples){assert(std::isfinite(x));peak=std::max(peak,std::abs(x));}assert(peak==1);
    a.build({1000000,0,20,20000},48000);assert(a.count==1000000&&a.samples.size()==4194304);
    for(float x:a.samples)assert(std::isfinite(x)&&std::abs(x)<=1);
    auto narrow=SineCloudTable::plan({10000000,0,100,100.01,true},48000);assert(narrow.count<20&&narrow.count>=1);
    a.build({100,0,24000,50000},48000);assert(a.last<a.samples.size()/2&&a.count==1);
    a.build({1,0,440,440},48000);assert(a.count==1&&a.first==a.last);
    a.build({7,42,700,100},65536);b.build({7,42,100,700},65536);assert(a.samples==b.samples);

    a.build({1001,42,1000,10000,true},65536);
    bins.assign(a.samples.begin(),a.samples.end());spectrumTransform(bins.data(),bins.size());
    unsigned lines=0,central=0;double minimum=1e100,maximum=0;
    for(unsigned k=1;k<bins.size()/2;k++)if(std::abs(bins[k])>1){
        ++lines;double mag=std::abs(bins[k]);minimum=std::min(minimum,mag);maximum=std::max(maximum,mag);
        if(k>=4000&&k<=7000)++central;
    }
    assert(lines==1001&&central>675&&central<695&&maximum/minimum<1.00001);
    assert(std::abs(SineCloudTable::gaussianPosition(.5)-.5)<1e-10);
    auto linear=SineCloudTable::plan({10000000,0,20,20000,false},48000);
    assert(linear.count==10000000&&linear.size==33554432);
    a.build({10000000,0,20,20000,true},48000);assert(a.count==10000000&&a.samples.size()==67108864);
    for(float x:a.samples)assert(std::isfinite(x)&&std::abs(x)<=1);

    SineCloudNode node;std::istringstream encoded(SINE_CLOUD_PAYLOAD);node.configure(encoded);
    assert(node.sources.size()==4);node.settings.count=7;node.prepare(48000,256);
    {SineCloudNode legacy;std::istringstream old("!2303724719fab0c8 -1 7 -1 20 -1 20000 -1 42");legacy.configure(old);assert(legacy.sources.size()==4&&!legacy.settings.gaussian&&legacy.settings.seed==42);}
    float output[256]{},count[256]{},silence[256]{};float* outputs[]={output,count};
    ProcessContext c{nullptr,0,output,256,48000,silence,256};c.outputs=outputs;c.outputCount=2;
    auto render=[&]{rendering=true;node.process(c);rendering=false;for(float x:output)assert(std::isfinite(x)&&std::abs(x)<=1.000001);};
    auto await=[&](unsigned wanted,std::uint64_t newer){
        for(unsigned attempts=0;attempts<2000;attempts++){
            render();if(node.active>=0&&!node.fading&&count[0]==wanted&&node.worker->slots[node.active].version>newer)return;
            std::this_thread::sleep_for(std::chrono::milliseconds(5));
        }
        assert(false&&"Sine Cloud build timed out");
    };
    await(7,0);auto version=node.worker->slots[node.active].version;
    // Playback reads the exact immutable table, including across its loop boundary.
    auto& table=node.worker->slots[node.active].table;
    node.cursor=table.samples.size()-128;render();
    for(unsigned i=0;i<256;i++)assert(output[i]==table.samples[(table.samples.size()-128+i)%table.samples.size()]);
    SineCloudNode configuration;configuration.settings=node.settings;configuration.settings.seed=8;
    configuration.settings.gaussian=true;
    rendering=true;node.applyParameters(configuration);rendering=false;await(7,version);
    // Rapid controls coalesce; the last requested table is eventually audible.
    float inputs[4]={31,50,10000,3};const float* pointers[]={inputs,inputs+1,inputs+2,inputs+3};char rates[]={'c','c','c','c'};
    c.inputs=pointers;c.inputCount=4;c.sourceRates=rates;
    for(unsigned j=0;j<20;j++){inputs[0]=31+j;render();}
    await(50,0);
    SineCloudNode replacement;replacement.settings=node.settings;replacement.prepare(48000,256);
    auto* engine=node.worker.get();rendering=true;replacement.adoptState(node);rendering=false;
    assert(replacement.worker.get()==engine&&replacement.active>=0);
}
