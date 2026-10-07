package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.vector.dot",title="Vector Dot Product",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorDotNode extends BasicNode {
    public static final PropertyId A=new PropertyId("a"),B=new PropertyId("b"),OUT=new PropertyId("dot");
    public void define(NodeBuilder n,NodeState s){n.addOutput(OUT,"Dot Product").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);VectorSupport.input(n,A,"A",0,0,0);VectorSupport.input(n,B,"B",0,0,0);ProcessingRate.addEditor(n,VectorSupport.RATE);}
    public NativeProcessor processor(){return new NativeProcessor("vectordot","nodes/VectorDotNode.hpp",VectorSupport.vector(VectorSupport.vector(ParameterContract.builder(),A,0,0,0),B,0,0,0).build());}
}
