package com.vin.easyknack.nodes;

/** Semantic colors, independent of signal rate and the port's compatibility type. */
public enum SignalCategory {
    AUDIO, VALUE, TRIGGER, WAVETABLE, MIDI, HISTORY, ANALYSIS, SPECTRUM, VECTOR, PARTIALS;
    public String valueTypeId() { return "easyknack." + name().toLowerCase(java.util.Locale.ROOT) + ".signal"; }
}

