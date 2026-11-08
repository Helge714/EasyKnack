package com.vin.easyknack.nodes;

import com.vingame.glswing.ui.GLSlider;
import com.vingame.glswing.ui.GLUnitSlider;
import com.vingame.glswing.node.model.GLNodeValue;
import java.util.function.Consumer;

/** Node-model adapter; conversion, bounds, menu and gesture handling belong to the Core. */
public record UnitSlider(Editor.Slider spec,UnitFormats formats,UnitFormat selected,java.util.List<UnitFormat> unitChoices) implements Editor {
    public UnitSlider(Editor.Slider spec,UnitFormats formats,UnitFormat selected){this(spec,formats,selected,formats.choices());}
    public UnitSlider{unitChoices=java.util.List.copyOf(unitChoices);}
    public GLNodeValue defaultValue(){return spec.defaultValue();}
    public void validate(GLNodeValue value){spec.validate(value);}
    public String viewKey(){return spec.viewKey()+":"+selected.id();}
    public String structuralKey(){return "unit:"+spec.structuralKey()+":"+formats+":"+unitChoices;}
    public double display(double value){return formats.stored().convert(value,selected);}
    public double stored(double value){return selected.convert(value,formats.stored());}
    @Override public Control create(String label,Consumer<GLNodeValue> changed){
        var slider=new GLUnitSlider<>(label,spec.value(),spec.min(),spec.max(),spec.step(),spec.integral(),
            spec.bar()?GLSlider.Presentation.BAR:GLSlider.Presentation.BUTTONS,formats.core(),selected);
        slider.setStoredValueListener(value->changed.accept(GLNodeValue.of(value)));
        slider.setToolTipText(com.vin.easyknack.i18n.AppText.literal("Input unit: ")+selected+". Changing units reinterprets connected values.");
        return new Control(slider,value->slider.setStoredValue(((Number)value.toJavaValue()).doubleValue()));
    }
    @SuppressWarnings("unchecked")
    public void bindUnitMenu(GLSlider slider,Consumer<UnitFormat> changed){
        ((GLUnitSlider<UnitFormat>)slider).setUnitSelectionListener(changed);
        if(!unitChoices.equals(formats.choices())){
            var menu=slider.getContextMenu();
            // The Core handles conversions only within a compatible family. Selecting
            // another interpretation writes the model and rebuilds its presentation.
            for(var item:menu.getItems())if(item.getSubMenu()!=null&&item.getText().equals("Unit"))menu.removeItem(item);
            var units=new com.vingame.glswing.ui.GLPopupMenu();
            for(var unit:unitChoices)units.addItem(unit.toString(),e->changed.accept(unit)).setEnabled(unit!=selected);
            menu.addSubMenu(com.vin.easyknack.i18n.AppText.text("port.unit"),units);
        }
    }
    public void updateUnitMenu(GLSlider slider){
        if(unitChoices.equals(formats.choices()))return;
        for(var item:slider.getContextMenu().getItems())if(item.getSubMenu()!=null)
            for(var entry:item.getSubMenu().getItems())for(var unit:unitChoices)
                if(entry.getText().equals(unit.toString()))entry.setEnabled(unit!=selected);
    }
    public static void bindHistory(GLSlider slider,java.util.function.Supplier<com.vingame.glswing.history.GLHistoryManager> history){
        if(slider instanceof GLUnitSlider<?> unitSlider)unitSlider.setGestureHistory(history);
    }
}
