package com.vin.easyknack.ui;
import java.util.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.*;

/** Reusable combobox definitions; values are separate from stable entry IDs. */
final class EditorTemplatesPanel extends GLContainer {
    private final EditorTemplates library;private final GLHistoryManager history;private final Runnable changed;
    private final List<GLContainer> rows=new ArrayList<>();private boolean updating;
    private final GLComboBox<EditorTemplates.Template> templates=new GLComboBox<>();private final GLComboBox<EditorTemplates.Entry> entries=new GLComboBox<>();
    private final GLTextEditor name=new GLTextEditor(),label=new GLTextEditor(),value=new GLTextEditor();
    private final GLComboBox<String> kind=new GLComboBox<>(List.of("Number","Text"));private final GLLabel status=new GLLabel("");
    EditorTemplatesPanel(EditorTemplates library,GLHistoryManager history,Runnable changed){this.library=library;this.history=history;this.changed=changed;
        kind.setItemTextProvider(AppText::literal);
        row(new GLLabel(com.vin.easyknack.ui.AppText.literal("Reusable editor templates")));row(templates);
        button(com.vin.easyknack.ui.AppText.literal("Add combobox"),()->{String id=UUID.randomUUID().toString(),entry=UUID.randomUUID().toString();put(new EditorTemplates.Template(id,"Selection",List.of(new EditorTemplates.Entry(entry,"Option 1",GLNodeValue.of(0.))),entry));});
        button(com.vin.easyknack.ui.AppText.literal("Remove template"),()->{var t=templates.getSelectedItem();if(t!=null)edit(()->library.remove(t.id()));});
        row(new GLLabel(com.vin.easyknack.ui.AppText.literal("Template name")));row(name);button(com.vin.easyknack.ui.AppText.literal("Rename template"),()->update(t->new EditorTemplates.Template(t.id(),required(name.getText()),t.entries(),t.initial())));
        row(new GLLabel(com.vin.easyknack.ui.AppText.literal("Entries")));row(entries);row(label);row(kind);row(value);
        button(com.vin.easyknack.ui.AppText.literal("Apply entry"),()->change(list->{int i=index(list);var old=list.get(i);GLNodeValue v;
            if("Number".equals(kind.getSelectedItem())){double number=Double.parseDouble(value.getText());if(!Double.isFinite(number))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Use a finite number"));v=GLNodeValue.of(number);}else v=GLNodeValue.of(required(value.getText()));
            list.set(i,new EditorTemplates.Entry(old.id(),required(label.getText()),v));}));
        button(com.vin.easyknack.ui.AppText.literal("Add entry"),()->change(list->list.add(new EditorTemplates.Entry(UUID.randomUUID().toString(),"Option "+(list.size()+1),GLNodeValue.of((double)list.size())))));
        button(com.vin.easyknack.ui.AppText.literal("Remove entry"),()->change(list->{if(list.size()<2)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Keep at least one entry"));list.remove(index(list));}));
        button(com.vin.easyknack.ui.AppText.literal("Move up"),()->move(-1));button(com.vin.easyknack.ui.AppText.literal("Move down"),()->move(1));button(com.vin.easyknack.ui.AppText.literal("Set default"),()->update(t->new EditorTemplates.Template(t.id(),t.label(),t.entries(),entries.getSelectedItem().id())));
        button(com.vin.easyknack.ui.AppText.literal("Assign indices 0, 1, ..."),()->change(list->{for(int i=0;i<list.size();i++){var e=list.get(i);list.set(i,new EditorTemplates.Entry(e.id(),e.label(),GLNodeValue.of((double)i)));}}));
        row(new GLLabel(com.vin.easyknack.ui.AppText.literal("Apply a template to an input in Group-IO.")));row(status);
        templates.addActionListener(e->{if(!updating)loadTemplate();});entries.addActionListener(e->{if(!updating)loadEntry();});refresh();setPreferredGLSize(280,rows.size()*32+20);
    }
    private static String required(String text){if(text.isBlank())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Enter a name/value"));return text.strip();}
    private void row(GLContainer c){rows.add(c);addGL(c);}
    private void button(String text,Runnable action){var b=new GLButton(text);b.addActionListener(e->{try{action.run();status.setText("");}catch(RuntimeException ex){status.setText(ex.getMessage());}});row(b);}
    private void edit(Runnable action){var before=library.snapshot();action.run();var after=library.snapshot();library.load(before);history.perform("Edit template",()->{library.load(after);refresh();changed.run();},()->{library.load(before);refresh();changed.run();});}
    private void put(EditorTemplates.Template t){edit(()->library.put(t));templates.setSelectedItem(library.all().stream().filter(v->v.id().equals(t.id())).findFirst().orElseThrow());}
    private void update(java.util.function.UnaryOperator<EditorTemplates.Template> action){var t=templates.getSelectedItem();if(t!=null)put(action.apply(t));}
    private int index(List<EditorTemplates.Entry> list){return Math.max(0,list.indexOf(entries.getSelectedItem()));}
    private void move(int delta){change(list->{int i=index(list),j=i+delta;if(j>=0&&j<list.size())Collections.swap(list,i,j);});}
    private void change(java.util.function.Consumer<List<EditorTemplates.Entry>> action){update(t->{var list=new ArrayList<>(t.entries());action.accept(list);String initial=list.stream().anyMatch(e->e.id().equals(t.initial()))?t.initial():list.get(0).id();return new EditorTemplates.Template(t.id(),t.label(),list,initial);});}
    void refresh(){String id=templates.getSelectedItem()==null?"":templates.getSelectedItem().id();updating=true;try{templates.removeAllItems();library.all().forEach(templates::addItem);templates.getItems().stream().filter(t->t.id().equals(id)).findFirst().ifPresent(templates::setSelectedItem);if(templates.getSelectedItem()==null&&!templates.getItems().isEmpty())templates.setSelectedIndex(0);}finally{updating=false;}loadTemplate();}
    private void loadTemplate(){var selected=entries.getSelectedItem();updating=true;try{entries.removeAllItems();var t=templates.getSelectedItem();name.setText(t==null?"":t.label());if(t!=null){t.entries().forEach(entries::addItem);entries.setSelectedItem(t.entries().stream().filter(e->selected!=null&&e.id().equals(selected.id())).findFirst().orElse(t.entries().stream().filter(e->e.id().equals(t.initial())).findFirst().orElseThrow()));}}finally{updating=false;}loadEntry();}
    private void loadEntry(){var e=entries.getSelectedItem();label.setText(e==null?"":e.label());value.setText(e==null?"":e.value().toJavaValue().toString());kind.setSelectedItem(e!=null&&e.value().toJavaValue() instanceof String?"Text":"Number");}
    @Override protected void resized(float w,float h){float y=8;for(var row:rows){row.setBounds(12,y,Math.max(0,w-24),28);y+=32;}setPreferredGLSize(280,y+12);}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
}

