package com.vin.easyknack.ui;
import java.util.*;
import java.util.function.Function;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.GLCompositeFlattenResult;
import com.vingame.glswing.node.model.*;

/** Keeps editable inner trigger buttons addressable after execution-domain compilation. */
final class CompiledManualTargets {
    static final String PROPERTY="easyknack.compiledManualTargets";
    static Map<String,List<String>> collect(List<GLGraphNode> nodes,GLCompositeFlattenResult flat,Function<GLGraphNode,String> target){
        var result=new LinkedHashMap<String,List<String>>();
        for(var node:nodes){
            var d=NodeRegistry.require(node.typeId());
            if(d.layout(new NodeState(node.properties())).rows().stream().noneMatch(r->r.editor() instanceof Editor.Trigger))continue;
            var origin=flat.origins().get(node.id());String source=(origin==null?node.id():origin.sourceNodeId()).toString();
            result.computeIfAbsent(source,k->new ArrayList<>()).add(target.apply(node));
            if(origin!=null)for(int i=origin.instances().size()-1;i>=0;i--){
                source=origin.instances().get(i).nodeId()+"/"+source;
                result.computeIfAbsent(source,k->new ArrayList<>()).add(target.apply(node));
            }
        }
        return result;
    }
    static Map<String,List<String>> read(GLGraphNode node){
        var value=node.properties().get(PROPERTY);if(value==null)return Map.of();
        var result=new LinkedHashMap<String,List<String>>();
        if(value.toJavaValue() instanceof Map<?,?> map)map.forEach((key,raw)->{
            if(raw instanceof List<?> list)result.put(key.toString(),list.stream().map(Object::toString).toList());
        });
        return result;
    }
    static void addAliases(Map<String,List<String>> aliases,GLGraphNode node){
        read(node).forEach((source,targets)->{
            var mapped=aliases.computeIfAbsent(source,k->new ArrayList<>());
            for(String target:targets)mapped.add(node.id()+"/"+target);
        });
    }
}
