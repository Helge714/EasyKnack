package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;
@RegisterNode(id="easyknack.midi.transpose",title="MIDI Transpose",category=NodeCategory.MIDI, kind=NodeKind.MIDI)
public final class MidiTransposeNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("midi-in"),OUT=new PropertyId("midi-out"),VALUE=new PropertyId("transpose");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"MIDI").dataType(PortDataType.MIDI).signal(SignalCategory.MIDI).rate(ProcessingRate.EVENT);
        n.addInput(IN,"MIDI").dataType(PortDataType.MIDI).signal(SignalCategory.MIDI).rate(ProcessingRate.EVENT);
        n.addEditor(VALUE,"Transpose",Editor.Slider.button(0,-48.0,48.0).formats(UnitFormats.PITCH_SEMITONE)).tooltip(com.vin.easyknack.ui.AppText.literal("Pitch offset: 1 st = one semitone, 2 st = one whole tone, 0.01 st = one cent. Applies to new notes; fractional tuning is preserved internally."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("miditranspose", "nodes/MidiTransposeNode.hpp",
            ParameterContract.builder().source(IN).value(VALUE).build()); }
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
