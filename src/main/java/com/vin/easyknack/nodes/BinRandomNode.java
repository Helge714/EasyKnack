package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.binrandom",title="Bin Random",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class BinRandomNode extends BasicNode {
    @Override public boolean requiresSpectralContext(){return true;}
    public static final PropertyId OUT=new PropertyId("out"),SEED=new PropertyId("seed"),MODE=new PropertyId("mode");
    public static final PropertyId BY_BIN=new PropertyId("by-bin"),BY_FRAME=new PropertyId("by-frame");
    @Override public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){
        var values=new java.util.HashMap<>(super.defaults());values.put(MODE.value(),com.vingame.glswing.node.model.GLNodeValue.of("fixed"));return java.util.Map.copyOf(values);
    }
    @Override public NodeState normalizeState(NodeState state){
        var values=new java.util.HashMap<>(state.values());
        String mode=state.text(MODE,"fixed");
        values.putIfAbsent(BY_BIN.value(),com.vingame.glswing.node.model.GLNodeValue.of(!mode.equals("constant")&&!mode.equals("shared-frame")));
        values.putIfAbsent(BY_FRAME.value(),com.vingame.glswing.node.model.GLNodeValue.of(mode.equals("frame")||mode.equals("shared-frame")));
        return new NodeState(values,state.connectedInputs());
    }
    @Override public String encode(NodeState state,java.util.function.ToIntFunction<PropertyId> input){
        state=normalizeState(state);var values=new java.util.HashMap<>(state.values());
        values.put(MODE.value(),com.vingame.glswing.node.model.GLNodeValue.of(state.flag(BY_BIN,true)?state.flag(BY_FRAME,false)?"frame":"fixed":state.flag(BY_FRAME,false)?"shared-frame":"constant"));
        return super.encode(new NodeState(values,state.connectedInputs()),input);
    }
    @Override public boolean supportsSpectralBins(){return true;}
    @Override public void define(NodeBuilder node,NodeState state){
        node.addOutput(OUT,"Random").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Uniform random value from 0 (inclusive) to 1 (exclusive) per bin. Same seed, bin and frame produce the same value, also across channels."));
        node.addEditor(SEED,"Seed",Editor.Slider.button(0,0.0,2147483647.0).integers().discrete());
        state=normalizeState(state);
        node.addEditor(BY_BIN,"Vary by bin",Editor.checkBox(state.flag(BY_BIN,true))).tooltip(com.vin.easyknack.ui.AppText.literal("Different random values for each frequency bin. Disabled: all bins share the same value."));
        node.addEditor(BY_FRAME,"Vary by frame",Editor.checkBox(state.flag(BY_FRAME,false))).tooltip(com.vin.easyknack.ui.AppText.literal("New random values for every FFT window, not every audio block. Disabled: values remain constant over time."));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("binrandom","nodes/BinRandomNode.hpp",
        ParameterContract.builder().integer(SEED).value(MODE).build());}
}
