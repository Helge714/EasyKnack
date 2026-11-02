package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.blur",title="Spectrum Blur",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectrumBlurNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),WIDTH=new PropertyId("width");
    public static final PropertyId UNIT=new PropertyId("width.unit");
    public static final java.util.List<UnitFormat> UNITS=java.util.List.of(UnitFormat.OCTAVE,UnitFormat.CENT,UnitFormat.SEMITONE,UnitFormat.BIN);
    public static final PropertyId KERNEL=new PropertyId("kernel"),NORMALIZE=new PropertyId("normalize");
    public static final PropertyId PRESERVE_PHASE=new PropertyId("preserve-phase");
    public static final PropertyId SYNTHESIS=new PropertyId("synthesis");
    @Override public NodeState normalizeState(NodeState state){
        if(state.text("mode","").equals("factor"))throw new IllegalArgumentException("Legacy Factor width is no longer supported; recreate Bin Width using Octaves or Bins.");
        var values=new java.util.HashMap<>(state.values());
        if(!values.containsKey(UNIT.value())){
            boolean bins=state.text("mode","").equals("bins");
            values.put(UNIT.value(),com.vingame.glswing.node.model.GLNodeValue.of(bins?"bin":"octave"));
            if(state.text("mode","").equals("octaves"))values.put(WIDTH.value(),com.vingame.glswing.node.model.GLNodeValue.of(state.number("octaves",0)));
        }
        if(!values.containsKey(SYNTHESIS.value())&&values.containsKey(PRESERVE_PHASE.value()))
            values.put(SYNTHESIS.value(),com.vingame.glswing.node.model.GLNodeValue.of(state.flag(PRESERVE_PHASE,false)?"preserve":"continuous"));
        return new NodeState(values,state.connectedInputs());
    }
    @Override public String encode(NodeState state,java.util.function.ToIntFunction<PropertyId> input){return super.encode(normalizeState(state),input);}
    private static UnitFormat unit(NodeState s){return UnitFormat.fromId(s.text(UNIT,"octave"));}
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(KERNEL,"Kernel",Editor.comboBox(java.util.List.of("Gaussian","Box","Maximum"),"Gaussian",v->v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(SYNTHESIS,"Synthesis",Editor.comboBox(java.util.List.of("Noise","Continuous","Preserve"),"Noise",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Noise fills frequency regions using a continuous noise source. Continuous produces bin-centred tones. Preserve changes magnitudes only and may cancel during IFFT overlap. Zero width is always an exact bypass."));
        n.addEditor(NORMALIZE,"Normalize power",Editor.checkBox(false))
            .tooltip(com.vin.easyknack.ui.AppText.literal("Preserve total spectral power; in Noise mode this is an expected value, not a per-frame noise limiter. Not a perceived loudness normalization."));
        var formats=unit(s)==UnitFormat.BIN?UnitFormats.fixed(UnitFormat.BIN):UnitFormats.OCTAVE_WIDTH;
        n.addInput(WIDTH,"Width",Editor.Slider.button(0,0.0,null)).formats(formats).unitChoices(UNITS).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.text("spectrum.blur.width.tooltip"));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumblur","nodes/SpectrumBlurNode.hpp",ParameterContract.builder().source(IN).source(WIDTH).value(WIDTH).mapped(UNIT,s->unit(s)==UnitFormat.BIN?"bin":"octave").value(KERNEL).value(NORMALIZE).value(SYNTHESIS).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
