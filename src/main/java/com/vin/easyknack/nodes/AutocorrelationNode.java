package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.autocorrelation",title="Legacy Audio Autocorrelation",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class AutocorrelationNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;}
    public static final PropertyId IN=new PropertyId("audio-in"),FREQUENCY=new PropertyId("frequency"),CONFIDENCE=new PropertyId("confidence"),
        GRAPH=new PropertyId("graph"),MINIMUM=new PropertyId("minimum"),MAXIMUM=new PropertyId("maximum");
    public static final PropertyId CHANNEL=new PropertyId("channel");
    public static final PropertyId VERTICAL_RANGE=new PropertyId("vertical-range");
    public static final PropertyId ANALYSIS=new PropertyId("analysis");
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof AnalysisEditor.Plot plot)plot.configure(state.number(VERTICAL_RANGE,1),state.number("low",-.1),state.number("high",.1));
    }
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(FREQUENCY,"Frequency [Hz]").formats(UnitFormats.CLOCK).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Estimated fundamental in Hz; 0 when no periodic signal is detected. Updated every 4096 samples (85 ms at 48 kHz)."));
        n.addOutput(CONFIDENCE,"Confidence").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Normalized correlation at the detected period: 0–1. Harmonic mixtures can produce octave ambiguity."));
        n.addOutput(ANALYSIS,"Analysis").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Shared autocorrelation curve for Analysis Viewer. Existing frequency and confidence outputs keep their positions."));
        n.addEditor(GRAPH,"Correlation / lag",new AnalysisEditor(true)).keepVisibleOnHide().growVertically();
        n.addInput(IN,"Audio In").tooltip(com.vin.easyknack.ui.AppText.literal("All polyphonic audio voices are summed before analysis."));
        n.addEditor(CHANNEL,"Channel",Editor.comboBox(java.util.List.of("Left","Right"),"Left",v->v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(VERTICAL_RANGE,"Vertical range",Editor.Slider.button(1,.001,null)).tooltip(com.vin.easyknack.ui.AppText.literal("Visible vertical range: minus this value to plus this value. Changes only the display scale, not the signal or analysis."));
        n.addEditor(MINIMUM,"Minimum",Editor.Slider.button(40,30.0,1000.0).formats(UnitFormats.FREQUENCY));
        n.addEditor(MAXIMUM,"Maximum",Editor.Slider.button(2000,100.0,10000.0).formats(UnitFormats.FREQUENCY));
    }
    public NativeProcessor processor(){return new NativeProcessor("autocorrelation","nodes/AutocorrelationNode.hpp",ParameterContract.builder().source(IN).value(MINIMUM).value(MAXIMUM).value(CHANNEL).build());}
}
