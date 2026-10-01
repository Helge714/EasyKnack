package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.spectral.blur",title="Spectrum Blur",category=NodeCategory.SPECTRAL,kind=NodeKind.ANALYSIS)
public final class SpectrumBlurNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("out"),WIDTH=new PropertyId("width");
    public static final PropertyId MODE=new PropertyId("mode"),FACTOR=new PropertyId("factor");
    public static final PropertyId OCTAVES=new PropertyId("octaves");
    @Override public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){
        var values=new java.util.HashMap<>(super.defaults());values.putIfAbsent("width",com.vingame.glswing.node.model.GLNodeValue.of(0.));values.putIfAbsent("factor",com.vingame.glswing.node.model.GLNodeValue.of(1.));values.putIfAbsent("octaves",com.vingame.glswing.node.model.GLNodeValue.of(0.));return java.util.Map.copyOf(values);
    }
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addEditor(MODE,"Distribution",Editor.comboBox(java.util.List.of("Bins","Octaves","Factor (legacy)"),"Bins",v->v.startsWith("Factor")?"factor":v.toLowerCase(java.util.Locale.ROOT)));
        if(s.text(MODE,"bins").equals("octaves"))n.addInput(OCTAVES,"Octaves",Editor.Slider.button(0,0.0,null)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Logarithmic radius in octaves. 0 is unchanged; 1 spreads from half to twice the frequency. Connected values above 1 are supported. Negative values act as 0. DC is unchanged."));
        else if(s.text(MODE,"bins").equals("factor"))n.addInput(FACTOR,"Factor",Editor.Slider.button(1,1.0,null)).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Logarithmic power distribution from frequency / factor to frequency * factor. Factor 2 spans one octave each way. Accepts connected values above 1 without an upper slider limit. Values below 1 are treated as 1; DC is unchanged. Discrete bin centres determine the available destinations."));
        else n.addInput(WIDTH,"Width (bins)",Editor.Slider.button(0,0.0,8192.0).integers()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.ui.AppText.literal("Radius in bins, rounded to whole bins. Distributes each bin's power uniformly to its neighbours, preserving total spectral power including at DC/Nyquist. Zero is an exact bypass. Keeps destination phases; use Bin Random separately for phase randomization. Applied to each FFT frame using the current block value."));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumblur","nodes/SpectrumBlurNode.hpp",ParameterContract.builder().source(IN).source(WIDTH,s->s.text(MODE,"bins").equals("bins")).value(WIDTH).source(FACTOR,s->s.text(MODE,"bins").equals("factor")).value(FACTOR).value(MODE).source(OCTAVES,s->s.text(MODE,"bins").equals("octaves")).value(OCTAVES).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
