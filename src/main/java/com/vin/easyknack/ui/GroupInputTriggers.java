package com.vin.easyknack.ui;
import java.util.*;
import com.vingame.glswing.node.composite.*;

/** Transient group buttons dispatch only along the current internal connections. */
final class GroupInputTriggers {
    static void fire(GLCompositeGraphRegistry groups,String instance,GLCompositeNodeDescriptor descriptor,GLCompositePortMapping port){
        var targets=new LinkedHashSet<String>();collect(groups,descriptor,port,"",targets,new HashSet<>());
        for(String target:targets){int split=target.lastIndexOf(':');com.vin.easyknack.audio.ManualTriggers.fire(instance+"/"+target.substring(0,split),target.substring(split+1));}
    }
    private static void collect(GLCompositeGraphRegistry groups,GLCompositeNodeDescriptor descriptor,GLCompositePortMapping port,String path,Set<String> targets,Set<String> visiting){
        String key=descriptor.assetId()+"/"+port.publicPort().id();if(!visiting.add(key))return;
        var asset=groups.findAsset(descriptor.assetId()).orElseThrow();
        if(FormulaGroups.isFormula(asset)){
            var materialized=FormulaGroups.materialize(asset);var mapped=materialized.compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals(port.publicPort().id())).findFirst().orElse(null);
            if(mapped!=null&&mapped.isBound())targets.add(path+mapped.internalEndpoint().nodeId()+":"+mapped.internalEndpoint().portId());visiting.remove(key);return;
        }
        for(var endpoint:port.internalEndpoints()){
            var node=asset.graph().findNode(endpoint.nodeId()).orElse(null);if(node==null)continue;
            var nested=groups.findComposite(node.typeId()).orElse(null);
            if(nested==null)targets.add(path+node.id()+":"+endpoint.portId());
            else for(var child:nested.portMappings())if(child.publicPort().id().equals(endpoint.portId()))collect(groups,nested,child,path+node.id()+"/",targets,visiting);
        }
        visiting.remove(key);
    }
}
