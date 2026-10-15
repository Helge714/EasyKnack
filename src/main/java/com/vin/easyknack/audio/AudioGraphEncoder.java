package com.vin.easyknack.audio;

import java.util.HashMap;
import java.util.Map;


import com.vingame.glswing.node.model.*;

/** Protocol 11 (per-output rates and typed resources): source references encode node index + 256 * output index (up to eight outputs). */
public final class AudioGraphEncoder {
    private AudioGraphEncoder() { }

    public static String encode(GLNodeGraphSnapshot graph) {
        graph = com.vin.easyknack.nodes.AutomaticRates.resolve(graph);
        SampleFeedback.validate(graph);
        if (graph.nodes().size() > 256) throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Maximal 256 Audio-Nodes"));
        Map<GLNodeId, Integer> indices = new HashMap<>();
        Map<GLNodeId, java.util.List<GLNodePortDescriptor>> ports = new HashMap<>();
        for (int i = 0; i < graph.nodes().size(); i++) {
            var node = graph.nodes().get(i);
            if (java.util.Set.of("easyknack.spectral.binread", "easyknack.spectral.bincontext", "easyknack.spectral.binoutput").contains(node.typeId()))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Bin nodes belong inside a Spectral Group (Nodes > Add spectral group)."));
            indices.put(node.id(), i);
            ports.put(node.id(), com.vin.easyknack.nodes.NodeRegistry.require(node.typeId())
                    .layout(new com.vin.easyknack.nodes.NodeState(node.properties())).ports());
            if (ports.get(node.id()).stream().filter(p -> p.direction() == GLPortDirection.OUTPUT).count() > 8)
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Maximal acht Ausgänge pro Audio-Node"));
        }
        Map<GLNodeEndpoint, Integer> inputs = new HashMap<>();
        for (var connection : graph.connections()) {
            Integer source = indices.get(connection.output().nodeId());
            if (source == null || !validPort(ports, connection.output(), GLPortDirection.OUTPUT)
                    || !validPort(ports, connection.input(), GLPortDirection.INPUT))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Ungültige Audioverbindung"));
            var fromPort=ports.get(connection.output().nodeId()).stream().filter(p->p.id().equals(connection.output().portId())).findFirst().orElseThrow();
            var toPort=ports.get(connection.input().nodeId()).stream().filter(p->p.id().equals(connection.input().portId())).findFirst().orElseThrow();
            var numeric=java.util.Set.of(com.vin.easyknack.nodes.PortDataType.SIGNAL.id(),com.vin.easyknack.nodes.SignalCategory.VALUE.valueTypeId());
            if(!fromPort.valueTypeId().equals(toPort.valueTypeId())&&!(numeric.contains(fromPort.valueTypeId())&&numeric.contains(toPort.valueTypeId()))
                    &&!(fromPort.valueTypeId().equals(com.vin.easyknack.nodes.PortDataType.SPECTRUM.id())&&toPort.valueTypeId().equals(com.vin.easyknack.nodes.PortDataType.ANALYSIS.id())))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Incompatible port types: use an explicit Trigger/MIDI converter"));
            var outputs=ports.get(connection.output().nodeId()).stream().filter(p->p.direction()==GLPortDirection.OUTPUT).toList();
            int slot=0;while(!outputs.get(slot).id().equals(connection.output().portId()))slot++;
            if (inputs.put(connection.input(), source+256*slot) != null)
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Mehrere Quellen an einem Audioeingang"));
        }
        // Materialize input-unit conversions as ordinary processors: rate scheduling, voice
        // propagation and pruning then apply without special cases in the real-time engine.
        var conversions=new java.util.ArrayList<String>();
        for(var node:graph.nodes()) {
            var layout=com.vin.easyknack.nodes.NodeRegistry.require(node.typeId()).layout(new com.vin.easyknack.nodes.NodeState(node.properties()));
            for(var row:layout.rows())if(row.kind()==com.vin.easyknack.nodes.NodeBuilder.RowKind.INPUT
                    && row.editor() instanceof com.vin.easyknack.nodes.UnitSlider slider
                    && slider.selected()!=slider.formats().stored()) {
                var endpoint=new GLNodeEndpoint(node.id(),row.id());
                Integer source=inputs.get(endpoint);if(source==null)continue;
                int index=graph.nodes().size()+conversions.size();
                if(index>=256)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Maximal 256 Audio-Nodes einschließlich Einheitenumrechnungen"));
                var converter=new com.vin.easyknack.nodes.UnitConverterNode();
                var values=new com.vin.easyknack.nodes.NodeState(Map.of("value",GLNodeValue.of(0.0),"from",GLNodeValue.of(slider.selected().id()),"to",GLNodeValue.of(slider.formats().stored().id())));
                conversions.add(" "+node.id()+".unit."+row.id()+" unitconvert !"+com.vin.easyknack.nodes.NativeContractGenerator.fingerprint(converter)+" "+converter.encode(values,key->source)+" @ "+row.rate().code()+" "+row.rate().code());
                inputs.put(endpoint,index);
            }
        }
        StringBuilder result = new StringBuilder("GRAPH ").append(graph.nodes().size()+conversions.size());
        for (var node : graph.nodes()) {
            var definition = com.vin.easyknack.nodes.NodeRegistry.require(node.typeId());
            var layout = definition.layout(new com.vin.easyknack.nodes.NodeState(node.properties()));
            var outputRows=layout.rows().stream().filter(row -> row.kind() == com.vin.easyknack.nodes.NodeBuilder.RowKind.OUTPUT).toList();
            var outputRate=outputRows.stream().map(com.vin.easyknack.nodes.NodeBuilder.Row::rate)
                    .reduce((a,b)->a==com.vin.easyknack.nodes.ProcessingRate.AUDIO||b==com.vin.easyknack.nodes.ProcessingRate.AUDIO?com.vin.easyknack.nodes.ProcessingRate.AUDIO:a==com.vin.easyknack.nodes.ProcessingRate.EVENT||b==com.vin.easyknack.nodes.ProcessingRate.EVENT?com.vin.easyknack.nodes.ProcessingRate.EVENT:com.vin.easyknack.nodes.ProcessingRate.CONTROL).orElse(com.vin.easyknack.nodes.ProcessingRate.AUDIO);
            StringBuilder inputRates = new StringBuilder();
            for (var key : definition.processor().inputs()) inputRates.append(layout.rows().stream()
                    .filter(row -> row.kind() == com.vin.easyknack.nodes.NodeBuilder.RowKind.INPUT && row.key().equals(key))
                    .map(row -> row.rate().code()).findFirst().orElse('c'));
            for (var row : layout.rows()) if (row.kind() == com.vin.easyknack.nodes.NodeBuilder.RowKind.INPUT
                    && !definition.processor().inputs().contains(row.key())) throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Missing native input binding: ") + row.id());
            result.append(' ').append(node.id()).append(' ').append(definition.processor().id()).append(' ')
                    .append("!").append(com.vin.easyknack.nodes.NativeContractGenerator.fingerprint(definition)).append(' ')
                    .append(definition.encode(new com.vin.easyknack.nodes.NodeState(node.properties()),
                            port -> {
                                var endpoint = new GLNodeEndpoint(node.id(), port.value());
                                if (!validPort(ports, endpoint, GLPortDirection.INPUT))
                                    throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Undeclared input: ") + node.typeId() + "/" + port.value());
                                return inputs.getOrDefault(endpoint, -1);
                            })).append(" @ ").append(outputRateMetadata(layout)).append(' ').append(inputRates.isEmpty() ? "-" : inputRates);
        }
        conversions.forEach(result::append);
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
