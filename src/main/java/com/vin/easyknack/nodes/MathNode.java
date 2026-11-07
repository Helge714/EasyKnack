package com.vin.easyknack.nodes;

import java.util.List;

import com.vin.easyknack.model.MathOperation;
import com.vingame.glswing.node.model.GLNodeValue;

@RegisterNode(id = "easyknack.audio.math", title = "Math", category = NodeCategory.MATH, kind = NodeKind.VALUE)
public final class MathNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback(){return true;}


	public static final PropertyId A = new PropertyId("a");
	public static final PropertyId B = new PropertyId("b");
	public static final PropertyId CLAMP = new PropertyId("clamp");
	public static final PropertyId OPERATION = new PropertyId("operation");
	public static final PropertyId OUT = new PropertyId("audio-out");
	public static final PropertyId RATE = new PropertyId("rate");

	@Override
	public void define(NodeBuilder node, NodeState state) {
		var signal = SignalCategory.VALUE;
		var operation = operation(state);
		node.addOutput(OUT, "Out").rates(PortRates.AUDIO_CONTROL).signal(signal);
		node.addEditor(OPERATION, "Operation",
				Editor.comboBox(List.of(MathOperation.values()), MathOperation.MULTIPLY, MathOperation::id).grouped(op->op.group().name().charAt(0)+op.group().name().substring(1).toLowerCase(java.util.Locale.ROOT)))
				.keepVisibleOnHide().tooltip(operation.tooltip());
		node.addEditor(CLAMP, "Clamp ±1", Editor.checkBox(false));
		ProcessingRate.addEditor(node,RATE);
		node.addInput(A, operation.a().label(), Editor.Slider.button(operation.a().defaultValue(), -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL)
				.signal(signal).tooltip(operation.tooltip());
		if (!operation.unary()) {
			node.addInput(B, operation.b().label(), Editor.Slider.button(operation.b().defaultValue(), -1000000.0, 1000000.0)).rates(PortRates.AUDIO_CONTROL)
					.signal(signal).tooltip(operation.tooltip());
		}
	}

	/**
	 * Keep persistent fallbacks for parameters that may disappear from the dynamic
	 * layout. Operation-specific automatic defaults will later be handled using
	 * explicit/default property provenance.
	 */
	@Override
	public java.util.Map<String, GLNodeValue> defaults() {
		var values = new java.util.HashMap<>(super.defaults());
		values.putIfAbsent(B.value(), GLNodeValue.of(1));
		return values;
	}

	private MathOperation operation(NodeState state) {
		return MathOperation.fromId(state.text(OPERATION, "multiply"));
	}

	@Override
	public NativeProcessor processor() {
		return new NativeProcessor("math", "nodes/MathNode.hpp", ParameterContract.builder().source(A).value(A)
				.source(B, state -> !operation(state).unary()).value(B).value(OPERATION).value(CLAMP).build());
	}

	@Override
	public void mute(MuteRouting routing, NodeState state) {
		routing.connect(A, OUT);
	}

	@Override
	public void insert(InsertionRouting routing, NodeState state) {
		routing.connect(A, OUT);
	}
    @Override public PropertyId rerouteConnection(PropertyId occupied,NodeState state){
        return occupied.equals(A)?B:occupied.equals(B)?A:null;
    }
}
