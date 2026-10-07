package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;

/** Positions are stored in metres; unit selection is presentation-only for this compound editor. */
public record VectorEditor(double x, double y, double z) implements Editor {
    public GLNodeValue defaultValue(){return value(x,y,z,UnitFormat.METRE);}
    public static GLNodeValue value(double x,double y,double z,UnitFormat unit){return GLNodeValue.of(List.of(x,y,z,unit.id()));}
    public static double component(NodeState state,PropertyId id,int axis,double fallback){
        var value=state.values().get(id.value());return value==null?fallback:((Number)((List<?>)value.toJavaValue()).get(axis)).doubleValue();
    }
    public void validate(GLNodeValue value){
        if(!(value.toJavaValue() instanceof List<?> a)||a.size()!=4)throw new IllegalArgumentException("Expected XYZ vector");
        for(int i=0;i<3;i++)if(!(a.get(i) instanceof Number n)||!Double.isFinite(n.doubleValue()))throw new IllegalArgumentException("Non-finite vector component");
        if(!UnitFormats.LENGTH.choices().contains(UnitFormat.fromId((String)a.get(3))))throw new IllegalArgumentException("Invalid position unit");
    }
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        var sliders=new ArrayList<GLUnitSlider<UnitFormat>>();
        boolean[] updating={false};
        Runnable publish=()->{if(!updating[0])changed.accept(value(sliders.get(0).getStoredValue(),sliders.get(1).getStoredValue(),sliders.get(2).getStoredValue(),sliders.get(0).getDisplayUnit()));};
        double[] initial={x,y,z};
        for(int i=0;i<3;i++){
            var slider=new GLUnitSlider<UnitFormat>(label+" "+"XYZ".charAt(i),initial[i],null,null,.01,false,GLSlider.Presentation.BUTTONS,UnitFormats.LENGTH.core(),UnitFormat.METRE);
            sliders.add(slider);slider.setStoredValueListener(v->publish.run());
            slider.setUnitSelectionListener(unit->{if(updating[0])return;updating[0]=true;try{sliders.forEach(s->s.setDisplayUnit(unit));}finally{updating[0]=false;}publish.run();});
        }
        var group=new GLSliderGroup(sliders.toArray(GLSlider[]::new));
        return new Control(group,v->{validate(v);var a=(List<?>)v.toJavaValue();updating[0]=true;try{for(int i=0;i<3;i++){sliders.get(i).setDisplayUnit(UnitFormat.fromId((String)a.get(3)));sliders.get(i).setStoredValue(((Number)a.get(i)).doubleValue());}}finally{updating[0]=false;}});
    }
}
