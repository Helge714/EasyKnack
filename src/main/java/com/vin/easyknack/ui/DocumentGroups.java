package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.NodeState;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Counts authored instances in every retained definition, including unreachable groups. */
final class DocumentGroups {
    record Use(String assetId,GLNodeId nodeId,String container,String name){}
    record Group(String id,String name,List<Use> uses,boolean open){
        Group{uses=List.copyOf(uses);}
        boolean deletable(){return !open&&uses.isEmpty();}
    }
    static List<Group> inspect(GLNodeGraphDocument document,List<String> open){
        var byType=new HashMap<String,GLNodeGraphAsset>();var uses=new HashMap<String,List<Use>>();
        for(var asset:document.embeddedAssets().values())if(asset.compositeDescriptor()!=null)byType.put(asset.compositeDescriptor().nodeTypeId(),asset);
        for(var owner:NodeDocuments.assets(document))for(var node:owner.graph().nodes()){
            var target=byType.get(node.typeId());if(target==null)continue;
            var view=owner.viewState().nodes().get(node.id());
            String name=view!=null&&view.customTitle()!=null?view.customTitle():name(target);
            uses.computeIfAbsent(target.id(),key->new ArrayList<>()).add(new Use(owner.id(),node.id(),owner.id().equals(document.root().id())?AppText.text("groups.manager.root"):name(owner),name));
        }
        return byType.values().stream().map(asset->new Group(asset.id(),name(asset),uses.getOrDefault(asset.id(),List.of()),open.contains(asset.id())))
            .sorted(Comparator.comparing(Group::name,String.CASE_INSENSITIVE_ORDER).thenComparing(Group::id)).toList();
    }
    private static String name(GLNodeGraphAsset asset){return new NodeState(asset.metadata()).text("easyknack.title",asset.id());}
}
