package com.vin.easyknack.nodes;
/** Internal fan-out boundary; retains the original buffer resource. */
@RegisterNode(id="easyknack.history.relay",title="Buffer Relay",category=NodeCategory.BUFFER,kind=NodeKind.HISTORY)
public final class HistoryRelayNode extends BasicNode {
 public boolean isListedInCatalog(){return false;}
 public void define(NodeBuilder n,NodeState s){
  n.addOutput(new PropertyId("out"),"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
  n.addInput(new PropertyId("in"),"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL);
 }
 public NativeProcessor processor(){return new NativeProcessor("historyrelay","nodes/HistoryRelayNode.hpp",ParameterContract.builder().source(new PropertyId("in")).build());}
}
