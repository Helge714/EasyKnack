package com.vin.easyknack.audio;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

/** Checks sample-feedback islands before submitting a graph to the host. */
public final class SampleFeedback {
    private SampleFeedback(){}
    public static void validate(GLNodeGraphSnapshot graph){
        var problems=diagnostics(graph);
        if(!problems.isEmpty())throw new IllegalArgumentException(problems.get(0).message());
    }
    /** All DSP problems, with stable identities for editor decoration and host rejection. */
    public static List<GLGraphDiagnostic> diagnostics(GLNodeGraphSnapshot graph){
        var problems=new LinkedHashSet<GLGraphDiagnostic>();
        var nodes=graph.nodes();var indices=new HashMap<GLNodeId,Integer>();var reach=new BitSet[nodes.size()];
        for(int i=0;i<nodes.size();i++){indices.put(nodes.get(i).id(),i);reach[i]=new BitSet(nodes.size());}
        for(var wire:graph.connections()){
            var to=indices.get(wire.input().nodeId());var from=indices.get(wire.output().nodeId());
            if(to!=null&&from!=null&&!nodes.get(to).typeId().equals("easyknack.audio.blockdelay"))reach[from].set(to);
        }
        for(int k=0;k<nodes.size();k++)for(int i=0;i<nodes.size();i++)if(reach[i].get(k))reach[i].or(reach[k]);
        for(int delay=0;delay<nodes.size();delay++)if(nodes.get(delay).typeId().equals("easyknack.audio.sampledelay")||nodes.get(delay).typeId().startsWith("easyknack.audio.delay")||nodes.get(delay).typeId().equals("easyknack.modulation.triggerdelay")){
            boolean blockSafe=true;
            for(int i=0;i<nodes.size();i++)if(reach[delay].get(i)&&reach[i].get(delay)){
                var n=nodes.get(i);var d=NodeRegistry.require(n.typeId());
                if(d instanceof SampleDelayNode||d instanceof TriggerDelayNode)blockSafe=false;
                if(d instanceof DelayNode&&(new NodeState(n.properties()).number(DelayNode.TIME,.25)+1e-9<(double)AudioProcessing.BLOCK_FRAMES/AudioProcessing.SAMPLE_RATE||graph.connections().stream().anyMatch(w->w.input().nodeId().equals(n.id())&&w.input().portId().equals(DelayNode.TIME.value()))))blockSafe=false;
            }
            for(int i=0;i<nodes.size();i++)if(reach[delay].get(i)&&reach[i].get(delay)){
                var node=nodes.get(i);var definition=NodeRegistry.require(node.typeId());
                if(!blockSafe&&!definition.supportsSampleFeedback())problems.add(problem("feedback.unsupported", "Sample feedback is not supported by "+definition.title()+". Use a fixed Delay of at least one block ("+AudioProcessing.BLOCK_FRAMES+" samples), or move this node outside the loop.",node.id(),Set.of()));
                final boolean wholeBlock=blockSafe;
                var blockInputs=definition.layout(new NodeState(node.properties())).rows().stream()
                        .filter(row->row.kind()==NodeBuilder.RowKind.INPUT&&(!wholeBlock&&row.dataType()==PortDataType.WAVEFORM||definition instanceof DelayNode&&row.key().equals(DelayNode.TIME)||definition instanceof TriggerDelayNode&&(row.key().equals(TriggerDelayNode.TIME)||row.key().equals(TriggerDelayNode.RESET)))).map(NodeBuilder.Row::id).toList();
                for(var wire:graph.connections())if(wire.input().nodeId().equals(node.id())&&blockInputs.contains(wire.input().portId())){
                    var source=indices.get(wire.output().nodeId());
                    if(source!=null&&reach[delay].get(source)&&reach[source].get(delay))
                        problems.add(problem("feedback.block-input",(definition instanceof DelayNode?"Delay time":definition instanceof TriggerDelayNode?"Trigger Delay time/reset":"Wavetable")+" must originate outside its feedback loop.",node.id(),Set.of(wire.id())));
                }
                if(!blockSafe&&!definition.supportsTriggerFeedback()&&definition.layout(new NodeState(node.properties())).rows().stream().anyMatch(row->row.kind()==NodeBuilder.RowKind.INPUT&&row.rate()==ProcessingRate.EVENT))
                    problems.add(problem("feedback.trigger","Trigger ports cannot run inside sample feedback. Use a fixed Delay of at least one block, or move "+definition.title()+" outside the loop.",node.id(),graph.connections().stream().filter(w->w.input().nodeId().equals(node.id())&&definition.layout(new NodeState(node.properties())).rows().stream().anyMatch(r->r.id().equals(w.input().portId())&&r.rate()==ProcessingRate.EVENT)).map(GLGraphConnection::id).collect(java.util.stream.Collectors.toSet())));
            }
        }
        return List.copyOf(problems);
    }
    private static GLGraphDiagnostic problem(String code,String message,GLNodeId node,Set<GLNodeConnectionId> wires){
        return new GLGraphDiagnostic(GLGraphDiagnostic.Severity.ERROR,code,message,Set.of(node),wires);
    }
}
