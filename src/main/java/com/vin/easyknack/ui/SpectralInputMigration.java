package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;
/** Make legacy readers' implicit spectrum source explicit, including nested groups. */
final class SpectralInputMigration {
    private record Added(String type,String port){}
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();NodeDocuments.assets(document).forEach(a->assets.put(a.id(),a));
        var added=new ArrayDeque<Added>();
        for(var asset:List.copyOf(assets.values())){
            if(asset.compositeDescriptor()==null)continue;
            var legacy=asset.graph().nodes().stream().filter(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof BinReadNode&&!n.properties().containsKey(BinReadNode.EXPLICIT)).toList();
            if(legacy.isEmpty())continue;
            var ids=legacy.stream().map(GLGraphNode::id).collect(java.util.stream.Collectors.toSet());
            var g=asset.graph();var graph=new GLNodeGraphSnapshot(g.graphId(),g.revision(),g.nodes().stream().map(n->ids.contains(n.id())?n.withProperty(BinReadNode.EXPLICIT,GLNodeValue.of(true)):n).toList(),g.connections());
            asset=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph,asset.viewState(),asset.metadata(),asset.compositeDescriptor());
            assets.put(asset.id(),bind(asset,legacy.stream().map(n->new GLNodeEndpoint(n.id(),"spectrum")).toList(),added));
        }
        var visited=new HashSet<Added>();
        while(!added.isEmpty()){
            var child=added.remove();if(!visited.add(child))continue;
            for(var parent:List.copyOf(assets.values()))if(parent.compositeDescriptor()!=null){
                var endpoints=parent.graph().nodes().stream().filter(n->n.typeId().equals(child.type())).map(n->new GLNodeEndpoint(n.id(),child.port())).toList();
                if(!endpoints.isEmpty())assets.put(parent.id(),bind(parent,endpoints,added));
            }
        }
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
    private static GLNodeGraphAsset bind(GLNodeGraphAsset asset,List<GLNodeEndpoint> endpoints,Deque<Added> added){
        var d=asset.compositeDescriptor();var ports=new ArrayList<>(d.portMappings());int index=-1;
        for(int i=0;i<ports.size();i++)if(ports.get(i).publicPort().direction()==GLPortDirection.INPUT&&ports.get(i).publicPort().valueTypeId().equals(PortDataType.SPECTRUM.id())){index=i;break;}
        if(index<0){
            var ids=ports.stream().map(p->p.publicPort().id()).collect(java.util.stream.Collectors.toSet());String id="spectrum";for(int i=2;ids.contains(id);i++)id="spectrum-"+i;
            ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input(id,PortDataType.SPECTRUM.id(),false,1),null).withEndpoints(endpoints));
            added.add(new Added(d.nodeTypeId(),id));
        }else{var all=new LinkedHashSet<>(ports.get(index).internalEndpoints());all.addAll(endpoints);ports.set(index,ports.get(index).withEndpoints(List.copyOf(all)));}
        var descriptor=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings());
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),asset.metadata(),descriptor);
    }
}
