package com.vin.easyknack.nodes;

import java.util.List;
@RegisterNode(id="easyknack.music.scalequantizer",title="Scale Quantizer",category=NodeCategory.MATH,kind=NodeKind.VALUE)
public final class ScaleQuantizerNode extends BasicNode {
    public static final PropertyId HYSTERESIS=new PropertyId("hysteresis");
    public static final UnitFormats HYSTERESIS_UNITS=new UnitFormats(UnitFormat.SEMITONE,UnitFormat.SEMITONE,List.of(UnitFormat.SEMITONE,UnitFormat.CENT));
    public static final PropertyId IN=new PropertyId("frequency"),OUT=new PropertyId("out"),MODE=new PropertyId("mode"),TARGET=new PropertyId("target"),ROOT=new PropertyId("root"),SCALE=new PropertyId("scale"),TUNING=new PropertyId("tuning"),RATE=new PropertyId("rate");
    public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Target [Hz]").formats(UnitFormats.CLOCK).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL);
        n.addEditor(MODE,"Mode",Editor.comboBox(List.of("Scale","Fixed"),"Scale",v->v.toLowerCase(java.util.Locale.ROOT)));
        n.addEditor(ROOT,"Root",Editor.comboBox(List.of("C","C#","D","D#","E","F","F#","G","G#","A","A#","B"),"C",v->Integer.toString(List.of("C","C#","D","D#","E","F","F#","G","G#","A","A#","B").indexOf(v))));
        n.addEditor(SCALE,"Scale",Editor.comboBox(List.of("Chromatic","Major","Minor","Major pentatonic","Minor pentatonic"),"Chromatic",v->v.toLowerCase(java.util.Locale.ROOT).replace(' ','_')));
        ProcessingRate.addEditor(n,RATE);
        n.addInput(IN,"Frequency",Editor.Slider.button(440,0.0,20000.0).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Quantizes positive Hz to the nearest allowed equal-tempered note, measured in semitones. Ties choose the lower note. Invalid/zero input returns zero in Scale mode. Use Pitch Hold before this node for unreliable detection."));
        n.addInput(TARGET,"Fixed target",Editor.Slider.button(440,1.0,20000.0).formats(UnitFormats.FREQUENCY)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL).tooltip(com.vin.easyknack.i18n.AppText.literal("Used only in Fixed mode. Scale mode uses Root and Scale instead."));
        n.addEditor(TUNING,"A4 tuning",Editor.Slider.button(440,400.0,480.0).formats(UnitFormats.FREQUENCY)).inspectorOnly();
        n.addInput(HYSTERESIS,"Hysteresis",Editor.Slider.button(.1,0.0,12.0).formats(HYSTERESIS_UNITS)).signal(SignalCategory.VALUE).rates(PortRates.AUDIO_CONTROL)
            .tooltip(com.vin.easyknack.i18n.AppText.literal("Extra distance beyond the midpoint before changing note. Default: 0.1 semitone (10 cent). Zero selects the nearest note immediately. Only affects Scale mode."));
    }
    public NativeProcessor processor(){return new NativeProcessor("scalequantizer","nodes/ScaleQuantizerNode.hpp",ParameterContract.builder().source(IN).value(IN).source(TARGET).value(TARGET).value(MODE).value(ROOT).value(SCALE).value(TUNING).source(HYSTERESIS).value(HYSTERESIS).build());}
    public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
