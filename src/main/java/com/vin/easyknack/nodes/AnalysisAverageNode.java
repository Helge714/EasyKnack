package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.analysisaverage",title="Analysis Average",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class AnalysisAverageNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("analysis"),OUT=new PropertyId("out");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Mean").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        AverageMeasurement.outputs(n);
        n.addInput(IN,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        AverageMeasurement.settings(n,s);
    }
    public NativeProcessor processor(){return new NativeProcessor("analysisaverage","nodes/AnalysisAverageNode.hpp",AverageMeasurement.contract(IN));}
}
