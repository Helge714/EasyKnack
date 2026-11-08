package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id="easyknack.midi.cc",title="MIDI CC",category=NodeCategory.MIDI, kind=NodeKind.MIDI)
public final class MidiCCNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("midi-in"),OUT=new PropertyId("value-out"),CHANNEL=new PropertyId("channel"),CC=new PropertyId("controller");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Value").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.i18n.AppText.literal("MIDI Control Change value, normalized from 0-127 to 0-1. Holds the most recently received value."));
        n.addInput(IN,"MIDI").dataType(PortDataType.MIDI).signal(SignalCategory.MIDI).rate(ProcessingRate.EVENT);
        n.addEditor(CHANNEL,"Channel (0 = all)",Editor.Slider.button(0,0.0,16.0).integers().discrete());
        n.addEditor(CC,"Controller",Editor.Slider.button(1,0.0,127.0).integers().discrete()).tooltip(com.vin.easyknack.i18n.AppText.literal("MIDI CC number: 1 = modulation wheel, 7 = volume, 64 = sustain pedal. Map the 0-1 output to your desired parameter range."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("midicc", "nodes/MidiCCNode.hpp",
            ParameterContract.builder().source(IN).integer(CHANNEL).integer(CC).build()); }
}
