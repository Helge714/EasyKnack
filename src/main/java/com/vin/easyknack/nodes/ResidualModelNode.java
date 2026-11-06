package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.residualmodel",title="Residual Model",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class ResidualModelNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),WIDTH=new PropertyId("width"),TIME=new PropertyId("time"),SEED=new PropertyId("seed");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Residual").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(WIDTH,"Envelope width",Editor.Slider.button(200,0.,20000.).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        n.addEditor(TIME,"Envelope smoothing",Editor.Slider.button(0,0.,10000.).formats(UnitFormats.TIME_MS)).inspectorOnly();
        n.addEditor(SEED,"Seed",Editor.Slider.button(0,0.,2147483647.).integers().discrete()).inspectorOnly();
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("residualmodel","nodes/ResidualModelNode.hpp",ParameterContract.builder().source(IN).source(WIDTH).value(WIDTH).value(TIME).integer(SEED).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
