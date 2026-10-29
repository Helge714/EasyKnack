package com.vin.easyknack.nodes;

import java.util.*;
import java.nio.charset.StandardCharsets;
import com.vingame.glswing.node.model.*;

/** Explicit port interpretation, never inferred from the unit at the opposite end of a wire. */
public final class PortUnitConversions {
    private PortUnitConversions(){}
    public static GLNodeGraphSnapshot materialize(GLNodeGraphSnapshot graph){
        var nodes=new ArrayList<>(graph.nodes());var wires=new ArrayList<>(graph.connections());
        for(var node:graph.nodes()) {
            var properties=new HashMap<>(node.properties());boolean changed=false;
            for(var row:NodeRegistry.require(node.typeId()).layout(new NodeState(node.properties())).rows()) {
                if(row.kind()==NodeBuilder.RowKind.EDITOR || row.formats()==null || row.unit()==row.formats().stored())continue;
                var endpoint=new GLNodeEndpoint(node.id(),row.id());boolean input=row.kind()==NodeBuilder.RowKind.INPUT;
                var connected=wires.stream().filter(w->(input?w.input():w.output()).equals(endpoint)).toList();
                if(connected.isEmpty())continue;
                if(input && connected.size()>1)throw new IllegalArgumentException("Multiple sources on a numeric input");
                var converter=new UnitConverterNode();var values=new HashMap<>(converter.defaults());
                values.put("from",GLNodeValue.of((input?row.unit():row.formats().stored()).id()));
                values.put("to",GLNodeValue.of((input?row.formats().stored():row.unit()).id()));
                values.put("rate",GLNodeValue.of(row.adaptive()?"auto":row.rate().id()));
                var id=new GLNodeId(UUID.nameUUIDFromBytes((node.id()+"/port-unit/"+row.kind()+"/"+row.id()).getBytes(StandardCharsets.UTF_8)));
                if(nodes.stream().anyMatch(n->n.id().equals(id)))throw new IllegalArgumentException("Duplicate port-unit adapter identity");
                nodes.add(new GLGraphNode(id,converter.registration().id(),values));
                var in=new GLNodeEndpoint(id,"value");var out=new GLNodeEndpoint(id,"out");
                wires.removeAll(connected);
                if(input){wires.add(wire(connected.get(0).output(),in));wires.add(wire(out,endpoint));}
                else {wires.add(wire(endpoint,in));for(var connection:connected)wires.add(wire(out,connection.input()));}
                // Idempotent: adapters now own this conversion in the executable graph.
                properties.put(row.unitProperty().value(),GLNodeValue.of(row.formats().stored().id()));changed=true;
            }
            if(changed)nodes.set(nodes.indexOf(node),new GLGraphNode(node.id(),node.typeId(),node.typeVersion(),properties));
        }
        return AutomaticRates.resolve(new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires));
    }
    private static GLGraphConnection wire(GLNodeEndpoint out,GLNodeEndpoint in){
        var id=new GLNodeConnectionId(UUID.nameUUIDFromBytes((out+"/unit-wire/"+in).getBytes(StandardCharsets.UTF_8)));
        return new GLGraphConnection(id,out,in);
    }
}
