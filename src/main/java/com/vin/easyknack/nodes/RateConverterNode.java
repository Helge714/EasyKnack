package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id="easyknack.converter.rate", title="Rate Converter", category=NodeCategory.CONVERTER, kind=NodeKind.VALUE)
public final class RateConverterNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("value"), OUT=new PropertyId("out"),
            RATE=new PropertyId("rate"), REDUCTION=new PropertyId("reduction");
    public enum Reduction {
        FIRST("first","First sample"), LAST("last","Last sample"), MEAN("mean","Mean"),
        RMS("rms","RMS"), MINIMUM("minimum","Minimum"), MAXIMUM("maximum","Maximum"), PEAK("peak","Absolute peak");
        private final String id,label;
        Reduction(String id,String label){this.id=id;this.label=label;}
        public String id(){return id;}
        @Override public String toString(){return com.vin.easyknack.ui.AppText.literal(label);}
    }
    @Override public void define(NodeBuilder n,NodeState s){
        var rate=ProcessingRate.fromId(s.text(RATE,"control"));
        n.addOutput(OUT,"Out").signal(SignalCategory.VALUE).rate(rate);
        n.addInput(IN,"In",Editor.Slider.button(0)).signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO)
                .tooltip(com.vin.easyknack.ui.AppText.literal("Accepts Audio and Control values. Reads the full block without smoothing."));
        n.addEditor(RATE,"Output Rate",Editor.comboBox(List.of(ProcessingRate.CONTROL,ProcessingRate.AUDIO),ProcessingRate.CONTROL,ProcessingRate::id))
                .tooltip(com.vin.easyknack.ui.AppText.literal("Control: reduce each actual DSP block to one held value. Audio: preserve each sample. Trigger conversion uses separate nodes."));
        n.addEditor(REDUCTION,"Reduction",Editor.comboBox(List.of(Reduction.values()),Reduction.FIRST,Reduction::id)).enabled(rate==ProcessingRate.CONTROL)
                .tooltip(com.vin.easyknack.ui.AppText.literal("First/Last: one sample. Mean: signed average. RMS: square root of mean square. Minimum/Maximum: signed extremes. Absolute peak: greatest magnitude, always nonnegative. Uses the actual block length (up to 256 samples); averaging a bipolar waveform can yield zero."));
    }
    @Override public NativeProcessor processor(){return new NativeProcessor("rateconvert","nodes/RateConverterNode.hpp",
            ParameterContract.builder().source(IN).value(IN).value(RATE).value(REDUCTION).build());}
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
