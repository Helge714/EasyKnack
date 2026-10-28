package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;
import com.vin.easyknack.audio.MidiDevices;
import com.vingame.glswing.node.model.GLNodeValue;

@RegisterNode(id="easyknack.midi.input",title="MIDI Input",category=NodeCategory.MIDI, kind=NodeKind.MIDI)
public final class MidiInputNode extends BasicNode {
    public static final PropertyId OUT=new PropertyId("midi-out"),DEVICE=new PropertyId("device"),PANIC=new PropertyId("panic");
    public static final PropertyId KEYBOARD=new PropertyId("keyboard"),TRANSPOSE=new PropertyId("transpose");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"MIDI").dataType(PortDataType.MIDI).signal(SignalCategory.MIDI).rate(ProcessingRate.EVENT);
        n.addEditor(DEVICE,"Device",new MidiDeviceEditor());
        boolean keyboard=MidiDevices.Device.reference(s.text(DEVICE,MidiDevices.KEYBOARD.reference())).id().equals(MidiDevices.KEYBOARD.id());
        n.addEditor(TRANSPOSE,"Transpose",Editor.Slider.button(0,-48.0,48.0).integers().discrete().formats(UnitFormats.PITCH_SEMITONE)).visible(keyboard).tooltip(com.vin.easyknack.ui.AppText.literal("Keyboard transposition: 12 semitones = one octave. A plays C4 at zero."));
        n.addEditor(KEYBOARD,"Keyboard",new KeyboardMidiEditor()).visible(keyboard).enabled(keyboard).keepVisibleOnHide();
        n.addEditor(PANIC,"All notes off",new PanicEditor()).keepVisibleOnHide();
    }
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState s){
        if(control instanceof KeyboardMidiEditor.Keyboard keyboard)keyboard.setTranspose((int)s.number(TRANSPOSE,0));
    }
    private static final class PanicEditor implements Editor {
        @Override public GLNodeValue defaultValue(){return GLNodeValue.of(false);}
        @Override public Control create(String label,java.util.function.Consumer<GLNodeValue> ignored){
            var button=new com.vingame.glswing.ui.GLButton(label);button.addActionListener(e->MidiDevices.requestPanic());return new Control(button,v->{});
        }
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("midiin", "nodes/MidiInputNode.hpp",
            ParameterContract.builder().mapped(DEVICE,s -> MidiDevices.Device.reference(s.text(DEVICE,MidiDevices.KEYBOARD.reference())).id()).build()); }
}
