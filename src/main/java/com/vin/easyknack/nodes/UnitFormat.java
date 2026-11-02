package com.vin.easyknack.nodes;

import java.util.Arrays;
import java.util.List;

/** Signal units. Ratio is the common base for gain and pitch; other dimensions stay separate. */
public enum UnitFormat implements com.vingame.glswing.ui.GLUnit {
    FACTOR("ratio","Factor","×"), PERCENT("ratio","Percent","%"), DB("ratio","Decibels","dB"),
    CENT("ratio","Cent","cent"), SEMITONE("ratio","Semitones","st"), OCTAVE("ratio","Octaves","oct"),
    HZ("frequency","Hertz","Hz"), KHZ("frequency","Kilohertz","kHz"), BPM("frequency","Beats per minute","BPM"),
    SECOND("time","Seconds","s"), MS("time","Milliseconds","ms"), SAMPLE("time","Samples","smp"),
    BIN("spectrum-bin","Bins","bins"),
    TURN("angle","Turns","turn"), DEGREE("angle","Degrees","°"), RADIAN("angle","Radians","rad"), Q("quality","Quality factor","Q"), METRE("length","Metres","m"), KILOMETRE("length","Kilometres","km"), CENTIMETRE("length","Centimetres","cm"), MILLIMETRE("length","Millimetres","mm");
    private final String dimension,label,suffix;
    UnitFormat(String dimension,String label,String suffix) { this.dimension=dimension;this.label=label;this.suffix=suffix; }
    public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
    public String suffix() { return suffix; }
    public Object dimension() { return dimension; }
    public String displayName() { return toString(); }
    public double preferredStep() { return switch(this){case FACTOR,OCTAVE,TURN,SECOND,KHZ->.001;case CENT,DEGREE,SAMPLE,BIN->1;default->.01;}; }
    public boolean compatible(UnitFormat other) { return isCompatible(other); }
    public List<UnitFormat> compatible() { return Arrays.stream(values()).filter(this::compatible).toList(); }
    public static UnitFormat fromId(String id) { return Arrays.stream(values()).filter(u->u.id().equals(id)).findFirst().orElseThrow(); }
    @Override public String toString() { return NodeText.text("unit."+id(),label); }
    public double toBase(double value) {
        return switch(this) {
            case FACTOR,HZ,SECOND,TURN,Q,METRE,BIN -> value;
            case PERCENT -> value/100;
            case DB -> value<=-160?0:Math.pow(10,value/20);
            case CENT -> Math.pow(2,value/1200);
            case SEMITONE -> Math.pow(2,value/12);
            case OCTAVE -> Math.pow(2,value);
            case KILOMETRE -> value*1000;
            case CENTIMETRE -> value/100;
            case MILLIMETRE -> value/1000;
            case KHZ -> value*1000;
            case BPM -> value/60;
            case MS -> value/1000;
            case SAMPLE -> value/com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE;
            case DEGREE -> value/360;
            case RADIAN -> value/(2*Math.PI);
        };
    }
    public double fromBase(double value) {
        return switch(this) {
            case FACTOR,HZ,SECOND,TURN,Q,METRE,BIN -> value;
            case PERCENT -> value*100;
            case DB -> Math.max(-160,20*Math.log10(Math.max(value,1e-8)));
            case CENT -> 1200*Math.log(Math.max(value,1e-8))/Math.log(2);
            case SEMITONE -> 12*Math.log(Math.max(value,1e-8))/Math.log(2);
            case OCTAVE -> Math.log(Math.max(value,1e-8))/Math.log(2);
            case KILOMETRE -> value/1000;
            case CENTIMETRE -> value*100;
            case MILLIMETRE -> value*1000;
            case KHZ -> value/1000;
            case BPM -> value*60;
            case MS -> value*1000;
            case SAMPLE -> value*com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE;
            case DEGREE -> value*360;
            case RADIAN -> value*(2*Math.PI);
        };
    }
    public double convert(double value,UnitFormat target) {
        return convertTo(value,target);
    }
}
