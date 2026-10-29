package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.binmemory",title="Bin Smoothing",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class BinMemoryNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("in"),UPDATE=new PropertyId("update"),OUT=new PropertyId("out");
    public boolean supportsSpectralBins(){return true;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Value").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);
        n.addInput(IN,"Value",Editor.Slider.button(0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(UPDATE,"Update",Editor.Slider.bar(1,0,1)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("New-value fraction per FFT frame: 0 holds, 1 replaces. The first frame initializes each bin. Smaller hops update more often. Audio inputs use the value at the window end."));
    }
    public NativeProcessor processor(){return new NativeProcessor("binmemory","nodes/BinMemoryNode.hpp",ParameterContract.builder().source(IN).value(IN).source(UPDATE).value(UPDATE).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
