package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.GLNodeEditorCapabilities;
import com.vingame.glswing.node.composite.GLNodeEditorCapabilities.Kind;
import java.util.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.GLNodeInputEditorRegistry;

/** Audio editor definitions adapted to the reusable Core capability contract. */
final class GroupEditorCapabilities {
    private GroupEditorCapabilities() { }
    static GLNodeEditorCapabilities of(NodeBuilder.Row row) {
        var editor=row.editor();
        Kind kind=editor instanceof Editor.Slider||editor instanceof UnitSlider?Kind.SLIDER:
            editor instanceof Editor.ComboBox<?>?Kind.CHOICE:
            editor instanceof Editor.CheckBox||editor instanceof Editor.Toggle?Kind.CHECKBOX:
            editor instanceof Editor.Trigger?Kind.TRIGGER:Kind.CUSTOM;
        return new GLNodeEditorCapabilities(kind,row.isEditorCopyable()&&!row.isMenuOnly()&&GroupEditorCopies.supported(editor),
            row.kind()==NodeBuilder.RowKind.EDITOR,
            editor==null?com.vingame.glswing.node.model.GLNodeValue.of(0):editor.defaultValue(),Map.of());
    }
    record Snapshot(String editorId,GLNodeEditorCapabilities capabilities) { }
    static Snapshot snapshot(NodeBuilder.Row row,GLGraphNode node){
        var opts=new HashMap<String,GLNodeValue>();String editor=GLNodeInputEditorRegistry.SLIDER;
        var current=node.properties().getOrDefault(row.id(),row.editor().defaultValue());
        if(row.editor() instanceof VectorEditor vector){editor=GroupVectorEditors.ID;opts.put("default",current);opts.put(GroupUnits.BASE,GLNodeValue.of(vector.formats().stored().id()));opts.put(GroupUnits.ALLOWED,GLNodeValue.of(vector.formats().choices().stream().map(UnitFormat::id).toList()));}
        else if(row.editor() instanceof Editor.ComboBox<?> combo){var t=EditorTemplates.inherit(row.label(),combo);var selected=t.entries().stream().filter(e->e.value().equals(current)).findFirst().map(EditorTemplates.Entry::id).orElse(t.initial());t=new EditorTemplates.Template(t.id(),t.label(),t.entries(),selected);opts.put("template",t.value());opts.put("autoBind",GLNodeValue.of(true));editor="easyknack.choice";}
        else if(row.editor() instanceof Editor.Trigger)editor=GroupInputSettings.BUTTON;
        else if(row.editor() instanceof Editor.CheckBox||row.editor() instanceof Editor.Toggle){opts.put("presentation",GLNodeValue.of("checkbox"));opts.put("default",GLNodeValue.of(Boolean.TRUE.equals(current.toJavaValue())||current.toJavaValue() instanceof Number number&&number.doubleValue()!=0?1.:0.));}
        else {
            var slider=row.editor() instanceof UnitSlider u?u.spec():(Editor.Slider)row.editor();
            opts.put("default",current);opts.put("minimumEnabled",GLNodeValue.of(slider.min()!=null));opts.put("maximumEnabled",GLNodeValue.of(slider.max()!=null));opts.put("minimum",GLNodeValue.of(slider.min()==null?0:slider.min()));opts.put("maximum",GLNodeValue.of(slider.max()==null?1:slider.max()));opts.put("presentation",GLNodeValue.of(slider.bar()?"bar":"buttons"));opts.put("numericType",GLNodeValue.of(slider.integral()?"integer":"float"));opts.put("step",GLNodeValue.of(slider.step()));opts.put("continuous",GLNodeValue.of(slider.continuousChanges()));
            if(row.editor() instanceof UnitSlider u){opts.put(GroupUnits.BASE,GLNodeValue.of(u.formats().stored().id()));opts.put(GroupUnits.INITIAL,GLNodeValue.of(u.selected().id()));opts.put(GroupUnits.ALLOWED,GLNodeValue.of(u.formats().choices().stream().map(UnitFormat::id).toList()));}
        }
        var capability=of(row);
        return new Snapshot(editor,new GLNodeEditorCapabilities(capability.kind(),capability.copyable(),capability.standalone(),current,opts));
    }
}
