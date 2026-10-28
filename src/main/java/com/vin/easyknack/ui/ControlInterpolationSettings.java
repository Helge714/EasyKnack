package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.NodeState;
import com.vingame.glswing.node.model.GLNodeValue;
import java.util.Map;

/** Document defaults or a driver override. All timing values are milliseconds. */
public record ControlInterpolationSettings(boolean enabled, double minimumMs, double maximumMs,
        boolean buffered, double bufferMs, boolean smoothing, double smoothingMs) {
    public static final String KEY="easyknack.document.controlInterpolation";
    public static final ControlInterpolationSettings DEFAULT=
            new ControlInterpolationSettings(true,5,50,true,50,true,5);
    /** Explicit legacy adaptive policy; keeps callers choosing three arguments source-compatible. */
    public ControlInterpolationSettings(boolean enabled,double minimumMs,double maximumMs){
        this(enabled,minimumMs,maximumMs,false,50,false,5);
    }
    public ControlInterpolationSettings {
        if(!Double.isFinite(minimumMs)||!Double.isFinite(maximumMs)||minimumMs<0||maximumMs<minimumMs||maximumMs>60000
                ||!Double.isFinite(bufferMs)||bufferMs<0||bufferMs>1000
                ||!Double.isFinite(smoothingMs)||smoothingMs<0||smoothingMs>100)
            throw new IllegalArgumentException("Invalid control interpolation timing");
    }
    public GLNodeValue value(){
        return GLNodeValue.of(Map.of("enabled",enabled,"minimumMs",minimumMs,"maximumMs",maximumMs,
                "buffered",buffered,"bufferMs",bufferMs,"smoothing",smoothing,"smoothingMs",smoothingMs));
    }
    public static ControlInterpolationSettings read(GLNodeValue value){
        if(value==null)return DEFAULT;
        if(!(value instanceof GLNodeValue.MapValue map))throw new IllegalArgumentException("Invalid control interpolation settings");
        var s=new NodeState(map.values());
        return new ControlInterpolationSettings(s.flag("enabled",true),s.number("minimumMs",5),s.number("maximumMs",50),
                s.flag("buffered",true),s.number("bufferMs",50),s.flag("smoothing",true),s.number("smoothingMs",5));
    }
}
