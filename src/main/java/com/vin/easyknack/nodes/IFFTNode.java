package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.ifft",title="IFFT",category=NodeCategory.SPECTRAL,kind=NodeKind.AUDIO)
public final class IFFTNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio").rate(ProcessingRate.AUDIO);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Reconstructs Spectrum frames with normalized overlap-add. Synchronous latency: one window. Asynchronous FFT is allowed, but late or missing frames may cause gaps or artifacts."));
    }
    public NativeProcessor processor(){return new NativeProcessor("ifft","nodes/IFFTNode.hpp",ParameterContract.builder().source(IN).build());}
}
