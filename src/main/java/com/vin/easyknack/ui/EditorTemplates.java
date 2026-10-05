package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;

/** Document-wide reusable definitions. Applied inputs retain a portable snapshot. */
final class EditorTemplates {
    static final String KEY="easyknack.editorTemplates", PREFIX="easyknack.template.";
    record Entry(String id,String label,GLNodeValue value){public String toString(){return label;}}
    record Template(String id,String label,List<Entry> entries,String initial){
        Template { entries=List.copyOf(entries); new GLNodeEditorTemplate(id,label,entries.stream().map(e->new GLNodeEditorTemplate.Entry(e.id(),e.label(),e.value())).toList(),initial); }
        public String toString(){return label;}
        GLNodeEditorTemplate core(){return new GLNodeEditorTemplate(id,label,entries.stream().map(e->new GLNodeEditorTemplate.Entry(e.id(),e.label(),e.value())).toList(),initial);}
        static Template fromCore(GLNodeEditorTemplate t){return new Template(t.id(),t.label(),t.entries().stream().map(e->new Entry(e.id(),e.label(),e.value())).toList(),t.initial());}
        GLNodeValue value(){return core().value();}
        static Template read(GLNodeValue value){return fromCore(GLNodeEditorTemplate.read(value));}
        Editor.ComboBox<GroupChoices.Option> editor(){var options=entries.stream().map(e->new GroupChoices.Option(e.id(),e.label())).toList();return Editor.comboBox(options,options.stream().filter(e->e.id().equals(initial)).findFirst().orElseThrow(),GroupChoices.Option::id);}
        Map<String,GLNodeValue> mapping(){return core().mapping();}
    }
    private final GLNodeEditorTemplateLibrary definitions=new GLNodeEditorTemplateLibrary();
    private final GLNodeInputEditorRegistry editors;
    private final Set<String> registered=new HashSet<>();
    EditorTemplates(GLNodeInputEditorRegistry editors){this.editors=editors;}
    List<Template> all(){return definitions.all().stream().map(Template::fromCore).toList();}
    void put(Template template){definitions.put(template.core());register(template);}
    void remove(String id){definitions.remove(id);editors.unregister(PREFIX+id);registered.remove(id);}
    GLNodeValue snapshot(){return definitions.snapshot();}
    void load(GLNodeValue value){definitions.load(value);for(var id:List.copyOf(registered))editors.unregister(PREFIX+id);registered.clear();for(var t:all())register(t);}
    void register(Template t){registered.add(t.id());editors.replace(new GLNodeInputEditorDescriptor(PREFIX+t.id(),t.label(),Set.of(SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId(),SignalCategory.WAVETABLE.valueTypeId()),Map.of("template",t.value(),"autoBind",GLNodeValue.of(true))));}
    static <T> Template inherit(String label,Editor.ComboBox<T> combo){return new Template(UUID.randomUUID().toString(),label,combo.items().stream().map(v->new Entry(combo.id().apply(v),v.toString(),GLNodeValue.of(combo.id().apply(v)))).toList(),combo.id().apply(combo.value()));}
}
