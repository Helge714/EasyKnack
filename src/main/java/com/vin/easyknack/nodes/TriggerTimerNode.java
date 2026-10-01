package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id="easyknack.modulation.triggertimer",title="Trigger Timer",category=NodeCategory.MODULATION,kind=NodeKind.VALUE)
public final class TriggerTimerNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("trigger"),ELAPSED=new PropertyId("elapsed"),INTERVAL=new PropertyId("interval"),SMOOTHING=new PropertyId("smoothing"),UNIT=new PropertyId("unit");
    public boolean supportsSampleFeedback(){return true;}
    public boolean supportsTriggerFeedback(){return true;}
    public void define(NodeBuilder n,NodeState s){
        var unit=UnitFormat.fromId(s.text(UNIT,"second"));
        n.addOutput(ELAPSED,NodeText.text("triggertimer.elapsed","Elapsed")+" ["+unit.suffix()+"]").rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggertimer.elapsed.tooltip"));
        n.addOutput(INTERVAL,NodeText.text("triggertimer.interval","Interval")+" ["+unit.suffix()+"]").rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggertimer.interval.tooltip"));
        n.addInput(IN,"Trigger",new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
        n.addInput(SMOOTHING,"Smoothing",Editor.Slider.bar(0,0,1)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggertimer.smoothing.tooltip"));
        n.addEditor(UNIT,"Unit",Editor.comboBox(List.of(UnitFormat.SECOND,UnitFormat.MS,UnitFormat.SAMPLE),UnitFormat.SECOND,UnitFormat::id));
    }
    public NativeProcessor processor(){return new NativeProcessor("triggertimer","nodes/TriggerTimerNode.hpp",
        ParameterContract.builder().source(IN).source(SMOOTHING).value(SMOOTHING).value(UNIT).build());}
}
