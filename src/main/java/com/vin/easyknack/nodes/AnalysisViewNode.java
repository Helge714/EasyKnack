package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.view",title="Legacy Analysis View",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class AnalysisViewNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;} // Existing documents/source plugins retain their contracts.
    public static final PropertyId IN=new PropertyId("analysis-in"),OUT=new PropertyId("analysis-out"),VIEW=new PropertyId("view");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addEditor(VIEW,"View",Editor.comboBox(java.util.List.of("Magnitude","Phase","Power","Correlation"),"Magnitude",v->v.toLowerCase(java.util.Locale.ROOT)));
    }
    public NativeProcessor processor(){return new NativeProcessor("analysisview","nodes/AnalysisViewNode.hpp",ParameterContract.builder().source(IN).value(VIEW).build());}
}
