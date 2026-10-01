package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.resource.history",title="Buffer",category=NodeCategory.BUFFER,kind=NodeKind.AUDIO)
public final class HistoryBufferNode extends BasicNode {
    public static final PropertyId OUT=new PropertyId("history"),IN=new PropertyId("audio-in"),SECONDS=new PropertyId("seconds");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Audio In");
        n.addEditor(SECONDS,"Size",Editor.Slider.button(2,com.vin.easyknack.audio.AudioProcessing.BLOCK_FRAMES/com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE,30.0).formats(UnitFormats.TIME_SECONDS)).tooltip(com.vin.easyknack.ui.AppText.literal("Buffer duration (seconds, milliseconds or samples at the DSP sample rate). Minimum: one DSP block (256 samples). Resizing allocates a new empty buffer outside the audio thread and resets its readers."));
    }
    public NativeProcessor processor(){return new NativeProcessor("history","nodes/HistoryBufferNode.hpp",ParameterContract.builder().source(IN).value(SECONDS).build());}
}
