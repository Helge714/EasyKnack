package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.fft",title="Legacy Shared Analysis",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class FFTAnalysisNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;} // Existing documents/source plugins retain their contracts.
    public static final PropertyId IN=new PropertyId("history"),OUT=new PropertyId("analysis"),MINIMUM=new PropertyId("minimum"),MAXIMUM=new PropertyId("maximum");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Buffer").dataType(PortDataType.HISTORY).signal(SignalCategory.HISTORY).rate(ProcessingRate.CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Shared 8192-sample snapshot. Updated every 4096 samples on a background worker; results arrive asynchronously."));
        n.addEditor(MINIMUM,"Minimum pitch",Editor.Slider.button(40,20.0,1000.0).formats(UnitFormats.FREQUENCY));
        n.addEditor(MAXIMUM,"Maximum pitch",Editor.Slider.button(2000,1001.0,20000.0).formats(UnitFormats.FREQUENCY));
    }
    public NativeProcessor processor(){return new NativeProcessor("fftanalysis","nodes/FFTAnalysisNode.hpp",ParameterContract.builder().source(IN).value(MINIMUM).value(MAXIMUM).build());}
}
