package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.vector.scale",title="Vector Scale",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorScaleNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("input"), SCALE=new PropertyId("scale"), ORIGIN=new PropertyId("origin"), OUT=new PropertyId("vector");
    @Override public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,OUT);}
    public void define(NodeBuilder n,NodeState s){
        VectorSupport.output(n,OUT);
        VectorSupport.input(n,IN,"Vector",0,0,0);
        n.addInput(SCALE,"Scale",new VectorEditor(1,1,1,UnitFormats.PROPORTION)).dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL).editorCopyable(false);
        VectorSupport.input(n,ORIGIN,"Origin",0,0,0);
        ProcessingRate.addEditor(n,VectorSupport.RATE);
    }
    public NativeProcessor processor(){return new NativeProcessor("vectorscale","nodes/VectorScaleNode.hpp",
        VectorSupport.vector(VectorSupport.vector(VectorSupport.vector(ParameterContract.builder(),IN,0,0,0),SCALE,1,1,1),ORIGIN,0,0,0).build());}
}
