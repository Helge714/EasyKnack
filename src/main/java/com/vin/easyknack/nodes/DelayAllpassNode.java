package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.audio.delayallpass",title="Delay Allpass",category=NodeCategory.EFFECT,kind=NodeKind.AUDIO)
public final class DelayAllpassNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),TIME=new PropertyId("time"),FEEDBACK=new PropertyId("feedback");
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio Out");n.addInput(IN,"Audio In");
        n.addInput(TIME,"Delay",Editor.Slider.button(.01,1./48000,1.).formats(UnitFormats.TIME_SECONDS)).rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE);
        n.addInput(FEEDBACK,"Feedback",Editor.Slider.bar(.5,-.99,.99)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("delayallpass","nodes/DelayAllpassNode.hpp",ParameterContract.builder().source(IN).source(TIME).value(TIME).source(FEEDBACK).value(FEEDBACK).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
