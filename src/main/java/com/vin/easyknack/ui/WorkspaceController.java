package com.vin.easyknack.ui;
import com.vin.easyknack.compiler.CompiledManualTargets;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;
import java.util.WeakHashMap;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vin.easyknack.nodes.NodeRegistry;
import com.vin.easyknack.nodes.BasicNode;
import com.vin.easyknack.nodes.OscillatorNode;
import com.vin.easyknack.nodes.GainNode;
import com.vin.easyknack.nodes.OutputNode;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vin.easyknack.audio.AudioHostService;
import com.vingame.glswing.node.composite.GLCompositeGraphFlattener;
import com.vingame.glswing.node.composite.GLCompositeForwarding;
import com.vingame.glswing.node.composite.GLCompositeExpansionLimits;
import com.vingame.glswing.node.document.GLNodeGraphAsset;
import com.vingame.glswing.node.document.GLNodeGraphDocument;
import com.vingame.glswing.node.document.GLNodeGraphAssetKind;
import com.vingame.glswing.node.execution.GLMutedGraphResolver;
import com.vingame.glswing.node.model.GLNodeGraphSnapshot;
import com.vingame.glswing.node.model.GLNodeState;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.GLNodeEditingActions;
import com.vingame.glswing.node.GLNodeWorkspace;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.binding.GLNodeViewState;
import com.vingame.glswing.node.composite.GLCompositeGraphRegistry;
import com.vingame.glswing.node.composite.GLNodeInputEditorRegistry;
import com.vingame.glswing.node.model.GLGraphConnection;
import com.vingame.glswing.node.model.GLGraphCyclePolicy;
import com.vingame.glswing.node.model.GLGraphNode;
import com.vingame.glswing.node.model.GLNodeCatalog;
import com.vingame.glswing.node.model.GLNodeConnectionId;
import com.vingame.glswing.node.model.GLNodeEndpoint;
import com.vingame.glswing.node.model.GLNodeGraph;
import com.vingame.glswing.node.model.GLNodeId;
import com.vingame.glswing.node.model.GLNodeTypeRegistry;
import com.vingame.glswing.node.model.GLNodeValueTypeDescriptor;
import com.vingame.glswing.node.model.GLNodeValueTypeRegistry;

/** Owns the model and registries; editing is delegated entirely to the workspace. */
final class WorkspaceController implements AutoCloseable {
    private final GLNodeTypeRegistry types = new GLNodeTypeRegistry();
    private final GLNodeCatalog catalog = new GLNodeCatalog();
    private final GLCompositeGraphRegistry composites = new GLCompositeGraphRegistry();
    private final GLHistoryManager history = new GLHistoryManager();
    private GLNodeGraph graph = new GLNodeGraph();
    private final NodeRegistry nodes = new NodeRegistry(types, catalog);
    private final Map<GLNodeGraph, GLNodeGraph.Listener> observed = new java.util.IdentityHashMap<>();
    private boolean changingProperties;
    private boolean closed;
    private final GLNodeWorkspace workspace;
    private MidiDrivers midiDrivers;
    private MeasurementRecorders measurementRecorders;
    private StepSwitchStates stepSwitchStates;
    private final EditorTemplates editorTemplates;
    private final CompositePortColors compositeColors;
    private int compositeNumber;
    private final java.util.Set<String> extractedGroupIds=new java.util.HashSet<>();
    private AudioHostService audio;
    private GroupBakes bakes;
    private FormulaRuntime formulas;
    private final Map<String,FormulaEditorPanel> formulaEditors=new HashMap<>();
    private boolean preparingBakeInspector;
    private final javax.swing.Timer audioUpdateTimer=new javax.swing.Timer(8,event->publishAudioUpdate());
    private Object rateTopology;
    private Map<GLNodeId,com.vingame.glswing.node.model.GLNodeValue> effectiveRates=Map.of();
    private final Map<GLNodeId,Map<String,com.vingame.glswing.node.model.GLNodeValue>> effectivePorts=new HashMap<>();
    private void collectPortRates(GLNodeId id,GLGraphNode node){
        var ports=effectivePorts.computeIfAbsent(id,key->new HashMap<>());
        node.properties().forEach((key,value)->{if(key.startsWith(com.vin.easyknack.nodes.ProcessingRate.EFFECTIVE+"."))ports.merge(key,value,(a,b)->a.toJavaValue().equals("audio")?a:b);});
    }
    private GLGraphNode withPortRates(GLGraphNode node){
        for(var entry:effectivePorts.getOrDefault(node.id(),Map.of()).entrySet())node=node.withProperty(entry.getKey(),entry.getValue());return node;
    }
    private final Map<com.vingame.glswing.node.GLNode,GLGraphNode> presented=new java.util.IdentityHashMap<>();
    private boolean presentationRefreshPending;
    private final WorkspaceGraphKey rateKeys=new WorkspaceGraphKey(n->List.of(n.id(),n.typeId(),n.properties().getOrDefault("rate",com.vingame.glswing.node.model.GLNodeValue.of(""))));
    private final com.vingame.glswing.history.GLHistoryManagerListener rateHistoryListener=manager->{presented.clear();refreshNodePresentation();};

    WorkspaceController() {
        this(com.vin.easyknack.plugins.PluginManager.current()::buildEphemeral);
    }

