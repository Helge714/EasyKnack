package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.hilbert", title="Hilbert", category=NodeCategory.EFFECT, kind=NodeKind.AUDIO)
public final class HilbertNode extends BasicNode {
    public static final PropertyId LENGTH=new PropertyId("length");
    public static final PropertyId IN=new PropertyId("audio-in"), DIRECT=new PropertyId("direct"), QUADRATURE=new PropertyId("quadrature");
    @Override public void define(NodeBuilder node, NodeState state) {
        node.addOutput(DIRECT,"Direct");
        node.addOutput(QUADRATURE,"Quadrature");
        node.addInput(IN,"Audio In");
        node.addEditor(LENGTH,"Filter length",Editor.comboBox(java.util.List.of("61","129","257","513","1025","2049"),"257",v->v));
    }
    @Override public NativeProcessor processor() {
        return new NativeProcessor("hilbert","nodes/HilbertNode.hpp",ParameterContract.builder().source(IN).value(LENGTH).build());
    }
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public void mute(MuteRouting routing,NodeState state){routing.connect(IN,DIRECT);}
    @Override public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,DIRECT);}
}
