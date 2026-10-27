package com.vin.easyknack.nodes;

import java.util.List;

/** Frequency-distribution descriptors, evaluated once per incoming FFT frame. */
@RegisterNode(id="easyknack.analysis.spectralfeatures",title="Spectral Features",category=NodeCategory.ANALYSIS,kind=NodeKind.ANALYSIS)
public final class SpectralFeaturesNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),WEIGHT=new PropertyId("weighting"),
        ROLL=new PropertyId("rolloff-fraction"),NORMALIZE=new PropertyId("normalize-flux"),SCALE=new PropertyId("frequency-scale");
    private enum FrequencyScale {
        LINEAR, LOGARITHMIC;
        String id(){return name().toLowerCase(java.util.Locale.ROOT);}
        @Override public String toString(){return NodeText.text("spectral.scale."+id(),this==LINEAR?"Linear":"Logarithmic");}
    }
    private static final UnitFormats LOG_SPREAD=new UnitFormats(UnitFormat.OCTAVE,UnitFormat.OCTAVE,List.of(UnitFormat.OCTAVE,UnitFormat.SEMITONE,UnitFormat.CENT));
    @Override public NodeState normalizeState(NodeState state){
        var formats=state.text(SCALE,"linear").equals("logarithmic")?LOG_SPREAD:UnitFormats.FREQUENCY;
        var unit=UnitFormat.fromId(state.text("spread.unit",formats.stored().id()));
        if(formats.choices().contains(unit))return state;
        var values=new java.util.HashMap<>(state.values());
        values.put("spread.unit",com.vingame.glswing.node.model.GLNodeValue.of(formats.initial().id()));
        return new NodeState(values,state.connectedInputs());
    }
    public static final List<String> OUTPUTS=List.of("centroid","spread","skewness","kurtosis","flux","rolloff","slope","flatness");
    @Override public void define(NodeBuilder n,NodeState s){
        boolean logarithmic=s.text(SCALE,"linear").equals("logarithmic");
        for(String name:OUTPUTS){
            var row=n.addOutput(new PropertyId(name),Character.toUpperCase(name.charAt(0))+name.substring(1))
                .signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
            if(name.equals("centroid")||name.equals("rolloff"))row.formats(UnitFormats.FREQUENCY);
            if(name.equals("spread"))row.formats(logarithmic?LOG_SPREAD:UnitFormats.FREQUENCY);
        }
        n.addInput(IN,"Spectrum").dataType(PortDataType.ANALYSIS).signal(SignalCategory.ANALYSIS).rate(ProcessingRate.CONTROL);
        n.addEditor(SCALE,"Frequency scale",Editor.comboBox(List.of(FrequencyScale.values()),FrequencyScale.LINEAR,FrequencyScale::id)).inspectorOnly()
            .tooltip(NodeText.text("spectral.scale.help","Linear: moments over Hz. Logarithmic: moments over log2(frequency), excluding DC. Centroid is returned in Hz; Spread in octaves. Skewness and Kurtosis use the selected scale. Flux, Roll-off, Slope and Flatness are unchanged; Slope remains per Hz."));
        n.addEditor(WEIGHT,"Weighting",Editor.comboBox(List.of("Power","Magnitude"),"Power",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip("Power uses one-sided bin energy; Magnitude uses one-sided amplitude. Moments describe the distribution over frequency, not bin-height statistics. Kurtosis is ordinary kurtosis (Gaussian = 3). Slope is normalized linear regression per Hz.");
        n.addEditor(ROLL,"Roll-off fraction",Editor.Slider.bar(.85,0,1).formats(UnitFormats.PROPORTION))
            .tooltip("Frequency below which this fraction of the selected spectral weight lies. Power weighting measures an energy fraction.");
        n.addEditor(NORMALIZE,"Normalize flux",Editor.checkBox(true))
            .tooltip("Enabled: Euclidean distance between unit-sum spectral weights; ignores overall level. Disabled: distance between unnormalized weights. First frame and FFT/source changes start with zero flux. Silence and undefined zero-spread moments return zero.");
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("spectralfeatures","nodes/SpectralFeaturesNode.hpp",
        ParameterContract.builder().source(IN).value(WEIGHT).value(ROLL).value(NORMALIZE).value(SCALE).build());}
}
