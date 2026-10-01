package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.viewer",title="Legacy Analysis Viewer",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class AnalysisViewerNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;} // Existing documents/source plugins retain their contracts.
    public static final PropertyId IN=new PropertyId("analysis"),GRAPH=new PropertyId("graph"),RANGE=new PropertyId("range");
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof AnalysisEditor.Plot plot)plot.configure(state.number(RANGE,1),0,0);
    }
    public void define(NodeBuilder n,NodeState s){
        n.addEditor(GRAPH,"Analysis",new AnalysisEditor(true,true)).keepVisibleOnHide().growVertically();
        n.addInput(IN,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addEditor(RANGE,"Vertical range",Editor.Slider.button(1,.001,null));
    }
    public NativeProcessor processor(){return new NativeProcessor("analysisviewer","nodes/AnalysisViewerNode.hpp",ParameterContract.builder().source(IN).build());}
}
