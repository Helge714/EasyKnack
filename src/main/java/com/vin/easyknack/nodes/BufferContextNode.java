package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.resource.buffercontext",title="Buffer Context",category=NodeCategory.BUFFER,kind=NodeKind.HISTORY)
public final class BufferContextNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("buffer"),SIZE=new PropertyId("size"),WRITE_POSITION=new PropertyId("write-position");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(SIZE,"Size").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .formats(new UnitFormats(UnitFormat.SAMPLE,UnitFormat.SAMPLE,UnitFormats.TIME_SECONDS.choices()));
        n.addOutput(WRITE_POSITION,"Write position").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO)
            .tooltip(NodeText.text("buffer.writePosition.help","Normalized next write position, 0–1, wrapping for recording buffers. File buffers report 1."));
        n.addInput(IN,"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL).optional();
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("buffercontext","nodes/BufferContextNode.hpp",ParameterContract.builder().source(IN).build());}
}
