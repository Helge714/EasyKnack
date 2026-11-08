package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.resource.historyreader",title="Buffer Reader (Speed)",category=NodeCategory.BUFFER,kind=NodeKind.AUDIO)
public final class HistoryReaderNode extends BasicNode {
    public static final PropertyId RESET=new PropertyId("reset"),READ_POSITION=new PropertyId("read-position");
    public static final PropertyId OUT=new PropertyId("audio-out"),DISTANCE=new PropertyId("distance"),HISTORY=new PropertyId("history"),SPEED=new PropertyId("speed"),OFFSET=new PropertyId("offset");
    public static final PropertyId WRAP=new PropertyId("wrap");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out");
        n.addOutput(DISTANCE,"Distance").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.i18n.AppText.literal("Shortest circular distance to the sample-accurate write head: 0 at the write head, 1 halfway around the buffer. Includes Offset. Each sample sees its own write and earlier writes, never later writes in the block."));
        n.addOutput(READ_POSITION,"Read position").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.i18n.AppText.literal("Actual position in the circular buffer, 0–1 (1 wraps to 0), including Offset. Connect to a Position Reader's Position input using the same Buffer to keep it sample-synchronized."));
        n.addEditor(WRAP,"Wrap",new Editor.CheckBox(true)).tooltip(com.vin.easyknack.i18n.AppText.text("buffer.wrapHint"));
        n.addInput(HISTORY,"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
        n.addInput(SPEED,"Speed",Editor.Slider.button(1,-8.0,8.0)).signal(SignalCategory.VALUE).tooltip(com.vin.easyknack.i18n.AppText.literal("1: normal speed; 0: hold position; negative: reverse. This reader owns the playback clock. Use Read position to drive a Position Reader."));
        n.addInput(OFFSET,"Offset",Editor.Slider.bar(0,0.0,1.0)).signal(SignalCategory.VALUE).tooltip(com.vin.easyknack.i18n.AppText.literal("Fraction of the buffer length behind the cursor: 0 and 1 are the same position; 0.5 is halfway around. Reading wraps; unwritten buffer regions are silent. No automatic crossfade."));
        n.addInput(RESET,"Reset",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).tooltip(com.vin.easyknack.i18n.AppText.literal("Reset to the current sample's write position, then apply Offset. Unconnected: click to reset at the next block."));
    }
    public NativeProcessor processor(){return new NativeProcessor("historyreader","nodes/HistoryReaderNode.hpp",ParameterContract.builder().token("normalized-position-v3").source(HISTORY).source(SPEED).value(SPEED).source(OFFSET).value(OFFSET).source(RESET).value(WRAP).build());}
}
