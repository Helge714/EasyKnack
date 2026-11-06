#include "AudioGraph.hpp"
#include "nodes/PartialAnalysisNode.hpp"
#include "nodes/PartialGraphNode.hpp"
#include "nodes/PartialsSpectrumNode.hpp"
#include "nodes/ResidualModelNode.hpp"
#include "BakedGraph.hpp"
#include "PartialsTestParameters.hpp"
#include <cassert>
#include <iostream>
#include <cstdlib>
#include <new>
using namespace easyknack;
static bool watching=false;static unsigned allocations=0;
void* operator new(std::size_t n){if(watching)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void operator delete(void* p)noexcept{std::free(p);}void operator delete(void* p,std::size_t)noexcept{std::free(p);}
static void frame(SpectrumStream& input,unsigned tick,bool tonal,bool silent=false){
    input.count=1;input.blockStart=tick*256;auto& f=input.frames[0];f.start=tick*256;
    for(unsigned i=0;i<input.windowSize;i++){
        double pos=double(f.start+i),noise=(double(partialHash(std::uint64_t(pos))>>40)/8388608.-1)*.1;
        double v=tonal?std::cos(partialTau*375*pos/48000)+.25*std::cos(partialTau*1125*pos/48000)+.15*std::cos(partialTau*1680*pos/48000)+noise:noise;
        f.bins[i]=(silent?0:v)*spectrumWindowValue(input.window,i,input.windowSize);
    }
    spectrumTransform(f.bins.data(),input.size);
}
static const Partial* find(const PartialAnalysisNode& a,double hz){auto& f=a.partials.frames[0];for(unsigned i=0;i<f.count;i++)if(std::abs(f.values[i].frequency-hz)<3)return &f.values[i];return nullptr;}
static void resolvedNeighbours(){
    // Independent known-signal oracle; the offline SMS comparison is optional.
    // Previously only the loudest of these eight resolved components survived
    // at 2048 samples, because neighbours contaminated the wide shape test.
    // Match the production FFT's twofold zero padding. The existing pipeline
    // tests separately cover reconstruction with and without padding.
    for(unsigned length:{2048u,4096u,8192u}){
        constexpr unsigned padding=2;
        SpectrumStream input;input.prepare(256);input.size=length*padding;input.windowSize=length;input.hop=length/4;input.sampleRate=48000;input.identity=1;input.window=SpectrumWindow::hann;input.count=1;
        PartialAnalysisNode node;node.prepare(48000,256);float silence[256]{},output[256]{};const DspResource* resource[]={&input,nullptr};
        ProcessContext c{};c.resourceInputs=resource;c.inputCount=2;c.bufferSize=256;c.sampleRate=48000;c.silence=silence;c.output=output;
        double residual=0,power=0;
        watching=true;
        for(unsigned t=0;t<24;t++){
            auto& f=input.frames[0];f.start=t*input.hop;input.blockStart=f.start+length-256;std::fill_n(f.bins.data(),input.size,std::complex<double>{});
            for(unsigned i=0;i<length;i++){double v=0;for(unsigned h=1;h<=8;h++)v+=.4/h*std::cos(partialTau*110*h*(f.start+i)/48000+.37);f.bins[i]=v*spectrumWindowValue(input.window,i,length);}
            spectrumTransform(f.bins.data(),input.size);node.process(c);
            if(t>8){for(unsigned h=1;h<=8;h++){auto p=find(node,110*h);assert(p&&std::abs(p->amplitude-.4/h)<.01);}
                for(unsigned k=0;k<input.size;k++){power+=std::norm(f.bins[k]);residual+=std::norm(node.residual.frames[0].bins[k]);}}
        }
        watching=false;assert(!allocations);assert(residual/power<.002);
    }
}
static void modulatedTracks(){
    constexpr unsigned length=8192,hop=length/4;
    SpectrumStream input;input.prepare(256);input.size=2*length;input.windowSize=length;input.hop=hop;input.sampleRate=48000;input.identity=33;input.window=SpectrumWindow::hann;input.count=1;
    PartialAnalysisNode node;node.prepare(48000,256);float silence[256]{},output[256]{};const DspResource* resource[]={&input,nullptr};
    ProcessContext c{};c.resourceInputs=resource;c.inputCount=2;c.bufferSize=256;c.sampleRate=48000;c.silence=silence;c.output=output;
    unsigned hits=0,total=0;watching=true;
    for(unsigned t=0;t<65;t++){
        auto& f=input.frames[0];f.start=t*hop;input.blockStart=f.start+length-256;std::fill_n(f.bins.data(),input.size,std::complex<double>{});
        for(unsigned i=0;i<length;i++){
            double time=double(f.start+i)/48000,v=0;
            for(unsigned h=1;h<=4;h++)v+=.4/h*std::cos(partialTau*110*h*time+110*h*.02/5*(1-std::cos(partialTau*5*time))+.37);
            f.bins[i]=v*spectrumWindowValue(input.window,i,length);
        }
        spectrumTransform(f.bins.data(),input.size);node.process(c);
        if(t>=2&&t<=62)for(unsigned h=1;h<=4;h++){
            double expected=110*h*(1+.02*std::sin(partialTau*5*(f.start+length/2.)/48000));bool found=false;
            for(unsigned j=0;j<node.partials.frames[0].count;j++)if(std::abs(node.partials.frames[0].values[j].frequency-expected)<=std::max(2.,expected*.01))found=true;
            hits+=found;++total;
        }
    }
    watching=false;assert(!allocations);assert(double(hits)/total>.85);
}
int main(){
    resolvedNeighbours();modulatedTracks();
    constexpr unsigned block=256;float output[block]{},silence[block]{};SpectrumStream input;input.prepare(block);input.size=input.windowSize=4096;input.hop=256;input.sampleRate=48000;input.identity=21;input.window=SpectrumWindow::hann;input.blockFrames=block;
    PartialAnalysisNode analysis;analysis.prepare(48000,block);PartialAnalysisNode raw;raw.sinusoidality=raw.prominence=raw.confidence=raw.duration=0;raw.prepare(48000,block);
    const DspResource* resources[]={&input,nullptr};ProcessContext c{};c.resourceInputs=resources;c.inputCount=2;c.bufferSize=block;c.sampleRate=48000;c.silence=silence;c.output=output;
    std::uint64_t id=0;double noiseCount=0,rawCount=0;
    watching=true;
    for(unsigned t=0;t<28;t++){
        frame(input,t,true);analysis.process(c);
        for(std::size_t i=1;i<analysis.partials.frames[0].count;i++)assert(analysis.partials.frames[0].values[i-1].amplitude>=analysis.partials.frames[0].values[i].amplitude);
        if(t>8){assert(find(analysis,375)&&find(analysis,1125)&&find(analysis,1680));assert(find(analysis,375)->tonality>=analysis.confidence);if(id)assert(id==find(analysis,375)->id);id=find(analysis,375)->id;}
    }
    frame(input,28,true,true);analysis.process(c);assert(!analysis.partials.frames[0].count);
    frame(input,29,true);analysis.process(c);assert(find(analysis,375)&&find(analysis,375)->id==id);
    for(unsigned t=30;t<39;t++){frame(input,t,true,true);analysis.process(c);}
    for(unsigned t=39;t<45;t++){frame(input,t,true);analysis.process(c);}assert(find(analysis,375)&&find(analysis,375)->id!=id);
    analysis.harmonic=true;analysis.fundamental=375;frame(input,45,true);analysis.process(c);assert(find(analysis,375)&&find(analysis,1125)&&!find(analysis,1680));
    analysis.fundamental=0;frame(input,46,true);analysis.process(c);assert(find(analysis,375)&&!find(analysis,1680));
    analysis.harmonic=false;input.identity++;
    for(unsigned t=0;t<80;t++){frame(input,t,false);analysis.process(c);raw.process(c);if(t>10){noiseCount+=analysis.partials.frames[0].count;rawCount+=raw.partials.frames[0].count;}}
    std::cout<<"Noise tracks classified/raw: "<<noiseCount<<" / "<<rawCount<<"\n";assert(noiseCount<rawCount*.25);
    watching=false;assert(!allocations);
    // The scheduler propagates the configured capacity, including nested graph preparations.
    std::string plan="GRAPH 2 a partialanalysis !fa4a30260a8836d4 -1 -80 400 0 24000 100 @ cc c b partialgraph "+std::string(identityParameters)+" @ c c";
    auto graph=AudioGraph::parse(plan);assert(graph.resourceCapacityHint(ResourceKind::partials)==400);graph.prepare(48000,block);
    auto& group=static_cast<PartialGraphNode&>(*graph.nodes[1].processor);assert(group.voices.size()>=400);
    struct Nested : BakedGraphProcessor {void configure(std::istream&)override{}void renderGraph(int,AudioIO*,MidiSource*)noexcept override{}} nested;
    auto factory=[](const std::string& id)->std::unique_ptr<NodeProcessor>{if(id=="baked_output")return std::make_unique<BakedOutput>();return createNodeProcessor(id);};
    auto nestedPlan=plan;nestedPlan.replace(0,7,"GRAPH 3");nestedPlan+=" z baked_output 1 8 @ c c";
    nested.configureGraph(nestedPlan,factory,{}, {2},{});assert(nested.resourceCapacityHint(ResourceKind::partials)==400);nested.prepare(48000,block);
    assert(static_cast<PartialGraphNode&>(*nested.runtime->graph.nodes[1].processor).voices.size()>=400);
    ResourcePreparationScope capacity(ResourceKind::partials,400);PartialAnalysisNode wide;wide.limit=400;wide.threshold=-80;wide.minimum=0;wide.maximum=24000;wide.sinusoidality=wide.prominence=wide.confidence=wide.duration=0;wide.prepare(48000,block);
    PartialsSpectrumNode synth;synth.prepare(48000,block);input.identity++;input.count=1;input.frames[0].start=0;input.blockStart=0;input.window=SpectrumWindow::rectangular;
    for(unsigned i=0;i<4096;i++){double v=0;for(unsigned k=1;k<=300;k++)v+=.002*std::cos(partialTau*(2*k)*i/4096.);input.frames[0].bins[i]=v;}spectrumTransform(input.frames[0].bins.data(),4096);
    watching=true;wide.process(c);assert(wide.partials.frames[0].count==300);resources[0]=&wide.partials;group.process(c);assert(group.stream.frames[0].count==300);resources[0]=&group.stream;synth.process(c);
    for(unsigned k=0;k<4096;k++)assert(std::abs(synth.stream.frames[0].bins[k]+wide.residual.frames[0].bins[k]-input.frames[0].bins[k])<1e-7);
    watching=false;assert(!allocations);
    ResidualModelNode model;model.prepare(48000,block);resources[0]=&input;watching=true;model.process(c);
    assert(model.stream.count==1&&model.stream.frames[0].start==input.frames[0].start);
    double before=ResidualModelNode::power(input.frames[0].bins.data(),4096),after=ResidualModelNode::power(model.stream.frames[0].bins.data(),4096);assert(std::abs(after/before-1)<1e-10);
    for(unsigned k=1;k<2048;k++)assert(std::abs(model.stream.frames[0].bins[4096-k]-std::conj(model.stream.frames[0].bins[k]))<1e-10);
    for(auto window:{SpectrumWindow::hann,SpectrumWindow::hamming,SpectrumWindow::blackman}){
        input.window=window;input.identity++;input.dcRemoved=true;frame(input,60,true);input.frames[0].bins[0]=0;
        model.process(c);before=ResidualModelNode::power(input.frames[0].bins.data(),4096);after=ResidualModelNode::power(model.stream.frames[0].bins.data(),4096);assert(std::abs(after/before-1)<1e-10);assert(model.stream.frames[0].bins[0]==std::complex<double>{});
    }
    model.time=50;frame(input,61,true,true);model.process(c);double held=ResidualModelNode::power(model.stream.frames[0].bins.data(),4096);assert(held>0&&held<before);
    auto repeated=model.stream.frames[0].bins[7];model.process(c);assert(std::abs(repeated-model.stream.frames[0].bins[7])<1e-10);
    watching=false;assert(!allocations);
    std::cout<<"Shape classification, coherent tracks, gap recovery, harmonics, 300 partials, preparation budgets and stochastic residual passed\n";
}
