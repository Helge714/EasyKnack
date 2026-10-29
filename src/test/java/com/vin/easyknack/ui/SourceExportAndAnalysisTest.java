package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class SourceExportAndAnalysisTest {
    @Test void nestedSourceExportProducesCompilableJavaAndKeepsTheEditableSource()throws Exception{
        for(boolean nested:new boolean[]{false,true}){
            var doc=GroupUnitsTest.document(nested);
            var asset=doc.embeddedAssets().values().stream().filter(a->a.compositeDescriptor().nodeTypeId().equals(docType(nested))).findFirst().orElseThrow();
            var metadata=new HashMap<>(asset.metadata());metadata.put("easyknack.title",GLNodeValue.of("Warm \"Filter\" / Test"));metadata.put("easyknack.category",GLNodeValue.of(NodeCategory.MATH.path()));metadata.put("easyknack.kind",GLNodeValue.of("VALUE"));
            doc=doc.withEmbeddedAsset(new com.vingame.glswing.node.document.GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,asset.compositeDescriptor()));
            var export=GroupSourceExporter.create(doc,doc.root().graph().findNode(GroupUnitsTest.GROUP).orElseThrow());
            assertEquals("Warm \"Filter\" / Test",export.displayName());assertEquals("Warm _Filter_ _ Test.knackplugin",export.suggestedFileName());
            var directory=Files.createTempDirectory(Path.of("target"),"export-java-");
            String key="java/com/vin/easyknack/nodes/"+export.className()+".java";
            var source=directory.resolve(export.className()+".java");Files.writeString(source,export.files().get(key));
            var log=new java.io.ByteArrayOutputStream();
            assertEquals(0,ToolProvider.getSystemJavaCompiler().run(null,log,log,"-proc:none","-classpath",System.getProperty("java.class.path"),"-d",directory.toString(),source.toString()),log.toString());
            try(var loader=new java.net.URLClassLoader(new java.net.URL[]{directory.toUri().toURL()},getClass().getClassLoader())){
                var node=(BasicNode)loader.loadClass("com.vin.easyknack.nodes."+export.className()).getConstructor().newInstance();
                assertEquals(export.displayName(),node.title());assertEquals(NodeCategory.MATH,node.registration().category());assertEquals(NodeKind.VALUE,node.registration().kind());
            }
            var archive=directory.resolve("node.zip");export.write(archive);
            try(var zip=new java.util.zip.ZipFile(archive.toFile())){assertNotNull(zip.getEntry("source.knack"));assertNotNull(zip.getEntry("native/"+export.className()+".hpp"));}
            assertEquals(List.of(new PropertyId("input_0")),export.definition().processor().inputs());
        }
    }
    private static String docType(boolean nested){return nested?"test.outer":"test.group";}
    @Test void contractsDeriveComplexInputOrderAndRejectInvalidValues(){
        var d=new AdditiveOscillatorNode();assertEquals(List.of(d.WAVEFORM,d.FREQUENCY,d.PHASE,d.HARMONICS,d.FALLOFF,d.SPREAD,d.RESET),d.processor().inputs());
        var properties=new HashMap<>(d.defaults());properties.put(d.HARMONICS.value(),GLNodeValue.of(1.5));
        assertThrows(IllegalArgumentException.class,()->d.encode(new NodeState(properties),p->-1));
        String generated=NativeContractGenerator.generate(List.of(d));assertTrue(generated.contains("waveform_source,frequency_source,phase_source,harmonics_source,falloff_source,spread_source,reset_source"));
        assertTrue(generated.contains(NativeContractGenerator.fingerprint(d)));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void hostAcceptsTemporalContractsAndPublishesLiveAnalysis()throws Exception{
        try(var host=new com.vin.easyknack.audio.AudioHostClient()){
            host.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);
            for(var definition:NodeRegistry.definitionsList()){
                var graph=new GLNodeGraph();graph.edit("Contract",e->e.addNode(new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults())));
                if(definition.requiresSpectralContext()){
                    var failure=assertThrows(IllegalArgumentException.class,()->com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()),definition.registration().id());
                    assertTrue(failure.getMessage().contains(definition.title()));
                }else assertEquals("OK",assertDoesNotThrow(()->host.command(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot())),definition.registration().id()),definition.title());
            }
            var graph=new GLNodeGraph();var osc=new OscillatorNode();var correlation=new AutocorrelationNode();var scope=new OscilloscopeNode();
            var a=GLNodeId.random();var b=GLNodeId.random();var c=GLNodeId.random();
            graph.edit("Analysis consumers",e->{
                e.addNode(new GLGraphNode(a,osc.registration().id(),osc.defaults()));e.addNode(new GLGraphNode(b,correlation.registration().id(),correlation.defaults()));e.addNode(new GLGraphNode(c,scope.registration().id(),scope.defaults()));
                e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,"audio-out"),new GLNodeEndpoint(b,"audio-in")));
                e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,"audio-out"),new GLNodeEndpoint(c,"audio-in")));
            });
            host.command(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()));host.command("PLAY");
            var frames=new HashMap<String,com.vin.easyknack.audio.AudioAnalysis.Frame>();long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(3);
            while(frames.size()<2&&System.nanoTime()<deadline){Thread.sleep(50);frames.putAll(com.vin.easyknack.audio.AudioAnalysis.parse(host.command("ANALYSIS")));}
            assertEquals(2,frames.size());assertEquals(440,frames.get(b.toString()).frequency(),1);assertTrue(frames.get(b.toString()).confidence()>.98);
            // Repeated live edits with both observers, including an inactive connected branch.
            var value=GLNodeId.random();var unused=GLNodeId.random();
            graph.edit("Unused branch",e->{
                e.addNode(new GLGraphNode(value,new ValueNode().registration().id(),new ValueNode().defaults()));
                e.addNode(new GLGraphNode(unused,new MathNode().registration().id(),new MathNode().defaults()));
                e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(value,"value-out"),new GLNodeEndpoint(unused,MathNode.A.value())));
            });
            for(int i=0;i<100;i++){
                final double frequency=220+i*3;
                graph.edit("Drag frequency",e->e.setProperty(a,OscillatorNode.FREQUENCY.value(),GLNodeValue.of(frequency)));
                assertEquals("OK",host.command(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot())));
                Thread.sleep(8);
                assertTrue(host.command("ANALYSIS").startsWith("OK"));
            }
            assertEquals("OK",host.command("GRAPH 0"));Thread.sleep(30);
            assertEquals("OK",host.command("STOP"));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void generatedNativeGroupMatchesOscillatorAndAnalysisDetectsPitch()throws Exception{
        var doc=GroupUnitsTest.document(true);var export=GroupSourceExporter.create(doc,doc.root().graph().findNode(GroupUnitsTest.GROUP).orElseThrow());
        var directory=Files.createTempDirectory(Path.of("target"),"export-native-").toAbsolutePath();
        var definitions=new ArrayList<BasicNode>(NodeRegistry.definitionsList());definitions.add(export.definition());
        Files.writeString(directory.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        Files.writeString(directory.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var analyzer=new AutocorrelationNode();var scope=new OscilloscopeNode();
        String code="""
                #include "%s.hpp"
                #include "nodes/AutocorrelationNode.hpp"
                #include "nodes/OscilloscopeNode.hpp"
                #include <cassert>
                #include <iostream>
                using namespace easyknack;
                int main(){
                    %s baked; std::istringstream configuration("%s");baked.configure(configuration);
                    OscillatorNode expected;std::istringstream oscillator("-1 440 wave sine -1");expected.configure(oscillator);
                    std::array<float,256> actual{},reference{},silence{};
                    ProcessContext a{nullptr,0,actual.data(),256,48000,silence.data(),256};
                    auto b=a;b.output=reference.data();
                    for(int block=0;block<40;block++){baked.process(a);expected.process(b);for(int i=0;i<256;i++)assert(std::abs(actual[i]-reference[i])<.0001f);}
                    AutocorrelationNode correlation;std::istringstream cp("%s");correlation.configure(cp);correlation.bindIdentity("correlation");
                    OscilloscopeNode scope;std::istringstream sp("%s");scope.configure(sp);scope.bindIdentity("scope");
                    std::array<float,256> hz{},confidence{},through{};const float* input[]={reference.data()};float* outputs[]={hz.data(),confidence.data()};
                    ProcessContext c{input,1,hz.data(),1,48000,silence.data(),256,nullptr,0,outputs,2};
                    auto sc=c;sc.output=through.data();sc.bufferSize=256;sc.outputs=nullptr;sc.outputCount=1;
                    for(int block=0;block<32;block++){expected.process(b);correlation.process(c);scope.process(sc);}
                    assert(std::abs(hz[0]-440)<1);assert(confidence[0]>.98f);
                    AnalysisFrame frame;assert(analysisMailbox("scope")->latest(frame));assert(frame.span>0);
                    assert(analysisMailbox("correlation")->latest(frame));assert(std::abs(frame.frequency-440)<1);
                    reference.fill(0);for(int block=0;block<32;block++)correlation.process(c);
                    assert(hz[0]==0&&confidence[0]==0);
                    // Right-channel selection must analyze the sum of all voice buffers.
                    AutocorrelationNode rightCorrelation;rightCorrelation.right=true;rightCorrelation.bindIdentity("right-correlation");
                    OscilloscopeNode rightScope;rightScope.right=true;rightScope.bindIdentity("right-scope");
                    assert(rightScope.mergesVoices()&&rightScope.separatesChannels());
                    assert(rightCorrelation.mergesVoices()&&rightCorrelation.separatesChannels());
                    std::array<float,256> left1{},left2{},right1{},right2{};
                    const float* leftVoices[]={left1.data(),left2.data()};const float* rightVoices[]={right1.data(),right2.data()};
                    c.inputs=leftVoices;c.inputCount=2;c.rightInputs=rightVoices;
                    sc.inputs=leftVoices;sc.inputCount=2;sc.rightInputs=rightVoices;
                    for(int block=0;block<64;block++){
                        for(int i=0;i<256;i++){
                            double t=(block*256+i)/48000.;
                            left1[i]=left2[i]=.05f*std::sin(2*3.141592653589793*220*t);
                            right1[i]=.2f*std::sin(2*3.141592653589793*660*t);
                            right2[i]=.3f*std::sin(2*3.141592653589793*660*t);
                        }
                        rightCorrelation.process(c);rightScope.process(sc);
                    }
                    assert(std::abs(hz[0]-660)<2);assert(confidence[0]>.95f);
                    assert(analysisMailbox("right-scope")->latest(frame));assert(frame.valueCount==256);
                    float peak=0;for(unsigned i=0;i<frame.valueCount;i++)peak=std::max(peak,std::abs(frame.values[i]));
                    assert(peak>.45f&&peak<.51f);
                    std::istringstream invalid("!0000000000000000 -1 40 2000");bool rejected=false;
                    try{correlation.configure(invalid);}catch(const std::runtime_error&){rejected=true;}assert(rejected);
                    std::cout<<"export and analysis passed\\n";
                }
                """.formatted(export.className(),export.className(),payload(export.definition()),payload(analyzer),payload(scope));
        var source=directory.resolve("test.cpp");Files.writeString(source,code);
        var executable=directory.resolve("test.exe");
        run(List.of(System.getProperty("easyknack.test.cxx"),"-std=c++17","-O2","-static","-I",directory.toString(),"-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),"-I",Path.of("target/native/generated").toAbsolutePath().toString(),source.toString(),"-o",executable.toString()));
        run(List.of(executable.toString()));
    }
    private static String payload(BasicNode d){return "!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->-1);}
    private static void run(List<String> command)throws Exception{
        var process=new ProcessBuilder(command).redirectErrorStream(true).start();String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(process.waitFor(120,java.util.concurrent.TimeUnit.SECONDS));assertEquals(0,process.exitValue(),output);
    }
}
