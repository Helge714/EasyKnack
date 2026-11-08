package com.vin.easyknack.nodes;
import java.util.List;
import java.util.function.ToIntFunction;
@RegisterNode(id="easyknack.midi.delay",title="MIDI Delay",category=NodeCategory.MIDI, kind=NodeKind.MIDI)
public final class MidiDelayNode extends BasicNode {
    public static final PropertyId IN=new PropertyId("midi-in"),OUT=new PropertyId("midi-out"),VALUE=new PropertyId("delay");
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"MIDI").dataType(PortDataType.MIDI).signal(SignalCategory.MIDI).rate(ProcessingRate.EVENT);
        n.addInput(IN,"MIDI").dataType(PortDataType.MIDI).signal(SignalCategory.MIDI).rate(ProcessingRate.EVENT);
        n.addEditor(VALUE,"Delay",Editor.Slider.button(100,0.0,10000.0).formats(UnitFormats.TIME_MS)).tooltip(com.vin.easyknack.i18n.AppText.literal("Delays notes and controller events together. Changes affect new events; queued events retain their due time. Panic clears the queue immediately."));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("mididelay", "nodes/MidiDelayNode.hpp",
            ParameterContract.builder().source(IN).value(VALUE).build()); }
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
