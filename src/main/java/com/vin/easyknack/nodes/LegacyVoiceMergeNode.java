package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;
@RegisterNode(id="easyknack.audio.legacyvoicemerge",title="Voice Merge (legacy stereo)",category=NodeCategory.ROUTING, kind=NodeKind.AUDIO)
public final class LegacyVoiceMergeNode extends BasicNode {
    @Override public boolean isListedInCatalog(){return false;}
    public enum Mode {
        SUM,MEAN,SPREAD,PITCH;
        public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
        public String toString(){return NodeText.text("merge."+id(),switch(this){case SUM->"Sum";case MEAN->"Mean";case SPREAD->"Stereo spread";case PITCH->"Stereo by pitch";});}
    }
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),MODE=new PropertyId("mode"),PITCH=new PropertyId("pitch"),WIDTH=new PropertyId("width");
    @Override public void define(NodeBuilder n,NodeState s){
        String mode=s.text(MODE,"sum");
        n.addOutput(OUT,"Audio").tooltip(com.vin.easyknack.i18n.AppText.literal("Mixed voices. Subsequent effects run once per channel. Sum can exceed ±1; Mean divides by configured voice count, including released voices."));
        n.addEditor(MODE,"Mode",Editor.comboBox(List.of(Mode.values()),Mode.SUM,Mode::id));
        n.addInput(IN,"Voices").tooltip(com.vin.easyknack.i18n.AppText.literal("Connect voice audio after its envelope. Outputs also sum voices automatically; use Merge to choose mixing and stereo distribution."));
        n.addInput(PITCH,"Pitch [Hz]").rate(ProcessingRate.AUDIO).signal(SignalCategory.VALUE).visible(mode.equals("pitch"))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Frequency from the same MIDI Note voice group: C4 is center, C2 left, C6 right. Without a connection voices stay centered."));
        n.addEditor(WIDTH,"Width",Editor.Slider.bar(1,0,1)).formats(UnitFormats.PROPORTION).visible(mode.equals("spread")||mode.equals("pitch"));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("voicemerge", "nodes/VoiceMergeNode.hpp",
            ParameterContract.builder().source(IN).token("mix").value(MODE).source(PITCH,s -> s.text(MODE,"sum").equals("pitch")).value(WIDTH).build()); }
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
