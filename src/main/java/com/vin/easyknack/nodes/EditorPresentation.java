package com.vin.easyknack.nodes;

import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.*;

/** Refresh metadata without replacing a focused editor or changing its model type. */
public final class EditorPresentation {
    private EditorPresentation() { }
    @SuppressWarnings("unchecked")
    public static void update(NodeBuilder.Row row, GLContainer component) {
        if(component instanceof GLControl control)control.setToolTipText(NodeHints.tooltip(row));
        if(component instanceof GLUnitSlider<?> raw && row.editor() instanceof UnitSlider spec){
            var slider=(GLUnitSlider<UnitFormat>)raw;slider.setText(row.label());
            slider.setStoredMetadata(spec.spec().value(),spec.spec().min(),spec.spec().max(),spec.spec().step());
            slider.setDisplayUnit(spec.selected());
        }
        if(component instanceof GLSlider slider && row.editor() instanceof Editor.Slider spec) {
            slider.setText(row.label());
            slider.setPresentation(spec.bar()?GLSlider.Presentation.BAR:GLSlider.Presentation.BUTTONS);
            if(slider.getMode()==GLSlider.Mode.FREE)slider.setValueBounds(spec.min(),spec.max());
            else if(spec.min()!=null&&spec.max()!=null)slider.setRange(spec.min(),spec.max());
            slider.setDefaultValue(spec.value());slider.setStep(spec.step());slider.setDragSensitivity(spec.step());
            slider.setUnit(spec.unitSuffix());slider.setIntegerValuesOnly(spec.integral());
        } else if(component instanceof GLLabel label && row.editor()==null)label.setText(row.label());
        else if(component instanceof GLCheckBox check)check.setText(row.label());
        if(component instanceof GLSlider slider)slider.setName(row.label()+(slider.getUnit().isBlank()?"":" ["+slider.getUnit()+"]"));
    }
}
