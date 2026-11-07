package com.vin.easyknack.ui;

import java.util.*;
import java.util.logging.Logger;
import com.vin.easyknack.nodes.PartialAnalysisNode;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Retire the experimental classifier without pretending its scores are Loris parameters. */
final class PartialAnalysisMigration {
    static final Set<String> RETIRED=Set.of("sinusoidality","prominence","assessment","confidence","classify","peak-snr","tolerance","gap","harmonic","fundamental","harmonic-tolerance","minimum-f0","maximum-f0");
    private static boolean analysis(GLGraphNode node){
        String id=new PartialAnalysisNode().registration().id();
        return node!=null&&(node.typeId().equals(id)||node.typeId().startsWith(id+".variant."));
    }
    private static boolean retired(GLNodeGraphAsset asset,GLNodeEndpoint endpoint){
        return analysis(asset.graph().findNode(endpoint.nodeId()).orElse(null))&&RETIRED.stream().anyMatch(k->endpoint.portId().equals(k)||endpoint.portId().startsWith(k+"."));
    }
    static GLNodeGraphDocument migrate(GLNodeGraphDocument document){
        var assets=new LinkedHashMap<String,GLNodeGraphAsset>();int changed=0;
        for(var asset:NodeDocuments.assets(document)){
            var graph=asset.graph();var nodes=new ArrayList<GLGraphNode>();
            for(var node:graph.nodes()){
                if(analysis(node)&&RETIRED.stream().anyMatch(node.properties()::containsKey)){
                    var values=new HashMap<>(node.properties());
                    if(!values.containsKey("bandwidth")){
                        boolean raw=GLNodeValue.of(false).equals(values.get("classify"))||values.containsKey("sinusoidality")&&values.containsKey("confidence")
                            &&((Number)values.get("sinusoidality").toJavaValue()).doubleValue()==0
                            &&((Number)values.get("confidence").toJavaValue()).doubleValue()==0;
                        values.put("bandwidth",GLNodeValue.of(raw?"none":"convergence"));
                    }
                    values.keySet().removeIf(k->RETIRED.stream().anyMatch(r->k.equals(r)||k.startsWith(r+".")));
                    node=new GLGraphNode(node.id(),node.typeId(),node.typeVersion(),values);changed++;
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
        if(changed>0)Logger.getLogger(PartialAnalysisMigration.class.getName()).warning("Partial Analysis now uses Loris. Experimental classifier/harmonic controls and their copied bindings or connections were removed; frequency range, amplitude floor, capacity and duration are retained. Existing analyses can sound different.");
        return new GLNodeGraphDocument(assets.remove(document.root().id()),assets,document.externalAssets());
    }
}
