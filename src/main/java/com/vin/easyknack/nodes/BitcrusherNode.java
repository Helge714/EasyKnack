package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id = "easyknack.audio.bitcrusher", title = "Bitcrusher", category=NodeCategory.EFFECT, kind = NodeKind.AUDIO)
public final class BitcrusherNode extends BasicNode {

	public static final PropertyId IN = new PropertyId("audio-in");
	public static final PropertyId BITS = new PropertyId("bits");
	public static final PropertyId DOWNSAMPLE = new PropertyId("downsample");
	public static final PropertyId OUT = new PropertyId("audio-out");
	public static final PropertyId RATE = new PropertyId("rate");

	@Override
	public void define(NodeBuilder node, NodeState state) {

		node.addOutput(OUT, "Out").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.AUDIO);
		ProcessingRate.addEditor(node,RATE);
		node.addInput(IN, "In", Editor.Slider.button(0.0, -1.0, 1.0)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.AUDIO);
		node.addInput(BITS, "Bits", Editor.Slider.button(8.0, 1.0, 24.0).integers().discrete()).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
		node.addInput(DOWNSAMPLE, "Downsample", Editor.Slider.button(1.0, 1.0, 1024.0).integers().discrete()).rates(PortRates.AUDIO_CONTROL)
				.signal(SignalCategory.VALUE);
	}

	@Override
	public NativeProcessor processor() {

		return new NativeProcessor("bitcrusher", "nodes/BitcrusherNode.hpp",
				ParameterContract.builder().source(IN).value(IN).source(BITS).value(BITS).source(DOWNSAMPLE)
						.value(DOWNSAMPLE).build());
	}

	@Override
	public void mute(MuteRouting routing, NodeState state) {
		routing.connect(IN, OUT);
	}

	@Override
	public void insert(InsertionRouting routing, NodeState state) {
		routing.connect(IN, OUT);
	}
}
