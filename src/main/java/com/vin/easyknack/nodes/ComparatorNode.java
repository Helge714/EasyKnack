package com.vin.easyknack.nodes;

import java.util.List;

import com.vin.easyknack.model.ComparatorOperation;

@RegisterNode(id = "easyknack.audio.comparator", title = "Comparator", category = NodeCategory.MATH, kind = NodeKind.VALUE)
public final class ComparatorNode extends BasicNode {
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public boolean supportsSpectralBins() { return true; }

	public static final PropertyId A = new PropertyId("a");
	public static final PropertyId B = new PropertyId("b");
	public static final PropertyId OPERATION = new PropertyId("operation");
	public static final PropertyId OUT = new PropertyId("value-out");
	public static final PropertyId RATE = new PropertyId("rate");

	@Override
	public void define(NodeBuilder node, NodeState state) {
		var signal = SignalCategory.VALUE;
		node.addOutput(OUT, "Out").rates(PortRates.AUDIO_CONTROL).signal(signal);
		node.addEditor(OPERATION, "Operation", Editor.comboBox(List.of(ComparatorOperation.values()),
				ComparatorOperation.LESS, ComparatorOperation::id)).keepVisibleOnHide();
		ProcessingRate.addEditor(node,RATE);
		node.addInput(A, "A", Editor.Slider.button(0.0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
		node.addInput(B, "B", Editor.Slider.button(0.0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(signal);
	}

	@Override
	public NativeProcessor processor() {

		return new NativeProcessor("comparator", "nodes/ComparatorNode.hpp",

				ParameterContract.builder().source(A).value(A).source(B).value(B).value(OPERATION).build());
	}

	@Override
	public void mute(MuteRouting routing, NodeState state) {
		routing.connect(A, OUT);
	}

	@Override
	public void insert(InsertionRouting routing, NodeState state) {
		routing.connect(A, OUT);
	}
}