package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id="easyknack.context.input",title="Context Input",category=NodeCategory.IO, kind=NodeKind.VALUE)
public final class ContextInputNode extends BasicNode {
    public static final PropertyId CHANNEL=new PropertyId("channel"),SAMPLE_RATE=new PropertyId("sample-rate"),BLOCK_SIZE=new PropertyId("block-size");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(CHANNEL,"Channel").rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Current rendering channel: −1 = left, +1 = right. Channel-dependent downstream nodes run separately for each side. This is a context value, not a pan control."));
        n.addOutput(SAMPLE_RATE,"Sample Rate [Hz]").rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.literal("DSP sample rate in Hz, currently 48000. This is the host processing rate, not an external device's rate."));
        n.addOutput(BLOCK_SIZE,"Block Size [samples]").rate(ProcessingRate.CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Actual frames per channel in the current DSP block, at most 256. The last block may be shorter; this is not total routing latency."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("context", "nodes/ContextInputNode.hpp",
            ParameterContract.builder().build()); }
}
