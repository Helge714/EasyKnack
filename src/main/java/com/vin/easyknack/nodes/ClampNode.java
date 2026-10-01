package com.vin.easyknack.nodes;

import java.util.HashMap;
import java.util.List;

import com.vin.easyknack.model.ClampMode;
import com.vingame.glswing.node.model.GLNodeValue;

@RegisterNode(id = "easyknack.audio.clamp", title = "Clamp", category = NodeCategory.MATH, kind = NodeKind.VALUE)
public final class ClampNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback() { return true; }

	public static final PropertyId IN = new PropertyId("audio-in");
	public static final PropertyId MODE = new PropertyId("mode");
	public static final PropertyId OUT = new PropertyId("audio-out");
	public static final PropertyId RATE = new PropertyId("rate");

	@Override
	public void define(NodeBuilder node, NodeState state) {
		var signal = SignalCategory.VALUE;

		node.addOutput(OUT, "Out").rates(PortRates.AUDIO_CONTROL).signal(signal);
		node.addEditor(MODE, "Mode", Editor.comboBox(List.of(ClampMode.values()), ClampMode.CLIP, ClampMode::id)).keepVisibleOnHide();
		ProcessingRate.addEditor(node,RATE);
		node.addInput(IN, "In", Editor.Slider.button(0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
	}

	@Override
	public java.util.Map<String, GLNodeValue> defaults() {
		return new HashMap<>(super.defaults());
	}

	@Override
	public NativeProcessor processor() {
		return new NativeProcessor("clamp", "nodes/ClampNode.hpp",
				ParameterContract.builder().source(IN).value(IN).value(MODE).build());
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