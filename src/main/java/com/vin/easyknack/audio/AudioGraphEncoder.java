package com.vin.easyknack.audio;

import java.util.HashMap;
import java.util.Map;


import com.vingame.glswing.node.model.*;

/** Protocol 13: source references encode node index + 256 * output index in a signed int. */
public final class AudioGraphEncoder {
    private AudioGraphEncoder() { }

    public static String encode(GLNodeGraphSnapshot graph) {
        graph = com.vin.easyknack.nodes.PortUnitConversions.materialize(com.vin.easyknack.nodes.AutomaticRates.resolve(graph));
        SampleFeedback.validate(graph);
        if (graph.nodes().size() > 256) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Maximal 256 Audio-Nodes"));
        Map<GLNodeId, Integer> indices = new HashMap<>();
        Map<GLNodeId, java.util.List<GLNodePortDescriptor>> ports = new HashMap<>();
        for (int i = 0; i < graph.nodes().size(); i++) {
            var node = graph.nodes().get(i);
            var definition=com.vin.easyknack.nodes.NodeRegistry.require(node.typeId());
            if(definition.requiresPartialContext())throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.text("partials.contextRequired"));
            if(definition.requiresSpectralContext())
                throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.format("spectral.contextRequired",definition.title(),node.id()));
            indices.put(node.id(), i);
            ports.put(node.id(), com.vin.easyknack.nodes.NodeRegistry.require(node.typeId())
                    .layout(new com.vin.easyknack.nodes.NodeState(node.properties())).ports());
        }
        Map<GLNodeEndpoint, Integer> inputs = new HashMap<>();
        Map<GLNodeId,java.util.Set<String>> connectedPorts=new HashMap<>();
        for (var connection : graph.connections()) {
            connectedPorts.computeIfAbsent(connection.input().nodeId(),id->new java.util.HashSet<>()).add(connection.input().portId());
            Integer source = indices.get(connection.output().nodeId());
            if (source == null || !validPort(ports, connection.output(), GLPortDirection.OUTPUT)
                    || !validPort(ports, connection.input(), GLPortDirection.INPUT))
                throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Ungültige Audioverbindung"));
            var fromPort=ports.get(connection.output().nodeId()).stream().filter(p->p.id().equals(connection.output().portId())).findFirst().orElseThrow();
            var toPort=ports.get(connection.input().nodeId()).stream().filter(p->p.id().equals(connection.input().portId())).findFirst().orElseThrow();
            var numeric=java.util.Set.of(com.vin.easyknack.nodes.PortDataType.SIGNAL.id(),com.vin.easyknack.nodes.SignalCategory.VALUE.valueTypeId());
            if(!fromPort.valueTypeId().equals(toPort.valueTypeId())&&!(numeric.contains(fromPort.valueTypeId())&&numeric.contains(toPort.valueTypeId()))
                    &&!(fromPort.valueTypeId().equals(com.vin.easyknack.nodes.PortDataType.SPECTRUM.id())&&toPort.valueTypeId().equals(com.vin.easyknack.nodes.PortDataType.ANALYSIS.id())))
                throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Incompatible port types: use an explicit Trigger/MIDI converter"));
            var outputs=ports.get(connection.output().nodeId()).stream().filter(p->p.direction()==GLPortDirection.OUTPUT).toList();
            int slot=0;while(!outputs.get(slot).id().equals(connection.output().portId()))slot++;
            if (inputs.put(connection.input(), Math.addExact(source,Math.multiplyExact(256,slot))) != null)
                throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Mehrere Quellen an einem Audioeingang"));
        }
        StringBuilder result = new StringBuilder("GRAPH ").append(graph.nodes().size());
        for (var node : graph.nodes()) {
            var definition = com.vin.easyknack.nodes.NodeRegistry.require(node.typeId());
            var layout = definition.layout(new com.vin.easyknack.nodes.NodeState(node.properties()));
            var outputRows=layout.rows().stream().filter(row -> row.kind() == com.vin.easyknack.nodes.NodeBuilder.RowKind.OUTPUT).toList();
            var outputRate=outputRows.stream().map(com.vin.easyknack.nodes.NodeBuilder.Row::rate)
                    .reduce((a,b)->a==com.vin.easyknack.nodes.ProcessingRate.AUDIO||b==com.vin.easyknack.nodes.ProcessingRate.AUDIO?com.vin.easyknack.nodes.ProcessingRate.AUDIO:a==com.vin.easyknack.nodes.ProcessingRate.EVENT||b==com.vin.easyknack.nodes.ProcessingRate.EVENT?com.vin.easyknack.nodes.ProcessingRate.EVENT:com.vin.easyknack.nodes.ProcessingRate.CONTROL).orElse(com.vin.easyknack.nodes.ProcessingRate.AUDIO);
            StringBuilder inputRates = new StringBuilder();
            for (var key : definition.processor().inputs(new com.vin.easyknack.nodes.NodeState(node.properties()))) inputRates.append(layout.rows().stream()
                    .filter(row -> row.kind() == com.vin.easyknack.nodes.NodeBuilder.RowKind.INPUT && row.key().equals(key))
                    .map(row -> row.rate().code()).findFirst().orElse('c'));
            for (var row : layout.rows()) if (row.kind() == com.vin.easyknack.nodes.NodeBuilder.RowKind.INPUT
                    && !definition.processor().inputs(new com.vin.easyknack.nodes.NodeState(node.properties())).contains(row.key())) throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Missing native input binding: ") + row.id());
            result.append(' ').append(node.id()).append(' ').append(definition.processor().id()).append(' ')
                    .append("!").append(com.vin.easyknack.nodes.NativeContractGenerator.fingerprint(definition)).append(' ')
                    .append(definition.encode(new com.vin.easyknack.nodes.NodeState(node.properties()),
                            port -> {
                                var endpoint = new GLNodeEndpoint(node.id(), port.value());
                                if (!validPort(ports, endpoint, GLPortDirection.INPUT))
                                    throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Undeclared input: ") + node.typeId() + "/" + port.value());
                                return inputs.getOrDefault(endpoint, -1);
                            })).append(" @ ").append(outputRateMetadata(layout)).append(' ').append(inputRates.isEmpty() ? "-" : inputRates).append(com.vin.easyknack.ui.ControlMetadata.encode(node,definition,layout,connectedPorts.getOrDefault(node.id(),java.util.Set.of())));
        }
        return result.toString();
    }
    public static String outputRateMetadata(com.vin.easyknack.nodes.NodeBuilder layout){
        var rates=layout.rows().stream().filter(r->r.kind()==com.vin.easyknack.nodes.NodeBuilder.RowKind.OUTPUT)
            .map(r->String.valueOf(r.rate().code())).toList();
        return rates.isEmpty()?"a":rates.stream().distinct().count()==1?rates.get(0):String.join("",rates);
    }
    private static boolean validPort(Map<GLNodeId, java.util.List<GLNodePortDescriptor>> ports,
            GLNodeEndpoint endpoint, GLPortDirection direction) {
        return ports.getOrDefault(endpoint.nodeId(), java.util.List.of()).stream()
                .anyMatch(p -> p.id().equals(endpoint.portId()) && p.direction() == direction);
    }
}
