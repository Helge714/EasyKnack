package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id="easyknack.audio.channelcombine", title="Channel Combine", category=NodeCategory.ROUTING, kind=NodeKind.AUDIO)
public final class ChannelCombineNode extends BasicNode {
    public static final PropertyId LEFT=new PropertyId("left"), RIGHT=new PropertyId("right"), OUT=new PropertyId("audio-out");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Stereo Out");
        n.addInput(LEFT,"Left").tooltip(com.vin.easyknack.i18n.AppText.literal("Signal for the left output. A stereo source contributes its left channel."));
        n.addInput(RIGHT,"Right").tooltip(com.vin.easyknack.i18n.AppText.literal("Signal for the right output. A stereo source contributes its right channel. Unconnected inputs are silent."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("channelcombine", "nodes/ChannelCombineNode.hpp",
            ParameterContract.builder().source(LEFT).source(RIGHT).build()); }
}
