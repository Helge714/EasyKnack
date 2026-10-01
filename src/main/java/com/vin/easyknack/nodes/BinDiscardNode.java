package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.bindiscard",title="Bin Discard",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class BinDiscardNode extends BasicNode {
    public static final PropertyId DISCARD=new PropertyId("discard");
    @Override public boolean supportsSpectralBins(){return true;}
    @Override public boolean isMuteable(){return false;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addInput(DISCARD,"Discard",new Editor.Toggle(false)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Nonzero discards this bin. Evaluated before the bin's other work, once per FFT frame. Connect a comparison of Bin Context amplitude. Audio-dependent conditions are not allowed."));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("bindiscard","nodes/BinDiscardNode.hpp",ParameterContract.builder().source(DISCARD).value(DISCARD).build());}
}
