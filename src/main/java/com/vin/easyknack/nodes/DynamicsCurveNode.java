package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.audio.dynamicscurve",title="Dynamics Curve",category=NodeCategory.MATH,kind=NodeKind.VALUE)
public final class DynamicsCurveNode extends BasicNode {
    private static final UnitFormats AMPLITUDE=new UnitFormats(UnitFormat.FACTOR,UnitFormat.FACTOR,UnitFormats.GAIN_DB.choices());
    public static final PropertyId IN=new PropertyId("level"),OUT=new PropertyId("gain"),THRESHOLD=new PropertyId("threshold"),RATIO=new PropertyId("ratio"),KNEE=new PropertyId("knee");
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public void define(NodeBuilder n,NodeState s){
        ProcessingRate.addEditor(n,new PropertyId("rate"));
        n.addOutput(OUT,"Gain").signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).formats(AMPLITUDE);
        n.addInput(IN,"Level",Editor.Slider.button(0,0.,null).formats(AMPLITUDE)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(THRESHOLD,"Threshold",Editor.Slider.button(-12,-120.,24.).formats(UnitFormats.GAIN_DB)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(RATIO,"Ratio",Editor.Slider.button(4,1.,100.)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(KNEE,"Knee",Editor.Slider.button(6,0.,48.).unit(UnitFormat.DB)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("dynamicscurve","nodes/DynamicsCurveNode.hpp",ParameterContract.builder().source(IN).value(IN).source(THRESHOLD).value(THRESHOLD).source(RATIO).value(RATIO).source(KNEE).value(KNEE).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.silence();}
}
