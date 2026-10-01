package com.vin.easyknack.nodes;

import java.util.List;

import com.vin.easyknack.model.EdgeMode;

@RegisterNode(id = "easyknack.audio.edgedetector", title = "Trigger Converter", category = NodeCategory.CONVERTER, kind = NodeKind.VALUE)
public final class EdgeDetectorNode extends BasicNode {

	public static final PropertyId IN = new PropertyId("in");
	public static final PropertyId THRESHOLD = new PropertyId("threshold");
	public static final PropertyId MODE = new PropertyId("mode");
	public static final PropertyId OUT = new PropertyId("out");
	public static final PropertyId RATE = new PropertyId("rate");

	@Override
	public void define(NodeBuilder node, NodeState state) {

		node.addOutput(OUT, "Trigger").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER)
                .tooltip(com.vin.easyknack.ui.AppText.literal("One trigger for each selected threshold crossing. Audio is checked per sample, Control at its block updates. The first value initializes the detector without a trigger."));
		node.addEditor(MODE, "Edge", Editor.comboBox(List.of(EdgeMode.values()), EdgeMode.RISING, EdgeMode::id))
				.keepVisibleOnHide();
		ProcessingRate.addEditor(node,RATE);
		node.addInput(IN, "In", Editor.Slider.button(0.0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL)
				.signal(SignalCategory.VALUE).tooltip(com.vin.easyknack.ui.AppText.literal("A crossing changes between value > threshold and value <= threshold. Choose Rising, Falling or Both. Auto follows the input rate."));
		node.addInput(THRESHOLD, "Threshold", Editor.Slider.button(0.5, -1000000.0, 1000000.0))
				.rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE).tooltip(com.vin.easyknack.ui.AppText.literal("A crossing changes between value > threshold and value <= threshold. Choose Rising, Falling or Both. Auto follows the input rate."));
	}

	@Override
	public NativeProcessor processor() {
		return new NativeProcessor("edgedetector", "nodes/EdgeDetectorNode.hpp", ParameterContract.builder().source(IN)
				.value(IN).source(THRESHOLD).value(THRESHOLD).value(MODE).build());
	}

	@Override
	public void mute(MuteRouting routing, NodeState state) {
		
	}

	@Override
	public void insert(InsertionRouting routing, NodeState state) {
		
	}
}