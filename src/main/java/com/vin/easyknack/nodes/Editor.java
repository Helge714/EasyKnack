package com.vin.easyknack.nodes;

import java.util.List;
import java.util.function.Function;
import com.vingame.glswing.node.model.GLNodeValue;

/** Editor descriptions contain no Swing objects and can also produce model defaults. */
public interface Editor {
    /** A momentary action, deliberately outside the property writer. */
    /** Numeric on/off input: portable 0/1 values, independent of boolean parameters. */
    record Toggle(boolean value) implements Editor {
        public GLNodeValue defaultValue(){return GLNodeValue.of(value?1.0:0.0);}
        public void validate(GLNodeValue v){if(!(v.toJavaValue() instanceof Number n)||(n.doubleValue()!=0&&n.doubleValue()!=1))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Checkbox value must be 0 or 1"));}
        public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
            var box=new com.vingame.glswing.ui.GLCheckBox(label);box.setSelected(value);
            box.addActionListener(e->changed.accept(GLNodeValue.of(box.isSelected()?1.0:0.0)));
            return new Control(box,v->box.setSelected(((Number)v.toJavaValue()).doubleValue()!=0));
        }
    }
    record Trigger() implements Editor {
        public GLNodeValue defaultValue(){return GLNodeValue.of(0.0);}
        public Control create(String label,java.util.function.Consumer<GLNodeValue> ignored){
            return new Control(new TriggerButton(label),value->{});
        }
    }
    final class TriggerButton extends com.vingame.glswing.ui.GLButton {
        private String nodeId,propertyId;
        public TriggerButton(String label){super(label);addActionListener(e->{if(nodeId!=null)com.vin.easyknack.audio.ManualTriggers.fire(nodeId,propertyId);});}
        public void bind(String id,String property){nodeId=id;propertyId=property;}
    }
    GLNodeValue defaultValue();
    default void validate(GLNodeValue value) { }
    /** Escape hatch for application-specific controls without changing AudioNodeFactory. */
    default Control create(String label, java.util.function.Consumer<GLNodeValue> changed) {
        if(this instanceof Slider spec){
            if(spec.formats()!=null)return new UnitSlider(spec,spec.formats(),spec.formats().initial()).create(label,changed);
            var slider=spec.min()!=null&&spec.max()!=null?com.vingame.glswing.ui.GLSlider.fixed(label,spec.min(),spec.max(),spec.value()):com.vingame.glswing.ui.GLSlider.free(label,spec.value());
            if(spec.min()==null||spec.max()==null)slider.setValueBounds(spec.min(),spec.max());
            slider.setPresentation(spec.bar()?com.vingame.glswing.ui.GLSlider.Presentation.BAR:com.vingame.glswing.ui.GLSlider.Presentation.BUTTONS);
            slider.setUnit(spec.unitSuffix());slider.setStep(spec.step());slider.setDragSensitivity(spec.step());slider.setIntegerValuesOnly(spec.integral());
            slider.addChangeListener(e->changed.accept(GLNodeValue.of(slider.getValue())));
            return new Control(slider,value->slider.setValue(((Number)value.toJavaValue()).doubleValue()));
        }
        if(this instanceof CheckBox spec){
            var checkbox=new com.vingame.glswing.ui.GLCheckBox(label);checkbox.setSelected(spec.value());
            checkbox.addActionListener(e->changed.accept(GLNodeValue.of(checkbox.isSelected())));
            return new Control(checkbox,value->checkbox.setSelected((Boolean)value.toJavaValue()));
        }
        if(this instanceof ComboBox<?> spec)return comboControl(spec,changed);
        throw new UnsupportedOperationException(com.vin.easyknack.ui.AppText.literal("Editor must provide a control: ") + getClass().getName());
    }
    private static <T> Control comboControl(ComboBox<T> spec,java.util.function.Consumer<GLNodeValue> changed){
        var combo=spec.createCombo();combo.setSelectedItem(spec.value());
        combo.addActionListener(e->changed.accept(GLNodeValue.of(spec.id().apply(combo.getSelectedItem()))));
        return new Control(combo,value->spec.items().stream().filter(item->spec.id().apply(item).equals(value.toJavaValue())).findFirst().ifPresent(combo::setSelectedItem));
    }
    /** Stable presentation key; override when state changes the custom control's structure. */
    default String viewKey() { return getClass().getName(); }
    default String structuralKey() { return viewKey(); }
    record Control(com.vingame.glswing.component.GLContainer component,
            java.util.function.Consumer<GLNodeValue> update) {
        public Control { java.util.Objects.requireNonNull(component); java.util.Objects.requireNonNull(update); }
    }

