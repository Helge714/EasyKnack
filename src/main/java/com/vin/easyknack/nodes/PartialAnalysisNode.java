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
        n.addEditor(new PropertyId("maximum-partials"),"Maximum partials",Editor.Slider.button(64,1.,32768.).integers()).inspectorOnly();
        n.addEditor(new PropertyId("minimum"),"Minimum frequency",Editor.Slider.button(20,0.,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(new PropertyId("maximum"),"Maximum frequency",Editor.Slider.button(20000,0.,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(new PropertyId("tolerance"),"Tracking tolerance",Editor.Slider.button(100,0.,2400.).unit(UnitFormat.CENT)).inspectorOnly();
        n.addEditor(new PropertyId("sinusoidality"),"Minimum sinusoidality",Editor.Slider.bar(.8,0,1)).inspectorOnly();
        n.addEditor(new PropertyId("prominence"),"Peak prominence",Editor.Slider.button(3,0.,120.).unit(UnitFormat.DB)).inspectorOnly();
        n.addEditor(new PropertyId("assessment"),"Assessment time",Editor.Slider.button(30,0.,2000.).formats(UnitFormats.TIME_MS)).inspectorOnly();
        n.addEditor(new PropertyId("confidence"),"Minimum tonality",Editor.Slider.bar(.7,0,1)).inspectorOnly();
        n.addEditor(new PropertyId("duration"),"Minimum track duration",Editor.Slider.button(20,0.,2000.).formats(UnitFormats.TIME_MS)).inspectorOnly();
        n.addEditor(new PropertyId("gap"),"Tracking gap",Editor.Slider.button(20,0.,2000.).formats(UnitFormats.TIME_MS)).inspectorOnly();
        n.addEditor(new PropertyId("harmonic"),"Harmonic only",Editor.checkBox(false)).inspectorOnly();
        n.addInput(new PropertyId("fundamental"),"Fundamental",Editor.Slider.button(0,0.,20000.).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).visible(s.flag("harmonic",false)).tooltip(com.vin.easyknack.ui.AppText.text("partials.fundamental.help"));
        n.addEditor(new PropertyId("harmonic-tolerance"),"Harmonic tolerance",Editor.Slider.button(30,0.,200.).unit(UnitFormat.CENT)).inspectorOnly().visible(s.flag("harmonic",false));
        n.addEditor(new PropertyId("minimum-f0"),"Minimum fundamental",Editor.Slider.button(40,1.,20000.).formats(UnitFormats.FREQUENCY)).inspectorOnly().visible(s.flag("harmonic",false));
        n.addEditor(new PropertyId("maximum-f0"),"Maximum fundamental",Editor.Slider.button(2000,1.,20000.).formats(UnitFormats.FREQUENCY)).inspectorOnly().visible(s.flag("harmonic",false));
    }
    // Classification timing is configured independently of FFT window duration.
    public NativeProcessor processor(){return new NativeProcessor("partialanalysis","nodes/PartialAnalysisNode.hpp",ParameterContract.builder().source(IN).value(new PropertyId("threshold")).value(new PropertyId("maximum-partials")).value(new PropertyId("minimum")).value(new PropertyId("maximum")).value(new PropertyId("tolerance")).value(new PropertyId("sinusoidality")).value(new PropertyId("prominence")).value(new PropertyId("assessment")).value(new PropertyId("confidence")).value(new PropertyId("duration")).value(new PropertyId("gap")).value(new PropertyId("harmonic")).source(new PropertyId("fundamental")).value(new PropertyId("fundamental")).value(new PropertyId("harmonic-tolerance")).value(new PropertyId("minimum-f0")).value(new PropertyId("maximum-f0")).build());}
}
