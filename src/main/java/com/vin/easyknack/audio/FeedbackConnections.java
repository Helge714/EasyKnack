package com.vin.easyknack.audio;

import java.util.*;
import com.vingame.glswing.node.model.*;

/** Non-destructive runtime view: cut only connections necessary to break unsafe cycles. */
public final class FeedbackConnections {
    private FeedbackConnections(){}
    private static final String CUT_INPUT = "easyknack.runtime.feedbackCutInput.";
    public static String cutInputProperty(String port){return CUT_INPUT+UUID.nameUUIDFromBytes(port.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public record Result(GLNodeGraphSnapshot graph,List<GLGraphDiagnostic> warnings,Set<GLNodeConnectionId> ignored){}

    /** Preserve delay-bypass destinations before muted nodes disappear. Runtime copies only. */
    public static GLNodeGraphSnapshot markDelayBypasses(GLNodeGraphSnapshot original, GLNodeGraphSnapshot resolved, GLNodeTypeRegistry types){
        Set<GLNodeEndpoint> targets=new HashSet<>();
        for(var node:original.nodes()){
            if(!GLNodeState.isMuted(node))continue;
            var definition=com.vin.easyknack.nodes.NodeRegistry.find(node.typeId()).orElse(null);
            if(!(definition instanceof com.vin.easyknack.nodes.DelayNode)&&!(definition instanceof com.vin.easyknack.nodes.TriggerDelayNode))continue;
            for(var bypass:types.muteBypasses(node.typeId()))
                follow(original,types,new GLNodeEndpoint(node.id(),bypass.outputPortId()),targets,new HashSet<>());
        }
        var nodes=resolved.nodes().stream().map(n->{
            var marked=n;
            for(var target:targets)if(target.nodeId().equals(n.id()))marked=marked.withProperty(cutInputProperty(target.portId()),GLNodeValue.of(true));
            return marked;
        }).toList();
        return new GLNodeGraphSnapshot(resolved.graphId(),resolved.revision(),nodes,resolved.connections());
    }
    private static void follow(GLNodeGraphSnapshot graph,GLNodeTypeRegistry types,GLNodeEndpoint output,Set<GLNodeEndpoint> targets,Set<GLNodeEndpoint> visited){
        if(!visited.add(output))return;
        for(var wire:graph.connections())if(wire.output().equals(output)){
            var target=graph.findNode(wire.input().nodeId()).orElseThrow();
            if(!GLNodeState.isMuted(target))targets.add(wire.input());
            else for(var bypass:types.muteBypasses(target.typeId()))if(bypass.inputPortId().equals(wire.input().portId()))
                follow(graph,types,new GLNodeEndpoint(target.id(),bypass.outputPortId()),targets,visited);
        }
    }
    public static Result resolve(GLNodeGraphSnapshot graph,GLNodeTypeRegistry types){
        var current=graph;
        var ignored=new LinkedHashSet<GLNodeConnectionId>();
        var warnings=new ArrayList<GLGraphDiagnostic>();
        while(true){
            var cycle=GLNodeGraphValidator.validate(current,types,GLGraphCyclePolicy.DELAYED_FEEDBACK).diagnostics().stream()
                .filter(d->d.code().equals("graph.cycle")).findFirst();
            if(cycle.isEmpty())break;
            var candidates=current.connections().stream().filter(w->cycle.get().connectionIds().contains(w.id())).toList();
            var cut=candidates.stream().filter(w->graph.findNode(w.input().nodeId()).orElseThrow().properties().getOrDefault(cutInputProperty(w.input().portId()),GLNodeValue.of(false)).equals(GLNodeValue.of(true)))
                .findFirst().orElse(candidates.get(0));
            ignored.add(cut.id());
            warnings.add(new GLGraphDiagnostic(GLGraphDiagnostic.Severity.WARNING,"feedback.ignored",
                "This feedback connection is not used because the loop has no protecting Delay. This input uses its editor value (or silence). Enable a Delay or break the loop to restore it.",
                Set.of(),Set.of(cut.id())));
            current=new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),graph.nodes(),graph.connections().stream().filter(w->!ignored.contains(w.id())).toList());
        }
        return new Result(current,List.copyOf(warnings),Set.copyOf(ignored));
    }
}
