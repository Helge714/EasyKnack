#include "AudioGraph.hpp"
#include "nodes/PartialAnalysisNode.hpp"
#include "nodes/PartialGraphNode.hpp"
#include "nodes/PartialsSpectrumNode.hpp"
#include "nodes/ResidualModelNode.hpp"
#include "BakedGraph.hpp"
#include "PartialsTestParameters.hpp"
#include "../third_party/loris/Analyzer.h"
#include "../third_party/loris/KaiserWindow.h"
#include <cassert>
#include <iostream>
#include <set>
using namespace easyknack;
static thread_local bool watching=false;
static thread_local unsigned allocations=0;
void* operator new(std::size_t n){if(watching)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void operator delete(void* p)noexcept{std::free(p);}void operator delete(void* p,std::size_t)noexcept{std::free(p);}
static const Partial* find(const PartialAnalysisNode& a,double hz){const auto& f=a.partials.frames[0];for(std::size_t i=0;i<f.count;i++)if(std::abs(f.values[i].frequency-hz)<1)return &f.values[i];return nullptr;}
static void reference(){
    // Compare the streaming bridge with the unmodified upstream Analyzer, not
    // another EasyKnack detector. Disable offline end-of-file phase correction.
    constexpr std::size_t n=2048,hop=512,frames=24;constexpr double rate=48000;
    std::vector<double> raw(n+frames*hop);
    for(std::size_t i=0;i<raw.size();i++)raw[i]=.7*std::cos(partialTau*440*i/rate+.3)+.2*std::cos(partialTau*997*i/rate+.1);
    for(int model:{0,1,2}){
        Loris::Analyzer original(2*rate/n);
        double shape=Loris::KaiserWindow::computeShape(90);
        original.setWindowWidth(2*std::sqrt(1+std::pow(shape/3.14159265358979323846,2))*rate/(n-2));
        original.setHopTime(hop/rate);original.setAmpFloor(-60);original.setFreqFloor(20);original.setPhaseCorrect(false);
        if(model==0)original.storeNoBandwidth();else if(model==1)original.storeResidueBandwidth(2000);else original.storeConvergenceBandwidth(.1);
        auto expected=original.analyze(raw,rate);
        SpectrumStream input;input.prepare(256);input.size=2*n;input.windowSize=n;input.hop=hop;input.sampleRate=rate;input.identity=10;input.count=1;
        PartialAnalysisNode node;node.settings.duration=0;node.settings.bandwidth=model;node.prepare(rate,256);
        const DspResource* resources[]={&input};float silence[256]{},output[256]{};
        ProcessContext c{};c.resourceInputs=resources;c.inputCount=1;c.bufferSize=256;c.sampleRate=rate;c.silence=silence;c.output=output;
        std::uint64_t id=0;double maxFrequencyError=0,maxAmplitudeError=0;
        for(std::size_t t=0;t<frames;t++){
            auto& f=input.frames[0];f.start=t*hop;input.blockStart=f.start+n-256;
            std::fill_n(f.bins.data(),input.size,std::complex<double>{});
            for(std::size_t i=0;i<n;i++)f.bins[i]=raw[f.start+i]*spectrumWindowValue(input.window,i,n);
            spectrumTransform(f.bins.data(),input.size);
            watching=true;node.process(c);watching=false;assert(!allocations);
            auto p=find(node,440);assert(p);if(id)assert(p->id==id);id=p->id;
            assert(p->tonality>=0&&p->tonality<=1);
            if(t>3&&t+3<frames){
                auto best=expected.end();double error=100;
                const double time=(f.start+n/2.)/rate;
                for(auto it=expected.begin();it!=expected.end();++it)if(it->startTime()<=time&&it->endTime()>=time&&std::abs(it->frequencyAt(time)-440)<error){error=std::abs(it->frequencyAt(time)-440);best=it;}
                assert(best!=expected.end());
                maxFrequencyError=std::max(maxFrequencyError,std::abs(p->frequency-best->frequencyAt(time)));
                const double amplitude=best->amplitudeAt(time)*std::sqrt(std::max(0.,1-best->bandwidthAt(time)));
                maxAmplitudeError=std::max(maxAmplitudeError,std::abs(p->amplitude-amplitude));
                assert(maxFrequencyError<.05&&maxAmplitudeError<.002);
            }
        }
        auto previous=find(node,440)->id;
        node.settings.threshold=-55;node.process(c);assert(find(node,440)->id>previous);
        node.settings.minimum=24000;node.process(c);assert(!node.partials.frames[0].count);
        std::cout<<"Upstream model "<<model<<": frequency error "<<maxFrequencyError<<" Hz; amplitude error "<<maxAmplitudeError<<"\n";
    }
}
static void capacityAndResidual(){
    constexpr std::size_t n=32768,block=256;
    auto graph=AudioGraph::parse("GRAPH 2 a partialanalysis "+std::string(wideAnalysisParameters)+" @ cc c b partialgraph "+std::string(identityParameters)+" @ c c");
    assert(graph.resourceCapacityHint(ResourceKind::partials)==400);graph.prepare(48000,block);
    assert(static_cast<PartialGraphNode&>(*graph.nodes[1].processor).voices.size()>=400);
    ResourcePreparationScope capacity(ResourceKind::partials,400);
    PartialAnalysisNode wide;wide.limit=400;wide.settings.threshold=-80;wide.settings.duration=0;wide.settings.bandwidth=0;wide.prepare(48000,block);
    SpectrumStream input;input.prepare(block);input.size=2*n;input.windowSize=n;input.hop=1024;input.sampleRate=48000;input.identity=22;input.count=1;
    std::fill_n(input.frames[0].bins.data(),input.size,std::complex<double>{});
    for(std::size_t i=0;i<n;i++){double v=0;for(unsigned h=1;h<=300;h++)v+=.002*std::cos(partialTau*(60*h)*i/48000);input.frames[0].bins[i]=v*spectrumWindowValue(input.window,i,n);}
    spectrumTransform(input.frames[0].bins.data(),input.size);
    const DspResource* resources[]={&input};float silence[block]{},output[block]{};
    ProcessContext c{};c.resourceInputs=resources;c.inputCount=1;c.bufferSize=block;c.sampleRate=48000;c.silence=silence;c.output=output;
    watching=true;wide.process(c);watching=false;assert(!allocations);assert(wide.partials.frames[0].count==300);
    std::set<std::size_t> slots;for(std::size_t i=0;i<wide.partials.frames[0].count;i++)assert(slots.insert(wide.partials.frames[0].values[i].slot).second);
    PartialsSpectrumNode synth;synth.prepare(48000,block);resources[0]=&wide.partials;synth.process(c);
    for(std::size_t k=0;k<input.size;k++)assert(std::abs(synth.stream.frames[0].bins[k]+wide.residual.frames[0].bins[k]-input.frames[0].bins[k])<1e-7);
    ResidualModelNode residual;residual.prepare(48000,block);resources[0]=&input;watching=true;residual.process(c);watching=false;assert(!allocations);
    double before=ResidualModelNode::power(input.frames[0].bins.data(),input.size),after=ResidualModelNode::power(residual.stream.frames[0].bins.data(),input.size);assert(std::abs(after/before-1)<1e-10);
    std::cout<<"300 Loris partials, capacity propagation and stochastic residual passed\n";
}
static void legacyContracts(){
    for(auto text:{"!fa4a30260a8836d4 -1 -80 400 0 24000 100",
                   "!944a705e1b770686 -1 -60 64 20 20000 100 .8 3 30 .7 20 20 0 -1 0 30 40 2000",
                   "!23333a42ca0bbca2 -1 -60 64 20 20000 100 1 30 20 20 0 -1 0 30 40 2000"}){
        PartialAnalysisNode node;std::istringstream config(text);node.configure(config);
        std::string extra;assert(!(config>>extra));assert(!node.sources.empty());
        assert(node.settings.threshold<=-60&&node.limit>=64);
        assert(node.settings.resolution==0&&node.settings.drift==0);
    }
}
int main(){legacyContracts();reference();capacityAndResidual();}
