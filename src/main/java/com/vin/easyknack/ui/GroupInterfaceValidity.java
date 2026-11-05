package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Remove provably stale application-owned bindings against the current graph. */
final class GroupInterfaceValidity {
    private GroupInterfaceValidity() { }
    private static List<GLNodePortDescriptor> ports(GLGraphNode node,GLCompositeGraphRegistry groups){
        var definition=NodeRegistry.find(node.typeId()).orElse(null);if(definition!=null)return definition.layout(new NodeState(node.properties())).ports();
        return groups.findComposite(node.typeId()).map(d->d.nodeType().ports()).orElse(null);
    }
    static GLNodeGraphSnapshot connections(GLNodeGraphSnapshot graph,GLCompositeGraphRegistry groups){
        var ports=new HashMap<GLNodeId,List<GLNodePortDescriptor>>();
        for(var node:graph.nodes())ports.put(node.id(),ports(node,groups));
        var types=NodeRegistry.executionContracts().types();
        var wires=graph.connections().stream().filter(w->{
            if(!ports.containsKey(w.output().nodeId())||!ports.containsKey(w.input().nodeId()))return false;
            var outputs=ports.get(w.output().nodeId());var inputs=ports.get(w.input().nodeId());
            var output=outputs==null?null:outputs.stream().filter(p->p.id().equals(w.output().portId())&&p.direction()==GLPortDirection.OUTPUT).findFirst().orElse(null);
            var input=inputs==null?null:inputs.stream().filter(p->p.id().equals(w.input().portId())&&p.direction()==GLPortDirection.INPUT).findFirst().orElse(null);
            if(outputs!=null&&output==null||inputs!=null&&input==null)return false;
            return output==null||input==null||types.compatibleValueTypes(output.valueTypeId(),input.valueTypeId());
        }).toList();
        return wires.equals(graph.connections())?graph:new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),graph.nodes(),wires);
    }
    private static NodeBuilder.Row row(GLGraphNode node,String property){
        var definition=NodeRegistry.find(node.typeId()).orElse(null);
        return definition==null?null:definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.id().equals(property)).findFirst().orElse(null);
    }
    static boolean property(GLNodeGraphAsset asset,GLNodeEndpoint endpoint,GLCompositeGraphRegistry groups){
        var node=asset.graph().findNode(endpoint.nodeId()).orElse(null);if(node==null)return false;
        var nested=groups.findComposite(node.typeId()).orElse(null);
        if(nested!=null)return nested.findProperty(endpoint.portId()).isPresent()||nested.portMappings().stream().anyMatch(p->
            endpoint.portId().equals(GroupUnits.key(p.publicPort().id()))||endpoint.portId().equals(GroupUnits.unitKey(p.publicPort().id()))
                ||GroupInputs.choice(p)&&endpoint.portId().equals(GroupInputs.property(p)));
        var definition=NodeRegistry.find(node.typeId()).orElse(null);if(definition==null)return true; // Unavailable plugins are not proof of deletion.
        return definition.layout(new NodeState(node.properties())).rows().stream().anyMatch(r->r.kind()!=NodeBuilder.RowKind.OUTPUT&&r.id().equals(endpoint.portId())||r.formats()!=null&&r.unitProperty().value().equals(endpoint.portId()));
    }
    private static boolean editor(GLNodeGraphAsset asset,GLNodeEndpoint endpoint,GLCompositePortMapping mapping,GLCompositeGraphRegistry groups){
        var node=asset.graph().findNode(endpoint.nodeId()).orElse(null);if(node==null)return false;
        if(NodeRegistry.find(node.typeId()).isEmpty())return groups.findComposite(node.typeId()).isEmpty();
        var row=row(node,endpoint.portId());if(row==null||!GroupEditorCopies.linkable(row))return false;
        if(GroupInputs.template(mapping)!=null){
            if(!(row.editor() instanceof Editor.ComboBox<?> combo))return false;
            try{
                for(var entry:GroupInputs.template(mapping).entries()){
                    if(entry.value().toJavaValue() instanceof Number number){double index=number.doubleValue();if(!Double.isFinite(index)||index!=Math.rint(index)||index<0||index>=combo.items().size())return false;}
                    else combo.validate(entry.value());
                }
                return true;
            }catch(IllegalArgumentException incompatible){return false;}
        }
        if(GroupVectorEditors.isVector(mapping))return row.editor() instanceof VectorEditor;
        if(GroupInputSettings.checkbox(mapping))return row.editor() instanceof Editor.CheckBox||row.editor() instanceof Editor.Toggle;
        if(mapping.presentation().inputEditorId().equals(GroupInputSettings.BUTTON))return row.editor() instanceof Editor.Trigger;
        return row.editor() instanceof Editor.Slider||row.editor() instanceof UnitSlider;
    }
    private static boolean port(GLNodeGraphAsset asset,GLNodeEndpoint endpoint,GLCompositePortMapping mapping,GLCompositeGraphRegistry groups){
        if(GLCompositeForwarding.isInput(endpoint))return GLCompositeForwarding.valid(mapping,asset.compositeDescriptor().portMappings());
        var node=asset.graph().findNode(endpoint.nodeId()).orElse(null);if(node==null)return false;
        var definition=NodeRegistry.find(node.typeId()).orElse(null);var nested=groups.findComposite(node.typeId()).orElse(null);
        if(definition==null&&nested==null)return true;
        // Older copied editors used an internal endpoint before TARGETS existed.
        if(definition!=null&&mapping.publicPort().direction()==GLPortDirection.INPUT
            &&GroupInputs.choice(mapping)&&editor(asset,endpoint,mapping,groups))return true;
        var ports=definition!=null?definition.layout(new NodeState(node.properties())).ports():nested.nodeType().ports();
        var target=ports.stream().filter(p->p.id().equals(endpoint.portId())).findFirst().orElse(null);
        if(target==null||target.direction()!=mapping.publicPort().direction())return false;
        var types=NodeRegistry.executionContracts().types();
        return target.direction()==GLPortDirection.INPUT?types.compatibleValueTypes(mapping.publicPort().valueTypeId(),target.valueTypeId())
            :types.compatibleValueTypes(target.valueTypeId(),mapping.publicPort().valueTypeId());
    }
    static GLCompositePortMapping port(GLCompositePortMapping mapping,GLNodeGraphAsset asset,GLCompositeGraphRegistry groups){
        if(asset==null||asset.compositeDescriptor()==null||FormulaGroups.isFormula(asset))return mapping;
        var result=mapping.withEndpoints(mapping.internalEndpoints().stream().filter(e->port(asset,e,mapping,groups)).toList());
        var targets=GroupEditorCopies.targets(mapping);
        if(!targets.isEmpty()){
            var remaining=targets.stream().filter(e->editor(asset,e,mapping,groups)).distinct().toList();
            if(!remaining.equals(targets)){
                var options=new HashMap<>(result.presentation().editorOptions());
                options.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(remaining));
                if(!remaining.isEmpty())options.put("copySource",GLNodeValue.of(remaining.get(0).toString()));
                result=new GLCompositePortMapping(result.publicPort(),result.internalEndpoint(),new GLCompositePortPresentation(result.presentation().inputEditorId(),result.presentation().socketVisible(),options),result.additionalEndpoints());
                if(remaining.isEmpty())return GroupEditorCopies.detachSource(result);
            }
        }
        return GroupEditorCopies.detachMissingSource(result);
    }
    static GLNodeGraphAsset clean(GLNodeGraphAsset asset,GLCompositeGraphRegistry groups){
        var descriptor=asset.compositeDescriptor();if(descriptor==null||FormulaGroups.isFormula(asset))return asset;
        var ports=descriptor.portMappings().stream().map(p->port(p,asset,groups)).toList();
        var mappings=descriptor.propertyMappings().stream().filter(m->property(asset,new GLNodeEndpoint(m.internalNodeId(),m.internalPropertyId()),groups)).toList();
        var graph=connections(asset.graph(),groups);
        if(ports.equals(descriptor.portMappings())&&mappings.equals(descriptor.propertyMappings())&&graph.equals(asset.graph()))return asset;
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph,asset.viewState(),asset.metadata(),new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),descriptor.minimumAssetVersion(),ports,descriptor.properties(),mappings));
    }
}
