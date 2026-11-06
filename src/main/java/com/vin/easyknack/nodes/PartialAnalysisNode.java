package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.partials.analysis",title="Partial Analysis",category=NodeCategory.SPECTRAL,kind=NodeKind.PARTIALS)
public final class PartialAnalysisNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("partials"),REST=new PropertyId("residual");
    @Override public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Partials").dataType(PortDataType.PARTIALS).signal(SignalCategory.PARTIALS).rate(ProcessingRate.CONTROL);
        n.addOutput(REST,"Residual").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(new PropertyId("threshold"),"Threshold",Editor.Slider.button(-60,-160.,0.).unit(UnitFormat.DB)).inspectorOnly();
        n.addEditor(new PropertyId("maximum-partials"),"Maximum partials",Editor.Slider.button(64,1.,256.).integers()).inspectorOnly();
        n.addEditor(new PropertyId("minimum"),"Minimum frequency",Editor.Slider.button(20,0.,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(new PropertyId("maximum"),"Maximum frequency",Editor.Slider.button(20000,0.,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(new PropertyId("tolerance"),"Tracking tolerance",Editor.Slider.button(100,0.,2400.).unit(UnitFormat.CENT)).inspectorOnly();
    }
    public NativeProcessor processor(){return new NativeProcessor("partialanalysis","nodes/PartialAnalysisNode.hpp",ParameterContract.builder().source(IN).value(new PropertyId("threshold")).value(new PropertyId("maximum-partials")).value(new PropertyId("minimum")).value(new PropertyId("maximum")).value(new PropertyId("tolerance")).build());}
}
