package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.vector.distance",title="Vector Distance",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorDistanceNode extends GenericVectorNode {
    public static final PropertyId A=new PropertyId("a"),B=new PropertyId("b"),OUT=new PropertyId("distance");
    public void define(NodeBuilder n,NodeState s){n.addOutput(OUT,"Distance [m]").formats(VectorQuantity.formats(s)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);VectorQuantity.input(n,A,"A",s);VectorQuantity.input(n,B,"B",s);VectorQuantity.addEditor(n);ProcessingRate.addEditor(n,VectorSupport.RATE);}
    public NativeProcessor processor(){return new NativeProcessor("vectordistance","nodes/VectorDistanceNode.hpp",VectorSupport.vector(VectorSupport.vector(ParameterContract.builder(),A,0,0,0),B,0,0,0).build());}
    @Override public PropertyId rerouteConnection(PropertyId occupied,NodeState state){
        return occupied.equals(A)?B:occupied.equals(B)?A:null;
    }
}
