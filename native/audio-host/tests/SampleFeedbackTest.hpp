#pragma once
#include "FilterMidiTest.hpp"
#include <chrono>
namespace sample_feedback_tests {
using namespace easyknack;
inline void check(bool ok,int line){if(!ok)throw std::runtime_error("Sample feedback assertion at "+std::to_string(line));}
#define SF_CHECK(...) check((__VA_ARGS__),__LINE__)
inline void run(){
    std::array<float,256> a{},b{};
    auto single=AudioGraph::parse("GRAPH 3 d sampledelay 1 0 @ a a v value .25 @ c - o out 0 0 @ a a");
    SF_CHECK(!single.hasSampleSteps);single.process(a.data(),8);SF_CHECK(a[0]==0&&a[1]==.25f&&a[7]==.25f);
    const std::string feedback="GRAPH 4 d sampledelay 2 0 @ a a gain math 0 0 -1 .5 multiply 0 @ a aa sum math 1 0 -1 1 add 0 @ a aa o out 0 0 @ a a";
    auto graph=AudioGraph::parse(feedback);SF_CHECK(graph.hasSampleSteps&&graph.steps.size()==2&&graph.steps[0].nodes.size()==3);
    graph.process(a.data(),256);float expected=0;for(float value:a){SF_CHECK(value==expected);expected=.5f*expected+1;}
    auto partial=AudioGraph::parse(feedback);partial.process(a.data(),3);SF_CHECK(a[2]==1.5f);partial.process(a.data(),7);SF_CHECK(a[0]==1.75f);
    auto replacement=AudioGraph::parse(feedback);replacement.inheritState(partial);partial.process(a.data(),256);replacement.process(b.data(),256);SF_CHECK(a==b);
    auto self=AudioGraph::parse("GRAPH 2 d sampledelay 0 0 @ a a o out 0 0 @ a a");self.process(a.data(),256);SF_CHECK(a[0]==0&&a[255]==0);
    auto control=AudioGraph::parse("GRAPH 3 d sampledelay 1 0 @ a a m math 0 0 -1 1 add 0 @ c ca o out 0 0 @ a a");
    control.process(a.data(),16);SF_CHECK(a[0]==0&&a[1]==1&&a[15]==1);control.process(a.data(),16);SF_CHECK(a[0]==1&&a[1]==2&&a[15]==2);
    auto stereo=AudioGraph::parse("GRAPH 4 d sampledelay 2 0 @ a a c context @ c - m math 0 0 1 0 add 0 @ a aa o out 0 0 @ a a");
    stereo.process(a.data(),256,0,nullptr);stereo.process(b.data(),256,1,nullptr);for(int i=0;i<256;i++)SF_CHECK(a[i]==-i&&b[i]==i);
    filter_midi_tests::Source midi;midi.block.append(3,filter_midi_tests::message(144,60,100));midi.block.append(7,filter_midi_tests::message(144,64,100));
    auto poly=AudioGraph::parse("GRAPH 5 d sampledelay 3 0 @ a a s midiin 1 @ c - n midinote 1 0 2 poly 2 @ a c m math 0 0 258 0 add 0 @ a aa o out 0 0 @ a a");
    poly.process(a.data(),16,0,nullptr,&midi);SF_CHECK(poly.nodes[0].voiceCount()==2&&a[3]==0&&a[4]==1&&a[7]==4&&a[8]==6);
    auto bad="GRAPH 3 d sampledelay 1 0 @ a a osc osc -1 440 wave sine 0 @ a ac o out 0 0 @ a a";
    bool rejected=false;try{AudioGraph::parse(bad);}catch(const std::runtime_error& e){rejected=std::string(e.what()).find("wavetable")!=std::string::npos;}SF_CHECK(rejected);
    // Multiple delay boundaries commit simultaneously, independent of graph order.
    auto two=AudioGraph::parse("GRAPH 4 a sampledelay 2 0 @ a a b sampledelay 0 0 @ a a m math 1 0 -1 1 add 0 @ a aa o out 0 0 @ a a");
    two.process(a.data(),8);SF_CHECK(a[0]==0&&a[1]==1&&a[2]==1&&a[3]==2&&a[7]==4);
    // Sample kernels must match ordinary block kernels, including UI smoothing.
    for(auto spec:{std::pair{"math","-1 0 -1 1 add 0"},std::pair{"mix","-1 0 -1 1 -1 .5"},std::pair{"gain","-1 0"},std::pair{"unitconvert","-1 1 factor percent"},std::pair{"filter","-1 -1 1000 -1 .707 lowpass"}}){
        auto block=createNodeProcessor(spec.first),sample=createNodeProcessor(spec.first);std::istringstream p(spec.second),q(spec.second);block->configure(p);sample->configure(q);block->prepare(48000,256);sample->prepare(48000,256);
        auto updated=createNodeProcessor(spec.first);std::string name=spec.first;
        std::istringstream update(name=="math"?"-1 0 -1 2 add 0":name=="mix"?"-1 0 -1 1 -1 .8":name=="filter"?"-1 -1 4000 -1 2 lowpass":spec.second);updated->configure(update);block->applyParameters(*updated);sample->applyParameters(*updated);
        std::array<float,256> input{};input[0]=1;const float* inputs[]={input.data(),nullptr,nullptr};
        ProcessContext c{inputs,block->sources.size(),a.data(),256,48000,nullptr};c.blockFrames=256;block->process(c);
        for(int i=0;i<256;i++){inputs[0]=input.data()+i;c.output=b.data()+i;c.bufferSize=1;c.sampleOffset=i;c.samplewise=true;sample->processSample(c);}for(int i=0;i<256;i++)SF_CHECK(std::abs(a[i]-b[i])<1e-6f);
    }
    // Expanded kernels use the same state and produce the same samples in both schedules.
    for(auto spec:{std::pair{"maprange","-1 0 -1 0 -1 1 -1 -1 -1 1 0"},std::pair{"clamp","-1 0 soft_clip"},std::pair{"curve","-1 0 0 1 0 1 0 2 0 0 0 0 0 0 0 1 1 1 1 1 1 0"},std::pair{"pan","-1 -1 .3"},std::pair{"osc","-1 440 wave sine -1"},std::pair{"additive","-1 sine 8 1 50 -1 440 -1 0"},std::pair{"noise","pink"},std::pair{"digitalnoise","-1 12000 -1 .5"}}){
        auto block=createNodeProcessor(spec.first),sample=createNodeProcessor(spec.first);SF_CHECK(block&&sample);
        std::istringstream p(spec.second),q(spec.second);block->configure(p);sample->configure(q);block->prepare(48000,256);sample->prepare(48000,256);sample->inheritState(*block);
        std::array<float,256> silence{},extraA{},extraB{},frequencyA{},frequencyB{};std::array<const float*,8> inputs{};float* outputsA[]={a.data(),extraA.data(),frequencyA.data()},*outputsB[]={b.data(),extraB.data(),frequencyB.data()};
        ValueEventBuffer evA,evB;ValueEventBuffer* eventsA[]={nullptr,&evA,nullptr},*eventsB[]={nullptr,&evB,nullptr};
        for(int pass=0;pass<2;pass++){
            evA.clear(256);evB.clear(256);ProcessContext c{inputs.data(),block->sources.size(),a.data(),256,48000,silence.data()};c.blockFrames=256;c.outputs=outputsA;c.outputCount=block->outputCount();c.valueEventOutputs=eventsA;block->process(c);
            for(int i=0;i<256;i++){outputsB[0]=b.data()+i;outputsB[1]=extraB.data()+i;outputsB[2]=frequencyB.data()+i;c.output=outputsB[0];c.outputs=outputsB;c.bufferSize=1;c.sampleOffset=i;c.samplewise=true;c.valueEventOutputs=eventsB;sample->processSample(c);}
            for(int i=0;i<256;i++){SF_CHECK(std::abs(a[i]-b[i])<1e-6f);SF_CHECK(extraA[i]==extraB[i]);SF_CHECK(frequencyA[i]==frequencyB[i]);}
            SF_CHECK(evA.count==evB.count);for(unsigned i=0;i<evA.count;i++)SF_CHECK(evA.events[i].sampleOffset==evB.events[i].sampleOffset);
        }
    }
    auto digital=AudioGraph::parse("GRAPH 3 d sampledelay 1 0 @ a a n digitalnoise -1 12000 0 .5 @ ae aa o out 1 0 @ a a");
    digital.process(a.data(),256);auto& events=digital.nodes[1].valueEvents[0][1];SF_CHECK(events.count>10);SF_CHECK(events.events[1].sampleOffset>0);
    auto oscillator=AudioGraph::parse("GRAPH 3 d sampledelay 1 0 @ a a n osc 0 440 @ a a o out 1 0 @ a a");oscillator.process(a.data(),256);
    bool tableRejected=false;try{AudioGraph::parse("GRAPH 3 d sampledelay 1 0 @ a a n osc -1 440 wave sine 0 @ a ac o out 1 0 @ a a");}catch(const std::runtime_error& e){tableRejected=std::string(e.what()).find("wavetable")!=std::string::npos;}SF_CHECK(tableRejected);
    auto panned=AudioGraph::parse("GRAPH 4 d sampledelay 2 0 @ a a m math 0 0 -1 .1 add 0 @ a aa p pan 1 -1 -1 @ a aa o out 2 0 @ a a");panned.process(a.data(),16,0,nullptr);panned.process(b.data(),16,1,nullptr);SF_CHECK(a[1]>a[0]&&std::abs(b[0])<1e-6f);
    // A complete external wavetable stays block-sized even inside a sample island.
    auto tableGraph=AudioGraph::parse("GRAPH 4 t osc -1 440 @ a a d sampledelay 2 0 @ a a n osc 1 440 wave sine 0 @ a ac o out 2 0 @ a a");
    tableGraph.process(a.data(),256);for(float value:a)SF_CHECK(std::isfinite(value));
    std::array<float,256> table{};for(int i=0;i<256;i++)table[i]=std::sin(i*.0245436926f);
    auto ob=createNodeProcessor("osc"),os=createNodeProcessor("osc");std::istringstream obp("-1 440 wave sine -1"),osp("-1 440 wave sine -1");ob->configure(obp);os->configure(osp);ob->prepare(48000,256);os->prepare(48000,256);
    const float* waveInputs[]={nullptr,table.data()};ProcessContext wc{waveInputs,2,a.data(),256,48000,nullptr};wc.blockFrames=256;ob->process(wc);
    for(int i=0;i<256;i++){wc.output=b.data()+i;wc.bufferSize=1;wc.sampleOffset=i;wc.samplewise=true;os->processSample(wc);}for(int i=0;i<256;i++)SF_CHECK(std::abs(a[i]-b[i])<1e-6f);
    // Shared processors belong to one island and run once, not once per loop.
    auto overlap=AudioGraph::parse("GRAPH 4 d sampledelay 2 0 @ a a e sampledelay 2 0 @ a a m math 0 0 1 0 add 0 @ a aa o out 2 0 @ a a");
    SF_CHECK(overlap.steps.size()==2&&overlap.steps[0].nodes.size()==3);
    auto disjoint=AudioGraph::parse("GRAPH 6 d sampledelay 1 0 @ a a m math 0 0 -1 1 add 0 @ a aa e sampledelay 3 0 @ a a n math 2 0 -1 2 add 0 @ a aa o math 1 0 3 0 add 0 @ a aa out out 4 0 @ a a");
    SF_CHECK(std::count_if(disjoint.steps.begin(),disjoint.steps.end(),[](const auto& step){return step.sample;})==2);
    auto filtered=AudioGraph::parse("GRAPH 5 d sampledelay 3 0 @ a a gain math 0 0 -1 .5 multiply 0 @ a aa sum math 1 0 -1 1 add 0 @ a aa f filter 2 -1 1000 -1 .707 lowpass @ a aaa o out 0 0 @ a a");
    filtered.process(a.data(),256);SF_CHECK(a[0]==0&&a[255]>1&&a[255]<3);for(float value:a)SF_CHECK(std::isfinite(value));
    // Compare like-for-like topology; this is CPU cost, not audio-device latency.
    auto blockText=feedback;blockText.replace(blockText.find("sampledelay"),11,"blockdelay");auto block=AudioGraph::parse(blockText);
    auto elapsed=[&](AudioGraph& g){for(int i=0;i<100;i++)g.process(a.data(),256);auto start=std::chrono::steady_clock::now();for(int i=0;i<3000;i++)g.process(a.data(),256);return std::chrono::duration<double,std::micro>(std::chrono::steady_clock::now()-start).count()/3000;};
    double blockUs=elapsed(block),sampleUs=elapsed(graph);std::cout<<"Feedback benchmark: block "<<blockUs<<" us, sample "<<sampleUs<<" us, ratio "<<sampleUs/blockUs<<"\n";
    std::cout<<"Sample feedback timing, control, stereo, MIDI voices, kernels and state tests passed\n";
}
#undef SF_CHECK
}
