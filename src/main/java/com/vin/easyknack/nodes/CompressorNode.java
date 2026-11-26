package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.audio.compressor",title="Compressor",category=NodeCategory.EFFECT,kind=NodeKind.AUDIO)
public final class CompressorNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),SIDECHAIN=new PropertyId("sidechain"),THRESHOLD=new PropertyId("threshold"),RATIO=new PropertyId("ratio"),KNEE=new PropertyId("knee"),ATTACK=new PropertyId("attack"),RELEASE=new PropertyId("release"),MAKEUP=new PropertyId("makeup"),LINK=new PropertyId("link");
    @Override public boolean supportsSampleFeedback(){return false;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio Out");n.addInput(IN,"Audio In");n.addInput(SIDECHAIN,"Sidechain").optional();
        n.addInput(THRESHOLD,"Threshold",Editor.Slider.button(-12,-120.,24.).formats(UnitFormats.GAIN_DB)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(RATIO,"Ratio",Editor.Slider.button(4,1.,100.)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(KNEE,"Knee",Editor.Slider.button(6,0.,48.).unit(UnitFormat.DB)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(ATTACK,"Attack",Editor.Slider.button(10,0.,2000.).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(RELEASE,"Release",Editor.Slider.button(100,0.,10000.).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(MAKEUP,"Make-up Gain",Editor.Slider.button(0,-60.,24.).formats(UnitFormats.GAIN_DB)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(LINK,"Stereo Link",Editor.checkBox(true));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("compressor","nodes/CompressorNode.hpp",ParameterContract.builder().source(IN).source(SIDECHAIN).source(THRESHOLD).value(THRESHOLD).source(RATIO).value(RATIO).source(KNEE).value(KNEE).source(ATTACK).value(ATTACK).source(RELEASE).value(RELEASE).source(MAKEUP).value(MAKEUP).value(LINK).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
