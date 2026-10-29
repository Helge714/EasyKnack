package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id = "easyknack.audio.additive", title="Additive Oscillator", category=NodeCategory.GENERATOR, kind=NodeKind.AUDIO)
public final class AdditiveOscillatorNode extends BasicNode {
    private static final String SPREAD_MODEL="easyknack.spread.model";
    @Override public NodeState normalizeState(NodeState state){
        if(state.text(SPREAD_MODEL,"").equals("power"))return state;
        var values=new java.util.HashMap<>(state.values());
        if(values.containsKey("spread"))values.put("spread",com.vingame.glswing.node.model.GLNodeValue.of(Math.max(0,Math.min(4,1+state.number("spread",0)/1200.))));
        values.remove("spread.unit");values.put(SPREAD_MODEL,com.vingame.glswing.node.model.GLNodeValue.of("power"));
        return new NodeState(values,state.connectedInputs());
    }
    @Override public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){
        var values=new java.util.HashMap<>(super.defaults());values.put(SPREAD_MODEL,com.vingame.glswing.node.model.GLNodeValue.of("power"));return java.util.Map.copyOf(values);
    }
    @Override public String encode(NodeState state,java.util.function.ToIntFunction<PropertyId> input){return super.encode(normalizeState(state),input);}

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
        node.addInput(SPREAD, "Stretch", Editor.Slider.button(1, 0d, 4d)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).visible(multiple);
        node.addInput(FREQUENCY, "Frequency", Editor.Slider.button(440, 0.00001, 20000.0).step(1).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        node.addInput(PHASE, "Phase", Editor.Slider.button(0, 0.0, 360.0).step(1).formats(UnitFormats.PHASE)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        node.addInput(RESET,"Reset",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Reset all partials before this sample; Phase is then applied once. Unconnected: click to reset at the next block."));
    }
    @Override public NativeProcessor processor() {
        return new NativeProcessor("additive", "nodes/AdditiveOscillatorNode.hpp",
                ParameterContract.builder().source(WAVEFORM).value(WAVEFORM).integer(HARMONICS)
                        .value(FALLOFF).token("power").value(SPREAD).source(FREQUENCY).value(FREQUENCY)
                        .source(PHASE).value(PHASE).token("inputs")
                        .source(HARMONICS).source(FALLOFF).source(SPREAD).source(RESET).build());
    }
}





