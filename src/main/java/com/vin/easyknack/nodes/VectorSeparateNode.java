package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.vector.separate",title="Vector Separate",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorSeparateNode extends GenericVectorNode {
    public static final PropertyId IN=new PropertyId("vector");
    public void define(NodeBuilder n,NodeState s){for(String axis:java.util.List.of("x","y","z"))n.addOutput(new PropertyId(axis),axis.toUpperCase()).formats(VectorQuantity.formats(s)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);VectorQuantity.input(n,IN,"Vector",s);VectorQuantity.addEditor(n);ProcessingRate.addEditor(n,VectorSupport.RATE);}
    public NativeProcessor processor(){return new NativeProcessor("vectorseparate","nodes/VectorSeparateNode.hpp",VectorSupport.vector(ParameterContract.builder(),IN,0,0,0).build());}
}
