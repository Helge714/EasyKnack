package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id="easyknack.audio.channelseparate", title="Channel Separate", category=NodeCategory.ROUTING, kind=NodeKind.AUDIO)
public final class ChannelSeparateNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("audio-in"), LEFT=new PropertyId("left"), RIGHT=new PropertyId("right");
    @Override public void define(NodeBuilder n, NodeState s) {
        n.addOutput(LEFT,"Left").tooltip(com.vin.easyknack.i18n.AppText.literal("Left input channel as a mono signal, identical on both channels."));
        n.addOutput(RIGHT,"Right").tooltip(com.vin.easyknack.i18n.AppText.literal("Right input channel as a mono signal, identical on both channels."));
        n.addInput(IN,"Stereo In").tooltip(com.vin.easyknack.i18n.AppText.literal("A mono source appears at both outputs. Voice groups are preserved."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("channelseparate", "nodes/ChannelSeparateNode.hpp",
            ParameterContract.builder().source(IN).build()); }
}
