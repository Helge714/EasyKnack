package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.blur",title="Spectrum Blur",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectrumBlurNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),WIDTH=new PropertyId("width");
    public static final PropertyId MODE=new PropertyId("mode"),FACTOR=new PropertyId("factor");
    public static final PropertyId OCTAVES=new PropertyId("octaves");
    public static final PropertyId KERNEL=new PropertyId("kernel"),NORMALIZE=new PropertyId("normalize");
    public static final PropertyId PRESERVE_PHASE=new PropertyId("preserve-phase");
    public static final PropertyId SYNTHESIS=new PropertyId("synthesis");
    @Override public NodeState normalizeState(NodeState state){
        if(state.values().containsKey(SYNTHESIS.value())||!state.values().containsKey(PRESERVE_PHASE.value()))return state;
        var values=new java.util.HashMap<>(state.values());
        values.put(SYNTHESIS.value(),com.vingame.glswing.node.model.GLNodeValue.of(state.flag(PRESERVE_PHASE,false)?"preserve":"continuous"));
        return new NodeState(values,state.connectedInputs());
    }
    @Override public String encode(NodeState state,java.util.function.ToIntFunction<PropertyId> input){return super.encode(normalizeState(state),input);}
    @Override public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){
        var values=new java.util.HashMap<>(super.defaults());values.putIfAbsent("width",com.vingame.glswing.node.model.GLNodeValue.of(0.));values.putIfAbsent("factor",com.vingame.glswing.node.model.GLNodeValue.of(1.));values.putIfAbsent("octaves",com.vingame.glswing.node.model.GLNodeValue.of(0.));return java.util.Map.copyOf(values);
    }
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(MODE,"Distribution",Editor.comboBox(java.util.List.of("Bins","Octaves","Factor (legacy)"),"Octaves",v->v.startsWith("Factor")?"factor":v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(KERNEL,"Kernel",Editor.comboBox(java.util.List.of("Gaussian","Box","Maximum"),"Gaussian",v->v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(SYNTHESIS,"Synthesis",Editor.comboBox(java.util.List.of("Noise","Continuous","Preserve"),"Noise",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Noise fills frequency regions using a continuous noise source. Continuous produces bin-centred tones. Preserve changes magnitudes only and may cancel during IFFT overlap. Zero width is always an exact bypass."));
        n.addEditor(NORMALIZE,"Normalize power",Editor.checkBox(false))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Preserve total spectral power; in Noise mode this is an expected value, not a per-frame noise limiter. Not a perceived loudness normalization."));
        if(s.text(MODE,"octaves").equals("octaves"))n.addInput(OCTAVES,"Octaves",Editor.Slider.button(0,0.0,null)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Logarithmic radius in octaves. 0 is unchanged; 1 spreads from half to twice the frequency. Connected values above 1 are supported. Negative values act as 0. DC is unchanged."));
        else if(s.text(MODE,"bins").equals("factor"))n.addInput(FACTOR,"Factor",Editor.Slider.button(1,1.0,null)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Logarithmic power distribution from frequency / factor to frequency * factor. Factor 2 spans one octave each way. Accepts connected values above 1 without an upper slider limit. Values below 1 are treated as 1; DC is unchanged. Discrete bin centres determine the available destinations."));
        else n.addInput(WIDTH,"Width (bins)",Editor.Slider.button(0,0.0,8192.0).integers().discrete()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Kernel radius in bins. Zero is an exact bypass. Gaussian uses a radius of three standard deviations. Synthesis selects noise, tones or magnitude-only processing."));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumblur","nodes/SpectrumBlurNode.hpp",ParameterContract.builder().source(IN).source(WIDTH,s->s.text(MODE,"octaves").equals("bins")).value(WIDTH).source(FACTOR,s->s.text(MODE,"octaves").equals("factor")).value(FACTOR).value(MODE).source(OCTAVES,s->s.text(MODE,"octaves").equals("octaves")).value(OCTAVES).value(KERNEL).value(NORMALIZE).value(SYNTHESIS).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
