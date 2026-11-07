package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.document.GLNodeGraphDocument;
import com.vingame.glswing.ui.*;

/** Checkbox selection supports several imported definitions in one operation. */
final class GroupImportPanel extends GLContainer {
    private final List<String> ids=new ArrayList<>();
    final List<GLCheckBox> choices=new ArrayList<>();
    final GLButton add=new GLButton(AppText.text("group.import.action"));
    private final GLButton cancel=new GLButton(AppText.text("cancel"));
    private final GLLabel message=new GLLabel("");
    private final GLContainer list=new GLContainer(){
        @Override protected void record(com.vingame.glswing.rendering.GLContext2D context){}
        @Override protected void resized(float w,float h){for(int i=0;i<choices.size();i++)choices.get(i).setBounds(4,i*32,Math.max(0,w-8),30);}
    };
    private final GLScrollPane scroll=new GLScrollPane(list);
    GroupImportPanel(GLNodeGraphDocument document,java.util.function.Consumer<List<String>> imported,Runnable closed){
        addChoice(document.root().id(),AppText.text("group.import.root"));
        document.embeddedAssets().values().stream().filter(a->a.compositeDescriptor()!=null)
            .sorted(Comparator.comparing(a->new com.vin.easyknack.nodes.NodeState(a.metadata()).text("easyknack.title",a.id())))
            .forEach(a->addChoice(a.id(),new com.vin.easyknack.nodes.NodeState(a.metadata()).text("easyknack.title",a.id())));
        list.setPreferredGLSize(300,choices.size()*32);scroll.setContentTracksViewportWidth(true);
        scroll.setHorizontalScrollBarPolicy(GLScrollPane.ScrollBarPolicy.NEVER);
        addGL(scroll);addGL(add);addGL(cancel);addGL(message);add.setEnabled(false);
        cancel.addActionListener(e->closed.run());
        add.addActionListener(e->{try{var selected=selectedIds();if(selected.isEmpty())return;imported.accept(selected);closed.run();}catch(RuntimeException failure){message.setText(failure.getMessage());}});
    }
    private void addChoice(String id,String title){ids.add(id);var box=new GLCheckBox(title);choices.add(box);list.addGL(box);box.addActionListener(e->add.setEnabled(!selectedIds().isEmpty()));}
    List<String> selectedIds(){var selected=new ArrayList<String>();for(int i=0;i<ids.size();i++)if(choices.get(i).isSelected())selected.add(ids.get(i));return List.copyOf(selected);}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D context){}
    @Override protected void resized(float w,float h){scroll.setBounds(20,16,w-40,Math.max(0,h-115));message.setBounds(20,h-94,w-40,40);cancel.setBounds(w-235,h-45,100,28);add.setBounds(w-125,h-45,105,28);}
}
