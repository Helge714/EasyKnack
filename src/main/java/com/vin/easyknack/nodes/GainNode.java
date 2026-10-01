package com.vin.easyknack.nodes;

import java.util.function.ToIntFunction;

@RegisterNode(id = "easyknack.audio.gain", title = "Gain", category=NodeCategory.ROUTING, kind=NodeKind.AUDIO)
public final class GainNode extends BasicNode {
    @Override public boolean supportsSampleFeedback(){return true;}

    public static final PropertyId GAIN_DB = new PropertyId("gainDb");
    public static final PropertyId IN = new PropertyId("audio-in");
    public static final PropertyId OUT = new PropertyId("audio-out");

    @Override public void define(NodeBuilder node, NodeState state) {
        node.addOutput(OUT, "Audio Out");
        node.addInput(GAIN_DB, "Gain", Editor.Slider.button(-12, -160.0, 6.0).step(.5).formats(UnitFormats.GAIN_DB)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        node.addInput(IN, "Audio In");
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("gain", "nodes/GainNode.hpp",
            ParameterContract.builder().source(IN).value(GAIN_DB).source(GAIN_DB).build()); }
    @Override public void mute(MuteRouting routing, NodeState state) { routing.connect(IN, OUT); }
    @Override public void insert(InsertionRouting routing, NodeState state) { routing.connect(IN, OUT); }
}


