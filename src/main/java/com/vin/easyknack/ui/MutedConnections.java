package com.vin.easyknack.ui;

import java.util.*;
import com.vingame.glswing.node.model.*;

/** Mirrors mute bypass resolution without treating an actual zero-valued source as absent. */
final class MutedConnections {
    static List<GLGraphDiagnostic> diagnostics(GLNodeGraphSnapshot graph,GLNodeTypeRegistry types){
        var result=new ArrayList<GLGraphDiagnostic>();
        for(var wire:graph.connections())if(!hasSource(wire.output(),graph,types,new HashSet<>()) || unusedMutedInput(wire.input(),graph,types))
            result.add(new GLGraphDiagnostic(GLGraphDiagnostic.Severity.WARNING,"mute.ignored",AppText.text(unusedMutedInput(wire.input(),graph,types)?"mute.inputIgnored":"mute.ignored"),Set.of(),Set.of(wire.id())));
        return result;
    }
    private static boolean unusedMutedInput(GLNodeEndpoint input,GLNodeGraphSnapshot graph,GLNodeTypeRegistry types){
        var node=graph.findNode(input.nodeId()).orElse(null);
        if(node==null||!GLNodeState.isMuted(node))return false;
        var definition=com.vin.easyknack.nodes.NodeRegistry.find(node.typeId()).orElse(null);
        if(definition!=null&&!definition.isMuteable())return false;
        return types.muteBypasses(node.typeId()).stream().filter(b->b.inputPortId().equals(input.portId()))
            .noneMatch(b->graph.connections().stream().anyMatch(w->w.output().nodeId().equals(node.id())&&w.output().portId().equals(b.outputPortId())));
    }
    private static boolean hasSource(GLNodeEndpoint output,GLNodeGraphSnapshot graph,GLNodeTypeRegistry types,Set<GLNodeEndpoint> path){
        var node=graph.findNode(output.nodeId()).orElse(null);
        if(node==null||!GLNodeState.isMuted(node))return true;
        var definition=com.vin.easyknack.nodes.NodeRegistry.find(node.typeId()).orElse(null);
        if(definition instanceof com.vin.easyknack.nodes.DigitalNoiseNode&&(output.portId().equals("factor")||output.portId().equals("frequency-out")))return true;
        // Audio device inputs deliberately remain connected zero sources when muted.
        if(definition instanceof com.vin.easyknack.nodes.InputNode || definition!=null&&!definition.isMuteable())return true;
        if(!path.add(output))return false;
        for(var bypass:types.muteBypasses(node.typeId()))if(bypass.outputPortId().equals(output.portId()))
            for(var wire:graph.connections())if(wire.input().nodeId().equals(node.id())&&wire.input().portId().equals(bypass.inputPortId())
                    &&hasSource(wire.output(),graph,types,new HashSet<>(path)))return true;
        return false;
    }
}
