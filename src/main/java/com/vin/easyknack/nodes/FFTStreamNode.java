package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.spectral.fft",title="FFT Stream",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class FFTStreamNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;} // Legacy documents and source plugins.
    public static final PropertyId IN=new PropertyId("in"),OUT=new PropertyId("spectrum"),SIZE=new PropertyId("size"),WINDOW=new PropertyId("window"),OVERLAP=new PropertyId("overlap");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Complete spectral audio stream for IFFT. No frames are dropped; stereo channels and voices remain independent."));
        n.addInput(IN,"Audio").rates(PortRates.AUDIO_CONTROL);
        n.addEditor(SIZE,"Window size",Editor.comboBox(List.of("256","512","1024","2048","4096","8192"),"2048",v->v)).tooltip(com.vin.easyknack.ui.AppText.literal("FFT to IFFT latency equals this many samples. Changing size resets the stream."));
        n.addEditor(WINDOW,"Window",Editor.comboBox(List.of("Hann","Hamming","Blackman","Rectangular"),"Hann",v->v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(OVERLAP,"Overlap",Editor.comboBox(List.of("50 %","75 %"),"75 %",v->v.startsWith("50")?"2":"4"));
    }
    public NativeProcessor processor(){return new NativeProcessor("fftstream","nodes/FFTStreamNode.hpp",ParameterContract.builder().source(IN).value(SIZE).value(WINDOW).value(OVERLAP).build());}
}
