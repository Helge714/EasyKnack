package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

/** Stable combobox selections map to internal parameter values through Core property mappings. */
final class GroupChoices {
    record Option(String id,String label){public String toString(){return label;}}
    static final String META="easyknack.editors.";
    record Choice(String id,String label,Editor.ComboBox<Option> editor){Choice(String id,Editor.ComboBox<Option> editor){this(id,id,editor);}public String toString(){return label;}}
    static final class Control extends com.vingame.glswing.ui.GLComboBox<Option>{
        private boolean updating;
        Control(Editor.ComboBox<Option> editor,GLNodeValue value,java.util.function.Consumer<GLNodeValue> changed){super(editor.items());sync(value);addActionListener(e->{if(!updating&&getSelectedItem()!=null)changed.accept(GLNodeValue.of(getSelectedItem().id()));});}
        void sync(GLNodeValue value){updating=true;try{getItems().stream().filter(o->o.id().equals(value.toJavaValue())).findFirst().ifPresent(this::setSelectedItem);}finally{updating=false;}}
    }
    private static final Map<GLNodePropertySchemaRegistry,Map<String,Set<String>>> registered=new WeakHashMap<>();
    private static final Map<GLNodePropertySchemaRegistry,Map<String,String>> inspectorKeys=new WeakHashMap<>();
    static void registerInspector(GLNodePropertySchemaRegistry schemas,GLCompositeGraphRegistry registry){
        for(var descriptor:registry.composites()){
            var choices=choices(registry.findAsset(descriptor.assetId()).orElseThrow(),registry);
            String key=choices.stream().map(c->c.id()+c.label()+c.editor().items()+c.editor().defaultValue()).toList().toString();
            var keys=inspectorKeys.computeIfAbsent(schemas,k->new HashMap<>());
            if(key.equals(keys.get(descriptor.nodeTypeId())))continue;
            keys.put(descriptor.nodeTypeId(),key);
            var properties=new ArrayList<>(schemas.properties(descriptor.nodeTypeId()));
            var previous=registered.computeIfAbsent(schemas,k->new HashMap<>());
            var old=previous.getOrDefault(descriptor.nodeTypeId(),Set.of());properties.removeIf(p->old.contains(p.id()));
            var ids=new HashSet<String>();
            for(var choice:choices){
                ids.add(choice.id());properties.removeIf(p->p.id().equals(choice.id()));
                properties.add(new GLCustomNodeProperty(choice.id(),choice.label(),
                    (node,changed)->new Control(choice.editor(),selection(registry.findAsset(descriptor.assetId()).orElseThrow(),choice,node.properties().getOrDefault(choice.id(),choice.editor().defaultValue())),changed),
                    (control,node)->((Control)control).sync(selection(registry.findAsset(descriptor.assetId()).orElseThrow(),choice,node.properties().getOrDefault(choice.id(),choice.editor().defaultValue())))));
            }
            previous.put(descriptor.nodeTypeId(),Set.copyOf(ids));schemas.register(descriptor.nodeTypeId(),properties);
        }
    }
    record Candidate(GLNodeId node,String property,String label,Editor.ComboBox<Option> editor){public String toString(){return label;}}
    static <T> Editor.ComboBox<Option> copy(Editor.ComboBox<T> source,GLNodeValue initial){
        var options=source.items().stream().map(v->new Option(source.id().apply(v),v.toString())).toList();
        var selected=options.stream().filter(v->v.id().equals(initial.toJavaValue())).findFirst().orElseThrow(()->new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unknown group choice: ")+initial));
        return Editor.comboBox(options,selected,Option::id);
    }
    static List<Choice> choices(GLNodeGraphAsset asset,GLCompositeGraphRegistry registry){
        asset=GroupInputs.materialize(asset,registry);
        if(asset.compositeDescriptor()==null)return List.of();
        var result=new ArrayList<Choice>();
        for(var p:asset.compositeDescriptor().properties()){
            var stored=asset.metadata().get(META+p.id());
            if(stored!=null){
                var data=(Map<?,?>)stored.toJavaValue();var options=new ArrayList<Option>();
                for(var raw:(List<?>)data.get("entries")){var entry=(Map<?,?>)raw;options.add(new Option((String)entry.get("id"),(String)entry.get("label")));}
                if(options.isEmpty())continue;
                var initial=options.stream().filter(o->o.id().equals(p.defaultValue().toJavaValue())).findFirst().orElse(options.get(0));
                result.add(new Choice(p.id(),(String)data.get("label"),Editor.comboBox(options,initial,Option::id)));continue;
            }
            var editor=resolve(asset,p.id(),registry,new HashSet<>());
            if(editor!=null)result.add(new Choice(p.id(),copy(editor,p.defaultValue())));
        }return result;
    }
    static GLNodeGraphAsset store(GLNodeGraphAsset asset,Choice choice,List<GLCompositePropertyMapping> bindings){
        if(choice.editor.items().isEmpty())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("A combobox needs at least one entry"));
        var metadata=new HashMap<>(asset.metadata());metadata.put(META+choice.id(),GLNodeValue.of(Map.of("label",choice.label(),"entries",choice.editor.items().stream().map(o->Map.of("id",o.id(),"label",o.label())).toList())));
        var d=asset.compositeDescriptor();var properties=new ArrayList<>(d.properties());int position=java.util.stream.IntStream.range(0,properties.size()).filter(i->properties.get(i).id().equals(choice.id())).findFirst().orElse(properties.size());properties.removeIf(p->p.id().equals(choice.id()));
        properties.add(position,new GLCompositePropertyDescriptor(choice.id(),"text",GLCompositeValueKind.TEXT,choice.editor.defaultValue()));
        var mappings=new ArrayList<>(d.propertyMappings());mappings.removeIf(m->m.publicPropertyId().equals(choice.id()));mappings.addAll(bindings);
        return GroupInputs.includeChoiceInput(new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings(),properties,mappings)),choice);
    }
    static GLNodeValue selection(GLNodeGraphAsset asset,Choice choice,GLNodeValue value){
        if(choice.editor().items().stream().noneMatch(o->o.id().equals(value.toJavaValue())))return choice.editor().defaultValue();
        return value;
    }
    static GLNodeGraphAsset normalizeSelections(GLNodeGraphAsset asset,GLCompositeGraphRegistry registry){
        var nodes=asset.graph().nodes().stream().map(node->{var d=registry.findComposite(node.typeId()).orElse(null);if(d==null)return node;var source=registry.findAsset(d.assetId()).orElseThrow();
            var choices=choices(source,registry);node=restoreRetiredSelection(node,source,choices);
            for(var c:choices){var v=node.properties().get(c.id());if(v!=null)node=node.withProperty(c.id(),selection(source,c,v));}return node;
        }).toList();
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(asset.graph().graphId(),asset.graph().revision(),nodes,asset.graph().connections()),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
    }
    private static GLGraphNode restoreRetiredSelection(GLGraphNode node,GLNodeGraphAsset source,List<Choice> choices){
        if(choices.isEmpty()||source.compositeDescriptor()==null)return node;
        // Custom template entries have stable UUIDs. Recover an orphaned explicit
        // selection only when its entry identifies exactly one current editor.
        // Never infer a link from common numeric/native IDs or overwrite a choice.
        var proposals=new HashMap<String,Set<GLNodeValue>>();
        for(var entry:node.properties().entrySet()){
            String key=entry.getKey();
            if(!key.startsWith("input-choice-")||source.compositeDescriptor().findProperty(key).isPresent()
                ||GLNodeValue.of(true).equals(node.properties().get(GLGraphNode.AUTOMATIC_PREFIX+key)))continue;
            if(!(entry.getValue().toJavaValue() instanceof String id)||!id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))continue;
            var matching=choices.stream().filter(c->c.editor().items().stream().anyMatch(o->o.id().equals(id))).toList();
            if(matching.size()==1)proposals.computeIfAbsent(matching.get(0).id(),k->new HashSet<>()).add(entry.getValue());
        }
        for(var proposal:proposals.entrySet())if(proposal.getValue().size()==1
            &&(!node.properties().containsKey(proposal.getKey())||GLNodeValue.of(true).equals(node.properties().get(GLGraphNode.AUTOMATIC_PREFIX+proposal.getKey()))))
            node=node.withExplicitProperty(proposal.getKey(),proposal.getValue().iterator().next());
        return node;
    }
    static GLNodeGraphAsset remove(GLNodeGraphAsset asset,String id){
        var metadata=new HashMap<>(asset.metadata());metadata.remove(META+id);var d=asset.compositeDescriptor();
        // Retain an inert descriptor for existing instances and undo history; no editor or DSP mapping remains.
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings().stream().filter(p->!GroupInputs.choice(p)||!GroupInputs.property(p).equals(id)).toList(),d.properties(),d.propertyMappings().stream().filter(m->!m.publicPropertyId().equals(id)).toList()));
    }
    static GLNodeGraphAsset move(GLNodeGraphAsset asset,String id,int delta){
        var d=asset.compositeDescriptor();var properties=new ArrayList<>(d.properties());int index=0;while(index<properties.size()&&!properties.get(index).id().equals(id))index++;
        if(index<properties.size()&&index+delta>=0&&index+delta<properties.size())Collections.swap(properties,index,index+delta);
        return withDescriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings(),properties,d.propertyMappings()));
    }
    static List<Candidate> bindingCandidates(GLNodeGraphAsset asset,GLCompositeGraphRegistry registry){
        var result=new ArrayList<Candidate>();
        for(var node:asset.graph().nodes()){
            var nested=registry.findComposite(node.typeId()).orElse(null);
            if(nested!=null){for(var c:choices(registry.findAsset(nested.assetId()).orElseThrow(),registry))result.add(new Candidate(node.id(),c.id(),c.label(),c.editor()));continue;}
            NodeRegistry.find(node.typeId()).ifPresent(d->{for(var row:d.layout(new NodeState(node.properties())).rows()){
                var combo=nodeEditor(node,row.id());
                if(combo!=null||row.editor() instanceof Editor.Slider||row.editor() instanceof UnitSlider)
                    result.add(new Candidate(node.id(),row.id(),d.title()+" / "+row.label()+" ("+node.id().toString().substring(0,8)+")",combo));
            }});
        }return result;
    }
    private static Editor.ComboBox<Option> resolve(GLNodeGraphAsset asset,String property,GLCompositeGraphRegistry registry,Set<String> visited){
        if(!visited.add(asset.id()+"/"+property))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Recursive group choice"));
        var stored=asset.metadata().get(META+property);
        if(stored!=null){var data=(Map<?,?>)stored.toJavaValue();var options=new ArrayList<Option>();for(var raw:(List<?>)data.get("entries")){var entry=(Map<?,?>)raw;options.add(new Option((String)entry.get("id"),(String)entry.get("label")));}
            if(options.isEmpty())return null;var initial=asset.compositeDescriptor().findProperty(property).orElseThrow().defaultValue();return Editor.comboBox(options,options.stream().filter(o->o.id().equals(initial.toJavaValue())).findFirst().orElse(options.get(0)),Option::id);
        }
        Editor.ComboBox<Option> result=null;
        for(var binding:asset.compositeDescriptor().propertyMappings())if(binding.publicPropertyId().equals(property)){
            var node=asset.graph().findNode(binding.internalNodeId()).orElseThrow();
            var nested=registry.findComposite(node.typeId()).orElse(null);
            var candidate=nested==null?nodeEditor(node,binding.internalPropertyId()):resolve(registry.findAsset(nested.assetId()).orElseThrow(),binding.internalPropertyId(),registry,new HashSet<>(visited));
            if(candidate==null)return null;
            if(result!=null&&!result.items().equals(candidate.items()))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Mapped group choices must have identical options: ")+property);
            result=candidate;
        }return result;
    }
    static Editor.ComboBox<Option> nodeEditor(GLGraphNode node,String property){
        var definition=NodeRegistry.find(node.typeId()).orElse(null);if(definition==null)return null;
        var state=new NodeState(node.properties());var layout=definition.layout(state);
        var row=layout.rows().stream().filter(r->r.id().equals(property)).findFirst().orElse(null);
        if(row==null||!(row.editor() instanceof Editor.ComboBox<?> combo))return null;
        if(definition.processor().contract().fields().stream().noneMatch(f->f.kind()==ParameterContract.Kind.VALUE&&f.id().value().equals(property)))return null;
        // Structural choices (e.g. changing a Math node's input count or processing rate) need a different graph variant.
        for(var item:copy(combo,combo.defaultValue()).items()){
            var values=new HashMap<>(node.properties());values.put(property,GLNodeValue.of(item.id()));
            var other=definition.layout(new NodeState(values));
            if(!layout.ports().equals(other.ports())||!layout.rows().stream().map(r->r.id()+":"+r.rate()).toList().equals(other.rows().stream().map(r->r.id()+":"+r.rate()).toList()))return null;
        }
        return copy(combo,node.properties().getOrDefault(property,combo.defaultValue()));
    }
    static List<Candidate> candidates(GLNodeGraphAsset asset,GLCompositeGraphRegistry registry){
        var result=new ArrayList<Candidate>();
        for(var node:asset.graph().nodes()){
            var nested=registry.findComposite(node.typeId()).orElse(null);
            if(nested!=null){for(var choice:choices(registry.findAsset(nested.assetId()).orElseThrow(),registry))result.add(new Candidate(node.id(),choice.id(),nested.nodeTypeId()+" / "+choice.id(),choice.editor()));}
            else NodeRegistry.find(node.typeId()).ifPresent(d->{for(var row:d.layout(new NodeState(node.properties())).rows()){
                var editor=nodeEditor(node,row.id());if(editor!=null)result.add(new Candidate(node.id(),row.id(),d.title()+" / "+row.label()+" ("+node.id().toString().substring(0,8)+")",editor));
            }});
        }
        result.removeIf(c->asset.compositeDescriptor().propertyMappings().stream().anyMatch(m->m.internalNodeId().equals(c.node())&&m.internalPropertyId().equals(c.property())));
        return result;
    }
    static GLNodeGraphAsset expose(GLNodeGraphAsset asset,Candidate candidate,String id){
        var d=asset.compositeDescriptor();var props=new ArrayList<>(d.properties());var mappings=new ArrayList<>(d.propertyMappings());
        props.add(new GLCompositePropertyDescriptor(id,"text",GLCompositeValueKind.TEXT,candidate.editor().defaultValue()));
        mappings.add(new GLCompositePropertyMapping(id,candidate.node(),candidate.property()));
        return GroupInputs.includeChoiceInput(withDescriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),d.portMappings(),props,mappings)),new Choice(id,id,candidate.editor()));
    }
    private static GLNodeGraphAsset withDescriptor(GLNodeGraphAsset a,GLCompositeNodeDescriptor d){return new GLNodeGraphAsset(a.id(),a.version(),a.kind(),a.graph(),a.viewState(),a.metadata(),d);}
}
