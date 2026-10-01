package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;

/** Rates belong to EasyKnack; public signal categories remain independent. */
final class GroupPortRates {
    record Key(String asset, GLNodeId node,String port) { Key(String asset,GLNodeId node){this(asset,node,"");} }
    private final GLCompositeGraphRegistry registry;
    private final Map<Key, GLNodeValue> effective;
    private final Map<String, GLNodeGraphSnapshot> graphs = new HashMap<>();
    private record PortKey(GLCompositeNodeDescriptor descriptor, GLCompositePortMapping mapping) { }
    private final Map<PortKey, ProcessingRate> resolved = new HashMap<>();

    GroupPortRates(GLCompositeGraphRegistry registry, Map<Key, GLNodeValue> effective) {
        this.registry = registry; this.effective = effective;
    }

    ProcessingRate rate(GLCompositeNodeDescriptor descriptor, GLCompositePortMapping mapping) {
        return resolved.computeIfAbsent(new PortKey(descriptor, mapping), key -> rate(descriptor, mapping, new HashSet<>()));
    }

    private ProcessingRate rate(GLCompositeNodeDescriptor descriptor, GLCompositePortMapping mapping, Set<String> visiting) {
        var requested=GroupInputSettings.selectedRate(mapping);
        var asset=registry.findAsset(descriptor.assetId()).orElse(null);
        if(FormulaGroups.isFormula(asset)&&mapping.publicPort().direction()==GLPortDirection.OUTPUT&&requested!=ProcessingRate.EVENT){
            var rate=new NodeState(asset.metadata()).text(FormulaGroups.RATE,"auto");if(!rate.equals("auto"))return ProcessingRate.fromId(rate);
            var override=effective.get(new Key(asset.id(),FormulaGroups.kernelId(asset)));if(override!=null)return ProcessingRate.fromId((String)override.toJavaValue());
        }
        if(requested!=ProcessingRate.AUTO)return requested;
        String key = descriptor.assetId() + "/" + mapping.publicPort().id();
        if (!visiting.add(key)) return ProcessingRate.AUDIO;
        try {
            var graph = graphs.computeIfAbsent(descriptor.assetId(), id -> registry.findAsset(id)
                    .map(a -> AutomaticRates.resolve(a.graph())).orElse(null));
            if (graph == null) return ProcessingRate.AUDIO;
            var result = SignalCategory.MIDI.valueTypeId().equals(mapping.publicPort().valueTypeId())
                    ? ProcessingRate.EVENT : ProcessingRate.CONTROL;
            for (var endpoint : mapping.internalEndpoints()) {
                var node = graph.findNode(endpoint.nodeId()).orElse(null);
                if (node == null) continue;
                var nested = registry.findComposite(node.typeId()).orElse(null);
                if (nested != null) {
                    for (var child : nested.portMappings()) if (child.publicPort().id().equals(endpoint.portId()))
                        result = maximum(result, rate(nested, child, visiting));
                } else {
                    var definition = NodeRegistry.find(node.typeId()).orElse(null);
                    if (definition == null) return ProcessingRate.AUDIO;
                    var override = effective.get(new Key(descriptor.assetId(), node.id()));
                    if (override != null) node = node.withProperty(ProcessingRate.EFFECTIVE, override);
                    var inputRate=effective.get(new Key(descriptor.assetId(),node.id(),endpoint.portId()));
                    if(inputRate!=null)node=node.withProperty(ProcessingRate.EFFECTIVE+".input."+endpoint.portId(),inputRate);
                    for (var row : definition.layout(new NodeState(node.properties())).rows())
                        if (row.kind() != NodeBuilder.RowKind.EDITOR && row.id().equals(endpoint.portId()))
                            result = maximum(result, row.rate());
                }
            }
            return result;
        } finally { visiting.remove(key); }
    }

    static ProcessingRate maximum(ProcessingRate a, ProcessingRate b) {
        if (a == ProcessingRate.AUDIO || b == ProcessingRate.AUDIO) return ProcessingRate.AUDIO;
        if (a == ProcessingRate.EVENT || b == ProcessingRate.EVENT) return ProcessingRate.EVENT;
        return ProcessingRate.CONTROL;
    }
}
