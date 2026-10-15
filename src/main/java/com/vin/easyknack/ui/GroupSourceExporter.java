package com.vin.easyknack.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.nodes.Editor;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Source bake: flatten once, prune dead branches, emit a statically wired native processor. */
final class GroupSourceExporter {
    record Port(GLCompositePortMapping mapping,PropertyId id,Editor editor,SignalCategory signal,Map<String,Double> choiceValues){
        PortDataType dataType(){return switch(signal){case TRIGGER->PortDataType.TRIGGER;case MIDI->PortDataType.MIDI;case WAVETABLE->PortDataType.WAVEFORM;case HISTORY->PortDataType.HISTORY;case ANALYSIS->PortDataType.ANALYSIS;case SPECTRUM->PortDataType.SPECTRUM;case VECTOR->PortDataType.VECTOR;default->PortDataType.SIGNAL;};}
        int nativeKind(){return switch(signal){case MIDI->1;case HISTORY->2;case ANALYSIS->3;case SPECTRUM->4;case TRIGGER->5;case VECTOR->6;case WAVETABLE->7;default->0;};}
        boolean resource(){return nativeKind()>=2&&nativeKind()<=4||nativeKind()==7;}
        BasicNode sentinel(boolean input){return input?switch(signal){case TRIGGER->new TriggerInputNode();case MIDI->new MidiInputNode();case HISTORY->new HistoryBufferNode();case ANALYSIS->new FFTAnalysisNode();case SPECTRUM->new FFTStreamNode();case VECTOR->new VectorNode();case WAVETABLE->new WaveformNode();default->new OscillatorNode();}:switch(signal){case TRIGGER->new TriggerPulseNode();case MIDI->new MidiTransposeNode();case HISTORY->new HistoryReaderNode();case ANALYSIS->new AnalysisViewerNode();case SPECTRUM->new IFFTNode();case VECTOR->new VectorSeparateNode();case WAVETABLE->new OscillatorNode();default->new GainNode();};}
        String endpoint(boolean input){return switch(signal){case TRIGGER->input?"trigger-out":"trigger";case MIDI->input?"midi-out":"midi-in";case HISTORY->"history";case ANALYSIS->"analysis";case SPECTRUM->"spectrum";case VECTOR->"vector";case WAVETABLE->"waveform";default->input?"audio-out":"audio-in";};}
    }
    record ChoiceParameter(String property,String label,PropertyId id,Editor.ComboBox<GroupChoices.Option> editor){}
    record Export(String className,String displayName,BasicNode definition,Map<String,String> files,GLNodeGraphDocument source,Map<String,List<String>> manualTargets){
        String suggestedFileName(){
            String safe=displayName.replaceAll("[\\x00-\\x1f<>:\"/\\\\|?*]","_").strip().replaceAll("[. ]+$","");
            if(safe.isBlank())safe="Group";
            if(safe.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?"))safe="Node-"+safe;
            return safe.substring(0,Math.min(100,safe.length()))+".knackplugin";
        }
        void write(Path destination)throws IOException{
            var absolute=destination.toAbsolutePath();var temporary=Files.createTempFile(absolute.getParent(),".knack-export-",".tmp");
            try{
                try(var zip=new ZipOutputStream(Files.newOutputStream(temporary),StandardCharsets.UTF_8)){
                    for(var file:files.entrySet()){zip.putNextEntry(new ZipEntry(file.getKey()));zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));zip.closeEntry();}
                    zip.putNextEntry(new ZipEntry("source.knack"));new KnackDocumentCodec().write(source,zip);zip.closeEntry();
                }
                try{Files.move(temporary,absolute,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
                catch(AtomicMoveNotSupportedException e){Files.move(temporary,absolute,StandardCopyOption.REPLACE_EXISTING);}
            }finally{Files.deleteIfExists(temporary);}
        }
    }
    static Export create(GLNodeGraphDocument document,GLGraphNode instance){
        return create(document,instance,false);
    }
    static Export create(GLNodeGraphDocument document,GLGraphNode instance,boolean local){
        var registry=new GLCompositeGraphRegistry();NodeDocuments.assets(document).forEach(registry::registerAsset);
        var descriptor=registry.findComposite(instance.typeId()).orElseThrow(()->new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Select one group to export.")));
        var asset=registry.findAsset(descriptor.assetId()).orElseThrow();
        String suffix=UUID.nameUUIDFromBytes((asset.id()+instance.id()).getBytes(StandardCharsets.UTF_8)).toString().replace("-","").substring(0,12);
        String className=(local?"LocalBakedGroup":"BakedGroup")+suffix,processor=(local?"local_baked_":"baked_")+suffix;
        var inputs=new ArrayList<Port>();var outputs=new ArrayList<Port>();
        var groupChoices=GroupChoices.choices(asset,registry);
        for(var mapping:descriptor.portMappings()){
            if(GroupInputs.editorOnly(mapping))continue;
            var signal=Arrays.stream(SignalCategory.values()).filter(v->v.valueTypeId().equals(mapping.publicPort().valueTypeId())).findFirst().orElse(SignalCategory.VALUE);
            boolean input=mapping.publicPort().direction()==GLPortDirection.INPUT;
            Editor slider=null;
            if(input&&mapping.presentation().inputEditorId().equals(GLNodeInputEditorRegistry.SLIDER)){
                var options=new NodeState(mapping.presentation().editorOptions());var base=GroupUnits.base(registry,descriptor,mapping);var selected=GroupUnits.selected(instance,mapping,base);
                double value=new NodeState(instance.properties()).number(GroupUnits.key(mapping.publicPort().id()),options.number("default",0));
                Double min=options.flag("minimumEnabled",true)?base.convert(options.number("minimum",0),selected):null;
                Double max=options.flag("maximumEnabled",true)?base.convert(options.number("maximum",1),selected):null;
                slider=new Editor.Slider(base.convert(value,selected),min,max,options.number("step",.01),options.text("numericType","float").equals("integer"),options.text("presentation","bar").equals("bar"),new UnitFormats(selected,selected,GroupUnits.allowed(mapping,base)));
            }
            if(input&&GroupInputSettings.checkbox(mapping))slider=new Editor.Toggle(new NodeState(instance.properties()).number(GroupUnits.key(mapping.publicPort().id()),new NodeState(mapping.presentation().editorOptions()).number("default",0))!=0);
            var choiceValues=new LinkedHashMap<String,Double>();
            if(input&&GroupInputs.choice(mapping)){
                var choice=groupChoices.stream().filter(c->c.id().equals(GroupInputs.property(mapping))).findFirst().orElseThrow();
                slider=GroupChoices.copy(choice.editor(),GroupChoices.selection(asset,choice,instance.properties().getOrDefault(choice.id(),choice.editor().defaultValue())));
                var template=GroupInputs.template(mapping);
                for(var option:choice.editor().items()){
                    var value=template==null?GLNodeValue.of(option.id()):template.entries().stream().filter(e->e.id().equals(option.id())).findFirst().orElseThrow().value();
                    choiceValues.put(option.id(),mapping.isBound()?GroupInputs.signalValue(asset,mapping.internalEndpoint(),value,registry):(double)choice.editor().items().indexOf(option));
                }
            }
            if(input&&signal==SignalCategory.TRIGGER)slider=new Editor.Trigger();
            var ports=input?inputs:outputs;ports.add(new Port(mapping,new PropertyId((input?"input_":"output_")+ports.size()),slider,signal,Map.copyOf(choiceValues)));
        }
        if(outputs.isEmpty()||outputs.size()>8)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Export requires between one and eight output ports."));
        // Sentinel leaves expose boundary endpoints through the existing nested-group flattener.
        var rootNodes=new ArrayList<GLGraphNode>();rootNodes.add(instance.withoutProperty(GLNodeState.HIDDEN).withoutProperty(GLNodeState.MUTED));
        var wires=new ArrayList<GLGraphConnection>();var inputIds=new ArrayList<GLNodeId>();var outputIds=new ArrayList<GLNodeId>();
        for(var p:inputs){var id=GLNodeId.random();inputIds.add(id);BasicNode d=p.sentinel(true);rootNodes.add(new GLGraphNode(id,d.registration().id(),d.defaults()));
            wires.add(wire(new GLNodeEndpoint(id,p.endpoint(true)),new GLNodeEndpoint(instance.id(),p.mapping.publicPort().id())));}
        for(var p:outputs){var id=GLNodeId.random();outputIds.add(id);BasicNode d=p.sentinel(false);rootNodes.add(new GLGraphNode(id,d.registration().id(),d.defaults()));
            wires.add(wire(new GLNodeEndpoint(instance.id(),p.mapping.publicPort().id()),new GLNodeEndpoint(id,p.endpoint(false))));}
        var root=new GLNodeGraphAsset("export.root",1,GLNodeGraphAssetKind.PROJECT,new GLNodeGraphSnapshot(document.root().graph().graphId(),0,rootNodes,wires),GLNodeEditorViewState.empty());
        var prepared=GroupCompiler.prepare(root,registry);
        var result=SpectralGroups.flatten(prepared,new GLCompositeExpansionLimits(16,256,512,10000));
        if(!result.isValid())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Cannot expand group: ")+result.diagnostics());
        var graph=materializeUnits(AutomaticRates.resolve(result.graph().orElseThrow()));
        com.vin.easyknack.audio.SampleFeedback.validate(graph);
        var validationTypes=new GLNodeTypeRegistry();new NodeRegistry(validationTypes,new GLNodeCatalog());
        // Sentinel leaves deliberately have unbound inputs; only validate cycle timing here.
        if(GLNodeGraphValidator.validate(graph,validationTypes,GLGraphCyclePolicy.DELAYED_FEEDBACK).diagnostics().stream().anyMatch(d->d.code().equals("graph.cycle")))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Feedback requires a Delay in every cycle"));
        Map<GLNodeEndpoint,GLNodeEndpoint> sources=new HashMap<>();graph.connections().forEach(w->sources.put(w.input(),w.output()));
        var order=new ArrayList<GLGraphNode>();var visiting=new HashSet<GLNodeId>();var done=new HashSet<GLNodeId>();
        for(var id:outputIds)visit(id,graph,sources,inputIds,visiting,done,order);
        order.removeIf(n->outputIds.contains(n.id()));
        Set<String> deviceIO=Set.of("input","out","deviceout");
        for(var n:order){var d=NodeRegistry.require(n.typeId());
            if(deviceIO.contains(d.processor().id()))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Keep audio device I/O outside exported processing groups: ")+d.title());
            if(GLNodeState.isMuted(n))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Resolve muted nodes before baking: ")+d.title());
        }
        ProcessingRate boundaryRate=ProcessingRate.CONTROL;
        for(int i=0;i<outputs.size();i++){
            var source=sources.get(new GLNodeEndpoint(outputIds.get(i),outputs.get(i).endpoint(false)));
            if(source==null)continue; // Disconnected outputs use the baked boundary's empty value.
            var sourceNode=graph.findNode(source.nodeId()).orElseThrow();
            var sourceRate=NodeRegistry.require(sourceNode.typeId()).layout(new NodeState(sourceNode.properties())).rows().stream()
                    .filter(r->r.id().equals(source.portId())).findFirst().orElseThrow().rate();
            boundaryRate=GroupPortRates.maximum(boundaryRate,sourceRate);
        }
        var ratePlan=ratePlan(order,sources,inputIds,outputIds,outputs);
        var choices=new ArrayList<ChoiceParameter>();
        for(var choice:groupChoices)choices.add(new ChoiceParameter(choice.id(),choice.label(),inputs.stream().filter(p->GroupInputs.choice(p.mapping)&&GroupInputs.property(p.mapping).equals(choice.id())).map(Port::id).findFirst().orElse(new PropertyId("choice_"+choices.size())),GroupChoices.copy(choice.editor(),GroupChoices.selection(asset,choice,instance.properties().getOrDefault(choice.id(),choice.editor().defaultValue())))));
        var dynamic=choiceTokens(root,registry,instance,choices,graph,order,sources,inputIds);
        var definition=new ExportDefinition(className,processor,inputs,outputs,ratePlan,choices,editorOrder(asset,inputs,choices));
        String monitorSource=new NodeState(asset.metadata()).text(PitchReadoutEditor.GROUP_MONITOR,"");
        String monitorPath="";
        for(int i=0;i<order.size();i++){
            var origin=result.origins().get(order.get(i).id());
            if(origin!=null&&origin.sourceNodeId().toString().equals(monitorSource)&&order.get(i).typeId().equals("easyknack.analysis.pitchmonitor"))monitorPath="node"+i;
        }
        String java=javaSource(className,processor,asset,inputs,outputs,ratePlan,choices,monitorPath);
        String cpp=nativeSource(className,processor,definition,order,sources,inputIds,outputIds,inputs,outputs,boundaryRate,dynamic,choices.isEmpty());
        var files=new LinkedHashMap<String,String>();
        files.putAll(com.vin.easyknack.plugins.PluginLicensing.files());
        files.put(com.vin.easyknack.plugins.PluginManifest.FILE,new com.vin.easyknack.plugins.PluginManifest(
                processor,"1.0.0",List.of("com.vin.easyknack.nodes."+className)).text());
        files.put("java/com/vin/easyknack/nodes/"+className+".java",java);
        files.put("native/"+className+".hpp",cpp);
        files.put("native/"+className+".cpp","#include \""+className+".hpp\"\n");
        files.put("README.md","""
                # EasyKnack source plugin

                This is a normal BasicNode and a concrete C++ NodeProcessor, not a navigable group.
                Install this archive using Plugins > Install source plugin. EasyKnack compiles Java
                and a matching native host once and caches the results. A JDK and LLVM-MinGW x64
                compiler are required for building; GLSwing stays an external dependency.
                Edit the sources via Plugins > Open plugin sources, then Rebuild plugins.
                Updated already-loaded nodes activate after restart. Build errors retain the prior
                compatible version. plugin.properties declares the plugin ID, version and Java nodes.

                Public input order, combobox models, checkboxes, trigger buttons, hidden sockets,
                slider fallbacks and unit choices remain editable. Internal settings
                are baked from the selected group instance. source.knack retains the editable original.
                Nested groups and unit adapters are expanded; unused branches are removed. The processor
                uses a generated, fixed sequence of concrete C++ processor calls and the same bounded
                event/voice/channel buffers as the host. The fixed wiring is prepared in configure(),
                never in the audio callback. Pure Math/Mix/MapRange/Unit Converter regions, including
                those inside mixed or nested groups, fuse compatible audio computations into one sample loop.
                Constant expressions are prepared once and identical expressions reused. Pure Math
                regions use SSE2 where available, with a scalar tail and unchanged numerical guards.
                Values consumed outside a region retain their buffers. Other rates, voices and
                unsupported regions retain the scheduled implementation. Internal feedback loops,
                including nested groups, use the live host's read/compute/commit scheduler.
                Feedback regions are not fused; keep an entire feedback loop inside the group
                when exporting it. Compiler
                optimization uses -O3 without fast-math or floating-point contraction. Performance
                equivalent to handwritten DSP is not guaranteed.

                MIDI processing and device input, internal voice allocation/Merge, value events and
                cross-channel routing are supported. Audio device I/O stays outside the group.
                Each output retains its own rate; MIDI and value ports retain timed events.
                History and analysis ports share typed resources without copying their payloads.
                Internal rates and event timing are preserved; Auto follows the connected input rates. Unmerged public voice outputs must belong
                to one voice domain, and must not be mixed with MIDI outputs in the same node.
                External polyphony gets independent state; internal voices are rendered once and cached.
                Device choices and internal parameters are baked. Export a MIDI input port to choose or
                play a keyboard outside the node. An internal keyboard has no focus widget in the export.
                This is a source package for the current EasyKnack API, not a binary plugin ABI.
                """);
        var manualTargets=new HashMap<String,List<String>>();
        for(int i=0;i<order.size();i++){
            var node=order.get(i);var d=NodeRegistry.require(node.typeId());
            String prefix=cpp.contains("struct "+className+":MultipleGraphNode")?"":"node"+i+"/";
            CompiledManualTargets.read(node).forEach((source,targets)->{
                var mapped=manualTargets.computeIfAbsent(source,key->new ArrayList<>());
                targets.forEach(target->mapped.add(prefix+target));
            });
            if(d.layout(new NodeState(node.properties())).rows().stream().noneMatch(r->r.editor() instanceof Editor.Trigger||r.editor() instanceof PitchReadoutEditor||r.editor() instanceof AnalysisEditor))continue;
            var origin=result.origins().get(node.id());String id=(origin==null?node.id():origin.sourceNodeId()).toString();
            manualTargets.computeIfAbsent(id,key->new ArrayList<>()).add("node"+i);
            if(origin!=null){String path=id;for(int j=origin.instances().size()-1;j>0;j--){path=origin.instances().get(j).nodeId()+"/"+path;manualTargets.computeIfAbsent(path,key->new ArrayList<>()).add("node"+i);}}
        }
        return new Export(className,groupTitle(asset),definition,Collections.unmodifiableMap(files),document,Map.copyOf(manualTargets));
    }
    private static void visit(GLNodeId id,GLNodeGraphSnapshot graph,Map<GLNodeEndpoint,GLNodeEndpoint> sources,List<GLNodeId> external,Set<GLNodeId> visiting,Set<GLNodeId> done,List<GLGraphNode> order){
        if(external.contains(id)||done.contains(id))return;if(!visiting.add(id))return;
        for(var wire:graph.connections())if(wire.input().nodeId().equals(id))visit(wire.output().nodeId(),graph,sources,external,visiting,done,order);
        visiting.remove(id);done.add(id);order.add(graph.findNode(id).orElseThrow());
    }
    static GLNodeGraphSnapshot materializeUnits(GLNodeGraphSnapshot graph){
        var nodes=new ArrayList<>(graph.nodes());var wires=new ArrayList<>(graph.connections());
        for(var node:graph.nodes())for(var row:NodeRegistry.require(node.typeId()).layout(new NodeState(node.properties())).rows()){
            if(row.kind()!=NodeBuilder.RowKind.INPUT||!(row.editor() instanceof UnitSlider slider)||slider.selected()==slider.formats().stored())continue;
            var endpoint=new GLNodeEndpoint(node.id(),row.id());
            var connection=wires.stream().filter(w->w.input().equals(endpoint)).findFirst().orElse(null);if(connection==null)continue;
            var converter=new UnitConverterNode();var values=new HashMap<>(converter.defaults());
            values.put("from",GLNodeValue.of(slider.selected().id()));values.put("to",GLNodeValue.of(slider.formats().stored().id()));
            values.put("rate",GLNodeValue.of((row.adaptive()||new NodeState(node.properties()).text("rate","").equals("auto"))?"auto":row.rate().id()));
            var id=GLNodeId.random();nodes.add(new GLGraphNode(id,converter.registration().id(),values));wires.remove(connection);
            wires.add(wire(connection.output(),new GLNodeEndpoint(id,"value")));wires.add(wire(new GLNodeEndpoint(id,"out"),endpoint));
        }
        return new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires);
    }
    private static GLGraphConnection wire(GLNodeEndpoint out,GLNodeEndpoint in){return new GLGraphConnection(GLNodeConnectionId.random(),out,in);}
    private static ProcessingRate rate(BasicNode d,GLGraphNode n){return d.layout(new NodeState(n.properties())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).map(NodeBuilder.Row::rate).reduce(ProcessingRate.CONTROL,GroupPortRates::maximum);}
    static String outputPolicy(GLGraphNode node){
        if(NodeRegistry.require(node.typeId()) instanceof SpatialReceiverNode)return "a?";
        var state=new NodeState(node.properties());
        return String.join("",NodeRegistry.require(node.typeId()).layout(state).rows().stream()
            .filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT)
            .map(r->r.adaptive()||r.rates()==null&&state.text("rate","").equals("auto")
                    &&r.dataType()==PortDataType.SIGNAL&&r.rate()!=ProcessingRate.EVENT?"?":String.valueOf(r.rate().code())).toList());
    }
    private static String quote(String text){return "\""+text.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n")+"\"";}
    private static String choiceEditor(ChoiceParameter p){
        java.util.function.Function<GroupChoices.Option,String> item=o->"new Choice("+quote(o.id())+","+quote(o.label())+")";
        return "Editor.comboBox(java.util.List.of("+String.join(",",p.editor.items().stream().map(item).toList())+"),"+item.apply(p.editor.value())+",Choice::id)";
    }
    private static String dynamicPayload(String payload,Map<Integer,String> replacements){
        var tokens=payload.split(" ");replacements.forEach((index,id)->tokens[index]="__"+id+"__");return String.join(" ",tokens);
    }
    private static String dynamicPlan(String plan,Map<GLNodeId,Map<Integer,String>> dynamic){
        String expression=quote(plan);
        for(var id:dynamic.values().stream().flatMap(m->m.values().stream()).distinct().toList())expression=expression.replace("__"+id+"__","\" + ("+new String(Base64.getUrlDecoder().decode(id),StandardCharsets.UTF_8)+") + \"");
        return expression;
    }
    private static Map<GLNodeId,Map<Integer,String>> choiceTokens(GLNodeGraphAsset root,GLCompositeGraphRegistry registry,GLGraphNode instance,List<ChoiceParameter> choices,GLNodeGraphSnapshot graph,List<GLGraphNode> order,Map<GLNodeEndpoint,GLNodeEndpoint> sources,List<GLNodeId> external){
        var result=new HashMap<GLNodeId,Map<Integer,String>>();var indices=new HashMap<GLNodeId,Integer>();
        for(int i=0;i<external.size();i++)indices.put(external.get(i),i);for(int i=0;i<order.size();i++)indices.put(order.get(i).id(),external.size()+i);
        for(var choice:choices){
          var variants=new LinkedHashMap<String,Map<GLNodeId,String[]>>();
          for(var option:choice.editor.items()){
            var nodes=root.graph().nodes().stream().map(n->n.id().equals(instance.id())?n.withProperty(choice.property,GLNodeValue.of(option.id())):n).toList();
            var variantRoot=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),root.graph().revision(),nodes,root.graph().connections()),root.viewState());
            var prepared=GroupCompiler.prepare(variantRoot,registry);var flat=SpectralGroups.flatten(prepared,new GLCompositeExpansionLimits(16,256,512,10000));
            if(!flat.isValid())throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid group choice: ")+choice.property);
            var variant=materializeUnits(AutomaticRates.resolve(flat.graph().orElseThrow()));
            if(!variant.connections().equals(graph.connections()))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Choice changes group wiring: ")+choice.property);
            var encoded=new LinkedHashMap<GLNodeId,String[]>();variants.put(option.id(),encoded);
            for(var node:order){
                var def=NodeRegistry.require(node.typeId());var changed=variant.findNode(node.id()).orElseThrow();
                java.util.function.ToIntFunction<PropertyId> input=k->reference(sources.get(new GLNodeEndpoint(node.id(),k.value())),order,indices,external);
                var before=choicePayload(def,node,input);var after=choicePayload(def,changed,input);
                if(before.length!=after.length)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Choice changes parameter layout: ")+choice.property);
                encoded.put(node.id(),after);
            }
          }
          for(var node:order){var first=variants.values().iterator().next().get(node.id());
            for(int i=0;i<first.length;i++){int index=i;String initial=first[i];
                if(variants.values().stream().allMatch(v->v.get(node.id())[index].equals(initial)))continue;
                String expression="std::string("+quote(initial)+")";
                for(var entry:variants.entrySet())expression="p."+choice.id.value()+"_value == "+quote(entry.getKey())+" ? std::string("+quote(entry.getValue().get(node.id())[i])+") : ("+expression+")";
                String token=Base64.getUrlEncoder().withoutPadding().encodeToString(expression.getBytes(StandardCharsets.UTF_8));
                if(result.computeIfAbsent(node.id(),id->new HashMap<>()).putIfAbsent(i,token)!=null)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Overlapping group choices"));
            }
          }
        }
        // A spectral processor contains an encoded scalar program. Substitute
        // its individual parameters, so independent editors remain independent.
        for(var node:order)if(NodeRegistry.require(node.typeId()) instanceof SpectralGraphNode&&result.containsKey(node.id())){
            var def=NodeRegistry.require(node.typeId());
            var tokens=choicePayload(def,node,k->reference(sources.get(new GLNodeEndpoint(node.id(),k.value())),order,indices,external));
            var changes=result.get(node.id());if(changes.keySet().stream().anyMatch(i->i<SpectralGraphNode.PROGRAM_TOKEN))throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Choice changes spectral input wiring"));
            var programChanges=new HashMap<Integer,String>();changes.forEach((index,value)->programChanges.put(index-SpectralGraphNode.PROGRAM_TOKEN,value));
            var program=dynamicPayload(String.join(" ",Arrays.copyOfRange(tokens,SpectralGraphNode.PROGRAM_TOKEN,tokens.length)),programChanges);
            var expression="SpectralGraphNode::encodeProgram("+dynamicPlan(program,Map.of(node.id(),programChanges))+")";
            result.put(node.id(),Map.of(SpectralGraphNode.PROGRAM_TOKEN,Base64.getUrlEncoder().withoutPadding().encodeToString(expression.getBytes(StandardCharsets.UTF_8))));
        }
        return result;
    }
    private static String[] choicePayload(BasicNode definition,GLGraphNode node,java.util.function.ToIntFunction<PropertyId> input){
        var payload=definition.encode(new NodeState(node.properties()),input).split(" ");
        if(!(definition instanceof SpectralGraphNode))return payload;
        var program=new String(HexFormat.of().parseHex(payload[SpectralGraphNode.PROGRAM_TOKEN]),StandardCharsets.UTF_8).split(" ");
        var expanded=new String[program.length+SpectralGraphNode.PROGRAM_TOKEN];System.arraycopy(payload,0,expanded,0,SpectralGraphNode.PROGRAM_TOKEN);System.arraycopy(program,0,expanded,SpectralGraphNode.PROGRAM_TOKEN,program.length);return expanded;
    }
    private static String editor(Editor value){
        if(value instanceof Editor.Trigger)return "new Editor.Trigger()";
        if(value instanceof Editor.ComboBox<?> c)return choiceEditor(new ChoiceParameter("","",new PropertyId("unused"),GroupChoices.copy(c,c.defaultValue())));
        if(value instanceof Editor.Toggle t)return "new Editor.Toggle("+t.value()+")";
        var s=(Editor.Slider)value;
        var f=s.formats();return "new Editor.Slider("+s.value()+", "+s.min()+", "+s.max()+", "+s.step()+", "+s.integral()+", "+s.bar()+", new UnitFormats(UnitFormat."+f.stored().name()+", UnitFormat."+f.initial().name()+", java.util.List.of("+String.join(",",f.choices().stream().map(u->"UnitFormat."+u.name()).toList())+")))";
    }
    private static String javaSource(String name,String processor,GLNodeGraphAsset asset,List<Port> inputs,List<Port> outputs,BakedRatePlan ratePlan,List<ChoiceParameter> choices,String monitorPath){
        String kind=new NodeState(asset.metadata()).text("easyknack.kind","AUDIO");
        var category=Arrays.stream(NodeCategory.values()).filter(c->c.matches(new NodeState(asset.metadata()).text("easyknack.category",""))).findFirst().orElse(NodeCategory.EFFECT);
        var s=new StringBuilder("package com.vin.easyknack.nodes;\n@RegisterNode(id=\"easyknack.baked.").append(processor.replace("_", "")).append("\",title=").append(quote(groupTitle(asset))).append(",category=NodeCategory.").append(category.name()).append(",kind=NodeKind.").append(NodeKind.valueOf(kind).name()).append(")\npublic final class ").append(name).append(" extends BasicNode {\n");
        s.append("    private record Choice(String id,String label){public String toString(){return label;}}\n");
        s.append("    private static final BakedRatePlan PLAN=").append(ratePlanSource(ratePlan)).append(";\n");
        s.append("    public java.util.Map<String,com.vingame.glswing.node.model.GLNodeValue> defaults(){var values=new java.util.HashMap<>(super.defaults());values.put(\"rate\",com.vingame.glswing.node.model.GLNodeValue.of(\"auto\"));return values;}\n");
        s.append("    public ProcessingRate resolveAutomaticRate(NodeState state,java.util.Map<PropertyId,ProcessingRate> incoming){return PLAN.resolve(incoming); }\n");
        s.append("    public java.util.List<ProcessingRate> resolveAutomaticOutputRates(NodeState state,java.util.Map<PropertyId,ProcessingRate> incoming){return PLAN.resolveOutputs(incoming); }\n");
        for(var p:concat(inputs,outputs))s.append("    public static final PropertyId ").append(p.id.value()).append("=new PropertyId(").append(quote(p.id.value())).append(");\n");
        s.append("    public void define(NodeBuilder n,NodeState s){\n");
        if(!monitorPath.isEmpty())s.append("        n.addEditor(new PropertyId(\"pitch_readout\"),\"Pitch\",new PitchReadoutEditor(").append(quote(monitorPath)).append(")).keepVisibleOnHide();\n");
        for(var p:outputs)s.append("        n.addOutput(").append(p.id.value()).append(',').append(quote(p.mapping.publicPort().id())).append(").signal(SignalCategory.").append(p.signal.name()).append(").dataType(PortDataType.").append(p.dataType().name()).append(").rate(").append((p.signal==SignalCategory.MIDI||p.signal==SignalCategory.TRIGGER)?"ProcessingRate.EVENT":"ProcessingRate.fromId(s.text(ProcessingRate.EFFECTIVE+\".output_"+outputs.indexOf(p)+"\",PLAN.resolveOutputs(java.util.Map.of()).get("+outputs.indexOf(p)+").id()))").append(");\n");
        for(var id:editorOrder(asset,inputs,choices)){
            var port=inputs.stream().filter(p->p.id.value().equals(id)).findFirst();
            if(port.isPresent()){var p=port.get();s.append("        n.addInput(").append(p.id.value()).append(',').append(quote(p.mapping.publicPort().id()));if(p.editor!=null)s.append(',').append(editor(p.editor));s.append(").signal(SignalCategory.").append(p.signal.name()).append(')');s.append(".dataType(PortDataType.").append(p.dataType().name()).append(").rate(ProcessingRate.").append((p.signal==SignalCategory.MIDI||p.signal==SignalCategory.TRIGGER)?"EVENT":p.resource()?"CONTROL":"AUDIO").append(").socketVisible(").append(p.mapping.presentation().socketVisible()).append(")").append(inputRates(p)==null?"":".rates(PortRates."+inputRates(p).name()+")").append(";\n");}
            else {var choice=choices.stream().filter(c->c.id.value().equals(id)).findFirst().orElseThrow();s.append("        n.addEditor(new PropertyId(").append(quote(choice.id.value())).append("),").append(quote(choice.label)).append(',').append(choiceEditor(choice)).append(");\n");}
        }
        s.append("    }\n    public NativeProcessor processor(){return new NativeProcessor(").append(quote(processor)).append(',').append(quote("nodes/"+name+".hpp")).append(",ParameterContract.builder()");
        for(var p:inputs){s.append(".source(").append(p.id.value()).append(')');if(p.editor!=null&&!(p.editor instanceof Editor.Trigger))s.append(".value(").append(p.id.value()).append(')');}
        for(var choice:choices)if(inputs.stream().noneMatch(p->p.id.equals(choice.id)))s.append(".value(new PropertyId(").append(quote(choice.id.value())).append("))");
        s.append(".build());}\n");
        appendRouting(s,asset,inputs,outputs,GroupRouting.INSERT_IN,GroupRouting.INSERT_OUT,"insert","InsertionRouting");
        appendRouting(s,asset,inputs,outputs,GroupRouting.MUTE_IN,GroupRouting.MUTE_OUT,"mute","MuteRouting");
        return s.append("}\n").toString();
    }
    private static PortRates inputRates(Port p){
        if(p.dataType()!=PortDataType.SIGNAL&&p.dataType()!=PortDataType.VECTOR)return null;
        return switch(GroupInputSettings.selectedRate(p.mapping)){case CONTROL->PortRates.CONTROL;case AUDIO->PortRates.AUDIO;default->PortRates.AUDIO_CONTROL;};
    }
    private static List<String> editorOrder(GLNodeGraphAsset asset,List<Port> inputs,List<ChoiceParameter> choices){
        var order=new LinkedHashSet<String>();
        for(var mapping:asset.compositeDescriptor().portMappings()){
            inputs.stream().filter(p->p.mapping.publicPort().id().equals(mapping.publicPort().id())).forEach(p->order.add(p.id.value()));
            if(GroupInputs.choice(mapping))choices.stream().filter(c->c.property.equals(GroupInputs.property(mapping))).forEach(c->order.add(c.id.value()));
        }
        inputs.forEach(p->order.add(p.id.value()));choices.forEach(c->order.add(c.id.value()));return List.copyOf(order);
    }
    private static String groupTitle(GLNodeGraphAsset asset){String title=new NodeState(asset.metadata()).text("easyknack.title","Group");return title.isBlank()?"Group":title;}
    private static void appendRouting(StringBuilder s,GLNodeGraphAsset asset,List<Port> inputs,List<Port> outputs,String inputKey,String outputKey,String method,String routing){
        var state=new NodeState(asset.metadata());
        var input=inputs.stream().filter(p->p.mapping.publicPort().id().equals(state.text(inputKey,""))).findFirst();
        var output=outputs.stream().filter(p->p.mapping.publicPort().id().equals(state.text(outputKey,""))).findFirst();
        if(input.isPresent()&&output.isPresent()&&input.get().dataType()==output.get().dataType())
            s.append("    public void ").append(method).append('(').append(routing).append(" routing,NodeState state){routing.connect(").append(input.get().id.value()).append(',').append(output.get().id.value()).append(");}\n");
    }
    private static List<Port> concat(List<Port> a,List<Port> b){var result=new ArrayList<>(a);result.addAll(b);return result;}
    static final class ExportDefinition extends BasicNode {
        final String name,id;final List<Port> inputs,outputs;final BakedRatePlan ratePlan;final List<ChoiceParameter> choices;final List<String> editorOrder;
        ExportDefinition(String name,String id,List<Port> inputs,List<Port> outputs,BakedRatePlan ratePlan,List<ChoiceParameter> choices,List<String> editorOrder){this.editorOrder=List.copyOf(editorOrder);this.name=name;this.id=id;this.inputs=inputs;this.outputs=outputs;this.ratePlan=ratePlan;this.choices=List.copyOf(choices);}
        public String textPrefix(){return "easyknack.export.definition";}
        public String nativeParameterType(){return name+"Parameters";}
        public void define(NodeBuilder n,NodeState s){
            for(var p:outputs)decorate(n.addOutput(p.id,p.mapping.publicPort().id()),p,s);
            for(var id:editorOrder){
                var port=inputs.stream().filter(p->p.id.value().equals(id)).findFirst();
                if(port.isPresent()){var p=port.get();decorate((p.editor==null?n.addInput(p.id,p.mapping.publicPort().id()):n.addInput(p.id,p.mapping.publicPort().id(),p.editor)).socketVisible(p.mapping.presentation().socketVisible()),p,s);}
                else {var choice=choices.stream().filter(c->c.id.value().equals(id)).findFirst().orElseThrow();n.addEditor(choice.id,choice.label,choice.editor);}
            }
        }
        private void decorate(NodeBuilder.Row row,Port p,NodeState s){row.signal(p.signal).dataType(p.dataType()).rate((p.signal==SignalCategory.MIDI||p.signal==SignalCategory.TRIGGER)?ProcessingRate.EVENT:row.kind()==NodeBuilder.RowKind.OUTPUT?ProcessingRate.fromId(s.text(ProcessingRate.EFFECTIVE+".output_"+outputs.indexOf(p),ratePlan.resolveOutputs(Map.of()).get(outputs.indexOf(p)).id())):p.resource()?ProcessingRate.CONTROL:ProcessingRate.AUDIO);if(row.kind()==NodeBuilder.RowKind.INPUT&&inputRates(p)!=null)row.rates(inputRates(p));}
        @Override public Map<String,GLNodeValue> defaults(){var values=new HashMap<>(super.defaults());values.put("rate",GLNodeValue.of("auto"));return values;}
        @Override public ProcessingRate resolveAutomaticRate(NodeState state,Map<PropertyId,ProcessingRate> incoming){return ratePlan.resolve(incoming);}
        @Override public List<ProcessingRate> resolveAutomaticOutputRates(NodeState state,Map<PropertyId,ProcessingRate> incoming){return ratePlan.resolveOutputs(incoming);}
        public NativeProcessor processor(){var b=ParameterContract.builder();for(var p:inputs){b.source(p.id);if(p.editor!=null&&!(p.editor instanceof Editor.Trigger))b.value(p.id);}for(var choice:choices)if(inputs.stream().noneMatch(p->p.id.equals(choice.id)))b.value(choice.id);return new NativeProcessor(id,"nodes/"+name+".hpp",b.build());}
    }
    private static BakedRatePlan ratePlan(List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,List<GLNodeId> external,List<GLNodeId> targets,List<Port> outputs){
        var indices=new HashMap<GLNodeId,Integer>();for(int i=0;i<external.size();i++)indices.put(external.get(i),i);for(int i=0;i<nodes.size();i++)indices.put(nodes.get(i).id(),external.size()+i);
        var steps=new ArrayList<BakedRatePlan.Step>();
        for(var node:nodes){var d=NodeRegistry.require(node.typeId());var properties=new HashMap<>(node.properties());if(d instanceof FormulaNode)properties.put("library",GLNodeValue.of("-"));steps.add(new BakedRatePlan.Step(d,properties,d.processor().inputs().stream().map(key->reference(sources.get(new GLNodeEndpoint(node.id(),key.value())),nodes,indices,external)).toList()));}
        var refs=new ArrayList<Integer>();for(int i=0;i<outputs.size();i++)refs.add(reference(sources.get(new GLNodeEndpoint(targets.get(i),outputs.get(i).endpoint(false))),nodes,indices,external));
        return new BakedRatePlan(external.size(),steps,refs);
    }
    private static String ratePlanSource(BakedRatePlan plan){
        var steps=new ArrayList<String>();
        for(var step:plan.steps()){
            var entries=new ArrayList<String>();for(var entry:new TreeMap<>(step.properties()).entrySet())entries.add("java.util.Map.entry("+quote(entry.getKey())+",com.vingame.glswing.node.model.GLNodeValue.of("+javaValue(entry.getValue().toJavaValue())+"))");
            steps.add("new BakedRatePlan.Step(new "+step.definition().getClass().getCanonicalName()+"(),java.util.Map.ofEntries("+String.join(",",entries)+"),java.util.List.of("+String.join(",",step.sources().stream().map(Object::toString).toList())+"))");
        }
        return "new BakedRatePlan("+plan.inputCount()+",java.util.List.of("+String.join(",",steps)+"),java.util.List.of("+String.join(",",plan.outputs().stream().map(Object::toString).toList())+"))";
    }
    private static String javaValue(Object value){
        if(value instanceof String text)return quote(text);
        if(value instanceof Boolean)return value.toString();
        if(value instanceof Number number)return Double.toString(number.doubleValue());
        if(value instanceof List<?> list)return "java.util.List.of("+String.join(",",list.stream().map(GroupSourceExporter::javaValue).toList())+")";
        if(value instanceof Map<?,?> map)return "java.util.Map.ofEntries("+String.join(",",map.entrySet().stream().map(e->"java.util.Map.entry("+quote(e.getKey().toString())+","+javaValue(e.getValue())+")").toList())+")";
        throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unsupported exported property: ")+value);
    }
    private static String nativeSource(String name,String processor,BasicNode definition,List<GLGraphNode> nodes,Map<GLNodeEndpoint,GLNodeEndpoint> sources,List<GLNodeId> external,List<GLNodeId> targets,List<Port> inputs,List<Port> outputs,ProcessingRate boundaryRate,Map<GLNodeId,Map<Integer,String>> dynamic,boolean allowFusion){
        var spectral=SpectralSourceSpecialization.emit(name,nodes,dynamic.keySet());
        if(allowFusion&&dynamic.isEmpty()){
            String direct=InstanceSourceSpecialization.emit(name,processor,nodes,sources,external,targets,inputs,outputs);
            if(direct!=null)return direct;
        }
        var indices=new HashMap<GLNodeId,Integer>();
        for(int i=0;i<external.size();i++)indices.put(external.get(i),i);
        for(int i=0;i<nodes.size();i++)indices.put(nodes.get(i).id(),external.size()+i);
        var plan=new StringBuilder("GRAPH ").append(inputs.size()+nodes.size()+outputs.size());
        var types=new ArrayList<String>();
        for(int i=0;i<inputs.size();i++){
            var p=inputs.get(i);boolean midi=p.signal==SignalCategory.MIDI||p.signal==SignalCategory.TRIGGER;
            plan.append(" input").append(i).append(" baked_input ").append(i).append(" ").append(p.nativeKind()).append(" 0 @ ").append(midi?"e":p.resource()?"c":"a").append(" -");
            types.add("BakedInput");
        }
        for(int i=0;i<nodes.size();i++){
            var n=nodes.get(i);var d=NodeRegistry.require(n.typeId());var state=new NodeState(n.properties());var layout=d.layout(state);
            if(d instanceof FormulaNode){var values=new HashMap<>(state.values());values.put("library",GLNodeValue.of("-"));state=new NodeState(values);}
            plan.append(" node").append(i).append(' ').append(d instanceof FormulaNode?"formula_"+i:d.processor().id()).append(" !").append(NativeContractGenerator.fingerprint(d)).append(' ')
                .append(dynamicPayload(d.encode(state,key->{int ref=reference(sources.get(new GLNodeEndpoint(n.id(),key.value())),nodes,indices,external);return ref>=0&&ref<inputs.size()&&inputs.get(ref).signal==SignalCategory.WAVETABLE?-10000-ref:ref;}),dynamic.getOrDefault(n.id(),Map.of()))).append(" @ ").append(com.vin.easyknack.audio.AudioGraphEncoder.outputRateMetadata(layout)).append(' ');
            var bindings=d.processor().inputs();if(bindings.isEmpty())plan.append('-');
            else for(var key:bindings)plan.append(layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.INPUT&&r.key().equals(key)).map(r->r.hostInputRate()).findFirst().orElse('c'));
            types.add(d instanceof FormulaNode?name+"Formula"+i:d instanceof SpectralGraphNode?spectral.type():nativeType(d));
        }
        var outputIndices=new ArrayList<Integer>();
        for(int i=0;i<outputs.size();i++){
            boolean midi=outputs.get(i).signal==SignalCategory.MIDI||outputs.get(i).signal==SignalCategory.TRIGGER;
            outputIndices.add(inputs.size()+nodes.size()+i);
            plan.append(" output").append(i).append(" baked_output ").append(reference(sources.get(new GLNodeEndpoint(targets.get(i),outputs.get(i).endpoint(false))),nodes,indices,external))
                .append(" ").append(outputs.get(i).nativeKind()).append(" @ ").append(midi?"e e":outputs.get(i).resource()?"c c":boundaryRate.code()+" "+boundaryRate.code());types.add("BakedOutput");
        }
        if(types.size()>256)throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Export exceeds the native graph budget including boundary ports (256)."));
        var s=new StringBuilder("#pragma once\n#include \"ParameterContracts.hpp\"\n#include \"BakedGraph.hpp\"\n#include \"SimdMath.hpp\"\n#include <sstream>\n");
        for(var header:nodes.stream().map(n->NodeRegistry.require(n.typeId()).processor().header()).distinct().toList())s.append("#include ").append(quote(header)).append('\n');
        boolean specialized=dynamic.isEmpty()&&StatefulChainSpecialization.supports(nodes,sources,inputs.size(),outputs.size()) && reference(sources.get(new GLNodeEndpoint(targets.get(0),"audio-in")),nodes,indices,external)==2;
        if(specialized)s.append("#include \"OscGainFilterKernel.hpp\"\n");
        s.append("namespace easyknack {\n").append(spectral.code());
        for(int i=0;i<nodes.size();i++)if(NodeRegistry.require(nodes.get(i).typeId()) instanceof FormulaNode){
            var state=new NodeState(nodes.get(i).properties());String code=state.text("cpp","");if(code.isEmpty())throw new IllegalArgumentException("Formula source missing for export");
            s.append("struct ").append(name).append("Formula").append(i).append(" final : FormulaNode {\nvoid evaluate(const float* input,float* output,const FormulaContext& context)noexcept override{\n").append(code).append("}\n");
            if(state.number("triggerInputs",0)==0&&state.number("triggerOutputs",0)==0){
                s.append("void process(ProcessContext& c)noexcept override{auto context=contextFor(c);\n");int inputCount=(int)state.number("inputs",1),outputCount=(int)state.number("outputs",1);
                for(int p=0;p<inputCount;p++)s.append("auto value").append(p).append("=c.numericInput(").append(p).append(",fallback[").append(p).append("]);\n");
                s.append("for(std::size_t frame=0;frame<c.bufferSize;frame++){context.sampleIndex=c.sampleOffset+frame;context.samplePosition=position+c.sampleOffset+frame;float input[16]{},output[8]{};\n");
                for(int p=0;p<inputCount;p++)s.append("input[").append(p).append("]=value").append(p).append("[frame];\n");s.append(code);
                for(int p=0;p<outputCount;p++)s.append("if(auto* dst=c.outputAt(").append(p).append("))dst[frame]=output[").append(p).append("];\n");s.append("}finishContext(c);}\n");
            }
            s.append("};\n");
        }
        s.append("struct ").append(name).append(":BakedGraphProcessor {\n");
        if(!allowFusion)s.append("    bool supportsLiveParameters()const noexcept override{return false;}\n");
        s.append("    void configure(std::istream& in)override{auto p=parameters::").append(name).append("Parameters::read(in);sources=p.sources();\n");
        String graphExpression=dynamicPlan(plan.toString(),dynamic);
        for(int i=0;i<inputs.size();i++)if(inputs.get(i).signal==SignalCategory.WAVETABLE)
            graphExpression=graphExpression.replaceAll("(?<= )"+(-10000-i)+"(?= )",java.util.regex.Matcher.quoteReplacement("\" + std::to_string(p."+inputs.get(i).id.value()+"_source < 0 ? -1 : "+i+") + \""));
        s.append("        configureGraph(").append(graphExpression).append(",[](const std::string& id)->std::unique_ptr<NodeProcessor>{\n");
        s.append("            if(id==\"baked_input\")return std::make_unique<BakedInput>();\n            if(id==\"baked_output\")return std::make_unique<BakedOutput>();\n");
        var emitted=new HashSet<String>();
        for(int i=0;i<nodes.size();i++){var d=NodeRegistry.require(nodes.get(i).typeId());String id=d instanceof FormulaNode?"formula_"+i:d.processor().id();if(emitted.add(id))s.append("            if(id==").append(quote(id)).append(")return std::make_unique<").append(d instanceof FormulaNode?name+"Formula"+i:d instanceof SpectralGraphNode?spectral.type():nativeType(d)).append(">();\n");}
        s.append("            throw std::runtime_error(\"Unknown baked processor\");}, {")
            .append(String.join(",",java.util.stream.IntStream.range(0,inputs.size()).mapToObj(Integer::toString).toList())).append("}, {")
            .append(String.join(",",outputIndices.stream().map(Object::toString).toList())).append("}, {")
            .append(String.join(",",java.util.stream.IntStream.range(0,nodes.size()).filter(i->outputPolicy(nodes.get(i)).contains("?")).mapToObj(i->Integer.toString(inputs.size()+i)).toList())).append("});\n");
        for(int i=0;i<nodes.size();i++)if(outputPolicy(nodes.get(i)).contains("?"))
            s.append("        runtime->outputPolicies[").append(inputs.size()+i).append("]=").append(quote(outputPolicy(nodes.get(i)))).append(";\n");
        for(int i=0;i<inputs.size();i++){
            var p=inputs.get(i);if(p.editor==null||p.editor instanceof Editor.Trigger)continue;
            String fallback="static_cast<float>(p."+p.id.value()+"_value)";
            if(!p.choiceValues.isEmpty()){
                fallback="0.f";
                for(var entry:new TreeMap<>(p.choiceValues).entrySet())fallback="(p."+p.id.value()+"_value=="+quote(entry.getKey())+" ? "+entry.getValue()+"f : "+fallback+")";
            }
            s.append("        setFallback(").append(i).append(',').append(fallback).append(");\n");
        }
        boolean feedback=nodes.stream().anyMatch(n->sources.entrySet().stream().anyMatch(w->w.getKey().nodeId().equals(n.id())&&indices.get(w.getValue().nodeId())>=indices.get(n.id())));
        var sampleRegions=feedback?List.<MathFusion.Region>of():SampleFusion.plan(nodes,sources,indices,endpoint->reference(endpoint,nodes,indices,external));
        var mathRegions=feedback?List.<MathFusion.Region>of():MathFusion.plan(nodes,sources,indices,endpoint->reference(endpoint,nodes,indices,external));
        var regions=java.util.stream.Stream.concat(sampleRegions.stream(),mathRegions.stream().filter(r->sampleRegions.stream().noneMatch(samp->r.first()<=samp.last()&&r.last()>=samp.first())))
            .filter(region->dynamic.keySet().stream().noneMatch(id->indices.get(id)>=region.first()&&indices.get(id)<=region.last())).toList();
        s.append("    }\n");regions.forEach(region->s.append(region.code()));
        if(specialized)s.append(StatefulChainSpecialization.code());
        s.append("    void renderGraph(int frames,AudioIO* io,MidiSource* midi)noexcept override{auto& graph=runtime->graph;\n");
        s.append("        if(graph.hasSampleSteps||!graph.deferred.empty()){renderFeedbackGraph(frames,io,midi);return;}\n");
        if(specialized)s.append("        if(renderSpecializedChain(frames))return;\n");
        // DFS emitted nodes are topologically ordered; both channels precede every consumer.
        for(int i=0;i<types.size();i++){
            final int index=i;var region=regions.stream().filter(r->r.first()==index).findFirst().orElse(null);
            if(region!=null){
                s.append("        if(").append(region.method()).append("(frames,io,midi)){}else{\n");
                for(int j=region.first();j<=region.last();j++)appendScheduled(s,types,j);
                s.append("        }\n");i=region.last();
            }else appendScheduled(s,types,i);
        }
        return s.append("    }\n};\nEASYKNACK_NODE(").append(name).append(',').append(quote(processor)).append(")\n}\n").toString();
    }
    private static void appendScheduled(StringBuilder s,List<String> types,int i){
        s.append("        if(graph.nodes[").append(i).append("].active){graph.processNodeAs<").append(types.get(i)).append(",true>(").append(i).append(",frames,0,io,midi);if(graph.nodes[").append(i).append("].stereo)graph.processNodeAs<").append(types.get(i)).append(",true>(").append(i).append(",frames,1,io,midi);}\n");
    }
    private static String nativeType(BasicNode d){return Path.of(d.processor().header()).getFileName().toString().replace(".hpp","");}
    private static int reference(GLNodeEndpoint endpoint,List<GLGraphNode> nodes,Map<GLNodeId,Integer> indices,List<GLNodeId> external){
        if(endpoint==null)return -1;int index=indices.get(endpoint.nodeId());if(external.contains(endpoint.nodeId()))return index;
        var n=nodes.get(index-external.size());var outputs=NodeRegistry.require(n.typeId()).layout(new NodeState(n.properties())).ports().stream().filter(p->p.direction()==GLPortDirection.OUTPUT).toList();
        for(int i=0;i<outputs.size();i++)if(outputs.get(i).id().equals(endpoint.portId()))return index+256*i;
        throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unresolved output: ")+endpoint);
    }
}
