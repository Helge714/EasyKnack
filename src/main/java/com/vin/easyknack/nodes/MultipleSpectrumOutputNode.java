package com.vin.easyknack.nodes;

/** Typed instance reduction boundary for aligned complex FFT frames. */
@RegisterNode(id="easyknack.multiple.spectrumoutput",title="Instance Spectrum Output",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class MultipleSpectrumOutputNode extends BasicNode {
    public boolean isListedInCatalog(){return false;}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(new PropertyId("out"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(new PropertyId("in"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
    }
    public NativeProcessor processor(){return new NativeProcessor("multiplespectrumoutput","nodes/MultipleSpectrumOutputNode.hpp",ParameterContract.builder().source(new PropertyId("in")).build());}
}
