#include "AudioGraph.hpp"
#include <cassert>
using namespace easyknack;
int main(){
    auto result=std::make_unique<AnalysisResult>();result->availableViews=8;result->correlationCount=4096;
    result->lagSpacingSeconds=1./48000;result->sampleRate=48000;result->correlationRms=.5;result->historyIdentity=19;
    auto& r=*result;auto& curve=r.views[3];curve[0]=1;
    curve[99]=curve[101]=.7f;curve[100]=.8f;
    AnalysisResource resource;resource.result=&r;const DspResource* resources[]={&resource};
    float out[5][4]{};float* outputs[]={out[0],out[1],out[2],out[3],out[4]};
    ProcessContext context{nullptr,1,out[0],4,48000,out[0],4};context.outputs=outputs;context.outputCount=5;context.resourceInputs=resources;
    CorrelationFeaturesNode node;node.process(context);
    assert(std::abs(out[0][3]-.8)<1e-6&&std::abs(out[1][3]-100./48000)<1e-9);
    assert(std::abs(out[2][3]-480)<1e-4&&std::abs(out[3][3]-10*std::log10(4.))<1e-5&&out[4][3]==1);
    // The same snapshot is held, including during graph handover; parameter changes re-evaluate it.
    curve[100]=.2f;node.process(context);assert(out[0][0]>.79);
    CorrelationFeaturesNode inherited;inherited.inheritState(node);inherited.process(context);assert(out[0][0]>.79);
    auto settings=node;settings.threshold=.9;node.applyParameters(settings);node.process(context);assert(out[4][0]==0);
    curve.fill(0);curve[0]=1;curve[99]=curve[101]=.7f;curve[100]=.8f;++r.sequence;
    node.process(context);assert(out[0][0]>.79&&out[1][0]==0&&out[2][0]==0&&out[3][0]>6&&out[4][0]==0);
    settings.threshold=.3;node.applyParameters(settings);node.process(context);assert(out[4][0]==1);
    // Silence is decided from pre-normalization source amplitude, not rho(0)=1.
    r.correlationRms=1e-9;++r.sequence;node.process(context);assert(out[0][0]==0&&out[3][0]==-120&&out[4][0]==0);
    r.correlationRms=.5;curve.fill(1);++r.sequence;node.process(context);assert(out[4][0]==0); // DC plateau is not a repetition peak
    // Off-grid sinusoid; prefer the first period over numerically equal later peaks.
    for(unsigned i=0;i<r.correlationCount;i++)curve[i]=std::cos(6.283185307179586*440*i/48000);
    ++r.sequence;node.process(context);assert(std::abs(out[2][0]-440)<.1&&out[4][0]==1&&std::isfinite(out[3][0]));
    settings.minimum=500;settings.maximum=450;node.applyParameters(settings);node.process(context);assert(out[4][0]==0);
    settings.minimum=20;settings.maximum=20000;node.applyParameters(settings);
    // Non-periodic weak peaks fail validity; lag zero and monotonic near-zero values are excluded.
    curve.fill(0);curve[0]=1;curve[100]=.05;++r.sequence;node.process(context);assert(out[0][0]<.051&&out[4][0]==0);
    resource.result=nullptr;node.process(context);assert(out[0][0]==0&&out[4][0]==0);
    resource.result=&r;r.availableViews=1;++r.sequence;node.process(context);assert(out[4][0]==0);r.availableViews=8;
    // Correlation averaging retains the mean source power for the same silence gate.
    auto average=std::make_unique<AnalysisAverageNode>();average->prepare(48000,256);average->begin(48000);
    r.correlationRms=.25;average->correlate(r);r.correlationRms=.75;average->correlate(r);
    assert(std::abs(average->result->correlationRms-std::sqrt(.3125))<1e-9);
    // The FFT-to-correlation worker preserves actual window-weighted amplitude and zero-energy silence.
    auto job=std::make_unique<AnalysisJob>();auto request=std::make_unique<AnalysisJob::Request>();
    auto& f=request->inputSpectrum;f.windowFrames=f.fftFrames=1024;f.windowSum=f.windowSumSquares=1024;f.sampleRate=48000;
    for(unsigned i=0;i<1024;i++)f.bins[i]=.1*std::sin(6.283185307179586*16*i/1024);
    spectrumTransform(f.bins.data(),1024);job->analyzeCorrelation(*request,r);assert(std::abs(r.correlationRms-.1/std::sqrt(2.))<1e-9);
    f.bins.fill({});++f.endSample;job->analyzeCorrelation(*request,r);assert(r.correlationRms==0);
}
