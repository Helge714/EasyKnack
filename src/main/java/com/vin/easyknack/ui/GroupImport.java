package com.vin.easyknack.ui;
import java.util.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;

/** Pure import with fresh asset, type, node, connection and template IDs. */
final class GroupImport {
    record Result(GLNodeGraphAsset selected,List<GLNodeGraphAsset> assets,List<EditorTemplates.Template> templates){}
    static Result copy(GLNodeGraphDocument source,String selectedId,String rootName){
        var plan=GLNodeGraphAssetCopy.plan(source,selectedId,"easyknack.asset.","easyknack.composite.");
        var ids=new HashMap<String,String>();
        var templates=new ArrayList<EditorTemplates.Template>();
        var value=source.root().metadata().get(EditorTemplates.KEY);
        if(value!=null)for(var raw:(List<?>)value.toJavaValue()){
            var t=EditorTemplates.Template.read(GLNodeValue.of(raw));var id=UUID.randomUUID().toString();
            ids.put(t.id(),id);ids.put(EditorTemplates.PREFIX+t.id(),EditorTemplates.PREFIX+id);
            templates.add(new EditorTemplates.Template(id,t.label(),t.entries(),t.initial()));
        }
        // Copied editors can carry a portable template without a library entry.
        for(var asset:plan.assets())if(asset.compositeDescriptor()!=null)for(var port:asset.compositeDescriptor().portMappings()){
            var t=GroupInputs.template(port);if(t==null||ids.containsKey(t.id()))continue;
            var id=UUID.randomUUID().toString();ids.put(t.id(),id);ids.put(EditorTemplates.PREFIX+t.id(),EditorTemplates.PREFIX+id);
        }
        var result=GLNodeGraphAssetCopy.copy(plan,ids,"easyknack.composite."+UUID.randomUUID(),new GLNodeGraphAssetCopy.Policy(){
            @Override public Map<String,GLNodeValue> nodeProperties(GLGraphNode node,GLNodeGraphAssetCopy.Context context){
                return rewriteMap(node.withoutProperty(GroupBakes.PROPERTY).properties(),context.ids());
            }
            @Override public Map<String,GLNodeValue> editorOptions(GLCompositePortMapping port,GLNodeGraphAssetCopy.Context context){
                var options=new LinkedHashMap<>(rewriteMap(port.presentation().editorOptions(),context.ids()));
                var copy=options.get("copySource");
                if(copy!=null&&copy.toJavaValue() instanceof String text){
                    for(var entry:context.nodeIds().entrySet())text=text.replace(entry.getKey().toString(),entry.getValue().toString());
                    options.put("copySource",GLNodeValue.of(text));
                }
                return options;
            }
            @Override public Map<String,GLNodeValue> metadata(GLNodeGraphAsset asset,GLNodeGraphAssetCopy.Context context){
                var metadata=new LinkedHashMap<>(rewriteMap(asset.metadata(),context.ids()));
                metadata.remove(GroupBakes.SAVED);metadata.remove(EditorTemplates.KEY);
                if(asset.compositeDescriptor()==null)metadata.put("easyknack.title",GLNodeValue.of(rootName));
                return metadata;
            }
            @Override public GLNodeValue value(GLNodeValue value,GLNodeGraphAssetCopy.Context context){
                return GLNodeValue.of(rewrite(value.toJavaValue(),context.ids()));
            }
        });
        return new Result(result.selected(),result.assets(),List.copyOf(templates));
    }
    private static Map<String,GLNodeValue> rewriteMap(Map<String,GLNodeValue> source,Map<String,String> ids){var out=new LinkedHashMap<String,GLNodeValue>();source.forEach((k,v)->out.put(k,GLNodeValue.of(rewrite(v.toJavaValue(),ids))));return out;}
    private static Object rewrite(Object v,Map<String,String> ids){
        if(v instanceof String s)return ids.getOrDefault(s,s);
        if(v instanceof List<?> l)return l.stream().map(x->rewrite(x,ids)).toList();
        if(v instanceof Map<?,?> m){var out=new LinkedHashMap<String,Object>();m.forEach((k,x)->{String key=k.toString();int slash=key.indexOf('/');if(slash>0&&ids.containsKey(key.substring(0,slash)))key=ids.get(key.substring(0,slash))+key.substring(slash);out.put(key,rewrite(x,ids));});return out;}
        return v;
    }
}
