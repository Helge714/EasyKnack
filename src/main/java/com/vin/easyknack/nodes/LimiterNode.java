package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.audio.limiter",title="Limiter",category=NodeCategory.EFFECT,kind=NodeKind.AUDIO)
public final class LimiterNode extends BasicNode {
    private static final UnitFormats AMPLITUDE=new UnitFormats(UnitFormat.FACTOR,UnitFormat.FACTOR,UnitFormats.GAIN_DB.choices());
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),CEILING=new PropertyId("ceiling"),RELEASE=new PropertyId("release"),LOOKAHEAD=new PropertyId("lookahead");
    @Override public boolean supportsSampleFeedback(){return false;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio Out");n.addInput(IN,"Audio In");
        n.addInput(CEILING,"Ceiling",Editor.Slider.button(.98,0.,1.).formats(AMPLITUDE)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(RELEASE,"Release",Editor.Slider.button(100,0.,10000.).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(LOOKAHEAD,"Lookahead",Editor.Slider.button(5,0.,20.).formats(UnitFormats.TIME_MS).discrete()).tooltip(NodeText.text("limiter.lookahead.tooltip","Latency equals Lookahead."));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("limiter","nodes/LimiterNode.hpp",ParameterContract.builder().source(IN).source(CEILING).value(CEILING).source(RELEASE).value(RELEASE).value(LOOKAHEAD).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
