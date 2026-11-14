package com.vin.easyknack.ui;

import java.util.*;
import java.util.logging.Logger;
import com.vin.easyknack.nodes.EnvelopeNode;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Upgrade combined envelope timing and retire the movable hold-position input. */
final class EnvelopeMigration {
    static final Set<String> RETIRED=Set.of("sustain-position");
    private static boolean envelope(GLGraphNode node){
        String id=new EnvelopeNode().registration().id();
        return node!=null&&(node.typeId().equals(id)||node.typeId().startsWith(id+".variant."));
    }
    private static boolean retired(GLNodeGraphAsset asset,GLNodeEndpoint endpoint){
        return envelope(asset.graph().findNode(endpoint.nodeId()).orElse(null))&&RETIRED.stream().anyMatch(k->endpoint.portId().equals(k)||endpoint.portId().startsWith(k+"."));
    }
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();int changed=0;
        for(var asset:NodeDocuments.assets(document)){
            var graph=asset.graph();var nodes=new ArrayList<GLGraphNode>();
            for(var node:graph.nodes()){
                if(envelope(node)&&!node.properties().containsKey("decay")){
                    var values=new HashMap<>(node.properties());var state=new com.vin.easyknack.nodes.NodeState(values);
                    var curve=com.vin.easyknack.nodes.CurveData.decode(state.text("curve",com.vin.easyknack.nodes.CurveData.encode(com.vin.easyknack.nodes.CurveData.ADSR)));
                    double cut=state.number("sustain-position",.5),peak=curve.points().size()>2?curve.points().get(1).x():cut*.5;
                    double ratio=Math.max(0,Math.min(1,peak/Math.max(.0001,cut))),combined=state.number("duration",300);
                    values.put("duration",GLNodeValue.of(combined*ratio));values.put("decay",GLNodeValue.of(combined*(1-ratio)));
                    values.remove("sustain-position");values.remove("sustain-position.unit");
                    node=new GLGraphNode(node.id(),node.typeId(),node.typeVersion(),values);changed++;
                }

                if(envelope(node)&&node.properties().containsKey("curve")){
                    var curve=com.vin.easyknack.nodes.CurveData.decode((String)node.properties().get("curve").toJavaValue());
                    node=node.withProperty("curve",GLNodeValue.of(com.vin.easyknack.nodes.CurveData.encode(com.vin.easyknack.nodes.CurveData.envelopePhases(curve))));
                }
                nodes.add(node);
            }
            var d=asset.compositeDescriptor();
            if(d!=null){
                var ports=new ArrayList<GLCompositePortMapping>();
                for(var port:d.portMappings()){
                    var targets=GroupEditorCopies.targets(port);var keep=targets.stream().filter(e->!retired(asset,e)).toList();
                    var endpoints=port.internalEndpoints().stream().filter(e->!retired(asset,e)).toList();
                    if(!keep.equals(targets)||!endpoints.equals(port.internalEndpoints())){
                        changed++;port=port.withEndpoints(endpoints);
                        if(keep.isEmpty())port=GroupEditorCopies.detachSource(port);
                        else{
                            var options=new HashMap<>(port.presentation().editorOptions());
                            options.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(keep));
                            options.put("copySource",GLNodeValue.of(keep.get(0).toString()));
                            port=new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),new GLCompositePortPresentation(port.presentation().inputEditorId(),port.presentation().socketVisible(),options),port.additionalEndpoints());
                        }
                    }
                    ports.add(port);
                }
                var mappings=d.propertyMappings().stream().filter(m->!retired(asset,new GLNodeEndpoint(m.internalNodeId(),m.internalPropertyId()))).toList();
                if(!mappings.equals(d.propertyMappings()))changed++;
                d=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),mappings);
            }
            var connections=graph.connections().stream().filter(c->!retired(asset,c.input())&&!retired(asset,c.output())).toList();
            if(connections.size()!=graph.connections().size())changed++;
            assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,connections),asset.viewState(),asset.metadata(),d));
        }
        if(changed>0)Logger.getLogger(EnvelopeMigration.class.getName()).warning("Envelope timing upgraded to separate Attack/Decay/Release. Previous combined timing is split by curve position; existing duration wires now control Attack. The retired Sustain position input and its copied bindings/wires are removed; Hold uses the end of Decay.");
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
}
