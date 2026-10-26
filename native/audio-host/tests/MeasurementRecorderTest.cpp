#include "AudioGraph.hpp"
#include <cassert>
using namespace easyknack;
int main(){
    auto node=std::make_unique<MeasurementRecorderNode>();node->count=2;node->bindIdentity("recorder-test");node->prepare(48000,8);
    float trigger[8]{},a[8]{1,2,3,4,5,6,7,8},b[8]{42},out[8]{};const float* inputs[]={trigger,a,b};char rates[]={'e','a','c'};
    ProcessContext c{inputs,3,out,8,48000,out,8};c.sourceRates=rates;
    ValueEventBuffer events;events.clear(8);events.push(3,1,ValueEventKind::trigger);const ValueEventBuffer* eventInputs[]={&events,nullptr,nullptr};c.valueEventInputs=eventInputs;
    node->process(c);assert(node->stored==1&&node->rows[0]==3&&node->rows[5]==4&&node->rows[6]==42);
    // Polling can skip several capture blocks without losing any rows; the full table is retained.
    node->process(c);node->process(c);node->process(c);assert(node->stored==4&&node->submitted==3);
    auto frame=std::make_unique<AnalysisFrame>();assert(node->mailbox->latest(*frame));assert(frame->values[3]==3);
    events.clear(8);node->process(c);assert(node->submitted==4);assert(node->mailbox->latest(*frame)&&frame->values[3]==4);
    // Handover preserves records, clock and session; a new Clear generation does not inherit them.
    auto next=std::make_unique<MeasurementRecorderNode>();next->count=2;next->bindIdentity("recorder-test");next->prepare(48000,8);next->adoptState(*node);
    assert(next->stored==4&&next->session==node->session&&next->samples==node->samples);
    auto cleared=std::make_unique<MeasurementRecorderNode>();cleared->generation=1;cleared->count=2;cleared->prepare(48000,8);cleared->adoptState(*next);assert(cleared->stored==0);
    // Manual recording works without numeric connections, and missing/non-finite values are explicit.
    inputs[0]=nullptr;inputs[1]=nullptr;next->fallback[0]=12;next->fallback[1]=0;
    c.valueEventInputs=nullptr;submitManualTrigger("recorder-test:record");++manualTriggerEpoch;next->process(c);
    assert(next->stored==5&&next->rows[4*7+5]==12);
    // Capture is bounded without overwriting earlier rows.
    trigger[0]=1;inputs[0]=trigger;for(int i=0;i<4100;i++)next->process(c);
    assert(next->stored==4096&&next->rows[0]==3&&next->rows[5]==4);
    assert(next->observesWithoutInputs());
}
