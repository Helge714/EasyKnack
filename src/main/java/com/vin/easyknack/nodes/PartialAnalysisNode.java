package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.partials.analysis",title="Partial Analysis",category=NodeCategory.SPECTRAL,kind=NodeKind.PARTIALS)
public final class PartialAnalysisNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("partials"),REST=new PropertyId("residual");
    @Override public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Partials").dataType(PortDataType.PARTIALS).signal(SignalCategory.PARTIALS).rate(ProcessingRate.CONTROL);
        n.addOutput(REST,"Residual").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(new PropertyId("resolution"),"Resolution",Editor.Slider.button(0,0.,20000.).formats(UnitFormats.FREQUENCY)).tooltip(com.vin.easyknack.i18n.AppText.text("partials.loris.resolution.help"));
        n.addEditor(new PropertyId("threshold"),"Threshold",Editor.Slider.button(-60,-160.,0.).unit(UnitFormat.DB));
        n.addEditor(new PropertyId("duration"),"Minimum track duration",Editor.Slider.button(20,0.,2000.).formats(UnitFormats.TIME_MS));
        n.addEditor(new PropertyId("maximum-partials"),"Maximum partials",Editor.Slider.button(64,1.,32768.).integers());
        n.addEditor(new PropertyId("minimum"),"Minimum frequency",Editor.Slider.button(20,0.,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(new PropertyId("maximum"),"Maximum frequency",Editor.Slider.button(20000,0.,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(new PropertyId("drift"),"Frequency drift",Editor.Slider.button(0,0.,20000.).formats(UnitFormats.FREQUENCY)).inspectorOnly().tooltip(com.vin.easyknack.i18n.AppText.text("partials.loris.drift.help"));
        n.addEditor(new PropertyId("bandwidth"),"Bandwidth model",Editor.comboBox(java.util.List.of("None","Residue","Convergence"),"Convergence",v->v.toLowerCase(java.util.Locale.ROOT))).inspectorOnly();
        n.addEditor(new PropertyId("convergence"),"Convergence tolerance",Editor.Slider.button(.1,.001,1.)).inspectorOnly().visible(s.text("bandwidth","convergence").equals("convergence"));
        n.addEditor(new PropertyId("region"),"Bandwidth region",Editor.Slider.button(2000,1.,20000.).formats(UnitFormats.FREQUENCY)).inspectorOnly().visible(s.text("bandwidth","convergence").equals("residue"));
    }
    public NativeProcessor processor(){return new NativeProcessor("partialanalysis","nodes/PartialAnalysisNode.hpp",ParameterContract.builder().source(IN).value(new PropertyId("threshold")).value(new PropertyId("maximum-partials")).value(new PropertyId("minimum")).value(new PropertyId("maximum")).value(new PropertyId("resolution")).value(new PropertyId("drift")).value(new PropertyId("duration")).value(new PropertyId("bandwidth")).value(new PropertyId("convergence")).value(new PropertyId("region")).build());}
}
