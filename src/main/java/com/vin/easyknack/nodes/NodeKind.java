package com.vin.easyknack.nodes;
/** Semantic title palette, independent of the Add menu category. */
public enum NodeKind { AUDIO, VALUE, MIDI, WAVETABLE, ANALYSIS, VECTOR;
    @Override public String toString(){return NodeText.text("kind."+name(),name().charAt(0)+name().substring(1).toLowerCase(java.util.Locale.ROOT));}
}
