package com.vin.easyknack.ui;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.lang.ref.WeakReference;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.plugins.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.*;

/** EDT-owned local bake state. Documents always retain the original editable graph. */
final class GroupBakes implements AutoCloseable {
    void addTriggerAliases(Map<String,List<String>> aliases){
        var hosts=new HashMap<>(aliases);
        entries.forEach((id,entry)->{
            if(entry.enabled&&entry.source!=null)for(String host:hosts.getOrDefault(id.toString(),List.of()))
                entry.source.manualTargets().forEach((source,targets)->{
                    var resolved=aliases.computeIfAbsent(source,key->new ArrayList<>());
                    for(String target:targets)resolved.add(host+"/"+target);
                    var scoped=aliases.computeIfAbsent(id+"/"+source,key->new ArrayList<>());
                    for(String target:targets)scoped.add(host+"/"+target);
                });
        });
    }
    static final String SAVED="easyknack.bakedGroups";
    private final Set<GLNodeId> restoring=new LinkedHashSet<>();
    private final javax.swing.Timer restoreTimer=new javax.swing.Timer(150,e->restoreNext());
    void restore(GLNodeGraphDocument doc){
        for(var asset:NodeDocuments.assets(doc)){var value=asset.metadata().get(SAVED);if(value!=null)for(String id:value.toJavaValue().toString().split(","))try{restoring.add(GLNodeId.parse(id));}catch(IllegalArgumentException ignored){}}
        if(!restoring.isEmpty())restoreTimer.start();
    }
    private void restoreNext(){if(busy)return;if(restoring.isEmpty()){restoreTimer.stop();return;}var id=restoring.iterator().next();restoring.remove(id);bake(id);}
    String saved(GLNodeGraphSnapshot graph){return graph.nodes().stream().map(GLGraphNode::id).filter(id->restoring.contains(id)||(entries.containsKey(id)&&entries.get(id).wanted)).map(id->id.value().toString()).sorted().collect(java.util.stream.Collectors.joining(","));}
    private static Map<String,GLNodeValue> dspMetadata(Map<String,GLNodeValue> source){var result=new HashMap<>(source);result.remove(SAVED);result.remove(FormulaGroups.DRAFT);result.remove(FormulaGroups.LIVE);return result;}
    static final String PROPERTY="easyknack.bake";
    interface Builder { PluginManager.Build build(Map<String,String> files,Consumer<String> progress)throws Exception; }
    private static final class Entry {
        Object key; GroupSourceExporter.Export source; BasicNode definition;
        boolean enabled,wanted; String status="Not baked";
    }
    private final Supplier<GLNodeGraphDocument> document;
    private final Builder builder;
    private final Runnable changed;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"EasyKnack group bake");t.setDaemon(true);return t;});
    private final Map<GLNodeId,Entry> entries=new HashMap<>();
    private final List<WeakReference<Panel>> panels=new ArrayList<>();
    private boolean busy,closed;private long generation;
    private java.nio.file.Path host;
    GroupBakes(Supplier<GLNodeGraphDocument> document,Builder builder,Runnable changed){this.document=document;this.builder=builder;this.changed=changed;}
    java.nio.file.Path host(){return host;}
    boolean busy(){return busy;}
    String status(GLNodeId id){var e=entries.get(id);return AppText.literal(e==null?"Not baked":e.status);}
    boolean enabled(GLNodeId id){var e=entries.get(id);return e!=null&&e.enabled;}
    void register(GLNodePropertySchemaRegistry schemas,GLCompositeGraphRegistry registry){
        for(var d:registry.composites()){
            var properties=new ArrayList<>(schemas.properties(d.nodeTypeId()));
            if(properties.stream().noneMatch(p->p.id().equals(PROPERTY))){
                properties.add(new GLCustomNodeProperty(PROPERTY,"Bake",(node,writer)->new Panel(node.id()),(control,node)->((Panel)control).refresh()));
                schemas.register(d.nodeTypeId(),properties);
            }
        }
    }
    private static GLGraphNode find(GLNodeGraphDocument doc,GLNodeId id){
        return NodeDocuments.assets(doc).stream().flatMap(a->a.graph().nodes().stream()).filter(n->n.id().equals(id)).findFirst().orElse(null);
    }
    /** Geometry/revisions do not invalidate DSP; properties and nested definitions do. */
    static Object key(GLNodeGraphDocument doc,GLNodeId id){
        var node=find(doc,id);if(node==null)return null;
        var assets=new HashMap<String,GLNodeGraphAsset>();
        for(var a:NodeDocuments.assets(doc))if(a.compositeDescriptor()!=null)assets.put(a.compositeDescriptor().nodeTypeId(),a);
        var signature=clean(node);
        var definition=assets.get(node.typeId());
        if(definition!=null)for(var port:definition.compositeDescriptor().portMappings())
            if(port.publicPort().direction()==GLPortDirection.INPUT && port.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER))
                signature=signature.withoutProperty(GroupUnits.key(port.publicPort().id()));
        if(definition!=null){var registry=new GLCompositeGraphRegistry();NodeDocuments.assets(doc).forEach(registry::registerAsset);
            for(var choice:GroupChoices.choices(definition,registry))signature=signature.withoutProperty(choice.id());}
        var parts=new ArrayList<Object>();parts.add(signature);
        collect(node.typeId(),assets,new HashSet<>(),parts);
        // Outer interfaces can change how a nested instance is bound.
        for(var a:NodeDocuments.assets(doc))if(a.graph().findNode(id).isPresent()&&a.compositeDescriptor()!=null)parts.add(a.compositeDescriptor());
        return List.copyOf(parts);
    }
    private static GLGraphNode withoutProvenance(GLGraphNode node){
        for(var key:List.copyOf(node.properties().keySet()))if(key.startsWith(GLGraphNode.AUTOMATIC_PREFIX)||key.startsWith(GLGraphNode.AUTOMATIC_DEFAULT_PREFIX))node=node.withoutProperty(key);
        return node;
    }
    private static GLGraphNode clean(GLGraphNode n){return withoutProvenance(n).withoutProperty(PROPERTY).withoutProperty(GLNodeState.HIDDEN).withoutProperty(GLNodeState.MUTED);}
    private static void collect(String type,Map<String,GLNodeGraphAsset> assets,Set<String> seen,List<Object> parts){
        var a=assets.get(type);if(a==null||!seen.add(type))return;
        // Internal mute changes do affect the exported sound.
        parts.add(List.of(a.id(),a.compositeDescriptor(),a.graph().nodes().stream().map(n->withoutProvenance(n).withoutProperty(PROPERTY).withoutProperty(GLNodeState.HIDDEN)).toList(),a.graph().connections(),dspMetadata(a.metadata())));
        for(var n:a.graph().nodes())collect(n.typeId(),assets,seen,parts);
    }
    void validate(){
        if(closed||entries.isEmpty())return;var doc=document.get();boolean invalid=false;
        for(var item:entries.entrySet()){
            var e=item.getValue();
            if(e.key!=null&&!Objects.equals(e.key,key(doc,item.getKey()))){
                e.key=null;e.wanted=false;e.enabled=false;e.status="Changed — bake again";invalid=true;
            }
        }
        if(invalid)generation++;
        refresh();
    }
    void reset(){restoreTimer.stop();restoring.clear();generation++;entries.clear();host=null;refresh();}
    void disable(GLNodeId id){
        var e=entries.get(id);if(e==null)return;
        generation++;e.wanted=false;e.enabled=false;e.status="Disabled";refresh();changed.run();
    }
    void bake(GLNodeId id){
        bake(id,false);
    }
    void bake(GLNodeId id,boolean force){
        if(closed||busy)return;
        validate();if(enabled(id)&&!force)return;var doc=document.get();var node=find(doc,id);
        if(node==null)return;
        var entry=entries.computeIfAbsent(id,k->new Entry());
        try {
            for(var asset:NodeDocuments.assets(doc))if(asset.compositeDescriptor()!=null)
                if(asset.compositeDescriptor().propertyMappings().stream().anyMatch(p->p.internalNodeId().equals(id)))
                    throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("This instance has properties controlled by its parent group. Bake the parent group instead."));
            entry.source=GroupSourceExporter.create(doc,node,true);entry.key=key(doc,id);entry.enabled=false;entry.wanted=true;
        }catch(Exception failure){entry.status=AppText.literal("Bake failed: ")+failure.getMessage();refresh();return;}
        var requested=new LinkedHashMap<GLNodeId,Entry>();
        entries.forEach((key,value)->{if(value.enabled||key.equals(id))requested.put(key,value);});
        var files=new TreeMap<String,String>();var names=new ArrayList<String>();
        for(var e:requested.values()){
            names.add("com.vin.easyknack.nodes."+e.source.className());
            e.source.files().forEach((path,text)->{if(path.startsWith("java/")||path.startsWith("native/"))files.put(path,text);});
        }
        files.put("plugin.properties",new PluginManifest("easyknack_local_bakes","1",names).text());
        // A fresh content-addressed build also has a fresh executable path. Never
        // overwrite a cached host that may still be running on Windows.
        if(force)files.put("bake-request.txt",UUID.randomUUID().toString());
        long ticket=++generation;busy=true;entry.status="Compiling…";refresh();
        worker.execute(()->{
            try {
                var build=builder.build(Map.copyOf(files),progress->javax.swing.SwingUtilities.invokeLater(()->{
                    if(!closed&&ticket==generation){entry.status=progress;refresh();}
                }));
                javax.swing.SwingUtilities.invokeLater(()->{
                    if(closed)return;busy=false;validate();
                    if(ticket!=generation){for(var e:requested.values())if(!e.enabled&&e.key!=null)e.status="Bake cancelled - graph changed";refresh();return;}
                    try {
                        var definitions=new HashMap<GLNodeId,BasicNode>();
                        requested.forEach((key,e)->definitions.put(key,build.definitions().stream()
                            .filter(d->d.processor().id().equals(e.source.definition().processor().id()))
                            .findFirst().orElseThrow(()->new IllegalStateException(com.vin.easyknack.ui.AppText.literal("Compiled group definition missing")))));
                        NodeRegistry.installPlugins(build.definitions());host=build.host();
                        requested.forEach((key,e)->{
                            e.definition=definitions.get(key);e.enabled=true;e.status=build.cached()?"Baked (cached)":"Baked";
                        });
                    } catch(Exception failure) {
                        entry.enabled=false;entry.wanted=false;entry.status=AppText.literal("Bake failed: ")+failure.getMessage();
                    }
                    refresh();changed.run();
                });
            }catch(Exception failure){javax.swing.SwingUtilities.invokeLater(()->{
                if(closed)return;busy=false;
                if(ticket==generation){entry.enabled=false;entry.wanted=false;entry.status=AppText.literal("Bake failed: ")+failure.getMessage();}
                refresh();changed.run();
            });}
        });
    }
    /** Substitute only in the transient execution graph, never in the document. */
    GLNodeGraphAsset apply(GLNodeGraphAsset asset){
        var replacements=new HashMap<GLNodeId,Entry>();
        for(var n:asset.graph().nodes()){
            var e=entries.get(n.id());if(e!=null&&e.enabled&&!GLNodeState.isMuted(n))replacements.put(n.id(),e);
        }
        if(replacements.isEmpty())return asset;
        BiFunction<GLNodeEndpoint,Boolean,GLNodeEndpoint> endpoint=(old,input)->{
            var e=replacements.get(old.nodeId());if(e==null)return old;
            var source=e.source.source();
            var n=find(source,old.nodeId());
            var descriptor=NodeDocuments.assets(source).stream().map(GLNodeGraphAsset::compositeDescriptor).filter(Objects::nonNull).filter(d->d.nodeTypeId().equals(n.typeId())).findFirst().orElseThrow();
            var ports=descriptor.portMappings().stream().filter(p->!GroupInputs.editorOnly(p)&&(p.publicPort().direction()==GLPortDirection.INPUT)==input).toList();
            for(int i=0;i<ports.size();i++)if(ports.get(i).publicPort().id().equals(old.portId()))return new GLNodeEndpoint(old.nodeId(),(input?"input_":"output_")+i);
            throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Baked group port changed"));
        };
        var graph=asset.graph();var nodes=graph.nodes().stream().map(n->{var e=replacements.get(n.id());return e==null?n:replacement(n,e);}).toList();
        var wires=graph.connections().stream().map(c->new GLGraphConnection(c.id(),endpoint.apply(c.output(),false),endpoint.apply(c.input(),true))).toList();
        var descriptor=asset.compositeDescriptor();
        if(descriptor!=null)descriptor=new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),descriptor.minimumAssetVersion(),
            descriptor.portMappings().stream().map(p->p.withEndpoints(p.internalEndpoints().stream().map(e->endpoint.apply(e,p.publicPort().direction()==GLPortDirection.INPUT)).toList())).toList(),descriptor.properties(),descriptor.propertyMappings().stream().map(m->{
                var entry=replacements.get(m.internalNodeId());if(entry==null)return m;
                var definition=(GroupSourceExporter.ExportDefinition)entry.source.definition();
                var choice=definition.choices.stream().filter(c->c.property().equals(m.internalPropertyId())).findFirst();
                return choice.isPresent()?new GLCompositePropertyMapping(m.publicPropertyId(),m.internalNodeId(),choice.get().id().value(),m.values()):m;
            }).toList());
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires),asset.viewState(),asset.metadata(),descriptor);
    }
    private GLGraphNode replacement(GLGraphNode node,Entry entry){
        var values=new HashMap<>(entry.definition.defaults());
        var registry=new GLCompositeGraphRegistry();NodeDocuments.assets(entry.source.source()).forEach(registry::registerAsset);
        var descriptor=registry.findComposite(node.typeId()).orElseThrow();
        var definition=(GroupSourceExporter.ExportDefinition)entry.source.definition();
        for(var port:definition.inputs)if(port.editor() instanceof Editor.Slider||port.editor() instanceof Editor.Toggle){
            var assignment=node.properties().get(MidiDriverBinding.key(GroupUnits.key(port.mapping().publicPort().id())));
            if(assignment!=null)values.put(MidiDriverBinding.key(port.id().value()),assignment);
            var mapping=port.mapping();var base=GroupUnits.base(registry,descriptor,mapping);
            var options=new NodeState(mapping.presentation().editorOptions());
            double value=new NodeState(node.properties()).number(GroupUnits.key(mapping.publicPort().id()),options.number("default",0));
            values.put(port.id().value(),GLNodeValue.of(port.editor() instanceof Editor.Toggle ? (value==0?0.0:1.0) : base.convert(value,((Editor.Slider)port.editor()).formats().stored())));
        }
        for(var choice:definition.choices)values.put(choice.id().value(),node.properties().getOrDefault(choice.property(),choice.editor().defaultValue()));
        return new GLGraphNode(node.id(),entry.definition.registration().id(),values);
    }
    private void refresh(){panels.removeIf(ref->ref.get()==null);for(var ref:panels){var panel=ref.get();if(panel!=null)panel.refresh();}}
    @Override public void close(){closed=true;restoreTimer.stop();generation++;worker.shutdownNow();panels.clear();}
    final class Panel extends GLContainer {
        final GLNodeId id;final GLLabel status=new GLLabel(com.vin.easyknack.ui.AppText.literal("Not baked"));final GLButton bake=new GLButton(com.vin.easyknack.ui.AppText.literal("Bake")),disable=new GLButton(com.vin.easyknack.ui.AppText.literal("Disable bake")),force=new GLButton(com.vin.easyknack.ui.AppText.literal("Force rebuild"));
        Panel(GLNodeId id){this.id=id;addGL(status);addGL(bake);addGL(disable);addGL(force);setPreferredGLSize(220,100);panels.add(new WeakReference<>(this));
            bake.addActionListener(e->GroupBakes.this.bake(id));disable.addActionListener(e->GroupBakes.this.disable(id));force.addActionListener(e->GroupBakes.this.bake(id,true));refresh();}
        void refresh(){status.setText(GroupBakes.this.status(id));bake.setToolTipText(com.vin.easyknack.ui.AppText.literal("Compile this editable group. Switching hosts briefly interrupts audio. ")+GroupBakes.this.status(id)+(host==null?"":"\nAudio host: "+host));bake.setEnabled(!busy&&!closed&&!enabled(id));disable.setEnabled(!busy&&enabled(id));force.setEnabled(!busy&&!closed);force.setToolTipText(com.vin.easyknack.ui.AppText.literal("Regenerate and compile this group without reusing a cached build. Switching hosts briefly interrupts audio."));}
        @Override protected void record(com.vingame.glswing.rendering.GLContext2D context){}
        @Override protected void resized(float w,float h){status.setBounds(0,0,w,26);bake.setBounds(0,30,w*.48f,28);disable.setBounds(w*.5f,30,w*.5f,28);force.setBounds(0,62,w,28);}
    }
}
