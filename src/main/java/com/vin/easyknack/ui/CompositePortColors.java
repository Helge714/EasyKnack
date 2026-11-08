package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;

/** Semantic colors for the views created internally by the external Core workspace. */
final class CompositePortColors implements GLNodeEditorListener {
    private final GLNodeWorkspace workspace;
    private final GLCompositeGraphRegistry composites;
    private final Map<GLNode, GLNodeId> groups = new IdentityHashMap<>();
    private boolean pending;
    private boolean closed;
    private GroupPortRates portRates;

    void setEffectiveRates(Map<GroupPortRates.Key, GLNodeValue> rates) { portRates = new GroupPortRates(composites, Map.copyOf(rates)); }

    CompositePortColors(GLNodeWorkspace workspace, GLCompositeGraphRegistry composites) {
        this.workspace = workspace; this.composites = composites;
        portRates = new GroupPortRates(composites, Map.of());
        workspace.editor().addNodeEditorListener(this);
    }

    @Override public void nodeAdded(GLNodeEditor editor, GLNode view) {
        if (!(view instanceof BasicNodeView)) view.setResizeMode(GLNodeResizeMode.HORIZONTAL);
        if (AudioNodeFactory.idOf(view).isEmpty() && workspace.currentGraph() != null) {
            // Core projects new model nodes in snapshot order, before adding boundary views.
            // Track identity at creation; never infer identity from titles or screen positions.
            workspace.currentGraph().snapshot().nodes().stream()
                    .filter(n -> composites.findComposite(n.typeId()).isPresent())
                    .filter(n -> !groups.containsValue(n.id())).findFirst()
                    .ifPresent(n -> groups.put(view, n.id()));
        }
        schedule();
    }
    @Override public void nodeRemoved(GLNodeEditor editor, GLNode view) { groups.remove(view); schedule(); }
    @Override public void connectionAdded(GLNodeEditor editor, GLNodeConnection connection) { schedule(); }
    @Override public void connectionRemoved(GLNodeEditor editor, GLNodeConnection connection) { schedule(); }

    void schedule() {
        if (pending || closed) return;
        pending = true;
        java.awt.EventQueue.invokeLater(() -> { pending = false; if (!closed) refresh(); });
    }

    void close() { closed = true; workspace.editor().removeNodeEditorListener(this); groups.clear(); }

    Optional<GLNodeId> modelId(GLNode view) { return AudioNodeFactory.idOf(view).or(() -> Optional.ofNullable(groups.get(view))); }

    void refresh() {
        var graph = workspace.currentGraph();
        if (graph == null) return;
        var snapshot = graph.snapshot();
        var rates = portRates;
        groups.forEach((view, id) -> {
            var model = snapshot.findNode(id).orElse(null);
            if (model != null) composites.findComposite(model.typeId()).ifPresent(descriptor -> applyRates(view, descriptor, rates));
        });
        // Boundary views use the explicit public contract too, including unbound ports.
        var assetId = workspace.currentAssetId();
        if (assetId != null) composites.findAsset(assetId).ifPresent(asset -> {
            if (asset.compositeDescriptor() != null)
                workspace.editor().getNodes().stream().filter(this::isBoundary)
                        .forEach(view -> applyRates(view, asset.compositeDescriptor(), rates));
        });
    }

    private boolean isBoundary(GLNode view) {
        return AudioNodeFactory.idOf(view).isEmpty() && !groups.containsKey(view);
    }

    private static void applyRates(GLNode view, GLCompositeNodeDescriptor descriptor, GroupPortRates rates) {
        apply(view, descriptor);
        for (var mapping : descriptor.portMappings()) {
            var port = view.findPort(mapping.publicPort().id());
            if (port == null) continue;
            var rate = rates.rate(descriptor, mapping);
            port.setShape(rate.shape());
            port.setToolTipText(mapping.publicPort().direction() == GLPortDirection.OUTPUT ? rate.outputHint() : rate.inputHint());
        }
    }

    static void apply(GLNode view, GLCompositeNodeDescriptor descriptor) {
        for (var mapping : descriptor.portMappings()) {
            var port = view.findPort(mapping.publicPort().id());
            if (port != null) {
                if(!GroupEditorCopies.targets(mapping).isEmpty())port.setSocketVisible(false);
                var category = Arrays.stream(SignalCategory.values())
                        .filter(c -> c.valueTypeId().equals(mapping.publicPort().valueTypeId()))
                        .findFirst().orElse(SignalCategory.AUDIO);
                port.setColor(ApplicationTheme.current().signal(category));
                port.setConnectionDashed(com.vin.easyknack.nodes.PortDataType.isReference(category.valueTypeId()));
                if(category==SignalCategory.MIDI){port.setShape(GLNodePort.Shape.DOUBLE_DIAMOND);port.setToolTipText(com.vin.easyknack.i18n.AppText.literal("MIDI events with sample positions."));}
            }
        }
    }
}
