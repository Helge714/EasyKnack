package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.formula.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;

class FormulaGroupsTest {
    @org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(booleans={false,true}) @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void persistentOscillatorMatchesNativeExportAndFeedbackAndRetainsState(boolean table)throws Exception{
        var f=fixture();var typedMeta=new HashMap<>(f.asset().metadata());typedMeta.put(FormulaGroups.MARKER+".version",GLNodeValue.of(3));var asset=FormulaGroups.accept(FormulaGroups.metadata(f.asset(),typedMeta),"state double phase=0; state bool started=false; if(!started){phase=0;started=true;} result=sin(tau*phase); phase=phase+signal/sampleRate;phase=phase-floor(phase);".replace("sin(tau*phase)",table?"sineWave(phase)":"sin(tau*phase)"));
        var document=new GLNodeGraphDocument(f.document().root(),Map.of(asset.id(),asset),List.of());var export=GroupSourceExporter.create(document,f.group());
        var dir=Files.createTempDirectory(Path.of("target"),"formula-state-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var built=new FormulaNativeCompiler(Path.of(System.getProperty("easyknack.test.cxx")),dir.resolve("cache")).compile(FormulaGroups.program(asset));
        var values=new HashMap<>(FormulaGroups.materialize(asset).graph().nodes().get(0).properties());var node=new FormulaNode();
        String plain="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(values),p->-1);
        values.put("library",GLNodeValue.of(FormulaNode.hex(built.library().toString())));String compiled="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(values),p->-1);
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(export.definition());Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        var definition=export.definition();String baked="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(definition.defaults()),p->0);
        var reordered=FormulaCompiler.compile("state bool started=false;state double phase=0;result=phase;",FormulaGroups.ports(asset));
        var reset=FormulaCompiler.compile("state double phase=0.5;result=phase;",FormulaGroups.ports(asset));
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                FormulaNode vm,native,chunk,replacement;std::istringstream a("%s"),b("%s"),c0("%s"),d("%s");vm.configure(a);native.configure(b);chunk.configure(c0);replacement.configure(d);assert(native.module);
                %s baked;std::istringstream e("%s");baked.configure(e);baked.prepare(48000,256);
                vm.position=native.position=chunk.position=5000000000ULL;
                float frequency[256]{},expected[256]{},actual[256]{},silence[256]{};const float* inputs[]{frequency};ProcessContext c{inputs,1,expected,256,48000,silence,256};c.sourceRates="c";
                FormulaNode* optimized=&native;double error=0;unsigned measured=0;
                for(int block=0;block<200;block++){
                    std::fill_n(frequency,256,block<100?440.f:660.f);
                    if(block==100){replacement.inheritState(vm);optimized=&replacement;}
                    c.output=expected;vm.process(c);c.output=actual;optimized->process(c);for(int i=0;i<256;i++)assert(std::abs(actual[i]-expected[i])<1e-5);
                    baked.process(c);for(int i=0;i<256;i++)assert(std::abs(actual[i]-expected[i])<1e-5);
                    c.samplewise=true;c.bufferSize=1;for(unsigned i=0;i<256;i++){c.sampleOffset=i;c.output=actual+i;chunk.process(c);}for(int i=0;i<256;i++)assert(std::abs(actual[i]-expected[i])<1e-5);
                    c.samplewise=false;c.bufferSize=256;c.sampleOffset=0;
                    assert(formulaUnpack<double>(vm.program.typed.values[0])>=0&&formulaUnpack<double>(vm.program.typed.values[0])<1);
                    if(block<18)for(int i=0;i<256;i++){double reference=std::sin(6.283185307179586*440*(block*256+i)/48000.);error+=(expected[i]-reference)*(expected[i]-reference);++measured;}
                }
                assert(std::sqrt(error/measured)<.001); // Clean bounded phase even at a very large samplePosition.
                FormulaProgram reordered;reordered.read("%s",1,1);reordered.inheritState(vm.program);assert(reordered.typed.values[0]==1&&reordered.typed.values[1]==vm.program.typed.values[0]);
                FormulaProgram reset;reset.read("%s",1,1);reset.inheritState(vm.program);assert(formulaUnpack<double>(reset.typed.values[0])==.5);
                FormulaNode independent;std::istringstream fresh("%s");independent.configure(fresh);assert(independent.program.typed.values[0]==0&&independent.program.typed.values[1]==0);
            }
            """.formatted(export.className(),plain,compiled,plain,compiled,export.className(),baked,reordered.bytecode(),reset.bytecode(),plain));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void editingAnOldGroupPreservesItsReadVersion(){
        var a=FormulaGroups.create();assertEquals(3,FormulaGroups.version(a));
        var metadata=new HashMap<>(a.metadata());metadata.put(FormulaGroups.MARKER+".version",GLNodeValue.of(1));a=FormulaGroups.metadata(a,metadata);
        a=FormulaGroups.accept(a,"result=signal*2;");assertEquals(1,FormulaGroups.version(a));assertNotNull(FormulaGroups.program(a));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void contextBuffersRandomAndExportAgreeAcrossChunksAndHotSwap()throws Exception{
        var f=fixture();var a=f.asset();var typedMeta=new HashMap<>(a.metadata());typedMeta.put(FormulaGroups.MARKER+".version",GLNodeValue.of(3));a=FormulaGroups.metadata(a,typedMeta);var d=a.compositeDescriptor();var mappings=new ArrayList<>(d.portMappings());
        mappings.add(FormulaGroups.normalize(new GLCompositePortMapping(GLNodePortDescriptor.input("buffer",SignalCategory.HISTORY.valueTypeId(),false,1),null)));
        a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),mappings));
        a=FormulaGroups.accept(a,"result=buffer.read((long)sampleIndex-1,true)+buffer.read(sampleIndex+0.5,false)+rand(-1,1,42)+samplePosition*0.01+blockSize*0.001+sampleRate*0.00001+buffer.size+buffer.read(buffer.end-1,Clamp,Nearest)+buffer.begin+buffer.writePosition+buffer.sampleRate+signal.value+signal.rate;");
        var fixture=new Fixture(new GLNodeGraphDocument(f.document().root(),Map.of(a.id(),a),List.of()),f.group(),a);
        assertEquals("audio",flatten(fixture).nodes().stream().filter(n->n.typeId().equals("easyknack.formula.kernel")).findFirst().orElseThrow().properties().get(ProcessingRate.EFFECTIVE).toJavaValue());
        var export=GroupSourceExporter.create(fixture.document(),fixture.group());assertNotNull(GroupBakeTest.definition(export));
        var dir=Files.createTempDirectory(Path.of("target"),"formula-context-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var compiled=new FormulaNativeCompiler(Path.of(System.getProperty("easyknack.test.cxx")),dir.resolve("cache")).compile(FormulaGroups.program(a));
        var values=new HashMap<>(FormulaGroups.materialize(a).graph().nodes().get(0).properties());var node=new FormulaNode();values.put("in0",GLNodeValue.of(.25));
        String vm="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(values),p->-1);
        values.put("library",GLNodeValue.of(FormulaNode.hex(compiled.library().toString())));String optimized="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(values),p->-1);
        var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(export.definition());Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        var definition=export.definition();String baked="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(definition.defaults()),p->-1);
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                FormulaNode vm,native,chunk;std::istringstream p("%s"),q("%s"),r("%s");vm.configure(p);native.configure(q);chunk.configure(r);assert(native.module);
                %s baked;std::istringstream b("%s");baked.configure(b);baked.prepare(48000,8);
                HistoryResource h;h.samples={0,1,2,3,4,5,6,7};h.end=8;h.immutable=true;
                float audio[8]{},expected[8]{},silence[8]{};const float* inputs[2]{};const DspResource* resources[]{nullptr,&h};
                ProcessContext c{inputs,2,audio,8,48000,silence,8};c.resourceInputs=resources;
                for(unsigned block=0;block<3;block++){
                    c.output=expected;vm.process(c);c.output=audio;native.process(c);for(int i=0;i<8;i++)assert(std::abs(audio[i]-expected[i])<1e-4);
                    baked.process(c);for(int i=0;i<8;i++)assert(std::abs(audio[i]-expected[i])<1e-4);
                    c.samplewise=true;c.bufferSize=1;for(unsigned i=0;i<8;i++){c.sampleOffset=i;c.output=audio+i;chunk.process(c);}for(int i=0;i<8;i++)assert(std::abs(audio[i]-expected[i])<1e-4);
                    c.samplewise=false;c.bufferSize=8;c.sampleOffset=0;
                }
                auto context=vm.contextFor(c);assert(context.read(1,-1)==0);assert(context.read(1,99)==7);assert(context.read(1,-1,true)==7);assert(context.read(1,1.5)==1.5);assert(context.read(1,1.5,false,false)==1);assert(context.read(0,0)==0);assert(context.size(1)==8);
                // Read-before-future-write protection is shared with regular Buffer Readers.
                h.immutable=false;h.overwritten.resize(8);h.beginBlock(8);for(int i=0;i<8;i++)h.append(float(i+10));
                context.sampleIndex=0;assert(context.read(1,1,true)==1);assert(context.read(1,0,true)==10);context.sampleIndex=1;assert(context.read(1,1,true)==11);
                assert(context.info(1,0)==2);assert(context.info(1,1)==10);assert(context.info(1,2)==10);
                assert(context.readTimeline(1,9,0,1)==11);assert(context.readTimeline(1,-100,0,1)==2);assert(context.readTimeline(1,-100,2,1)==0);assert(context.readTimeline(1,10,1,0)==2);
                assert(context.readTimeline(1,8.5,0,1)==10.5);assert(context.readTimeline(1,8.5,0,0)==11);
                FormulaNode replacement;std::istringstream fresh("%s");replacement.configure(fresh);replacement.inheritState(vm);assert(replacement.position==24);
                c.resourceInputs=nullptr;c.output=expected;vm.process(c);c.output=audio;replacement.process(c);for(int i=0;i<8;i++)assert(std::abs(audio[i]-expected[i])<1e-4);
            }
            """.formatted(export.className(),vm,optimized,vm,export.className(),baked,optimized));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void templateSelectionKeepsPortIdentityAndConvertsStringEntriesToIndices(){
        var asset=FormulaGroups.create();var input=asset.compositeDescriptor().portMappings().get(1);String id=FormulaGroups.portId(input);
        var template=new EditorTemplates.Template("mode","Mode",List.of(new EditorTemplates.Entry("a","A",GLNodeValue.of("first")),new EditorTemplates.Entry("b","B",GLNodeValue.of("second"))),"a");
        asset=GroupInputs.applyTemplate(asset,input,template);assertEquals(id,FormulaGroups.portId(asset.compositeDescriptor().portMappings().get(1)));
        var runtime=FormulaGroups.materialize(asset);var port=runtime.compositeDescriptor().portMappings().get(1);
        assertEquals(1.,GroupInputs.signalValue(runtime,port.internalEndpoint(),GLNodeValue.of("second"),new GLCompositeGraphRegistry()));
    }
    record Fixture(GLNodeGraphDocument document,GLGraphNode group,GLNodeGraphAsset asset){}
    static Fixture fixture(){
        var original=FormulaGroups.create();var legacy=new HashMap<>(original.metadata());legacy.put(FormulaGroups.MARKER+".version",GLNodeValue.of(2));var asset=FormulaGroups.accept(FormulaGroups.metadata(original,legacy),"result = signal * 2;\n");
        var group=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of(GroupUnits.key("signal"),GLNodeValue.of(.25)));
        var output=new OutputNode();var out=new GLGraphNode(GLNodeId.random(),output.registration().id(),output.defaults());
        var graph=new GLNodeGraph();graph.edit("Formula",e->{e.addNode(group);e.addNode(out);e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(group.id(),"result"),new GLNodeEndpoint(out.id(),"audio-in")));});
        var root=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());
        return new Fixture(new GLNodeGraphDocument(root,Map.of(asset.id(),asset),List.of()),group,asset);
    }
    static GLNodeGraphSnapshot flatten(Fixture f){var registry=new GLCompositeGraphRegistry();NodeDocuments.assets(f.document()).forEach(registry::registerAsset);var p=GroupCompiler.prepare(f.document().root(),registry);var flat=SpectralGroups.flatten(p,new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flat.isValid(),flat.diagnostics().toString());return AutomaticRates.resolve(flat.graph().orElseThrow());}
    @Test void sourcePortsAndDraftPersistAndCompile(){
        var f=fixture();var flat=flatten(f);var node=flat.nodes().stream().filter(n->n.typeId().equals("easyknack.formula.kernel")).findFirst().orElseThrow();assertEquals("control",node.properties().get(ProcessingRate.EFFECTIVE).toJavaValue());
        assertTrue(com.vin.easyknack.audio.AudioGraphEncoder.encode(flat).contains(" formula "));
        var meta=new HashMap<>(f.asset().metadata());meta.put(FormulaGroups.DRAFT,GLNodeValue.of("result = ;"));var invalid=FormulaGroups.metadata(f.asset(),meta);
        assertEquals(FormulaGroups.program(f.asset()),FormulaGroups.program(invalid));assertThrows(FormulaCompiler.Failure.class,()->FormulaGroups.accept(invalid,FormulaGroups.draft(invalid)));
        try{var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(new GLNodeGraphDocument(f.document().root(),Map.of(invalid.id(),invalid),List.of()),bytes);var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()).embeddedAssets().get(invalid.id());assertEquals(invalid.metadata(),restored.metadata());assertEquals(FormulaGroups.ports(invalid),FormulaGroups.ports(restored));assertEquals(FormulaGroups.draft(invalid),FormulaGroups.draft(restored));}catch(Exception e){throw new AssertionError(e);}
    }
    @Test void exportedKernelContainsDirectCodeAndNoMachineLibrary()throws Exception{
        var f=fixture();var export=GroupSourceExporter.create(f.document(),f.group());var cpp=export.files().get("native/"+export.className()+".hpp");assertTrue(cpp.contains("final : FormulaNode"));assertTrue(cpp.contains("input[0] * 2.0f"));assertFalse(cpp.contains("formula-cache"));
        assertNotNull(GroupBakeTest.definition(export));
    }
    @Test void portRenameAndInterfaceChangesRetainTheAcceptedProgram(){
        var a=FormulaGroups.create();var in=a.compositeDescriptor().portMappings().get(1);String stable=FormulaGroups.portId(in);
        a=FormulaGroups.rename(a,in,"wave");assertEquals("result = wave;\n",FormulaGroups.source(a));assertEquals(stable,FormulaGroups.ports(a).get(1).id());
        var extra=FormulaGroups.normalize(new GLCompositePortMapping(GLNodePortDescriptor.output("extra",SignalCategory.VALUE.valueTypeId()),null));var d=a.compositeDescriptor();var ports=new ArrayList<>(d.portMappings());ports.add(extra);
        a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports));
        var old=FormulaGroups.program(a);assertEquals(2,old.ports().size());assertFalse(FormulaGroups.materialize(a).compositeDescriptor().portMappings().get(2).isBound());
        var accepted=FormulaGroups.accept(a,"result=wave; extra=wave*2;");assertEquals(3,FormulaGroups.program(accepted).ports().size());assertTrue(FormulaGroups.materialize(accepted).compositeDescriptor().portMappings().get(2).isBound());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void formulaHostPreservesTriggerOffsetsIncludingFeedbackChunks()throws Exception{
        var program=FormulaCompiler.compile("if(clock){ result=signal*2; fire=true; }else{ result=-signal; }",List.of(new FormulaCompiler.Port("in0","signal",FormulaCompiler.Type.NUMBER,false),new FormulaCompiler.Port("in1","clock",FormulaCompiler.Type.BOOLEAN,false),new FormulaCompiler.Port("out0","result",FormulaCompiler.Type.NUMBER,true),new FormulaCompiler.Port("out1","fire",FormulaCompiler.Type.BOOLEAN,true)));
        var values=new HashMap<String,GLNodeValue>();values.put("inputs",GLNodeValue.of(2));values.put("outputs",GLNodeValue.of(2));values.put("triggerInputs",GLNodeValue.of(2));values.put("triggerOutputs",GLNodeValue.of(2));values.put("bytecode",GLNodeValue.of(program.bytecode()));
        var def=new FormulaNode();String payload="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(values),p->-1);
        var dir=Files.createTempDirectory(Path.of("target"),"formula-trigger-").toAbsolutePath();
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "AudioGraph.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){FormulaNode node;std::istringstream data("%s");node.configure(data);
                float signal[]{1,2,3,4,5},out[5]{},fire[5]{},silence[5]{};const float* inputs[]{signal,nullptr};float* outputs[]{out,fire};
                ValueEventBuffer clock,resultEvents;clock.clear(5);clock.push(1,1,ValueEventKind::trigger);clock.push(3,1,ValueEventKind::trigger);resultEvents.clear(5);
                const ValueEventBuffer* events[]{nullptr,&clock};ValueEventBuffer* emitted[]{nullptr,&resultEvents};
                ProcessContext c{inputs,2,out,5,48000,silence,5};c.outputs=outputs;c.outputCount=2;c.valueEventInputs=events;c.valueEventOutputs=emitted;c.sourceRates="ae";
                node.process(c);assert(out[0]==-1&&out[1]==4&&out[2]==-3&&out[3]==8&&out[4]==-5);assert(resultEvents.count==2&&resultEvents.events[0].sampleOffset==1&&resultEvents.events[1].sampleOffset==3);
                resultEvents.clear(5);c.samplewise=true;
                for(unsigned i=0;i<5;i++){inputs[0]=signal+i;outputs[0]=out+i;outputs[1]=fire+i;c.output=out+i;c.bufferSize=1;c.sampleOffset=i;node.processSample(c);}
                assert(resultEvents.count==2&&resultEvents.events[0].sampleOffset==1&&resultEvents.events[1].sampleOffset==3);
            }
            """.formatted(payload));RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void workspaceOpensCodeAndRetainsInspectorAndUndo()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{var f=fixture();try(var controller=new WorkspaceController()){
            controller.openDocument("Formula",f.document());assertTrue(controller.workspace().openComposite(f.asset().id()));assertFalse(controller.workspace().editor().isVisible());
            var panel=GroupEditingTest.descendants(controller.workspace()).filter(c->c instanceof FormulaEditorPanel).map(c->(FormulaEditorPanel)c).findFirst().orElseThrow();
            panel.code.setText("result = signal * 3;");panel.apply();assertEquals("result = signal * 3;",FormulaGroups.source(controller.snapshotDocument().embeddedAssets().get(f.asset().id())));
            controller.history().undo();assertEquals("result = signal * 2;\n",FormulaGroups.source(controller.snapshotDocument().embeddedAssets().get(f.asset().id())));
            assertTrue(controller.workspace().navigateBack());assertTrue(controller.workspace().editor().isVisible());
        }});
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostPreviewAndExportProduceTheSameSamples()throws Exception{
        var f=fixture();String graph=com.vin.easyknack.audio.AudioGraphEncoder.encode(flatten(f));var export=GroupSourceExporter.create(f.document(),f.group());
        var dir=Files.createTempDirectory(Path.of("target"),"formula-host-").toAbsolutePath();Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var program=FormulaGroups.program(f.asset());var compiled=new FormulaNativeCompiler(Path.of(System.getProperty("easyknack.test.cxx")),dir.resolve("cache")).compile(program);
        String nativeGraph;
        FormulaGroups.libraries.put(FormulaGroups.key(program),FormulaNode.hex(compiled.library().toString()));
        try{nativeGraph=com.vin.easyknack.audio.AudioGraphEncoder.encode(flatten(f));}finally{FormulaGroups.libraries.remove(FormulaGroups.key(program));}
        var defs=new ArrayList<>(NodeRegistry.builtinDefinitions());defs.add(export.definition());Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(defs));
        var d=export.definition();String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);
        var cpp="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){
                auto graph=AudioGraph::parse("%s");float out[256]{};graph.process(out,256);for(float v:out)assert(std::abs(v-.5f)<1e-6);
                auto optimized=AudioGraph::parse("%s");bool loaded=false;for(auto& n:optimized.nodes)if(auto* f=dynamic_cast<FormulaNode*>(n.processor.get())){assert(f->module);loaded=true;}assert(loaded);
                optimized.process(out,256);for(float v:out)assert(std::abs(v-.5f)<1e-6);
                AudioEngine engine;engine.playing=true;engine.publish(std::move(graph));float stereo[512]{};for(int i=0;i<16;i++)engine.render(stereo,256);
                engine.publish(std::move(optimized));engine.render(stereo,256);for(float v:stereo)assert(std::abs(v-.1f)<1e-5);
                %s baked;std::istringstream p("%s");baked.configure(p);baked.prepare(48000,256);
                float silence[256]{};const float* inputs[]{nullptr};ProcessContext c{inputs,1,out,256,48000,silence,256};baked.process(c);for(float v:out)assert(std::abs(v-.5f)<1e-6);
            }
            """.formatted(export.className(),graph,nativeGraph,export.className(),payload);
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
