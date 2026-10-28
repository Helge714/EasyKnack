package com.vin.easyknack.nodes;
/** Typed instance boundary, separate from existing numeric instance contracts. */
@RegisterNode(id="easyknack.multiple.vectorinput",title="Instance Vector Input",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class MultipleVectorInputNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Vector").dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rate(ProcessingRate.AUDIO);
  n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.0,8.0).integers().discrete());
 }
 public NativeProcessor processor(){return new NativeProcessor("multiplevectorinput","nodes/MultipleVectorInputNode.hpp",ParameterContract.builder().integer(new PropertyId("slot")).build());}
}
