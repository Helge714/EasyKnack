package com.vin.easyknack.nodes;

import java.util.List;

/** Frequency-distribution descriptors, evaluated once per incoming FFT frame. */
@RegisterNode(id="easyknack.analysis.spectralfeatures",title="Spectral Features",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class SpectralFeaturesNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),WEIGHT=new PropertyId("weighting"),
        ROLL=new PropertyId("rolloff-fraction"),NORMALIZE=new PropertyId("normalize-flux");
    public static final List<String> OUTPUTS=List.of("centroid","spread","skewness","kurtosis","flux","rolloff","slope","flatness");
    @Override public void define(NodeBuilder n,NodeState s){
        for(String name:OUTPUTS){
            var row=n.addOutput(new PropertyId(name),Character.toUpperCase(name.charAt(0))+name.substring(1))
                .signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
            if(name.equals("centroid")||name.equals("spread")||name.equals("rolloff"))row.formats(UnitFormats.FREQUENCY);
        }
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(WEIGHT,"Weighting",Editor.comboBox(List.of("Power","Magnitude"),"Power",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip("Power uses one-sided bin energy; Magnitude uses one-sided amplitude. Moments describe the distribution over frequency, not bin-height statistics. Kurtosis is ordinary kurtosis (Gaussian = 3). Slope is normalized linear regression per Hz.");
        n.addEditor(ROLL,"Roll-off fraction",Editor.Slider.bar(.85,0,1).formats(UnitFormats.PROPORTION))
            .tooltip("Frequency below which this fraction of the selected spectral weight lies. Power weighting measures an energy fraction.");
        n.addEditor(NORMALIZE,"Normalize flux",Editor.checkBox(true))
            .tooltip("Enabled: Euclidean distance between unit-sum spectral weights; ignores overall level. Disabled: distance between unnormalized weights. First frame and FFT/source changes start with zero flux. Silence and undefined zero-spread moments return zero.");
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("spectralfeatures","nodes/SpectralFeaturesNode.hpp",
        ParameterContract.builder().source(IN).value(WEIGHT).value(ROLL).value(NORMALIZE).build());}
}
