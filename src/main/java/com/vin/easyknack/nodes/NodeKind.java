package com.vin.easyknack.nodes;
/** Semantic title palette, independent of the Add menu category. */
public enum NodeKind { AUDIO, VALUE, MIDI, WAVETABLE, ANALYSIS, VECTOR, TRIGGER, HISTORY, SPECTRUM, PARTIALS;
    public SignalCategory signal(){return SignalCategory.valueOf(name());}
    public static NodeKind fromPort(String valueType){return java.util.Arrays.stream(SignalCategory.values()).filter(s->s.valueTypeId().equals(valueType)).map(s->NodeKind.valueOf(s.name())).findFirst().orElse(VALUE);}
    @Override public String toString(){return NodeText.text("kind."+name(),name().charAt(0)+name().substring(1).toLowerCase(java.util.Locale.ROOT));}
}
