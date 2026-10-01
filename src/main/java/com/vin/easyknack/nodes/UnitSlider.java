package com.vin.easyknack.nodes;

import com.vingame.glswing.ui.GLSlider;
import com.vingame.glswing.ui.GLUnitSlider;
import com.vingame.glswing.node.model.GLNodeValue;
import java.util.function.Consumer;

/** Node-model adapter; conversion, bounds, menu and gesture handling belong to the Core. */
public record UnitSlider(Editor.Slider spec,UnitFormats formats,UnitFormat selected) implements Editor {
    public GLNodeValue defaultValue(){return spec.defaultValue();}
    public void validate(GLNodeValue value){spec.validate(value);}
    public String viewKey(){return spec.viewKey()+":"+selected.id();}
    public String structuralKey(){return "unit:"+spec.structuralKey();}
    public double display(double value){return formats.stored().convert(value,selected);}
    public double stored(double value){return selected.convert(value,formats.stored());}
    @Override public Control create(String label,Consumer<GLNodeValue> changed){
        var slider=new GLUnitSlider<>(label,spec.value(),spec.min(),spec.max(),spec.step(),spec.integral(),
            spec.bar()?GLSlider.Presentation.BAR:GLSlider.Presentation.BUTTONS,formats.core(),selected);
        slider.setStoredValueListener(value->changed.accept(GLNodeValue.of(value)));
        slider.setToolTipText(com.vin.easyknack.ui.AppText.literal("Input unit: ")+selected+". Changing units reinterprets connected values.");
        return new Control(slider,value->slider.setStoredValue(((Number)value.toJavaValue()).doubleValue()));
    }
    @SuppressWarnings("unchecked")
    public void bindUnitMenu(GLSlider slider,Consumer<UnitFormat> changed){
        ((GLUnitSlider<UnitFormat>)slider).setUnitSelectionListener(changed);
    }
    public static void bindHistory(GLSlider slider,java.util.function.Supplier<com.vingame.glswing.history.GLHistoryManager> history){
        if(slider instanceof GLUnitSlider<?> unitSlider)unitSlider.setGestureHistory(history);
    }
}
