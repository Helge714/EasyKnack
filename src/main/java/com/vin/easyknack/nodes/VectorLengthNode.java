package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.vector.length",title="Vector Length",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorLengthNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("vector"),OUT=new PropertyId("length");
    public void define(NodeBuilder n,NodeState s){n.addOutput(OUT,"Length").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);VectorSupport.input(n,IN,"Vector",0,0,0);ProcessingRate.addEditor(n,VectorSupport.RATE);}
    public NativeProcessor processor(){return new NativeProcessor("vectorlength","nodes/VectorLengthNode.hpp",VectorSupport.vector(ParameterContract.builder(),IN,0,0,0).build());}
}
