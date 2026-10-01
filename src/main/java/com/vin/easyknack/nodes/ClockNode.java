package com.vin.easyknack.nodes;

@RegisterNode(id = "easyknack.audio.clock", title = "Clock", category=NodeCategory.MODULATION, kind = NodeKind.VALUE)
public final class ClockNode extends BasicNode {

	public static final PropertyId FREQUENCY = new PropertyId("frequency");
	public static final PropertyId SYNC = new PropertyId("sync");
	public static final PropertyId OUT = new PropertyId("value-out");

	@Override
	public void define(NodeBuilder node, NodeState state) {

		node.addOutput(OUT, "Out").rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
		node.addInput(FREQUENCY,"Frequency",Editor.Slider.button(1.0,0.001,20000.0).formats(UnitFormats.CLOCK)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        ProcessingRate.addEditor(node,new PropertyId("rate"));
		node.addInput(SYNC, "Sync", new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
	}

	@Override
	public NativeProcessor processor() {

		return new NativeProcessor("clock", "nodes/ClockNode.hpp",
				ParameterContract.builder().source(FREQUENCY).value(FREQUENCY).source(SYNC).value(SYNC).build());
	}

	@Override
	public void mute(MuteRouting routing, NodeState state) {
	}

	@Override
	public void insert(InsertionRouting routing, NodeState state) {
	}
}
