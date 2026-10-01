package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.multiple.input",title="Instance Input",category=NodeCategory.ROUTING,kind=NodeKind.VALUE)
public final class MultipleInputNode extends BasicNode {
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("out"),"Out").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);
        n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.0,8.0).integers());
    }
    public NativeProcessor processor(){return new NativeProcessor("multipleinput","nodes/MultipleInputNode.hpp",ParameterContract.builder().integer(new PropertyId("slot")).build());}
}
