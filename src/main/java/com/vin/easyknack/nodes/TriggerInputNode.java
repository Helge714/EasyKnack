package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.input.trigger",title="Trigger Input",category=NodeCategory.IO,kind=NodeKind.VALUE)
public final class TriggerInputNode extends BasicNode {
    public static final PropertyId OUT=new PropertyId("trigger-out"),TRIGGER=new PropertyId("trigger");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Trigger").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addEditor(TRIGGER,"Trigger",new Editor.Trigger()).tooltip(com.vin.easyknack.ui.AppText.literal("Send one event at the next audio block. Connect this output to several Reset inputs to reset them at the same sample."));
    }
    public NativeProcessor processor(){return new NativeProcessor("triggerinput","nodes/TriggerInputNode.hpp",ParameterContract.builder().token("trigger-v1").build());}
}
