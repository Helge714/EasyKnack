package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.spectrumviewer",title="Analysis Viewer",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class SpectrumViewerNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("analysis"),VIEW=new PropertyId("view"),GRAPH=new PropertyId("graph"),RANGE=new PropertyId("range");
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof AnalysisEditor.Plot plot)plot.configure(state.number(RANGE,1),0,0);
    }
    public void define(NodeBuilder n,NodeState s){
        n.addEditor(GRAPH,"Analysis",new AnalysisEditor(true,true)).keepVisibleOnHide().growVertically();
        n.addInput(IN,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addEditor(VIEW,"Data",Editor.comboBox(java.util.List.of("Auto","Magnitude","Phase","Power","Correlation"),"Auto",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Auto follows the incoming analysis: spectrum or autocorrelation. Unsupported selections fall back to the source's default view."));
        n.addEditor(RANGE,"Vertical range",Editor.Slider.button(s.text(VIEW,"magnitude").equals("phase")?Math.PI:1,.001,null));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumviewer","nodes/SpectrumViewerNode.hpp",ParameterContract.builder().source(IN).value(VIEW).build());}
}
