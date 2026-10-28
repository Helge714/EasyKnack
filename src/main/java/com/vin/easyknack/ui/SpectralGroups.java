package com.vin.easyknack.ui;

import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Spectral groups are edited normally, but their graph is evaluated over bins, not audio time. */
final class SpectralGroups {
    static final String MARKER="easyknack.spectral";
    static final String SKIP_EMPTY="easyknack.spectral.skipEmptyBins";
    static final String PARALLEL="easyknack.spectral.parallelBins";
    static final String CREATE_TYPE="easyknack.action.spectralgroup";
    static boolean supports(BasicNode node){return node!=null&&(node.supportsSpectralBins()||node instanceof OscillatorNode||node instanceof NoiseNode||node instanceof DigitalNoiseNode||node instanceof InstanceCombineNode||node instanceof InstanceContextNode);}
    static boolean spectral(GLNodeGraphAsset asset){return new NodeState(asset.metadata()).flag(MARKER,false);}
    static Set<GLNodeId> temporalNodes(GLNodeGraphSnapshot graph){
        var result=new HashSet<GLNodeId>();for(var n:graph.nodes()){var d=NodeRegistry.find(n.typeId()).orElse(null);if(d instanceof OscillatorNode||d instanceof NoiseNode||d instanceof DigitalNoiseNode||d instanceof InstanceCombineNode)result.add(n.id());}
        boolean changed;do{changed=false;for(var w:graph.connections())if(result.contains(w.output().nodeId()))changed|=result.add(w.input().nodeId());}while(changed);return result;
    }
    static Map<GLNodeId,String> executionAreas(GLNodeGraphSnapshot graph){
        var temporal=temporalNodes(graph);var shared=MultipleGroups.sharedNodes(graph);var selection=new HashSet<GLNodeId>();
        for(var n:graph.nodes())if(NodeRegistry.find(n.typeId()).orElse(null) instanceof BinDiscardNode)ancestors(n.id(),graph,Set.of(),selection);
        var result=new HashMap<GLNodeId,String>();for(var n:graph.nodes()){var d=NodeRegistry.find(n.typeId()).orElse(null);
            result.put(n.id(),d instanceof BinDiscardNode?"Discard":d instanceof InstanceCombineNode?"Combine":shared.contains(n.id())?"Shared":selection.contains(n.id())?"Selection":temporal.contains(n.id())?"Audio / Bin":"FFT / Bin");}
        return result;
    }
    static List<GLGraphDiagnostic> diagnostics(GLNodeGraphSnapshot graph){
        var temporal=temporalNodes(graph);var result=new ArrayList<>(MultipleGroups.diagnostics(graph));
        for(var n:graph.nodes()){var d=NodeRegistry.find(n.typeId()).orElse(null);
            if(temporal.contains(n.id())&&(d instanceof BinDiscardNode||d instanceof BinOutputNode))result.add(new GLGraphDiagnostic(GLGraphDiagnostic.Severity.ERROR,"spectral.frame-dependency",d.title()+" must depend only on FFT-frame values, not on generated audio.",Set.of(n.id()),Set.of()));}
        return result;
    }
    static GLNodeGraphAsset create(){
        String id="spectral-"+UUID.randomUUID();var context=new BinContextNode();var output=new BinOutputNode();
        var a=new GLGraphNode(GLNodeId.random(),context.registration().id(),context.defaults());var b=new GLGraphNode(GLNodeId.random(),output.registration().id(),output.defaults());
        var graph=new GLNodeGraph();graph.edit("Spectral group",e->{e.addNode(a);e.addNode(b);e.addConnection(wire(a.id(),"real",b.id(),"a"));e.addConnection(wire(a.id(),"imaginary",b.id(),"b"));});
        var ports=List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.SPECTRUM.valueTypeId()),new GLNodeEndpoint(b.id(),"spectrum")),new GLCompositePortMapping(GLNodePortDescriptor.input("spectrum",SignalCategory.SPECTRUM.valueTypeId(),false,1),new GLNodeEndpoint(a.id(),"spectrum")));
        var d=new GLCompositeNodeDescriptor("easyknack.group."+id,id,1,ports);
        var metadata=Map.of(MARKER,GLNodeValue.of(true),"easyknack.title",GLNodeValue.of("Spectral Group"),"easyknack.category",GLNodeValue.of(NodeCategory.SPECTRAL.path()),"easyknack.kind",GLNodeValue.of("SPECTRUM"),GroupRouting.INSERT_IN,GLNodeValue.of("spectrum"),GroupRouting.INSERT_OUT,GLNodeValue.of("out"),GroupRouting.MUTE_IN,GLNodeValue.of("spectrum"),GroupRouting.MUTE_OUT,GLNodeValue.of("out"));
        return new GLNodeGraphAsset(id,1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),new GLNodeEditorViewState(Map.of(a.id(),new GLNodeViewState(200,120),b.id(),new GLNodeViewState(600,120))),metadata,d);
    }
    private static GLGraphConnection wire(GLNodeId a,String out,GLNodeId b,String in){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,out),new GLNodeEndpoint(b,in));}
    static GLCompositeFlattenResult flatten(GroupCompiler.Prepared prepared,GLCompositeExpansionLimits limits){
        var original=prepared.registry();var registry=new GLCompositeGraphRegistry();var spectralTypes=new HashMap<String,GLNodeGraphAsset>();
        for(var asset:original.assets()){
            if(asset.compositeDescriptor()!=null&&(spectral(asset)||MultipleGroups.multiple(asset))){
                spectralTypes.put(asset.compositeDescriptor().nodeTypeId(),asset);
                registry.registerAsset(new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),asset.metadata(),null));
            }else registry.registerAsset(asset);
        }
        for(var d:original.composites())if(!spectralTypes.containsKey(d.nodeTypeId())&&registry.findComposite(d.nodeTypeId()).isEmpty())registry.registerComposite(d);
        var flat=GLCompositeGraphFlattener.flatten(prepared.root(),registry,limits);if(!flat.isValid()||spectralTypes.isEmpty())return flat;
        var graph=flat.graph().orElseThrow();var nodes=new ArrayList<GLGraphNode>();var renamed=new HashMap<GLNodeEndpoint,GLNodeEndpoint>();var extraWires=new ArrayList<GLGraphConnection>();
        for(var node:graph.nodes()){
            var asset=spectralTypes.get(node.typeId());if(asset==null){nodes.add(node);continue;}
            if(MultipleGroups.multiple(asset)){
                var connected=graph.connections().stream().filter(w->w.input().nodeId().equals(node.id())).map(w->w.input().portId()).collect(java.util.stream.Collectors.toSet());
                var expansion=MultipleGroups.compile(node,asset,original,limits,connected,renamed);nodes.addAll(expansion.nodes());extraWires.addAll(expansion.wires());continue;
            }
            var d=asset.compositeDescriptor();
            var ports=d.portMappings().stream().filter(p->!GroupInputs.editorOnly(p)).toList();
            var inputs=ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.INPUT&&!p.publicPort().valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId())).toList();
            if(ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.INPUT&&p.publicPort().valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId())).count()!=1
                ||ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.OUTPUT&&p.publicPort().valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId())).count()>1
                ||ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.OUTPUT).count()>8
                ||ports.stream().anyMatch(p->p.publicPort().direction()==GLPortDirection.OUTPUT&&!Set.of(SignalCategory.SPECTRUM.valueTypeId(),SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId()).contains(p.publicPort().valueTypeId()))
                ||inputs.size()>SpectralGraphNode.MAX_EXTERNAL||inputs.stream().anyMatch(p->!Set.of(SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId(),SignalCategory.WAVETABLE.valueTypeId()).contains(p.publicPort().valueTypeId())||GroupInputSettings.selectedRate(p)==ProcessingRate.EVENT))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("A spectral group needs one Spectrum input, at most one Spectrum output and seven audio Combine outputs, and up to 16 numeric frame inputs."));
            var connected=graph.connections().stream().filter(w->w.input().nodeId().equals(node.id())).map(w->w.input().portId()).collect(java.util.stream.Collectors.toSet());
            var state=new NodeState(asset.metadata());
            int flags=(state.text(SKIP_EMPTY,"false").equals("true")?1:0)|(state.text(PARALLEL,"true").equals("true")?0:2);
            var compiled=program(node,original,limits,inputs,connected,flags);
            for(var p:ports)renamed.put(new GLNodeEndpoint(node.id(),p.publicPort().id()),p.publicPort().direction()==GLPortDirection.OUTPUT?compiled.outputs().get(p.publicPort().id()):compiled.inputs().getOrDefault(p.publicPort().id(),new GLNodeEndpoint(node.id(),inputs.contains(p)?SpectralGraphNode.external(inputs.indexOf(p)).value():"spectrum")));
            nodes.addAll(compiled.shared());extraWires.addAll(compiled.wires());
            var values=new HashMap<String,GLNodeValue>();values.put("program",GLNodeValue.of(compiled.program()));values.put("audioOutputs",GLNodeValue.of(compiled.audioOutputs()));
            values.put(CompiledManualTargets.PROPERTY,GLNodeValue.of(compiled.manualTargets()));
            for(String key:List.of(GLNodeState.MUTED,GLNodeState.HIDDEN))if(node.properties().containsKey(key))values.put(key,node.properties().get(key));
            nodes.add(new GLGraphNode(node.id(),new SpectralGraphNode().registration().id(),values));
        }
        var wires=new ArrayList<>(graph.connections().stream().filter(w->(!renamed.containsKey(w.output())||renamed.get(w.output())!=null)&&(!renamed.containsKey(w.input())||renamed.get(w.input())!=null)).map(w->new GLGraphConnection(w.id(),renamed.getOrDefault(w.output(),w.output()),renamed.getOrDefault(w.input(),w.input()))).toList());wires.addAll(extraWires);
        return new GLCompositeFlattenResult(Optional.of(new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires)),flat.origins(),flat.diagnostics());
    }
    private record Program(String program,Map<String,GLNodeEndpoint> outputs,int audioOutputs,List<GLGraphNode> shared,List<GLGraphConnection> wires,Map<String,GLNodeEndpoint> inputs,Map<String,List<String>> manualTargets){}
    private static Program program(GLGraphNode instance,GLCompositeGraphRegistry registry,GLCompositeExpansionLimits limits,List<GLCompositePortMapping> inputs,Set<String> connected,int flags){
        var graph=new GLNodeGraph();graph.edit("Compile bins",e->e.addNode(instance.withoutProperty(GLNodeState.MUTED).withoutProperty(GLNodeState.HIDDEN)));
        // A boundary sentinel exposes the actual group wiring during flattening; it is never a bin instruction.
        var boundary=new GLGraphNode(GLNodeId.random(),new FFTStreamNode().registration().id(),new FFTStreamNode().defaults());
        var descriptor=registry.findComposite(instance.typeId()).orElseThrow();
        var outputMarkers=new LinkedHashMap<String,GLNodeId>();
        graph.edit("Output boundaries",e->{for(var port:descriptor.portMappings())if(port.publicPort().direction()==GLPortDirection.OUTPUT){
            var id=GLNodeId.random();outputMarkers.put(port.publicPort().id(),id);
            e.addNode(new GLGraphNode(id,new MultipleOutputNode().registration().id(),Map.of()));
            e.addConnection(wire(instance.id(),port.publicPort().id(),id,"in"));
        }});
        graph.edit("Spectrum boundary",e->{e.addNode(boundary);for(var port:descriptor.portMappings())
            if(port.publicPort().direction()==GLPortDirection.INPUT&&port.publicPort().valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId()))
                e.addConnection(wire(boundary.id(),"spectrum",instance.id(),port.publicPort().id()));});
        graph.edit("Frame values",e->{for(int i=0;i<inputs.size();i++){
            var port=inputs.get(i);if(!connected.contains(port.publicPort().id()))continue;
            var def=new BinUniformNode();var values=new HashMap<>(def.defaults());values.put("slot",GLNodeValue.of(i));
            var uniform=new GLGraphNode(GLNodeId.random(),def.registration().id(),values);e.addNode(uniform);
            e.addConnection(wire(uniform.id(),"out",instance.id(),port.publicPort().id()));
        }});
        var root=new GLNodeGraphAsset("bin-program",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());
        var flat=GLCompositeGraphFlattener.flatten(root,registry,limits);if(!flat.isValid())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid spectral group: ")+flat.diagnostics());
        var source=GroupSourceExporter.materializeUnits(AutomaticRates.resolve(resolveMutes(flat.graph().orElseThrow())));
        var roots=new LinkedHashMap<String,GLNodeEndpoint>();
        for(var marker:outputMarkers.entrySet()){
            source.connections().stream().filter(w->w.input().nodeId().equals(marker.getValue())).map(GLGraphConnection::output).findFirst().ifPresent(endpoint->roots.put(marker.getKey(),endpoint));
        }
        if(roots.isEmpty()){
            var out=new BinOutputNode();
            return new Program(SpectralGraphNode.hex("BIN1 1 binoutput !"+NativeContractGenerator.fingerprint(out)+" "+out.encode(new NodeState(out.defaults()),p->-1)),Map.of(),0,List.of(),List.of(),Map.of(),Map.of());
        }
        var needed=new HashSet<GLNodeId>();for(var endpoint:roots.values())ancestors(endpoint.nodeId(),source,Set.of(),needed);
        var boundaries=source.nodes().stream().filter(n->needed.contains(n.id())&&(NodeRegistry.require(n.typeId()) instanceof InstanceCombineNode||NodeRegistry.require(n.typeId()) instanceof BinOutputNode)).map(GLGraphNode::id).collect(java.util.stream.Collectors.toSet());
        if(boundaries.isEmpty())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Use Bin Output or Instance Combine before the group output"));
        var sharedIds=new HashSet<GLNodeId>();for(var endpoint:roots.values())ancestors(endpoint.nodeId(),source,boundaries,sharedIds);sharedIds.removeAll(boundaries);
        var order=new ArrayList<GLGraphNode>();var done=new HashSet<GLNodeId>();
        for(var n:source.nodes())if(NodeRegistry.require(n.typeId()) instanceof BinDiscardNode)visit(n.id(),source,new HashSet<>(),done,order);
        for(var n:source.nodes())if(boundaries.contains(n.id()))visit(n.id(),source,new HashSet<>(),done,order);
        if(order.stream().filter(n->NodeRegistry.require(n.typeId()) instanceof BinDiscardNode).count()>1)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Use one Bin Discard; combine conditions with Math or Comparator"));
        var combines=order.stream().filter(n->NodeRegistry.require(n.typeId()) instanceof InstanceCombineNode).toList();
        if(combines.size()>7)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("A spectral group supports seven audio Combine outputs"));
        var substitutions=new HashMap<GLNodeEndpoint,GLNodeEndpoint>();
        for(var n:order)if(boundaries.contains(n.id())){boolean spectrum=NodeRegistry.require(n.typeId()) instanceof BinOutputNode;substitutions.put(new GLNodeEndpoint(n.id(),spectrum?"spectrum":"out"),new GLNodeEndpoint(instance.id(),spectrum?"out":"audio"+combines.indexOf(n)));}
        var shared=new ArrayList<GLGraphNode>();var sharedWires=new ArrayList<GLGraphConnection>();var sharedInputs=new HashMap<String,GLNodeEndpoint>();
        for(var n:source.nodes())if(sharedIds.contains(n.id())){
            var def=NodeRegistry.require(n.typeId());
            if(def instanceof BinUniformNode){
                int slot=(int)new NodeState(n.properties()).number("slot",0);var pass=new MathNode();var values=new HashMap<>(pass.defaults());values.put("operation",GLNodeValue.of("add"));values.put("a",GLNodeValue.of(0.));values.put("b",GLNodeValue.of(0.));values.put("clamp",GLNodeValue.of(false));
                shared.add(new GLGraphNode(n.id(),pass.registration().id(),values));
                substitutions.put(new GLNodeEndpoint(n.id(),"out"),new GLNodeEndpoint(n.id(),"audio-out"));
                sharedInputs.put(inputs.get(slot).publicPort().id(),new GLNodeEndpoint(n.id(),"a"));
                sharedWires.add(wire(n.id(),"audio-out",instance.id(),SpectralGraphNode.external(slot).value()));
            }else{
                if(done.contains(n.id())||def instanceof BinContextNode||def instanceof BinReadNode||def instanceof InstanceContextNode||def instanceof BinRandomNode||def instanceof BinDiscardNode)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Per-bin values must pass through Instance Combine before shared processing"));
                shared.add(n);
            }
        }
        for(var w:source.connections())if(sharedIds.contains(w.input().nodeId()))sharedWires.add(new GLGraphConnection(w.id(),substitutions.getOrDefault(w.output(),w.output()),w.input()));
        var outputNames=new HashMap<String,GLNodeEndpoint>();roots.forEach((id,endpoint)->outputNames.put(id,substitutions.getOrDefault(endpoint,endpoint)));
        if(order.size()>256)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Spectral group exceeds 256 scalar nodes"));
        var indices=new HashMap<GLNodeId,Integer>();for(int i=0;i<order.size();i++)indices.put(order.get(i).id(),i);
        var wires=new HashMap<GLNodeEndpoint,GLNodeEndpoint>();source.connections().forEach(w->wires.put(w.input(),w.output()));
        var temporal=new HashSet<GLNodeId>();
        for(var n:order){var def=NodeRegistry.require(n.typeId());
            boolean audio=def instanceof OscillatorNode||def instanceof NoiseNode||def instanceof DigitalNoiseNode||def instanceof InstanceCombineNode||source.connections().stream().anyMatch(w->w.input().nodeId().equals(n.id())&&temporal.contains(w.output().nodeId()));
            if(audio)temporal.add(n.id());
            if(audio&&(def instanceof BinDiscardNode||def instanceof BinOutputNode))throw new IllegalArgumentException(def.title()+" must depend only on FFT-frame values, not on generated audio");
            if(def instanceof OscillatorNode&&wires.containsKey(new GLNodeEndpoint(n.id(),"waveform")))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("A custom wavetable needs a complete table, not a scalar bin value"));
            if(source.connections().stream().anyMatch(w->w.input().nodeId().equals(n.id())&&combines.stream().anyMatch(combine->combine.id().equals(w.output().nodeId()))))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Shared Combine outputs cannot feed back into the per-bin area"));
        }
        if(!combines.isEmpty()&&order.size()>64)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("A spectral audio program supports up to 64 per-bin nodes"));
        var result=new StringBuilder(flags!=0?"BIN2 ":"BIN1 ").append(order.size());if(flags!=0)result.append(' ').append(flags);
        for(var node:order){var def=NodeRegistry.require(node.typeId());if(!supports(def))throw new IllegalArgumentException(def.title()+" is not a stateless bin operation. Use nodes supporting independent spectral-bin evaluation.");
            result.append(' ').append(def.processor().id()).append(" !").append(NativeContractGenerator.fingerprint(def)).append(' ').append(def.encode(new NodeState(node.properties(),wires.containsKey(new GLNodeEndpoint(node.id(),"spectrum"))?Set.of(BinReadNode.SPECTRUM):Set.of()),port->{
                var endpoint=wires.get(new GLNodeEndpoint(node.id(),port.value()));if(endpoint==null||def instanceof BinContextNode||def instanceof BinReadNode&&port.equals(BinReadNode.SPECTRUM))return -1;
                var upstream=source.findNode(endpoint.nodeId()).orElseThrow();var layout=NodeRegistry.require(upstream.typeId()).layout(new NodeState(upstream.properties()));
                var ports=layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).toList();int index=0;while(index<ports.size()&&!ports.get(index).id().equals(endpoint.portId()))index++;
                if(index==ports.size())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Missing spectral output ")+endpoint.portId()+" on "+upstream.typeId());return indices.get(endpoint.nodeId())+256*index;
            }));
        }return new Program(SpectralGraphNode.hex(result.toString()),outputNames,combines.size(),shared,sharedWires,sharedInputs,CompiledManualTargets.collect(order,flat,n->Integer.toString(indices.get(n.id()))));
    }
    private static void ancestors(GLNodeId id,GLNodeGraphSnapshot graph,Set<GLNodeId> boundaries,Set<GLNodeId> result){
        if(!result.add(id)||boundaries.contains(id))return;
        for(var w:graph.connections())if(w.input().nodeId().equals(id)&&!w.input().portId().equals("spectrum"))ancestors(w.output().nodeId(),graph,boundaries,result);
    }
    private static GLNodeGraphSnapshot resolveMutes(GLNodeGraphSnapshot graph){
        if(graph.nodes().stream().noneMatch(GLNodeState::isMuted))return graph;
        var contracts=NodeRegistry.executionContracts();var types=contracts.types();
        var definitions=contracts.definitions();
        var eligible=new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),graph.nodes().stream()
            .map(n->NodeRegistry.require(n.typeId()).isMuteable()?n:n.withoutProperty(GLNodeState.MUTED)).toList(),graph.connections());
        // Resolve after flattening so bypasses can cross nested group boundaries,
        // but before traversal so unused branches are not part of the bin program.
        return com.vingame.glswing.node.execution.GLMutedGraphResolver.resolve(definitions.normalize(eligible),types);
    }
    private static void visit(GLNodeId id,GLNodeGraphSnapshot graph,Set<GLNodeId> visiting,Set<GLNodeId> done,List<GLGraphNode> order){
        if(done.contains(id))return;if(!visiting.add(id))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Cyclic bin graph"));
        for(var w:graph.connections())if(w.input().nodeId().equals(id)&&!w.input().portId().equals("spectrum"))visit(w.output().nodeId(),graph,visiting,done,order);
        visiting.remove(id);done.add(id);order.add(graph.findNode(id).orElseThrow());
    }
}
