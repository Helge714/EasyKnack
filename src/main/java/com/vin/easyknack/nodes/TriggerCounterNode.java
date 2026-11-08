package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.modulation.triggercounter",title="Trigger Counter",category=NodeCategory.TRIGGER,kind=NodeKind.VALUE)
public final class TriggerCounterNode extends BasicNode {
    public boolean supportsSampleFeedback(){return true;}
    public boolean supportsTriggerFeedback(){return true;}
    public static final PropertyId INDEX=new PropertyId("index"),OUT=new PropertyId("trigger-out"),IN=new PropertyId("trigger"),RESET=new PropertyId("reset"),WRAP=new PropertyId("wrap"),COUNT=new PropertyId("count");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(INDEX,"Index").rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.text("triggercounter.index.tooltip"));
        n.addOutput(OUT,"Trigger").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(IN,"Trigger",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(RESET,"Reset",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER)
            .tooltip(com.vin.easyknack.i18n.AppText.text("triggercounter.reset.tooltip"));
        n.addEditor(WRAP,"Wrap",Editor.checkBox(false));
        n.addInput(COUNT,"Count",Editor.Slider.button(16,1.0,16777216.0).integers().discrete()).rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.text("triggercounter.count.tooltip"));
    }
    public NativeProcessor processor(){return new NativeProcessor("triggercounter","nodes/TriggerCounterNode.hpp",
        ParameterContract.builder().source(IN).source(RESET).source(COUNT).value(COUNT).value(WRAP).build());}
    public void mute(MuteRouting routing,NodeState state){routing.connect(IN,OUT);}
    public void insert(InsertionRouting routing,NodeState state){routing.connect(IN,OUT);}
}
