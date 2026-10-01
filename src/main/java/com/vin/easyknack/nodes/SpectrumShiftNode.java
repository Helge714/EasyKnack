package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.shift",title="Spectrum Shift",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class SpectrumShiftNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),SHIFT=new PropertyId("shift"),COMPENSATE=new PropertyId("compensate");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(SHIFT,"Shift (bins)",Editor.Slider.button(0,-8192.0,8192.0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Positive shifts move bins upward. One bin = sample rate / FFT size Hz. Fractional values interpolate complex bins; out-of-range energy is discarded. This is frequency translation, not musical pitch transposition."));
        n.addEditor(COMPENSATE,"Continuous phase",Editor.checkBox(true)).tooltip(com.vin.easyknack.ui.AppText.literal("Compensates frame-time phase for a constant bin shift. Fast shift modulation can still produce artifacts."));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumshift","nodes/SpectrumShiftNode.hpp",ParameterContract.builder().source(IN).source(SHIFT).value(SHIFT).value(COMPENSATE).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
