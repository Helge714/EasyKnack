package com.vin.easyknack.nodes;

import java.util.*;
import java.util.function.Consumer;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;

/** Periodic curve editor. Coordinates are normalized; audio amplitude is 2*y-1. */
public final class WaveformEditor implements Editor {
    public static GLFunctionCurve sine(){
        double k=Math.PI/12;
        return new GLFunctionCurve(List.of(
            new GLFunctionCurve.Point(0,.5,0,.5,1./12,.5+k,GLFunctionCurve.Mode.BEZIER,true),
            new GLFunctionCurve.Point(.25,1,1./6,1,1./3,1,GLFunctionCurve.Mode.BEZIER,true),
            new GLFunctionCurve.Point(.5,.5,5./12,.5+k,7./12,.5-k,GLFunctionCurve.Mode.BEZIER,true),
            new GLFunctionCurve.Point(.75,0,2./3,0,5./6,0,GLFunctionCurve.Mode.BEZIER,true),
            new GLFunctionCurve.Point(1,.5,11./12,.5-k,1,.5,GLFunctionCurve.Mode.BEZIER,true)));
    }
    public static GLFunctionCurve periodic(GLFunctionCurve value){
        var points=new ArrayList<>(value.points());var first=points.get(0);var end=points.get(points.size()-1);
        double delta=first.y()-end.y();points.set(points.size()-1,new GLFunctionCurve.Point(1,first.y(),end.inX(),Math.max(0,Math.min(1,end.inY()+delta)),1,first.y(),end.mode(),end.linked()));
        return new GLFunctionCurve(points);
    }
    public static void copyCapture(float[] samples){
        WaveformSamples.copy(samples);
    }

    public GLNodeValue defaultValue(){return GLNodeValue.of(CurveData.encode(sine()));}
    public void validate(GLNodeValue value){WaveformSamples.fromValue((String)value.toJavaValue());}
    public Control create(String label,Consumer<GLNodeValue> changed){
        var editor=new WavetableCanvas();editor.onValue(v->changed.accept(GLNodeValue.of(v)));editor.setAxes(Double.NaN,.5);editor.setCurve(sine());editor.setResetCurve(sine());
        var previous=new GLFunctionCurve[]{sine()};
        Consumer<GLFunctionCurve> apply=curve->{
            if(curve.points().get(0).y()==previous[0].points().get(0).y()&&curve.points().get(curve.points().size()-1).y()!=previous[0].points().get(previous[0].points().size()-1).y())curve=curve.move(0,0,curve.points().get(curve.points().size()-1).y());
            var periodic=periodic(curve);previous[0]=periodic;editor.value(CurveData.encode(periodic));changed.accept(GLNodeValue.of(CurveData.encode(periodic)));};
        editor.setChangeListener(apply);
        editor.setToolTipText(NodeText.text("waveform.help","One cycle: phase 0–1, amplitude −1–1. Endpoints are linked. Right-click for shapes, sample mode or resource clipboard."));
        editor.setContextMenuCustomizer(menu->{
        menu.addItem(NodeText.text("waveform.sine","Sine"),e->apply.accept(sine()));
        menu.addItem(NodeText.text("waveform.triangle","Triangle"),e->apply.accept(new GLFunctionCurve(List.of(GLFunctionCurve.Point.at(0,.5),GLFunctionCurve.Point.at(.25,1),GLFunctionCurve.Point.at(.75,0),GLFunctionCurve.Point.at(1,.5)))));
        menu.addItem(NodeText.text("waveform.saw","Sawtooth"),e->apply.accept(new GLFunctionCurve(List.of(GLFunctionCurve.Point.at(0,0),GLFunctionCurve.Point.at(.9998,1),GLFunctionCurve.Point.at(1,0)))));
        menu.addItem(NodeText.text("waveform.square","Square"),e->apply.accept(new GLFunctionCurve(List.of(new GLFunctionCurve.Point(0,1,0,1,0,1,GLFunctionCurve.Mode.STEP,false),new GLFunctionCurve.Point(.5,0,.5,0,.5,0,GLFunctionCurve.Mode.STEP,false),GLFunctionCurve.Point.at(1,1)))));
        editor.actions(menu);
        });
        return new Control(editor,value->{String text=(String)value.toJavaValue();editor.value(text);if(!editor.resourceMode())previous[0]=editor.getCurve();});
    }
}
