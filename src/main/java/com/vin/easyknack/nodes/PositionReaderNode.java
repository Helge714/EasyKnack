package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.resource.positionreader",title="Buffer Reader (Position)",category=NodeCategory.BUFFER,kind=NodeKind.AUDIO)
public final class PositionReaderNode extends BasicNode {
    public static final PropertyId OUT=new PropertyId("audio-out"),DISTANCE=new PropertyId("distance"),READ_POSITION=new PropertyId("read-position"),BUFFER=new PropertyId("history"),POSITION=new PropertyId("position"),OFFSET=new PropertyId("offset");
    public static final PropertyId WRAP=new PropertyId("wrap");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out");
        n.addOutput(DISTANCE,"Distance").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.i18n.AppText.literal("Shortest distance to the write head: 0 at the head, 1 halfway around the buffer. Includes Offset; suitable for a crossfade weight."));
        n.addOutput(READ_POSITION,"Read position").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.i18n.AppText.literal("Actual circular buffer position, 0–1, including Offset. Can drive another Position Reader."));
        n.addEditor(WRAP,"Wrap",new Editor.CheckBox(true)).tooltip(com.vin.easyknack.i18n.AppText.text("buffer.wrapHint"));
        n.addInput(BUFFER,"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
        n.addInput(POSITION,"Position",Editor.Slider.bar(0,0,1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).tooltip(com.vin.easyknack.i18n.AppText.literal("Absolute position in the buffer: 0–1, wrapping at the ends. No internal clock. Connect a Buffer Reader's Read position using the same Buffer; the follower remains synchronized even when connected later."));
        n.addInput(OFFSET,"Offset",Editor.Slider.bar(0,0,1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).tooltip(com.vin.easyknack.i18n.AppText.literal("Fraction of the buffer subtracted from Position. Use 0.5 for a second head halfway around the buffer. No automatic crossfade; unwritten samples are silent."));
    }
    public NativeProcessor processor(){return new NativeProcessor("positionreader","nodes/PositionReaderNode.hpp",ParameterContract.builder().source(BUFFER).source(POSITION).value(POSITION).source(OFFSET).value(OFFSET).value(WRAP).build());}
}
