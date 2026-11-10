package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

class FoundationReformTest {
    @Test void automaticOperandsFollowOperationsButExplicitAndLegacyValuesSurvive(){
        var registry=new NodeRegistry(new GLNodeTypeRegistry(),new GLNodeCatalog());var definition=new MathNode();
        var node=registry.normalize(new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()));
        String type=node.typeId();assertTrue(node.isPropertyAutomatic("b"));assertEquals(1,new NodeState(node.properties()).number("b",-1));
        node=registry.normalize(node.withExplicitProperty("operation",GLNodeValue.of("power")));
        assertEquals(type,node.typeId());assertEquals(2,new NodeState(node.properties()).number("b",-1));
        node=registry.normalize(node.withExplicitProperty("b",GLNodeValue.of(3)).withExplicitProperty("operation",GLNodeValue.of("add")));
        assertEquals(type,node.typeId());assertEquals(3,new NodeState(node.properties()).number("b",-1));assertFalse(node.isPropertyAutomatic("b"));
        node=registry.normalize(node.withExplicitProperty("operation",GLNodeValue.of("absolute")));
        node=registry.normalize(node.withExplicitProperty("operation",GLNodeValue.of("multiply")));assertEquals(3,new NodeState(node.properties()).number("b",-1));
        node=registry.normalize(node.withoutProperty(GLGraphNode.AUTOMATIC_DEFAULT_PREFIX+"b").withProperty(GLGraphNode.AUTOMATIC_PREFIX+"b",GLNodeValue.of(true)));
        assertEquals(1,new NodeState(node.properties()).number("b",-1));assertTrue(node.isPropertyAutomatic("b"));
        var legacy=new GLGraphNode(GLNodeId.random(),definition.registration().id(),Map.of("operation",GLNodeValue.of("power"),"b",GLNodeValue.of(7)));
        assertEquals(7,new NodeState(registry.normalize(legacy).properties()).number("b",-1));
    }
    @Test void resourceTypesAndMixedOutputRatesAreIndependent(){
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());
        assertFalse(types.compatibleValueTypes(PortDataType.HISTORY.id(),PortDataType.SIGNAL.id()));
        assertFalse(types.compatibleValueTypes(PortDataType.HISTORY.id(),PortDataType.ANALYSIS.id()));
        var noise=new DigitalNoiseNode();assertEquals("aea",AudioGraphEncoder.outputRateMetadata(noise.layout(new NodeState(noise.defaults()))));
        var plan=new BakedRatePlan(0,List.of(new BakedRatePlan.Step(noise,noise.defaults(),List.of(-1,-1))),List.of(0,256));
        assertEquals(List.of(ProcessingRate.AUDIO,ProcessingRate.EVENT),plan.resolveOutputs(Map.of()));
    }
    static Fixture resourceFixture(){
        var inner=new GLNodeGraph();var reader=add(inner,new HistoryReaderNode());var history=add(inner,new HistoryBufferNode());connect(inner,reader,"audio-out",history,"audio-in");
        var descriptor=new GLCompositeNodeDescriptor("test.resources","test.resources.asset",1,List.of(input("history-in",SignalCategory.HISTORY,reader,"history"),output("history-out",SignalCategory.HISTORY,history,"history")));
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();var context=add(root,new ContextInputNode());var source=add(root,new HistoryBufferNode());var group=new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(group));var target=add(root,new HistoryReaderNode());var sink=add(root,new OutputNode());
        connect(root,context,"channel",source,"audio-in");connect(root,source,"history",group.id(),"history-in");connect(root,group.id(),"history-out",target,"history");connect(root,target,"audio-out",sink,"audio-in");
        return new Fixture(new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()),group,"",false);
    }
    @Test void resourcesExportAsTypedPortsAndCompileAsJava()throws Exception{
        var f=resourceFixture();var export=GroupSourceExporter.create(f.document(),f.group());
        var layout=export.definition().layout(new NodeState(export.definition().defaults()));assertTrue(layout.ports().stream().allMatch(p->p.valueTypeId().equals(PortDataType.HISTORY.id())));
        var dir=Files.createTempDirectory(Path.of("target"),"resource-java-");var source=dir.resolve(export.className()+".java");Files.writeString(source,export.files().get("java/com/vin/easyknack/nodes/"+export.className()+".java"));
        var log=new java.io.ByteArrayOutputStream();assertEquals(0,javax.tools.ToolProvider.getSystemJavaCompiler().run(null,log,log,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",dir.toString(),source.toString()),log.toString());
        var bytes=new java.io.ByteArrayOutputStream();new KnackDocumentCodec().write(f.document(),bytes);var codec=new KnackDocumentCodec();var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());assertEquals(f.document().root().graph().connections(),restored.root().graph().connections());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedResourceGraphMatchesOriginalInBothChannels()throws Exception{
        var f=resourceFixture();var export=GroupSourceExporter.create(f.document(),f.group());var d=export.definition();
        var dir=Files.createTempDirectory(Path.of("target"),"resource-native-").toAbsolutePath();var definitions=new ArrayList<>(NodeRegistry.definitionsList());definitions.add(d);
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var history=new HistoryBufferNode();var reader=new HistoryReaderNode();
        String patch="GRAPH 5 context context @ c - history history !"+NativeContractGenerator.fingerprint(history)+" "+history.encode(new NodeState(history.defaults()),p->0)+" @ c a baked "+d.processor().id()+" !"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->1)+" @ c c reader historyreader !"+NativeContractGenerator.fingerprint(reader)+" "+reader.encode(new NodeState(reader.defaults()),p->p.equals(HistoryReaderNode.HISTORY)?2:-1)+" @ aaa caae out out 3 0 @ a a";
        String code="#include \"AudioGraph.hpp\"\n#include \""+export.className()+".hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){auto factory=[](const std::string& id)->std::unique_ptr<NodeProcessor>{if(id=="+quote(d.processor().id())+")return std::make_unique<"+export.className()+">();return createNodeProcessor(id);};auto baked=AudioGraph::compile("+quote(patch)+",factory);auto original=AudioGraph::parse("+quote(reference(f))+");std::array<float,256>a{},b{};for(int block=0;block<8;block++)for(unsigned channel=0;channel<2;channel++){baked.process(a.data(),256,channel,nullptr);original.process(b.data(),256,channel,nullptr);for(int i=0;i<256;i++)assert(a[i]==b[i]);assert(a[100]==(channel?1:-1));}}";
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void sharedAnalysisPublishesACompleteAsynchronousFrame()throws Exception{
        var graph=new GLNodeGraph();var osc=add(graph,new OscillatorNode());var history=add(graph,new HistoryBufferNode());var fft=add(graph,new FFTAnalysisNode());var view=add(graph,new AnalysisViewNode(),Map.of("view",GLNodeValue.of("correlation")));var viewer=add(graph,new AnalysisViewerNode());
        connect(graph,osc,"audio-out",history,"audio-in");connect(graph,history,"history",fft,"history");connect(graph,fft,"analysis",view,"analysis-in");connect(graph,view,"analysis-out",viewer,"analysis");
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));host.command("PLAY");
            com.vin.easyknack.audio.AudioAnalysis.Frame frame=null;long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(4);
            while(frame==null&&System.nanoTime()<deadline){Thread.sleep(40);frame=com.vin.easyknack.audio.AudioAnalysis.parse(host.command("ANALYSIS")).get(viewer.toString());}
            assertNotNull(frame);assertTrue(frame.symmetric());assertEquals(8192,frame.values().length);assertEquals(440,frame.frequency(),2);assertTrue(frame.confidence()>.9);
            assertEquals("OK",host.command("STOP"));
        }
    }
    private static void run(List<String> command)throws Exception{var p=new ProcessBuilder(command).redirectErrorStream(true).start();String out=new String(p.getInputStream().readAllBytes());assertTrue(p.waitFor(120,java.util.concurrent.TimeUnit.SECONDS));assertEquals(0,p.exitValue(),out);}
}

