package com.vin.easyknack.nodes;
/** Typed instance boundary, separate from existing numeric instance contracts. */
@RegisterNode(id="easyknack.multiple.waveforminput",title="Instance Wavetable Input",category=NodeCategory.SPECTRAL,kind=NodeKind.WAVETABLE)
public final class MultipleWaveformInputNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Wavetable").dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
  n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.0,null).integers().discrete());
 }
 public NativeProcessor processor(){return new NativeProcessor("multiplewaveforminput","nodes/MultipleWaveformInputNode.hpp",ParameterContract.builder().integer(new PropertyId("slot")).build());}
}
