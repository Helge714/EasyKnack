package com.vin.easyknack.ui;

import java.util.*;
import java.nio.charset.StandardCharsets;
import com.vin.easyknack.nodes.BufferContextNode;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Move legacy writer metadata connections onto a shared resource context. */
final class BufferContextMigration {
    static Set<GLNodeId> writers(GLNodeGraphAsset asset){
        var used=new HashSet<GLNodeEndpoint>();asset.graph().connections().forEach(w->used.add(w.output()));
        if(asset.compositeDescriptor()!=null)asset.compositeDescriptor().portMappings().stream().filter(p->p.publicPort().direction()==GLPortDirection.OUTPUT).forEach(p->used.addAll(p.internalEndpoints()));
        var result=new LinkedHashSet<GLNodeId>();
        for(var n:asset.graph().nodes())if((n.typeId().equals("easyknack.resource.history")||n.typeId().startsWith("easyknack.resource.history.variant."))&&used.contains(new GLNodeEndpoint(n.id(),"write-position")))result.add(n.id());
        return result;
    }
    static int count(GLNodeGraphDocument document){return NodeDocuments.assets(document).stream().mapToInt(a->writers(a).size()).sum();}
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();
        for(var asset:NodeDocuments.assets(document)){
            var writers=writers(asset);if(writers.isEmpty()){assets.put(asset.id(),asset);continue;}
            var graph=asset.graph();var nodes=new ArrayList<>(graph.nodes());var views=new LinkedHashMap<>(asset.viewState().nodes());var replacements=new HashMap<GLNodeId,GLNodeId>();
            var links=new ArrayList<GLGraphConnection>();
            for(var writer:writers){
                int suffix=0;GLNodeId id;
                do{id=new GLNodeId(UUID.nameUUIDFromBytes((graph.graphId()+":"+writer+":buffer-context:"+suffix++).getBytes(StandardCharsets.UTF_8)));}while(contains(nodes,id));
                replacements.put(writer,id);var definition=new BufferContextNode();nodes.add(new GLGraphNode(id,definition.registration().id(),definition.defaults()));
                links.add(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(writer,"history"),new GLNodeEndpoint(id,"buffer")));
                var view=asset.viewState().nodes().get(writer);if(view!=null)views.put(id,new GLNodeViewState(view.x()+view.width(),view.y()+Math.max(view.height(),160)));
            }
            java.util.function.UnaryOperator<GLNodeEndpoint> remap=e->replacements.containsKey(e.nodeId())&&e.portId().equals("write-position")?new GLNodeEndpoint(replacements.get(e.nodeId()),"write-position"):e;
            var wires=new ArrayList<GLGraphConnection>();for(var w:graph.connections())wires.add(new GLGraphConnection(w.id(),remap.apply(w.output()),w.input()));wires.addAll(links);
            var d=asset.compositeDescriptor();if(d!=null)d=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().map(p->p.publicPort().direction()==GLPortDirection.OUTPUT?p.withEndpoints(p.internalEndpoints().stream().map(remap).toList()):p).toList(),d.properties(),d.propertyMappings());
            assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires),new GLNodeEditorViewState(views,asset.viewState().auxiliaryNodes()),asset.metadata(),d));
        }
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
    private static boolean contains(List<GLGraphNode> nodes,GLNodeId id){return nodes.stream().anyMatch(n->n.id().equals(id));}
}
