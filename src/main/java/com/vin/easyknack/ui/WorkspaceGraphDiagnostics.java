package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.node.model.*;

/** Cached application diagnostics and execution-area annotations for the visible graph. */
final class WorkspaceGraphDiagnostics {
    private Object diagnosticKey;
    private List<com.vingame.glswing.node.model.GLGraphDiagnostic> visibleDiagnostics=List.of();
    void refresh(com.vingame.glswing.node.GLNodeWorkspace workspace,GLNodeGraph graph,
            com.vingame.glswing.node.composite.GLCompositeGraphRegistry composites,GLNodeTypeRegistry types,
            java.util.function.Supplier<com.vingame.glswing.node.composite.GLCompositeFlattenResult> flattened,
            boolean multiple,boolean spectral,Map<com.vingame.glswing.node.GLNode,GLGraphNode> presented){
        var current=workspace.currentGraph();if(current==null)return;
        var snapshot=current.snapshot();
        var key=List.of(GraphDiagnostics.key(snapshot),GraphDiagnostics.key(graph.snapshot()),composites.assets().stream().map(a->List.of(GraphDiagnostics.key(a.graph()),a.compositeDescriptor()==null?"":a.compositeDescriptor())).toList());
        if(!key.equals(diagnosticKey)){
            diagnosticKey=key;
            try{visibleDiagnostics=GraphDiagnostics.collect(flattened.get(),snapshot,types);}
            catch(IllegalArgumentException incomplete){visibleDiagnostics=List.of();}
        }
        if(multiple){
            var combined=new java.util.ArrayList<>(visibleDiagnostics);combined.addAll(MultipleGroups.diagnostics(snapshot));visibleDiagnostics=combined.stream().distinct().toList();
            var shared=MultipleGroups.sharedNodes(snapshot);
            for(var view:workspace.editor().getNodes())if(view instanceof BasicNodeView basic)basic.executionArea(snapshot.findNode(basic.nodeId()).map(n->n.typeId().equals(new com.vin.easyknack.nodes.InstanceCombineNode().registration().id())).orElse(false)?"Combine":shared.contains(basic.nodeId())?"Shared":"Instance");
        }else if(spectral){
            var combined=new java.util.ArrayList<>(visibleDiagnostics);combined.addAll(SpectralGroups.diagnostics(snapshot));visibleDiagnostics=combined.stream().distinct().toList();
            var areas=SpectralGroups.executionAreas(snapshot);for(var view:workspace.editor().getNodes())if(view instanceof BasicNodeView basic)basic.executionArea(areas.getOrDefault(basic.nodeId(),""));
        }else for(var view:workspace.editor().getNodes())if(view instanceof BasicNodeView basic)basic.executionArea("");
        var combinedMute=new ArrayList<>(visibleDiagnostics);combinedMute.addAll(MutedConnections.diagnostics(snapshot,types));
        var displayDiagnostics=combinedMute.stream().distinct().toList();
        var inactive=displayDiagnostics.stream().filter(d->(d.code().equals("feedback.ignored")||d.code().equals("mute.ignored"))).flatMap(d->d.connectionIds().stream()).collect(java.util.stream.Collectors.toSet());
        workspace.setAdditionalGraphDiagnostics(displayDiagnostics,inactive);
        for(var view:workspace.editor().getNodes())if(view instanceof BasicNodeView basic){var model=presented.get(basic);if(model!=null)basic.update(model);}
    }
}
