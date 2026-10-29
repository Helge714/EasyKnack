#include "AudioGraph.hpp"
#include "EXPORT_HEADER"
#include <cassert>
using namespace easyknack;
static void click(){
    std::vector<std::string> keys;for(auto& entry:manualTriggerMailboxes)if(!entry.second.expired())keys.push_back(entry.first);
    for(auto& key:keys)submitManualTrigger(key);
}
template<class T> void check(const char* payload){
    auto node=std::make_unique<T>();std::istringstream params(payload);node->configure(params);node->prepare(48000,256);node->bindIdentity("hold-test");
    SpectrumStream input;input.prepare(256);input.size=input.windowSize=1024;input.hop=256;input.identity=17;input.sampleRate=48000;
    float out[256]{},silence[256]{};const float* inputs[17]{};char rates[17]{};std::fill_n(rates,17,'c');rates[1]='e';
    ValueEventBuffer events;const ValueEventBuffer* eventInputs[17]{};eventInputs[1]=&events;
    const DspResource* resources[17]{};resources[0]=&input;
    ProcessContext c{inputs,17,out,256,48000,silence,256};c.resourceInputs=resources;c.sourceRates=rates;c.valueEventInputs=eventInputs;
    auto run=[&](int block,int end,int value,bool trigger,int expected){
        ++manualTriggerEpoch;input.blockStart=block;input.count=end?1:0;input.frames[0].start=end-1024;
        for(int k=0;k<=512;k++)input.frames[0].bins[k]={value?double(value+k):0.,0};SpectrumStream::realSymmetry(input.frames[0],1024);
        events.clear(256);if(trigger){if(EXTERNAL_TRIGGER)events.push(7,1,ValueEventKind::trigger);else click();}
        node->process(c);auto* result=static_cast<SpectrumStream*>(node->resourceOutput(0));
        assert(result->count==(end?1:0));
        if(end)for(int k=0;k<=512;k++)assert(result->frames[0].bins[k].real()==expected+k);
    };
    run(0,256,5,false,5); // Before capture: pass through.
    run(256,0,0,true,0); // A short trigger with no FFT result yet.
    run(512,512,10,false,10); // Delayed result captures the historical trigger.
    run(768,768,20,true,10); // Today's trigger must not affect an older FFT frame.
    run(1024,1024,30,false,30);
    run(1280,1280,0,false,30); // Held bins remain even when live bins become empty.
    auto replacement=std::make_unique<T>();std::istringstream again(payload);replacement->configure(again);replacement->prepare(48000,256);replacement->bindIdentity("hold-test");replacement->adoptState(*node);node=std::move(replacement);
    run(1536,1536,40,false,30);
    input.identity=18;run(1792,2048,50,false,50); // A new spectrum invalidates bin positions.
}
static void channels(){
    SpectrumStream source;source.prepare(256);source.size=source.windowSize=1024;source.identity=33;
    BinHoldNode left,right;for(auto* node:{&left,&right}){node->prepare(48000,256);node->bindIdentity("stereo");node->frame=&source.frames[0];node->bin=32;}
    float a=2,b=7,out=0,zero=0;const float* inputs[]{&a,nullptr};ProcessContext c{inputs,2,&out,1,48000,&zero};
    ++manualTriggerEpoch;submitManualTrigger("stereo:trigger");
    left.beginBlock(source);right.beginBlock(source);left.beginFrame(256);right.beginFrame(256);
    left.process(c);assert(out==2);inputs[0]=&b;right.process(c);assert(out==7);
    left.beginFrame(512);right.beginFrame(512);a=9;b=11;
    inputs[0]=&a;left.process(c);assert(out==2);inputs[0]=&b;right.process(c);assert(out==7);
}
int main(){channels();check<SpectralGraphNode>(DIRECT_PAYLOAD);check<EXPORT_TYPE>(EXPORT_PAYLOAD);}
