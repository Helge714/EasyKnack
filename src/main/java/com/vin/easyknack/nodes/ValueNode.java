package com.vin.easyknack.nodes;

@RegisterNode(id = "easyknack.audio.value", title = "Value", category=NodeCategory.IO, kind=NodeKind.VALUE)
public final class ValueNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback(){return true;}

    public static final PropertyId VALUE = new PropertyId("value"), OUT = new PropertyId("value-out");
    @Override public void define(NodeBuilder node, NodeState state) {
        node.addOutput(OUT, "Value Out").rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE);
        node.addEditor(VALUE, "Value", Editor.Slider.button(0));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("value", "nodes/ValueNode.hpp",
            ParameterContract.builder().value(VALUE).build()); }
}

