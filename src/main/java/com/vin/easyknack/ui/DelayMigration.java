package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
/** Preserve old delay values, node identities, placement and exposed group ports. */
final class DelayMigration {
    private DelayMigration(){}
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var result=new LinkedHashMap<String,GLNodeGraphAsset>();
        for(var asset:NodeDocuments.assets(document)){
            var graph=asset.graph();var old=new HashSet<GLNodeId>();
            var nodes=graph.nodes().stream().map(node->{
                String type=node.typeId().split("\\.variant\\.")[0];
                if(!type.equals("easyknack.audio.sampledelay")&&!type.equals("easyknack.audio.blockdelay"))return node;
                old.add(node.id());double samples=type.endsWith("sampledelay")?1:com.vin.easyknack.audio.AudioProcessing.BLOCK_FRAMES;
                var converted=node.withType("easyknack.audio.delay",node.typeVersion())
                    .withProperty(DelayNode.IN.value(),node.properties().getOrDefault("in",GLNodeValue.of(0)))
                    .withProperty(DelayNode.TIME.value(),GLNodeValue.of(samples/com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE)).withProperty("time.unit",GLNodeValue.of("sample"));
                return converted.withoutProperty("in");
            }).toList();
            java.util.function.UnaryOperator<GLNodeEndpoint> endpoint=e->old.contains(e.nodeId())?new GLNodeEndpoint(e.nodeId(),e.portId().equals("in")?DelayNode.IN.value():e.portId().equals("out")?DelayNode.OUT.value():e.portId()):e;
            var wires=graph.connections().stream().map(w->new GLGraphConnection(w.id(),endpoint.apply(w.output()),endpoint.apply(w.input()))).toList();
            var d=asset.compositeDescriptor();if(d!=null)d=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().map(m->m.withEndpoints(m.internalEndpoints().stream().map(endpoint).toList())).toList(),d.properties(),d.propertyMappings());
            result.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires),asset.viewState(),asset.metadata(),d));
        }
        return new GLNodeGraphDocument(result.remove(document.root().id()),result,document.externalAssets());
    }
}
