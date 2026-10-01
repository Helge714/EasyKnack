package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.modulation.triggerdelay",title="Trigger Delay",category=NodeCategory.MODULATION,kind=NodeKind.VALUE)
public final class TriggerDelayNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("trigger"),OUT=new PropertyId("trigger-out"),TIME=new PropertyId("time"),RESET=new PropertyId("reset");
    public boolean supportsSampleFeedback(){return true;}
    public boolean supportsTriggerFeedback(){return true;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Trigger").previousStep().rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(IN,"Trigger",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(TIME,"Time",Editor.Slider.button(.25,1.0/com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE,10.0).formats(UnitFormats.TIME_SECONDS)).rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggerdelay.time.tooltip"));
        n.addInput(RESET,"Reset",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggerdelay.reset.tooltip"));
    }
    public NativeProcessor processor(){return new NativeProcessor("triggerdelay","nodes/TriggerDelayNode.hpp",
        ParameterContract.builder().source(IN).source(TIME).value(TIME).source(RESET).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
