package com.vin.easyknack.nodes;

/** Component scale followed by Euler X, Y, Z rotation about the origin. */
@RegisterNode(id="easyknack.vector.mapping", title="Vector Mapping", category=NodeCategory.VECTOR, kind=NodeKind.VECTOR)
public final class VectorMappingNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("input"), SCALE=new PropertyId("scale"),
            ROTATION=new PropertyId("rotation"), OUT=new PropertyId("vector");
    public void define(NodeBuilder n, NodeState s) {
        VectorSupport.output(n,OUT);
        VectorSupport.input(n,IN,"Vector",0,0,0);
        input(n,SCALE,"Scale",new VectorEditor(1,1,1,UnitFormats.PROPORTION));
        input(n,ROTATION,"Rotation",new VectorEditor(0,0,0,new UnitFormats(UnitFormat.DEGREE,
                UnitFormat.DEGREE,java.util.List.of(UnitFormat.DEGREE,UnitFormat.RADIAN,UnitFormat.TURN))));
        ProcessingRate.addEditor(n,VectorSupport.RATE);
    }
    private static void input(NodeBuilder n,PropertyId id,String label,VectorEditor editor) {
        n.addInput(id,label,editor).dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR)
                .rates(PortRates.AUDIO_CONTROL).editorCopyable(false);
    }
    public NativeProcessor processor() {
        return new NativeProcessor("vectormapping","nodes/VectorMappingNode.hpp",
                VectorSupport.vector(VectorSupport.vector(VectorSupport.vector(ParameterContract.builder(),
                        IN,0,0,0),SCALE,1,1,1),ROTATION,0,0,0).build());
    }
}
