package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.NodeState;
import com.vingame.glswing.node.model.GLNodeValue;
import java.util.Map;
/** Document policy or an explicit per-driver override. Times are milliseconds. */
public record ControlInterpolationSettings(boolean enabled,double minimumMs,double maximumMs){
 public static final String KEY="easyknack.document.controlInterpolation";
 public static final ControlInterpolationSettings DEFAULT=new ControlInterpolationSettings(true,5,50);
 public ControlInterpolationSettings{if(!Double.isFinite(minimumMs)||!Double.isFinite(maximumMs)||minimumMs<0||maximumMs<minimumMs||maximumMs>60000)throw new IllegalArgumentException("Invalid MIDI interpolation bounds");}
 public GLNodeValue value(){return GLNodeValue.of(Map.of("enabled",enabled,"minimumMs",minimumMs,"maximumMs",maximumMs));}
 public static ControlInterpolationSettings read(GLNodeValue value){if(value==null)return DEFAULT;if(!(value instanceof GLNodeValue.MapValue m))throw new IllegalArgumentException("Invalid MIDI interpolation settings");var s=new NodeState(m.values());return new ControlInterpolationSettings(s.flag("enabled",true),s.number("minimumMs",5),s.number("maximumMs",50));}
}
