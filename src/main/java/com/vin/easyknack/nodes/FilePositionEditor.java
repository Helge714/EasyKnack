package com.vin.easyknack.nodes;

import com.vingame.glswing.ui.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vin.easyknack.audio.AudioFileDecoder;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

/** File-relative display units; graph values remain normalized positions. */
public record FilePositionEditor(double value, double seconds) implements Editor {
    private static final Map<String,Double> durations=new java.util.concurrent.ConcurrentHashMap<>();
    public static FilePositionEditor of(double value,String reference){
        return new FilePositionEditor(value,durations.computeIfAbsent(reference,key->{
            try {var path=AudioFileDecoder.cached(key);if(path!=null)try(var in=Files.newInputStream(path)){
                var bytes=in.readNBytes(24);if(bytes.length==24){var b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);int rate=b.getInt(8);long frames=b.getLong(16);if(rate>0&&frames>0)return frames/(double)rate;}
            }}catch(Exception ignored){}return 1.0;
        }));
    }
    public GLNodeValue defaultValue(){return GLNodeValue.of(value);}
    public void validate(GLNodeValue value){Editor.Slider.bar(this.value,0,1).validate(value);}
    public String viewKey(){return getClass().getName()+":"+seconds;}
    public record Unit(String id,String displayName,String suffix,double scale) implements GLUnit {
        public Object dimension(){return "file-position";}
        public double toBase(double value){return value/scale;}
        public double fromBase(double value){return value*scale;}
    }
    public GLUnitFormats<Unit> formats(){
        var fraction=new Unit("fraction",UnitFormat.FACTOR.displayName(),"",1);
        return new GLUnitFormats<>(fraction,fraction,List.of(fraction,
            new Unit("s",UnitFormat.SECOND.displayName(),"s",seconds),
            new Unit("ms",UnitFormat.MS.displayName(),"ms",seconds*1000),
            new Unit("smp",UnitFormat.SAMPLE.displayName(),"smp",seconds*48000)));
    }
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var formats=formats();var slider=new GLUnitSlider<>(label,value,0.0,1.0,.001,false,GLSlider.Presentation.BAR,formats,formats.initial());
        slider.setStoredValueListener(v->changed.accept(GLNodeValue.of(v)));
        return new Control(slider,v->slider.setStoredValue(((Number)v.toJavaValue()).doubleValue()));
    }
}
