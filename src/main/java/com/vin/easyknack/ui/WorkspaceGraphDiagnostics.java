package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.node.model.*;

/** Cached application diagnostics and execution-area annotations for the visible graph. */
final class WorkspaceGraphDiagnostics {
    private Object diagnosticKey;
    private final WorkspaceGraphKey keys=new WorkspaceGraphKey(GraphDiagnostics::nodeKey);
    private List<GLGraphDiagnostic> displayDiagnostics=List.of();
    private Set<GLNodeConnectionId> inactive=Set.of();
    private Map<GLNodeId,String> areas=Map.of();
    private final Map<com.vingame.glswing.node.GLNode,String> appliedAreas=new IdentityHashMap<>();
    private java.util.Locale areaLocale;
    void refresh(com.vingame.glswing.node.GLNodeWorkspace workspace,GLNodeGraph graph,
            com.vingame.glswing.node.composite.GLCompositeGraphRegistry composites,GLNodeTypeRegistry types,
            java.util.function.Supplier<com.vingame.glswing.node.composite.GLCompositeFlattenResult> flattened,
            boolean multiple,boolean spectral,Map<com.vingame.glswing.node.GLNode,GLGraphNode> presented){
        var current=workspace.currentGraph();if(current==null)return;
        var snapshot=current.snapshot();
        var locale=ApplicationSettings.locale();
        var key=List.of(keys.update(snapshot,graph.snapshot(),composites.assets()),multiple,spectral,locale,types,com.vin.easyknack.nodes.NodeRegistry.definitionsList());
        if(!key.equals(diagnosticKey)){
            diagnosticKey=key;
            List<GLGraphDiagnostic> visibleDiagnostics;
            try{visibleDiagnostics=GraphDiagnostics.collect(flattened.get(),snapshot,types);}
            catch(IllegalArgumentException incomplete){visibleDiagnostics=List.of();}
            var combined=new ArrayList<>(visibleDiagnostics);
            if(multiple){
                combined.addAll(MultipleGroups.diagnostics(snapshot));
                var shared=MultipleGroups.sharedNodes(snapshot);var labels=new HashMap<GLNodeId,String>();
                for(var node:snapshot.nodes())labels.put(node.id(),com.vin.easyknack.nodes.NodeRegistry.find(node.typeId()).orElse(null) instanceof com.vin.easyknack.nodes.InstanceCombineNode?"Combine":shared.contains(node.id())?"Shared":"Instance");
                areas=Map.copyOf(labels);
            }else if(spectral){combined.addAll(SpectralGroups.diagnostics(snapshot));areas=Map.copyOf(SpectralGroups.executionAreas(snapshot));}
            else areas=Map.of();
            combined.addAll(MutedConnections.diagnostics(snapshot,types));
            displayDiagnostics=combined.stream().distinct().toList();
            inactive=displayDiagnostics.stream().filter(d->d.code().equals("feedback.ignored")||d.code().equals("mute.ignored")).flatMap(d->d.connectionIds().stream()).collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
        if(!locale.equals(areaLocale)){appliedAreas.clear();areaLocale=locale;}
        appliedAreas.keySet().removeIf(view->view.getGLParent()!=workspace.editor());
        workspace.setAdditionalGraphDiagnostics(displayDiagnostics,inactive);
        for(var view:workspace.editor().getNodes())if(view instanceof BasicNodeView basic){
            var area=areas.getOrDefault(basic.nodeId(),"");
            if(!area.equals(appliedAreas.get(basic))){basic.executionArea(area);appliedAreas.put(basic,area);}
            var model=presented.get(basic);if(model!=null)basic.update(model);
        }
    }
}
