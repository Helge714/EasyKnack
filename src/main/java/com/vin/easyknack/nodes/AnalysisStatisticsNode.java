package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.statistics",title="Legacy Analysis Statistics",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class AnalysisStatisticsNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;} // Existing documents/source plugins retain their contracts.
    public static final PropertyId IN=new PropertyId("analysis"),OUT=new PropertyId("value"),STATISTIC=new PropertyId("statistic");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Value").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addEditor(STATISTIC,"Statistic",Editor.comboBox(java.util.List.of("Peak","Mean","RMS","Integral","Frequency","Periodicity","Correlation time"),"Peak",v->v.toLowerCase(java.util.Locale.ROOT).replace(" ",""))).tooltip(com.vin.easyknack.ui.AppText.literal("Statistics use the selected view. Frequency is in Hz, periodicity is 0–1, correlation time integrates positive normalized lags up to the first zero crossing (seconds). Results are asynchronous and held until a new snapshot arrives."));
    }
    public NativeProcessor processor(){return new NativeProcessor("analysisstatistics","nodes/AnalysisStatisticsNode.hpp",ParameterContract.builder().source(IN).value(STATISTIC).build());}
}
