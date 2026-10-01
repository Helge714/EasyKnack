package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;

/** Application unit policy stored in otherwise generic composite editor options. */
final class GroupUnits {
    static final String BASE="easyknack.unit.base", ALLOWED="easyknack.unit.allowed", PREFIX="easyknack.input.";
    static String key(String port){return PREFIX+HexFormat.of().formatHex(port.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    static String unitKey(String port){return key(port)+".unit";}
    static UnitFormat target(GLCompositeGraphRegistry registry,GLCompositeNodeDescriptor descriptor,GLCompositePortMapping port){
        return target(registry,descriptor,port,new HashSet<>());
    }
    private static UnitFormat target(GLCompositeGraphRegistry registry,GLCompositeNodeDescriptor descriptor,GLCompositePortMapping port,Set<String> visited){
        if(!port.isBound()||!visited.add(descriptor.assetId()))return UnitFormat.FACTOR;
        var asset=registry.findAsset(descriptor.assetId()).orElseThrow();
        var node=asset.graph().findNode(port.internalEndpoint().nodeId()).orElse(null);
        if(node==null)return UnitFormat.FACTOR;
        var nested=registry.findComposite(node.typeId()).orElse(null);
        if(nested!=null){
            var inner=nested.portMappings().stream().filter(p->p.publicPort().id().equals(port.internalEndpoint().portId())).findFirst().orElse(null);
            if(inner==null)return UnitFormat.FACTOR;
            var options=new NodeState(inner.presentation().editorOptions());
            var base=inner.presentation().editorOptions().containsKey(BASE)?UnitFormat.fromId(options.text(BASE,"factor")):target(registry,nested,inner,visited);
            return selected(node,inner,base);
        }
        return NodeRegistry.find(node.typeId()).map(def->def.layout(new NodeState(node.properties())).rows().stream()
            .filter(r->r.id().equals(port.internalEndpoint().portId())&&r.editor() instanceof UnitSlider)
            .map(r->((UnitSlider)r.editor()).selected()).findFirst().orElse(UnitFormat.FACTOR)).orElse(UnitFormat.FACTOR);
    }
    static UnitFormat base(GLCompositeGraphRegistry registry,GLCompositeNodeDescriptor descriptor,GLCompositePortMapping port){
        if(GroupInputSettings.checkbox(port))return UnitFormat.FACTOR;
        return port.presentation().editorOptions().containsKey(BASE)?UnitFormat.fromId(new NodeState(port.presentation().editorOptions()).text(BASE,"factor")):target(registry,descriptor,port);
    }
    static List<UnitFormat> allowed(GLCompositePortMapping port,UnitFormat base){
        var raw=port.presentation().editorOptions().get(ALLOWED);
        if(!(raw instanceof GLNodeValue.ListValue list))return List.of(base);
        var values=list.values().stream().map(v->UnitFormat.fromId((String)v.toJavaValue())).distinct().toList();
        if(values.isEmpty()||values.stream().anyMatch(v->!base.compatible(v)))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Incompatible group units"));
        return values;
    }
    static UnitFormat selected(GLGraphNode node,GLCompositePortMapping port,UnitFormat base){
        var selected=UnitFormat.fromId(new NodeState(node.properties()).text(unitKey(port.publicPort().id()),base.id()));
        return allowed(port,base).contains(selected)?selected:base;
    }
}
