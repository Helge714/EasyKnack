package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Keeps external Count modulation and its saved value; removes only obsolete internal binding. */
final class InstanceGroupMigration {
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();
        for(var asset:NodeDocuments.assets(document)){
            var d=asset.compositeDescriptor();
            if(MultipleGroups.multiple(asset)&&d!=null){
                var ports=new ArrayList<GLCompositePortMapping>();
                for(var p:d.portMappings()){
                    boolean legacy=p.publicPort().id().equals("count")&&p.internalEndpoints().stream().anyMatch(e->e.portId().equals("count")&&asset.graph().findNode(e.nodeId()).map(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof InstanceContextNode).orElse(false));
                    if(legacy){var fixed=MultipleGroups.countPort(p.publicPort().id());var options=new HashMap<>(fixed.presentation().editorOptions());options.put("default",p.presentation().editorOptions().getOrDefault("default",GLNodeValue.of(8.)));p=new GLCompositePortMapping(fixed.publicPort(),null,new GLCompositePortPresentation(fixed.presentation().inputEditorId(),true,options));}
                    ports.add(p);
                }
                var managed=ports.stream().filter(MultipleGroups::countPort).map(p->p.publicPort().id()).collect(java.util.stream.Collectors.toSet());
                d=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings().stream().filter(p->managed.stream().noneMatch(id->p.publicPropertyId().equals(GroupUnits.key(id))||p.publicPropertyId().equals(GroupUnits.unitKey(id)))).toList());
            }
            assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),asset.metadata(),d));
        }
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
}
