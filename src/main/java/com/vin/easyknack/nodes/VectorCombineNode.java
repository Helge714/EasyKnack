package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.vector.combine",title="Vector Combine",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorCombineNode extends GenericVectorNode {
    public static final PropertyId X=new PropertyId("x"),Y=new PropertyId("y"),Z=new PropertyId("z"),OUT=new PropertyId("vector");
    public void define(NodeBuilder n,NodeState s){VectorSupport.output(n,OUT);for(var id:java.util.List.of(X,Y,Z))n.addInput(id,id.value().toUpperCase(),Editor.Slider.button(0).formats(VectorQuantity.formats(s))).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);VectorQuantity.addEditor(n);ProcessingRate.addEditor(n,VectorSupport.RATE);}
    public NativeProcessor processor(){return new NativeProcessor("vectorcombine","nodes/VectorCombineNode.hpp",ParameterContract.builder().source(X).source(Y).source(Z).value(X).value(Y).value(Z).build());}
}
