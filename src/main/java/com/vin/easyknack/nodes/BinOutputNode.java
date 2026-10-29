package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.binoutput",title="Bin Output",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class BinOutputNode extends BasicNode {
    @Override public boolean requiresSpectralContext(){return true;}
    @Override public boolean supportsSpectralBins() { return true; }
    public static final PropertyId A=new PropertyId("a"),B=new PropertyId("b"),MODE=new PropertyId("mode"),OUT=new PropertyId("spectrum");
    public boolean isListedInCatalog(){return false;}
    public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(MODE,"Components",Editor.comboBox(java.util.List.of("Real / imaginary","Magnitude / phase"),"Real / imaginary",v->v.startsWith("Real")?"cartesian":"polar"));
        boolean polar=s.text(MODE,"cartesian").equals("polar");
        n.addInput(A,polar?"Magnitude":"Real",Editor.Slider.button(0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(B,polar?"Phase [rad]":"Imaginary",Editor.Slider.button(0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
    }
    public NativeProcessor processor(){return new NativeProcessor("binoutput","nodes/BinOutputNode.hpp",ParameterContract.builder().source(A).value(A).source(B).value(B).value(MODE).build());}
}
