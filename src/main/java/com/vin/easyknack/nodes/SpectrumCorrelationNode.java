package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.correlation",title="Autocorrelation",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class SpectrumCorrelationNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("analysis"),NORMALIZATION=new PropertyId("normalization"),SMOOTHING=new PropertyId("smoothing");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Correlation").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Computes IFFT of the squared complex magnitude on the analysis worker. Normalized to lag zero; silence gives zero. FFT padding 2x gives linear correlation, None gives circular correlation. Windowing and DC removal follow the FFT settings."));
        n.addEditor(NORMALIZATION,"Normalization",Editor.comboBox(java.util.List.of("Biased","Unbiased"),"Biased",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Unbiased compensates for fewer overlapping samples at large lags in linear correlation; values are limited to ±1 like the legacy node. Use Rectangular, 2x padding and Remove DC for legacy behavior. Circular correlation has constant overlap. Window tapering is not undone."));
        n.addEditor(SMOOTHING,"Smoothing factor",Editor.Slider.bar(1,0,1).step(.01))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Per analysis frame: old × (1 − factor) + new × factor. 1 = immediate, 0 = hold the first frame. Smaller values smooth more. Initializes from the first frame; resets on FFT or normalization changes. Overlap determines update frequency."));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumcorrelation","nodes/SpectrumCorrelationNode.hpp",ParameterContract.builder().source(IN).value(NORMALIZATION).value(SMOOTHING).build());}
}
