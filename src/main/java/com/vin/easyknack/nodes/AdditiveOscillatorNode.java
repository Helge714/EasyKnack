package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id = "easyknack.audio.additive", title = "Complex Oscillator", category=NodeCategory.GENERATOR, kind=NodeKind.AUDIO)
public final class AdditiveOscillatorNode extends BasicNode {
    @Override public boolean supportsSampleFeedback() { return true; }
    public enum Waveform {
        SINE("Sine"), TRIANGLE("Triangle"), SAW("Sawtooth"), SQUARE("Square");
        private final String label;
        Waveform(String label) { this.label = label; }
        public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
        @Override public String toString() { return NodeText.text("waveform." + id(), label); }
    }
    public static final PropertyId OUT = new PropertyId("audio-out"), WAVEFORM = new PropertyId("waveform"),
            HARMONICS = new PropertyId("harmonics"), FALLOFF = new PropertyId("falloff"), SPREAD = new PropertyId("spread"),
            FREQUENCY = new PropertyId("frequency"), PHASE = new PropertyId("phase"), RESET = new PropertyId("reset");

    @Override public void define(NodeBuilder node, NodeState state) {
        boolean multiple = state.number(HARMONICS, 1) > 1 || state.isConnected(HARMONICS);

        node.addOutput(OUT, "Audio Out");
        node.addInput(WAVEFORM, "Waveform", Editor.comboBox(List.of(Waveform.values()), Waveform.SINE, Waveform::id))
                .dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
        node.addInput(HARMONICS, "Partials", Editor.Slider.bar(1, 1, 144).integers().discrete()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addInput(FALLOFF, "Falloff", Editor.Slider.button(1, 0d, 4d)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).visible(multiple);
        node.addInput(SPREAD, "Spreading", Editor.Slider.button(0, -2400d, 2400d).formats(UnitFormats.PITCH_CENT)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).visible(multiple);
        node.addInput(FREQUENCY, "Frequency", Editor.Slider.button(440, 0.00001, 20000.0).step(1).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        node.addInput(PHASE, "Phase", Editor.Slider.button(0, 0.0, 360.0).step(1).formats(UnitFormats.PHASE)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        node.addInput(RESET,"Reset",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Reset all partials before this sample; Phase is then applied once. Unconnected: click to reset at the next block."));
    }
    @Override public NativeProcessor processor() {
        return new NativeProcessor("additive", "nodes/AdditiveOscillatorNode.hpp",
                ParameterContract.builder().source(WAVEFORM).value(WAVEFORM).integer(HARMONICS)
                        .value(FALLOFF).value(SPREAD).source(FREQUENCY).value(FREQUENCY)
                        .source(PHASE).value(PHASE).token("inputs")
                        .source(HARMONICS).source(FALLOFF).source(SPREAD).source(RESET).build());
    }
}





