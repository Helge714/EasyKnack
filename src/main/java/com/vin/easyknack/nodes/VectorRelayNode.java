package com.vin.easyknack.nodes;
/** Internal fan-out boundary; forwards vector resources without copying component buffers. */
@RegisterNode(id="easyknack.vector.relay",title="Vector Relay",category=NodeCategory.VECTOR,kind=NodeKind.VECTOR)
public final class VectorRelayNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Vector").dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL);
  n.addInput(new PropertyId("in"),"Vector").dataType(PortDataType.VECTOR).signal(SignalCategory.VECTOR).rates(PortRates.AUDIO_CONTROL);
 }
 public NativeProcessor processor(){return new NativeProcessor("vectorrelay","nodes/VectorRelayNode.hpp",ParameterContract.builder().source(new PropertyId("in")).build());}
}
