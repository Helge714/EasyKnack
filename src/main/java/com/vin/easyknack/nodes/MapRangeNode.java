package com.vin.easyknack.nodes;

import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id = "easyknack.audio.maprange", title = "Map Range", category=NodeCategory.MATH, kind=NodeKind.VALUE)
public final class MapRangeNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback() { return true; }
    public static final PropertyId IN = new PropertyId("audio-in");
    public static final PropertyId FROM_MIN = new PropertyId("from-min");
    public static final PropertyId FROM_MAX = new PropertyId("from-max");
    public static final PropertyId TO_MIN = new PropertyId("to-min");
    public static final PropertyId TO_MAX = new PropertyId("to-max");
    public static final PropertyId CLAMP = new PropertyId("clamp");
    public static final PropertyId OUT = new PropertyId("audio-out");
    public static final PropertyId RATE = new PropertyId("rate");

    @Override public void define(NodeBuilder node, NodeState state) {
        var signal = SignalCategory.VALUE;
        node.addOutput(OUT, "Out").rates(PortRates.AUDIO_CONTROL).signal(signal);
        ProcessingRate.addEditor(node,RATE);
        node.addEditor(CLAMP, "Clamp ±1", Editor.checkBox(false));
        node.addInput(IN, "Value", Editor.Slider.button(0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
        node.addInput(FROM_MIN, "From Min", Editor.Slider.button(0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
        node.addInput(FROM_MAX, "From Max", Editor.Slider.button(1, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
        node.addInput(TO_MIN, "To Min", Editor.Slider.button(0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
        node.addInput(TO_MAX, "To Max", Editor.Slider.button(1, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("maprange", "nodes/MapRangeNode.hpp",
            ParameterContract.builder().source(IN).value(IN).source(FROM_MIN).value(FROM_MIN).source(FROM_MAX).value(FROM_MAX).source(TO_MIN).value(TO_MIN).source(TO_MAX).value(TO_MAX).value(CLAMP).build()); }
    @Override public void mute(MuteRouting routing, NodeState state) { routing.connect(IN, OUT); }
    @Override public void insert(InsertionRouting routing, NodeState state) { routing.connect(IN, OUT); }
}

