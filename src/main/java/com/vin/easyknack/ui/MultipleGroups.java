package com.vin.easyknack.ui;
import com.vin.easyknack.compiler.CompiledManualTargets;
import java.util.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Compiles an editable group into independent scheduled instances, not cloned editor nodes. */
final class MultipleGroups {
    static final String MARKER="easyknack.multiple",MAXIMUM="easyknack.multiple.maximum",MERGE="easyknack.multiple.merge",CREATE_TYPE="easyknack.action.multiple",COUNT_PROPERTY="easyknack.instance.count",EXECUTION="easyknack.instance.execution";
    private static final Set<String> SUPPORTED=Set.of(
        "formula","stepswitch",
        "residualmodel","partialanalysis","partialsspectrum","partialgraph","spectrummath","spectrummix","spectrumblur","spectrumshift","spectrumprune","spectrumcorrelation","spectrumstatistics","spectralfeatures","correlationfeatures","analysisaverage",
        "valueaverage","levelanalysis","pitchdetection","envelope","triggerinput","triggercounter","triggerdelay","triggerdivider",
        "triggerfilter","triggermerge","triggertimer","history","historyreader","positionreader","spectrumrelay","vectorrelay",
        "historyrelay","multiplehistoryinput","buffercontext","waveformrelay","waveform","multiplewaveforminput","math","maprange","mix","curve","clamp",
        "unitconvert","value","comparator","gain","hilbert","filter","delay","osc","additive",
        "digitalnoise","noise","pan","channelseparate","channelcombine","context","slew","bitcrusher",
        "samplehold","edgedetector","clock","rateconvert","triggerpulse","scalequantizer","pitchhold","instancecontext",
        "multiplegraph","multipleinput","multiplespectrumoutput","multiplespectruminput","vector","vectorcombine","vectorseparate","vectormath","vectorrotate",
        "vectorscale","vectorlength","vectordistance","vectordot","spatialreceiver","multiplevectorinput","multipleoutput","instancecombine",
        "spectrum","fftstream","spectralgraph","ifft"
    );
    static final String COUNT_ROLE="easyknack.instance.role";
    static boolean countPort(GLCompositePortMapping port){return GLNodeValue.of("count").equals(port.presentation().editorOptions().get(COUNT_ROLE));}
    /** A forwarded Count editor is an ordinary parameter of its enclosing group. */
    static GLCompositePortMapping exposedPort(GLCompositePortMapping port){
        if(!countPort(port))return port;
        var options=new HashMap<>(port.presentation().editorOptions());
        options.remove(COUNT_ROLE);options.remove("applicationManaged");
        return new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),
                new GLCompositePortPresentation(port.presentation().inputEditorId(),port.presentation().socketVisible(),options),port.additionalEndpoints());
    }
    static GLCompositePortMapping countPort(String id){
        var options=new HashMap<String,GLNodeValue>();options.put("applicationManaged",GLNodeValue.of(true));options.put(COUNT_ROLE,GLNodeValue.of("count"));
        options.put("default",GLNodeValue.of(8.));options.put("minimum",GLNodeValue.of(0.));options.put("maximum",GLNodeValue.of(256.));options.put("numericType",GLNodeValue.of("integer"));options.put("continuous",GLNodeValue.of(false));options.put("presentation",GLNodeValue.of("button"));options.put(GroupInputSettings.RATE,GLNodeValue.of("control"));
        return new GLCompositePortMapping(GLNodePortDescriptor.input(id,SignalCategory.VALUE.valueTypeId(),false,1),null,new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,options));
    }
    static boolean multiple(GLNodeGraphAsset asset){return new NodeState(asset.metadata()).flag(MARKER,false);}
    static boolean supports(BasicNode node){return node!=null&&SUPPORTED.contains(node.processor().id());}
    private static GLNodeId stable(String key){return new GLNodeId(UUID.nameUUIDFromBytes(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
    private static GLGraphConnection wire(GLNodeEndpoint out,GLNodeEndpoint in){return new GLGraphConnection(new GLNodeConnectionId(UUID.nameUUIDFromBytes((out+"/"+in).getBytes(java.nio.charset.StandardCharsets.UTF_8))),out,in);}
    private static GLGraphNode node(BasicNode d){return new GLGraphNode(GLNodeId.random(),d.registration().id(),d.defaults());}
    static GLNodeGraphAsset create(){
        String id="multiple-"+UUID.randomUUID();var context=node(new InstanceContextNode());var harmonic=node(new MathNode());var frequency=node(new MathNode());var oscillator=node(new OscillatorNode());var combine=node(new InstanceCombineNode());
        var graph=new GLNodeGraph();graph.edit("Instance Group",e->{
            for(var n:List.of(context,harmonic,frequency,oscillator,combine))e.addNode(n);
            e.setProperty(harmonic.id(),"operation",GLNodeValue.of("add"));e.setProperty(harmonic.id(),"b",GLNodeValue.of(1.));e.setProperty(harmonic.id(),"clamp",GLNodeValue.of(false));
            e.setProperty(frequency.id(),"operation",GLNodeValue.of("multiply"));e.setProperty(frequency.id(),"b",GLNodeValue.of(220.));e.setProperty(frequency.id(),"clamp",GLNodeValue.of(false));
            e.addConnection(wire(new GLNodeEndpoint(oscillator.id(),"audio-out"),new GLNodeEndpoint(combine.id(),"in")));
            e.addConnection(wire(new GLNodeEndpoint(context.id(),"index"),new GLNodeEndpoint(harmonic.id(),"a")));
            e.addConnection(wire(new GLNodeEndpoint(harmonic.id(),"audio-out"),new GLNodeEndpoint(frequency.id(),"a")));
            e.addConnection(wire(new GLNodeEndpoint(frequency.id(),"audio-out"),new GLNodeEndpoint(oscillator.id(),"frequency")));
        });
        var freqOptions=Map.of("default",GLNodeValue.of(220.),"minimumEnabled",GLNodeValue.of(false),"maximumEnabled",GLNodeValue.of(false),"presentation",GLNodeValue.of("button"));
        var ports=List.of(new GLCompositePortMapping(GLNodePortDescriptor.output("out",SignalCategory.AUDIO.valueTypeId()),new GLNodeEndpoint(combine.id(),"out")),countPort("Count"),
            new GLCompositePortMapping(GLNodePortDescriptor.input("frequency",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(frequency.id(),"b"),new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,freqOptions)));
        var d=new GLCompositeNodeDescriptor("easyknack.group."+id,id,1,ports);
        var metadata=Map.of(EXECUTION,GLNodeValue.of(2.),MARKER,GLNodeValue.of(true),MAXIMUM,GLNodeValue.of("16"),MERGE,GLNodeValue.of("mean"),"easyknack.title",GLNodeValue.of("Instance Group"),"easyknack.category",GLNodeValue.of(NodeCategory.ROUTING.path()),"easyknack.kind",GLNodeValue.of("AUDIO"));
        return new GLNodeGraphAsset(id,1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),new GLNodeEditorViewState(Map.of(context.id(),new GLNodeViewState(180,100),harmonic.id(),new GLNodeViewState(450,100),frequency.id(),new GLNodeViewState(720,100),oscillator.id(),new GLNodeViewState(990,100),combine.id(),new GLNodeViewState(1260,100))),metadata,d);
    }
    record Expansion(List<GLGraphNode> nodes,List<GLGraphConnection> wires){}
    static Expansion compile(GLGraphNode instance,GLNodeGraphAsset asset,GLCompositeGraphRegistry registry,GLCompositeExpansionLimits limits,Set<String> connected,Map<GLNodeEndpoint,GLNodeEndpoint> renamed){
        var ports=asset.compositeDescriptor().portMappings().stream().filter(p->!GroupInputs.editorOnly(p)).toList();
        var countPort=ports.stream().filter(MultipleGroups::countPort).findFirst().orElse(null);
        var inputs=ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.INPUT&&!countPort(p)).toList();
        var outputs=ports.stream().filter(p->p.publicPort().direction()==GLPortDirection.OUTPUT).toList();
        if(outputs.isEmpty()||ports.stream().anyMatch(p->!Set.of(SignalCategory.AUDIO.valueTypeId(),SignalCategory.VALUE.valueTypeId(),SignalCategory.WAVETABLE.valueTypeId(),SignalCategory.SPECTRUM.valueTypeId()).contains(p.publicPort().valueTypeId())&&!(p.publicPort().direction()==GLPortDirection.INPUT&&Set.of(SignalCategory.HISTORY.valueTypeId(),SignalCategory.SPECTRUM.valueTypeId(),SignalCategory.VECTOR.valueTypeId()).contains(p.publicPort().valueTypeId()))))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.text("instance.boundaryLimits"));
        for(var p:ports){String id=p.publicPort().id();renamed.put(new GLNodeEndpoint(instance.id(),id),new GLNodeEndpoint(instance.id(),countPort(p)?"count":p.publicPort().direction()==GLPortDirection.INPUT?MultipleGraphNode.input(inputs.indexOf(p)).value():MultipleGraphNode.output(outputs.indexOf(p)).value()));}
        int maximum=Integer.parseInt(new NodeState(asset.metadata()).text(MAXIMUM,"16"));if(maximum<1||maximum>256)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Maximum instances must be between 1 and 256"));
        int count=(int)new NodeState(instance.properties()).number(countPort==null?COUNT_PROPERTY:GroupUnits.key(countPort.publicPort().id()),new NodeState(instance.properties()).number(COUNT_PROPERTY,8));
        checkNested(asset,registry,new HashSet<>());
        var graph=new GLNodeGraph();var inputIds=new ArrayList<GLNodeId>();var outputIds=new ArrayList<GLNodeId>();
        graph.edit("Compile instances",e->{
            e.addNode(instance.withoutProperty(GLNodeState.MUTED).withoutProperty(GLNodeState.HIDDEN));
            for(int i=0;i<=inputs.size();i++){
                String port=i==0?"__instance_count":inputs.get(i-1).publicPort().id();var id=stable(instance.id()+"/"+asset.id()+(i==0?"/execution/count":"/input/"+port));inputIds.add(id);
                BasicNode boundary=i>0&&inputs.get(i-1).publicPort().valueTypeId().equals(SignalCategory.HISTORY.valueTypeId())?new MultipleHistoryInputNode():i>0&&inputs.get(i-1).publicPort().valueTypeId().equals(SignalCategory.WAVETABLE.valueTypeId())?new MultipleWaveformInputNode():i>0&&inputs.get(i-1).publicPort().valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId())?new MultipleSpectrumInputNode():i>0&&inputs.get(i-1).publicPort().valueTypeId().equals(SignalCategory.VECTOR.valueTypeId())?new MultipleVectorInputNode():new MultipleInputNode();
                e.addNode(new GLGraphNode(id,boundary.registration().id(),Map.of("slot",GLNodeValue.of(i))));
                if(i>0&&connected.contains(port))e.addConnection(wire(new GLNodeEndpoint(id,"out"),new GLNodeEndpoint(instance.id(),port)));
            }
            for(var port:outputs){var id=stable(instance.id()+"/"+asset.id()+"/output/"+port.publicPort().id());outputIds.add(id);e.addNode(new GLGraphNode(id,(port.publicPort().valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId())?new MultipleSpectrumOutputNode():new MultipleOutputNode()).registration().id(),Map.of()));e.addConnection(wire(new GLNodeEndpoint(instance.id(),port.publicPort().id()),new GLNodeEndpoint(id,"in")));}
        });
        var root=new GLNodeGraphAsset("multiple-program",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());
        // Expand this instance boundary as an ordinary group, but compile nested
        // spectral boundaries into their own frame processors before scheduling instances.
        var nestedRegistry=new GLCompositeGraphRegistry();
        for(var candidate:registry.assets()){
            if(candidate.id().equals(asset.id())){
                var metadata=new HashMap<>(candidate.metadata());metadata.remove(MARKER);
                candidate=new GLNodeGraphAsset(candidate.id(),candidate.version(),candidate.kind(),candidate.graph(),candidate.viewState(),metadata,candidate.compositeDescriptor());
            }
            nestedRegistry.registerAsset(candidate);
        }
        for(var descriptor:registry.composites())if(nestedRegistry.findComposite(descriptor.nodeTypeId()).isEmpty())nestedRegistry.registerComposite(descriptor);
        var flat=SpectralGroups.flatten(new GroupCompiler.Prepared(root,nestedRegistry),limits);if(!flat.isValid())throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Invalid Multiple Group: ")+flat.diagnostics());
        var source=flat.graph().orElseThrow();
        for(var n:source.nodes())if(!supports(NodeRegistry.find(n.typeId()).orElse(null)))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Unsupported node inside Multiple Group: ")+n.typeId());
        var contracts=NodeRegistry.executionContracts();var types=contracts.types();var definitions=contracts.definitions();
        source=com.vingame.glswing.node.execution.GLMutedGraphResolver.resolve(definitions.normalize(source),types);
        // An empty public boundary has no runtime consumer. Do not schedule orphaned
        // instance processors (including Combine sinks) behind disconnected outputs.
        if(source.connections().stream().noneMatch(w->outputIds.contains(w.input().nodeId()))){
            for(var port:ports)renamed.put(new GLNodeEndpoint(instance.id(),port.publicPort().id()),null);
            return new Expansion(List.of(),List.of());
        }
        var needed=new HashSet<GLNodeId>();for(var output:outputIds)ancestors(output,source,Set.of(),needed);
        var combines=source.nodes().stream().filter(n->needed.contains(n.id())&&NodeRegistry.require(n.typeId()) instanceof InstanceCombineNode).toList();
        var spectralOutputs=source.nodes().stream().filter(n->outputIds.contains(n.id())&&NodeRegistry.require(n.typeId()) instanceof MultipleSpectrumOutputNode).map(GLGraphNode::id).toList();
        if(combines.isEmpty()&&new NodeState(asset.metadata()).number(EXECUTION,1)>=2&&source.connections().stream().anyMatch(w->outputIds.contains(w.input().nodeId())&&!spectralOutputs.contains(w.input().nodeId())))throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Connect an Instance Combine to the group output before shared processing"));
        var extraNodes=new ArrayList<GLGraphNode>();var extraWires=new ArrayList<GLGraphConnection>();
        String marker="MULT1";
        if(!combines.isEmpty()){
            marker="MULT2";
            var boundaries=combines.stream().map(GLGraphNode::id).collect(java.util.stream.Collectors.toSet());boundaries.addAll(spectralOutputs);
            var perInstance=new HashSet<GLNodeId>();
            for(var boundary:boundaries)ancestors(boundary,source,Set.of(),perInstance);
            var shared=new HashSet<GLNodeId>();
            for(var output:outputIds)ancestors(output,source,boundaries,shared);
            shared.removeAll(boundaries);
            var overlap=new HashSet<>(shared);overlap.retainAll(perInstance);overlap.removeAll(inputIds);
            if(!overlap.isEmpty())throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Instance boundary crossed without Combine: connect per-instance signals through Instance Combine before shared processing"));
            for(var n:source.nodes())if(shared.contains(n.id())&&NodeRegistry.require(n.typeId()) instanceof InstanceContextNode)
                throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Instance Context cannot be used in the shared area after Combine"));
            // A Combine feeding another Combine would require entering a new instance domain.
            for(var w:source.connections())if(boundaries.contains(w.output().nodeId())&&perInstance.contains(w.input().nodeId()))
                throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Shared output cannot feed an instance input; use a separate Instance Group"));
            var substitutions=new HashMap<GLNodeEndpoint,GLNodeEndpoint>();
            for(int i=0;i<combines.size();i++)substitutions.put(new GLNodeEndpoint(combines.get(i).id(),"out"),new GLNodeEndpoint(instance.id(),MultipleGraphNode.output(i).value()));
            // Boundary proxies fan out external inputs to both execution domains.
            for(int i=1;i<inputIds.size();i++){
                var id=inputIds.get(i);if(!shared.contains(id))continue;
                if(Set.of(SignalCategory.HISTORY.valueTypeId(),SignalCategory.WAVETABLE.valueTypeId(),SignalCategory.SPECTRUM.valueTypeId(),SignalCategory.VECTOR.valueTypeId()).contains(inputs.get(i-1).publicPort().valueTypeId())){
                    BasicNode relay=inputs.get(i-1).publicPort().valueTypeId().equals(SignalCategory.HISTORY.valueTypeId())?new HistoryRelayNode():inputs.get(i-1).publicPort().valueTypeId().equals(SignalCategory.WAVETABLE.valueTypeId())?new WaveformRelayNode():inputs.get(i-1).publicPort().valueTypeId().equals(SignalCategory.VECTOR.valueTypeId())?new VectorRelayNode():new SpectrumRelayNode();extraNodes.add(new GLGraphNode(id,relay.registration().id(),relay.defaults()));
                    renamed.put(new GLNodeEndpoint(instance.id(),inputs.get(i-1).publicPort().id()),new GLNodeEndpoint(id,"in"));
                    extraWires.add(wire(new GLNodeEndpoint(id,"out"),new GLNodeEndpoint(instance.id(),MultipleGraphNode.input(i-1).value())));
                    continue;
                }
                var def=new MathNode();var values=new HashMap<>(def.defaults());values.put("operation",GLNodeValue.of("add"));values.put("clamp",GLNodeValue.of(false));values.put("a",GLNodeValue.of(0.));values.put("b",GLNodeValue.of(0.));
                extraNodes.add(new GLGraphNode(id,def.registration().id(),values));
                substitutions.put(new GLNodeEndpoint(id,"out"),new GLNodeEndpoint(id,"audio-out"));
                var publicId=inputs.get(i-1).publicPort().id();
                renamed.put(new GLNodeEndpoint(instance.id(),publicId),new GLNodeEndpoint(id,"a"));
                extraWires.add(wire(new GLNodeEndpoint(id,"audio-out"),new GLNodeEndpoint(instance.id(),MultipleGraphNode.input(i-1).value())));
            }
            for(var n:source.nodes())if(shared.contains(n.id())&&!inputIds.contains(n.id())&&!outputIds.contains(n.id()))extraNodes.add(n);
            for(var w:source.connections())if(shared.contains(w.input().nodeId())&&!outputIds.contains(w.input().nodeId()))
                extraWires.add(new GLGraphConnection(w.id(),substitutions.getOrDefault(w.output(),w.output()),w.input()));
            for(int i=0;i<outputIds.size();i++){
                final var id=outputIds.get(i);
                var upstream=source.connections().stream().filter(w->w.input().nodeId().equals(id)).map(GLGraphConnection::output).findFirst().orElse(null);
                renamed.put(new GLNodeEndpoint(instance.id(),outputs.get(i).publicPort().id()),spectralOutputs.contains(id)?new GLNodeEndpoint(instance.id(),MultipleGraphNode.output(combines.size()+spectralOutputs.indexOf(id)).value()):substitutions.getOrDefault(upstream,upstream));
            }
            perInstance.addAll(inputIds);
            source=new GLNodeGraphSnapshot(source.graphId(),source.revision(),source.nodes().stream().filter(n->perInstance.contains(n.id())).toList(),source.connections().stream().filter(w->perInstance.contains(w.input().nodeId())&&perInstance.contains(w.output().nodeId())).toList());
            outputIds.clear();outputIds.addAll(combines.stream().map(GLGraphNode::id).toList());outputIds.addAll(spectralOutputs);
        }
        long expandedNodes=source.nodes().stream().mapToLong(n->1+(NodeRegistry.require(n.typeId()) instanceof MultipleGraphNode?(long)new NodeState(n.properties()).number("instanceNodeCost",0):0)).sum()*maximum;
        if(expandedNodes>4096)throw new IllegalArgumentException(com.vin.easyknack.i18n.AppText.literal("Instance Group exceeds 4096 node instances; reduce Maximum instances"));
        var ids=source.nodes().stream().map(GLGraphNode::id).toList();
        // Preserve adaptive output policies: external boundaries only acquire their
        // actual rate in the host, including when this group is inside a bake.
        var policies=new ArrayList<String>();
        for(int i=0;i<source.nodes().size();i++){
            String policy=GroupSourceExporter.outputPolicy(source.nodes().get(i));
            if(policy.contains("?"))policies.add(i+" "+policy);
        }
        String program="MULT3 "+inputIds.size()+" "+outputIds.size()+" "+String.join(" ",inputIds.stream().map(id->Integer.toString(ids.indexOf(id))).toList())+" "+String.join(" ",outputIds.stream().map(id->Integer.toString(ids.indexOf(id))).toList())+" "+(marker.equals("MULT2")?1:0)+" "+policies.size()+" "+String.join(" ",policies)+" "+com.vin.easyknack.audio.AudioGraphEncoder.encode(source);
        var values=new HashMap<String,GLNodeValue>();values.put("program",GLNodeValue.of(SpectralGraphNode.hex(program)));values.put("instanceNodeCost",GLNodeValue.of(expandedNodes));values.put("maximum",GLNodeValue.of(maximum));values.put("count",GLNodeValue.of(Math.max(0,Math.min(256,count))));values.put("outputs",GLNodeValue.of(outputIds.size()));values.put("inputs",GLNodeValue.of(inputs.size()));values.put("spectrumInputs",GLNodeValue.of(java.util.stream.IntStream.range(0,inputs.size()).filter(i->inputs.get(i).publicPort().valueTypeId().equals(SignalCategory.SPECTRUM.valueTypeId())).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","))));values.put("historyInputs",GLNodeValue.of(java.util.stream.IntStream.range(0,inputs.size()).filter(i->inputs.get(i).publicPort().valueTypeId().equals(SignalCategory.HISTORY.valueTypeId())).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","))));values.put("waveformInputs",GLNodeValue.of(java.util.stream.IntStream.range(0,inputs.size()).filter(i->inputs.get(i).publicPort().valueTypeId().equals(SignalCategory.WAVETABLE.valueTypeId())).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","))));values.put("vectorInputs",GLNodeValue.of(java.util.stream.IntStream.range(0,inputs.size()).filter(i->inputs.get(i).publicPort().valueTypeId().equals(SignalCategory.VECTOR.valueTypeId())).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","))));values.put("mode",GLNodeValue.of(new NodeState(asset.metadata()).text(MERGE,"mean")));
        values.put("spectrumOutputs",GLNodeValue.of(java.util.stream.IntStream.range(0,outputIds.size()).filter(i->spectralOutputs.contains(outputIds.get(i))).mapToObj(Integer::toString).collect(java.util.stream.Collectors.joining(","))));
        values.put(CompiledManualTargets.PROPERTY,GLNodeValue.of(CompiledManualTargets.collect(source.nodes(),flat,n->n.id().toString())));
        for(String key:List.of(GLNodeState.MUTED,GLNodeState.HIDDEN))if(instance.properties().containsKey(key))values.put(key,instance.properties().get(key));
        extraNodes.add(0,new GLGraphNode(instance.id(),new MultipleGraphNode().registration().id(),values));
        return new Expansion(extraNodes,extraWires);
    }
    static Set<GLNodeId> sharedNodes(GLNodeGraphSnapshot graph){
        var boundary=graph.nodes().stream().filter(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof InstanceCombineNode).map(GLGraphNode::id).collect(java.util.stream.Collectors.toSet());
        var downstream=new HashSet<>(boundary);boolean changed;
        do{changed=false;for(var w:graph.connections())if(downstream.contains(w.output().nodeId()))changed|=downstream.add(w.input().nodeId());}while(changed);
        var shared=new HashSet<GLNodeId>();for(var id:downstream)ancestors(id,graph,boundary,shared);shared.removeAll(boundary);return shared;
    }
    static List<GLGraphDiagnostic> diagnostics(GLNodeGraphSnapshot graph){
        var perInstance=new HashSet<GLNodeId>();var boundaries=new HashSet<GLNodeId>();
        for(var n:graph.nodes())if(NodeRegistry.find(n.typeId()).orElse(null) instanceof InstanceCombineNode){boundaries.add(n.id());ancestors(n.id(),graph,Set.of(),perInstance);}
        var shared=sharedNodes(graph);var result=new ArrayList<GLGraphDiagnostic>();
        for(var w:graph.connections()){
            boolean context=graph.findNode(w.output().nodeId()).map(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof InstanceContextNode).orElse(false);
            if(shared.contains(w.input().nodeId())&&!boundaries.contains(w.output().nodeId())&&(perInstance.contains(w.output().nodeId())||context)
                ||boundaries.contains(w.output().nodeId())&&perInstance.contains(w.input().nodeId()))
                result.add(new GLGraphDiagnostic(GLGraphDiagnostic.Severity.ERROR,"instance.boundary","Connect per-instance signals through Instance Merge. Shared signals cannot feed back into the instance area.",Set.of(),Set.of(w.id())));
        }
        return result;
    }
    private static void ancestors(GLNodeId id,GLNodeGraphSnapshot graph,Set<GLNodeId> stop,Set<GLNodeId> result){
        if(!result.add(id)||stop.contains(id))return;
        for(var wire:graph.connections())if(wire.input().nodeId().equals(id))ancestors(wire.output().nodeId(),graph,stop,result);
    }
    private static void checkNested(GLNodeGraphAsset asset,GLCompositeGraphRegistry registry,Set<String> visited){
        if(!visited.add(asset.id()))throw new IllegalArgumentException("Recursive Instance Group reference: "+asset.id());
        if(visited.size()>16)throw new IllegalArgumentException("Instance Group nesting exceeds 16 levels");
        for(var node:asset.graph().nodes())registry.findComposite(node.typeId()).flatMap(d->registry.findAsset(d.assetId())).ifPresent(child->{
            checkNested(child,registry,visited);
        });
        visited.remove(asset.id());
    }

}
