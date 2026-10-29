package com.vin.easyknack.nodes;
/** Compiler bridge: collect external triggers between successive FFT frame ends. */
@RegisterNode(id="easyknack.spectral.trigger",title="Frame Trigger",category=NodeCategory.SPECTRAL,kind=NodeKind.TRIGGER)
public final class BinTriggerNode extends BasicNode {
    public boolean supportsSpectralBins(){return true;}
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("out"),"Trigger").signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT);
        n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.0,15.0).integers());
    }
    public NativeProcessor processor(){return new NativeProcessor("bintrigger","nodes/BinTriggerNode.hpp",ParameterContract.builder().integer(new PropertyId("slot")).build());}
}
