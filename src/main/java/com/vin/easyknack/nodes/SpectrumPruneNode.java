package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.prune",title="Spectrum Prune",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectrumPruneNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),KEEP=new PropertyId("keep");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(KEEP,"Keep",Editor.Slider.bar(1,0,1).formats(UnitFormats.PROPORTION)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Keep the loudest fraction of nonnegative FFT bins, including DC and Nyquist. Count is rounded to nearest integer. 0 clears all bins, 1 is unchanged. Rank uses single-sided amplitude; ties prefer lower bin indices. Retained coefficients and phases are unchanged, without gain normalization. Applied per FFT frame using the current block value."));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumprune","nodes/SpectrumPruneNode.hpp",ParameterContract.builder().source(IN).source(KEEP).value(KEEP).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
