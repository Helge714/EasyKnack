package com.vin.easyknack.nodes;

import java.util.*;
import com.vingame.glswing.node.model.*;

/** Derived rates are recalculated from scratch; saved caches never affect execution. */
public final class AutomaticRates {
    private AutomaticRates() { }
    public static GLNodeGraphSnapshot resolve(GLNodeGraphSnapshot graph) {
        var resolver = new Resolver(graph);
        for (var id : resolver.nodes.keySet()) resolver.visit(id);
        return new GLNodeGraphSnapshot(graph.graphId(), graph.revision(), List.copyOf(resolver.nodes.values()), graph.connections());
    }
    /** Shared by ordinary graphs and exported rate plans; never persists derived state. */
    public static Map<String,GLNodeValue> resolveProperties(BasicNode definition,Map<String,GLNodeValue> properties,
            Map<PropertyId,ProcessingRate> incoming) {
        var values=new HashMap<>(properties);
        values.keySet().removeIf(key->key.equals(ProcessingRate.EFFECTIVE)||key.startsWith(ProcessingRate.EFFECTIVE+"."));
        var state=new NodeState(values);
        if(definition instanceof FormulaNode&&state.text("rate","auto").equals("control")){
            int used=(int)state.number("usedInputs",65535);
            if(state.number("triggerOutputs",0)!=0||((int)state.number("triggerInputs",0)&used)!=0||incoming.entrySet().stream().anyMatch(e->(used&(1<<Integer.parseInt(e.getKey().value().substring(2))))!=0&&e.getValue()==ProcessingRate.AUDIO))
                throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.text("formula.controlLoss"));
        }
        var layout=definition.layout(state);
        if(!state.text("rate","").equals("auto")&&layout.rows().stream().noneMatch(NodeBuilder.Row::adaptive))return values;
        var sampled=new HashMap<>(incoming);
        for(var row:layout.rows())if(row.kind()==NodeBuilder.RowKind.INPUT&&sampled.containsKey(row.key())&&row.rates()!=null)
            sampled.put(row.key(),row.rates().resolve(sampled.get(row.key())));
        for(var input:sampled.entrySet())values.put(ProcessingRate.EFFECTIVE+".input."+input.getKey().value(),GLNodeValue.of(input.getValue().id()));
        state=new NodeState(values);
        values.put(ProcessingRate.EFFECTIVE,GLNodeValue.of(definition.resolveAutomaticRate(state,sampled).id()));
        var perOutput=definition.resolveAutomaticOutputRates(new NodeState(values),sampled);
        for(int p=0;p<perOutput.size();p++)values.put(ProcessingRate.EFFECTIVE+".output_"+p,GLNodeValue.of(perOutput.get(p).id()));
        return values;
    }
    private static final class Resolver {
        final Map<GLNodeId,GLGraphNode> nodes = new LinkedHashMap<>();
        final Map<GLNodeId,List<GLGraphConnection>> inputs = new HashMap<>();
        final Map<GLNodeEndpoint,ProcessingRate> outputs = new HashMap<>();
        final Set<GLNodeId> visiting = new HashSet<>(), done = new HashSet<>();
        Resolver(GLNodeGraphSnapshot graph) {
            for (var node : graph.nodes()) {
                var values=new HashMap<>(node.properties());
                values.keySet().removeIf(key->key.equals(ProcessingRate.EFFECTIVE)||key.startsWith(ProcessingRate.EFFECTIVE+"."));
                if(!values.equals(node.properties()))node=new GLGraphNode(node.id(),node.typeId(),node.typeVersion(),values);
                nodes.put(node.id(),node);
            }
            for (var wire : graph.connections()) inputs.computeIfAbsent(wire.input().nodeId(),id->new ArrayList<>()).add(wire);
        }
        void visit(GLNodeId id) {
            // Cycles are rejected by graph validation; remain conservative during editing.
            if(done.contains(id)||!visiting.add(id))return;
            var node=nodes.get(id);
            if(node==null)return;
            var definition=NodeRegistry.find(node.typeId()).orElse(null);
            if(definition!=null) {
                Map<PropertyId,ProcessingRate> incoming=new HashMap<>();
                for(var wire:inputs.getOrDefault(id,List.of())) {
                    var source=wire.output();
                    visit(source.nodeId());
                    incoming.put(new PropertyId(wire.input().portId()),outputs.getOrDefault(source,ProcessingRate.AUDIO));
                }
                node=new GLGraphNode(node.id(),node.typeId(),node.typeVersion(),resolveProperties(definition,node.properties(),incoming));
                nodes.put(id,node);
            }
            if(definition!=null)for(var row:definition.layout(new NodeState(node.properties())).rows())
                if(row.kind()==NodeBuilder.RowKind.OUTPUT)outputs.put(new GLNodeEndpoint(id,row.id()),row.rate());
            visiting.remove(id);done.add(id);
        }
    }
}
