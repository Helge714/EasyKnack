package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Editor snapshots and explicit coupling of non-port properties. */
final class GroupEditorCopies {
    static final String TARGETS="easyknack.editor.targets";
    static final String SOURCE_TYPE="easyknack.editor.sourceType", SOURCE_PROPERTY="easyknack.editor.sourceProperty";
    private static final String PROPERTIES="easyknack.editor.boundProperties";
    static List<GLNodeEndpoint> targets(GLCompositePortMapping p){
        var raw=p.presentation().editorOptions().get(TARGETS);if(raw==null)return List.of();
        return ((List<?>)raw.toJavaValue()).stream().map(v->{var m=(Map<?,?>)v;return new GLNodeEndpoint(new GLNodeId(UUID.fromString((String)m.get("node"))),(String)m.get("property"));}).toList();
    }
    static GLNodeValue targetsValue(List<GLNodeEndpoint> targets){return GLNodeValue.of(targets.stream().map(e->Map.of("node",e.nodeId().toString(),"property",e.portId())).toList());}
    static PropertyId controlledKey(PropertyId property){
        String key=property.value();if(key.startsWith(GLGraphNode.AUTOMATIC_PREFIX))key=key.substring(GLGraphNode.AUTOMATIC_PREFIX.length());
        if(key.endsWith(".unit"))key=key.substring(0,key.length()-5);return new PropertyId(key);
    }
    static boolean supported(Editor e){return e instanceof VectorEditor||e instanceof Editor.Slider||e instanceof UnitSlider||e instanceof Editor.ComboBox<?>||e instanceof Editor.CheckBox||e instanceof Editor.Toggle||e instanceof Editor.Trigger;}
    static boolean linkable(NodeBuilder.Row row){return GroupEditorCapabilities.of(row).linkable();}
    /** Public semantic category and requested rate, not the host's storage type/resolved rate. */
    static GLCompositePortMapping inheritPort(GLCompositePortMapping p,GLNodeEndpoint endpoint,GLNodeGraphAsset asset,GLCompositeGraphRegistry groups){
        var node=asset.graph().findNode(endpoint.nodeId()).orElseThrow();var definition=NodeRegistry.find(node.typeId()).orElse(null);
        var copied=p.publicPort().direction()==GLPortDirection.INPUT?copy(p,endpoint,asset,groups):p;
        String type=p.publicPort().valueTypeId();var options=new HashMap<>(copied.presentation().editorOptions());
        if(definition!=null){
            var row=definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.id().equals(endpoint.portId())).findFirst().orElseThrow();
            type=row.signal().valueTypeId();options.put(GroupInputSettings.RATE,GLNodeValue.of((row.adaptive()?ProcessingRate.AUTO:row.rate()).id()));
            if(row.editor()!=null&&row.isEditorCopyable()){var mode=row.editor() instanceof VectorEditor?GroupInputSettings.Kind.COPY:row.editor() instanceof Editor.Trigger?GroupInputSettings.Kind.BUTTON:row.editor() instanceof Editor.ComboBox<?>?GroupInputSettings.Kind.COMBO:row.editor() instanceof Editor.CheckBox||row.editor() instanceof Editor.Toggle?GroupInputSettings.Kind.CHECKBOX:GroupInputSettings.Kind.SLIDER;options.put(GroupInputSettings.MODE,GLNodeValue.of(mode.name()));}
        }else{
            var nested=groups.findComposite(node.typeId()).orElse(null);
            if(nested!=null){var port=nested.portMappings().stream().filter(v->v.publicPort().id().equals(endpoint.portId())).findFirst().orElseThrow();type=port.publicPort().valueTypeId();options.put(GroupInputSettings.RATE,GLNodeValue.of(GroupInputSettings.selectedRate(port).id()));}
        }
        var port=p.publicPort();return new GLCompositePortMapping(new GLNodePortDescriptor(port.id(),port.direction(),type,port.timing(),port.required(),port.maximumConnections()),copied.internalEndpoint(),new GLCompositePortPresentation(copied.presentation().inputEditorId(),copied.presentation().socketVisible(),options),copied.additionalEndpoints());
    }
    static GLCompositePortMapping detachMissingSource(GLCompositePortMapping p){
        var source=new NodeState(p.presentation().editorOptions()).text("copySource","");
        if(source.isEmpty()||!targets(p).isEmpty()||p.internalEndpoints().stream().anyMatch(e->e.toString().equals(source)))return p;
        var options=new HashMap<String,GLNodeValue>();
        for(var key:List.of(GroupInputSettings.RATE,FormulaGroups.ID,FormulaGroups.NAME))if(p.presentation().editorOptions().containsKey(key))options.put(key,p.presentation().editorOptions().get(key));
        options.put(GroupInputSettings.MODE,GLNodeValue.of(GroupInputSettings.Kind.NONE.name()));
        return new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),new GLCompositePortPresentation(GLNodeInputEditorRegistry.NONE,true,options),p.additionalEndpoints());
    }
    /** View-only projection: originals return automatically when the coupling is removed. */
    static GLGraphNode previewDefaults(GLNodeGraphAsset asset,GLGraphNode node){
        if(asset==null||asset.compositeDescriptor()==null)return node;
        var result=node;
        for(var p:asset.compositeDescriptor().portMappings())for(var endpoint:targets(p))if(endpoint.nodeId().equals(node.id())){
            var definition=NodeRegistry.find(node.typeId()).orElse(null);if(definition==null)continue;
            var row=definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.id().equals(endpoint.portId())).findFirst().orElse(null);if(row==null)continue;
            var template=GroupInputs.template(p);GLNodeValue value;
            if(template!=null)value=template.entries().stream().filter(e->e.id().equals(template.initial())).findFirst().orElseThrow().value();
            else if(row.editor() instanceof Editor.Trigger)continue;
            else if(row.editor() instanceof VectorEditor)value=p.presentation().editorOptions().get("default");
            else{double number=new NodeState(p.presentation().editorOptions()).number("default",0);value=row.editor() instanceof Editor.CheckBox?GLNodeValue.of(number!=0):GLNodeValue.of(number);}
            result=result.withExplicitProperty(endpoint.portId(),value);
        }return result;
    }
    static GLNodeGraphAsset materialize(GLNodeGraphAsset asset){
        var d=asset.compositeDescriptor();if(d==null)return asset;
        var properties=new ArrayList<>(d.properties());var mappings=new ArrayList<>(d.propertyMappings());
        // Rebuild our own mappings so uncoupling and deleting a target cannot leave stale bindings.
        var owned=new HashSet<String>();var previous=asset.metadata().get(PROPERTIES);
        if(previous!=null)for(var key:(List<?>)previous.toJavaValue())owned.add(key.toString());
        d.portMappings().stream().filter(p->!targets(p).isEmpty()).map(p->GroupUnits.key(p.publicPort().id())).forEach(owned::add);
        mappings.removeIf(m->owned.contains(m.publicPropertyId()));properties.removeIf(p->owned.contains(p.id()));
        var current=new ArrayList<String>();
        for(var p:d.portMappings()){
            var targets=targets(p);if(targets.isEmpty()||GroupInputs.choice(p)||p.presentation().inputEditorId().equals(GroupInputSettings.BUTTON))continue;
            String key=GroupUnits.key(p.publicPort().id());
            current.add(key);
            boolean vector=GroupVectorEditors.isVector(p);
            var fallback=vector?p.presentation().editorOptions().get("default"):GLNodeValue.of(new NodeState(p.presentation().editorOptions()).number("default",0));
            properties.add(new GLCompositePropertyDescriptor(key,vector?"vector":"number",vector?GLCompositeValueKind.LIST:GLCompositeValueKind.DECIMAL,fallback));
            for(var endpoint:targets){
                var node=asset.graph().findNode(endpoint.nodeId()).orElse(null);if(node==null)continue;
                var definition=NodeRegistry.find(node.typeId()).orElse(null);if(definition==null)continue;
                var row=definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.id().equals(endpoint.portId())).findFirst().orElse(null);
                if(row==null)continue;
                Map<String,GLNodeValue> values=row.editor() instanceof Editor.CheckBox
                    ?Map.of("0.0",GLNodeValue.of(false),"1.0",GLNodeValue.of(true),"0",GLNodeValue.of(false),"1",GLNodeValue.of(true)):Map.of();
                mappings.add(new GLCompositePropertyMapping(key,node.id(),endpoint.portId(),values));
            }
        }
        var metadata=new HashMap<>(asset.metadata());if(current.isEmpty())metadata.remove(PROPERTIES);else metadata.put(PROPERTIES,GLNodeValue.of(current));
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings(),properties,mappings));
    }
    static GLCompositePortMapping copy(GLCompositePortMapping p,GLNodeEndpoint endpoint,GLNodeGraphAsset asset,GLCompositeGraphRegistry groups){
        var n=asset.graph().findNode(endpoint.nodeId()).orElseThrow();var def=NodeRegistry.find(n.typeId()).orElse(null);
        if(def==null){
            var nested=groups.findComposite(n.typeId()).orElse(null);if(nested==null)return p;
            var child=nested.portMappings().stream().filter(v->v.publicPort().id().equals(endpoint.portId())).findFirst().orElse(null);if(child==null)return p;
            child=MultipleGroups.exposedPort(child);
            var options=new HashMap<>(child.presentation().editorOptions());options.remove(TARGETS);
            options.put(GroupInputSettings.MODE,GLNodeValue.of(GroupInputSettings.Kind.COPY.name()));options.put("copySource",GLNodeValue.of(endpoint.toString()));
            if(GroupInputs.choice(child))options.put(GroupInputs.CHOICE,GLNodeValue.of(GroupInputs.property(p)));
            return new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),new GLCompositePortPresentation(child.presentation().inputEditorId(),p.presentation().socketVisible(),options),p.additionalEndpoints());
        }
        var row=def.layout(new NodeState(n.properties())).rows().stream().filter(r->r.id().equals(endpoint.portId())).findFirst().orElse(null);
        if(row==null||!row.isEditorCopyable()||!supported(row.editor()))return p;
        var opts=new HashMap<String,GLNodeValue>();String editor=GLNodeInputEditorRegistry.SLIDER;
        opts.put(GroupInputSettings.MODE,GLNodeValue.of(GroupInputSettings.Kind.COPY.name()));opts.put("copySource",GLNodeValue.of(endpoint.toString()));opts.put(GroupInputSettings.RATE,GLNodeValue.of((row.adaptive()?ProcessingRate.AUTO:row.rate()).id()));
        opts.put(SOURCE_TYPE,GLNodeValue.of(n.typeId()));opts.put(SOURCE_PROPERTY,GLNodeValue.of(row.id()));
        var snapshot=GroupEditorCapabilities.snapshot(row,n);
        editor=snapshot.editorId();opts.putAll(snapshot.capabilities().options());
        if(snapshot.capabilities().kind()==GLNodeEditorCapabilities.Kind.CHOICE)opts.put(GroupInputs.CHOICE,GLNodeValue.of(GroupInputs.property(p)));
        boolean coupled=row.kind()==NodeBuilder.RowKind.EDITOR;
        if(coupled)opts.put(TARGETS,targetsValue(List.of(endpoint)));
        return new GLCompositePortMapping(p.publicPort(),coupled?null:p.internalEndpoint(),new GLCompositePortPresentation(editor,!coupled&&p.presentation().socketVisible(),opts),coupled?List.of():p.additionalEndpoints());
    }
}
