package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.partials.context",title="Partial Context",category=NodeCategory.SPECTRAL,kind=NodeKind.PARTIALS)
public final class PartialContextNode extends BasicNode {
    public boolean requiresPartialContext(){return true;}
    public boolean isListedInCatalog(){return false;}public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        String[] ids={"frequency","amplitude","phase","id","age","index","time","seed","tonality"};String[] labels={"Frequency","Amplitude","Phase","ID","Age","Index","Frame time","Seed","Tonality"};
        for(int i=0;i<ids.length;i++){var row=n.addOutput(new PropertyId(ids[i]),labels[i]).signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).formats(switch(ids[i]){case "frequency"->UnitFormats.FREQUENCY;case "phase"->new UnitFormats(UnitFormat.TURN,UnitFormat.TURN,UnitFormats.PHASE.choices());case "age","time"->UnitFormats.TIME_SECONDS;default->null;});
            if(ids[i].equals("id"))row.tooltip(com.vin.easyknack.ui.AppText.text("partials.id.help"));
            if(ids[i].equals("seed"))row.tooltip(com.vin.easyknack.ui.AppText.text("partials.seed.help"));
        }
        n.addInput(new PropertyId("partials"),"Partials").dataType(PortDataType.PARTIALS).signal(SignalCategory.PARTIALS).rate(ProcessingRate.CONTROL);
    }
    public NativeProcessor processor(){return new NativeProcessor("partialcontext","nodes/PartialGraphNode.hpp",ParameterContract.builder().source(new PropertyId("partials")).build());}
}
