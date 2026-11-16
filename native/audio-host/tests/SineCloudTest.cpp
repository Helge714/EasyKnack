#include "nodes/SineCloudNode.hpp"
#include <cassert>
#include <cstdlib>
#include <new>
#include <sstream>
#include <iostream>
using namespace easyknack;
thread_local bool rendering=false;
void* operator new(std::size_t n){assert(!rendering);if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void* operator new[](std::size_t n){return ::operator new(n);}
void operator delete(void* p)noexcept{assert(!rendering);std::free(p);}
void operator delete[](void* p)noexcept{::operator delete(p);}
void operator delete(void* p,std::size_t)noexcept{::operator delete(p);}
void operator delete[](void* p,std::size_t)noexcept{::operator delete(p);}
int main(){
    // Mixed-radix output must match direct additive synthesis, not a rounded-bin approximation.
    constexpr double tau=6.283185307179586476925286766559;
    for(std::size_t n:{2,6,10,15,30,125}){
        std::vector<std::complex<double>> bins(n);
        for(std::size_t k=0;k<n;k++)bins[k]={std::sin(double(k)),std::cos(double(k)*.7)};
        auto samples=sineCloudInverse(bins);
        for(std::size_t i=0;i<n;i++){
            std::complex<double> expected=0;
            for(std::size_t k=0;k<n;k++)expected+=bins[k]*std::polar(1.,tau*k*i/n);
            assert(std::abs(samples[i]-expected/double(n))<1e-12);
        }
    }
    SineCloudTable a,b;
    SineCloudTable::Settings p{7,42,0,800,false,true,1};
    a.build(p,65536);b.build(p,65536);
    assert(a.count==7&&a.samples==b.samples&&a.samples.size()==16384);
    assert(std::abs(a.periodSeconds-.01)<1e-12);
    p.seed=43;b.build(p,65536);assert(a.samples!=b.samples);
    std::vector<std::complex<double>> bins(a.samples.begin(),a.samples.end());spectrumTransform(bins.data(),bins.size());
    double magnitude=std::abs(bins[25]);assert(magnitude>1);
    for(unsigned k=1;k<bins.size()/2;k++){
        if(k<=175&&k%25==0)assert(std::abs(std::abs(bins[k])/magnitude-1)<1e-5);
        else assert(std::abs(bins[k])<magnitude*1e-5);
    }
    p.randomPhases=false;a.build(p,65536);p.seed=900;b.build(p,65536);
    assert(a.samples==b.samples&&std::abs(a.samples[0])<1e-6);
    p={1000,42,100,102,false,true,1};a.build(p,65536);
    assert(a.count==3&&a.first==100&&a.last==102); // Narrow bands cap count, not grid spacing.
    p={3,42,100,301,false,true,1};a.build(p,65536);assert(a.periodSeconds==1);
    p={1,42,440,440,false,true,.01};
    auto start=std::chrono::steady_clock::now();a.build(p,48000);
    assert(a.samples.size()==1200&&a.count==1&&a.first==11);
    assert(std::abs(a.periodSeconds-1./440)<1e-12);
    float peak=0;for(float x:a.samples){assert(std::isfinite(x));peak=std::max(peak,std::abs(x));}assert(peak==1);
    // A non-power-of-two table reconstructs the desired additive sinusoid.
    double cosine=0,sine=0;
    for(std::size_t i=0;i<4800;i++){cosine+=a.samples[i%a.samples.size()]*std::cos(tau*440*i/48000);sine+=a.samples[i%a.samples.size()]*std::sin(tau*440*i/48000);}
    cosine/=2400;sine/=2400;
    for(std::size_t i=0;i<4800;i++)assert(std::abs(a.samples[i%a.samples.size()]-(cosine*std::cos(tau*440*i/48000)+sine*std::sin(tau*440*i/48000)))<2e-6);
    for(bool randomized:{false,true})for(unsigned seed:{0u,42u}){
        p={2,seed,0,1320,false,randomized,.01};a.build(p,48000);
        assert(a.count==2&&a.samples.size()==1200&&std::abs(a.periodSeconds-1./440)<1e-12);
        std::vector<double> reference(a.samples.size());std::uint64_t rng=seed;
        double phases[2];for(auto& phase:phases)phase=randomized?(SineCloudTable::random(rng)>>11)*0x1.0p-53*tau:-tau/4;
        double peak=0,power=0;
        for(std::size_t i=0;i<reference.size();i++){
            reference[i]=std::cos(tau*440*i/48000+phases[0])+std::cos(tau*880*i/48000+phases[1]);
            peak=std::max(peak,std::abs(reference[i]));
        }
        for(std::size_t i=0;i<reference.size();i++){assert(std::abs(a.samples[i]-reference[i]/peak)<1e-6);power+=a.samples[i]*a.samples[i];}
        assert(power/reference.size()>.2); // Two audible tones must not be silent.
    }
    a.build({2,0,20,20000,false,true,.01},48000);
    assert(a.count==2&&a.samples.size()==2400&&a.periodSeconds==.05);
    a.build({2,0,0,24000,false,true,.01},48000);
    assert(a.count==2&&a.samples.size()==6&&a.first==1&&a.last==2&&a.periodSeconds==1./8000);
    double twoTonePower=0;for(float x:a.samples)twoTonePower+=x*x;assert(twoTonePower/a.samples.size()>.2);
    p={100,42,20,20000,false,true,.01};start=std::chrono::steady_clock::now();b.build(p,48000);assert(b.samples.size()==4800000);
    std::cout<<"4.8M sample table build seconds="<<std::chrono::duration<double>(std::chrono::steady_clock::now()-start).count()<<'\n';
    auto wide=SineCloudTable::plan({10000000,0,20,20000,false,true,.01},48000);
    assert(wide.size==4800000&&wide.count==1998001);
    auto denser=SineCloudTable::plan({10000000,0,20,20000,false,true,.001},48000);
    assert(denser.size==48000000&&denser.count==10000000);
    auto odd=SineCloudTable::plan({1,0,88573.5,88573.5,false,true,1},177147);
    assert(odd.size==177147&&odd.last==88573);
    for(double rate:{44100.,48000.,96000.}){
        auto layout=SineCloudTable::plan({7,0,rate*.5-1000,rate,false,true,.01},rate);
        assert(rate/layout.size<=.01&&layout.last<layout.size/2&&layout.count==7);
    }
    bool rejected=false;try{SineCloudTable::plan({1,0,20,20000,false,true,.00001},48000);}catch(const std::length_error&){rejected=true;}assert(rejected);
    SineCloudNode node;std::istringstream encoded(SINE_CLOUD_PAYLOAD);node.configure(encoded);
    assert(node.sources.size()==4&&node.settings.binSpacing==.01);
    node.settings={7,42,0,800,false,true,1};node.prepare(65536,256);
    for(const char* payload:{"!2303724719fab0c8 -1 7 -1 20 -1 20000 -1 42","!19022bc74d228d07 -1 7 -1 20 -1 20000 -1 42 1","!46e3c55a54e309e8 -1 7 -1 20 -1 24000 -1 42 1 0"}){
        SineCloudNode legacy;std::istringstream old(payload);legacy.configure(old);assert(legacy.sources.size()==4&&legacy.settings.binSpacing==.01);
    }
    float output[256]{},count[256]{},period[256]{},silence[256]{};float* outputs[]={output,count,period};
    ProcessContext c{nullptr,0,output,256,65536,silence,256};c.outputs=outputs;c.outputCount=3;
    auto render=[&]{rendering=true;node.process(c);rendering=false;
        for(float x:output)assert(std::isfinite(x)&&std::abs(x)<=1.000001);
        double expected=node.active>=0?node.worker->slots[node.active].table.periodSeconds:0;
        for(float x:period)assert(std::abs(x-expected)<1e-6);
    };
    auto await=[&](unsigned wanted,std::uint64_t newer){
        for(unsigned attempts=0;attempts<2000;attempts++){
            render();if(node.active>=0&&!node.fading&&count[0]==wanted&&node.worker->slots[node.active].version>newer)return;
            std::this_thread::sleep_for(std::chrono::milliseconds(5));
        }assert(false&&"Sine Cloud build timed out");
    };
    {SineCloudNode unprepared;unprepared.process(c);assert(period[0]==0&&count[0]==0);}
    await(7,0);auto version=node.worker->slots[node.active].version;
    auto& table=node.worker->slots[node.active].table;node.cursor=table.samples.size()-128;render();
    for(unsigned i=0;i<256;i++)assert(output[i]==table.samples[(table.samples.size()-128+i)%table.samples.size()]);
    SineCloudNode configuration;configuration.settings=node.settings;configuration.settings.binSpacing=.5;
    node.applyParameters(configuration);await(7,version);assert(node.worker->slots[node.active].table.samples.size()==16384);
    version=node.worker->slots[node.active].version;configuration.settings.randomPhases=false;node.applyParameters(configuration);await(7,version);
    version=node.worker->slots[node.active].version;configuration.settings.seed=900;node.applyParameters(configuration);for(int j=0;j<20;j++)render();assert(node.worker->slots[node.active].version==version);
    float inputs[4]={1000,100,102,3};const float* pointers[]={inputs,inputs+1,inputs+2,inputs+3};char rates[]={'c','c','c','c'};
    c.inputs=pointers;c.inputCount=4;c.sourceRates=rates;await(5,version);
    SineCloudNode replacement;replacement.settings=node.settings;replacement.prepare(65536,256);
    auto* engine=node.worker.get();rendering=true;replacement.adoptState(node);rendering=false;assert(replacement.worker.get()==engine&&replacement.active>=0);
}
