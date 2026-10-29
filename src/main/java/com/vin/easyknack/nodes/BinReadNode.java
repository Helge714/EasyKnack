package com.vin.easyknack.nodes;
@RegisterNode(id="easyknack.spectral.binread",title="Bin Reader",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class BinReadNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    public static final String EXPLICIT="easyknack.bin.explicit-spectrum";
    @Override public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){var values=new java.util.HashMap<>(super.defaults());values.put(EXPLICIT,com.vingame.glswing.node.model.GLNodeValue.of(true));return java.util.Map.copyOf(values);}
    public static final PropertyId SPECTRUM=new PropertyId("spectrum"),INDEX=new PropertyId("index"),BOUNDARY=new PropertyId("boundary");
    @Override public String encode(NodeState state,java.util.function.ToIntFunction<PropertyId> input){
        if(!state.values().containsKey(EXPLICIT)){var values=new java.util.HashMap<>(state.values());values.put(EXPLICIT,com.vingame.glswing.node.model.GLNodeValue.of(false));state=new NodeState(values,state.connectedInputs());}
        return super.encode(state,input);
    }
    public void define(NodeBuilder n,NodeState s){
        for(String id:java.util.List.of("real","imaginary","magnitude","phase"))n.addOutput(new PropertyId(id),id.equals("phase")?"Phase [rad]":Character.toUpperCase(id.charAt(0))+id.substring(1)).formats(id.equals("phase")?new UnitFormats(UnitFormat.RADIAN,UnitFormat.RADIAN,UnitFormats.PHASE.choices()):null).signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO);
        n.addOutput(new PropertyId("amplitude"),"Amplitude").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.ui.AppText.literal("Single-sided amplitude normalized by the analysis window sum. Suitable for an additive oscillator's weight; Magnitude keeps raw FFT coefficients."));
        n.addOutput(new PropertyId("frequency"),"Frequency").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.ui.AppText.literal("Frequency of the selected bin in Hz."));
        n.addInput(SPECTRUM,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Connect the spectral group input. Reads bins from that incoming spectrum; unconnected readers output zero."));
        n.addInput(INDEX,"Bin index",Editor.Slider.button(0)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).tooltip(com.vin.easyknack.ui.AppText.literal("Inside a spectral group: reads the original incoming spectrum at a fractional bin index. Neighbours can be combined without depending on processing order."));
        n.addEditor(BOUNDARY,"Outside spectrum",Editor.comboBox(java.util.List.of("Zero","Clamp","Wrap"),"Zero",v->v.toLowerCase(java.util.Locale.ROOT)));
    }
    public NativeProcessor processor(){return new NativeProcessor("binread","nodes/BinReadNode.hpp",ParameterContract.builder().source(INDEX).value(INDEX).value(BOUNDARY).source(SPECTRUM).mapped(new PropertyId("spectrum-bound"),s->!s.flag(EXPLICIT,false)||s.isConnected(SPECTRUM)?"1":"0").build());}
}
