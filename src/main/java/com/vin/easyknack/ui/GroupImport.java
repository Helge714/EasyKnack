package com.vin.easyknack.ui;
import java.util.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;

/** Pure import with fresh asset, type, node, connection and template IDs. */
final class GroupImport {
    record Result(GLNodeGraphAsset selected,List<GLNodeGraphAsset> assets,List<EditorTemplates.Template> templates){}
    static Result copy(GLNodeGraphDocument source,String selectedId,String rootName){
        var selected=source.findAsset(selectedId).orElseThrow();
        var byType=new HashMap<String,GLNodeGraphAsset>();
        for(var asset:NodeDocuments.assets(source))if(asset.compositeDescriptor()!=null)byType.put(asset.compositeDescriptor().nodeTypeId(),asset);
        var needed=new LinkedHashMap<String,GLNodeGraphAsset>();collect(selected,byType,needed);
        var ids=new HashMap<String,String>();
        for(var asset:needed.values()){ids.put(asset.id(),"easyknack.asset."+UUID.randomUUID());if(asset.compositeDescriptor()!=null)ids.put(asset.compositeDescriptor().nodeTypeId(),"easyknack.composite."+UUID.randomUUID());}
        var templates=new ArrayList<EditorTemplates.Template>();
        var value=source.root().metadata().get(EditorTemplates.KEY);
        if(value!=null)for(var raw:(List<?>)value.toJavaValue()){
            var t=EditorTemplates.Template.read(GLNodeValue.of(raw));var id=UUID.randomUUID().toString();
            ids.put(t.id(),id);ids.put(EditorTemplates.PREFIX+t.id(),EditorTemplates.PREFIX+id);
            templates.add(new EditorTemplates.Template(id,t.label(),t.entries(),t.initial()));
        }
        // Copied editors can carry a portable template without a library entry.
        for(var asset:needed.values())if(asset.compositeDescriptor()!=null)for(var port:asset.compositeDescriptor().portMappings()){
            var t=GroupInputs.template(port);if(t==null||ids.containsKey(t.id()))continue;
            var id=UUID.randomUUID().toString();ids.put(t.id(),id);ids.put(EditorTemplates.PREFIX+t.id(),EditorTemplates.PREFIX+id);
        }
        var result=new ArrayList<GLNodeGraphAsset>();
        for(var asset:needed.values()){
            var graph=asset.graph();
            var nodeIds=new HashMap<GLNodeId,GLNodeId>();graph.nodes().forEach(n->nodeIds.put(n.id(),GLNodeId.random()));
            var localIds=new HashMap<>(ids);nodeIds.forEach((oldId,newId)->localIds.put(oldId.toString(),newId.toString()));
            var nodes=graph.nodes().stream().map(n->new GLGraphNode(nodeIds.get(n.id()),ids.getOrDefault(n.typeId(),n.typeId()),n.typeVersion(),rewriteMap(n.withoutProperty(GroupBakes.PROPERTY).properties(),localIds))).toList();
            java.util.function.Function<GLNodeEndpoint,GLNodeEndpoint> endpoint=e->e==null?null:new GLNodeEndpoint(nodeIds.get(e.nodeId()),e.portId());
            var descriptor=asset.compositeDescriptor();
            if(descriptor==null)descriptor=new GLCompositeNodeDescriptor("easyknack.composite."+UUID.randomUUID(),ids.get(asset.id()),1,List.of());
            else descriptor=new GLCompositeNodeDescriptor(ids.get(descriptor.nodeTypeId()),ids.get(asset.id()),descriptor.minimumAssetVersion(),descriptor.portMappings().stream().map(p->{
                var options=new LinkedHashMap<>(rewriteMap(p.presentation().editorOptions(),localIds));
                var copy=options.get("copySource");if(copy!=null&&copy.toJavaValue() instanceof String text){for(var entry:nodeIds.entrySet())text=text.replace(entry.getKey().toString(),entry.getValue().toString());options.put("copySource",GLNodeValue.of(text));}
                return new GLCompositePortMapping(p.publicPort(),endpoint.apply(p.internalEndpoint()),new GLCompositePortPresentation(ids.getOrDefault(p.presentation().inputEditorId(),p.presentation().inputEditorId()),p.presentation().socketVisible(),options),p.additionalEndpoints().stream().map(endpoint).toList());
            }).toList(),descriptor.properties(),descriptor.propertyMappings().stream().map(p->new GLCompositePropertyMapping(p.publicPropertyId(),nodeIds.get(p.internalNodeId()),p.internalPropertyId(),p.values())).toList());
            var metadata=new LinkedHashMap<>(rewriteMap(asset.metadata(),localIds));metadata.remove(GroupBakes.SAVED);metadata.remove(EditorTemplates.KEY);
            if(asset.compositeDescriptor()==null)metadata.put("easyknack.title",GLNodeValue.of(rootName));
            var positions=new LinkedHashMap<GLNodeId,com.vingame.glswing.node.binding.GLNodeViewState>();asset.viewState().nodes().forEach((oldId,state)->{if(nodeIds.containsKey(oldId))positions.put(nodeIds.get(oldId),state);});
            var wires=graph.connections().stream().map(c->new GLGraphConnection(GLNodeConnectionId.random(),endpoint.apply(c.output()),endpoint.apply(c.input()))).toList();
            result.add(new GLNodeGraphAsset(ids.get(asset.id()),asset.version(),GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraphSnapshot(GLNodeGraphId.random(),0,nodes,wires),new com.vingame.glswing.node.binding.GLNodeEditorViewState(positions,asset.viewState().auxiliaryNodes()),metadata,descriptor));
        }
        return new Result(result.stream().filter(a->a.id().equals(ids.get(selectedId))).findFirst().orElseThrow(),List.copyOf(result),List.copyOf(templates));
    }
    private static void collect(GLNodeGraphAsset a,Map<String,GLNodeGraphAsset> types,Map<String,GLNodeGraphAsset> result){if(result.putIfAbsent(a.id(),a)!=null)return;for(var node:a.graph().nodes()){var child=types.get(node.typeId());if(child!=null)collect(child,types,result);}}
    private static Map<String,GLNodeValue> rewriteMap(Map<String,GLNodeValue> source,Map<String,String> ids){var out=new LinkedHashMap<String,GLNodeValue>();source.forEach((k,v)->out.put(k,GLNodeValue.of(rewrite(v.toJavaValue(),ids))));return out;}
    private static Object rewrite(Object v,Map<String,String> ids){
        if(v instanceof String s)return ids.getOrDefault(s,s);
        if(v instanceof List<?> l)return l.stream().map(x->rewrite(x,ids)).toList();
        if(v instanceof Map<?,?> m){var out=new LinkedHashMap<String,Object>();m.forEach((k,x)->{String key=k.toString();int slash=key.indexOf('/');if(slash>0&&ids.containsKey(key.substring(0,slash)))key=ids.get(key.substring(0,slash))+key.substring(slash);out.put(key,rewrite(x,ids));});return out;}
        return v;
    }
}
