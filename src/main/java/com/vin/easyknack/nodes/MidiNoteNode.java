package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;

@RegisterNode(id="easyknack.midi.note",title="MIDI Note",category=NodeCategory.MIDI, kind=NodeKind.MIDI)
public final class MidiNoteNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("midi-in"),FREQUENCY=new PropertyId("frequency"),GATE=new PropertyId("gate"),
            TRIGGER=new PropertyId("trigger"),VELOCITY=new PropertyId("velocity"),CHANNEL=new PropertyId("channel"),BEND=new PropertyId("bend-range"),VOICES=new PropertyId("voices");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(FREQUENCY,"Frequency").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.ui.AppText.literal("Note frequency in Hz, including transposition and pitch bend. A4 = 440 Hz."));
        n.addOutput(GATE,"Gate").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.ui.AppText.literal("0 = released, 1 = held (including sustain pedal). Connect to Envelope Gate."));
        n.addOutput(TRIGGER,"Trigger").signal(SignalCategory.TRIGGER).dataType(PortDataType.TRIGGER).rate(ProcessingRate.EVENT).tooltip(com.vin.easyknack.ui.AppText.literal("One sample-timed trigger for each new note. Restarts an envelope even when Gate stays high."));
        n.addOutput(VELOCITY,"Velocity").signal(SignalCategory.VALUE).rate(ProcessingRate.AUDIO).tooltip(com.vin.easyknack.ui.AppText.literal("Note-on strength, normalized from MIDI 1-127 to 0-1; 0 when released."));
        n.addInput(IN,"MIDI").dataType(PortDataType.MIDI).signal(SignalCategory.MIDI).rate(ProcessingRate.EVENT);
        n.addEditor(CHANNEL,"Channel (0 = all)",Editor.Slider.button(0,0.0,16.0).integers().discrete());
        n.addEditor(VOICES,"Voices",Editor.Slider.button(1,1.0,32.0).integers().discrete()).tooltip(com.vin.easyknack.ui.AppText.literal("1 = monophonic last-note priority. 2-32 = separate voices; oldest held voice is stolen when full. Use Voice Merge after each voice envelope."));
        n.addEditor(BEND,"Pitch bend",Editor.Slider.button(2,0.0,48.0).formats(UnitFormats.PITCH_SEMITONE));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("midinote", "nodes/MidiNoteNode.hpp",
            ParameterContract.builder().source(IN).integer(CHANNEL).value(BEND).token("poly").integer(VOICES).build()); }
}
