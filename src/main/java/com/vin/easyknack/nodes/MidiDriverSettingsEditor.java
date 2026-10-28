package com.vin.easyknack.nodes;

import com.vin.easyknack.ui.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLDriverTarget;

/** Inspector-only assignment settings; MIDI events never pass through this editor. */
public record MidiDriverSettingsEditor(Editor targetEditor,GLNodeValue initial) implements Editor {
    public GLNodeValue defaultValue(){return initial;}
    public void validate(GLNodeValue value){MidiDriverBinding.read(value);}
    public Control create(String label,java.util.function.Consumer<GLNodeValue> changed){
        Editor target=targetEditor instanceof UnitSlider u?u.spec():targetEditor;GLDriverTarget driver;
        if(target instanceof Editor.Slider slider)driver=GLDriverTarget.number(()->0,v->{},slider.min()==null?-Double.MAX_VALUE:slider.min(),slider.max()==null?Double.MAX_VALUE:slider.max(),slider.integral());
        else if(target instanceof Editor.ComboBox<?> combo)driver=GLDriverTarget.choice(()->0,v->{},()->combo.items().size());
        else if(target instanceof Editor.Trigger)driver=GLDriverTarget.action(()->{});
        else driver=GLDriverTarget.toggle(()->0,v->{});
        var panel=new MidiDriverSettingsPanel(MidiDriverBinding.read(initial),driver,changed,target instanceof Editor.Slider s&&s.continuousChanges());
        return new Control(panel,v->panel.update(MidiDriverBinding.read(v)));
    }
    public static void addSettings(NodeBuilder builder,NodeState state){
        var rows=builder.rows();
        state.values().forEach((key,value)->{
            if(!key.startsWith(GLDriverTarget.PROPERTY_PREFIX))return;
            String property=MidiDriverBinding.property(key);var row=rows.stream().filter(r->r.id().equals(property)&&r.isDriverAssignable()).findFirst().orElse(null);
            if(row!=null)builder.addEditor(new PropertyId(key),row.label()+" — "+AppText.text("driver.title"),new MidiDriverSettingsEditor(row.editor(),value)).inspectorOnly().editorCopyable(false).driverAssignable(false);
        });
    }
}
