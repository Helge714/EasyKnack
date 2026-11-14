package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.ui.GLFunctionCurve;
import com.vingame.glswing.ui.GLFunctionCurve.*;

/** Versioned document representation, independent of native table resolution. */
public final class CurveData {
    private CurveData() { }
    public static final GLFunctionCurve ADSR = new GLFunctionCurve(List.of(
            Point.at(0,0),Point.at(.2,1),Point.at(.5,.65),Point.at(1,0)));
    public static final GLFunctionCurve ADSR_PHASES=envelopePhases(ADSR);
    /** Reparameterize phase shapes to equal thirds without changing their levels. */
    public static GLFunctionCurve envelopePhases(GLFunctionCurve curve){
        var points=curve.points();double third=1.0/3;
        if(points.size()>=4){
            double attack=points.get(1).x(),decay=points.get(points.size()-2).x();
            if(attack==third&&decay==2*third)return curve;
            java.util.function.DoubleUnaryOperator map=x->x<=0?0:x>=1?1:x<=attack?third*x/attack:x<=decay?third+third*(x-attack)/(decay-attack):2*third+third*(x-decay)/(1-decay);
            var shifted=new ArrayList<Point>();
            for(var p:points)shifted.add(new Point(map.applyAsDouble(p.x()),p.y(),map.applyAsDouble(p.inX()),p.inY(),map.applyAsDouble(p.outX()),p.outY(),p.mode(),p.linked()));
            try{return new GLFunctionCurve(shifted);}catch(IllegalArgumentException tooClose){
                return new GLFunctionCurve(List.of(Point.at(0,points.get(0).y()),Point.at(third,points.get(1).y()),Point.at(2*third,points.get(points.size()-2).y()),Point.at(1,points.get(points.size()-1).y())));
            }
        }
        if(points.size()==2)return curve; // Native two-point curves already use equal thirds.
        var a=points.get(0);var b=points.get(1);var end=points.get(2);
        return new GLFunctionCurve(List.of(
            new Point(0,a.y(),0,a.inY(),third*a.outX()/b.x(),a.outY(),a.mode(),false),
            new Point(third,b.y(),third*b.inX()/b.x(),b.inY(),third,b.y(),Mode.LINEAR,false),
            new Point(2*third,b.y(),2*third,b.y(),2*third+third*(b.outX()-b.x())/(1-b.x()),b.outY(),b.mode(),false),
            new Point(1,end.y(),2*third+third*(end.inX()-b.x())/(1-b.x()),end.inY(),1,end.outY(),end.mode(),false)));

    }
    public static String encode(GLFunctionCurve curve) {
        var text=new StringBuilder("v1 ").append(curve.points().size());
        for(var p:curve.points()) text.append(' ').append(p.x()).append(' ').append(p.y())
                .append(' ').append(p.inX()).append(' ').append(p.inY()).append(' ').append(p.outX()).append(' ').append(p.outY())
                .append(' ').append(p.mode().ordinal()).append(' ').append(p.linked()?1:0);
        return text.toString();
    }
    public static GLFunctionCurve decode(String text) {
        if(text.length()>16000)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Curve too large"));
        String[] tokens=text.trim().split("\\s+");
        try {
            if(tokens.length<2||!tokens[0].equals("v1"))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unsupported curve version"));
            int count=Integer.parseInt(tokens[1]);
            if(count<2||count>64||tokens.length!=2+count*8)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid curve size"));
            var points=new ArrayList<Point>();
            for(int i=0;i<count;i++){
                int n=2+i*8,mode=Integer.parseInt(tokens[n+6]),linked=Integer.parseInt(tokens[n+7]);
                if(mode<0||mode>=Mode.values().length||linked<0||linked>1)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid curve mode"));
                points.add(new Point(Double.parseDouble(tokens[n]),Double.parseDouble(tokens[n+1]),Double.parseDouble(tokens[n+2]),
                        Double.parseDouble(tokens[n+3]),Double.parseDouble(tokens[n+4]),Double.parseDouble(tokens[n+5]),Mode.values()[mode],linked==1));
            }
            return new GLFunctionCurve(points);
        } catch(NumberFormatException error){throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid curve data"),error);}
    }
    public static String nativePayload(String text) {
        var curve=decode(text);var result=new StringBuilder().append(curve.points().size());
        for(var p:curve.points()) result.append(' ').append(p.x()).append(' ').append(p.y())
                .append(' ').append(p.inX()).append(' ').append(p.inY()).append(' ').append(p.outX()).append(' ').append(p.outY()).append(' ').append(p.mode().ordinal());
        return result.toString();
    }
}
