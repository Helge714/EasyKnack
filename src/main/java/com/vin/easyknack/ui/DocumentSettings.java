package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.ui.*;

/** Document-local persistence policy; never removes assets from the live workspace. */
final class DocumentSettings {
    static final String PRESERVE="easyknack.document.preserveUnusedGroups";
    static boolean preserve(GLNodeGraphDocument document){return document.root().metadata().getOrDefault(PRESERVE,GLNodeValue.of(false)).equals(GLNodeValue.of(true));}
    static GLNodeGraphDocument forSave(GLNodeGraphDocument document){
        if(preserve(document))return document;
        var byType=new HashMap<String,GLNodeGraphAsset>();
        for(var asset:document.embeddedAssets().values())if(asset.compositeDescriptor()!=null)byType.put(asset.compositeDescriptor().nodeTypeId(),asset);
        var retained=new LinkedHashMap<String,GLNodeGraphAsset>();
        var pending=new ArrayDeque<GLNodeGraphAsset>();pending.add(document.root());
        var visited=new HashSet<String>();
        while(!pending.isEmpty()){
            var asset=pending.removeFirst();if(!visited.add(asset.id()))continue;
            for(var node:asset.graph().nodes()){
                var dependency=byType.get(node.typeId());
                if(dependency!=null){retained.put(dependency.id(),dependency);pending.add(dependency);}
            }
        }
        return new GLNodeGraphDocument(document.root(),retained,document.externalAssets());
    }
    static void show(GLParent owner,WorkspaceController controller){
        var ref=new GLDialog[1];
        var content=new GLContainer(){
            final GLCheckBox preserve=new GLCheckBox(AppText.text("document.preserveGroups"));
            final GLLabel hint=new GLLabel(AppText.text("document.pruningHint"));
            final GLButton save=new GLButton(AppText.text("save")),cancel=new GLButton(AppText.text("cancel"));
            {preserve.setSelected(controller.preserveUnusedGroups);addGL(preserve);addGL(hint);addGL(save);addGL(cancel);
                save.addActionListener(e->{controller.preserveUnusedGroups=preserve.isSelected();ref[0].close();});
                cancel.addActionListener(e->ref[0].close());}
            protected void record(com.vingame.glswing.rendering.GLContext2D c){}
            protected void resized(float w,float h){preserve.setBounds(20,20,w-40,30);hint.setBounds(20,60,w-40,40);cancel.setBounds(w-230,h-45,100,28);save.setBounds(w-120,h-45,100,28);}
        };
        ref[0]=new GLDialog(AppText.text("document.settings"),content);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(650,170);ref[0].show(owner);
    }
}
