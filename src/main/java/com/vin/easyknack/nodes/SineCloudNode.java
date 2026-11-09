package com.vin.easyknack.nodes;

@RegisterNode(id="easyknack.audio.sinecloud", title="Sine Cloud", category=NodeCategory.GENERATOR, kind=NodeKind.AUDIO)
public final class SineCloudNode extends BasicNode {
    public static final PropertyId COUNT=new PropertyId("count"), MIN=new PropertyId("minimum"),
        MAX=new PropertyId("maximum"), SEED=new PropertyId("seed");
    @Override public void define(NodeBuilder node,NodeState state){
        node.addOutput(new PropertyId("audio-out"),"Audio Out").rate(ProcessingRate.AUDIO);
        node.addOutput(new PropertyId("actual-count"),"Actual count").signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addInput(COUNT,"Count",Editor.Slider.button(10000,1d,1000000d).integers().discrete()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addInput(MIN,"Min Frequency",Editor.Slider.button(20,.00001,20000d).formats(UnitFormats.FREQUENCY).discrete()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addInput(MAX,"Max Frequency",Editor.Slider.button(20000,.00001,20000d).formats(UnitFormats.FREQUENCY).discrete()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
        node.addInput(SEED,"Seed",Editor.Slider.button(0,0d,16777215d).integers().discrete()).signal(SignalCategory.VALUE).rate(ProcessingRate.CONTROL);
    }
    @Override public NativeProcessor processor(){
        return new NativeProcessor("sinecloud","nodes/SineCloudNode.hpp",ParameterContract.builder()
            .source(COUNT).integer(COUNT).source(MIN).value(MIN).source(MAX).value(MAX).source(SEED).integer(SEED).build());
    }
}
