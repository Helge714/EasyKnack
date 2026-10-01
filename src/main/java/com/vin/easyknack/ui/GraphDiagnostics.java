package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.audio.SampleFeedback;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;

/** Maps DSP errors back from executable leaves to the currently visible group level. */
final class GraphDiagnostics {
    private GraphDiagnostics(){}
    static List<GLGraphDiagnostic> collect(GLCompositeFlattenResult flat,GLNodeGraphSnapshot visible,GLNodeTypeRegistry types){
        if(!flat.isValid())return flat.diagnostics();
        var graph=AutomaticRates.resolve(flat.graph().orElseThrow());
        var resolved=com.vin.easyknack.audio.FeedbackConnections.resolve(graph,types);
        var problems=new ArrayList<>(SampleFeedback.diagnostics(resolved.graph()));problems.addAll(resolved.warnings());problems.addAll(fftWarnings(resolved.graph()));
        var result=new ArrayList<GLGraphDiagnostic>();
        for(var problem:problems){
            var nodes=new LinkedHashSet<GLNodeId>();
            problem.nodeIds().forEach(id->nodes.addAll(visibleNodes(id,flat,visible)));
            var wires=new LinkedHashSet<GLNodeConnectionId>();
            for(var id:problem.connectionIds()){
                if(visible.findConnection(id).isPresent()){wires.add(id);continue;}
                graph.findConnection(id).ifPresent(w->{
                    var from=visibleNodes(w.output().nodeId(),flat,visible);var to=visibleNodes(w.input().nodeId(),flat,visible);
                    for(var candidate:visible.connections())
                        if(to.contains(candidate.input().nodeId())&&matchesPort(w.input(),candidate.input(),flat)
                            &&upstreamMatches(candidate.output(),w.output(),from,flat,visible,types,new HashSet<>()))wires.add(candidate.id());
                });
            }
            if(!nodes.isEmpty()||!wires.isEmpty())result.add(new GLGraphDiagnostic(problem.severity(),problem.code(),problem.message(),nodes,wires));
        }
        return List.copyOf(result);
    }
    /** A muted node is absent from the executable graph; decorate the last visible wire of its bypass route. */
    private static boolean upstreamMatches(GLNodeEndpoint candidate,GLNodeEndpoint source,Set<GLNodeId> from,GLCompositeFlattenResult flat,GLNodeGraphSnapshot visible,GLNodeTypeRegistry types,Set<GLNodeEndpoint> visited){
        if(!visited.add(candidate))return false;
        if(from.contains(candidate.nodeId())&&matchesPort(source,candidate,flat))return true;
        var node=visible.findNode(candidate.nodeId()).orElse(null);
        if(node==null||!GLNodeState.isMuted(node))return false;
        for(var bypass:types.muteBypasses(node.typeId()))if(bypass.outputPortId().equals(candidate.portId()))
            for(var wire:visible.connections())if(wire.input().nodeId().equals(node.id())&&wire.input().portId().equals(bypass.inputPortId())
                &&upstreamMatches(wire.output(),source,from,flat,visible,types,visited))return true;
        return false;
    }
    private static boolean matchesPort(GLNodeEndpoint leaf,GLNodeEndpoint visible,GLCompositeFlattenResult flat){
        var origin=flat.origins().get(leaf.nodeId());
        return origin==null||!origin.sourceNodeId().equals(visible.nodeId())||leaf.portId().equals(visible.portId());
    }
    private static Set<GLNodeId> visibleNodes(GLNodeId id,GLCompositeFlattenResult flat,GLNodeGraphSnapshot visible){
        var result=new LinkedHashSet<GLNodeId>();
        if(visible.findNode(id).isPresent())result.add(id);
        var origin=flat.origins().get(id);
        if(origin!=null){
            if(visible.findNode(origin.sourceNodeId()).isPresent())result.add(origin.sourceNodeId());
            for(var instance:origin.instances())if(visible.findNode(instance.nodeId()).isPresent())result.add(instance.nodeId());
        }
        return result;
    }
    static List<GLGraphDiagnostic> fftWarnings(GLNodeGraphSnapshot graph){
        var result=new ArrayList<GLGraphDiagnostic>();
        for(var node:graph.nodes())if(NodeRegistry.find(node.typeId()).orElse(null) instanceof IFFTNode
                &&asyncSpectrum(node.id(),graph,new HashSet<>()))
            result.add(new GLGraphDiagnostic(GLGraphDiagnostic.Severity.WARNING,"fft.asynchronous",
                "Asynchronous spectrum: delayed or missing frames may cause audio gaps or artifacts. Enable Synchronous on the source FFT for continuous reconstruction.",Set.of(node.id()),Set.of()));
        return result;
    }
    private static boolean asyncSpectrum(GLNodeId id,GLNodeGraphSnapshot graph,Set<GLNodeId> seen){
        if(!seen.add(id))return false;
        var node=graph.findNode(id).orElseThrow();var def=NodeRegistry.find(node.typeId()).orElse(null);
        if(def instanceof SpectrumNode)return !new NodeState(node.properties()).flag("synchronous",true);
        if(def==null)return false;
        var spectrumInputs=def.layout(new NodeState(node.properties())).ports().stream()
            .filter(p->p.direction()==GLPortDirection.INPUT&&p.valueTypeId().equals(PortDataType.SPECTRUM.id())).map(GLNodePortDescriptor::id).collect(java.util.stream.Collectors.toSet());
        for(var wire:graph.connections())if(wire.input().nodeId().equals(id)&&spectrumInputs.contains(wire.input().portId())&&asyncSpectrum(wire.output().nodeId(),graph,seen))return true;
        return false;
    }
    /** Slider values do not alter legality, except the fixed feedback delay length. */
    static Object key(GLNodeGraphSnapshot graph){
        return List.of(graph.graphId(),graph.connections(),graph.nodes().stream().map(n->List.of(n.id(),n.typeId(),GLNodeState.isMuted(n),
            new NodeState(n.properties()).text("rate","auto"),new NodeState(n.properties()).flag("synchronous",true),n.typeId().contains("delay")?n.properties():Map.of())).toList());
    }
}
