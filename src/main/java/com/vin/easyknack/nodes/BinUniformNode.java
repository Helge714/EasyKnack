package com.vin.easyknack.nodes;
/** Compiler-generated bridge from a public numeric group input to a held frame value. */
@RegisterNode(id="easyknack.spectral.uniform",title="Frame Value",category=NodeCategory.SPECTRAL,kind=NodeKind.VALUE)
public final class BinUniformNode extends BasicNode {
    public boolean supportsSpectralBins(){return true;}
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("out"),"Value").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);
        n.addEditor(new PropertyId("slot"),"Slot",Editor.Slider.button(0,0.0,15.0).integers().discrete());
    }
    public NativeProcessor processor(){return new NativeProcessor("binuniform","nodes/BinUniformNode.hpp",ParameterContract.builder().integer(new PropertyId("slot")).build());}
}
