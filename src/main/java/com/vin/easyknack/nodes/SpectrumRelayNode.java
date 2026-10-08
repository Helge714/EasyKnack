package com.vin.easyknack.nodes;
/** Internal fan-out boundary; retains the original immutable spectrum frames. */
@RegisterNode(id="easyknack.spectrum.relay",title="Spectrum Relay",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class SpectrumRelayNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
  n.addInput(new PropertyId("in"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
 }
 public NativeProcessor processor(){return new NativeProcessor("spectrumrelay","nodes/SpectrumRelayNode.hpp",ParameterContract.builder().source(new PropertyId("in")).build());}
}
