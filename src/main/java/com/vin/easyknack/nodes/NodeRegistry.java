package com.vin.easyknack.nodes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import com.vingame.glswing.node.model.*;

/** Bridges generated definitions to GLSwing's static type contracts. */
public final class NodeRegistry {
    private static final Map<String, BasicNode> BUILTINS = definitions();
    private static volatile Map<String, BasicNode> DEFINITIONS = BUILTINS;
    public static Collection<BasicNode> builtinDefinitions() { return BUILTINS.values(); }
    public static synchronized void installPlugins(Collection<BasicNode> plugins) {
        var next = new LinkedHashMap<>(BUILTINS);
        for(var node:plugins) if(next.putIfAbsent(node.registration().id(),node)!=null)
            throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Duplicate plugin node: ")+node.registration().id());
        DEFINITIONS=Collections.unmodifiableMap(next);
    }
    /** Thread-confined execution contracts, invalidated whenever plugin definitions change. */
    public record ExecutionContracts(GLNodeTypeRegistry types,NodeRegistry definitions){}
    private record ExecutionCache(Map<String,BasicNode> generation,ExecutionContracts contracts){}
    private static final ThreadLocal<ExecutionCache> EXECUTION=new ThreadLocal<>();
    public static ExecutionContracts executionContracts(){
        var generation=DEFINITIONS;var cached=EXECUTION.get();
        if(cached==null||cached.generation()!=generation){
            var types=new GLNodeTypeRegistry();
            cached=new ExecutionCache(generation,new ExecutionContracts(types,new NodeRegistry(types,new GLNodeCatalog())));
            EXECUTION.set(cached);
        }
        return cached.contracts();
    }
    private final GLNodeTypeRegistry types;
    private final GLNodePropertySchemaRegistry schemas = new GLNodePropertySchemaRegistry();
    private final Map<String, String> defaultContracts = new HashMap<>();
    private static Map<String, BasicNode> definitions() {
        Map<String, BasicNode> result = new LinkedHashMap<>();
        for (BasicNode node : GeneratedNodes.create()) {
            if (result.putIfAbsent(node.registration().id(), node) != null)
                throw new IllegalStateException(com.vin.easyknack.ui.AppText.literal("Duplicate node type: ") + node.registration().id());
        }
        return Collections.unmodifiableMap(result);
    }
    public static Collection<BasicNode> definitionsList() { return DEFINITIONS.values(); }
    public static Optional<BasicNode> find(String typeId) {
        int variant = typeId.indexOf(".variant.");
        return Optional.ofNullable(DEFINITIONS.get(variant < 0 ? typeId : typeId.substring(0, variant)));
    }
    public static BasicNode require(String typeId) {
        return find(typeId).orElseThrow(() -> new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unknown audio node: ") + typeId));
    }
    public NodeRegistry(GLNodeTypeRegistry types, GLNodeCatalog catalog) {
        this.types = types;
        catalog.setRecentLimit(0);
        catalog.setCategoryOrder(NodeCategory.menuOrder());
        var numeric=Set.of(SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId(),SignalCategory.WAVETABLE.valueTypeId(),"easyknack.waveform");
        types.setValueTypeCompatibility((a,b)->a.equals(b)||numeric.contains(a)&&numeric.contains(b)||a.equals(PortDataType.SPECTRUM.id())&&b.equals(PortDataType.ANALYSIS.id()));
        catalog.setValueTypeCompatibility(types::compatibleValueTypes);
        refreshCatalog(catalog);
    }
    public void refreshCatalog(GLNodeCatalog catalog) {
        for (BasicNode definition : DEFINITIONS.values()) {
            if(defaultContracts.containsKey(definition.registration().id()))continue;
            NodeState state = new NodeState(definition.defaults());
            var layout = definition.layout(state);
            var mute = routing(definition, state);
            var insertion = insertion(definition, state);
            var exclusion = definition.exclusionInput(state);
            String id = definition.registration().id();
            defaultContracts.put(id, signature(layout, mute, insertion, exclusion));
            var descriptor = register(id, layout, mute, insertion, exclusion, definition);
            if(definition.isListedInCatalog()) catalog.register(new GLNodeCatalogEntry(descriptor, definition.title(),
                    definition.registration().category().path(), definition.defaults(), "audio"));
        }
    }
    public GLNodePropertySchemaRegistry schemas() { return schemas; }
    private List<GLNodeMuteBypass> routing(BasicNode definition, NodeState state) {
        var routing = new MuteRouting(); definition.mute(routing, state); return routing.bypasses();
    }
    private GLNodeInsertionPorts insertion(BasicNode definition, NodeState state) {
        var routing = new InsertionRouting(); definition.insert(routing, state); return routing.ports();
    }
    private static String signature(NodeBuilder layout, List<GLNodeMuteBypass> mute, GLNodeInsertionPorts insertion, PropertyId exclusion) {
        return layout.ports().toString() + "|" + mute + "|" + insertion + "|" + exclusion + "|" + layout.rows().stream()
                .map(r -> r.id() + ":" + r.isInspectorOnly()
                        + ":" + (r.editor()==null?"":r.editor().structuralKey())).toList();
    }
    private boolean refreshingLanguage;
    public void refreshLanguage(GLNodeCatalog catalog,Collection<GLGraphNode> models){
        refreshingLanguage=true;
        try{
            for(var definition:DEFINITIONS.values()){
                var state=new NodeState(definition.defaults());var id=definition.registration().id();
                var type=register(id,definition.layout(state),routing(definition,state),insertion(definition,state),definition.exclusionInput(state),definition);
                if(catalog.find(id).isPresent())catalog.replace(new GLNodeCatalogEntry(type,definition.title(),definition.registration().category().path(),definition.defaults(),"audio"));
            }
            for(var model:models)normalize(model);
        }finally{refreshingLanguage=false;}
    }
    private GLNodeTypeDescriptor register(String id, NodeBuilder layout, List<GLNodeMuteBypass> mute, GLNodeInsertionPorts insertion, PropertyId exclusion, BasicNode definition) {
        var existing = types.find(id);
        if (existing.isPresent()&&!refreshingLanguage) return existing.get();
        var descriptor = new GLNodeTypeDescriptor(id, layout.ports());
        if(existing.isEmpty())types.register(descriptor);
        types.setMuteBypasses(id, mute);
        types.setInsertionPorts(id, insertion);
        types.setExclusionInput(id, exclusion==null ? null : exclusion.value());
        List<GLNodePropertyDescriptor> properties = new ArrayList<>();
        for (var row : layout.rows()) {
            // Presentation-only visibility changes must not replace the active control.
            if (row.isMenuOnly()) continue;
            if(row.editor() instanceof Editor.ComboBox<?> combo && combo.items().stream().allMatch(item->emptyGroup(combo,item)) && !hasChoiceLabels(combo))properties.add(enumProperty(row,combo));
            else if (row.editor() instanceof Editor.Slider || row.editor() instanceof Editor.CheckBox || row.editor() instanceof Editor.ComboBox<?>)
                properties.add(InspectorEditor.property(row,definition));
            else if(row.editor() instanceof UnitSlider slider) properties.add(new GLCustomNodeProperty(row.id(),row.label(),
                    (model,changed)->{
                        var selected=(UnitSlider)definition.layout(new NodeState(model.properties())).rows().stream().filter(r->r.id().equals(row.id())).findFirst().orElseThrow().editor();
                        var control=selected.create(row.label(),changed);
                        control.update().accept(model.properties().getOrDefault(row.id(),slider.defaultValue()));
                        selected.bindUnitMenu((com.vingame.glswing.ui.GLSlider)control.component(),unit->{
                            for(var parent=control.component().getGLParent();parent!=null;parent=parent instanceof com.vingame.glswing.component.GLContainer container?container.getGLParent():null)
                                if(parent instanceof com.vingame.glswing.node.GLNodePropertyInspector inspector){inspector.setSelectedProperty(row.unitProperty().value(),GLNodeValue.of(unit.id()));break;}
                        });
                        ((com.vingame.glswing.ui.GLSlider)control.component()).getContextMenu().addItem(com.vin.easyknack.ui.AppText.literal("Reset to automatic"),e->{
                            for(var parent=control.component().getGLParent();parent!=null;parent=parent instanceof com.vingame.glswing.component.GLContainer container?container.getGLParent():null)
                                if(parent instanceof com.vingame.glswing.node.GLNodePropertyInspector inspector){inspector.setSelectedProperty(GLGraphNode.AUTOMATIC_PREFIX+row.id(),GLNodeValue.of(true));break;}
                        });
                        return control.component();
                    },(control,model)->{
                        var current=definition.layout(new NodeState(model.properties())).rows().stream().filter(r->r.id().equals(row.id())).findFirst().orElseThrow();
                        if(current.editor() instanceof UnitSlider next && control instanceof com.vingame.glswing.ui.GLUnitSlider<?> existingSlider && !existingSlider.getUnitFormats().equals(next.formats().core())){
                            for(var parent=control.getGLParent();parent!=null;parent=parent instanceof com.vingame.glswing.component.GLContainer container?container.getGLParent():null)
                                if(parent instanceof com.vingame.glswing.node.GLNodePropertyInspector inspector){java.awt.EventQueue.invokeLater(inspector::refresh);break;}
                            return;
                        }
                        EditorPresentation.update(current,control);
                        ((com.vingame.glswing.ui.GLUnitSlider<?>)control).setStoredValue(new NodeState(model.properties()).number(row.key(),slider.spec().value()));
                    }));
            else if(row.editor() instanceof CurveEditor curve) properties.add(new GLCustomNodeProperty(row.id(),row.label(),
                    (model,changed)->{
                        var control=(com.vingame.glswing.ui.GLCurveEditor)curve.create(row.label(),changed).component();
                        control.setCurve(CurveData.decode((String)model.properties().get(row.id()).toJavaValue()));
                        control.setLarge(true); CurveEditor.bindHistory(control,control::getGLHistoryManager);
                        definition.updateEditor(row.key(),control,new NodeState(model.properties()));
                        return control;
                    },(control,model)->{
                        ((com.vingame.glswing.ui.GLCurveEditor)control).setCurve(CurveData.decode((String)model.properties().get(row.id()).toJavaValue()));
                        definition.updateEditor(row.key(),control,new NodeState(model.properties()));
                    }));
            else if(row.editor()!=null) properties.add(InspectorEditor.property(row,definition));
        }
        schemas.register(id, properties);
        return descriptor;
    }
    @SuppressWarnings({"rawtypes","unchecked"}) private static boolean emptyGroup(Editor.ComboBox combo,Object item){Object label=combo.group().apply(item);return label==null||label.toString().isBlank();}
    private static <T> boolean hasChoiceLabels(Editor.ComboBox<T> combo){return combo.items().stream().anyMatch(item->!(item instanceof ProcessingRate)&&!combo.id().apply(item).equals(item.toString()));}
    private static <T> GLEnumNodeProperty enumProperty(NodeBuilder.Row row, Editor.ComboBox<T> combo) {
        return new GLEnumNodeProperty(row.id(), row.label(), combo.items().stream().map(combo.id()).toList(),row.tooltip());
    }
    public GLGraphNode normalize(GLGraphNode node) {
        node=node.withoutProperty(ProcessingRate.EFFECTIVE);
        var definition = find(node.typeId()).orElse(null);
        if(GLNodeValue.of("event").equals(node.properties().get("rate")))node=node.withProperty("rate",GLNodeValue.of("audio"));
        if (definition == null) return node;
        if(definition instanceof AdditiveOscillatorNode)node=node.withoutProperty("noise").withoutProperty("noise.unit");
        for(var entry:definition.normalizeState(new NodeState(node.properties())).values().entrySet())
            if(!entry.getValue().equals(node.properties().get(entry.getKey())))node=node.withProperty(entry.getKey(),entry.getValue());
        if(definition instanceof VoiceMergeNode && java.util.Set.of("spread","pitch").contains(new NodeState(node.properties()).text(VoiceMergeNode.MODE,"sum"))){
            definition=DEFINITIONS.get("easyknack.audio.legacyvoicemerge");
            node=node.withType(definition.registration().id(),node.typeVersion());
        }
        if(!node.properties().containsKey("rate") && (definition instanceof MathNode || definition instanceof MapRangeNode
                || definition instanceof MixNode || definition instanceof CurveNode || definition instanceof UnitConverterNode))
            node=node.withProperty("rate",GLNodeValue.of(definition instanceof UnitConverterNode?"control":"audio"));
        var state = new NodeState(node.properties());
        var layout = definition.layout(state);
        // Update only automatic defaults; unmarked legacy values are explicit.
        for(var row:layout.rows())if(row.editor()!=null && node.isPropertyAutomatic(row.id()))
            node=node.withAutomaticProperty(row.id(),row.editor().defaultValue());
        state=new NodeState(node.properties());layout=definition.layout(state);
        // Old documents keep their signal interpretation, but gain editable format selectors.
        for(var row:layout.rows())if(row.editor() instanceof UnitSlider slider && !node.properties().containsKey(row.id()+".unit"))
            node=node.withProperty(row.id()+".unit",GLNodeValue.of(slider.selected().id()));
        // Populate missing editor properties in older documents, including runtime placeholders.
        for(var row:layout.rows())if(row.editor()!=null&&!node.properties().containsKey(row.id()))
            node=node.withProperty(row.id(),row.editor().defaultValue());
        var mute = routing(definition, state);
        var insertion = insertion(definition, state);
        var exclusion = definition.exclusionInput(state);
        String signature = signature(layout, mute, insertion, exclusion);
        String id = definition.registration().id();
        if (!signature.equals(defaultContracts.get(id))) id += ".variant." + digest(signature);
        register(id, layout, mute, insertion, exclusion, definition);
        return node.typeId().equals(id) ? node : node.withType(id, node.typeVersion());
    }
    /** Ports, their cables and model properties are replaced in one graph transaction. */
    public GLNodeGraphSnapshot normalize(GLNodeGraphSnapshot snapshot) {return normalize(snapshot,null);}
    public GLNodeGraphSnapshot normalize(GLNodeGraphSnapshot snapshot,Set<GLNodeId> changed) {
        var nodes = new ArrayList<>(snapshot.nodes().stream().map(n->changed==null||changed.contains(n.id())?normalize(n):n).toList());
        Map<GLNodeId, GLGraphNode> byId = new HashMap<>(); nodes.forEach(n -> byId.put(n.id(), n));
        var connections = new ArrayList<GLGraphConnection>();
        for(var c:snapshot.connections()){
            if(!valid(byId,c.input(),GLPortDirection.INPUT)||!valid(byId,c.output(),GLPortDirection.OUTPUT))continue;
            String from=portType(byId,c.output()),to=portType(byId,c.input());
            boolean fromTrigger=PortDataType.TRIGGER.id().equals(from),toTrigger=PortDataType.TRIGGER.id().equals(to);
            boolean numeric=Set.of(PortDataType.SIGNAL.id(),SignalCategory.VALUE.valueTypeId()).contains(fromTrigger?to:from);
            if(fromTrigger!=toTrigger&&numeric){
                // Preserve old documents with explicit, visible conversion nodes.
                BasicNode adapter=fromTrigger?new TriggerPulseNode():new EdgeDetectorNode();
                var id=GLNodeId.parse(UUID.nameUUIDFromBytes((c.id()+"/trigger-adapter").getBytes(StandardCharsets.UTF_8)).toString());
                var values=new HashMap<>(adapter.defaults());if(!fromTrigger)values.put("rate",GLNodeValue.of("audio"));
                var node=normalize(new GLGraphNode(id,adapter.registration().id(),values));nodes.add(node);byId.put(id,node);
                connections.add(new GLGraphConnection(c.id(),c.output(),new GLNodeEndpoint(id,fromTrigger?"trigger":"in")));
                var wireId=GLNodeConnectionId.parse(UUID.nameUUIDFromBytes((c.id()+"/trigger-output").getBytes(StandardCharsets.UTF_8)).toString());
                connections.add(new GLGraphConnection(wireId,new GLNodeEndpoint(id,fromTrigger?"audio-out":"out"),c.input()));
            }else connections.add(c);
        }
        return new GLNodeGraphSnapshot(snapshot.graphId(), snapshot.revision(), nodes, connections);
    }
    private String portType(Map<GLNodeId,GLGraphNode> nodes,GLNodeEndpoint endpoint){
        return types.find(nodes.get(endpoint.nodeId()).typeId()).flatMap(t->t.findPort(endpoint.portId())).orElseThrow().valueTypeId();
    }
    private boolean valid(Map<GLNodeId, GLGraphNode> nodes, GLNodeEndpoint endpoint, GLPortDirection direction) {
        var node = nodes.get(endpoint.nodeId());
        if (node == null) return false;
        return types.find(node.typeId()).flatMap(t -> t.findPort(endpoint.portId()))
                .map(p -> p.direction() == direction).orElse(false);
    }
    private static String digest(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
    }
}

