package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;
import com.vin.easyknack.compiler.SpectralSourceSpecialization;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;

class SpectralOptimizationTest {
    private static String instruction(BasicNode d,Map<String,GLNodeValue> properties,int... refs){
        var values=new HashMap<>(d.defaults());values.putAll(properties);
        var inputs=d.processor().inputs();
        return d.processor().id()+" !"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->refs[inputs.indexOf(p)]);
    }
    private static String program(boolean skip){
        // A constant multiplication must run once per frame; a bin-index comparison
        // is the discard condition. The remaining multiplication varies per bin.
        return (skip?"BIN2 6 1 ":"BIN1 6 ")
            +instruction(new BinContextNode(),Map.of(),-1)+" "
            +instruction(new MathNode(),Map.of("a",GLNodeValue.of(2.),"b",GLNodeValue.of(3.),"clamp",GLNodeValue.of(false)),-1,-1)+" "
            +instruction(new ComparatorNode(),Map.of("operation",GLNodeValue.of("greater"),"b",GLNodeValue.of(3.)),1280,-1)+" "
            +instruction(new BinDiscardNode(),Map.of(),2)+" "
            +instruction(new MathNode(),Map.of("clamp",GLNodeValue.of(false)),0,1)+" "
            +instruction(new BinOutputNode(),Map.of(),4,-1);
    }
    private static GLGraphNode compiled(String program){return new GLGraphNode(GLNodeId.random(),new SpectralGraphNode().registration().id(),Map.of("program",GLNodeValue.of(SpectralGraphNode.hex(program))));}
    private static String payload(String program){var d=new SpectralGraphNode();return "!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(compiled(program).properties()),p->-1);}
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void parallelRangesMatchSerialAndBakedAndBusyWorkerFallsBack()throws Exception{
        String body=instruction(new BinContextNode(),Map.of(),-1)+" "
            +instruction(new BinRandomNode(),Map.of("by-frame",GLNodeValue.of(true)),1280)+" "
            +instruction(new MathNode(),Map.of("operation",GLNodeValue.of("add"),"clamp",GLNodeValue.of(false)),0,1)+" "
            +instruction(new MathNode(),Map.of("b",GLNodeValue.of(.5),"clamp",GLNodeValue.of(false)),2,-1)+" "
            +instruction(new BinOutputNode(),Map.of(),3,256);
        String parallel="BIN1 5 "+body,serial="BIN2 5 2 "+body;
        var specialization=SpectralSourceSpecialization.emit("Parallel",List.of(compiled(parallel)),Set.of());
        String code="""
            #include "AudioGraph.hpp"
            #include <cassert>
            namespace easyknack {
            %s
            }
            using namespace easyknack;
            int main(){
                SpectralGraphNode serial,parallel;ParallelSpectralKernel baked;
                std::istringstream a(%s),b(%s),d(%s);serial.configure(a);parallel.configure(b);baked.configure(d);
                for(auto* p:{&serial,&parallel,static_cast<SpectralGraphNode*>(&baked)})p->prepare(48000,256);
                SpectrumStream input;input.prepare(256);input.size=input.windowSize=4096;input.sampleRate=48000;input.count=2;input.hop=128;
                const DspResource* resources[]{&input};float silence[256]{},out[256]{};
                ProcessContext c{nullptr,1,out,256,48000,silence,256};c.resourceInputs=resources;
                for(int frame=0;frame<20;frame++){
                    for(int f=0;f<2;f++){input.frames[f].start=frame*256+f*128;for(int k=0;k<4096;k++)input.frames[f].bins[k]={std::sin(k*.7+frame),std::cos(k*.3)};}
                    serial.process(c);parallel.process(c);baked.process(c);
                    for(int f=0;f<2;f++)for(int k=0;k<4096;k++){
                        assert(serial.stream.frames[f].bins[k]==parallel.stream.frames[f].bins[k]);
                        assert(serial.stream.frames[f].bins[k]==baked.stream.frames[f].bins[k]);
                    }
                }
                if(std::thread::hardware_concurrency()>1){
                    assert(parallel.parallelFrames>0&&baked.parallelFrames>0);
                    auto pool=SpectralWorker::shared();std::atomic<bool> release{false};
                    assert(pool->submit([](void* p)noexcept{while(!static_cast<std::atomic<bool>*>(p)->load())std::this_thread::yield();},&release));
                    auto previous=parallel.parallelFrames;parallel.process(c);assert(parallel.parallelFrames==previous);
                    release.store(true);pool->join();
                    for(int f=0;f<2;f++)for(int k=0;k<4096;k++)assert(serial.stream.frames[f].bins[k]==parallel.stream.frames[f].bins[k]);
                }
                auto previous=parallel.parallelFrames;input.size=input.windowSize=256;parallel.process(c);assert(parallel.parallelFrames==previous);
            }
            """.formatted(specialization.code(),AdvancedSourceExportTest.quote(payload(serial)),AdvancedSourceExportTest.quote(payload(parallel)),AdvancedSourceExportTest.quote(payload(parallel)));
        var dir=Files.createTempDirectory(Path.of("target"),"spectral-parallel-").toAbsolutePath();var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void staticProgramsAreSpecializedAndDynamicProgramsRemainSafe(){
        var node=compiled(program(false));var result=SpectralSourceSpecialization.emit("Test",List.of(node),Set.of());
        assertEquals("TestSpectralKernel",result.type());assertTrue(result.code().contains("MathOperation::multiply"));
        assertFalse(result.code().contains("MathNode::parseOperation"));
        assertEquals("SpectralGraphNode",SpectralSourceSpecialization.emit("Test",List.of(node),Set.of(node.id())).type());
    }
    @Test void emptyBinSettingIsSavedCompiledAndInvalidatesBake()throws Exception{
        var asset=SpectralGroups.create();var root=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of());root.edit("Group",e->e.addNode(instance));
        var rootAsset=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty());
        var before=new GLNodeGraphDocument(rootAsset,Map.of(asset.id(),asset),List.of());
        var metadata=new HashMap<>(asset.metadata());metadata.put(SpectralGroups.SKIP_EMPTY,GLNodeValue.of("true"));metadata.put(SpectralGroups.PARALLEL,GLNodeValue.of("false"));
        asset=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,asset.compositeDescriptor());
        var after=new GLNodeGraphDocument(rootAsset,Map.of(asset.id(),asset),List.of());
        assertNotEquals(GroupBakes.key(before,instance.id()),GroupBakes.key(after,instance.id()));
        var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(after,bytes);
        var saved=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(saved);
        var flat=SpectralGroups.flatten(GroupCompiler.prepare(saved.root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));
        var compiled=flat.graph().orElseThrow().nodes().get(0);
        String program=new String(HexFormat.of().parseHex(new NodeState(compiled.properties()).text("program","")),java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(program.startsWith("BIN2 2 3 "));
        var export=GroupSourceExporter.create(saved,instance);
        assertTrue(export.files().get("native/"+export.className()+".hpp").contains("SpectralKernel:SpectralGraphNode"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void preparedAndBakedBinsKeepDiscardAndExplicitEmptyBinSemantics()throws Exception{
        var programs=List.of(program(false),program(true));
        var specialization=SpectralSourceSpecialization.emit("Test",programs.stream().map(SpectralOptimizationTest::compiled).toList(),Set.of());
        var code=new StringBuilder("#include \"AudioGraph.hpp\"\n#include <cassert>\nnamespace easyknack {\n").append(specialization.code()).append("}\nusing namespace easyknack;int main(){\n");
        for(String program:programs)for(String type:List.of("SpectralGraphNode","TestSpectralKernel")){
            code.append("{").append(type).append(" node;std::istringstream params(").append(AdvancedSourceExportTest.quote(payload(program))).append(");node.configure(params);node.prepare(48000,256);")
                .append("assert(node.invariantSteps.size()==1);SpectrumStream input;input.prepare(256);input.size=input.windowSize=8;input.sampleRate=48000;input.count=1;input.hop=4;float silence=0,out=0;const DspResource* resources[]{&input};ProcessContext c{nullptr,1,&out,1,48000,&silence,1};c.resourceInputs=resources;")
                .append("for(int f=0;f<3;f++){for(int k=0;k<5;k++)input.frames[0].bins[k]={double(k),0};node.process(c);for(int k=0;k<5;k++)assert(node.stream.frames[0].bins[k].real()==(k>3?0:6*k));}")
                .append("assert(node.discardedBinFrames==").append(program.startsWith("BIN2")?6:3).append(");}\n");
        }
        // An empty input bin can produce a nonzero output. Never enable skipping implicitly.
        for(boolean skip:List.of(false,true)){
            String p=(skip?"BIN2 1 1 ":"BIN1 1 ")+instruction(new BinOutputNode(),Map.of("a",GLNodeValue.of(7.)),-1,-1);
            code.append("{SpectralGraphNode node;std::istringstream p(").append(AdvancedSourceExportTest.quote(payload(p))).append(");node.configure(p);node.prepare(48000,256);SpectrumStream input;input.prepare(256);input.size=input.windowSize=8;input.count=1;float out=0;const DspResource* resources[]{&input};ProcessContext c{nullptr,1,&out,1,48000,&out,1};c.resourceInputs=resources;node.process(c);assert(node.stream.frames[0].bins[1].real()==").append(skip?0:7).append(");}\n");
        }
        code.append("}");var dir=Files.createTempDirectory(Path.of("target"),"spectral-optimization-").toAbsolutePath();var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
