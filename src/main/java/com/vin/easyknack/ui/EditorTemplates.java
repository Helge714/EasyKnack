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
        Template{entries=List.copyOf(entries);if(entries.isEmpty()||entries.stream().noneMatch(e->e.id().equals(initial))||entries.stream().map(Entry::id).distinct().count()!=entries.size())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unique entries and a valid default required"));}
        public String toString(){return label;}
        GLNodeValue value(){return GLNodeValue.of(Map.of("id",id,"label",label,"initial",initial,"entries",entries.stream().map(e->Map.of("id",e.id(),"label",e.label(),"value",e.value().toJavaValue())).toList()));}
        static Template read(GLNodeValue value){var m=(Map<?,?>)value.toJavaValue();var entries=new ArrayList<Entry>();for(var raw:(List<?>)m.get("entries")){var e=(Map<?,?>)raw;entries.add(new Entry((String)e.get("id"),(String)e.get("label"),GLNodeValue.of(e.get("value"))));}return new Template((String)m.get("id"),(String)m.get("label"),entries,(String)m.get("initial"));}
        Editor.ComboBox<GroupChoices.Option> editor(){var options=entries.stream().map(e->new GroupChoices.Option(e.id(),e.label())).toList();return Editor.comboBox(options,options.stream().filter(e->e.id().equals(initial)).findFirst().orElseThrow(),GroupChoices.Option::id);}
        Map<String,GLNodeValue> mapping(){var result=new LinkedHashMap<String,GLNodeValue>();entries.forEach(e->result.put(e.id(),e.value()));return result;}
    }
    private final LinkedHashMap<String,Template> definitions=new LinkedHashMap<>();
    private final GLNodeInputEditorRegistry editors;
    private final Set<String> registered=new HashSet<>();
    EditorTemplates(GLNodeInputEditorRegistry editors){this.editors=editors;}
    List<Template> all(){return List.copyOf(definitions.values());}
    void put(Template template){definitions.put(template.id(),template);register(template);}
    void remove(String id){definitions.remove(id);editors.unregister(PREFIX+id);registered.remove(id);}
    GLNodeValue snapshot(){return GLNodeValue.of(all().stream().map(t->t.value().toJavaValue()).toList());}
    void load(GLNodeValue value){for(var id:List.copyOf(registered))remove(id);if(value!=null)for(var raw:(List<?>)value.toJavaValue())put(Template.read(GLNodeValue.of(raw)));}
    void register(Template t){registered.add(t.id());editors.replace(new GLNodeInputEditorDescriptor(PREFIX+t.id(),t.label(),Set.of(SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId(),SignalCategory.WAVETABLE.valueTypeId()),Map.of("template",t.value(),"autoBind",GLNodeValue.of(true))));}
    static <T> Template inherit(String label,Editor.ComboBox<T> combo){return new Template(UUID.randomUUID().toString(),label,combo.items().stream().map(v->new Entry(combo.id().apply(v),v.toString(),GLNodeValue.of(combo.id().apply(v)))).toList(),combo.id().apply(combo.value()));}
}
