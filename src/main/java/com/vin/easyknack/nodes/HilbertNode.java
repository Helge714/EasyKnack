package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.hilbert", title="Hilbert", category=NodeCategory.EFFECT, kind=NodeKind.AUDIO)
public final class HilbertNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"), DIRECT=new PropertyId("direct"), QUADRATURE=new PropertyId("quadrature");
    @Override public void define(NodeBuilder node, NodeState state) {
        node.addOutput(DIRECT,"Direct");
        node.addOutput(QUADRATURE,"Quadrature");
        node.addInput(IN,"Audio In");
    }
    @Override public NativeProcessor processor() {
        return new NativeProcessor("hilbert","nodes/HilbertNode.hpp",ParameterContract.builder().source(IN).build());
    }
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public void mute(MuteRouting routing,NodeState state){routing.connect(IN,DIRECT);}
    @Override public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,DIRECT);}
}
