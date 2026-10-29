package com.vin.easyknack.nodes;

/** An execution boundary, lowered by the Instance Group compiler. */
@RegisterNode(id="easyknack.multiple.combine",title="Instance Merge",category=NodeCategory.ROUTING,kind=NodeKind.AUDIO)
public final class InstanceCombineNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("in"),WEIGHT=new PropertyId("weight"),OUT=new PropertyId("out"),MODE=new PropertyId("mode");
    @Override public boolean isMuteable(){return false;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Combined").tooltip(com.vin.easyknack.ui.AppText.literal("Shared signal: nodes after this output run once, after all instances have been combined."));
        n.addEditor(MODE,"Combine",Editor.comboBox(java.util.List.of("Sum","Mean","Weighted Mean"),"Mean",v->switch(v){case "Sum"->"sum";case "Weighted Mean"->"weighted";default->"mean";}));
        n.addInput(IN,"Per-instance signal",Editor.Slider.button(0)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(WEIGHT,"Per-instance weight",Editor.Slider.bar(1,0,1)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Nonnegative weight per instance. Sum adds weighted signals; Mean divides by active count; Weighted Mean divides by total weight. Zero total weight produces silence."));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("instancecombine","nodes/InstanceCombineNode.hpp",ParameterContract.builder().source(IN).value(IN).source(WEIGHT).value(WEIGHT).value(MODE).build());}
}
