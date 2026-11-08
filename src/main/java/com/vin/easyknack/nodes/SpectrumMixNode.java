package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.mix",title="Spectrum Mix",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectrumMixNode extends BasicNode {
    public static final PropertyId A=new PropertyId("a"),B=new PropertyId("b");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("spectrum"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        for(String id:java.util.List.of("a","b"))n.addInput(new PropertyId(id),id.toUpperCase()).dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL).optional();
        n.addEditor(new PropertyId("mode"),"Mode",Editor.comboBox(java.util.List.of("Linear","Equal Power"),"Linear",v->v.equals("Equal Power")?"equalpower":"linear"));
        n.addInput(new PropertyId("factor"),"Factor",Editor.Slider.bar(.5,0,1).formats(UnitFormats.PROPORTION)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.text("spectrum.mix.factorHelp"));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrummix","nodes/SpectrumMixNode.hpp",ParameterContract.builder().source(new PropertyId("a")).source(new PropertyId("b")).source(new PropertyId("factor")).value(new PropertyId("factor")).value(new PropertyId("mode")).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(new PropertyId("a"),new PropertyId("spectrum"));}
    public void insert(InsertionRouting r,NodeState s){r.connect(new PropertyId("a"),new PropertyId("spectrum"));}
    @Override public PropertyId rerouteConnection(PropertyId occupied,NodeState state){
        return occupied.equals(A)?B:occupied.equals(B)?A:null;
    }
}
