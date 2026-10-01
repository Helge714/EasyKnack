#include "AudioGraph.hpp"
#include "RenderTelemetry.hpp"
#include "BakedGraph.hpp"
#include <cassert>
#include <iostream>
using namespace easyknack;
#include "ReaderTriggerTest.hpp"
#include "RateSeparationTest.hpp"
#include "SpectrumTest.hpp"
#include "PositionReaderTest.hpp"
#include "HarmonizerTest.hpp"

struct Probe : NodeProcessor {
    inline static Probe* rendered=nullptr;
    float value=0; unsigned blocks=0,updates=0;
    bool isSink()const noexcept override{return true;}
    bool canShareChannels()const noexcept override{return true;}
    bool supportsLiveParameters()const noexcept override{return true;}
    void configure(std::istream& in)override{in>>value;}
    void applyParameters(const NodeProcessor& p)noexcept override{value=static_cast<const Probe&>(p).value;++updates;}
    void process(ProcessContext& c)noexcept override{rendered=this;++blocks;std::fill_n(c.output,c.bufferSize,value);}
};
struct MixedRateExport : BakedGraphProcessor {
    void configure(std::istream&)override{
        configureGraph("GRAPH 3 n digitalnoise -1 440 -1 0 @ ae aa factor baked_output 0 0 @ a a trigger baked_output 256 0 @ e e",[](const std::string& type)->std::unique_ptr<NodeProcessor>{
            if(type=="baked_output")return std::make_unique<BakedOutput>();return createNodeProcessor(type);
        },{},{1,2},{});
    }
    void renderGraph(int frames,AudioIO* io,MidiSource* midi)noexcept override{
        auto& graph=runtime->graph;for(int index:graph.order){graph.processNode(index,frames,0,io,midi);if(graph.nodes[index].stereo)graph.processNode(index,frames,1,io,midi);}
    }
};
void resourceAndEvents(){
    ValueEventBuffer input,output;input.clear(256);output.clear(256);
    input.push(10,1,ValueEventKind::trigger);input.push(10,1,ValueEventKind::trigger);input.push(250,1,ValueEventKind::trigger);
    assert(input.count==2&&input.valueAt(10)==0);
    std::array<float,256> samples{},silence{},result{};for(unsigned i=0;i<256;i++)samples[i]=static_cast<float>(i);
    const float* inputs[]{samples.data(),nullptr};const ValueEventBuffer* streams[]{nullptr,&input};ValueEventBuffer* outputs[]{&output};char rates[]{'a','e'};
    ProcessContext c{inputs,2,result.data(),256,48000,silence.data(),256};c.sourceRates=rates;c.valueEventInputs=streams;c.valueEventOutputs=outputs;
    SampleHoldNode hold;hold.processEventBlock(c);assert(output.count==2&&output.events[0].sampleOffset==10&&output.events[0].value==10&&output.events[1].value==250);
    output.clear(256);ClockNode clock;clock.frequencyValue=480;inputs[0]=nullptr;rates[0]='c';clock.processEventBlock(c);
    assert(output.events[0].sampleOffset==10&&output.events[1].sampleOffset>10); // one sync trigger per sample
    output.clear(256);streams[1]=nullptr;clock.phase=0;clock.processEventBlock(c);assert(output.count==2&&output.events[0].sampleOffset==99&&output.events[1].sampleOffset==199);
    BakedInput boundary;boundary.external=&c;boundary.port=1;streams[1]=&input;output.clear(256);boundary.process(c);assert(output.count==2&&output.events[1].kind==ValueEventKind::trigger);
    ValueEventBuffer rightStream;rightStream.clear(256);rightStream.push(30,1,ValueEventKind::trigger);const ValueEventBuffer* rightStreams[]{nullptr,&rightStream};
    c.rightValueEventInputs=rightStreams;c.channelIndex=1;output.clear(256);boundary.process(c);assert(output.count==1&&output.events[0].sampleOffset==30);c.channelIndex=0;
    ValueEventBuffer overflow;overflow.clear(600);for(unsigned i=0;i<600;i++)overflow.push(i,1,ValueEventKind::trigger);assert(overflow.count==512&&overflow.dropped==88);

    HistoryResource history;history.prepare(48000,.25);for(unsigned i=0;i<256;i++)history.append(static_cast<float>(i));history.blockFrames=256;
    HistoryReaderNode reader;const DspResource* resources[]{&history,nullptr,nullptr};const float* readInputs[]{nullptr,nullptr,nullptr};
    ProcessContext r{readInputs,3,result.data(),256,48000,silence.data(),256};r.resourceInputs=resources;reader.process(r);assert(result[0]==0&&result[255]==255);
    HistoryReaderNode slow;slow.speed=.5;slow.process(r);assert(result[1]==.5&&result[255]==127.5);
    HistoryReaderNode missing;missing.offset=.5;missing.process(r);assert(result[255]==0);
    auto oldIdentity=history.identity;history.prepare(48000,.5);assert(history.identity!=oldIdentity);
    for(unsigned i=0;i<256;i++)history.append(2);history.blockFrames=256;reader.process(r);assert(result[0]==2&&result[255]==2);
    BakedOutput resourceBoundary;std::istringstream config("0 2");resourceBoundary.configure(config);resourceBoundary.process(r);assert(resourceBoundary.resourceOutput(0)==&history);

    auto factory=[](const std::string& type)->std::unique_ptr<NodeProcessor>{if(type=="mixed")return std::make_unique<MixedRateExport>();return createNodeProcessor(type);};
    auto baked=AudioGraph::compile("GRAPH 3 n mixed @ ae - m math 0 0 256 0 add 0 @ a aa o out 1 0 @ a a",factory);
    auto original=AudioGraph::parse("GRAPH 3 n digitalnoise -1 440 -1 0 @ ae aa m math 0 0 256 0 add 0 @ a aa o out 1 0 @ a a");
    std::array<float,256> expected{};for(unsigned block=0;block<8;block++){baked.process(result.data(),256);original.process(expected.data(),256);assert(result==expected);}
    auto job=std::make_unique<AnalysisJob>();HistoryResource wave;wave.prepare(48000,1);
    for(unsigned i=0;i<8192;i++)wave.append(std::sin(2*3.141592653589793*440*i/48000));
    assert(job->submit(wave,40,2000));job->work();auto* first=job->acquire();assert(first&&first->endSample==8192&&std::abs(first->frequency-440)<2&&first->confidence>.9);
    AnalysisResource analysis;analysis.result=first;AnalysisViewNode view;const DspResource* analyses[]{&analysis};r.resourceInputs=analyses;r.inputCount=1;view.resource.view=AnalysisView::correlation;view.process(r);assert(view.resource.result==first);
    AnalysisStatisticsNode stats;stats.mode=4;stats.process(r);assert(std::abs(result[0]-440)<2);
    for(unsigned i=0;i<8192;i++)wave.append(0);assert(job->submit(wave,40,2000));job->work();auto* next=job->acquire();assert(next&&next->sequence>first->sequence&&next->frequency==0);assert(next->statistics[0][0]==0); // no stale FFT for silence
    auto background=AnalysisWorker::instance().create();assert(background->submit(wave,40,2000));const AnalysisResult* ready=nullptr;
    for(unsigned i=0;i<1000&&!ready;i++){std::this_thread::sleep_for(std::chrono::milliseconds(1));ready=background->acquire();}assert(ready&&ready->endSample==wave.end);
}
void wrappingHistoryReader(){
    HistoryResource ring;ring.samples.resize(8);ring.sampleRate=8;
    for(int i=0;i<8;i++)ring.append(static_cast<float>(i));
    std::array<float,16> audio{},distance{},silence{};float* outputs[]{audio.data(),distance.data()};
    const float* inputs[]{nullptr,nullptr,nullptr};const DspResource* resources[]{&ring,nullptr,nullptr};
    ProcessContext c{inputs,3,audio.data(),16,8,silence.data(),16};c.resourceInputs=resources;c.outputs=outputs;c.outputCount=2;
    HistoryReaderNode forward;forward.process(c);
    for(int i=0;i<16;i++){assert(audio[i]==i%8);assert(distance[i]==2.f*std::min(i%8,8-i%8)/8);}
    HistoryReaderNode reverse;reverse.speed=-1;reverse.process(c);
    for(int i=0;i<16;i++)assert(audio[i]==(8-i%8)%8);
    HistoryReaderNode half;half.speed=.5;half.process(c);assert(audio[15]==3.5f); // interpolate across physical end
    HistoryReaderNode offset;offset.speed=0;offset.offset=.5;offset.process(c);assert(audio[0]==4&&distance[0]==1);
    offset.offset=.25;offset.process(c);assert(audio[0]==6&&distance[0]==.5);
    offset.offset=1;offset.process(c);assert(audio[0]==0&&distance[0]==0);
    ring.append(8);offset.offset=0;offset.process(c);assert(audio[0]==8&&distance[0]==.25); // writer moved, held reader did not
    std::array<float,16> modulation{};for(int i=0;i<16;i++)modulation[i]=(i%8)/8.f;
    inputs[2]=modulation.data();offset.process(c);for(int i=0;i<16;i++)assert(audio[i]==ring.samples[(8-i%8)%8]);
    inputs[2]=nullptr;offset.offset=std::numeric_limits<float>::quiet_NaN();offset.speed=std::numeric_limits<float>::infinity();offset.process(c);
    for(int i=0;i<16;i++){assert(std::isfinite(audio[i]));assert(distance[i]>=0&&distance[i]<=1);}
    HistoryReaderNode inherited;inherited.inheritState(offset);assert(inherited.cursor==offset.cursor);
    resources[0]=nullptr;offset.process(c);for(int i=0;i<16;i++)assert(audio[i]==0&&distance[i]==0);
}
// Independent oracle: interleave one write and all reads for each individual sample.
void sampleAccurateHistory(){
    constexpr unsigned readers=6;
    for(unsigned capacity:{256u,257u,1024u})for(unsigned channel=0;channel<2;channel++){
        HistoryBufferNode writer;writer.seconds=capacity/48000.;writer.prepare(48000,256);
        std::vector<float> reference(capacity,0);std::size_t head=0;
        std::array<HistoryReaderNode,readers> nodes;
        const float speeds[]{0,1,2,-1,.5f,1.75f};
        std::array<double,readers> cursors{};
        std::array<float,256> input{},offset{},silence{};
        std::array<std::array<float,256>,readers> actual{},distance{},expected{},expectedDistance{};
        const float* writerInputs[]{input.data()};const float* readerInputs[]{nullptr,nullptr,offset.data()};
        const DspResource* resources[]{&writer.history,nullptr,nullptr};
        auto* storage=writer.history.samples.data();auto* shadow=writer.history.overwritten.data();
        for(unsigned r=0;r<readers;r++)nodes[r].speed=speeds[r];
        unsigned time=0;
        for(unsigned block=0;block<32;block++){
            unsigned count=std::array<unsigned,4>{256,17,128,1}[block%4];
            for(unsigned i=0;i<count;i++){
                input[i]=static_cast<float>((time+i+1)*(channel?-.01:.01));
                offset[i]=static_cast<float>(((time+i)*13)%997)/997;
                reference[head]=input[i];head=(head+1)%capacity;
                for(unsigned r=0;r<readers;r++){
                    double position=cursors[r]-static_cast<double>(offset[i])*capacity;
                    position-=std::floor(position/capacity)*capacity;
                    auto a=static_cast<std::size_t>(position),b=(a+1)%capacity;
                    expected[r][i]=reference[a]+(reference[b]-reference[a])*static_cast<float>(position-a);
                    double delta=std::abs(position-head);expectedDistance[r][i]=static_cast<float>(2*std::min(delta,capacity-delta)/capacity);
                    cursors[r]+=speeds[r];cursors[r]-=std::floor(cursors[r]/capacity)*capacity;
                }
            }
            ProcessContext write{writerInputs,1,input.data(),count,48000,silence.data(),count};writer.process(write);
            for(unsigned r=0;r<readers;r++){
                float* outputs[]{actual[r].data(),distance[r].data()};
                ProcessContext read{readerInputs,3,actual[r].data(),count,48000,silence.data(),count};read.outputs=outputs;read.outputCount=2;read.resourceInputs=resources;
                nodes[r].process(read);
                for(unsigned i=0;i<count;i++){assert(std::abs(actual[r][i]-expected[r][i])<1e-5);assert(std::abs(distance[r][i]-expectedDistance[r][i])<1e-6);}
            }
            assert(writer.history.samples.data()==storage&&writer.history.overwritten.data()==shadow);
            if(block==10){
                HistoryBufferNode replacement;replacement.seconds=writer.seconds;replacement.prepare(48000,256);replacement.adoptState(writer);
                assert(replacement.history.samples.data()==storage&&replacement.history.overwritten.data()==shadow);
                writer.adoptState(replacement); // Both buffers and the temporal snapshot survive handover.
                assert(writer.history.samples.data()==storage&&writer.history.overwritten.data()==shadow);
            }
            time+=count;
        }
    }
    // A changed maximum block size retains samples but uses newly prepared shadow storage.
    HistoryBufferNode previous,next;previous.prepare(48000,128);next.prepare(48000,256);
    auto* samples=previous.history.samples.data();next.adoptState(previous);
    assert(next.history.samples.data()==samples&&next.history.overwritten.size()==256);
}
int main(){
    harmonizerTests();readerTriggers();positionReaderTests();rateSeparation();spectrumTests();
    resourceAndEvents();
    wrappingHistoryReader();
    sampleAccurateHistory();
    for(double rate:{44100.,48000.,96000.}){
        HistoryBufferNode small;small.seconds=256/rate;small.prepare(rate,256);assert(small.history.samples.size()==256);
        small.seconds=1/rate;small.prepare(rate,256);assert(small.history.samples.size()==256);
        small.seconds=512/rate;small.prepare(rate,256);assert(small.history.samples.size()==512);
        for(int i=0;i<768;i++)small.history.append(static_cast<float>(i));
        assert(small.history.begin()==256&&small.history.sample(256)==256&&small.history.sample(767)==767);
        assert(std::abs(UnitConverterNode::evaluate(rate,UnitFormat::sample,UnitFormat::second,rate)-1)<1e-6);
        assert(std::abs(UnitConverterNode::evaluate(10,UnitFormat::ms,UnitFormat::sample,rate)-rate*.01)<1e-3);
        assert(unitDimension(UnitFormat::sample)==unitDimension(UnitFormat::second));
        UnitConverterNode converter;converter.from=UnitFormat::sample;converter.to=UnitFormat::ms;converter.value=static_cast<float>(rate*.01);
        float converted=0,zero=0;ProcessContext conversion{nullptr,0,&converted,1,rate,&zero,1};converter.process(conversion);assert(std::abs(converted-10)<1e-5);
        DelayNode delay;delay.prepare(rate,256);delay.timeValue=static_cast<float>(4/rate);
        std::array<float,16> in{},out{},silence{};in[0]=1;const float* inputs[]{in.data(),nullptr};
        ProcessContext c{inputs,2,out.data(),16,rate,silence.data(),16};delay.process(c);
        assert(std::abs(out[4]-1)<1e-5);assert(std::abs(out[0])<1e-5);
        DelayNode next;next.prepare(rate,256);auto storage=delay.buffer.data();auto index=delay.writeIndex;
        next.adoptState(delay);assert(next.buffer.data()==storage&&next.writeIndex==index);
        assert(next.buffer[0]==1);
        DelayNode half;half.prepare(rate,256);half.timeValue=static_cast<float>(.5/rate);half.process(c);
        assert(std::abs(out[0]-.5)<1e-5&&std::abs(out[1]-.5)<1e-5);
    }
    auto factory=[](const std::string&){return std::make_unique<Probe>();};
    AudioEngine engine;engine.publish(AudioGraph::compile("GRAPH 1 x probe 1 @ a -",factory));
    assert(engine.publishParameters(AudioGraph::compile("GRAPH 1 x probe 2 @ a -",factory,true)));
    assert(engine.publishParameters(AudioGraph::compile("GRAPH 1 x probe 3 @ a -",factory,true)));
    std::array<float,512> stereo{};engine.render(stereo.data(),256);
    auto instance=Probe::rendered;assert(instance&&instance->value==3&&instance->blocks==1);
    assert(engine.publishParameters(AudioGraph::compile("GRAPH 1 x probe 4 @ a -",factory,true)));
    engine.render(stereo.data(),256);assert(Probe::rendered==instance&&instance->blocks==2&&instance->value==4);
    assert(instance->updates==2);
    assert(engine.publishParameters(AudioGraph::compile("GRAPH 1 x probe 4 @ a -",factory,true)));engine.render(stereo.data(),256);assert(instance->updates==2);
    assert(!engine.publishParameters(AudioGraph::compile("GRAPH 1 other probe 5 @ a -",factory,true)));
    auto curvePayload=[](float end){return std::string("GRAPH 2 c curve -1 .5 0 1 0 1 0 2 0 0 0 0 0 0 0 1 ")+std::to_string(end)+" 1 1 1 1 0 @ c c o out 0 0 @ a a";};
    auto curveFactory=[](const std::string& id){return createNodeProcessor(id);};
    AudioEngine curves;curves.publish(AudioGraph::parse(curvePayload(1)));curves.render(stereo.data(),256);
    for(unsigned i=0;i<20;i++){assert(curves.publishParameters(AudioGraph::compile(curvePayload(i%2),curveFactory,true)));curves.render(stereo.data(),256);}
    auto analyzer=std::make_unique<AutocorrelationNode>();auto nextAnalyzer=std::make_unique<AutocorrelationNode>();auto address=analyzer->samples.data();analyzer->samples[0]=.25;nextAnalyzer->adoptState(*analyzer);assert(nextAnalyzer->samples.data()==address&&nextAnalyzer->samples[0]==.25);
    RenderTelemetry telemetry;for(int i=0;i<100;i++)telemetry.observe(.00001*i,256,48000,100+i);
    assert(std::abs(telemetry.snapshot(200).p99us-980)<1e-6);
    std::cout<<"Preparation, sparse triggers, resources, analysis worker and live parameter coalescing passed\n";
}
