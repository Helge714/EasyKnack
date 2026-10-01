package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.ui.GLFunctionCurve;
import com.vingame.glswing.ui.GLFunctionCurve.*;

/** Versioned document representation, independent of native table resolution. */
public final class CurveData {
    private CurveData() { }
    public static final GLFunctionCurve ADSR = new GLFunctionCurve(List.of(
            Point.at(0,0),Point.at(.2,1),Point.at(.5,.65),Point.at(1,0)));
    public static String encode(GLFunctionCurve curve) {
        var text=new StringBuilder("v1 ").append(curve.points().size());
        for(var p:curve.points()) text.append(' ').append(p.x()).append(' ').append(p.y())
                .append(' ').append(p.inX()).append(' ').append(p.inY()).append(' ').append(p.outX()).append(' ').append(p.outY())
                .append(' ').append(p.mode().ordinal()).append(' ').append(p.linked()?1:0);
        return text.toString();
    }
    public static GLFunctionCurve decode(String text) {
        if(text.length()>16000)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Curve too large"));
        String[] tokens=text.trim().split("\\s+");
        try {
            if(tokens.length<2||!tokens[0].equals("v1"))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unsupported curve version"));
            int count=Integer.parseInt(tokens[1]);
            if(count<2||count>64||tokens.length!=2+count*8)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid curve size"));
            var points=new ArrayList<Point>();
            for(int i=0;i<count;i++){
                int n=2+i*8,mode=Integer.parseInt(tokens[n+6]),linked=Integer.parseInt(tokens[n+7]);
                if(mode<0||mode>=Mode.values().length||linked<0||linked>1)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid curve mode"));
                points.add(new Point(Double.parseDouble(tokens[n]),Double.parseDouble(tokens[n+1]),Double.parseDouble(tokens[n+2]),
                        Double.parseDouble(tokens[n+3]),Double.parseDouble(tokens[n+4]),Double.parseDouble(tokens[n+5]),Mode.values()[mode],linked==1));
            }
            return new GLFunctionCurve(points);
        } catch(NumberFormatException error){throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid curve data"),error);}
    }
    public static String nativePayload(String text) {
        var curve=decode(text);var result=new StringBuilder().append(curve.points().size());
        for(var p:curve.points()) result.append(' ').append(p.x()).append(' ').append(p.y())
                .append(' ').append(p.inX()).append(' ').append(p.inY()).append(' ').append(p.outX()).append(' ').append(p.outY()).append(' ').append(p.mode().ordinal());
        return result.toString();
    }
}
