package com.vin.easyknack.ui;

import java.util.*;
import java.util.stream.Collectors;
import com.vin.easyknack.nodes.PropertyId;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.GLCompositeGraphRegistry;

/** Presentation-only coupling of group defaults to the live inspector. */
final class WorkspaceGroupBindings {
    private GLNodePropertyInspector inspector;
    private Set<GLNodeEndpoint> targets=Set.of();
    void refresh(GLNodeWorkspace workspace,GLCompositeGraphRegistry groups){
        if(inspector!=workspace.propertyInspector()){
            inspector=workspace.propertyInspector();
            if(inspector!=null){
                inspector.setPresentationModel(n->GroupEditorCopies.previewDefaults(
                    workspace.currentAssetId()==null?null:groups.findAsset(workspace.currentAssetId()).orElse(null),n));
                inspector.setReadOnlyReason((node,property)->properties(workspace,groups,node.id())
                    .contains(GroupEditorCopies.controlledKey(new PropertyId(property)))?AppText.text("group.editor.controlled"):null);
            }
        }
        var asset=workspace.currentAssetId()==null?null:groups.findAsset(workspace.currentAssetId()).orElse(null);
        var next=asset==null||asset.compositeDescriptor()==null?Set.<GLNodeEndpoint>of():asset.compositeDescriptor().portMappings()
            .stream().flatMap(p->GroupEditorCopies.targets(p).stream()).collect(Collectors.toSet());
        if(!targets.equals(next)){targets=Set.copyOf(next);if(inspector!=null)inspector.refresh();}
    }
    void refreshInspector(){if(inspector!=null)inspector.refresh();}
    Set<PropertyId> properties(GLNodeWorkspace workspace,GLCompositeGraphRegistry groups,GLNodeId id){
        if(workspace.currentAssetId()==null)return Set.of();
        var asset=groups.findAsset(workspace.currentAssetId()).orElse(null);
        if(asset==null||asset.compositeDescriptor()==null)return Set.of();
        return asset.compositeDescriptor().portMappings().stream().flatMap(p->GroupEditorCopies.targets(p).stream())
            .filter(e->e.nodeId().equals(id)).map(e->new PropertyId(e.portId())).collect(Collectors.toSet());
    }
}
