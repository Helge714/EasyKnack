package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.blockdelay",title="Block Delay",category=NodeCategory.EFFECT,kind=NodeKind.VALUE)
public final class BlockDelayNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;}
    public static final PropertyId IN=new PropertyId("in"),OUT=new PropertyId("out");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out").previousStep().rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Previous DSP block. Starts with silence. One block is 256 samples (5.33 ms at 48 kHz). Breaks feedback cycles."));
        n.addInput(IN,"In",Editor.Slider.button(0)).rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Audio or Control value stored after the current block has finished. Independent state per channel and voice."));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("blockdelay","nodes/BlockDelayNode.hpp",ParameterContract.builder().source(IN).value(IN).build());}
    // Muting silences the delay; bypassing it would turn a valid feedback loop into a zero-delay cycle.
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
