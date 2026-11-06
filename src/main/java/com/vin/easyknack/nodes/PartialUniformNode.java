package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.partials.uniform",title="Partial Frame Input",category=NodeCategory.SPECTRAL,kind=NodeKind.VALUE)
public final class PartialUniformNode extends BasicNode {
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){n.addOutput(new PropertyId("out"),"Value").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.,255.).integers()).inspectorOnly();}
    public NativeProcessor processor(){return new NativeProcessor("partialuniform","nodes/PartialGraphNode.hpp",ParameterContract.builder().value(new PropertyId("slot")).build());}
}
