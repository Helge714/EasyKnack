package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.pitchdetection",title="Pitch Detection",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class PitchDetectionNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("analysis"),FREQUENCY=new PropertyId("frequency"),CONFIDENCE=new PropertyId("confidence"),MINIMUM=new PropertyId("minimum"),MAXIMUM=new PropertyId("maximum");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(FREQUENCY,"Frequency [Hz]").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Estimated fundamental in Hz; zero if no periodic peak is found. Held between analysis frames."));
        n.addOutput(CONFIDENCE,"Confidence").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Normalized correlation at the detected period, 0–1. Windowing and short windows reduce confidence; octave ambiguity is possible."));
        n.addInput(IN,"Correlation").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addEditor(MINIMUM,"Minimum",Editor.Slider.button(40,1.0,20000.0).formats(UnitFormats.FREQUENCY));
        n.addEditor(MAXIMUM,"Maximum",Editor.Slider.button(2000,1.0,20000.0).formats(UnitFormats.FREQUENCY))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Use a Rectangular FFT window with 2x zero padding and several periods of the lowest expected frequency. Minimum must be below Maximum."));
    }
    public NativeProcessor processor(){return new NativeProcessor("pitchdetection","nodes/PitchDetectionNode.hpp",ParameterContract.builder().source(IN).value(MINIMUM).value(MAXIMUM).build());}
}
