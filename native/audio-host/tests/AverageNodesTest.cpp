#include "AudioGraph.hpp"
#include <cassert>
#include <cmath>
using namespace easyknack;
static void valueStatistics(){
    float signal[8]={1,3,5,7,9,11,13,15},trigger[8]={1};
    float out[8]{},count[8]{},finished[8]{},deviation[8]{},minimum[8]{},maximum[8]{};
    const float* inputs[]={signal,trigger};float* outputs[]={out,count,finished,deviation,minimum,maximum};
    char rates[]={'a','e'};
    ProcessContext c{inputs,2,out,8,8,out,8};c.outputs=outputs;c.outputCount=6;c.sourceRates=rates;
    ValueAverageNode value;value.measurement.measurements=true;value.measurement.requestedCount=4;
    assert(value.outputCount()==6);value.process(c);
    assert(deviation[0]==0&&minimum[0]==1&&maximum[0]==1);
    assert(std::abs(deviation[3]-std::sqrt(5.))<1e-6&&minimum[3]==1&&maximum[3]==7);
    trigger[0]=0;value.process(c);
    assert(out[7]==4&&std::abs(deviation[7]-std::sqrt(5.))<1e-6&&minimum[7]==1&&maximum[7]==7);
    // Control observations have equal weight, regardless of the number of held samples.
    rates[0]='c';trigger[0]=1;signal[0]=10;value.measurement.requestedCount=2;value.process(c);
    assert(count[7]==1&&minimum[7]==10&&maximum[7]==10&&deviation[7]==0);
    trigger[0]=0;signal[0]=14;value.process(c);
    assert(out[7]==12&&deviation[7]==2&&minimum[7]==10&&maximum[7]==14&&finished[0]==1);
    // A rebuild during acquisition preserves all accumulated moments and extrema.
    rates[0]='a';trigger[0]=1;value.measurement.requestedCount=4;c.bufferSize=2;
    signal[0]=-5;signal[1]=-3;value.process(c);
    ValueAverageNode replacement;replacement.inheritState(value);trigger[0]=0;
    signal[0]=-1;signal[1]=1;replacement.process(c);
    assert(count[1]==4&&out[1]==-2&&std::abs(deviation[1]-std::sqrt(5.))<1e-6);
    assert(minimum[1]==-5&&maximum[1]==1&&finished[1]==1);
    // Stable variance for small changes around a large baseline.
    trigger[0]=1;value.measurement.requestedCount=2;signal[0]=1000000;signal[1]=1000002;value.process(c);
    assert(out[1]==1000001&&deviation[1]==1);
    // Empty acquisitions and non-finite observations never leak the previous statistics.
    value.measurement.measurements=false;value.measurement.duration=.25;
    signal[0]=std::numeric_limits<float>::quiet_NaN();signal[1]=std::numeric_limits<float>::infinity();
    value.process(c);assert(count[1]==0&&finished[1]==1&&out[1]==0&&deviation[1]==0&&minimum[1]==0&&maximum[1]==0);
}
int main(){
    valueStatistics();
    float signal[8]={1,3,5,7,9,11,13,15},trigger[8]={1},out[8]{},count[8]{},finished[8]{};
    const float* inputs[]={signal,trigger};float* outputs[]={out,count,finished};char rates[]={'a','e'};
    ProcessContext c{inputs,2,out,8,8,out,8};c.outputs=outputs;c.outputCount=3;c.sourceRates=rates;
    ValueAverageNode value;value.measurement.measurements=true;value.measurement.requestedCount=4;value.process(c);
    assert(out[3]==4&&out[7]==4&&count[7]==4&&finished[3]==1&&finished[4]==0);
    trigger[0]=0;value.process(c);assert(out[7]==4&&finished[3]==0);
    trigger[2]=1;value.process(c);assert(out[5]==8&&finished[5]==1);trigger[2]=0;
    rates[0]='c';trigger[0]=1;value.measurement.requestedCount=2;value.process(c);assert(count[7]==1);
    trigger[0]=0;signal[0]=5;value.process(c);assert(out[0]==3&&finished[0]==1&&count[7]==2);
    rates[0]='a';signal[0]=1;value.measurement.measurements=false;value.measurement.duration=.5;
    trigger[0]=1;value.process(c);assert(out[7]==4&&finished[3]==1);
    // Running settings are latched, and state survives graph reconstruction.
    value.measurement.measurements=true;value.measurement.requestedCount=16;value.process(c);
    ValueAverageNode replacement;replacement.measurement.requestedCount=1;replacement.inheritState(value);
    trigger[0]=0;replacement.process(c);assert(count[7]==16&&finished[7]==1);
    auto analysis=std::make_unique<AnalysisAverageNode>();analysis->prepare(8,8);
    analysis->measurement.measurements=true;analysis->measurement.requestedCount=2;
    auto stream=std::make_unique<SpectrumStream>();stream->prepare(8);stream->size=8;stream->windowSize=8;
    stream->hop=4;stream->sampleRate=8;stream->identity=42;stream->window=SpectrumWindow::rectangular;
    stream->blockStart=0;stream->count=1;stream->frames[0].start=0;stream->frames[0].bins[1]={0,2};
    const DspResource* resources[]={stream.get(),nullptr};c.resourceInputs=resources;trigger[0]=1;
    analysis->process(c);assert(count[7]==1&&analysis->result->availableViews==5);
    stream->blockStart=8;stream->frames[0].start=4;stream->frames[0].bins[1]={0,-2};trigger[0]=0;
    c.bufferSize=4;analysis->process(c);assert(count[3]==2&&finished[3]==1);
    assert(std::abs(std::norm(analysis->result->spectrum.bins[1])-4)<1e-9);
    // Every completed synchronous frame contributes, even within one audio block.
    stream->identity++;stream->windowSize=4;stream->blockStart=0;stream->count=2;
    stream->frames[0].start=0;stream->frames[1].start=4;stream->frames[1].bins[1]={0,-2};
    c.bufferSize=8;trigger[0]=1;analysis->process(c);
    assert(count[7]==2&&finished[7]==1&&std::norm(analysis->result->spectrum.bins[1])==4);
    // Async SpectrumNode delivers through SpectrumStream too, with capture times
    // before this block. Count new arrivals once, not only synchronous timestamps.
    stream->identity++;stream->count=1;stream->blockStart=100;stream->frames[0].start=84;
    trigger[0]=1;analysis->process(c);assert(count[7]==0);
    trigger[0]=0;stream->blockStart=108;stream->frames[0].start=88;analysis->process(c);
    assert(count[7]==1);analysis->process(c);assert(count[7]==1);
    stream->blockStart=116;stream->frames[0].start=92;analysis->process(c);
    assert(count[7]==2&&finished[0]==1&&analysis->resource.result);
    // Features consume mean power, rather than a mean of descriptor values.
    analysis->begin(8);std::complex<double> a[8]{},b[8]{};
    a[0]=2;a[2]=std::sqrt(2.);a[4]=2;b[1]=b[3]=std::sqrt(2.);
    analysis->spectrum(a,8,8,4,8,55,8,SpectrumWindow::rectangular,false);
    analysis->spectrum(b,8,8,4,8,55,12,SpectrumWindow::rectangular,false);
    SpectralFeaturesNode features;features.prepare(8,8);trigger[0]=0;resources[0]=&analysis->resource;
    features.process(c);assert(std::abs(features.values[7]-1)<1e-6);
    auto snapshot=std::make_unique<AnalysisResult>();AnalysisResource resource;resource.result=snapshot.get();resources[0]=&resource;
    snapshot->availableViews=8;snapshot->correlationCount=3;snapshot->lagSpacingSeconds=.125;
    snapshot->historyIdentity=77;snapshot->sequence=1;snapshot->sampleRate=8;snapshot->views[3][0]=1;snapshot->views[3][1]=.2;
    c.bufferSize=4;trigger[0]=1;analysis->process(c);assert(count[3]==0); // baseline is not a new observation
    trigger[0]=0;snapshot->sequence++;analysis->process(c);assert(count[3]==1);
    analysis->process(c);assert(count[3]==1); // repeated snapshots never count twice
    snapshot->sequence++;snapshot->views[3][1]=.6;analysis->process(c);
    assert(count[3]==2&&finished[0]==1&&std::abs(analysis->result->views[3][1]-.4)<1e-6);
    snapshot->historyIdentity++;snapshot->sequence++;analysis->process(c);assert(count[3]==2); // held experiment
    trigger[0]=1;analysis->measurement.measurements=false;analysis->measurement.duration=.5;analysis->process(c);
    assert(count[3]==0&&finished[3]==1&&analysis->resource.result==nullptr); // no fresh data
    trigger[0]=1;analysis->measurement.measurements=true;analysis->process(c);trigger[0]=0;
    snapshot->sequence++;analysis->process(c);snapshot->historyIdentity++;snapshot->sequence++;analysis->process(c);
    assert(!analysis->measurement.active&&analysis->resource.result==nullptr&&finished[0]==0);
    // Manual buttons use the same finite acquisition and preserve exact trigger offsets.
    ValueAverageNode manual;manual.bindIdentity("average-test");manual.measurement.measurements=true;
    manual.measurement.requestedCount=2;inputs[1]=nullptr;c.resourceInputs=nullptr;
    ValueEventBuffer events;events.clear(16);ValueEventBuffer* eventOutputs[]={nullptr,nullptr,&events};
    c.valueEventOutputs=eventOutputs;c.sampleOffset=0;submitManualTrigger("average-test:start");++manualTriggerEpoch;
    manual.process(c);assert(events.count==1&&events.events[0].sampleOffset==1&&finished[1]==1);
    events.clear(16);++manualTriggerEpoch;manual.process(c);assert(events.count==0&&finished[1]==0);
    // Connected event offsets remain absolute even in feedback chunks.
    inputs[1]=trigger;trigger[0]=1;c.sampleOffset=6;events.clear(16);manual.process(c);
    assert(events.count==1&&events.events[0].sampleOffset==7);
}
