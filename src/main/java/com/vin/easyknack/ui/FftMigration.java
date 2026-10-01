package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
/** Retain old analysis timing and streaming routes while presenting a single FFT. */
final class FftMigration {
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();
        for(var asset:NodeDocuments.assets(document)){
            var old=new HashSet<GLNodeId>();var graph=asset.graph();
            var nodes=graph.nodes().stream().map(n->{
                String type=n.typeId().split("\\.variant\\.")[0];
                if(type.equals("easyknack.spectral.fft")){
                    old.add(n.id());var values=new HashMap<>(new SpectrumNode().defaults());values.putAll(n.properties());
                    values.put("synchronous",GLNodeValue.of(true));values.put("channel",GLNodeValue.of("all"));
                    values.put("overlap",GLNodeValue.of(new NodeState(n.properties()).text("overlap","4").equals("2")?.5:.75));
                    return new GLGraphNode(n.id(),"easyknack.analysis.spectrum",n.typeVersion(),values);
                }
                if(type.equals("easyknack.analysis.spectrum")&&!n.properties().containsKey("synchronous"))
                    return n.withProperty("synchronous",GLNodeValue.of(false)).withProperty("channel",n.properties().getOrDefault("channel",GLNodeValue.of("left")));
                return n;
            }).toList();
            java.util.function.UnaryOperator<GLNodeEndpoint> endpoint=e->old.contains(e.nodeId())?new GLNodeEndpoint(e.nodeId(),e.portId().equals("in")?"audio-in":e.portId().equals("spectrum")?"analysis":e.portId()):e;
            var wires=graph.connections().stream().map(w->new GLGraphConnection(w.id(),endpoint.apply(w.output()),endpoint.apply(w.input()))).toList();
            var d=asset.compositeDescriptor();if(d!=null)d=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().map(m->m.withEndpoints(m.internalEndpoints().stream().map(endpoint).toList())).toList(),d.properties(),d.propertyMappings());
            assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires),asset.viewState(),asset.metadata(),d));
        }
        // Update exposed spectrum contracts from leaves outwards, without renaming public ports.
        for(int pass=0;pass<assets.size();pass++){
            var types=new HashMap<String,GLCompositeNodeDescriptor>();assets.values().forEach(a->{if(a.compositeDescriptor()!=null)types.put(a.compositeDescriptor().nodeTypeId(),a.compositeDescriptor());});
            for(var asset:List.copyOf(assets.values())){
                var d=asset.compositeDescriptor();if(d==null)continue;
                var ports=d.portMappings().stream().map(m->{
                    if(!m.publicPort().valueTypeId().equals(PortDataType.ANALYSIS.id())||m.internalEndpoints().isEmpty())return m;
                    boolean spectrum=m.internalEndpoints().stream().allMatch(e->{
                        var node=asset.graph().findNode(e.nodeId()).orElse(null);if(node==null)return false;
                        var def=NodeRegistry.find(node.typeId());
                        var portsAtNode=def.isPresent()?def.get().layout(new NodeState(node.properties())).ports():types.containsKey(node.typeId())?types.get(node.typeId()).nodeType().ports():List.<GLNodePortDescriptor>of();
                        return portsAtNode.stream().anyMatch(p->p.id().equals(e.portId())&&p.valueTypeId().equals(PortDataType.SPECTRUM.id()));
                    });
                    if(!spectrum)return m;var p=m.publicPort();
                    return new GLCompositePortMapping(new GLNodePortDescriptor(p.id(),p.direction(),PortDataType.SPECTRUM.id(),p.timing(),p.required(),p.maximumConnections()),m.internalEndpoint(),m.presentation(),m.additionalEndpoints());
                }).toList();
                var next=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings());
                assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),asset.metadata(),next));
            }
        }
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
}
