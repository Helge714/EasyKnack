package com.vin.easyknack.ui;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Inserts unit adapters at public input boundaries before the Core flattens groups. */
final class GroupCompiler {
    record Prepared(GLNodeGraphAsset root,GLCompositeGraphRegistry registry){}
    private record Adapter(GLCompositePortMapping port,UnitFormat base,String valueKey,String unitKey){}
    static Prepared prepare(GLNodeGraphAsset root,GLCompositeGraphRegistry original){
        return prepare(root,original,true);
    }
    static Prepared prepareUngroup(GLNodeGraphAsset root,GLCompositeGraphRegistry original){return prepare(root,original,false);}
    private static Prepared prepare(GLNodeGraphAsset root,GLCompositeGraphRegistry original,boolean rewriteNested){
        Map<String,List<Adapter>> adapters=new HashMap<>();
        Map<String,GLNodeGraphAsset> expanded=new LinkedHashMap<>();
        for(var asset:original.assets()){
            asset=GLCompositeForwarding.materialize(asset);
            asset=FormulaGroups.materialize(asset);
            asset=GroupInputs.materialize(asset,original);
            asset=GroupChoices.normalizeSelections(asset,original);
            var d=asset.compositeDescriptor();if(d==null){expanded.put(asset.id(),asset);continue;}
            var nodes=new ArrayList<>(asset.graph().nodes());var wires=new ArrayList<>(asset.graph().connections());
            var ports=new ArrayList<GLCompositePortMapping>();var properties=new ArrayList<>(d.properties());var mappings=new ArrayList<>(d.propertyMappings());
            if(MultipleGroups.multiple(asset)&&properties.stream().noneMatch(p->p.id().equals(MultipleGroups.COUNT_PROPERTY)))properties.add(new GLCompositePropertyDescriptor(MultipleGroups.COUNT_PROPERTY,"number",GLCompositeValueKind.DECIMAL,GLNodeValue.of(8.)));
            var bindings=new ArrayList<Adapter>();
            for(var p:d.portMappings()){
                if(MultipleGroups.countPort(p)){
                    String key=GroupUnits.key(p.publicPort().id()),unit=GroupUnits.unitKey(p.publicPort().id());
                    properties.removeIf(v->v.id().equals(key)||v.id().equals(unit));
                    properties.add(new GLCompositePropertyDescriptor(key,"number",GLCompositeValueKind.DECIMAL,GLNodeValue.of(8.)));
                    properties.add(new GLCompositePropertyDescriptor(unit,"text",GLCompositeValueKind.TEXT,GLNodeValue.of("factor")));
                    bindings.add(new Adapter(p,UnitFormat.FACTOR,key,unit));ports.add(p.withEndpoints(List.of()));continue;
                }
                if(p.publicPort().direction()==GLPortDirection.OUTPUT && p.isBound() && GroupUnits.numeric(p)) {
                    var base=GroupUnits.base(original,d,p);var sourceUnit=GroupUnits.target(original,d,p);
                    var instances=java.util.stream.Stream.concat(java.util.stream.Stream.of(root),original.assets().stream()).flatMap(a->a.graph().nodes().stream()).filter(n->n.typeId().equals(d.nodeTypeId()));
                    boolean convert=GroupUnits.initial(p,base)!=sourceUnit || instances.anyMatch(n->GroupUnits.selected(n,p,base)!=sourceUnit);
                    if(!convert){ports.add(p);continue;}
                    String ukey=GroupUnits.unitKey(p.publicPort().id());
                    var id=new GLNodeId(UUID.nameUUIDFromBytes((asset.id()+"/output-unit/"+p.publicPort().id()).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    var def=new UnitConverterNode();var values=new HashMap<>(def.defaults());
                    values.put("from",GLNodeValue.of(sourceUnit.id()));values.put("to",GLNodeValue.of(base.id()));
                    nodes.add(new GLGraphNode(id,def.registration().id(),values));
                    wires.add(new GLGraphConnection(new GLNodeConnectionId(UUID.nameUUIDFromBytes((id+"/wire").getBytes(java.nio.charset.StandardCharsets.UTF_8))),p.internalEndpoint(),new GLNodeEndpoint(id,"value")));
                    ports.add(p.withEndpoints(List.of(new GLNodeEndpoint(id,"out"))));
                    properties.removeIf(property->property.id().equals(ukey));
                    properties.add(new GLCompositePropertyDescriptor(ukey,"text",GLCompositeValueKind.TEXT,GLNodeValue.of(base.id())));
                    mappings.add(new GLCompositePropertyMapping(ukey,id,"to"));
                    bindings.add(new Adapter(p,base,"",ukey));continue;
                }
                // Unbound inputs still own their editor values (including pending Formula ports).
                // Declare them without creating DSP adapters until an internal target exists.
                if(p.publicPort().direction()==GLPortDirection.INPUT && !p.isBound() && Set.of(SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId()).contains(p.publicPort().valueTypeId())
                        && (p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)
                            || p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.NONE))) {
                    String key=GroupUnits.key(p.publicPort().id()),unit=GroupUnits.unitKey(p.publicPort().id());
                    double fallback=new NodeState(p.presentation().editorOptions()).number("default",0);
                    var base=GroupUnits.base(original,d,p);
                    properties.removeIf(v->v.id().equals(key)||v.id().equals(unit));
                    properties.add(new GLCompositePropertyDescriptor(key,"number",GLCompositeValueKind.DECIMAL,GLNodeValue.of(fallback)));
                    properties.add(new GLCompositePropertyDescriptor(unit,"text",GLCompositeValueKind.TEXT,GLNodeValue.of(base.id())));
                    ports.add(p);continue;
                }
                if(p.publicPort().direction()==GLPortDirection.INPUT && GroupVectorEditors.isVector(p)){
                    String key=GroupUnits.key(p.publicPort().id());var fallback=p.presentation().editorOptions().get("default");
                    properties.removeIf(v->v.id().equals(key));properties.add(new GLCompositePropertyDescriptor(key,"vector",GLCompositeValueKind.LIST,fallback));
                    var endpoints=new ArrayList<GLNodeEndpoint>();
                    for(var endpoint:p.internalEndpoints()){
                        var id=new GLNodeId(UUID.nameUUIDFromBytes((asset.id()+"/vector-input/"+p.publicPort().id()+"/"+endpoint).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        var def=new VectorMathNode();var values=new HashMap<>(def.defaults());values.put("a",fallback);values.put("vectorUnit",p.presentation().editorOptions().get(GroupUnits.BASE));values.put("b",VectorEditor.value(0,0,0,GroupVectorEditors.editor(p).formats().stored()));values.put("rate",GLNodeValue.of(GroupInputSettings.selectedRate(p).id()));
                        nodes.add(new GLGraphNode(id,def.registration().id(),values));endpoints.add(new GLNodeEndpoint(id,"a"));
                        wires.add(new GLGraphConnection(new GLNodeConnectionId(UUID.nameUUIDFromBytes((id+"/wire").getBytes(java.nio.charset.StandardCharsets.UTF_8))),new GLNodeEndpoint(id,"vector"),endpoint));
                        mappings.add(new GLCompositePropertyMapping(key,id,"a"));
                    }
                    ports.add(p.withEndpoints(endpoints));continue;
                }
                var requested=GroupInputSettings.selectedRate(p);
                if(p.publicPort().direction()==GLPortDirection.INPUT&&p.isBound()&&GroupInputs.template(p)!=null&&!p.publicPort().valueTypeId().equals(SignalCategory.WAVETABLE.valueTypeId())&&(requested==ProcessingRate.AUDIO||requested==ProcessingRate.CONTROL)){
                    var endpoints=new ArrayList<GLNodeEndpoint>();var template=GroupInputs.template(p);
                    for(var endpoint:p.internalEndpoints()){
                        var id=new GLNodeId(UUID.nameUUIDFromBytes((asset.id()+"/rate/"+p.publicPort().id()+"/"+endpoint).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        var table=new LinkedHashMap<String,GLNodeValue>();for(var entry:template.entries())table.put(entry.id(),GLNodeValue.of(GroupInputs.signalValue(asset,endpoint,entry.value(),original)));
                        var def=new RateConverterNode();var values=new HashMap<>(def.defaults());values.put("rate",GLNodeValue.of(requested.id()));values.put("value",table.get(template.initial()));
                        nodes.add(new GLGraphNode(id,def.registration().id(),values));endpoints.add(new GLNodeEndpoint(id,"value"));
                        wires.add(new GLGraphConnection(new GLNodeConnectionId(UUID.nameUUIDFromBytes((id+"/wire").getBytes(java.nio.charset.StandardCharsets.UTF_8))),new GLNodeEndpoint(id,"out"),endpoint));
                        mappings.add(new GLCompositePropertyMapping(GroupInputs.property(p),id,"value",table));
                    }
                    ports.add(p.withEndpoints(endpoints));continue;
                }
                if(p.publicPort().direction()!=GLPortDirection.INPUT||!p.isBound()||!GroupUnits.numeric(p)||!(p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)||p.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.NONE))){ports.add(p);continue;}
                String key=GroupUnits.key(p.publicPort().id()),ukey=GroupUnits.unitKey(p.publicPort().id());
                var base=GroupUnits.base(original,d,p);
                double fallback=new NodeState(p.presentation().editorOptions()).number("default",0);
                var adapterEndpoints=new ArrayList<GLNodeEndpoint>();
                for(var endpoint:p.internalEndpoints()) {
                var target=GroupInputSettings.checkbox(p)?UnitFormat.FACTOR:GroupUnits.targetOrBase(original,d,p.withEndpoints(List.of(endpoint)),base);
                if(!base.compatible(target))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Group input units do not match the internal input: ")+p.publicPort().id());
                var id=new GLNodeId(UUID.nameUUIDFromBytes((asset.id()+"/unit/"+p.publicPort().id()+"/"+endpoint).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                var def=new UnitConverterNode();var values=new HashMap<>(def.defaults());
                values.put("from",GLNodeValue.of(base.id()));values.put("to",GLNodeValue.of(target.id()));values.put("value",GLNodeValue.of(fallback));values.put("rate",GLNodeValue.of(new NodeState(p.presentation().editorOptions()).text(GroupInputSettings.RATE,"auto")));
                values.put(ControlMetadata.DISCRETE,GLNodeValue.of(!new NodeState(p.presentation().editorOptions()).flag("continuous",true)));
                nodes.add(new GLGraphNode(id,def.registration().id(),values));
                wires.add(new GLGraphConnection(new GLNodeConnectionId(UUID.nameUUIDFromBytes((id+"/wire").getBytes(java.nio.charset.StandardCharsets.UTF_8))),new GLNodeEndpoint(id,"out"),endpoint));
                adapterEndpoints.add(new GLNodeEndpoint(id,"value"));
                mappings.add(new GLCompositePropertyMapping(key,id,"value"));mappings.add(new GLCompositePropertyMapping(ukey,id,"from"));
                }
                String driverKey=MidiDriverBinding.key(key);
                properties.add(new GLCompositePropertyDescriptor(driverKey,"driver",GLCompositeValueKind.MAP,GLNodeValue.of(Map.of())));
                for(var endpoint:adapterEndpoints)mappings.add(new GLCompositePropertyMapping(driverKey,endpoint.nodeId(),MidiDriverBinding.key("value")));
                ports.add(p.withEndpoints(adapterEndpoints));
                properties.add(new GLCompositePropertyDescriptor(key,"number",GLCompositeValueKind.DECIMAL,GLNodeValue.of(fallback)));
                properties.add(new GLCompositePropertyDescriptor(ukey,"text",GLCompositeValueKind.TEXT,GLNodeValue.of(base.id())));
                bindings.add(new Adapter(p,base,key,ukey));
            }
            adapters.put(d.nodeTypeId(),bindings);
            var descriptor=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,properties,mappings);
            expanded.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(asset.graph().graphId(),asset.graph().revision(),nodes,wires),asset.viewState(),asset.metadata(),descriptor));
        }
        var declared=new HashMap<String,Set<String>>();
        for(var asset:expanded.values())if(asset.compositeDescriptor()!=null){
            var d=asset.compositeDescriptor();
            declared.put(d.nodeTypeId(),d.properties().stream().map(GLCompositePropertyDescriptor::id).collect(java.util.stream.Collectors.toSet()));
        }
        var registry=new GLCompositeGraphRegistry();
        for(var asset:expanded.values())registry.registerAsset(rewriteNested?rewrite(asset,adapters,declared):asset);
        return new Prepared(rewrite(GroupChoices.normalizeSelections(root,original),adapters,declared),registry);
    }
    private static GLNodeGraphAsset rewrite(GLNodeGraphAsset asset,Map<String,List<Adapter>> adapters,Map<String,Set<String>> declared){
        var graph=asset.graph();var nodes=graph.nodes().stream().map(raw->{
            var node=raw.withoutProperty(GroupBakes.PROPERTY);
            var bindings=adapters.get(node.typeId());if(bindings==null)return node;
            var values=new HashMap<>(node.properties());
            for(var binding:bindings){
                var selected=GroupUnits.selected(node,binding.port,binding.base);
                if(binding.port.publicPort().direction()==GLPortDirection.OUTPUT){values.put(binding.unitKey,GLNodeValue.of(selected.id()));continue;}

                double fallback=new NodeState(binding.port.presentation().editorOptions()).number("default",0);
                double value=new NodeState(node.properties()).number(binding.valueKey,MultipleGroups.countPort(binding.port)?new NodeState(node.properties()).number(MultipleGroups.COUNT_PROPERTY,fallback):fallback);
                if(GroupInputSettings.checkbox(binding.port))value=value==0?0:1;
                values.put(binding.valueKey,GLNodeValue.of(binding.base.convert(value,selected)));values.put(binding.unitKey,GLNodeValue.of(selected.id()));
                var driverKey=MidiDriverBinding.key(binding.valueKey);var assignment=values.get(driverKey);
                if(binding.base!=selected&&assignment instanceof GLNodeValue.MapValue map&&map.values().containsKey("device")){
                    var driver=MidiDriverBinding.read(assignment);var converted=new HashMap<>(map.values());
                    converted.put("nativeValues",GLNodeValue.of(java.util.stream.IntStream.range(0,128).mapToObj(i->binding.base.convert(driver.mapping().map(i/127.),selected)).toList()));
                    values.put(driverKey,GLNodeValue.of(converted));
                }
            }
            // Interface edits can leave former input defaults on existing instances.
            // Only discard our reserved input keys, never arbitrary composite properties.
            var valid=declared.getOrDefault(node.typeId(),Set.of());
            values.keySet().removeIf(key->{
                String property=key.startsWith(GLGraphNode.AUTOMATIC_DEFAULT_PREFIX)?key.substring(GLGraphNode.AUTOMATIC_DEFAULT_PREFIX.length()):key.startsWith(GLGraphNode.AUTOMATIC_PREFIX)?key.substring(GLGraphNode.AUTOMATIC_PREFIX.length()):key;
                return property.matches("easyknack\\.input\\.[0-9a-f]+(?:\\.unit)?")&&!valid.contains(property);
            });
            return new GLGraphNode(node.id(),node.typeId(),node.typeVersion(),values);
        }).toList();
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,graph.connections()),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
    }
}
