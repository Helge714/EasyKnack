package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.partials.noise",title="Partial Noise",category=NodeCategory.SPECTRAL,kind=NodeKind.PARTIALS)
public final class PartialNoiseNode extends BasicNode {
    public static final PropertyId OUT=new PropertyId("out"),SEED=new PropertyId("seed"),BY_PARTIAL=new PropertyId("by-partial"),BY_FRAME=new PropertyId("by-frame");
    @Override public boolean requiresPartialContext(){return true;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Random").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.ui.AppText.text("partials.noise.help"));
        n.addEditor(SEED,"Seed",Editor.Slider.button(0,0.,2147483647.).integers().discrete());
        n.addEditor(BY_PARTIAL,"Vary by partial",Editor.checkBox(true));
        n.addEditor(BY_FRAME,"Vary by frame",Editor.checkBox(true));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("partialnoise","nodes/PartialNoiseNode.hpp",ParameterContract.builder().integer(SEED).value(BY_PARTIAL).value(BY_FRAME).build());}
}
