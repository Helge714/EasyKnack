package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;

/** Remove obsolete crossing wires, including outputs exposed through nested groups. */
final class ReaderPositionMigration {
    private ReaderPositionMigration(){}
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=NodeDocuments.assets(document);
        Map<String,Set<GLNodeEndpoint>> removed=new HashMap<>();
        for(var asset:assets){
            var endpoints=new HashSet<GLNodeEndpoint>();
            for(var node:asset.graph().nodes())if(node.typeId().equals("easyknack.resource.historyreader")||node.typeId().startsWith("easyknack.resource.historyreader.variant."))
                endpoints.add(new GLNodeEndpoint(node.id(),"write-head-crossing"));
            removed.put(asset.id(),endpoints);
        }
        Map<String,Set<String>> removedPorts=new HashMap<>();
        boolean changed;
        do{
            changed=false;
            for(var asset:assets){var descriptor=asset.compositeDescriptor();if(descriptor==null)continue;
                var ports=removedPorts.computeIfAbsent(descriptor.nodeTypeId(),key->new HashSet<>());
                for(var mapping:descriptor.portMappings())if(mapping.publicPort().direction()==GLPortDirection.OUTPUT
                    &&mapping.internalEndpoints().stream().anyMatch(removed.get(asset.id())::contains))changed|=ports.add(mapping.publicPort().id());
            }
            for(var asset:assets)for(var node:asset.graph().nodes())for(var port:removedPorts.getOrDefault(node.typeId(),Set.of()))
                changed|=removed.get(asset.id()).add(new GLNodeEndpoint(node.id(),port));
        }while(changed);
        Map<String,GLNodeGraphAsset> result=new LinkedHashMap<>();
        for(var asset:assets){
            var graph=asset.graph();var wires=graph.connections().stream().filter(w->!removed.get(asset.id()).contains(w.output())).toList();
            var descriptor=asset.compositeDescriptor();
            if(descriptor!=null){var deleted=removedPorts.getOrDefault(descriptor.nodeTypeId(),Set.of());
                descriptor=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),descriptor.minimumAssetVersion(),
                    descriptor.portMappings().stream().filter(p->!deleted.contains(p.publicPort().id())).toList(),descriptor.properties(),descriptor.propertyMappings());
            }
            result.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),graph.nodes(),wires),asset.viewState(),asset.metadata(),descriptor));
        }
        var root=result.remove(document.root().id());return new GLNodeGraphDocument(root,result,document.externalAssets());
    }
}
