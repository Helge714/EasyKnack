package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.converter.triggerpulse",title="Trigger to Pulse",category=NodeCategory.CONVERTER,kind=NodeKind.VALUE)
public final class TriggerPulseNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("trigger"),OUT=new PropertyId("audio-out");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Pulse").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);
        n.addInput(IN,"Trigger").dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggerpulse.tooltip"));
    }
    public NativeProcessor processor(){return new NativeProcessor("triggerpulse","nodes/TriggerPulseNode.hpp",ParameterContract.builder().source(IN).build());}
}
