package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.document.GLNodeGraphDocument;
import com.vingame.glswing.ui.*;
import com.vin.easyknack.nodes.*;

/** The whole graph is independent of the categorized definition selection. */
final class GroupImportPanel extends GLContainer {
    private final List<String> ids=new ArrayList<>();
    final List<GLCheckBox> choices=new ArrayList<>();
    final GLCheckBox all=new GLCheckBox(AppText.text("group.import.all"));
    final GLButton add=new GLButton(AppText.text("group.import.action"));
    private final GLButton cancel=new GLButton(AppText.text("cancel"));
    private final GLLabel message=new GLLabel("");
    private final List<GLContainer> rows=new ArrayList<>();
    private boolean updating;
    private final GLContainer list=new GLContainer(){
        @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
        @Override protected void resized(float w,float h){float y=0;for(var row:rows){float height=row instanceof GLLabel?26:32;row.setBounds(row instanceof GLLabel?4:16,y,Math.max(0,w-24),height);y+=height;}}
    };
    private final GLScrollPane scroll=new GLScrollPane(list);
    GroupImportPanel(GLNodeGraphDocument document,java.util.function.Consumer<List<String>> imported,Runnable closed){
        addChoice(document.root().id(),AppText.text("group.import.root"));addGL(choices.get(0));
        var categories=new LinkedHashMap<String,List<com.vingame.glswing.node.document.GLNodeGraphAsset>>();
        document.embeddedAssets().values().stream().filter(a->a.compositeDescriptor()!=null)
            .sorted(Comparator.comparing(a->new NodeState(a.metadata()).text("easyknack.title",a.id())))
            .forEach(a->categories.computeIfAbsent(NodeCategory.catalogPath(new NodeState(a.metadata()).text("easyknack.category",NodeCategory.ROUTING.path())),k->new ArrayList<>()).add(a));
        var ordered=new ArrayList<String>();for(var category:NodeCategory.menuOrder())if(categories.containsKey(category))ordered.add(category);categories.keySet().stream().filter(k->!ordered.contains(k)).sorted().forEach(ordered::add);
        for(var category:ordered){var heading=new GLLabel(AppText.literal(category));rows.add(heading);list.addGL(heading);for(var asset:categories.get(category)){var box=addChoice(asset.id(),new NodeState(asset.metadata()).text("easyknack.title",asset.id()));rows.add(box);list.addGL(box);}}
        list.setPreferredGLSize(300,rows.stream().mapToInt(r->r instanceof GLLabel?26:32).sum());scroll.setContentTracksViewportWidth(true);scroll.setHorizontalScrollBarPolicy(GLScrollPane.ScrollBarPolicy.NEVER);
        addGL(all);addGL(scroll);addGL(add);addGL(cancel);addGL(message);add.setEnabled(false);all.setEnabled(choices.size()>1);
        all.addActionListener(e->{if(updating)return;updating=true;try{for(int i=1;i<choices.size();i++)choices.get(i).setSelected(all.isSelected());}finally{updating=false;}selectionChanged();});
        cancel.addActionListener(e->closed.run());
        add.addActionListener(e->{try{var selected=selectedIds();if(selected.isEmpty())return;imported.accept(selected);closed.run();}catch(RuntimeException failure){message.setText(failure.getMessage());}});
    }
    private GLCheckBox addChoice(String id,String title){ids.add(id);var box=new GLCheckBox(title);choices.add(box);box.addActionListener(e->selectionChanged());return box;}
    private void selectionChanged(){add.setEnabled(!selectedIds().isEmpty());if(updating)return;updating=true;try{all.setSelected(choices.size()>1&&choices.subList(1,choices.size()).stream().allMatch(GLCheckBox::isSelected));}finally{updating=false;}}
    List<String> selectedIds(){var selected=new ArrayList<String>();for(int i=0;i<ids.size();i++)if(choices.get(i).isSelected())selected.add(ids.get(i));return List.copyOf(selected);}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D c){}
    @Override protected void resized(float w,float h){choices.get(0).setBounds(20,16,w-40,30);all.setBounds(20,60,w-40,30);scroll.setBounds(20,98,w-40,Math.max(0,h-200));message.setBounds(20,h-94,w-40,40);cancel.setBounds(w-235,h-45,100,28);add.setBounds(w-125,h-45,105,28);}
}
