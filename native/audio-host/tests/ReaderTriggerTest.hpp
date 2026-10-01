#pragma once
// Independent sample-time expectations, including reverse crossings and stereo latching.
void readerTriggers(){
    HistoryResource history;history.prepare(8,1,8);
    for(int i=0;i<8;i++)history.append(static_cast<float>(i));
    history.beginBlock(8);for(int i=8;i<16;i++)history.append(static_cast<float>(i));
    std::array<float,8> audio{},distance{},pulse{},silence{};
    const float* inputs[]{nullptr,nullptr,nullptr,nullptr};
    const DspResource* resources[]{&history,nullptr,nullptr,nullptr};
    float* outputs[]{audio.data(),distance.data(),pulse.data()};
    ValueEventBuffer resets,crossings;resets.clear(8);crossings.clear(8);
    resets.push(3,1,ValueEventKind::trigger);resets.push(3,1,ValueEventKind::trigger);resets.push(6,1,ValueEventKind::trigger);
    const ValueEventBuffer* events[]{nullptr,nullptr,nullptr,&resets};
    ValueEventBuffer* resultEvents[]{nullptr,nullptr,&crossings};
    ProcessContext c{inputs,4,audio.data(),8,8,silence.data(),8};
    c.resourceInputs=resources;c.outputs=outputs;c.outputCount=3;c.valueEventInputs=events;c.valueEventOutputs=resultEvents;
    HistoryReaderNode first;first.speed=0;first.process(c);
    assert(audio[3]==11&&audio[4]==11&&audio[6]==14&&audio[7]==14);
    assert(pulse[3]==3.f/8&&pulse[6]==6.f/8); // Position follows the sample-accurate reset.
    HistoryReaderNode second;second.speed=0;second.offset=.5;second.process(c);
    assert(audio[3]==7&&audio[4]==7&&audio[6]==10&&audio[7]==10);
    events[3]=nullptr;
    HistoryReaderNode faster;faster.speed=2;faster.process(c);assert(pulse[0]==0&&pulse[1]==.25f&&pulse[4]==0);
    HistoryReaderNode still;still.speed=0;still.process(c);for(float p:pulse)assert(p==0);
    history.beginBlock(8);for(int i=16;i<24;i++)history.append(static_cast<float>(i));
    still.process(c);for(float p:pulse)assert(p==0);
    HistoryReaderNode reverse;reverse.speed=-1;reverse.process(c);assert(pulse[0]==0&&pulse[1]==.875f);

    TriggerInputNode trigger,right;trigger.bindIdentity("test-trigger");right.shareChannelState(trigger);
    ValueEventBuffer leftEvents,rightEvents;leftEvents.clear(8);rightEvents.clear(8);
    ValueEventBuffer* triggerOut[]{&leftEvents};c.outputCount=1;c.valueEventOutputs=triggerOut;
    submitManualTrigger("test-trigger:trigger");submitManualTrigger("test-trigger:trigger");++manualTriggerEpoch;
    trigger.processEventBlock(c);assert(leftEvents.count==1&&leftEvents.events[0].sampleOffset==0);
    submitManualTrigger("test-trigger:trigger"); // arrives between channels; defer equally to next block
    triggerOut[0]=&rightEvents;right.processEventBlock(c);assert(rightEvents.count==1);
    ++manualTriggerEpoch;leftEvents.clear(8);rightEvents.clear(8);
    triggerOut[0]=&leftEvents;trigger.processEventBlock(c);triggerOut[0]=&rightEvents;right.processEventBlock(c);
    assert(leftEvents.count==1&&rightEvents.count==1);
    ++manualTriggerEpoch;leftEvents.clear(8);triggerOut[0]=&leftEvents;trigger.processEventBlock(c);assert(leftEvents.count==0);
    HistoryReaderNode manual;manual.bindIdentity("test-reset");submitManualTrigger("test-reset:reset");++manualTriggerEpoch;
    c.outputCount=3;c.valueEventOutputs=resultEvents;manual.offset=.5;manual.speed=0;crossings.clear(8);manual.process(c);
    assert(audio[0]==12&&audio[7]==20); // writer catches a stationary reader at sample 4
}
