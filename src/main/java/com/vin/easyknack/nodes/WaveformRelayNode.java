package com.vin.easyknack.nodes;
/** Internal fan-out boundary; retains the original immutable wavetable resource. */
@RegisterNode(id="easyknack.waveform.relay",title="Wavetable Relay",category=NodeCategory.SPECTRAL,kind=NodeKind.WAVETABLE)
public final class WaveformRelayNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Wavetable").dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
  n.addInput(new PropertyId("in"),"Wavetable").dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
 }
 public NativeProcessor processor(){return new NativeProcessor("waveformrelay","nodes/WaveformRelayNode.hpp",ParameterContract.builder().source(new PropertyId("in")).build());}
}
