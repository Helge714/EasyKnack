package com.vin.easyknack.nodes;

import java.util.List;

import com.vin.easyknack.model.NoiseMode;

@RegisterNode(id = "easyknack.audio.noise", title = "Noise", category = NodeCategory.GENERATOR, kind = NodeKind.AUDIO)
public final class NoiseNode extends BasicNode {
    @Override public boolean supportsSampleFeedback() { return true; }

	public static final PropertyId MODE = new PropertyId("mode");
	public static final PropertyId OUT = new PropertyId("audio-out");
	public static final PropertyId RATE = new PropertyId("rate");
	public static final PropertyId SEED = new PropertyId("seed");

	@Override
	public void define(NodeBuilder node, NodeState state) {

		var rate = ProcessingRate.selected(state, RATE, "audio");
		var signal = SignalCategory.AUDIO;

		node.addOutput(OUT, "Out").rate(rate).signal(signal);

        node.addInput(SEED,"Seed",Editor.Slider.button(-1,-1.0,16777215.0).integers().discrete())
            .signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("-1 = automatic seed per logical node. Channel and instance copies share it. Connect Context Channel or Instance Index/Seed for independent noise. Changing the seed restarts the sequence."));

		node.addEditor(MODE, "Noise", Editor.comboBox(List.of(NoiseMode.values()), NoiseMode.WHITE, NoiseMode::id))
				.keepVisibleOnHide();

        ProcessingRate.addEditor(node,RATE,java.util.List.of(ProcessingRate.AUDIO,ProcessingRate.CONTROL),ProcessingRate.AUDIO);
	}

	@Override
	public NativeProcessor processor() {
		return new NativeProcessor("noise", "nodes/NoiseNode.hpp", ParameterContract.builder().value(MODE).source(SEED).integer(SEED).build());
	}
}
