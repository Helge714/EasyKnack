package com.vin.easyknack.nodes;

/** Descriptors of an existing autocorrelation; no FFT or additional audio history. */
@RegisterNode(id="easyknack.analysis.correlationfeatures",title="Correlation Features",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class CorrelationFeaturesNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("analysis"),MINIMUM=new PropertyId("minimum"),MAXIMUM=new PropertyId("maximum"),
        THRESHOLD=new PropertyId("threshold"),SILENCE=new PropertyId("silence");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("periodicity"),"Periodicity").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip("Normalized positive autocorrelation peak, 0–1. Measures repetition strength, not a probability or perceived pitch.");
        n.addOutput(new PropertyId("period"),"Period").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).formats(UnitFormats.TIME_SECONDS);
        n.addOutput(new PropertyId("frequency"),"Frequency").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).formats(UnitFormats.FREQUENCY);
        n.addOutput(new PropertyId("hnr"),"HNR").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).formats(UnitFormats.fixed(UnitFormat.DB))
            .tooltip("Estimated harmonic-to-noise ratio: 10 log10(p / (1 − p)), limited to −120…120 dB. Depends on upstream correlation/window normalization; not an independent measurement.");
        n.addOutput(new PropertyId("valid"),"Valid").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip("1 when a positive local peak passes the periodicity and silence thresholds in the search range. Otherwise Period and Frequency are zero.");
        n.addInput(IN,"Autocorrelation").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addEditor(MINIMUM,"Minimum frequency",Editor.Slider.button(20,.001,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(MAXIMUM,"Maximum frequency",Editor.Slider.button(20000,.001,null).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addEditor(THRESHOLD,"Periodicity threshold",Editor.Slider.bar(.3,0,1).formats(UnitFormats.PROPORTION)).inspectorOnly();
        n.addEditor(SILENCE,"Silence threshold",Editor.Slider.button(.000001,0.,null).formats(UnitFormats.PROPORTION)).inspectorOnly()
            .tooltip("Minimum window-weighted source RMS in linear amplitude. Correlation requires at least three periods in its window. Outputs hold between frames; unsupported resources and silence clear them.");
    }
    public NativeProcessor processor(){return new NativeProcessor("correlationfeatures","nodes/CorrelationFeaturesNode.hpp",
        ParameterContract.builder().source(IN).value(MINIMUM).value(MAXIMUM).value(THRESHOLD).value(SILENCE).build());}
}
