package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.binhold",title="Bin Sample & Hold",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class BinHoldNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("in"),TRIGGER=new PropertyId("trigger"),OUT=new PropertyId("out");
    @Override public boolean supportsSpectralBins(){return true;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Value").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);
        n.addInput(IN,"Value",Editor.Slider.button(0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(TRIGGER,"Capture",new Editor.Trigger()).signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Captures the next complete FFT frame, separately for each bin and channel. Before capture, values pass through. Holding magnitude alone keeps the incoming phase; it is not an independent spectral freeze."));
    }
    public NativeProcessor processor(){return new NativeProcessor("binhold","nodes/BinHoldNode.hpp",ParameterContract.builder().source(IN).value(IN).source(TRIGGER).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
