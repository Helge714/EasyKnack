package com.vin.easyknack.nodes;

@RegisterNode(id = "easyknack.audio.delay", title = "Delay", category=NodeCategory.EFFECT, kind = NodeKind.VALUE)
public final class DelayNode extends BasicNode {
    @Override public boolean supportsSampleFeedback(){return true;}

	public static final PropertyId IN = new PropertyId("audio-in");
	public static final PropertyId TIME = new PropertyId("time");
	public static final PropertyId OUT = new PropertyId("audio-out");

	@Override
	public void define(NodeBuilder node, NodeState state) {

		node.addOutput(OUT, "Out").previousStep().rate(ProcessingRate.AUDIO).signal(SignalCategory.AUDIO);
		node.addInput(IN, "In", Editor.Slider.button(0.0)).rate(ProcessingRate.AUDIO)
				.signal(SignalCategory.AUDIO);
		node.addInput(TIME, "Time", Editor.Slider.button(0.25, 0.0, 10.0).formats(UnitFormats.TIME_SECONDS)).rate(ProcessingRate.CONTROL)
				.signal(SignalCategory.VALUE).tooltip(com.vin.easyknack.i18n.AppText.literal("Delay time, sampled once per block. Feedback uses at least one sample and automatically selects the processing step. Time modulation must originate outside the feedback loop."));
	}

	@Override
	public NativeProcessor processor() {

		return new NativeProcessor("delay", "nodes/DelayNode.hpp",
				ParameterContract.builder().source(IN).value(IN).source(TIME).value(TIME).build());
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
