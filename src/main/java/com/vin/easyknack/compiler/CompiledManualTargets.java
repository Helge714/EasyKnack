package com.vin.easyknack.compiler;
import com.vin.easyknack.i18n.AppText;
import java.util.*;
import java.util.function.Function;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.GLCompositeFlattenResult;
import com.vingame.glswing.node.model.*;

/** Keeps inner manual controls and representative readouts addressable after compilation. */
public final class CompiledManualTargets {
    public static final String PROPERTY="easyknack.compiledManualTargets";
    public static Map<String,List<String>> collect(List<GLGraphNode> nodes,GLCompositeFlattenResult flat,Function<GLGraphNode,String> target){
        var result=new LinkedHashMap<String,List<String>>();
        for(var node:nodes){
            var d=NodeRegistry.require(node.typeId());
            var paths=new LinkedHashMap<String,List<String>>();
            read(node).forEach((key,values)->paths.put(key,new ArrayList<>(values)));
            if(d instanceof StepSwitchNode||d.layout(new NodeState(node.properties())).rows().stream().anyMatch(r->r.editor() instanceof Editor.Trigger)){
                var origin=flat.origins().get(node.id());String source=(origin==null?node.id():origin.sourceNodeId()).toString();
                paths.computeIfAbsent(source,k->new ArrayList<>()).add("");
            }
            paths.forEach((key,values)->{
                for(var suffix:values){
                    String source=key;
                    String destination=target.apply(node)+(suffix.isEmpty()?"":"/"+suffix);
                    result.computeIfAbsent(source,k->new ArrayList<>()).add(destination);
                    var origin=flat.origins().get(node.id());
                    if(origin!=null)for(int i=origin.instances().size()-1;i>=0;i--){
                        source=origin.instances().get(i).nodeId()+"/"+source;
                        result.computeIfAbsent(source,k->new ArrayList<>()).add(destination);
                    }
                }
            });
        }
        return result;
    }
    public static Map<String,List<String>> read(GLGraphNode node){
        var value=node.properties().get(PROPERTY);if(value==null)return Map.of();
        var result=new LinkedHashMap<String,List<String>>();
        if(value.toJavaValue() instanceof Map<?,?> map)map.forEach((key,raw)->{
            if(raw instanceof List<?> list)result.put(key.toString(),list.stream().map(Object::toString).toList());
        });
        return result;
    }
    public static void addAliases(Map<String,List<String>> aliases,GLGraphNode node){
        read(node).forEach((source,targets)->{
            var mapped=aliases.computeIfAbsent(source,k->new ArrayList<>());
            for(String target:targets)mapped.add(node.id()+"/"+target);
        });
    }
}
