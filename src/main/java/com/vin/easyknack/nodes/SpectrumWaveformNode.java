package com.vin.easyknack.nodes;
import com.vingame.glswing.node.model.*;
@RegisterNode(id="easyknack.spectrum.waveform",title="Spectrum to Waveform",category=NodeCategory.SPECTRAL,kind=NodeKind.WAVETABLE)
public final class SpectrumWaveformNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("spectrum"),OUT=new PropertyId("waveform"),FREQUENCY=new PropertyId("frequency"),CAPTURE=new PropertyId("capture"),NORMALIZE=new PropertyId("normalize"),SAVED=new PropertyId("saved"),COPY=new PropertyId("copy");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Waveform").dataType(PortDataType.WAVEFORM).signal(SignalCategory.WAVETABLE).rate(ProcessingRate.CONTROL);
        n.addInput(IN,"Spectrum").dataType(PortDataType.SPECTRUM).signal(SignalCategory.SPECTRUM).rate(ProcessingRate.CONTROL);
        n.addInput(FREQUENCY,"Fundamental",Editor.Slider.button(440,1.0,20000.0).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addInput(CAPTURE,"Capture",new Editor.Trigger()).dataType(PortDataType.TRIGGER).signal(SignalCategory.TRIGGER).rate(ProcessingRate.EVENT);
        n.addEditor(NORMALIZE,"Normalize",new Editor.CheckBox(true));
        n.addEditor(COPY,"Copy captured curve",new CaptureReadout()).editorCopyable(false);
        n.addEditor(SAVED,"Captured samples",new Editor(){
            public GLNodeValue defaultValue(){return GLNodeValue.of("empty");}
            public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){return new Control(new com.vingame.glswing.ui.GLLabel(""),v->{});}
            public void validate(GLNodeValue value){String text=(String)value.toJavaValue();if(!text.equals("empty")&&!text.matches("[0-9a-f]{16384}"))throw new IllegalArgumentException("Invalid captured waveform");}
        }).visible(false);
    }
    public NativeProcessor processor(){return new NativeProcessor("spectrumwaveform","nodes/SpectrumWaveformNode.hpp",ParameterContract.builder().source(IN).source(FREQUENCY).value(FREQUENCY).source(CAPTURE).value(NORMALIZE).value(SAVED).build());}
    @Override public com.vin.easyknack.ui.BasicNodeView createView(com.vin.easyknack.ui.NodeViewContext context,GLGraphNode model){return new com.vin.easyknack.ui.WaveformCaptureView(this,context,model);}
}
