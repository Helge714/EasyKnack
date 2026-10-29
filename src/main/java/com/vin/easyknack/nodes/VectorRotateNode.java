package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.vector.rotate",title="Vector Rotate",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorRotateNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("input"), ROTATION=new PropertyId("rotation"), ORIGIN=new PropertyId("origin"), OUT=new PropertyId("vector");
    @Override public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,OUT);}
    public void define(NodeBuilder n,NodeState s){
        VectorSupport.output(n,OUT);
        VectorSupport.input(n,IN,"Vector",0,0,0);
        n.addInput(ROTATION,"Rotation",new VectorEditor(0,0,0,new UnitFormats(UnitFormat.DEGREE,UnitFormat.DEGREE,java.util.List.of(UnitFormat.DEGREE,UnitFormat.RADIAN,UnitFormat.TURN)))).dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL);
        VectorSupport.input(n,ORIGIN,"Origin",0,0,0);
        ProcessingRate.addEditor(n,VectorSupport.RATE);
    }
    public NativeProcessor processor(){return new NativeProcessor("vectorrotate","nodes/VectorRotateNode.hpp",
        VectorSupport.vector(VectorSupport.vector(VectorSupport.vector(ParameterContract.builder(),IN,0,0,0),ROTATION,0,0,0),ORIGIN,0,0,0).build());}
}
