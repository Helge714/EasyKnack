package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.partials.spectrum",title="Partials to Spectrum",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class PartialsSpectrumNode extends BasicNode {
    public void define(NodeBuilder n,NodeState s){n.addOutput(new PropertyId("spectrum"),"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);n.addInput(new PropertyId("partials"),"Partials").dataType(PortDataType.PARTIALS).signal(SignalCategory.PARTIALS).rate(ProcessingRate.CONTROL);}
    public NativeProcessor processor(){return new NativeProcessor("partialsspectrum","nodes/PartialsSpectrumNode.hpp",ParameterContract.builder().source(new PropertyId("partials")).build());}
}
