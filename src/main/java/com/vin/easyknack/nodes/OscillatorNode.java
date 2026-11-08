package com.vin.easyknack.nodes;

import java.util.function.ToIntFunction;

@RegisterNode(id = "easyknack.audio.oscillator", title = "Oscillator", category=NodeCategory.GENERATOR, kind=NodeKind.AUDIO)
public final class OscillatorNode extends BasicNode {
    @Override public boolean supportsSampleFeedback() { return true; }
    public static final PropertyId FREQUENCY = new PropertyId("frequency");
    public static final PropertyId WAVEFORM = new PropertyId("waveform");
    public static final PropertyId OUT = new PropertyId("audio-out");
    public static final PropertyId PHASE = new PropertyId("phase");
    public static final PropertyId RESET = new PropertyId("reset");

    @Override public void define(NodeBuilder node, NodeState state) {
        node.addOutput(OUT, "Audio Out");
        node.addInput(WAVEFORM, "Waveform", Editor.comboBox(java.util.List.of(AdditiveOscillatorNode.Waveform.values()), AdditiveOscillatorNode.Waveform.SINE, AdditiveOscillatorNode.Waveform::id)).dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
        node.addInput(FREQUENCY, "Frequency", Editor.Slider.button(440, 0.00001, 20000.0).step(1).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        node.addInput(PHASE, "Phase", Editor.Slider.button(0, null, null).formats(UnitFormats.PHASE)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Phase offset; one turn is a complete cycle. Adds to the running oscillator phase, without resetting it."));
        node.addInput(RESET,"Reset",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Reset the running phase before this sample; Phase is then applied once. Unconnected: click to reset at the next block."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("osc", "nodes/OscillatorNode.hpp",
            ParameterContract.builder().source(FREQUENCY).value(FREQUENCY).token("wave").value(WAVEFORM).source(WAVEFORM).source(PHASE).value(PHASE).source(RESET).build()); }
}




