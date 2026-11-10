#include "AudioGraph.hpp"
#include <cassert>
#include <cmath>
using namespace easyknack;
static bool approximately(double a,double b){return std::abs(a-b)<1e-5;}
int main(){
    SpectralFeaturesNode node;node.prepare(800,256);
    std::complex<double> bins[8]{};bins[2]=1;bins[4]=std::sqrt(2.); // equal one-sided energy
    auto evaluate=[&](std::uint64_t end){node.evaluate(bins,8,8,4,800,7,end,SpectrumWindow::rectangular,false);};
    evaluate(8);
    assert(approximately(node.values[0],300));assert(approximately(node.values[1],100));
    assert(approximately(node.values[2],0));assert(approximately(node.values[3],1));assert(approximately(node.values[4],0));
    assert(approximately(node.values[5],400));assert(approximately(node.values[6],.001));assert(node.values[7]<.01);
    bins[2]=0;bins[4]=0;bins[1]=1;evaluate(12);assert(approximately(node.values[4],std::sqrt(1.5)));
    auto held=node.values;evaluate(12);assert(node.values==held); // repeated block is not a new frame
    bins[1]=0;evaluate(16);assert(approximately(node.values[4],1));for(int p:{0,1,2,3,5,6,7})assert(node.values[p]==0);
    bins[0]=std::sqrt(2.);bins[4]=std::sqrt(2.);for(int k=1;k<4;k++)bins[k]=1;
    evaluate(20);assert(approximately(node.values[0],200));assert(approximately(node.values[7],1));assert(approximately(node.values[6],0));
    node.evaluate(bins,8,8,4,800,8,24,SpectrumWindow::rectangular,false);assert(node.values[4]==0);
    // Equal energy at 220 and 880 Hz: linear centre 550 Hz, log centre 440 Hz.
    SpectralFeaturesNode linear,log;linear.prepare(1760,256);log.prepare(1760,256);log.logarithmic=true;
    std::complex<double> octaveBins[8]{};octaveBins[1]=1;octaveBins[4]=std::sqrt(2.);
    auto measure=[&](SpectralFeaturesNode& n,double sr=1760){n.evaluate(octaveBins,8,8,4,sr,31,8,SpectrumWindow::rectangular,false);};
    measure(linear);measure(log);
    assert(approximately(linear.values[0],550));assert(approximately(linear.values[1],330));
    assert(approximately(log.values[0],440));assert(approximately(log.values[1],1));
    assert(approximately(log.values[2],0));assert(approximately(log.values[3],1));
    for(int p:{4,5,6,7})assert(approximately(linear.values[p],log.values[p]));
    // Equal weights remain equal: there is no rebinning or implicit 1/f weighting.
    octaveBins[0]=100;log.ready=false;measure(log);assert(approximately(log.values[0],440));assert(approximately(log.values[1],1));
    octaveBins[0]=0;log.ready=false;measure(log,3520);assert(approximately(log.values[0],880));assert(approximately(log.values[1],1));
    octaveBins[4]=std::sqrt(6.);log.ready=false;measure(log);
    assert(approximately(log.values[0],220*std::exp2(1.5)));assert(approximately(log.values[1],std::sqrt(.75)));
    assert(approximately(log.values[2],-2/std::sqrt(3.)));assert(approximately(log.values[3],7./3));
    // A live scale change re-evaluates the held frame and resets flux history.
    SpectralFeaturesNode settings;settings.applyParameters(linear);settings.logarithmic=true;linear.applyParameters(settings);
    octaveBins[4]=std::sqrt(2.);measure(linear);assert(approximately(linear.values[0],440));assert(linear.values[4]==0);
    octaveBins[1]=octaveBins[4]=0;octaveBins[0]=1;log.ready=false;measure(log);
    for(int p:{0,1,2,3})assert(log.values[p]==0); // DC has no log frequency.
    octaveBins[0]=0;log.ready=false;measure(log);for(double value:log.values)assert(value==0);
    // Process every synchronous frame, not only the last frame of the block.
    SpectrumStream stream;stream.prepare(256);stream.size=8;stream.windowSize=8;stream.hop=4;stream.sampleRate=800;stream.identity=9;stream.count=2;
    stream.frames[0].start=0;stream.frames[0].bins[1]=1;stream.frames[1].start=4;stream.frames[1].bins[2]=1;
    const DspResource* resources[]={&stream};const float* input[]={nullptr};float out[8]{};float* outputs[8];for(int p=0;p<8;p++)outputs[p]=out+p;
    ProcessContext c{input,1,out,1,800,out,256};c.outputs=outputs;c.outputCount=8;c.resourceInputs=resources;
    node.process(c);assert(approximately(out[0],200));assert(approximately(out[4],std::sqrt(2.)));
    auto async=std::make_unique<AnalysisResource>();auto result=std::make_unique<AnalysisResult>();async->result=result.get();
    auto& frame=result->spectrum;frame.fftFrames=8;frame.windowFrames=8;frame.hopFrames=4;frame.sampleRate=800;frame.sourceIdentity=11;frame.endSample=8;frame.window=SpectrumWindow::rectangular;frame.bins[3]=1;
    resources[0]=async.get();node.process(c);assert(approximately(out[0],300));assert(out[4]==0);
    // Capturing values never drops a short peak between UI polls.
    ValueViewerNode viewer;viewer.count=2;viewer.sources={0,1};viewer.bindIdentity("test-values");viewer.prepare(1000,20);
    float a[20]{},b=4,output[20]{};a[3]=9;const float* inputs[]={a,&b};char rates[]={'a','c'};
    ProcessContext v{inputs,2,output,20,1000,output,20};v.sourceRates=rates;viewer.process(v);
    auto packet=std::make_unique<AnalysisFrame>();assert(viewer.mailbox->latest(*packet));assert(packet->frequency==2);assert(packet->values[2]==2);
    assert(packet->values[8]==0&&packet->values[9]==9&&packet->values[10]==0);
    assert(packet->values[11]==4&&packet->values[12]==4&&packet->values[13]==4);
    assert(packet->values[4]==20);assert(viewer.isObserver());
    // Exact sample clock survives beyond float's integer precision.
    viewer.samples=(std::uint64_t{1}<<40);viewer.process(v);
    assert(viewer.mailbox->latest(*packet));std::uint64_t clock=0;for(int j=0;j<4;j++)clock|=static_cast<std::uint64_t>(packet->values[4+j])<<(16*j);
    assert(clock==(std::uint64_t{1}<<40)+20);
}
