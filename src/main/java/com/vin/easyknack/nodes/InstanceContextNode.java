package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.multiple.context",title="Instance Context",category=NodeCategory.ROUTING,kind=NodeKind.VALUE)
public final class InstanceContextNode extends BasicNode {
    public static final PropertyId COUNT=new PropertyId("count");
    public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        String[] ids={"index","count-out","position","seed"},labels={"Index","Count","Position","Seed"};
        for(int i=0;i<ids.length;i++)n.addOutput(new PropertyId(ids[i]),labels[i]).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Instance Group context: zero-based instance index, active count, normalized position (0 for a single instance), and a stable per-instance seed."));

    }
    public NativeProcessor processor(){return new NativeProcessor("instancecontext","nodes/InstanceContextNode.hpp",ParameterContract.builder().build());}
}
