package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.history.GLHistoryManagerListener;

/** Document-local group inventory and undoable removal of unused definitions. */
final class GroupManagerPanel extends GLContainer implements AutoCloseable {
    private final WorkspaceController controller;
    final GLList<DocumentGroups.Group> groups=new GLList<>();
    final GLList<String> uses=new GLList<>();
    final GLButton delete=new GLButton(AppText.text("groups.manager.delete"));
    private final GLButton refresh=new GLButton(AppText.text("groups.manager.refresh"));
    private final GLLabel groupLabel=new GLLabel(AppText.text("groups.manager.groups")),useLabel=new GLLabel(AppText.text("groups.manager.uses")),status=new GLLabel("");
    private final GLLabel hint=new GLLabel(AppText.text("groups.manager.hint"));
    private final GLScrollPane groupScroll=new GLScrollPane(groups),useScroll=new GLScrollPane(uses);
    private final GLHistoryManagerListener changed=history->refresh();
    private boolean updating;
    GroupManagerPanel(WorkspaceController controller){
        this.controller=controller;
        for(var component:List.of(groupLabel,useLabel,groupScroll,useScroll,hint,status,delete,refresh))addGL(component);
        groups.setItemTextProvider(group->AppText.format("groups.manager.entry",group.name(),group.uses().size()));
        groups.getSelectionModel().addSelectionListener((model,oldSelection,newSelection)->{if(!updating)selectionChanged();});
        delete.addActionListener(event->{var group=groups.getSelectedValue();if(group==null)return;try{controller.deleteUnusedGroup(group.id());refresh();}catch(IllegalArgumentException failure){refresh();status.setText(failure.getMessage());}});
        refresh.addActionListener(event->refresh());controller.history().addListener(changed);refresh();setPreferredGLSize(800,430);
    }
    void refresh(){
        var selected=groups.getSelectedValue();String id=selected==null?null:selected.id();
        updating=true;try{
            var entries=controller.documentGroups();groups.setItems(entries);
            int at=0;for(int i=0;i<entries.size();i++)if(entries.get(i).id().equals(id))at=i;
            if(!entries.isEmpty())groups.setSelectedIndex(at);
        }finally{updating=false;}
        selectionChanged();
    }
    private void selectionChanged(){
        var group=groups.getSelectedValue();delete.setEnabled(group!=null&&group.deletable());
        uses.setItems(group==null?List.of():group.uses().stream().map(use->use.container()+" / "+use.name()+" · "+use.nodeId().toString().substring(0,8)).toList());
        status.setText(AppText.text(group==null?"groups.manager.empty":group.open()?"groups.manager.open":!group.uses().isEmpty()?"groups.manager.used":"groups.manager.unused"));
    }
    @Override public void close(){controller.history().removeListener(changed);}
    @Override protected void record(com.vingame.glswing.rendering.GLContext2D context){}
    @Override protected void resized(float w,float h){
        float left=(w-52)*.45f,right=w-52-left;
        groupLabel.setBounds(16,12,left,26);useLabel.setBounds(left+36,12,right,26);
        groupScroll.setBounds(16,44,left,Math.max(0,h-160));useScroll.setBounds(left+36,44,right,Math.max(0,h-160));
        hint.setBounds(16,h-108,w-32,34);status.setBounds(16,h-68,w-32,26);
        refresh.setBounds(16,h-36,120,28);delete.setBounds(w-196,h-36,180,28);
    }
    static void show(GLParent owner,WorkspaceController controller){
        var panel=new GroupManagerPanel(controller);var dialog=new GLDialog(AppText.text("groups.manager.title"),panel);
        dialog.setDecoration(GLDialog.Decoration.THEMED);dialog.setSize(860,470);dialog.closedFuture().thenRun(panel::close);dialog.show(owner);
    }
}
