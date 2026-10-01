package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.modulation.triggermerge",title="Trigger Merge",category=NodeCategory.MODULATION,kind=NodeKind.VALUE)
public final class TriggerMergeNode extends BasicNode {
    public static final int MAX_INPUTS=16;
    public static final PropertyId OUT=new PropertyId("trigger-out"),COUNT=new PropertyId("inputs");
    public static PropertyId input(int i){return new PropertyId("input-"+i);}
    public static int count(NodeState s){return Math.max(2,Math.min(MAX_INPUTS,(int)s.number(COUNT,2)));}
    public boolean supportsSampleFeedback(){return true;}
    public boolean supportsTriggerFeedback(){return true;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Trigger").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER)
            .tooltip(com.vin.easyknack.ui.AppText.text("triggermerge.tooltip"));
        n.addEditor(COUNT,"Inputs",Editor.Slider.button(2,2.,(double)MAX_INPUTS).integers()).keepVisibleOnHide();
        for(int i=0;i<count(s);i++)n.addInput(input(i),NodeText.text("triggermerge.input","Input")+" "+(i+1),new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
    }
    private static final ParameterContract CONTRACT=contract();
    private static ParameterContract contract(){var b=ParameterContract.builder().integer(COUNT);for(int i=0;i<MAX_INPUTS;i++){final int j=i;b.source(input(i),s->j<count(s));}return b.build();}
    public NativeProcessor processor(){return new NativeProcessor("triggermerge","nodes/TriggerMergeNode.hpp",CONTRACT);}
    public void mute(MuteRouting r,NodeState s){r.connect(input(0),OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(input(0),OUT);}
}
