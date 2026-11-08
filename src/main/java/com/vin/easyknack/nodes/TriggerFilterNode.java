package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.modulation.triggerfilter",title="Trigger Filter",category=NodeCategory.TRIGGER,kind=NodeKind.TRIGGER)
public final class TriggerFilterNode extends BasicNode {
    public boolean supportsSampleFeedback(){return true;}
    public boolean supportsTriggerFeedback(){return true;}
    public static final PropertyId OUT=new PropertyId("trigger-out"),IN=new PropertyId("trigger"),CONDITION=new PropertyId("condition");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Trigger").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(IN,"Trigger",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(CONDITION,"Condition",Editor.Slider.bar(1,0,1)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.text("triggerfilter.condition.tooltip"));
    }
    public NativeProcessor processor(){return new NativeProcessor("triggerfilter","nodes/TriggerFilterNode.hpp",
        ParameterContract.builder().source(IN).source(CONDITION).value(CONDITION).build());}
    public void mute(MuteRouting routing,NodeState state){routing.connect(IN,OUT);}
    public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,OUT);}
}
