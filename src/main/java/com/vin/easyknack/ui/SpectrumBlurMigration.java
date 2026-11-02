package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Consolidate earlier octave/bin radii into Width; obsolete Factor mode is rejected. */
final class SpectrumBlurMigration {
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();
        for(var asset:NodeDocuments.assets(document)){
            var graph=asset.graph();var renamed=new HashSet<GLNodeId>();
            var nodes=graph.nodes().stream().map(node->{
                if(!node.typeId().split("\\.variant\\.")[0].equals("easyknack.spectral.blur"))return node;
                var state=new NodeState(node.properties());
                if(!state.values().containsKey(SpectrumBlurNode.UNIT.value())&&state.text("mode","").equals("octaves"))renamed.add(node.id());
                var values=new HashMap<>(new SpectrumBlurNode().normalizeState(state).values());
                for(String key:List.of("mode","factor","octaves"))values.remove(key);
                return new GLGraphNode(node.id(),node.typeId(),node.typeVersion(),values);
            }).toList();
            java.util.function.UnaryOperator<GLNodeEndpoint> endpoint=e->e!=null&&renamed.contains(e.nodeId())&&e.portId().equals("octaves")?new GLNodeEndpoint(e.nodeId(),"width"):e;
            var wires=graph.connections().stream().map(w->new GLGraphConnection(w.id(),endpoint.apply(w.output()),endpoint.apply(w.input()))).toList();
            var descriptor=asset.compositeDescriptor();
            if(descriptor!=null){
                var ports=descriptor.portMappings().stream().map(p->{
                    var options=new HashMap<>(p.presentation().editorOptions());
                    if(p.publicPort().direction()==GLPortDirection.INPUT&&!p.internalEndpoints().isEmpty()&&p.internalEndpoints().stream().allMatch(e->renamed.contains(e.nodeId())&&e.portId().equals("octaves"))){
                        options.put(GroupUnits.BASE,GLNodeValue.of("octave"));options.put(GroupUnits.INITIAL,GLNodeValue.of("octave"));
                        options.put(GroupUnits.ALLOWED,GLNodeValue.of(List.of("octave","cent","semitone")));
                    }
                    if(options.containsKey(GroupEditorCopies.TARGETS))options.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(GroupEditorCopies.targets(p).stream().map(endpoint).toList()));
                    if(new NodeState(options).text(GroupEditorCopies.SOURCE_TYPE,"").equals("easyknack.spectral.blur")&&new NodeState(options).text(GroupEditorCopies.SOURCE_PROPERTY,"").equals("octaves"))options.put(GroupEditorCopies.SOURCE_PROPERTY,GLNodeValue.of("width"));
                    return new GLCompositePortMapping(p.publicPort(),endpoint.apply(p.internalEndpoint()),new GLCompositePortPresentation(p.presentation().inputEditorId(),p.presentation().socketVisible(),options),p.additionalEndpoints().stream().map(endpoint).toList());
                }).toList();
                var properties=descriptor.propertyMappings().stream().map(p->renamed.contains(p.internalNodeId())&&p.internalPropertyId().equals("octaves")?new GLCompositePropertyMapping(p.publicPropertyId(),p.internalNodeId(),"width",p.values()):p).toList();
                descriptor=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),descriptor.minimumAssetVersion(),ports,descriptor.properties(),properties);
            }
            assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires),asset.viewState(),asset.metadata(),descriptor));
        }
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
}