    WorkspaceController(GroupBakes.Builder bakeBuilder) {
        audioUpdateTimer.setRepeats(false);
        var valueTypes = new GLNodeValueTypeRegistry();
        for (var category : com.vin.easyknack.nodes.SignalCategory.values())
            valueTypes.register(new GLNodeValueTypeDescriptor(category.valueTypeId(),
                    category==com.vin.easyknack.nodes.SignalCategory.HISTORY?AppText.text("datatype.buffer"):category.name()));
        var inputEditors=new GLNodeInputEditorRegistry();
        inputEditors.addSupportedValueTypes(GLNodeInputEditorRegistry.SLIDER,java.util.Set.of(
                com.vin.easyknack.nodes.SignalCategory.AUDIO.valueTypeId(),com.vin.easyknack.nodes.SignalCategory.VALUE.valueTypeId(),com.vin.easyknack.nodes.SignalCategory.WAVETABLE.valueTypeId()));
        workspace = new GLNodeWorkspace(types, composites, catalog, new AudioNodeFactory(this::setNodeProperty, history, this::observeCurrentGraph),
                GLGraphCyclePolicy.DELAYED_FEEDBACK, history, valueTypes, inputEditors,
                nodes.schemas());
        midiDrivers=new MidiDrivers(this);
        measurementRecorders=new MeasurementRecorders(this);stepSwitchStates=new StepSwitchStates(this);
        workspace.setCompositeViewFactory(new GroupViewFactory(composites,catalog,this::setNodeProperty,history,this::observeCurrentGraph));
        workspace.setGraphHistoryEnabled(true);
        var groupIcon=com.vingame.glswing.rendering.GLIcon.fromPath(new com.vingame.glswing.rendering.GLPath2D()
            .moveTo(4,0).lineTo(16,0).lineTo(16,12).lineTo(4,12).closePath().moveTo(5,1).lineTo(5,11).lineTo(15,11).lineTo(15,1).closePath()
            .moveTo(0,4).lineTo(12,4).lineTo(12,16).lineTo(0,16).closePath().moveTo(1,5).lineTo(1,15).lineTo(11,15).lineTo(11,5).closePath());
        workspace.setNodeCatalogIconProvider(entry->composites.findComposite(entry.type().id()).isPresent()
            ||java.util.Set.of(MultipleGroups.CREATE_TYPE,SpectralGroups.CREATE_TYPE,PartialGroups.CREATE,FormulaGroups.CREATE).contains(entry.type().id())?groupIcon:null);
        workspace.interfaceEditor().setDefaultValueType(com.vin.easyknack.nodes.SignalCategory.VALUE.valueTypeId());
        workspace.setBoundaryPortFactory((port,endpoint)->{
            var asset=composites.findAsset(workspace.currentAssetId()).orElseThrow();
            return GroupEditorCopies.inheritPort(port,endpoint,asset,composites);
        });
        editorTemplates=new EditorTemplates(inputEditors);
        inputEditors.register(new com.vingame.glswing.node.composite.GLNodeInputEditorDescriptor(GroupInputSettings.BUTTON,"Button",java.util.Set.of(com.vin.easyknack.nodes.SignalCategory.TRIGGER.valueTypeId()),Map.of()));
        inputEditors.register(new com.vingame.glswing.node.composite.GLNodeInputEditorDescriptor("easyknack.choice","Combobox",java.util.Set.of(com.vin.easyknack.nodes.SignalCategory.VALUE.valueTypeId(),com.vin.easyknack.nodes.SignalCategory.WAVETABLE.valueTypeId(),com.vin.easyknack.nodes.SignalCategory.AUDIO.valueTypeId()),Map.of()));
        var groupSettings=new GroupSettings(workspace.interfaceEditor(),composites,catalog,types,history,()->{
            workspace.currentGraph().replace("Refresh group presentation",workspace.currentGraph().snapshot());
            compositePresentationChanged();
        },editorTemplates);
        workspace.interfaceEditor().setApplicationSettings(groupSettings,groupSettings::refresh);
        workspace.setGroupEditors(groupSettings.templatesPanel());workspace.setInspectorWidth(300);
        workspace.setInspectorVisible(false);
        compositeColors = new CompositePortColors(workspace, composites);
        bakes=new GroupBakes(this::snapshotDocument,bakeBuilder,this::syncAudio);
        workspace.setCompositePortOrder(com.vingame.glswing.node.GLNodePortOrder.OUTPUTS_FIRST);
        workspace.setGLHistoryManager(history);
        workspace.setCompositeExtractionSupplier(() -> {
            String id = UUID.randomUUID().toString();
            extractedGroupIds.add("easyknack.asset."+id);
            return new GLNodeWorkspace.CompositeExtraction("easyknack.asset." + id,
                    "easyknack.composite." + id, "Audio Group " + ++compositeNumber, "Audio / Groups");
        });
        formulas=new FormulaRuntime(this::syncAudio);
        workspace.setCompositeEditorFactory(asset->FormulaGroups.isFormula(asset)?formulaEditors.computeIfAbsent(asset.id(),id->new FormulaEditorPanel(id,composites,history,formulas,this::syncAudio)):null);
        openExample();
        workspace.editor().addNodeEditorListener(new com.vingame.glswing.node.GLNodeEditorListener() {
            @Override public void selectionChanged(com.vingame.glswing.node.GLNodeEditor editor) { guardActions(); prepareBakeInspector(); }
        });
        guardActions();
        for(var definition:List.of(new com.vin.easyknack.nodes.BinContextNode(),new com.vin.easyknack.nodes.BinOutputNode(),new com.vin.easyknack.nodes.PartialContextNode(),new com.vin.easyknack.nodes.PartialOutputNode()))
            catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(types.find(definition.registration().id()).orElseThrow(),definition.title(),com.vin.easyknack.nodes.NodeCategory.SPECTRAL.path(),definition.defaults(),"bin"));
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(new com.vingame.glswing.node.model.GLNodeTypeDescriptor(SpectralGroups.CREATE_TYPE),com.vin.easyknack.i18n.AppText.literal("New Spectral Group"),com.vin.easyknack.nodes.NodeCategory.SPECTRAL.path(),Map.of(),"bins spectrum"));
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(new com.vingame.glswing.node.model.GLNodeTypeDescriptor(MultipleGroups.CREATE_TYPE),com.vin.easyknack.i18n.AppText.literal("New Instance Group"),com.vin.easyknack.nodes.NodeCategory.ROUTING.path(),Map.of(),"instances additive unison"));
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(new com.vingame.glswing.node.model.GLNodeTypeDescriptor(FormulaGroups.CREATE),AppText.text("formula.new"),com.vin.easyknack.nodes.NodeCategory.MATH.path(),Map.of(),"code formula math"));
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(new com.vingame.glswing.node.model.GLNodeTypeDescriptor(PartialGroups.CREATE),AppText.text("partials.new"),com.vin.easyknack.nodes.NodeCategory.SPECTRAL.path(),Map.of(),"partials tracks"));
        workspace.setNodeCreationFilter(this::nodeAllowedInContext);
        workspace.setNodeCreationHandler((entry,x,y)->{
            if(entry.type().id().equals(PartialGroups.CREATE)){addPartialGroup(x,y);return true;}
            if(entry.type().id().equals(FormulaGroups.CREATE)){addFormulaGroup(x,y);return true;}
            if(entry.type().id().equals(MultipleGroups.CREATE_TYPE)){addMultipleGroup(x,y);return true;}
            if(!entry.type().id().equals(SpectralGroups.CREATE_TYPE))return false;
            addSpectralGroup(x,y);return true;
        });
        workspace.setChangeListener(this::workspaceChanged);
        history.addListener(rateHistoryListener);
    }

    private void compositePresentationChanged(){
        groupBindings.refreshInspector();
        workspace.refreshNodeViews();
        if(compositeColors!=null)compositeColors.schedule();
        if(workspace.currentGraph()!=null)for(var view:workspace.editor().getNodes())
            if(view instanceof GroupViewFactory.GroupView group)compositeColors.modelId(view).flatMap(workspace.currentGraph().snapshot()::findNode).ifPresent(group::update);
        syncAudio();
    }

    GLNodeWorkspace workspace() { return workspace; }
    EditorTemplates editorTemplates(){return editorTemplates;}
    java.util.Set<GLNodeId> selectedNodeIds(){return workspace.editor().getSelectedNodes().stream().flatMap(n->compositeColors.modelId(n).stream()).collect(java.util.stream.Collectors.toSet());}
    void importGroup(GroupImport.Result imported){
        importGroups(new GroupImport.Batch(List.of(imported.selected()),imported.assets(),imported.templates()));
    }
    void importGroups(GroupImport.Batch imported){
        if(isSpectralContext())throw new IllegalArgumentException(AppText.text("group.import.context"));
        var target=workspace.currentGraph();var added=imported.selected().stream().map(asset->new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of())).toList();
        for(var asset:imported.assets()){
            composites.registerAsset(asset);types.register(asset.compositeDescriptor().nodeType());workspace.includeEmbeddedAsset(asset.id());
            var title=new com.vin.easyknack.nodes.NodeState(asset.metadata()).text("easyknack.title","Group");
            var category=com.vin.easyknack.nodes.NodeCategory.catalogPath(new com.vin.easyknack.nodes.NodeState(asset.metadata()).text("easyknack.category",com.vin.easyknack.nodes.NodeCategory.ROUTING.path()));
            catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(asset.compositeDescriptor().nodeType(),title,category,Map.of(),"group"));
        }
        imported.templates().forEach(editorTemplates::put);
        history.perform(AppText.text("group.import.action"),()->target.edit("Import groups",e->added.forEach(e::addNode)),()->target.edit("Remove imported groups",e->added.forEach(n->e.removeNode(n.id()))));
        var views=new java.util.ArrayList<com.vingame.glswing.node.GLNode>();float y=120;
        for(var node:added)for(var view:workspace.editor().getNodes())if(compositeColors.modelId(view).filter(node.id()::equals).isPresent()){view.setLocation(160,y);y+=view.getHeight()+workspace.editor().getHorizontalNodeSpacing();views.add(view);break;}
        workspace.editor().setSelectedNodes(views);
        syncAudio();
    }
    GLNodeEditingActions actions() { guardActions(); return workspace.editor().getEditingActions(); }
    GLHistoryManager history() { return history; }
    void ungroupSelection() {
        var ids=selectedNodeIds();if(ids.size()!=1)return;
        var instance=workspace.currentGraph().snapshot().findNode(ids.iterator().next()).orElseThrow();
        var descriptor=composites.findComposite(instance.typeId()).orElse(null);if(descriptor==null)return;
        var asset=composites.findAsset(descriptor.assetId()).orElseThrow();
        if(MultipleGroups.multiple(asset)||SpectralGroups.spectral(asset)||PartialGroups.partial(asset)||FormulaGroups.isFormula(asset))
            throw new IllegalArgumentException(AppText.text("ungroup.special"));
        if(GLNodeState.isMuted(instance))throw new IllegalArgumentException(AppText.text("ungroup.muted"));
        var root=new GLNodeGraphAsset("ungroup.root",1,GLNodeGraphAssetKind.PROJECT,
            workspace.currentGraph().snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty());
        var prepared=GroupCompiler.prepareUngroup(root,composites);
        var configured=prepared.registry().findAsset(asset.id()).orElseThrow();
        var normalized=nodes.normalize(configured.graph());
        configured=new GLNodeGraphAsset(configured.id(),configured.version(),configured.kind(),normalized,configured.viewState(),configured.metadata(),configured.compositeDescriptor());
        var ungroupDescriptor=configured.compositeDescriptor();
        var ungroupAsset=configured;
        editGroup("Ungroup",()->workspace.ungroup(prepared.root().graph().findNode(instance.id()).orElseThrow(),ungroupAsset,
            (parent,result)->UngroupEditors.remap(parent,result,instance.id(),ungroupDescriptor)));
    }


    private void setInspectorNodeProperty(GLNodeId anchor,com.vin.easyknack.nodes.PropertyId property,GLNodeValue value){
        var selection=selectedNodeIds();var targets=selection.contains(anchor)?java.util.List.copyOf(selection):java.util.List.of(anchor);
        try(var transaction=history.beginTransaction("Change group input")){for(var id:targets)setNodeProperty(id,property,value);}
    }
    void setNodeProperty(GLNodeId id, com.vin.easyknack.nodes.PropertyId property, com.vingame.glswing.node.model.GLNodeValue value) {
        String key = property.value();
        if(midiDrivers!=null)midiDrivers.manualEdit(driverCurrentAsset(),id,key);
        var target = workspace.currentGraph();
        if (target == null) return;
        var node = target.snapshot().findNode(id).orElse(null);
        if(coupledProperties(id).contains(GroupEditorCopies.controlledKey(property)))return;
        if (node == null || value.equals(node.properties().get(key)) && !node.isPropertyAutomatic(key)) return;
        var before = target.snapshot();
        var after = nodes.normalize(new GLNodeGraphSnapshot(before.graphId(), before.revision(), before.nodes().stream()
                .map(n -> n.id().equals(id) ? (key.startsWith(GLGraphNode.AUTOMATIC_PREFIX)?n.withoutProperty(GLGraphNode.AUTOMATIC_DEFAULT_PREFIX+key.substring(GLGraphNode.AUTOMATIC_PREFIX.length())).withProperty(key,value):n.withExplicitProperty(key, value)) : n).toList(), before.connections()),java.util.Set.of(id));
        Runnable apply = () -> replaceProperties(target, "Change " + key, after);
        Runnable undo = () -> replaceProperties(target, "Change " + key, before);
        if (history.isApplying()) apply.run();
        else try (var transaction = history.beginTransaction("Change " + key)) {
            history.perform("Change " + key, apply, undo);
            reconcileCompositeInterfaces();
        }
    }

    /** Also handles property edits originating in the Core inspector, including composites. */
    private void observeCurrentGraph() {
        if (closed) return;
        for(var id:extractedGroupIds)composites.findAsset(id).filter(a->!a.metadata().containsKey("easyknack.kind")).ifPresent(a->{var initialized=GroupKinds.initialize(a,composites);composites.replaceAsset(initialized);types.replace(initialized.compositeDescriptor().nodeType());});
        // Views are created before Core restores their connections. Navigation to an
        // already observed graph still needs a fresh projection once binding finishes.
        if (!presentationRefreshPending) {
            presentationRefreshPending=true;
            java.awt.EventQueue.invokeLater(() -> {
                try {
                    if(closed)return;
                    // guardActions observes the graph again. Keep this refresh pending
                    // until it finishes so observation cannot enqueue itself forever.
                    presented.clear();guardActions();refreshNodePresentation();
                } finally {
                    presentationRefreshPending=false;
                }
            });
        }
        var target = workspace.currentGraph();
        if (target == null || observed.containsKey(target)) return;
        GLNodeGraph.Listener listener = (source, change) -> {
            if (changingProperties || history.isApplying()) return;
            if(midiDrivers!=null)midiDrivers.manualGraphChange(change.before(),change.after());
            boolean parametersChanged = change.after().nodes().stream().anyMatch(n -> {
                var old = change.before().findNode(n.id()).orElse(null);
                if (old == null || NodeRegistry.find(n.typeId()).isEmpty()) return false;
                return !old.withoutProperty(GLNodeState.HIDDEN).withoutProperty(GLNodeState.MUTED).properties()
                        .equals(n.withoutProperty(GLNodeState.HIDDEN).withoutProperty(GLNodeState.MUTED).properties());
            });
            if (!parametersChanged) return;
            var changedIds=change.after().nodes().stream().filter(n->!change.before().findNode(n.id()).map(n::equals).orElse(false)).map(GLGraphNode::id).collect(java.util.stream.Collectors.toSet());
            var after = nodes.normalize(change.after(),changedIds);
            try (var transaction = history.beginTransaction(change.name())) {
                replaceProperties(source, change.name(), after);
                history.record(change.name(), () -> replaceProperties(source, change.name(), change.before()),
                        () -> replaceProperties(source, change.name(), after));
                reconcileCompositeInterfaces();
            }
        };
        observed.put(target, listener); target.addListener(listener);
    }

    private void replaceProperties(GLNodeGraph target, String name, GLNodeGraphSnapshot replacement) {
        Map<GLNodeId, GLNodeViewState> bounds = new HashMap<>();
        Map<GLNodeId, com.vingame.glswing.layout.GLSize> preferredBefore = new HashMap<>();
        List<GLNodeId> selection = workspace.editor().getSelectedNodes().stream().flatMap(n -> compositeColors.modelId(n).stream()).toList();
        var otherSelection = workspace.editor().getSelectedNodes().stream().filter(n -> compositeColors.modelId(n).isEmpty()).toList();
        var prior=target.snapshot();
        var changedIds=replacement.nodes().stream().filter(n->!prior.findNode(n.id()).map(n::equals).orElse(false)).map(GLGraphNode::id).collect(java.util.stream.Collectors.toSet());
        if (workspace.currentGraph() == target) workspace.editor().getNodes().stream().filter(v->compositeColors.modelId(v).filter(changedIds::contains).isPresent()).forEach(view ->
                compositeColors.modelId(view).ifPresent(id -> {
                    bounds.put(id, GLNodeViewState.capture(view)); preferredBefore.put(id, view.getPreferredGLSize());
                }));
        boolean previous = changingProperties; changingProperties = true;
        try {
            target.replace(name, replacement);
            if (workspace.currentGraph() == target) {
                for (var view : workspace.editor().getNodes()) compositeColors.modelId(view).ifPresent(id -> {
                    var state = bounds.get(id);
                    if (state != null) {
                        view.setLocation(state.x(), state.y());
                        var before = preferredBefore.get(id); var after = view.getPreferredGLSize();
                        view.setSize(Math.abs(state.width() - before.width()) < .5f ? after.width() : Math.max(state.width(), after.width()),
                                view.isContentHidden() || Math.abs(state.height() - before.height()) < .5f
                                        ? after.height() : Math.max(state.height(), after.height()));
                    }
                });
                workspace.editor().setSelectedNodes(workspace.editor().getNodes().stream()
                        .filter(view -> compositeColors.modelId(view).map(selection::contains).orElseGet(() -> otherSelection.contains(view))).toList());
            }
        } finally { changingProperties = previous; }
    }

    private boolean reconcilingInterfaces,interfaceReconciliationPending;
    private final Map<String,Object> interfaceValidationKeys=new HashMap<>();
    private final Map<GLNodeGraph,Object> connectionValidationKeys=new WeakHashMap<>();
    private int groupEditDepth;
    private void workspaceChanged(){
        if(reconcilingInterfaces)return;
        if(!closed&&!history.isApplying()&&groupEditDepth==0&&!interfaceReconciliationPending){
            interfaceReconciliationPending=true;
            java.awt.EventQueue.invokeLater(()->{
                interfaceReconciliationPending=false;if(closed||history.isApplying())return;
                try(var transaction=history.beginTransaction("Update group bindings")){reconcileCompositeInterfaces();}
                syncAudio();
            });
        }
        syncAudio();
    }
    private void editGroup(String name,Runnable action){
        groupEditDepth++;
        try(var transaction=history.beginTransaction(name)){action.run();reconcileCompositeInterfaces();}
        finally{groupEditDepth--;}
        syncAudio();
    }
    private void applyGroupRepair(GLNodeGraphAsset asset){
        for(var graph:List.copyOf(observed.keySet()))if(graph.snapshot().graphId().equals(asset.graph().graphId()))
            if(!graph.snapshot().nodes().equals(asset.graph().nodes())||!graph.snapshot().connections().equals(asset.graph().connections()))
                replaceProperties(graph,"Update group bindings",asset.graph());
        composites.replaceAsset(asset);types.replace(asset.compositeDescriptor().nodeType());
    }
    /** Keep public controls and ports, removing only bindings that no longer work. */
    private void reconcileCompositeInterfaces() {
        if(reconcilingInterfaces||closed||history.isApplying())return;
        reconcilingInterfaces=true;
        try{
            boolean changed;
            do {
                changed = false;
                for (var asset : composites.assets()) {
                    var descriptor = asset.compositeDescriptor();
                    if (descriptor == null) continue;
                    var key=List.of(asset,asset.graph().nodes().stream().map(n->List.of(NodeRegistry.find(n.typeId()),composites.findComposite(n.typeId()))).toList());
                    if(key.equals(interfaceValidationKeys.get(asset.id())))continue;
                    var clean=GroupInterfaceValidity.clean(asset,composites);
                    if(clean.equals(asset)){interfaceValidationKeys.put(asset.id(),key);continue;}
                    var repaired=GroupInputs.materialize(clean,composites);
                    history.perform("Update group bindings",()->applyGroupRepair(repaired),()->applyGroupRepair(asset));
                    changed = true;
                }
                for (var dependent : List.copyOf(observed.keySet())) {
                    var before = dependent.snapshot();
                    var key=List.of(before.connections(),before.nodes().stream().map(n->List.of(n.id(),n.typeId(),types.find(n.typeId()).map(t->t.ports()).orElse(List.of()))).toList());
                    if(!changed&&key.equals(connectionValidationKeys.get(dependent)))continue;
                    var after = GroupInterfaceValidity.connections(nodes.normalize(before),composites);
                    connectionValidationKeys.put(dependent,key);
                    if (before.connections().equals(after.connections()) && before.nodes().equals(after.nodes())) continue;
                    replaceProperties(dependent, "Update composite connections", after);
                    history.record("Update composite connections", () -> replaceProperties(dependent, "Restore composite connections", before),
                            () -> replaceProperties(dependent, "Update composite connections", after));
                    changed=true;
                }
            } while (changed);
            if(workspace.interfaceEditor().getAssetId()!=null)workspace.interfaceEditor().refresh();
        }finally{reconcilingInterfaces=false;}
    }

    private void guardActions() {
        if (closed) return;
        observeCurrentGraph();
        var core = workspace.editor().getEditingActions();
        if (core == null || core instanceof NodeEditingActions) return;
        workspace.editor().setEditingActions(new NodeEditingActions(core, () ->
                workspace.editor().getSelectedNodes().stream().allMatch(view -> AudioNodeFactory.idOf(view)
                        .flatMap(id -> workspace.currentGraph().snapshot().findNode(id))
                        .flatMap(n -> NodeRegistry.find(n.typeId())).map(n -> n.isMuteable()).orElse(true)),this::editGroup));
    }

    private void openExample() {
        GLNodeId source = GLNodeId.random(), gain = GLNodeId.random(), output = GLNodeId.random();
        graph.edit("Beispielgraph", edit -> {
            edit.addNode(exampleNode(source, new OscillatorNode()));
            edit.addNode(exampleNode(gain, new GainNode()));
            edit.addNode(exampleNode(output, new OutputNode()));
            edit.addConnection(connection(source, gain));
            edit.addConnection(connection(gain, output));
        });
        var initialView = new GLNodeEditorViewState(Map.of(
                source, new GLNodeViewState(70, 140),
                gain, new GLNodeViewState(390, 200),
                output, new GLNodeViewState(710, 140)));
        workspace.openDocument("Audio Graph", new com.vingame.glswing.node.document.GLNodeGraphDocument(
                new GLNodeGraphAsset("easyknack.root", 1, GLNodeGraphAssetKind.PROJECT, graph.snapshot(), initialView)));
        graph = workspace.currentGraph();
        history.reset();
    }

    boolean preserveUnusedGroups;
    ControlInterpolationSettings controlInterpolation=ControlInterpolationSettings.DEFAULT;
    void controlSettingsChanged(){history.markExternalChange();syncAudio();}
    List<DocumentGroups.Group> documentGroups(){return DocumentGroups.inspect(captureDocument(),workspace.openAssetIds());}
    void deleteUnusedGroup(String id){
        var group=documentGroups().stream().filter(g->g.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException(AppText.text("groups.manager.missing")));
        if(!group.deletable())throw new IllegalArgumentException(AppText.text(group.open()?"groups.manager.open":"groups.manager.used"));
        var asset=composites.findAsset(id).orElseThrow();String type=asset.compositeDescriptor().nodeTypeId();
        var entry=catalog.find(type).orElse(null);var descriptor=types.find(type).orElseThrow();boolean favorite=catalog.isFavorite(type);
        var bypasses=types.muteBypasses(type);var insertion=types.insertionPorts(type).orElse(null);var exclusion=types.exclusionInput(type).orElse(null);
        history.perform(AppText.text("groups.manager.delete"),()->{
            var current=documentGroups().stream().filter(g->g.id().equals(id)).findFirst().orElseThrow();
            if(!current.deletable())throw new IllegalArgumentException(AppText.text(current.open()?"groups.manager.open":"groups.manager.used"));
            workspace.excludeEmbeddedAsset(id);catalog.unregister(type);types.unregister(type);composites.unregisterAsset(id);
            interfaceValidationKeys.remove(id);syncAudio();
        },()->{
            composites.registerAsset(asset);types.register(descriptor);types.setMuteBypasses(type,bypasses);types.setInsertionPorts(type,insertion);types.setExclusionInput(type,exclusion);
            if(entry!=null){catalog.register(entry);if(favorite)catalog.toggleFavorite(type);}
            workspace.includeEmbeddedAsset(id);syncAudio();
        });
    }

    com.vingame.glswing.node.document.GLNodeGraphDocument snapshotDocument() {
        return prepareDocumentSnapshot(captureDocument());
    }
    /** Captures immutable model values and UI positions on the EDT; no document migration. */
    com.vingame.glswing.node.document.GLNodeGraphDocument captureDocument() {
        var source = workspace.snapshotDocument();
        Map<String, GLNodeGraphAsset> assets = new java.util.LinkedHashMap<>();
        for (var asset : NodeDocuments.assets(source)) {
            var state = asset.viewState();
            var contents = asset.graph();
            boolean active = workspace.currentAssetId() == null ? asset.id().equals(source.root().id())
                    : asset.id().equals(workspace.currentAssetId());
            if (active) {
                Map<GLNodeId, GLNodeViewState> positions = new java.util.LinkedHashMap<>();
                Map<String, GLNodeViewState> auxiliary = new java.util.LinkedHashMap<>(state.auxiliaryNodes());
                for (var view : workspace.editor().getNodes()) {
                    var id = compositeColors.modelId(view);
                    if (id.isPresent()) positions.put(id.get(), GLNodeViewState.capture(view));
                    else if (!view.getPorts().isEmpty()) auxiliary.put(
                            view.getPorts().get(0).getDirection() == com.vingame.glswing.node.GLNodePort.Direction.OUTPUT
                                    ? "composite.boundary.input" : "composite.boundary.output", GLNodeViewState.capture(view));
                }
                state = new GLNodeEditorViewState(positions, auxiliary,state.camera());
                contents = workspace.currentGraph().snapshot();
            }
            contents=new GLNodeGraphSnapshot(contents.graphId(),contents.revision(),contents.nodes().stream().map(n->n.withoutProperty(GroupBakes.PROPERTY)).toList(),contents.connections());
            Map<String, com.vingame.glswing.node.model.GLNodeValue> metadata = new HashMap<>(asset.metadata());
            metadata.remove(GroupBakes.SAVED);
            if(bakes!=null){var saved=bakes.saved(contents);if(!saved.isEmpty())metadata.put(GroupBakes.SAVED,com.vingame.glswing.node.model.GLNodeValue.of(saved));}
            if (asset.compositeDescriptor() != null) catalog.find(asset.compositeDescriptor().nodeTypeId()).ifPresent(entry -> {
                metadata.put("easyknack.title", com.vingame.glswing.node.model.GLNodeValue.of(entry.displayName()));
                metadata.put("easyknack.category", com.vingame.glswing.node.model.GLNodeValue.of(entry.category()));
            });
            assets.put(asset.id(), new GLNodeGraphAsset(asset.id(), asset.version(), asset.kind(), contents,
                    state, metadata, asset.compositeDescriptor()));
        }

        var root = assets.remove(source.root().id());
        var rootMetadata=new HashMap<>(root.metadata());rootMetadata.put(ControlInterpolationSettings.KEY,controlInterpolation.value());rootMetadata.put(DocumentSettings.PRESERVE,com.vingame.glswing.node.model.GLNodeValue.of(preserveUnusedGroups));if(!editorTemplates.all().isEmpty())rootMetadata.put(EditorTemplates.KEY,editorTemplates.snapshot());else rootMetadata.remove(EditorTemplates.KEY);
        root=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),root.graph(),root.viewState(),rootMetadata,root.compositeDescriptor());
        return new com.vingame.glswing.node.document.GLNodeGraphDocument(root, assets, source.externalAssets());
    }

    /** Uses an isolated registry so background persistence never reads the live graph. */
    static com.vingame.glswing.node.document.GLNodeGraphDocument prepareDocumentSnapshot(com.vingame.glswing.node.document.GLNodeGraphDocument source){
        var registry=new com.vingame.glswing.node.composite.GLCompositeGraphRegistry();registry.registerDocument(source);
        var assets=new java.util.LinkedHashMap<String,GLNodeGraphAsset>();
        for(var asset:NodeDocuments.assets(source))assets.put(asset.id(),GroupInputs.migrate(asset,registry));
        return new com.vingame.glswing.node.document.GLNodeGraphDocument(assets.remove(source.root().id()),assets,source.externalAssets());
    }

    void openDocument(String title, com.vingame.glswing.node.document.GLNodeGraphDocument input) {
        var document = NodeDocuments.prepare(input); // Validate with isolated registries before changing the editor.
        preserveUnusedGroups=DocumentSettings.preserve(document);
        controlInterpolation=ControlInterpolationSettings.read(document.root().metadata().get(ControlInterpolationSettings.KEY));
        editorTemplates.load(document.root().metadata().get(EditorTemplates.KEY));
        if(midiDrivers!=null)midiDrivers.documentChanged();
        for(var asset:NodeDocuments.assets(document))if(asset.compositeDescriptor()!=null)for(var port:asset.compositeDescriptor().portMappings()){var template=GroupInputs.template(port);if(template!=null&&port.presentation().inputEditorId().startsWith(EditorTemplates.PREFIX)&&editorTemplates.all().stream().noneMatch(t->t.id().equals(template.id())))editorTemplates.register(template);}
        stopAudio();
        bakes.reset();
        workspace.openRoot("", new GLNodeGraph(), GLNodeEditorViewState.empty());
        formulaEditors.values().forEach(FormulaEditorPanel::close);formulaEditors.clear();formulas.reset();
        observed.forEach(GLNodeGraph::removeListener); observed.clear();interfaceValidationKeys.clear();connectionValidationKeys.clear();
        for (var composite : composites.composites()) { types.unregister(composite.nodeTypeId()); catalog.unregister(composite.nodeTypeId()); }
        for (var asset : composites.assets()) composites.unregisterAsset(asset.id());
        for (var asset : NodeDocuments.assets(document)) {
            composites.registerAsset(asset);
            var descriptor = asset.compositeDescriptor();
            if (descriptor != null) {
                types.register(descriptor.nodeType());
                var metadata = new com.vin.easyknack.nodes.NodeState(asset.metadata());
                catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(descriptor.nodeType(),
                        metadata.text("easyknack.title", "Audio Group"), com.vin.easyknack.nodes.NodeCategory.catalogPath(metadata.text("easyknack.category", "Audio / Groups")), Map.of(), "group"));
            }
        }
        // Materialize the oscillator's one-time semantic migration before editors
        // read values or a later user edit could be mistaken for legacy cents.
        var migratedRoot=document.root();var migratedAssets=new java.util.LinkedHashMap<>(document.embeddedAssets());
        for(var asset:NodeDocuments.assets(document)){
            var migrated=asset.graph().nodes().stream().map(node->{var normalized=nodes.normalize(node);
                return NodeRegistry.find(node.typeId()).orElse(null) instanceof com.vin.easyknack.nodes.AdditiveOscillatorNode?normalized:node;}).toList();
            if(migrated.equals(asset.graph().nodes()))continue;
            var snapshot=new GLNodeGraphSnapshot(asset.graph().graphId(),asset.graph().revision(),migrated,asset.graph().connections());
            var replacement=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),snapshot,asset.viewState(),asset.metadata(),asset.compositeDescriptor());
            if(asset.id().equals(document.root().id()))migratedRoot=replacement;else if(migratedAssets.containsKey(asset.id()))migratedAssets.put(asset.id(),replacement);
        }
        document=new GLNodeGraphDocument(migratedRoot,migratedAssets,document.externalAssets());
        workspace.openDocument(title, document);
        graph = workspace.currentGraph();
        if(measurementRecorders!=null)measurementRecorders.documentChanged();
        history.reset(); guardActions(); syncAudio();
        bakes.restore(document);
    }

    private GLGraphNode exampleNode(GLNodeId id, BasicNode definition) {
        return new GLGraphNode(id, definition.registration().id(), definition.defaults());
    }

    private GLGraphConnection connection(GLNodeId output, GLNodeId input) {
        return new GLGraphConnection(GLNodeConnectionId.random(),
                new GLNodeEndpoint(output, "audio-out"), new GLNodeEndpoint(input, "audio-in"));
    }

    void paste() {
        var editor = workspace.editor();
        var point = editor.pointerGraphPosition();
        if (actions().canPaste()) editor.beginPlacement(actions().beginPaste(point[0],point[1]));
    }
    void duplicate() {
        var editor = workspace.editor();
        var point = editor.pointerGraphPosition();
        if (actions().canDuplicate()) editor.beginPlacement(actions().beginDuplicate(point[0],point[1]));
    }

    void showNodeCatalog() {
        var editor = workspace.editor();
        float[] bounds = editor.getLastRenderedScreenBounds();
        if (bounds == null) return;
        actions().requestNodeCreation((80 - editor.getPanX()) / editor.getZoom(),
                (100 - editor.getPanY()) / editor.getZoom(), bounds[0] + 80, bounds[1] + 100);
    }

    void enableAudio(java.util.function.Consumer<String> status, java.util.function.Consumer<String> load) {
        audio = new AudioHostService();
        com.vin.easyknack.audio.ManualTriggers.receiver(audio::trigger);
        audio.setStatusListener(status);
        audio.setLoadListener(load);
        syncAudio();
    }

    void startAudio() {
        if (audio == null) return;
        try { audio.update(audioGraph(),bakes.host()); }
        catch (RuntimeException failure) { audio.invalidGraph(failure.getMessage()); }
        audio.start();
    }

    void stopAudio() { if (audio != null) audio.stop(); }
    void pluginsInstalled(com.vin.easyknack.plugins.PluginManager.Build build) {
        bakes.reset();
        com.vin.easyknack.plugins.PluginManager.current().activate(build);
        nodes.refreshCatalog(catalog);
        syncAudio();
        if(audio!=null)audio.reloadHost();
    }
    void setDiagnosticsListener(java.util.function.Consumer<com.vin.easyknack.audio.AudioDiagnostics> listener){if(audio!=null)audio.setDiagnosticsListener(listener);}
    void resetDiagnostics(){if(audio!=null)audio.resetDiagnostics();}
    void setAudioStateListener(java.util.function.Consumer<AudioHostService.State> listener) { if(audio!=null) audio.setStateListener(listener); }
    void setVolumeKeyListener(java.util.function.Consumer<AudioHostService.VolumeKeyEvent> listener) { if(audio!=null) audio.setVolumeKeyListener(listener); }
    void refreshAudioEndpoints() { if (audio != null) audio.refreshEndpoints(); }

    private final WorkspaceGraphDiagnostics graphDiagnostics=new WorkspaceGraphDiagnostics();
    private void refreshGraphDiagnostics(){
        graphDiagnostics.refresh(workspace,graph,composites,types,this::flattenedAudioGraph,isMultipleContext()&&!isSpectralContext(),isSpectralContext(),presented);
    }
    void refreshAppearance(){
        workspace.interfaceEditor().valueTypes().replace(new GLNodeValueTypeDescriptor(
                com.vin.easyknack.nodes.SignalCategory.HISTORY.valueTypeId(),AppText.text("datatype.buffer")));
        workspace.interfaceEditor().refresh();
        var models=new java.util.ArrayList<GLGraphNode>(graph.snapshot().nodes());
        composites.assets().forEach(a->models.addAll(a.graph().nodes()));
        nodes.refreshLanguage(catalog,models);
        catalog.setCategoryOrder(com.vin.easyknack.nodes.NodeCategory.menuOrder());
        for(var entry:catalog.entries())catalog.replace(new com.vingame.glswing.node.model.GLNodeCatalogEntry(
                entry.type(),AppText.translateExisting(entry.displayName()),AppText.translateExisting(entry.category()),entry.keywords(),entry.defaultProperties()));
        workspace.setCompositeViewFactory(new GroupViewFactory(composites,catalog,this::setNodeProperty,history,this::observeCurrentGraph));
        presented.clear();refreshNodePresentation();compositeColors.refresh();
        for(var view:workspace.editor().getNodes())if(view instanceof BasicNodeView basic)basic.refreshPortColors();
        if(workspace.propertyInspector()!=null)workspace.propertyInspector().refresh();
        LiveLanguage.refresh(workspace);
    }
    private final WorkspaceGroupBindings groupBindings=new WorkspaceGroupBindings();
    private void refreshNodePresentation() {
        if(closed)return;
        groupBindings.refresh(workspace,composites);
        var coupledAsset=workspace.currentAssetId()==null?null:composites.findAsset(workspace.currentAssetId()).orElse(null);
        // Also called after history commits: Core restores views after applying graph edits.
        var current = workspace.currentGraph();
        if (current != null) {
            var snapshot = current.snapshot();
            var topology=rateKeys.update(snapshot,graph.snapshot(),composites.assets());
            boolean connectionsChanged=!topology.equals(rateTopology);
            if(connectionsChanged){
                rateTopology=topology;var rates=new HashMap<GLNodeId,com.vingame.glswing.node.model.GLNodeValue>();
                effectivePorts.clear();
                for(var n:com.vin.easyknack.nodes.AutomaticRates.resolve(snapshot).nodes()){collectPortRates(n.id(),n);if(n.properties().containsKey(com.vin.easyknack.nodes.ProcessingRate.EFFECTIVE))rates.put(n.id(),n.properties().get(com.vin.easyknack.nodes.ProcessingRate.EFFECTIVE));}
                rates.putAll(visibleAutomaticRates());effectiveRates=Map.copyOf(rates);
            }
            var rates=effectiveRates;
            presented.keySet().retainAll(workspace.editor().getNodes());
            for (var view : workspace.editor().getNodes())
                if (view instanceof BasicNodeView basic) {
                    basic.controlledByGroup(coupledProperties(basic.nodeId()));
                    snapshot.findNode(basic.nodeId()).map(n->GroupEditorCopies.previewDefaults(coupledAsset,n)).map(n -> rates.containsKey(n.id())
                            ? n.withProperty(com.vin.easyknack.nodes.ProcessingRate.EFFECTIVE,rates.get(n.id())) : n).map(this::withPortRates).ifPresent(n->{if(connectionsChanged||!n.equals(presented.get(basic))){basic.update(n);presented.put(basic,n);}});
                }else if(view instanceof GroupViewFactory.GroupView group){
                    group.controlledByGroup(coupledProperties(group.nodeId()));
                }
        }
        refreshGraphDiagnostics();
        if(midiDrivers!=null)midiDrivers.refresh();
    }
    private java.util.Set<com.vin.easyknack.nodes.PropertyId> coupledProperties(GLNodeId id){
        return groupBindings.properties(workspace,composites,id);
    }

    private void prepareBakeInspector(){
        if(bakes==null||preparingBakeInspector||closed)return;
        preparingBakeInspector=true;
        try{
            GroupChoices.registerInspector(nodes.schemas(),composites);
            GroupInspectorInputs.register(nodes.schemas(),composites,history,this::setInspectorNodeProperty);
            InstanceCountInspector.register(nodes.schemas(),composites);
            bakes.register(nodes.schemas(),composites);
            registerDriverInspector();
            var current=workspace.currentGraph();if(current==null)return;
            var missing=current.snapshot().nodes().stream().filter(n->composites.findComposite(n.typeId()).isPresent()&&!n.properties().containsKey(GroupBakes.PROPERTY)).toList();
            if(!missing.isEmpty()){
                current.edit("Initialize bake inspector",e->missing.forEach(n->e.setProperty(n.id(),GroupBakes.PROPERTY,com.vingame.glswing.node.model.GLNodeValue.of(false))));
                if(workspace.propertyInspector()!=null)workspace.propertyInspector().refresh();
            }
        }finally{preparingBakeInspector=false;}
    }
    GroupBakes bakes(){return bakes;}
    private void syncAudio() {
        if(closed)return;
        if(formulas!=null){formulas.refresh(composites);for(var entry:formulaEditors.entrySet())if(composites.findAsset(entry.getKey()).isPresent())entry.getValue().refresh();}
        prepareBakeInspector();
        if(bakes!=null)bakes.validate();
        GroupRouting.synchronize(composites,types);
        compositeColors.schedule();
        if(workspace.propertyInspector()!=null)workspace.propertyInspector().synchronizeCustomProperties();
        refreshNodePresentation();
        if (audio != null&&!audioUpdateTimer.isRunning())audioUpdateTimer.start();
    }
    private void publishAudioUpdate(){
        if(audio==null||closed)return;
        try { audio.update(audioGraph(),bakes.host()); }
        catch (RuntimeException failure) { audio.invalidGraph(failure.getMessage()); }
    }

    /** Always compile the root project, even while the UI navigates inside a composite. */
    String audioGraph() {
        try(var policy=ControlMetadata.use(controlInterpolation)){return encodeAudioGraph();}
    }
    private String encodeAudioGraph() {
        GroupRouting.synchronize(composites,types);
        if(audio!=null) audio.setVolumeLinks(audioVolumeLinks());
        bakes.validate();
        var flattened = flattenedAudioGraph(true);
        if (!flattened.isValid()) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Composite-Graph ist nicht ausführbar: ")+flattened.diagnostics());
        var aliases=new java.util.HashMap<String,java.util.List<String>>();
        flattened.origins().forEach((id,origin)->{
            aliases.computeIfAbsent(origin.sourceNodeId().toString(),key->new java.util.ArrayList<>()).add(id.toString());
            for(var instance:origin.instances())aliases.computeIfAbsent(instance.nodeId()+"/"+origin.sourceNodeId(),key->new java.util.ArrayList<>()).add(id.toString());
            String path=origin.sourceNodeId().toString();
            for(int i=origin.instances().size()-1;i>=0;i--){path=origin.instances().get(i).nodeId()+"/"+path;aliases.computeIfAbsent(path,key->new java.util.ArrayList<>()).add(id.toString());}
        });
        for(var node:flattened.graph().orElseThrow().nodes())CompiledManualTargets.addAliases(aliases,node);
        bakes.addTriggerAliases(aliases);
        com.vin.easyknack.audio.AudioAnalysis.aliases(aliases);
        com.vin.easyknack.audio.ManualTriggers.aliases(aliases);
        var executable=executableGraph(flattened.graph().orElseThrow());
        return AudioGraphEncoder.encode(com.vin.easyknack.audio.FeedbackConnections.resolve(executable,types).graph());
    }

    private com.vingame.glswing.node.composite.GLCompositeFlattenResult flattenedAudioGraph(){return flattenedAudioGraph(false);}
    private com.vingame.glswing.node.composite.GLCompositeFlattenResult flattenedAudioGraph(boolean useBakes) {
        return flattenedAudioGraph(useBakes,false);
    }
    private static GLNodeGraphSnapshot withoutPresentationFlags(GLNodeGraphSnapshot snapshot){
        return new GLNodeGraphSnapshot(snapshot.graphId(),snapshot.revision(),snapshot.nodes().stream()
                .map(n->n.withoutProperty(GLNodeState.HIDDEN).withoutProperty(GLNodeState.MUTED)).toList(),snapshot.connections());
    }
    private com.vingame.glswing.node.composite.GLCompositeFlattenResult flattenedAudioGraph(boolean useBakes,boolean volumeTargets) {
        var registry = new GLCompositeGraphRegistry();
        for (var source : GroupCompiler.reachableAssets(graph.snapshot(),composites)) {
            var preparedSource=ControlMetadata.prepare(source,(id,key)->midiDrivers!=null&&midiDrivers.isExternal(source.id(),id,key));
            var asset=useBakes?bakes.apply(preparedSource):preparedSource;
            registry.registerAsset(new GLNodeGraphAsset(asset.id(), asset.version(), asset.kind(), volumeTargets?withoutPresentationFlags(asset.graph()):executableCompositeMutes(asset.graph()),
                asset.viewState(), asset.metadata(), asset.compositeDescriptor()));
        }
        for (var composite : composites.composites())
            if (registry.findComposite(composite.nodeTypeId()).isEmpty()) registry.registerComposite(composite);
        var root = new GLNodeGraphAsset("easyknack.root", 1, GLNodeGraphAssetKind.PROJECT,
                volumeTargets?withoutPresentationFlags(graph.snapshot()):executableCompositeMutes(graph.snapshot()), GLNodeEditorViewState.empty());
        root=ControlMetadata.prepare(root,(id,key)->midiDrivers!=null&&midiDrivers.isExternal(MidiDrivers.ROOT,id,key));
        if(useBakes)root=bakes.apply(root);
        var prepared=GroupCompiler.prepare(root,registry);
        var flat=SpectralGroups.flatten(prepared,new GLCompositeExpansionLimits(16, 256, 512, 10000));
        if(!flat.isValid()||volumeTargets)return flat;
        return new com.vingame.glswing.node.composite.GLCompositeFlattenResult(
            java.util.Optional.of(executableGraph(flat.graph().orElseThrow())),flat.origins(),flat.diagnostics());
    }

    /** Group definitions can have several instances; show the highest rate in use. */
    private Map<GLNodeId,com.vingame.glswing.node.model.GLNodeValue> visibleAutomaticRates() {
        Map<GLNodeId,com.vingame.glswing.node.model.GLNodeValue> result=new HashMap<>();
        Map<GroupPortRates.Key,com.vingame.glswing.node.model.GLNodeValue> groupRates=new HashMap<>();
        compositeColors.setEffectiveRates(groupRates);
        if(composites.composites().isEmpty())return result;
        try {
            var flattened=flattenedAudioGraph();
            if(!flattened.isValid())return result;
            var resolved=com.vin.easyknack.nodes.AutomaticRates.resolve(executableGraph(flattened.graph().orElseThrow()));
            String asset=workspace.currentGraph()==graph?"easyknack.root":workspace.currentAssetId();
            for(var node:resolved.nodes()) {
                var origin=flattened.origins().get(node.id());
                var rate=node.properties().get(com.vin.easyknack.nodes.ProcessingRate.EFFECTIVE);
                if(origin==null||rate==null)continue;
                node.properties().forEach((key,value)->{
                    String prefix=com.vin.easyknack.nodes.ProcessingRate.EFFECTIVE+".input.";
                    if(key.startsWith(prefix))groupRates.merge(new GroupPortRates.Key(origin.sourceAssetId(),origin.sourceNodeId(),key.substring(prefix.length())),value,(a,b)->a.toJavaValue().equals("audio")?a:b);
                });
                groupRates.merge(new GroupPortRates.Key(origin.sourceAssetId(),origin.sourceNodeId()),rate,
                        (a,b)->a.toJavaValue().equals("audio")||b.toJavaValue().equals("control")?a:b);
                if(!origin.sourceAssetId().equals(asset))continue;
                collectPortRates(origin.sourceNodeId(),node);
                result.merge(origin.sourceNodeId(),rate,(a,b)->a.toJavaValue().equals("audio")||b.toJavaValue().equals("control")?a:b);
            }
        } catch(IllegalArgumentException invalidGraph) { /* Keep local display while the graph is incomplete. */ }
        compositeColors.setEffectiveRates(groupRates);
        return result;
    }

    private GLNodeGraphSnapshot executableCompositeMutes(GLNodeGraphSnapshot snapshot) {
        return com.vin.easyknack.graph.GraphExecutionPolicy.compositeMutes(snapshot,composites,types);
    }
    private GLNodeGraphSnapshot executableGraph(GLNodeGraphSnapshot snapshot) {
        return com.vin.easyknack.graph.GraphExecutionPolicy.executable(snapshot,types);
    }

    GroupSourceExporter.Export exportSelectedGroup() {
        var selected=workspace.editor().getSelectedNodes();
        if(selected.size()!=1)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Select exactly one group to export."));
        var id=compositeColors.modelId(selected.iterator().next()).orElseThrow(()->new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Select a group to export.")));
        return GroupSourceExporter.create(snapshotDocument(),workspace.currentGraph().snapshot().findNode(id).orElseThrow());
    }

    java.util.Set<String> audioVolumeLinks() {
        var flattened=flattenedAudioGraph(false,true);
        if(!flattened.isValid()) return java.util.Set.of();
        var result=new java.util.HashSet<String>();
        for(var node:flattened.graph().orElseThrow().nodes()) {
            if(!(NodeRegistry.find(node.typeId()).orElse(null) instanceof OutputNode)) continue;
            var state=new com.vin.easyknack.nodes.NodeState(node.properties());
            if(!state.flag(OutputNode.VOLUME_KEYS,false)) continue;
            var endpoint=com.vin.easyknack.audio.AudioEndpoint.reference(state.text(OutputNode.ENDPOINT,com.vin.easyknack.audio.AudioEndpoint.DEFAULT.reference()));
            if(endpoint.id().equals("default") || endpoint.id().startsWith("out:")) result.add(endpoint.id());
        }
        return java.util.Set.copyOf(result);
    }

    boolean isSpectralContext(){
        return workspace.openAssetIds().stream().anyMatch(id->composites.findAsset(id).map(SpectralGroups::spectral).orElse(false));
    }
    boolean isPartialContext(){return workspace.openAssetIds().stream().anyMatch(id->composites.findAsset(id).map(PartialGroups::partial).orElse(false));}
    boolean isMultipleContext(){return workspace.openAssetIds().stream().anyMatch(id->composites.findAsset(id).map(MultipleGroups::multiple).orElse(false));}
    private boolean nodeAllowedInContext(com.vingame.glswing.node.model.GLNodeCatalogEntry entry){
        if(!ApplicationSettings.experimentalFeatures()&&ExperimentalFeatures.requiresOptIn(entry.type().id(),composites.assets()))return false;
        if(workspace.currentAssetId()!=null&&composites.findAsset(workspace.currentAssetId()).map(FormulaGroups::isFormula).orElse(false))return false;
        var definition=NodeRegistry.find(entry.type().id()).orElse(null);
        if(isPartialContext())return entry.type().id().equals(FormulaGroups.CREATE)||PartialGroups.supports(definition)||definition==null&&composites.findComposite(entry.type().id()).flatMap(d->composites.findAsset(d.assetId())).map(a->!PartialGroups.partial(a)&&!SpectralGroups.spectral(a)&&!MultipleGroups.multiple(a)).orElse(false);
        if(definition!=null&&definition.requiresPartialContext())return false;
        if(definition!=null&&definition.requiresSpectralContext()&&!isSpectralContext())return false;
        if(isMultipleContext()&&!isSpectralContext())return entry.type().id().equals(FormulaGroups.CREATE)||entry.type().id().equals(SpectralGroups.CREATE_TYPE)||entry.type().id().equals(PartialGroups.CREATE)||entry.type().id().equals(MultipleGroups.CREATE_TYPE)||MultipleGroups.supports(definition)||definition==null&&composites.findComposite(entry.type().id()).flatMap(d->composites.findAsset(d.assetId())).isPresent();
        if(isSpectralContext()){
            if(entry.type().id().equals(FormulaGroups.CREATE)||composites.findComposite(entry.type().id()).flatMap(d->composites.findAsset(d.assetId())).map(FormulaGroups::isFormula).orElse(false))return true; if(!SpectralGroups.supports(definition))return false;
            // One output is the program's root. Offer its replacement after deletion.
            return true;
        }
        return !(definition instanceof com.vin.easyknack.nodes.InstanceCombineNode||definition instanceof com.vin.easyknack.nodes.InstanceContextNode);
    }
    List<com.vingame.glswing.node.model.GLNodeCatalogEntry> availableNodes(){return catalog.entries().stream().filter(workspace::isNodeCreationAllowed).toList();}
    void addFormulaGroup(float x,float y){
        var asset=FormulaGroups.create();var descriptor=asset.compositeDescriptor();composites.registerAsset(asset);types.register(descriptor.nodeType());
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(descriptor.nodeType(),"Formula",com.vin.easyknack.nodes.NodeCategory.MATH.path(),Map.of(),"formula"));workspace.includeEmbeddedAsset(asset.id());
        var target=workspace.currentGraph();var node=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());
        history.perform(AppText.text("formula.new"),()->target.edit("Formula",e->e.addNode(node)),()->target.edit("Remove formula",e->e.removeNode(node.id())));
        for(var view:workspace.editor().getNodes())if(compositeColors.modelId(view).filter(node.id()::equals).isPresent()){view.setLocation(x,y);workspace.editor().setSelectedNode(view);break;}syncAudio();
    }
    void addPartialGroup(float x,float y){
        var asset=PartialGroups.create();var d=asset.compositeDescriptor();composites.registerAsset(asset);types.register(d.nodeType());
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(d.nodeType(),AppText.text("partials.group"),com.vin.easyknack.nodes.NodeCategory.SPECTRAL.path(),Map.of(),"partials"));workspace.includeEmbeddedAsset(asset.id());
        var target=workspace.currentGraph();var node=new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of());history.perform(AppText.text("partials.new"),()->target.edit("Partial group",e->e.addNode(node)),()->target.edit("Remove partial group",e->e.removeNode(node.id())));
        for(var view:workspace.editor().getNodes())if(compositeColors.modelId(view).filter(node.id()::equals).isPresent()){view.setLocation(x,y);workspace.editor().setSelectedNode(view);break;}syncAudio();
    }

    void addSpectralGroup(){addSpectralGroup(160,120);}
    private void addSpectralGroup(float x,float y){
        if(isSpectralContext())return;
        var asset=SpectralGroups.create();var descriptor=asset.compositeDescriptor();
        composites.registerAsset(asset);types.register(descriptor.nodeType());
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(descriptor.nodeType(),"Spectral Group",com.vin.easyknack.nodes.NodeCategory.SPECTRAL.path(),Map.of(),"spectral"));
        workspace.includeEmbeddedAsset(asset.id());
        var target=workspace.currentGraph();var node=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());
        history.perform("Add spectral group",()->target.edit("Add spectral group",e->e.addNode(node)),()->target.edit("Remove spectral group",e->e.removeNode(node.id())));
        for(var view:workspace.editor().getNodes())if(compositeColors.modelId(view).filter(node.id()::equals).isPresent()){view.setLocation(x,y);workspace.editor().setSelectedNode(view);break;}
        syncAudio();
    }

    void addMultipleGroup(float x,float y){
        if(isSpectralContext())return;
        var asset=MultipleGroups.create();var descriptor=asset.compositeDescriptor();
        composites.registerAsset(asset);types.register(descriptor.nodeType());
        catalog.register(new com.vingame.glswing.node.model.GLNodeCatalogEntry(descriptor.nodeType(),"Multiple Group",com.vin.easyknack.nodes.NodeCategory.ROUTING.path(),Map.of(),"instances"));
        workspace.includeEmbeddedAsset(asset.id());var target=workspace.currentGraph();var node=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());
        history.perform("Add multiple group",()->target.edit("Add multiple group",e->e.addNode(node)),()->target.edit("Remove multiple group",e->e.removeNode(node.id())));
        for(var view:workspace.editor().getNodes())if(compositeColors.modelId(view).filter(node.id()::equals).isPresent()){view.setLocation(x,y);workspace.editor().setSelectedNode(view);break;}syncAudio();
    }

    String driverCurrentAsset(){return workspace.currentAssetId()==null?MidiDrivers.ROOT:workspace.currentAssetId();}
    Map<String,GLNodeGraphSnapshot> driverGraphs(){
        var result=new java.util.LinkedHashMap<String,GLNodeGraphSnapshot>();result.put(MidiDrivers.ROOT,graph.snapshot());
        composites.assets().forEach(a->result.put(a.id(),a.graph()));
        if(workspace.currentGraph()!=null)result.put(driverCurrentAsset(),workspace.currentGraph().snapshot());return result;
    }
    MidiDrivers.Spec driverGroupSpec(GLGraphNode node,String property){
        var descriptor=composites.findComposite(node.typeId()).orElse(null);if(descriptor==null)return null;
        var asset=GroupInputs.materialize(composites.findAsset(descriptor.assetId()).orElseThrow());
        var choice=GroupChoices.choices(asset,composites).stream().filter(c->c.id().equals(property)).findFirst().orElse(null);
        if(choice!=null){var port=descriptor.portMappings().stream().filter(p->GroupInputs.choice(p)&&GroupInputs.property(p).equals(property)).map(p->p.publicPort().id()).findFirst().orElse(null);return new MidiDrivers.Spec(choice.editor(),port,()->{});}
        var port=descriptor.portMappings().stream().filter(p->GroupUnits.key(p.publicPort().id()).equals(property)).findFirst().orElse(null);
        if(port==null)return null;String id=port.publicPort().id();
        if(port.presentation().inputEditorId().equals(GroupInputSettings.BUTTON))return new MidiDrivers.Spec(new com.vin.easyknack.nodes.Editor.Trigger(),id,()->GroupInputTriggers.fire(composites,node.id().toString(),descriptor,port));
        var options=new com.vin.easyknack.nodes.NodeState(port.presentation().editorOptions());
        if(GroupInputSettings.checkbox(port))return new MidiDrivers.Spec(new com.vin.easyknack.nodes.Editor.Toggle(options.number("default",0)!=0),id,()->{});
        if(!port.presentation().inputEditorId().equals(com.vingame.glswing.node.composite.GLNodeInputEditorRegistry.SLIDER)||GroupVectorEditors.isVector(port))return null;
        Double min=options.flag("minimumEnabled",true)?options.number("minimum",0):null,max=options.flag("maximumEnabled",true)?options.number("maximum",1):null;
        return new MidiDrivers.Spec(new com.vin.easyknack.nodes.Editor.Slider(options.number("default",0),min,max,options.number("step",.01),options.text("numericType","float").equals("integer"),false),id,()->{});
    }
    boolean driverControlled(String asset,GLNodeId node,String property){
        var source=composites.findAsset(asset).orElse(null);if(source==null||source.compositeDescriptor()==null)return false;
        return source.compositeDescriptor().portMappings().stream().flatMap(p->GroupEditorCopies.targets(p).stream()).anyMatch(e->e.nodeId().equals(node)&&e.portId().equals(property));
    }
    void driverConfigure(String asset,GLNodeId node,String property,com.vingame.glswing.node.model.GLNodeValue value){
        if(!asset.equals(driverCurrentAsset()))throw new IllegalStateException("Open the owning group to edit its driver");
        if(value!=null)setNodeProperty(node,new com.vin.easyknack.nodes.PropertyId(property),value);
        else {var target=workspace.currentGraph();var before=target.snapshot();var draft=new GLNodeGraph(before);draft.edit("Remove driver",e->e.removeProperty(node,property));var after=nodes.normalize(draft.snapshot(),java.util.Set.of(node));changingProperties=true;
            try{history.perform(AppText.text("driver.remove"),()->replaceProperties(target,"Remove driver",after),()->replaceProperties(target,"Restore driver",before));}
            finally{changingProperties=false;}}
        prepareBakeInspector();if(workspace.propertyInspector()!=null)workspace.propertyInspector().refresh();syncAudio();
    }
    void driverWrite(String asset,GLNodeId node,String property,com.vingame.glswing.node.model.GLNodeValue value){
        writeExternalProperty(asset,node,property,value,"MIDI driver");
    }
    void writeExternalProperty(String asset,GLNodeId node,String property,com.vingame.glswing.node.model.GLNodeValue value,String action){
        var before=driverGraphs().get(asset);if(before==null)return;var model=before.findNode(node).orElse(null);
        if(model==null||value.equals(model.properties().get(property))&&!model.isPropertyAutomatic(property))return;
        var edited=new GLNodeGraph(before);edited.edit(action,e->e.setExplicitProperty(node,property,value));
        var after=nodes.normalize(edited.snapshot(),java.util.Set.of(node));
        boolean previous=changingProperties;changingProperties=true;
        try{
            var target=asset.equals(driverCurrentAsset())?workspace.currentGraph():asset.equals(MidiDrivers.ROOT)?graph:null;
            if(target!=null)replaceProperties(target,action,after);
            else {var original=composites.findAsset(asset).orElseThrow();composites.replaceAsset(new GLNodeGraphAsset(original.id(),original.version(),original.kind(),after,original.viewState(),original.metadata(),original.compositeDescriptor()));}
        }finally{changingProperties=previous;}
        history.markExternalChange();syncAudio();
    }
    private void registerDriverInspector(){
        if(midiDrivers==null||workspace.currentGraph()==null)return;
        for(var node:workspace.currentGraph().snapshot().nodes())if(selectedNodeIds().contains(node.id())&&composites.findComposite(node.typeId()).isPresent()){
            var properties=new java.util.ArrayList<>(nodes.schemas().properties(node.typeId()));properties.removeIf(p->p.id().startsWith(com.vingame.glswing.ui.GLDriverTarget.PROPERTY_PREFIX));
            node.properties().forEach((key,value)->{if(key.startsWith(com.vingame.glswing.ui.GLDriverTarget.PROPERTY_PREFIX)){
                String property=MidiDriverBinding.property(key);var address=new MidiDrivers.Address(driverCurrentAsset(),node.id(),property);var target=midiDrivers.target(address);if(target==null)return;
                properties.add(new com.vingame.glswing.node.model.GLCustomNodeProperty(key,AppText.text("driver.title"),(model,ignored)->new MidiDriverSettingsPanel(MidiDriverBinding.read(model.properties().getOrDefault(key,value)),target,updated->driverConfigure(driverCurrentAsset(),model.id(),key,updated),MidiDrivers.continuous(midiDrivers.spec(address))),
                    (control,model)->((MidiDriverSettingsPanel)control).update(MidiDriverBinding.read(model.properties().getOrDefault(key,value)))));
            }});nodes.schemas().register(node.typeId(),properties);
        }
    }
    @Override public void close() {
        closed = true; audioUpdateTimer.stop();presented.clear();
        if(midiDrivers!=null)midiDrivers.close();
        if(measurementRecorders!=null)measurementRecorders.close();
        if(stepSwitchStates!=null)stepSwitchStates.close();
        bakes.close();if(formulas!=null)formulas.close();formulaEditors.values().forEach(FormulaEditorPanel::close);formulaEditors.clear();
        history.removeListener(rateHistoryListener);
        compositeColors.close();
        observed.forEach(GLNodeGraph::removeListener); observed.clear();
        if (audio != null) audio.close();
        workspace.close();
    }
}


