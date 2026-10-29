package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Portable editor-backed inputs. Property mappings remain the fallback when no wire is connected. */
final class GroupInputs {
    static final String CHOICE="choiceProperty";
    static String property(GLCompositePortMapping port){return new NodeState(port.presentation().editorOptions()).text(CHOICE,"input-choice-"+UUID.nameUUIDFromBytes(port.publicPort().id().getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
    /** Allocate once when creating a choice editor; renaming/reusing a port name must not alias parameters. */
    static String allocateProperty(GLCompositePortMapping port){
        var existing=port.presentation().editorOptions().get(CHOICE);
        return existing==null?"input-choice-"+UUID.randomUUID():existing.toJavaValue().toString();
    }
    private static GLNodeGraphAsset uniqueChoiceProperties(GLNodeGraphAsset asset){
        var d=asset.compositeDescriptor();var used=new HashSet<String>();var ports=new ArrayList<GLCompositePortMapping>();
        for(var p:d.portMappings()){
            if(choice(p)){
                String id=property(p);
                if(!used.add(id)&&template(p)!=null){
                    int suffix=0;String replacement;
                    do{replacement="input-choice-"+UUID.nameUUIDFromBytes((asset.id()+"/choice/"+p.publicPort().id()+"/"+suffix++).getBytes(java.nio.charset.StandardCharsets.UTF_8));}while(!used.add(replacement));
                    var options=new HashMap<>(p.presentation().editorOptions());options.put(CHOICE,GLNodeValue.of(replacement));
                    p=new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),new GLCompositePortPresentation(p.presentation().inputEditorId(),p.presentation().socketVisible(),options),p.additionalEndpoints());
                }
            }ports.add(p);
        }
        return ports.equals(d.portMappings())?asset:descriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings()));
    }
    static EditorTemplates.Template template(GLCompositePortMapping port){var value=port.presentation().editorOptions().get("template");return value==null?null:EditorTemplates.Template.read(value);}
    static boolean choice(GLCompositePortMapping port){return template(port)!=null||port.presentation().editorOptions().containsKey(CHOICE);}
    static boolean editorOnly(GLCompositePortMapping port){return (choice(port)||!GroupEditorCopies.targets(port).isEmpty())&&!port.presentation().socketVisible();}
    static GLCompositePortMapping normalize(GLCompositePortMapping port,GLNodeGraphAsset asset,GLCompositeGraphRegistry groups){
        port=GroupEditorCopies.detachMissingSource(port);
        if(!GroupEditorCopies.targets(port).isEmpty())port=new GLCompositePortMapping(port.publicPort(),null,new GLCompositePortPresentation(port.presentation().inputEditorId(),false,port.presentation().editorOptions()));
        if(template(port)==null)return port;
        var options=new HashMap<>(port.presentation().editorOptions());options.put(CHOICE,GLNodeValue.of(property(port)));
        var targets=new HashMap<String,String>();var values=new HashMap<String,Object>();
        if(asset!=null)for(var endpoint:port.internalEndpoints()){
            var model=asset.graph().findNode(endpoint.nodeId()).orElse(null);
            var nested=model==null?null:groups.findComposite(model.typeId()).orElse(null);
            String id=endpoint.portId();
            if(nested!=null){var inner=nested.portMappings().stream().filter(p->p.publicPort().id().equals(endpoint.portId())).findFirst().orElse(null);if(inner!=null)id=choice(inner)?property(inner):GroupUnits.key(id);}
            targets.put(endpoint.nodeId()+"/"+endpoint.portId(),id);
            var mapping=new LinkedHashMap<String,Object>();
            for(var entry:template(port).entries())mapping.put(entry.id(),parameterValue(asset,endpoint,entry.value(),groups).toJavaValue());
            values.put(endpoint.nodeId()+"/"+endpoint.portId(),mapping);
        }
        options.put("targetProperties",GLNodeValue.of(targets));
        options.put("targetValues",GLNodeValue.of(values));
        return new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),new GLCompositePortPresentation(port.presentation().inputEditorId(),port.presentation().socketVisible(),options),port.additionalEndpoints());
    }
    static GLNodeGraphAsset materialize(GLNodeGraphAsset asset,GLCompositeGraphRegistry groups){
        var d=asset.compositeDescriptor();if(d==null)return asset;
        return materialize(descriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().map(p->normalize(p,asset,groups)).toList(),d.properties(),d.propertyMappings())));
    }
    static GLNodeGraphAsset materialize(GLNodeGraphAsset asset){
        if(asset.compositeDescriptor()==null)return asset;
        asset=uniqueChoiceProperties(asset);var d=asset.compositeDescriptor();
        if(new NodeState(asset.metadata()).flag("easyknack.inputsUnified",false)){
            var retained=d.portMappings().stream().filter(GroupInputs::choice).map(GroupInputs::property).collect(java.util.stream.Collectors.toSet());
            var removed=asset.metadata().keySet().stream().filter(k->k.startsWith(GroupChoices.META)).map(k->k.substring(GroupChoices.META.length())).filter(k->!retained.contains(k)).collect(java.util.stream.Collectors.toSet());
            if(!removed.isEmpty()){var metadata=new HashMap<>(asset.metadata());removed.forEach(k->metadata.remove(GroupChoices.META+k));d=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings(),d.properties(),d.propertyMappings().stream().filter(m->!removed.contains(m.publicPropertyId())).toList());asset=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,d);}
        }
        var result=asset;
        for(var port:d.portMappings()){
            var t=template(port);if(t==null)continue;String id=property(port);
            var maps=new ArrayList<>(result.compositeDescriptor().propertyMappings().stream().filter(m->m.publicPropertyId().equals(id)).toList());
            if(new NodeState(port.presentation().editorOptions()).flag("autoBind",false)){
                maps.clear();
                var boundTargets=new LinkedHashSet<>(port.internalEndpoints());boundTargets.addAll(GroupEditorCopies.targets(port));
                for(var target:boundTargets) {
                    if(asset.graph().findNode(target.nodeId()).isEmpty())continue;
                    String key=target.nodeId()+"/"+target.portId();
                    var targets=port.presentation().editorOptions().get("targetProperties");
                    String property=targets==null?target.portId():String.valueOf(((Map<?,?>)targets.toJavaValue()).getOrDefault(key, null));
                    if(property.equals("null"))property=target.portId();
                    String targetProperty=property;
                    if(maps.stream().noneMatch(m->m.internalNodeId().equals(target.nodeId())&&m.internalPropertyId().equals(targetProperty)))
                        maps.add(new GLCompositePropertyMapping(id,target.nodeId(),property,targetMapping(port,key,t)));
                }
            }
            result=GroupChoices.store(result,new GroupChoices.Choice(id,port.publicPort().id(),t.editor()),maps);
        }
        return GroupEditorCopies.materialize(result);
    }
    private static Map<String,GLNodeValue> targetMapping(GLCompositePortMapping port,String key,EditorTemplates.Template template){
        var tables=port.presentation().editorOptions().get("targetValues");
        if(tables==null)return template.mapping();var table=((Map<?,?>)tables.toJavaValue()).get(key);
        if(!(table instanceof Map<?,?> entries))return template.mapping();
        var result=new LinkedHashMap<String,GLNodeValue>();entries.forEach((k,v)->result.put(k.toString(),GLNodeValue.of(v)));return result;
    }
    /** Numeric template values select enum entries by index; stable enum IDs remain valid. */
    static GLNodeValue parameterValue(GLNodeGraphAsset asset,GLNodeEndpoint endpoint,GLNodeValue value,GLCompositeGraphRegistry groups){
        if(FormulaGroups.isFormula(asset)&&!(value.toJavaValue() instanceof Number)){
            for(var p:asset.compositeDescriptor().portMappings())if(p.internalEndpoints().contains(endpoint)&&template(p)!=null){
                var entries=template(p).entries();for(int i=0;i<entries.size();i++)if(entries.get(i).value().equals(value))return GLNodeValue.of(i);
            }
        }
        var combo=targetCombo(asset,endpoint,groups);if(combo==null)return value;
        if(value.toJavaValue() instanceof Number number){double index=number.doubleValue();if(index!=Math.rint(index)||index<0||index>=combo.items().size())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Combobox index outside connected editor: ")+index);return GLNodeValue.of(combo.items().get((int)index).id());}
        combo.validate(value);return value;
    }
    static double signalValue(GLNodeGraphAsset asset,GLNodeEndpoint endpoint,GLNodeValue value,GLCompositeGraphRegistry groups){
        var mapped=parameterValue(asset,endpoint,value,groups);
        var node=asset.graph().findNode(endpoint.nodeId()).orElseThrow();var nested=groups.findComposite(node.typeId()).orElse(null);
        if(nested!=null){var port=nested.portMappings().stream().filter(p->p.publicPort().id().equals(endpoint.portId())).findFirst().orElseThrow();var template=template(port);
            if(template!=null&&port.isBound()){var entry=template.entries().stream().filter(e->e.id().equals(mapped.toJavaValue())).findFirst().orElseThrow();return signalValue(groups.findAsset(nested.assetId()).orElseThrow(),port.internalEndpoint(),entry.value(),groups);}}
        if(mapped.toJavaValue() instanceof Number number)return number.doubleValue();
        var combo=targetCombo(asset,endpoint,groups);if(combo!=null)for(int i=0;i<combo.items().size();i++)if(combo.items().get(i).id().equals(mapped.toJavaValue()))return i;
        throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Connected input requires a numeric value"));
    }
    private static Editor.ComboBox<GroupChoices.Option> targetCombo(GLNodeGraphAsset asset,GLNodeEndpoint endpoint,GLCompositeGraphRegistry groups){
        var node=asset.graph().findNode(endpoint.nodeId()).orElse(null);if(node==null)return null;
        var nested=groups.findComposite(node.typeId()).orElse(null);if(nested==null)return GroupChoices.nodeEditor(node,endpoint.portId());
        var port=nested.portMappings().stream().filter(p->p.publicPort().id().equals(endpoint.portId())).findFirst().orElse(null);
        if(port==null||!choice(port))return null;var template=template(port);if(template!=null)return template.editor();
        return GroupChoices.choices(groups.findAsset(nested.assetId()).orElseThrow(),groups).stream().filter(c->c.id().equals(property(port))).map(GroupChoices.Choice::editor).findFirst().orElse(null);
    }
    static GLNodeGraphAsset includeChoiceInput(GLNodeGraphAsset asset,GroupChoices.Choice choice){
        var d=asset.compositeDescriptor();if(d.portMappings().stream().anyMatch(p->choice(p)&&property(p).equals(choice.id())))return asset;
        var names=d.portMappings().stream().map(p->p.publicPort().id()).collect(java.util.stream.Collectors.toSet());String name=choice.label();int suffix=2;while(names.contains(name))name=choice.label()+" "+suffix++;
        var port=new GLCompositePortMapping(GLNodePortDescriptor.input(name,SignalCategory.VALUE.valueTypeId(),false,1),null,new GLCompositePortPresentation("easyknack.choice",false,Map.of(CHOICE,GLNodeValue.of(choice.id()))));
        var ports=new ArrayList<>(d.portMappings());ports.add(port);
        return descriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings()));
    }
    static GLNodeGraphAsset migrate(GLNodeGraphAsset asset,GLCompositeGraphRegistry registry){
        if(asset.compositeDescriptor()==null)return asset;
        asset=materialize(asset,registry);for(var c:GroupChoices.choices(asset,registry))asset=includeChoiceInput(asset,c);
        var metadata=new HashMap<>(asset.metadata());metadata.put("easyknack.inputsUnified",GLNodeValue.of(true));
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,asset.compositeDescriptor());
    }
    static GLNodeGraphAsset applyTemplate(GLNodeGraphAsset asset,GLCompositePortMapping port,EditorTemplates.Template template){return applyTemplate(asset,port,template,null);}
    static GLNodeGraphAsset applyTemplate(GLNodeGraphAsset asset,GLCompositePortMapping port,EditorTemplates.Template template,GLCompositeGraphRegistry registry){
        var options=new HashMap<String,GLNodeValue>();options.put("template",template.value());options.put("autoBind",GLNodeValue.of(true));options.put(CHOICE,GLNodeValue.of(allocateProperty(port)));
        for(String key:List.of(FormulaGroups.ID,FormulaGroups.NAME))if(port.presentation().editorOptions().containsKey(key))options.put(key,port.presentation().editorOptions().get(key));
        var presentation=new GLCompositePortPresentation(EditorTemplates.PREFIX+template.id(),port.presentation().socketVisible(),options);
        var replacement=new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),presentation,port.additionalEndpoints());
        var applied=registry==null?replacement:normalize(replacement,asset,registry);
        var d=asset.compositeDescriptor();return materialize(descriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().map(p->p.publicPort().id().equals(port.publicPort().id())?applied:p).toList(),d.properties(),d.propertyMappings().stream().filter(m->!m.publicPropertyId().equals(property(port))).toList())));
    }
    static GLNodeGraphAsset descriptor(GLNodeGraphAsset a,GLCompositeNodeDescriptor d){return new GLNodeGraphAsset(a.id(),a.version(),a.kind(),a.graph(),a.viewState(),a.metadata(),d);}
}
