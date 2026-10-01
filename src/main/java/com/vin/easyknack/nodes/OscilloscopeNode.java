package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.oscilloscope",title="Oscilloscope",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class OscilloscopeNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"),GRAPH=new PropertyId("graph"),
        LOW=new PropertyId("low"),HIGH=new PropertyId("high"),WINDOW=new PropertyId("window");
    public static final PropertyId CHANNEL=new PropertyId("channel");
    public static final PropertyId VERTICAL_RANGE=new PropertyId("vertical-range");
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof AnalysisEditor.Plot plot)plot.configure(state.number(VERTICAL_RANGE,1),state.number("low",-.1),state.number("high",.1));
    }
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    public void define(NodeBuilder n,NodeState s){
        n.addEditor(GRAPH,"Oscilloscope",new AnalysisEditor(false)).keepVisibleOnHide().growVertically();
        n.addInput(IN,"Audio In").tooltip(com.vin.easyknack.ui.AppText.literal("All polyphonic audio voices are summed before analysis."));
        n.addEditor(CHANNEL,"Channel",Editor.comboBox(java.util.List.of("Left","Right","Mix"),"Left",v->v.toLowerCase(java.util.Locale.ROOT))).tooltip(com.vin.easyknack.ui.AppText.literal("Mix displays (Left + Right) / 2. Mono keeps its level; opposite-phase channels cancel."));
        n.addEditor(VERTICAL_RANGE,"Vertical range",Editor.Slider.button(1,.001,null)).tooltip(com.vin.easyknack.ui.AppText.literal("Visible vertical range: minus this value to plus this value. Changes only the display scale, not the signal or analysis."));
        n.addEditor(LOW,"Trigger low",Editor.Slider.button(-.1,-1.0,1.0)).tooltip(com.vin.easyknack.ui.AppText.literal("Arms below this threshold; captures when the signal then crosses the high threshold. Auto-captures when no crossing occurs."));
        n.addEditor(HIGH,"Trigger high",Editor.Slider.button(.1,-1.0,1.0));
        n.addEditor(WINDOW,"Window",Editor.Slider.button(20,6.0,200.0).formats(UnitFormats.TIME_MS));
    }
    public NativeProcessor processor(){return new NativeProcessor("scope","nodes/OscilloscopeNode.hpp",ParameterContract.builder().source(IN).value(LOW).value(HIGH).value(WINDOW).value(CHANNEL).build());}
}
