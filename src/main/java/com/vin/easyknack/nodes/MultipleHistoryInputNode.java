package com.vin.easyknack.nodes;
/** Typed instance boundary, separate from existing numeric instance contracts. */
@RegisterNode(id="easyknack.multiple.historyinput",title="Instance Buffer Input",category=NodeCategory.BUFFER,kind=NodeKind.HISTORY)
public final class MultipleHistoryInputNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
  n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.0,null).integers().discrete());
 }
 public NativeProcessor processor(){return new NativeProcessor("multiplehistoryinput","nodes/MultipleHistoryInputNode.hpp",ParameterContract.builder().integer(new PropertyId("slot")).build());}
}
