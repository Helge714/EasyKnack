package com.vin.easyknack.nodes;

import java.util.Map;
import java.util.function.ToIntFunction;
import com.vingame.glswing.node.model.GLNodeValue;

/** Stateless definition, shared by all instances of one node type. */
public abstract class BasicNode {
    public com.vingame.glswing.node.GLNodeResizeMode resizeMode() { return com.vingame.glswing.node.GLNodeResizeMode.HORIZONTAL; }
    public com.vingame.glswing.node.GLNodePortOrder portOrder() { return com.vingame.glswing.node.GLNodePortOrder.OUTPUTS_FIRST; }
    /** Override for a custom view; definitions remain stateless and views belong to node instances. */
    public com.vin.easyknack.ui.BasicNodeView createView(com.vin.easyknack.ui.NodeViewContext context,
            com.vingame.glswing.node.model.GLGraphNode model) {
        return new com.vin.easyknack.ui.BasicNodeView(this,context,model);
    }
    public static final String AUDIO_SIGNAL = "easyknack.audio.signal";
    public abstract void define(NodeBuilder node, NodeState state);
    /** Compatibility normalization without changing a saved node's identity or connections. */
    public NodeState normalizeState(NodeState state){return state;}
    /** Presentation updates shared by the node and the inspector, without recreating controls. */
    public void updateEditor(PropertyId id, com.vingame.glswing.component.GLContainer control, NodeState state) { }
    public abstract NativeProcessor processor();
    /** Numeric signal defaults that the host may interpolate. Extra virtual ports are opt-in. */
    public Map<PropertyId,Integer> manualControlPorts(){var result=new java.util.LinkedHashMap<PropertyId,Integer>();var inputs=processor().inputs();for(int i=0;i<inputs.size();i++)result.put(inputs.get(i),i);return Map.copyOf(result);}
    public String nativeParameterType(){return getClass().getSimpleName()+"Parameters";}
    /** Protocol payload is owned by the node alongside its native binding. */
    public String encode(NodeState state, ToIntFunction<PropertyId> input) { return processor().contract().encode(this,state,input); }
    public boolean isListedInCatalog() { return true; }
    public boolean isMuteable() { return true; }
    /** Opt in only when the native processor implements the sample-feedback contract. */
    public boolean supportsSampleFeedback() { return false; }
    /** Audited sample kernel preserves trigger offsets inside feedback chunks. */
    public boolean supportsTriggerFeedback() { return false; }
    /** Opt in for bin-independent scalar evaluation; no temporal state or smoothing across bins. */
    public boolean supportsSpectralBins() { return false; }
    /** Auto scheduling can be derived from an exported internal plan instead of all inputs equally. */
    public ProcessingRate resolveAutomaticRate(NodeState state,Map<PropertyId,ProcessingRate> inputs) {
        return inputs.values().stream().filter(rate->rate!=ProcessingRate.EVENT).reduce(ProcessingRate.CONTROL,ProcessingRate::maximum);
    }
    public java.util.List<ProcessingRate> resolveAutomaticOutputRates(NodeState state,Map<PropertyId,ProcessingRate> inputs) {
        return java.util.List.of(); // Default: declared output rates follow the node's effective rate.
    }
    public void mute(MuteRouting routing, NodeState state) { routing.silence(); }
    /** Opt in with one input/output pair; generators and sinks remain excluded by default. */
    public void insert(InsertionRouting routing, NodeState state) { }
    /** Preferred source for Ctrl+Delete; null uses the first connected input in definition order. */
    public PropertyId exclusionInput(NodeState state) {
        var routing = new InsertionRouting(); insert(routing,state);
        return routing.ports()==null ? null : new PropertyId(routing.ports().inputPortId());
    }
    public final RegisterNode registration() {
        RegisterNode registration = getClass().getAnnotation(RegisterNode.class);
        if (registration == null) throw new IllegalStateException(com.vin.easyknack.ui.AppText.literal("Missing @RegisterNode: ") + getClass());
        return registration;
    }
    public String textPrefix(){return registration().id();}
    public final NodeBuilder layout(NodeState state) {
        state=normalizeState(state);NodeBuilder builder = new NodeBuilder(); define(builder, state); builder.applyInteractionPolicy(manualControlPorts()); builder.applyFormats(state); MidiDriverSettingsEditor.addSettings(builder,state); builder.applyRates(state); builder.translate(textPrefix()); builder.validate(state); return builder;
    }
    public final String title() { return NodeText.text(registration().id() + ".title", registration().title()); }
    public Map<String, GLNodeValue> defaults() { return layout(new NodeState(Map.of())).defaults(); }
}

