package com.vin.easyknack.nodes;

/** Complex arithmetic on aligned STFT frames. */
@RegisterNode(id="easyknack.spectral.math",title="Spectrum Math",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectrumMathNode extends BasicNode {
    public static final PropertyId A=new PropertyId("a"),B=new PropertyId("b");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("spectrum"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(new PropertyId("operation"),"Operation",Editor.comboBox(java.util.List.of("Add","Subtract","Multiply","Divide"),"Add",v->v.toLowerCase(java.util.Locale.ROOT))).keepVisibleOnHide();
        for(String id:java.util.List.of("a","b"))n.addInput(new PropertyId(id),id.toUpperCase()).dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL).optional();
        // Preserve independently weighted additions from earlier documents.
        for(String id:java.util.List.of("a","b"))n.addEditor(new PropertyId("gain-"+id),"Gain "+id.toUpperCase(),Editor.Slider.button(1,0.,null).formats(UnitFormats.PROPORTION)).inspectorOnly();
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrummath","nodes/SpectrumMathNode.hpp",ParameterContract.builder().source(new PropertyId("a")).source(new PropertyId("b")).value(new PropertyId("operation")).value(new PropertyId("gain-a")).value(new PropertyId("gain-b")).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(new PropertyId("a"),new PropertyId("spectrum"));}
    public void insert(InsertionRouting r,NodeState s){r.connect(new PropertyId("a"),new PropertyId("spectrum"));}
    @Override public PropertyId rerouteConnection(PropertyId occupied,NodeState state){
        return occupied.equals(A)?B:occupied.equals(B)?A:null;
    }
}
