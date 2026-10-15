package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Retarget editor provenance when an inner group instance is expanded. */
final class UngroupEditors {
    static GLNodeGraphAsset remap(GLNodeGraphAsset parent,GLCompositeUngroup.Result result,
            GLNodeId instance,GLCompositeNodeDescriptor child){
        var d=parent.compositeDescriptor();
        var ports=d.portMappings().stream().map(p->{
            var options=new HashMap<>(p.presentation().editorOptions());
            var source=options.get("copySource");
            if(source!=null)for(var port:child.portMappings()){
                var endpoint=new GLNodeEndpoint(instance,port.publicPort().id());
                if(source.toJavaValue().equals(endpoint.toString())){
                    var targets=result.resolve(endpoint,instance);
                    if(targets.isEmpty())options.remove("copySource");else options.put("copySource",GLNodeValue.of(targets.get(0).toString()));
                }
            }
            if(!GroupEditorCopies.targets(p).isEmpty()){
                var targets=new ArrayList<GLNodeEndpoint>();
                for(var target:GroupEditorCopies.targets(p)){
                    if(!target.nodeId().equals(instance)){targets.add(target);continue;}
                    for(var mapping:child.propertyMappings())if(mapping.publicPropertyId().equals(target.portId()))
                        targets.add(new GLNodeEndpoint(result.nodeIds().get(mapping.internalNodeId()),mapping.internalPropertyId()));
                }
                options.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(targets));
            }
            return new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),new GLCompositePortPresentation(p.presentation().inputEditorId(),p.presentation().socketVisible(),options),p.additionalEndpoints());
        }).toList();
        return new GLNodeGraphAsset(parent.id(),parent.version(),parent.kind(),parent.graph(),parent.viewState(),parent.metadata(),
            new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings()));
    }
}