    record Slider(double value, Double min, Double max, double step, boolean integral, boolean bar, UnitFormats formats) implements Editor {
        public Slider(double value, Double min, Double max, double step, boolean integral, boolean bar) { this(value,min,max,step,integral,bar,null); }
        public Slider(double value, Double min, Double max, double step, boolean integral) { this(value,min,max,step,integral,false); }
        public Slider(double value, Double min, Double max, double step) { this(value, min, max, step, false); }
        public Slider {
            if (integral && Double.isFinite(step) && step > 0) step = Math.max(1, step);
            if (!Double.isFinite(value) || min != null && !Double.isFinite(min)
                    || max != null && !Double.isFinite(max) || min != null && max != null && min >= max
                    || min != null && value < min || max != null && value > max
                    || !Double.isFinite(step) || step <= 0 || integral && value != Math.rint(value))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid slider bounds/default/step"));
        }
        public static Slider button(double value, Double min, Double max) { return new Slider(value, min, max, .01); }
        public static Slider button(double value, double step) { return new Slider(value, null, null, step); }
        public static Slider button(double value) { return new Slider(value, null, null, .01); }
        public Slider step(double step) { return new Slider(value, min, max, step, integral,bar,formats); }
        public Slider integers() { return new Slider(value, min, max, Math.max(1, step), true,bar,formats); }
        public Slider unit(UnitFormat unit) { return formats(UnitFormats.fixed(java.util.Objects.requireNonNull(unit))); }
        public Slider formats(UnitFormats formats) { return new Slider(value,min,max,step,integral,bar,java.util.Objects.requireNonNull(formats)); }
        public String unitSuffix() { return formats==null?"":formats.initial().suffix(); }
        public static Slider bar(double value, double min, double max) { return new Slider(value,min,max,.01,false,true); }
        @Override public GLNodeValue defaultValue() { return GLNodeValue.of(value); }
        @Override public String viewKey() { return "slider:"+bar+":"+integral+":"+(min!=null&&max!=null?"fixed":"free"); }
        @Override public void validate(GLNodeValue value) {
            Object raw = value.toJavaValue();
            if (!(raw instanceof Number n) || !Double.isFinite(n.doubleValue())
                    || min != null && n.doubleValue() < min || max != null && n.doubleValue() > max
                    || integral && n.doubleValue() != Math.rint(n.doubleValue()))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Value outside slider bounds"));
        }
    }
    record CheckBox(boolean value) implements Editor {
        @Override public GLNodeValue defaultValue() { return GLNodeValue.of(value); }
        @Override public String viewKey() { return "checkbox"; }
        @Override public void validate(GLNodeValue value) {
            if (!(value instanceof GLNodeValue.BooleanValue)) throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Boolean required"));
        }
    }
    record ComboBox<T>(List<T> items, T value, Function<T, String> id, Function<T,String> group) implements Editor {
        public ComboBox(List<T> items,T value,Function<T,String> id){this(items,value,id,item->"");}
        public ComboBox<T> grouped(Function<T,String> group){return new ComboBox<>(items,value,id,group);}
        public com.vingame.glswing.ui.GLComboBox<T> createCombo(){
            var combo=new com.vingame.glswing.ui.GLComboBox<T>();
            combo.setItemTextProvider(item->item instanceof Enum<?>||item instanceof String?com.vin.easyknack.ui.AppText.literal(item.toString()):item.toString());
            var groups=new java.util.LinkedHashMap<String,java.util.List<T>>();
            for(T item:items)groups.computeIfAbsent(group.apply(item),k->new java.util.ArrayList<>()).add(item);
            groups.forEach((label,values)->{if(label==null||label.isBlank())values.forEach(combo::addItem);else combo.addSubMenu(com.vin.easyknack.ui.AppText.literal(label),values);});
            combo.setSelectedItem(value);return combo;
        }
        public ComboBox {
            items = List.copyOf(items);
            if (!items.contains(value) || items.stream().map(id).distinct().count() != items.size())
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unique choices and a valid default required"));
        }
        @Override public GLNodeValue defaultValue() { return GLNodeValue.of(id.apply(value)); }
        @Override public String viewKey() { return "combo:" + items.stream().map(id).toList(); }
        @Override public void validate(GLNodeValue value) {
            if (!(value instanceof GLNodeValue.TextValue text) || items.stream().noneMatch(i -> id.apply(i).equals(text.value())))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unknown choice: ") + value);
        }
    }
    static CheckBox checkBox(boolean value) { return new CheckBox(value); }
    static <T> ComboBox<T> comboBox(List<T> choices, T value, Function<T, String> id) {
        return new ComboBox<>(choices, value, id);
    }
}

