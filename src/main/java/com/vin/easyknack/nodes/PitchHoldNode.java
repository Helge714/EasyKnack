package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.pitchhold",title="Pitch Hold",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS,experimental=true)
public final class PitchHoldNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("frequency"),CONFIDENCE=new PropertyId("confidence"),THRESHOLD=new PropertyId("threshold"),INITIAL=new PropertyId("initial"),OUT=new PropertyId("out"),VALID=new PropertyId("valid"),RATE=new PropertyId("rate");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Frequency [Hz]").formats(UnitFormats.CLOCK).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addOutput(VALID,"Valid").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).tooltip(com.vin.easyknack.i18n.AppText.literal("1 while the current frequency is positive and finite and confidence meets the threshold; otherwise 0. This is a numeric gate, not a Trigger."));
        ProcessingRate.addEditor(n,RATE);
        n.addInput(IN,"Frequency",Editor.Slider.button(440,0.0,20000.0).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(CONFIDENCE,"Confidence",Editor.Slider.bar(1,0,1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(THRESHOLD,"Minimum confidence",Editor.Slider.bar(.8,0,1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Hold the last accepted frequency while confidence is below this threshold, or frequency is zero/non-finite. No automatic fade or smoothing."));
        n.addEditor(INITIAL,"Initial frequency",Editor.Slider.button(440,1.0,20000.0).formats(UnitFormats.FREQUENCY)).inspectorOnly().tooltip(com.vin.easyknack.i18n.AppText.literal("Fallback until the first valid frequency. After that, only accepted measurements update the held value."));
    }
    public NativeProcessor processor(){return new NativeProcessor("pitchhold","nodes/PitchHoldNode.hpp",ParameterContract.builder().source(IN).value(IN).source(CONFIDENCE).value(CONFIDENCE).source(THRESHOLD).value(THRESHOLD).value(INITIAL).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
