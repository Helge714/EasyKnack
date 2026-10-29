package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.modulation.triggerdivider",title="Trigger Divider",category=NodeCategory.TRIGGER,kind=NodeKind.TRIGGER)
public final class TriggerDividerNode extends BasicNode {
    public boolean supportsSampleFeedback(){return true;}
    public boolean supportsTriggerFeedback(){return true;}
    public static final PropertyId IN=new PropertyId("trigger"),RESET=new PropertyId("reset"),DIVISOR=new PropertyId("divisor"),OUT=new PropertyId("trigger-out");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Trigger").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(IN,"Trigger",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggerdivider.trigger.tooltip"));
        n.addInput(DIVISOR,"Division",Editor.Slider.button(2,1.0,1024.0).integers().discrete()).rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE);
        n.addInput(RESET,"Reset",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggerdivider.reset.tooltip"));
    }
    public NativeProcessor processor(){return new NativeProcessor("triggerdivider","nodes/TriggerDividerNode.hpp",
        ParameterContract.builder().source(IN).source(DIVISOR).value(DIVISOR).source(RESET).build());}
    public void mute(MuteRouting routing,NodeState state){routing.connect(IN,OUT);}
    public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,OUT);}
}
