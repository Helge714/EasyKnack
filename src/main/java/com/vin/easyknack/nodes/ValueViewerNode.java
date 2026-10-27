package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id="easyknack.analysis.valueviewer",title="Value Viewer",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class ValueViewerNode extends BasicNode {
    public static final int MAX_INPUTS=8;
    public static final PropertyId COUNT=new PropertyId("inputs"),GRAPH=new PropertyId("graph"),WINDOW=new PropertyId("time-window"),
        RAW=new PropertyId("record-samples"),CHANNEL=new PropertyId("channel");
    public static PropertyId input(int i){return new PropertyId("value-"+i);}
    public static int count(NodeState s){return Math.max(1,Math.min(MAX_INPUTS,(int)s.number(COUNT,1)));}
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addEditor(GRAPH,"Values",new ValueViewerEditor()).growVertically().keepVisibleOnHide().editorCopyable(false);
        n.addEditor(COUNT,"Inputs",Editor.Slider.button(1,1.,8.).integers()).editorCopyable(false);
        n.addEditor(WINDOW,"Time window",Editor.Slider.button(10,.1,600.).formats(UnitFormats.TIME_SECONDS));
        n.addEditor(RAW,"Record every sample",Editor.checkBox(false)).inspectorOnly()
            .tooltip("Off: retain minimum, maximum and last value per 10 ms, including short peaks. On: retain each sample. History is bounded to one million input records; the visible duration can therefore be shorter than requested. Transport gaps are shown as gaps, never interpolated.");
        n.addEditor(CHANNEL,"Channel",Editor.comboBox(List.of("Left","Right","Mix"),"Left",v->v.toLowerCase(java.util.Locale.ROOT))).inspectorOnly();
        n.addEditor(ValueSeries.SETTINGS,"Values",new ValueSeriesEditor(s,count(s))).inspectorOnly().editorCopyable(false);
        for(var entry:ValueSeries.visible(s,count(s))){
            int i=entry.input();
            n.addInput(input(i),entry.name())
            .rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE)
            .tooltip("Audio is recorded sample by sample before display reduction. Control values are held over their block. Right-click the graph for Freeze, views, enlarged inspection and export.");
        }
    }
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof com.vin.easyknack.ui.ValueViewerPanel panel)panel.configure(state);
        if(control instanceof com.vin.easyknack.ui.ValueSeriesSettingsPanel panel)panel.configure(state);
    }
    private static final ParameterContract CONTRACT=contract();
    private static ParameterContract contract(){
        var b=ParameterContract.builder().integer(COUNT).value(RAW).value(CHANNEL);
        for(int i=0;i<MAX_INPUTS;i++){final int index=i;b.source(input(i),s->index<count(s));}return b.build();
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("valueviewer","nodes/ValueViewerNode.hpp",CONTRACT);}
}
