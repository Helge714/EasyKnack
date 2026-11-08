package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.bincontext",title="Bin Context",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class BinContextNode extends BasicNode {
    @Override public boolean requiresSpectralContext(){return true;}
    @Override public boolean supportsSpectralBins() { return true; }
    public static final PropertyId IN=new PropertyId("spectrum");
    public boolean isListedInCatalog(){return false;}
    public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        String[] ids={"real","imaginary","magnitude","phase","frequency","index","position","time"};
        String[] labels={"Real","Imaginary","Magnitude","Phase [rad]","Frequency [Hz]","Bin index","Position [0-1]","Frame time [s]"};
        for(int i=0;i<ids.length;i++)n.addOutput(new PropertyId(ids[i]),labels[i]).formats(switch(ids[i]){case "phase"->new UnitFormats(UnitFormat.RADIAN,UnitFormat.RADIAN,UnitFormats.PHASE.choices());case "frequency"->UnitFormats.CLOCK;case "time"->UnitFormats.TIME_SECONDS;default->null;}).signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.i18n.AppText.literal("Evaluated once per bin inside a spectral group. Magnitude uses raw FFT coefficients; phase uses radians. This is a bin vector, not a temporal audio signal."));
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
    }
    public NativeProcessor processor(){return new NativeProcessor("bincontext","nodes/BinContextNode.hpp",ParameterContract.builder().source(IN).build());}
}
