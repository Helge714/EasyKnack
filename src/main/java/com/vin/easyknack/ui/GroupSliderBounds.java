package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.GLNodeValue;

/** The edited value wins; dependent limits and the default follow it. */
final class GroupSliderBounds {
    static Map<String,GLNodeValue> adjust(Map<String,GLNodeValue> source,String key,Double value){
        var result=new HashMap<>(source);var s=new NodeState(source);
        boolean integer=s.text("numericType","float").equals("integer");
        if(value!=null){if(!Double.isFinite(value))throw new IllegalArgumentException("Finite value required");if(integer)value=Math.rint(value);}
        Double min=s.flag("minimumEnabled",true)?s.number("minimum",0):null;
        Double max=s.flag("maximumEnabled",true)?s.number("maximum",1):null;
        double def=s.number("default",0),span=min!=null&&max!=null?max-min:1;
        span=Math.max(integer?1:.01,span);
        switch(key){
            case "minimum"->{min=value;if(min!=null&&max!=null&&min>=max)max=Math.max(Math.nextUp(min),min+span);}
            case "maximum"->{max=value;if(min!=null&&max!=null&&max<=min)min=Math.min(Math.nextDown(max),max-span);}
            case "default"->{def=Objects.requireNonNull(value);if(min!=null&&def<min)min=def;if(max!=null&&def>max)max=def;}
            default->throw new IllegalArgumentException(key);
        }
        if(min!=null)def=Math.max(def,min);if(max!=null)def=Math.min(def,max);
        new Editor.Slider(def,min,max,s.number("step",.01),integer);
        result.put("minimumEnabled",GLNodeValue.of(min!=null));result.put("maximumEnabled",GLNodeValue.of(max!=null));
        result.put("minimum",GLNodeValue.of(min==null?0:min));result.put("maximum",GLNodeValue.of(max==null?1:max));result.put("default",GLNodeValue.of(def));
        return result;
    }
}
