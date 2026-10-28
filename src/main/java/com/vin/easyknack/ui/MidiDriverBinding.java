package com.vin.easyknack.ui;

import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLDriverMapping;
import com.vingame.glswing.ui.GLDriverTarget;
import java.util.*;

/** Saved assignment, separate from runtime pickup/edge detection state. */
public record MidiDriverBinding(String device,String name,int channel,int number,boolean cc,
        GLDriverMapping mapping,boolean hold,int threshold,ControlInterpolationSettings interpolation){
    public MidiDriverBinding(String device,String name,int channel,int number,boolean cc,GLDriverMapping mapping,boolean hold,int threshold){this(device,name,channel,number,cc,mapping,hold,threshold,null);}
    public MidiDriverBinding {
        Long.parseUnsignedLong(device);Objects.requireNonNull(name);Objects.requireNonNull(mapping);
        if(channel<0||channel>15||number<0||number>127||threshold<1||threshold>127)throw new IllegalArgumentException("Invalid MIDI assignment");
    }
    public static String key(String property){return GLDriverTarget.PROPERTY_PREFIX+HexFormat.of().formatHex(property.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public static String property(String key){return new String(HexFormat.of().parseHex(key.substring(GLDriverTarget.PROPERTY_PREFIX.length())),java.nio.charset.StandardCharsets.UTF_8);}
    public GLNodeValue value(){return GLNodeValue.of(Map.ofEntries(Map.entry("device",device),Map.entry("name",name),Map.entry("channel",channel),Map.entry("number",number),Map.entry("cc",cc),
        Map.entry("minimum",mapping.minimum()),Map.entry("maximum",mapping.maximum()),Map.entry("reverse",mapping.reversed()),Map.entry("log",mapping.logarithmic()),Map.entry("pickup",mapping.pickup()),Map.entry("hold",hold),Map.entry("threshold",threshold),Map.entry("interpolation",interpolation==null?"inherit":interpolation.enabled()?"linear":"off"),Map.entry("interpolationMinMs",interpolation==null?5.:interpolation.minimumMs()),Map.entry("interpolationMaxMs",interpolation==null?50.:interpolation.maximumMs())));}
    public static MidiDriverBinding read(GLNodeValue value){
        if(!(value instanceof GLNodeValue.MapValue map))throw new IllegalArgumentException("Invalid MIDI assignment");
        var s=new com.vin.easyknack.nodes.NodeState(map.values());
        if(!Set.of("inherit","linear","off").contains(s.text("interpolation","inherit")))throw new IllegalArgumentException("Invalid interpolation mode");
        return new MidiDriverBinding(s.text("device","0"),s.text("name","MIDI"),(int)s.number("channel",0),(int)s.number("number",0),s.flag("cc",true),
            new GLDriverMapping(s.number("minimum",0),s.number("maximum",1),s.flag("reverse",false),s.flag("log",false),s.flag("pickup",false)),s.flag("hold",false),(int)s.number("threshold",64),s.text("interpolation","inherit").equals("inherit")?null:new ControlInterpolationSettings(s.text("interpolation","inherit").equals("linear"),s.number("interpolationMinMs",5),s.number("interpolationMaxMs",50)));
    }
    public String description(){return name+" · "+AppText.text("driver.channel")+" "+(channel+1)+" · "+(cc?"CC ":"Note ")+number;}
}
