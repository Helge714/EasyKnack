package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.audio.allpass",title="Allpass",category=NodeCategory.EFFECT,kind=NodeKind.AUDIO)
public final class AllpassNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),ORDER=new PropertyId("order"),FREQUENCY=new PropertyId("frequency"),Q=new PropertyId("resonance");
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio Out");
        n.addEditor(ORDER,"Order",Editor.comboBox(List.of("1","2"),"1",v->v));
        n.addInput(IN,"Audio In");
        n.addInput(FREQUENCY,"Frequency",Editor.Slider.button(1000,1.,20000.).formats(UnitFormats.FREQUENCY)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(Q,"Resonance",Editor.Slider.button(.7071067811865476,.1,30.).unit(UnitFormat.Q)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE).visible(s.text(ORDER,"1").equals("2"));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("allpass","nodes/AllpassNode.hpp",ParameterContract.builder().source(IN).source(FREQUENCY).value(FREQUENCY).source(Q).value(Q).value(ORDER).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
