#include "nodes/PartialAnalysisNode.hpp"
#include "nodes/PartialGraphNode.hpp"
#include "nodes/PartialsSpectrumNode.hpp"
#include "nodes/SpectrumMathNode.hpp"
#include "nodes/IFFTNode.hpp"
#include "PartialsTestParameters.hpp"
#include <cassert>
#include <iostream>
using namespace easyknack;
static thread_local bool watching=false;
static thread_local unsigned allocations=0;
void* operator new(std::size_t n){if(watching)++allocations;if(auto* p=std::malloc(n?n:1))return p;throw std::bad_alloc();}
void operator delete(void* p)noexcept{std::free(p);}
void operator delete(void* p,std::size_t)noexcept{std::free(p);}

int main(){
    constexpr std::size_t block=256;std::array<float,block> a{},b{},silence{};
    for(auto window:{SpectrumWindow::rectangular,SpectrumWindow::hann,SpectrumWindow::hamming,SpectrumWindow::blackman})for(unsigned padding:{1,2}){
        SpectrumStream input;input.prepare(block);input.size=1024*padding;input.windowSize=1024;input.hop=block;input.sampleRate=48000;input.identity=42;input.window=window;input.blockFrames=block;
        PartialAnalysisNode analysis;analysis.threshold=-45;analysis.minimum=100;analysis.maximum=2000;analysis.limit=16;analysis.prepare(48000,block);
        PartialGraphNode group;std::istringstream cfg(identityParameters);group.configure(cfg);group.prepare(48000,block);
        PartialsSpectrumNode synthesis;synthesis.prepare(48000,block);SpectrumMathNode mix;mix.prepare(48000,block);
        IFFTNode original,rebuilt;original.prepare(48000,block);rebuilt.prepare(48000,block);
        std::uint64_t track=0;double error=0;
        for(unsigned tick=0;tick<24;tick++){
            input.count=1;input.blockStart=tick*block;auto& frame=input.frames[0];frame.start=std::int64_t((tick+1)*block)-1024;
            std::fill_n(frame.bins.data(),input.size,std::complex<double>{});
            for(unsigned i=0;i<1024;i++){auto time=frame.start+i;double value=time<0?0:std::cos(partialTau*375*time/48000)+(tick<12?.3:1.3)*std::cos(partialTau*750*time/48000);frame.bins[i]=value*spectrumWindowValue(window,i,1024);}
            spectrumTransform(frame.bins.data(),input.size);
            const DspResource* resources[]={&input};ProcessContext c{};c.resourceInputs=resources;c.inputCount=1;c.bufferSize=block;c.sampleRate=48000;c.output=a.data();c.silence=silence.data();watching=true;analysis.process(c);
            if(tick>4){auto& f=analysis.partials.frames[0];assert(f.count>=2);auto found=std::min_element(f.values.begin(),f.values.begin()+f.count,[](auto& x,auto& y){return std::abs(x.frequency-375)<std::abs(y.frequency-375);});assert(std::abs(found->frequency-375)<1);if(track)assert(track==found->id);track=found->id;}
            resources[0]=&analysis.partials;group.process(c);resources[0]=&group.stream;synthesis.process(c);
            const DspResource* mixed[]={&synthesis.stream,&analysis.residual};c.resourceInputs=mixed;c.inputCount=2;mix.process(c);assert(mix.stream.size==input.size);
            for(unsigned k=0;k<input.size;k++)assert(std::abs(mix.stream.frames[0].bins[k]-frame.bins[k])<1e-8);
            c.resourceInputs=resources;c.inputCount=1;resources[0]=&input;original.process(c);resources[0]=&mix.stream;c.output=b.data();rebuilt.process(c);for(unsigned i=0;i<block;i++)error=std::max(error,std::abs(double(a[i]-b[i])));
            watching=false;assert(allocations==0);
        }
        assert(error<1e-5);
        // Incompatible batches cannot be combined accidentally.
        synthesis.stream.hop++;const DspResource* mismatch[]={&synthesis.stream,&analysis.residual};ProcessContext c{};c.resourceInputs=mismatch;c.inputCount=2;c.bufferSize=block;mix.process(c);assert(mix.stream.size==0);
    }
    // A non-bin-aligned shifted sinusoid has a continuous phase across STFT frames.
    SpectrumStream input;input.prepare(block);input.size=input.windowSize=1024;input.hop=block;input.sampleRate=48000;input.identity=99;input.window=SpectrumWindow::hann;input.blockFrames=block;
    PartialAnalysisNode analysis;analysis.threshold=-30;analysis.minimum=200;analysis.maximum=600;analysis.limit=1;analysis.prepare(48000,block);
    PartialGraphNode group;std::istringstream cfg(shiftParameters);group.configure(cfg);group.prepare(48000,block);PartialsSpectrumNode synth;synth.prepare(48000,block);IFFTNode inverse;inverse.prepare(48000,block);
    double error=0,power=0;
    for(unsigned tick=0;tick<80;tick++){
        input.count=1;input.blockStart=tick*block;auto& f=input.frames[0];f.start=std::int64_t((tick+1)*block)-1024;
        for(unsigned i=0;i<1024;i++){auto time=f.start+i;f.bins[i]=(time<0?0:std::cos(partialTau*375*time/48000))*spectrumWindowValue(input.window,i,1024);}
        spectrumTransform(f.bins.data(),1024);const DspResource* resources[]={&input};ProcessContext c{};c.resourceInputs=resources;c.inputCount=1;c.bufferSize=block;c.sampleRate=48000;c.output=a.data();c.silence=silence.data();watching=true;analysis.process(c);resources[0]=&analysis.partials;group.process(c);resources[0]=&group.stream;synth.process(c);resources[0]=&synth.stream;inverse.process(c);
        watching=false;assert(allocations==0);
        if(tick>12)for(unsigned i=0;i<block;i++){double expected=std::cos(partialTau*475*(double(tick*block+i)-1024)/48000);error+=std::pow(a[i]-expected,2);power+=expected*expected;}
    }
    assert(std::sqrt(error/power)<.03);
    // Counter-based noise follows stable IDs rather than amplitude order or slot order.
    PartialsResource batch;batch.prepare(block);batch.size=batch.windowSize=1024;batch.hop=256;batch.sampleRate=48000;batch.identity=555;batch.count=1;
    auto& frame=batch.frames[0];frame.start=1024;frame.count=2;frame.values[0]={10,0,440,1,0,0,440,1,0};frame.values[1]={20,1,660,1,0,0,660,1,0};
    PartialGraphNode noisy;std::istringstream np(noiseParameters);noisy.configure(np);noisy.prepare(48000,block);
    const DspResource* rr[]={&batch};ProcessContext nc{};nc.resourceInputs=rr;nc.inputCount=1;nc.bufferSize=block;nc.sampleRate=48000;nc.silence=silence.data();nc.output=a.data();
    watching=true;noisy.process(nc);double n0=noisy.stream.frames[0].values[0].amplitude,n1=noisy.stream.frames[0].values[1].amplitude;
    assert(n0>=0&&n0<1&&n1>=0&&n1<1&&n0!=n1);
    std::swap(frame.values[0],frame.values[1]);noisy.process(nc);assert(noisy.stream.frames[0].values[0].amplitude==n1&&noisy.stream.frames[0].values[1].amplitude==n0);
    frame.start+=256;noisy.process(nc);assert(noisy.stream.frames[0].values[0].amplitude!=n1);
    // Both checkboxes can be disabled independently or together.
    PartialNoiseNode random;random.active=true;random.partialId=10;random.frameStart=1024;random.process(nc);float first=a[0];
    random.perPartial=false;random.process(nc);first=a[0];random.partialId=20;random.process(nc);assert(a[0]==first);
    random.perFrame=false;random.process(nc);first=a[0];random.frameStart+=256;random.process(nc);assert(a[0]==first);
    random.perPartial=true;random.process(nc);first=a[0];random.frameStart+=256;random.process(nc);assert(a[0]==first);
    random.seed=1;random.process(nc);assert(a[0]!=first);
    PartialContextNode context;context.partial=&frame.values[0];float contextValues[8]{};float* contextOutputs[8];for(unsigned i=0;i<8;i++)contextOutputs[i]=contextValues+i;
    auto cc=nc;cc.output=contextValues;cc.outputs=contextOutputs;cc.outputCount=8;cc.bufferSize=1;context.process(cc);
    assert(contextValues[3]==float(context.partial->id));assert(contextValues[7]==partialSeed(context.partial->id));
    assert(contextValues[7]>=0&&contextValues[7]<=16777215&&std::floor(contextValues[7])==contextValues[7]);
    watching=false;assert(allocations==0);
    std::cout<<"Partial reconstruction, tracking, fractional frequency synthesis and frame validation passed\n";
}
