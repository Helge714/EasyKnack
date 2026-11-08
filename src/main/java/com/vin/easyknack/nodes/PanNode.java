package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;
@RegisterNode(id="easyknack.audio.pan",title="Pan",category=NodeCategory.ROUTING, kind=NodeKind.AUDIO)
public final class PanNode extends BasicNode {
    @Override public boolean supportsSampleFeedback() { return true; }
    public static final PropertyId IN=new PropertyId("audio-in"),PAN=new PropertyId("pan"),OUT=new PropertyId("audio-out");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Stereo Out");
        n.addInput(IN,"Audio In");
        n.addInput(PAN,"Pan",Editor.Slider.bar(0,-1,1)).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("−1 = left, 0 = center, +1 = right. Constant-power pan for mono; balance for stereo. Before Merge: per voice. After Merge: whole mix."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("pan", "nodes/PanNode.hpp",
            ParameterContract.builder().source(IN).source(PAN).value(PAN).build()); }
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
}
