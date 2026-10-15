package com.vin.easyknack.nodes;
/** Typed instance boundary, separate from existing numeric instance contracts. */
@RegisterNode(id="easyknack.multiple.spectruminput",title="Instance Spectrum Input",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class MultipleSpectrumInputNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
  n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.0,8.0).integers());
 }
 public NativeProcessor processor(){return new NativeProcessor("multiplespectruminput","nodes/MultipleSpectrumInputNode.hpp",ParameterContract.builder().integer(new PropertyId("slot")).build());}
}
