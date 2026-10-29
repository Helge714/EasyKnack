package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.ifft",title="IFFT",category=NodeCategory.SPECTRAL,kind=NodeKind.AUDIO)
public final class IFFTNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out");
    public static final PropertyId STABILITY=new PropertyId("stability");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio").rate(ProcessingRate.AUDIO);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Reconstructs Spectrum frames with normalized overlap-add. Synchronous latency: one window. Asynchronous FFT is allowed, but late or missing frames may cause gaps or artifacts."));
        n.addEditor(STABILITY,"Stability",Editor.Slider.bar(0,0,1))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Smooths short loudness fluctuations after overlap-add. 0 is unchanged; 1 applies full bounded RMS correction (10 ms / 500 ms). May soften intentional dynamics and slightly broaden the spectrum. Adds no latency."));
    }
    public NativeProcessor processor(){return new NativeProcessor("ifft","nodes/IFFTNode.hpp",ParameterContract.builder().source(IN).value(STABILITY).build());}
}
