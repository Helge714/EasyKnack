package com.vin.easyknack.nodes;

import java.util.List;

import com.vingame.glswing.node.model.GLNodeValue;

@RegisterNode(id = "easyknack.audio.samplehold", title = "Sample & Hold", category=NodeCategory.MODULATION, kind = NodeKind.VALUE)
public final class SampleHoldNode extends BasicNode {

	public static final PropertyId IN = new PropertyId("audio-in");
	public static final PropertyId TRIGGER = new PropertyId("trigger");
	public static final PropertyId OUT = new PropertyId("value-out");
	public static final PropertyId RATE = new PropertyId("rate");

	@Override
	public void define(NodeBuilder node, NodeState state) {

		node.addOutput(OUT, "Out").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
		ProcessingRate.addEditor(node,RATE);
		node.addInput(IN, "In", Editor.Slider.button(0, -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
		node.addInput(TRIGGER, "Trigger", new Editor.Trigger()).rate(ProcessingRate.EVENT).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER);
	}
	
	@Override
	public java.util.Map<String, GLNodeValue> defaults() {
	    var values = new java.util.HashMap<>(super.defaults());

	    values.putIfAbsent(IN.value(), GLNodeValue.of(0));
	    values.putIfAbsent(TRIGGER.value(), GLNodeValue.of(0));

	    return values;
	}

    @Override public ProcessingRate resolveAutomaticRate(NodeState state,java.util.Map<PropertyId,ProcessingRate> inputs){
        return inputs.containsKey(TRIGGER)?ProcessingRate.AUDIO:super.resolveAutomaticRate(state,inputs);
    }
	@Override
	public NativeProcessor processor() {
		return new NativeProcessor("samplehold", "nodes/SampleHoldNode.hpp",
				ParameterContract.builder().source(IN).value(IN).source(TRIGGER).value(TRIGGER).build());
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
