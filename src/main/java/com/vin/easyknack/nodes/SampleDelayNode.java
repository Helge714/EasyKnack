package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.sampledelay",title="Sample Delay",category=NodeCategory.EFFECT,kind=NodeKind.VALUE)
public final class SampleDelayNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;}
    public static final PropertyId IN=new PropertyId("in"),OUT=new PropertyId("out");
    @Override public boolean supportsSampleFeedback(){return true;}
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out").previousStep().rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Previous sample. Starts with silence. One sample is 20.83 microseconds at 48 kHz. Only the feedback loop runs sample by sample."));
        n.addInput(IN,"In",Editor.Slider.button(0)).rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Audio or Control. Feedback supports Math, Mix, Gain, Unit Converter and Filter. Block-based processors require Block Delay instead."));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("sampledelay","nodes/SampleDelayNode.hpp",ParameterContract.builder().source(IN).value(IN).build());}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
