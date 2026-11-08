package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.analysis.spectrum",title="FFT",category=NodeCategory.SPECTRAL,kind=NodeKind.SPECTRUM)
public final class SpectrumNode extends BasicNode {
    public static final java.util.List<String> WINDOW_SIZES=java.util.List.of("64","128","256","512","1024","2048","4096","8192","16384","32768","65536");
    public static int minimumSynchronousHop(int size){return Math.min(64,size/4);}
    @Override public NodeState normalizeState(NodeState state){
        var value=state.values().get("overlap");
        if(value!=null&&value.toJavaValue() instanceof String old){
            double overlap=switch(old){case "none"->0;case "half"->.5;case "threequarters"->.75;default->throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unknown legacy overlap: ")+old);};
            var values=new java.util.HashMap<>(state.values());values.put("overlap",com.vingame.glswing.node.model.GLNodeValue.of(overlap));
            return new NodeState(values,state.connectedInputs());
        }
        return state;
    }
    @Override public String encode(NodeState state,java.util.function.ToIntFunction<PropertyId> input){return super.encode(normalizeState(state),input);}
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("analysis"),
        SIZE=new PropertyId("size"),WINDOW=new PropertyId("window"),OVERLAP=new PropertyId("overlap"),CHANNEL=new PropertyId("channel"),PADDING=new PropertyId("padding"),REMOVE_DC=new PropertyId("remove-dc"),SYNC=new PropertyId("synchronous"),INTERVAL=new PropertyId("interval-ms");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Complex spectrum for analysis, bin processing and IFFT. Every frame retains its original sample position."));
        n.addInput(IN,"Audio In").rates(PortRates.AUDIO_CONTROL).tooltip(com.vin.easyknack.i18n.AppText.literal("Audio is collected in an internal buffer. All preserves stereo channels and voices; Left, Right and Mix sum voices."));
        n.addEditor(SYNC,"Synchronous",Editor.checkBox(true)).tooltip(com.vin.easyknack.i18n.AppText.literal("Enabled: compute every frame in the audio path. Disabled: background analysis may deliver late or missing frames; IFFT remains allowed but can produce gaps or artifacts."));
        n.addEditor(INTERVAL,"Minimum frame interval",Editor.Slider.button(0,0.0,60000.0).unit(UnitFormat.MS)).inspectorOnly().visible(!s.flag(SYNC,true))
            .tooltip(NodeText.text("fft.interval.help","Minimum time between background FFT requests. Zero uses the overlap setting. Synchronous FFT is unaffected."));
        n.addEditor(SIZE,"Window size",Editor.comboBox(WINDOW_SIZES,"2048",v->v))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Samples per FFT window. A larger window improves frequency separation but covers a longer period of time. Changing this setting resets the analysis."));
        n.addEditor(WINDOW,"Window",Editor.comboBox(java.util.List.of("Hann","Hamming","Blackman","Rectangular"),"Hann",v->v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(PADDING,"Zero padding",Editor.comboBox(java.util.List.of("None","Linear correlation (2x)"),"None",v->v.equals("None")?"none":"double"))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("2x pads the window with zeros for linear autocorrelation. It interpolates the spectrum but does not improve frequency resolution. For pitch analysis, use a Rectangular window and 2x padding."));
        n.addEditor(REMOVE_DC,"Remove DC",Editor.checkBox(false)).tooltip(com.vin.easyknack.i18n.AppText.literal("Subtract the input window mean before windowing. Enable for the former DC-centered autocorrelation behavior."));
        int size=Integer.parseInt(s.text(SIZE,"2048"));
        int hop=Math.max(s.flag(SYNC,true)?minimumSynchronousHop(size):1,(int)Math.round(size*(1-s.number(OVERLAP,.75))));
        n.addEditor(OVERLAP,"Overlap",Editor.Slider.bar(.75,0,1).step(.001).formats(UnitFormats.PROPORTION))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Window overlap: 0–1 (or percent). Rounded to a whole-sample hop, minimum one sample. Current hop: ")+hop+com.vin.easyknack.i18n.AppText.text("fft.overlap.tail"));
        n.addEditor(CHANNEL,"Channel",Editor.comboBox(java.util.List.of("All","Left","Right","Mix"),"All",v->v.toLowerCase(java.util.Locale.ROOT)))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("All preserves stereo and polyphony for IFFT. Mix analyzes (Left + Right) / 2. Mono keeps its level; opposite-phase channels cancel. Polyphonic voices are summed."));
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrum","nodes/SpectrumNode.hpp",ParameterContract.builder()
        .source(IN).value(SIZE).value(WINDOW).value(OVERLAP).value(CHANNEL).value(PADDING).value(REMOVE_DC).value(SYNC).value(INTERVAL).build());}
}
