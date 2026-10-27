package com.vin.easyknack.nodes;

@RegisterNode(id = "easyknack.audio.digitalnoise", title = "Digital Noise", category = NodeCategory.GENERATOR, kind = NodeKind.VALUE)
public final class DigitalNoiseNode extends BasicNode {
    @Override public boolean supportsSampleFeedback() { return true; }

	public static final PropertyId FREQUENCY = new PropertyId("frequency");
	public static final PropertyId STRENGTH = new PropertyId("strength");
	public static final PropertyId SEED = new PropertyId("seed");
	public static final PropertyId FACTOR = new PropertyId("factor");
	public static final PropertyId TRIGGER = new PropertyId("trigger");
	public static final PropertyId FREQUENCY_OUT = new PropertyId("frequency-out");
	public static final PropertyId DISTRIBUTION = new PropertyId("distribution");
	private enum Distribution {
		UNIFORM, GAUSSIAN;
		String id(){return name().toLowerCase(java.util.Locale.ROOT);}
		@Override public String toString(){return NodeText.text("digitalnoise.distribution."+id(),this==UNIFORM?"Uniform":"Gaussian");}
	}

	@Override
	public void define(NodeBuilder node, NodeState state) {

		node.addOutput(FACTOR, "Factor").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO)
				.tooltip(NodeText.text("digitalnoise.factor.help","Random factor held for one self-modulated cycle. Time-weighted deviations are symmetric in octaves. Strength 1 spans 0.5x to 2x; this does not fix the arithmetic mean frequency or linear spectral centroid."));
		node.addOutput(TRIGGER, "Trigger").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER)
				.tooltip(com.vin.easyknack.ui.AppText.literal("One-sample pulse whenever a new random factor is drawn."));
		node.addOutput(FREQUENCY_OUT, "Frequency").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO)
				.tooltip(com.vin.easyknack.ui.AppText.literal("Base frequency multiplied by this sample's Factor, in Hz. Connect directly to an oscillator's Frequency input."));
		node.addInput(FREQUENCY, "Frequency",
				Editor.Slider.button(440, 0.00001, 20000.0).step(1).formats(UnitFormats.FREQUENCY))
				.signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
				.tooltip(com.vin.easyknack.ui.AppText.literal("Base frequency. The currently held random factor determines ")
						+ com.vin.easyknack.ui.AppText.literal("the duration of the current cycle."));
		node.addInput(STRENGTH, "Strength", Editor.Slider.button(0, 0.0, 1.0)).signal(SignalCategory.VALUE)
				.rates(PortRates.AUDIO_CONTROL)
				.tooltip(NodeText.text("digitalnoise.strength.help","Maximum deviation in octaves: 0 = factor 1; 1 = 0.5x to 2x. Gaussian uses a standard deviation of Strength / 3, truncated at ±Strength. Distribution is compensated for each cycle's duration."));
		node.addEditor(DISTRIBUTION,"Distribution",Editor.comboBox(java.util.List.of(Distribution.values()),Distribution.UNIFORM,Distribution::id));
		node.addInput(SEED,"Seed",Editor.Slider.button(-1,-1.0,16777215.0).integers()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
		    .tooltip(com.vin.easyknack.ui.AppText.literal("-1 = automatic seed per logical node, shared by channel and instance copies. Connect Context Channel or Instance Index/Seed for independent randomness. Changing the seed restarts the random cycle."));
	}

	@Override
	public NativeProcessor processor() {

		return new NativeProcessor("digitalnoise", "nodes/DigitalNoiseNode.hpp",

				ParameterContract.builder().source(FREQUENCY).value(FREQUENCY).source(STRENGTH).value(STRENGTH).source(SEED).integer(SEED)
						.value(DISTRIBUTION).build());
	}
}
