package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Earlier Spectrum Mix nodes implement weighted addition, not a crossfade. */
final class SpectrumMixMigration {
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();
        String oldType=new SpectrumMixNode().registration().id(),newType=new SpectrumMathNode().registration().id();
        for(var asset:NodeDocuments.assets(document)){
            var graph=asset.graph();
            var nodes=graph.nodes().stream().map(n->{
                if(!n.typeId().split("\\.variant\\.")[0].equals(oldType)||!(n.properties().containsKey("gain-a")||n.properties().containsKey("gain-b")||!n.properties().containsKey("factor")&&!n.properties().containsKey("mode")))return n;
                var values=new HashMap<>(n.properties());values.put("operation",GLNodeValue.of("add"));
                return new GLGraphNode(n.id(),newType+n.typeId().substring(oldType.length()),n.typeVersion(),values);
            }).toList();
            var descriptor=asset.compositeDescriptor();
            if(descriptor!=null){
                var ports=descriptor.portMappings().stream().map(p->{
                    var options=new HashMap<>(p.presentation().editorOptions());var state=new NodeState(options);
                    if(state.text(GroupEditorCopies.SOURCE_TYPE,"").split("\\.variant\\.")[0].equals(oldType)&&Set.of("gain-a","gain-b").contains(state.text(GroupEditorCopies.SOURCE_PROPERTY,"")))options.put(GroupEditorCopies.SOURCE_TYPE,GLNodeValue.of(newType));
                    return new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),new GLCompositePortPresentation(p.presentation().inputEditorId(),p.presentation().socketVisible(),options),p.additionalEndpoints());
                }).toList();
                descriptor=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),descriptor.minimumAssetVersion(),ports,descriptor.properties(),descriptor.propertyMappings());
            }
            assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,graph.connections()),asset.viewState(),asset.metadata(),descriptor));
        }
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
}
