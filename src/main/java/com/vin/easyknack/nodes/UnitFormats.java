package com.vin.easyknack.nodes;
import java.util.List;
/** Reusable format sets can be attached to any numeric row with Row.formats(...). */
public record UnitFormats(UnitFormat stored,UnitFormat initial,List<UnitFormat> choices) {
    public static final UnitFormats LENGTH=new UnitFormats(UnitFormat.METRE,UnitFormat.METRE,List.of(UnitFormat.METRE,UnitFormat.KILOMETRE,UnitFormat.CENTIMETRE,UnitFormat.MILLIMETRE));
    public static final UnitFormats GAIN_DB=new UnitFormats(UnitFormat.DB,UnitFormat.FACTOR,List.of(UnitFormat.FACTOR,UnitFormat.PERCENT,UnitFormat.DB));
    public static final UnitFormats PITCH_CENT=pitch(UnitFormat.CENT),PITCH_SEMITONE=pitch(UnitFormat.SEMITONE);
    public static final UnitFormats FREQUENCY=new UnitFormats(UnitFormat.HZ,UnitFormat.HZ,List.of(UnitFormat.HZ,UnitFormat.KHZ));
    public static final UnitFormats CLOCK=new UnitFormats(UnitFormat.HZ,UnitFormat.HZ,List.of(UnitFormat.HZ,UnitFormat.BPM));
    public static final UnitFormats TIME_MS=new UnitFormats(UnitFormat.MS,UnitFormat.MS,List.of(UnitFormat.MS,UnitFormat.SECOND,UnitFormat.SAMPLE));
    public static final UnitFormats TIME_SECONDS=new UnitFormats(UnitFormat.SECOND,UnitFormat.SECOND,List.of(UnitFormat.SECOND,UnitFormat.MS,UnitFormat.SAMPLE));
    public static final UnitFormats PHASE=new UnitFormats(UnitFormat.DEGREE,UnitFormat.TURN,List.of(UnitFormat.TURN,UnitFormat.DEGREE,UnitFormat.RADIAN));
    public static final UnitFormats PROPORTION=new UnitFormats(UnitFormat.FACTOR,UnitFormat.FACTOR,List.of(UnitFormat.FACTOR,UnitFormat.PERCENT));
    private static UnitFormats pitch(UnitFormat stored){return new UnitFormats(stored,stored,List.of(UnitFormat.CENT,UnitFormat.SEMITONE,UnitFormat.OCTAVE,UnitFormat.FACTOR));}
    public UnitFormats { choices=List.copyOf(choices);new com.vingame.glswing.ui.GLUnitFormats<>(stored,initial,choices); }
    public com.vingame.glswing.ui.GLUnitFormats<UnitFormat> core() { return new com.vingame.glswing.ui.GLUnitFormats<>(stored,initial,choices); }
    public static UnitFormats fixed(UnitFormat unit) { return new UnitFormats(unit,unit,List.of(unit)); }
}
