package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;
@RegisterNode(id="easyknack.audio.voicemerge",title="Voice Merge",category=NodeCategory.ROUTING, kind=NodeKind.AUDIO)
public final class VoiceMergeNode extends BasicNode {
    public enum Mode {
        ADD("sum","Add"),MEAN("mean","Mean");
        private final String id,label;
        Mode(String id,String label){this.id=id;this.label=label;}
        public String id(){return id;}
        public String toString(){return NodeText.text("merge."+id,label);}
    }
    public static final PropertyId IN=new PropertyId("audio-in"),OUT=new PropertyId("audio-out"),MODE=new PropertyId("mode");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Audio").tooltip(com.vin.easyknack.i18n.AppText.literal("Combines voices into one signal per channel. Stereo positions are preserved; downstream effects run once per channel."));
        n.addEditor(MODE,"Mode",Editor.comboBox(List.of(Mode.values()),Mode.ADD,Mode::id))
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Add sums all voices. Mean divides by the configured voice count, including silent/released voices. A mono input passes through."));
        n.addInput(IN,"Voices").tooltip(com.vin.easyknack.i18n.AppText.literal("Voice audio after its envelope. Use Pan before Merge for per-voice placement or after Merge for the whole mix."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("voicemerge", "nodes/VoiceMergeNode.hpp",
            ParameterContract.builder().source(IN).token("combine").value(MODE).build()); }
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
