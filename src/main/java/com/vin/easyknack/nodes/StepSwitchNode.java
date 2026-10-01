package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;

@RegisterNode(id="easyknack.routing.stepswitch",title="Step Switch",category=NodeCategory.ROUTING,kind=NodeKind.VALUE)
public final class StepSwitchNode extends BasicNode {
    public static final int MAX_STEPS=32;
    public static final PropertyId STEPS=new PropertyId("steps"),NEXT=new PropertyId("next"),RESET=new PropertyId("reset"),
        OUT=new PropertyId("value"),INDEX=new PropertyId("index"),RATE=new PropertyId("rate");
    public static PropertyId input(int index){return new PropertyId("step-"+index);}
    public static int count(NodeState s){return Math.max(1,Math.min(MAX_STEPS,(int)s.number(STEPS,4)));}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Value").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addOutput(INDEX,"Index").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE).tooltip("Active step index, starting at 0. Follows the output rate.");
        n.addEditor(STEPS,"Steps",Editor.Slider.button(4,1.,(double)MAX_STEPS).integers()).keepVisibleOnHide()
            .tooltip("Number of inputs, 1–32. Removing the active step returns to the first input. Undo restores removed connections.");
        n.addInput(NEXT,"Next",new Editor.Trigger()).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT).signal(SignalCategory.TRIGGER)
            .tooltip("Each trigger advances one step and wraps at the end. Unconnected: click to advance at the next block.");
        n.addInput(RESET,"Reset",new Editor.Trigger()).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT).signal(SignalCategory.TRIGGER)
            .tooltip("Select the first input. Reset wins over Next at the same sample.");
        for(int i=0;i<count(s);i++)n.addInput(input(i),NodeText.text("easyknack.routing.stepswitch.input","Input")+" "+(i+1),Editor.Slider.button(0))
            .rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        ProcessingRate.addEditor(n,RATE).tooltip("Auto uses AudioRate when a trigger or audio input is connected, otherwise ControlRate. ControlRate samples at block start and defers later triggers to the next block. Audio switching is immediate and can click.");
    }
    @Override public Map<String,GLNodeValue> defaults(){
        var values=new HashMap<>(super.defaults());for(int i=0;i<MAX_STEPS;i++)values.putIfAbsent(input(i).value(),GLNodeValue.of(0.));return Map.copyOf(values);
    }
    private static final ParameterContract CONTRACT=contract();
    private static ParameterContract contract(){
        var b=ParameterContract.builder().integer(STEPS).source(NEXT).source(RESET);
        for(int i=0;i<MAX_STEPS;i++){final int index=i;b.source(input(i),s->index<count(s)).value(input(i));}return b.build();
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("stepswitch","nodes/StepSwitchNode.hpp",CONTRACT);}
    @Override public ProcessingRate resolveAutomaticRate(NodeState s,Map<PropertyId,ProcessingRate> incoming){
        return incoming.containsKey(NEXT)||incoming.containsKey(RESET)?ProcessingRate.AUDIO:super.resolveAutomaticRate(s,incoming);
    }
    @Override public com.vin.easyknack.ui.BasicNodeView createView(com.vin.easyknack.ui.NodeViewContext context,com.vingame.glswing.node.model.GLGraphNode model){
        return new com.vin.easyknack.ui.StepSwitchView(this,context,model);
    }
    @Override public void mute(MuteRouting r,NodeState s){r.connect(input(0),OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(input(0),OUT);}
}
