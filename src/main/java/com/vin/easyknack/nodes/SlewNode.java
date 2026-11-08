package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.modulation.slew",title="Smoothing",category=NodeCategory.MATH,kind=NodeKind.VALUE)
public final class SlewNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("in"),OUT=new PropertyId("out"),RISE=new PropertyId("rise"),FALL=new PropertyId("fall"),RATE=new PropertyId("rate");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        ProcessingRate.addEditor(n,RATE,ProcessingRate.NUMERIC,ProcessingRate.AUDIO);
        n.addInput(IN,"In",Editor.Slider.button(0,null,null)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(RISE,"Rise",Editor.Slider.button(.03,0.0,60.0).formats(UnitFormats.TIME_SECONDS)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Exponential rise time constant: reaches 63% of a step after this time, about 95% after three times. Zero follows immediately. Audio output smooths Control steps sample by sample."));
        n.addInput(FALL,"Fall",Editor.Slider.button(.03,0.0,60.0).formats(UnitFormats.TIME_SECONDS)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Exponential fall time constant. First valid input initializes directly; graph edits preserve the current value. Control output updates once per block using the elapsed block duration."));
    }
    public NativeProcessor processor(){return new NativeProcessor("slew","nodes/SlewNode.hpp",ParameterContract.builder().source(IN).value(IN).source(RISE).value(RISE).source(FALL).value(FALL).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
