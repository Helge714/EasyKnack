package com.vin.easyknack.nodes;

import java.util.List;
import java.util.function.ToIntFunction;
import com.vingame.glswing.ui.GLFunctionCurve;

@RegisterNode(id="easyknack.audio.curve",title="Curve Mapping",category=NodeCategory.MATH, kind=NodeKind.VALUE)
public final class CurveNode extends BasicNode {
    @Override public boolean supportsSpectralBins() { return true; }
    @Override public boolean supportsSampleFeedback() { return true; }
    @Override public com.vingame.glswing.node.GLNodeResizeMode resizeMode(){return com.vingame.glswing.node.GLNodeResizeMode.BOTH;}
    public static final PropertyId CURVE=new PropertyId("curve"), IN=new PropertyId("audio-in"), OUT=new PropertyId("audio-out"),
            FROM_MIN=new PropertyId("from-min"),FROM_MAX=new PropertyId("from-max"),TO_MIN=new PropertyId("to-min"),TO_MAX=new PropertyId("to-max"),
            OUTSIDE=new PropertyId("outside"), RATE=new PropertyId("rate");
    public enum Outside { CLAMP, EXTEND;
        public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
        @Override public String toString(){return NodeText.text("curve.outside."+id(),this==CLAMP?"Clamp":"Linear extension");}
    }
    @Override public void define(NodeBuilder n,NodeState s){
        n.addOutput(OUT,"Out").rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(CURVE,"Curve",new CurveEditor(GLFunctionCurve.identity())).keepVisibleOnHide().growVertically();
        n.addEditor(OUTSIDE,"Outside range",Editor.comboBox(List.of(Outside.values()),Outside.CLAMP,Outside::id));
        ProcessingRate.addEditor(n,RATE);
        n.addInput(IN,"Value",Editor.Slider.button(0,-1000000.0,1000000.0)).rates(PortRates.AUDIO_CONTROL).signal(SignalCategory.VALUE);
        n.addEditor(FROM_MIN,"Input min",Editor.Slider.button(0,-1000000.0,1000000.0));
        n.addEditor(FROM_MAX,"Input max",Editor.Slider.button(1,-1000000.0,1000000.0));
        n.addEditor(TO_MIN,"Output min",Editor.Slider.button(0,-1000000.0,1000000.0));
        n.addEditor(TO_MAX,"Output max",Editor.Slider.button(1,-1000000.0,1000000.0));
    }
    @Override public NativeProcessor processor() { return new NativeProcessor("curve", "nodes/CurveNode.hpp",
            ParameterContract.builder().source(IN).value(IN).value(FROM_MIN).value(FROM_MAX).value(TO_MIN).value(TO_MAX).mapped(OUTSIDE,s -> s.text(OUTSIDE,"clamp").equals("extend")?"1":"0").curve(CURVE).build()); }
    @Override public void updateEditor(PropertyId id,com.vingame.glswing.component.GLContainer control,NodeState state){
        if(control instanceof com.vingame.glswing.ui.GLCurveEditor curve){
            double a=state.number(FROM_MIN,0),b=state.number(FROM_MAX,1),c=state.number(TO_MIN,0),d=state.number(TO_MAX,1);
            curve.setAxes(a==b?Double.NaN:-a/(b-a),c==d?Double.NaN:-c/(d-c));
        }
    }
    @Override public void mute(MuteRouting r,NodeState s){r.connect(IN,OUT);}
    @Override public void insert(InsertionRouting r,NodeState s){r.connect(IN,OUT);}
}
