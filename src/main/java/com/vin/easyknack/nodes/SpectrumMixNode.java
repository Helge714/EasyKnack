package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.mix",title="Spectrum Mix",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectrumMixNode extends BasicNode {
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("spectrum"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        for(String id:java.util.List.of("a","b"))n.addInput(new PropertyId(id),id.toUpperCase()).dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL).optional();
        n.addEditor(new PropertyId("gain-a"),"Gain A",Editor.Slider.button(1,0.,null).formats(UnitFormats.PROPORTION));
        n.addEditor(new PropertyId("gain-b"),"Gain B",Editor.Slider.button(1,0.,null).formats(UnitFormats.PROPORTION));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrummix","nodes/SpectrumMixNode.hpp",ParameterContract.builder().source(new PropertyId("a")).source(new PropertyId("b")).value(new PropertyId("gain-a")).value(new PropertyId("gain-b")).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(new PropertyId("a"),new PropertyId("spectrum"));}
    public void insert(InsertionRouting r,NodeState s){r.connect(new PropertyId("a"),new PropertyId("spectrum"));}
}
