package com.vin.easyknack.nodes;

import java.util.List;

@RegisterNode(id="easyknack.audio.mix", title="Mix", category=NodeCategory.ROUTING, kind=NodeKind.VALUE)
public final class MixNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback(){return true;}

    public static final PropertyId A=new PropertyId("a"), B=new PropertyId("b"),
            FACTOR=new PropertyId("factor"), OUT=new PropertyId("audio-out"), RATE=new PropertyId("rate"), MODE=new PropertyId("mode");
    @Override public void define(NodeBuilder n, NodeState s) {
        n.addOutput(OUT,"Out").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(MODE,"Mode",Editor.comboBox(List.of("Linear","Equal Power"),"Linear",v->v.equals("Equal Power")?"equalpower":"linear"))
                .tooltip(com.vin.easyknack.i18n.AppText.literal("Linear preserves the level of identical signals. Equal Power preserves expected power for uncorrelated signals; identical signals can gain about 3 dB at the midpoint."));
        n.addInput(FACTOR,"Factor",Editor.Slider.bar(.5,0,1)).formats(UnitFormats.PROPORTION).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Crossfade: 0 = A, 1 = B. Linear uses weights 1−factor and factor; Equal Power uses cosine and sine weights. Factor is clamped to 0–1."));
        n.addInput(A,"A",Editor.Slider.button(0)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addInput(B,"B",Editor.Slider.button(0)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        ProcessingRate.addEditor(n,RATE);
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("mix", "nodes/MixNode.hpp",
            ParameterContract.builder().source(A).value(A).source(B).value(B).source(FACTOR).value(FACTOR).value(MODE).build()); }
    @Override public void mute(MuteRouting r,NodeState s) { r.connect(A,OUT); }
    @Override public void insert(InsertionRouting r,NodeState s) { r.connect(A,OUT); }
    @Override public PropertyId rerouteConnection(PropertyId occupied,NodeState state){
        return occupied.equals(A)?B:occupied.equals(B)?A:null;
    }
}
