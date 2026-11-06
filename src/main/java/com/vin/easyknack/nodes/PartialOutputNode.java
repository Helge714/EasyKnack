package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.partials.output",title="Partial Output",category=NodeCategory.SPECTRAL,kind=NodeKind.PARTIALS)
public final class PartialOutputNode extends BasicNode {
    public boolean requiresPartialContext(){return true;}
    public boolean isListedInCatalog(){return false;}public boolean isMuteable(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("partials"),"Partials").dataType(PortDataType.PARTIALS).signal(SignalCategory.PARTIALS).rate(ProcessingRate.CONTROL);
        n.addInput(new PropertyId("frequency"),"Frequency",Editor.Slider.button(440,0.,null).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(new PropertyId("amplitude"),"Amplitude",Editor.Slider.button(1,0.,null)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(new PropertyId("phase"),"Phase",Editor.Slider.button(0).formats(new UnitFormats(UnitFormat.TURN,UnitFormat.TURN,UnitFormats.PHASE.choices()))).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
    }
    public NativeProcessor processor(){return new NativeProcessor("partialoutput","nodes/PartialGraphNode.hpp",ParameterContract.builder().source(new PropertyId("frequency")).value(new PropertyId("frequency")).source(new PropertyId("amplitude")).value(new PropertyId("amplitude")).source(new PropertyId("phase")).value(new PropertyId("phase")).build());}
}
