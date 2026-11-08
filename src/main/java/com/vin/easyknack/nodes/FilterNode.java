package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id="easyknack.audio.filter",title="Filter",category=NodeCategory.EFFECT, kind=NodeKind.AUDIO)
public final class FilterNode extends BasicNode {
    public static final PropertyId ORDER=new PropertyId("order"),COMPENSATE=new PropertyId("compensate");
    @Override public boolean supportsSampleFeedback(){return true;}
    public enum Mode { LOWPASS("Lowpass"),HIGHPASS("Highpass"),BANDPASS("Bandpass");
        private final String label;Mode(String label){this.label=label;}
        public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
        @Override public String toString(){return NodeText.text("filter.mode."+id(),label);}
    }
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),CUTOFF=new PropertyId("cutoff"),Q=new PropertyId("resonance"),MODE=new PropertyId("mode");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio Out");
        n.addEditor(MODE,"Mode",Editor.comboBox(List.of(Mode.values()),Mode.LOWPASS,Mode::id)).keepVisibleOnHide();
        n.addEditor(ORDER,"Order",Editor.comboBox(List.of("2","4","6","8"),"2",v->v))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Higher orders give steeper slopes using cascaded second-order sections. Resonance is distributed across the sections. Order 2 preserves the original filter."));
        n.addEditor(COMPENSATE,"Level compensation",Editor.checkBox(false))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Matches smoothed output RMS to input RMS with gain limited to 0.25-4. Not loudness normalization or a limiter; cannot restore removed frequencies. Adds no latency."));
        n.addInput(IN,"Audio In");
        n.addInput(CUTOFF,"Cutoff",Editor.Slider.button(1000,5.0,20000.0).step(1).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE);
        n.addInput(Q,"Resonance",Editor.Slider.bar(.707,.1,30).unit(UnitFormat.Q)).signal(SignalCategory.VALUE);
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("filter", "nodes/FilterNode.hpp",
            ParameterContract.builder().source(IN).source(CUTOFF).value(CUTOFF).source(Q).value(Q).value(MODE).value(ORDER).value(COMPENSATE).build()); }
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
