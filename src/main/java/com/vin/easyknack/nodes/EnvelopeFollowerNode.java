package com.vin.easyknack.nodes;
import java.util.List;
@RegisterNode(id="easyknack.audio.envelopefollower",title="Envelope Follower",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class EnvelopeFollowerNode extends BasicNode {
    public enum Detector {
        PEAK,RMS;
        public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
        @Override public String toString(){return NodeText.text("envelopefollower.detector."+id(),this==PEAK?"Peak":"RMS");}
    }
    private static final UnitFormats AMPLITUDE=new UnitFormats(UnitFormat.FACTOR,UnitFormat.FACTOR,UnitFormats.GAIN_DB.choices());
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("envelope"),MODE=new PropertyId("mode"),ATTACK=new PropertyId("attack"),RELEASE=new PropertyId("release");
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Envelope").signal(SignalCategory.VALUE).formats(AMPLITUDE);
        n.addEditor(MODE,"Detector",Editor.comboBox(List.of(Detector.values()),Detector.PEAK,Detector::id));
        n.addInput(IN,"Audio In");
        n.addInput(ATTACK,"Attack",Editor.Slider.button(5,0.,2000.).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(RELEASE,"Release",Editor.Slider.button(100,0.,10000.).formats(UnitFormats.TIME_MS)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("envelopefollower","nodes/EnvelopeFollowerNode.hpp",ParameterContract.builder().source(IN).source(ATTACK).value(ATTACK).source(RELEASE).value(RELEASE).value(MODE).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.silence();}
}
