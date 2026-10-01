package com.vin.easyknack.nodes;

/** Data contract, independently of display color and processing rate. */
public enum PortDataType {
    SIGNAL(BasicNode.AUDIO_SIGNAL), TRIGGER("easyknack.trigger.signal"), WAVEFORM("easyknack.wavetable.signal"), MIDI("easyknack.midi.signal"), HISTORY("easyknack.history.signal"), ANALYSIS("easyknack.analysis.signal"), SPECTRUM("easyknack.spectrum.signal");
    private final String id;
    PortDataType(String id) { this.id = id; }
    public String id() { return id; }
}

