package com.vin.easyknack.nodes;

/** Data contract, independently of display color and processing rate. */
public enum PortDataType {
    SIGNAL(BasicNode.AUDIO_SIGNAL), VECTOR("easyknack.vector.signal"), TRIGGER("easyknack.trigger.signal"), WAVEFORM("easyknack.wavetable.signal"), MIDI("easyknack.midi.signal"), HISTORY("easyknack.history.signal"), ANALYSIS("easyknack.analysis.signal"), SPECTRUM("easyknack.spectrum.signal"), PARTIALS("easyknack.partials.signal");
    private final String id;
    PortDataType(String id) { this.id = id; }
    public String id() { return id; }
    /** Borrowed resource handles, drawn as reference cables rather than numeric values. */
    public boolean isReference(){return switch(this){case HISTORY,ANALYSIS,SPECTRUM,WAVEFORM,PARTIALS->true;default->false;};}
    public static boolean isReference(String valueType){return java.util.Arrays.stream(values()).anyMatch(t->t.id.equals(valueType)&&t.isReference());}
}

