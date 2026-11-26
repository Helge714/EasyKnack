package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.document.KnackDocumentCodec;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class FreeverbTest {
    @Test void registeredControlsAndRoutingUseTheNativeContract() {
        var node=new FreeverbNode();var state=new NodeState(node.defaults());var layout=node.layout(state);
        assertEquals(7,layout.ports().size());assertEquals(6,node.processor().inputs().size());
        assertEquals(.5,state.number(FreeverbNode.ROOM,0));assertEquals(.5,state.number(FreeverbNode.DAMP,0));
        assertEquals(1,state.number(FreeverbNode.WIDTH,0));assertEquals(.5,state.number(FreeverbNode.DRY,0));
        assertTrue(MultipleGroups.supports(node));assertFalse(node.supportsSampleFeedback());
        var registry=new NodeRegistry(new GLNodeTypeRegistry(),new com.vingame.glswing.node.model.GLNodeCatalog());assertNotNull(NodeRegistry.require(node.registration().id()));
        assertTrue(node.encode(state,key->-1).contains("-1"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.freeverb",matches="true")
    void nativeImpulseStereoStateAndAllocationChecks()throws Exception {
        var dir=Files.createTempDirectory(Path.of("target"),"freeverb-native-").toAbsolutePath();
        var executable=dir.resolve("test.exe");compile(Path.of("native/audio-host/tests/FreeverbTest.cpp").toAbsolutePath(),executable,dir,false);
        run(List.of(executable.toString()),dir.resolve("run.log"));System.out.print(Files.readString(dir.resolve("run.log")));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.freeverbDocument",matches=".+")
    void savedFreeverbGraphMatchesTheDirectKernelAndBenchmarks()throws Exception {
        var codec=new KnackDocumentCodec();GLNodeGraphDocument original;
        try(var in=Files.newInputStream(Path.of(System.getProperty("easyknack.test.freeverbDocument")))){original=codec.read(in,codec.currentSchemaVersion());}
        var freeverb=original.root().graph().nodes().stream().filter(n->original.embeddedAssets().values().stream().anyMatch(a->a.compositeDescriptor()!=null&&a.compositeDescriptor().nodeTypeId().equals(n.typeId())&&new NodeState(a.metadata()).text("easyknack.title","").equals("Freeverb"))).findFirst().orElseThrow();
        var source=original.root().graph().nodes().stream().filter(n->n.typeId().startsWith("easyknack.audio.input")).findFirst().orElseThrow();
        var value=new ValueNode();var graph=new GLNodeGraph();graph.edit("Reference source",e->{
            for(var node:original.root().graph().nodes())e.addNode(node.id().equals(source.id())?new GLGraphNode(node.id(),value.registration().id(),value.defaults()):node);
            for(var edge:original.root().graph().connections())e.addConnection(edge.output().nodeId().equals(source.id())?new GLGraphConnection(edge.id(),new GLNodeEndpoint(source.id(),"value-out"),edge.input()):edge);
        });
        var root=original.root();var document=new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),graph.snapshot(),root.viewState(),root.metadata(),root.compositeDescriptor()),original.embeddedAssets(),original.externalAssets());
        String[] program={null};SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){c.openDocument("Freeverb reference",document);program[0]=c.audioGraph();}});
        double[] p={.5,.5,1,1.0/3,.5};String[] names={"roomSize","damp","width","wet","dry"};
        for(int i=0;i<names.length;i++){var v=freeverb.properties().get(GroupUnits.key(names[i]));if(v!=null)p[i]=((Number)v.toJavaValue()).doubleValue();}
        var dir=Files.createTempDirectory(Path.of("target"),"freeverb-graph-").toAbsolutePath();var cpp=dir.resolve("test.cpp");
        Files.writeString(cpp,"""
            #include "AudioGraph.hpp"
            #include "nodes/FreeverbNode.hpp"
            #include <cassert>
            #include <chrono>
            #include <cstdio>
            using namespace easyknack;
            int main(){
              auto graph=AudioGraph::parse(%s);graph.prepare(48000,256);
              FreeverbNode direct,right;right.shareChannelState(direct);direct.prepare(48000,256);right.prepare(48000,256);
              direct.values={%sf,%sf,%sf,%sf,%sf};
              std::array<float,256> input{},left{},outRight{},expected{},expectedRight{},silence{};
              const float* inputs[6]{input.data()},*rights[6]{input.data()};char rates[6]{'a','c','c','c','c','c'};
              auto render=[&](int count){ProcessContext c{inputs,6,left.data(),std::size_t(count),48000,silence.data(),std::size_t(count)};c.rightInputs=rights;c.sourceRates=rates;direct.process(c);c.output=outRight.data();c.channelIndex=1;right.process(c);};
              auto setValue=[&](float value){for(auto& n:graph.nodes)if(n.processorId=="value"){auto& l=static_cast<ValueNode&>(*n.processor);l.controls.reset();l.value=value;l.sliding=false;auto& r=static_cast<ValueNode&>(*n.rightProcessor);r.controls.reset();r.value=value;r.sliding=false;}};
              float error=0;double energy=0;input[0]=1;setValue(1);render(1);graph.process(expected.data(),1,0,nullptr);graph.process(expectedRight.data(),1,1,nullptr);
              assert(std::abs(left[0]-expected[0])<1e-5);assert(std::abs(outRight[0]-expectedRight[0])<1e-5);
              input.fill(0);setValue(0);double graphTime=0,directTime=0;
              for(int b=0;b<375;b++){
                auto start=std::chrono::steady_clock::now();graph.process(expected.data(),256,0,nullptr);graph.process(expectedRight.data(),256,1,nullptr);graphTime+=std::chrono::duration<double>(std::chrono::steady_clock::now()-start).count();
                start=std::chrono::steady_clock::now();render(256);directTime+=std::chrono::duration<double>(std::chrono::steady_clock::now()-start).count();
                for(int i=0;i<256;i++){error=std::max({error,std::abs(left[i]-expected[i]),std::abs(outRight[i]-expectedRight[i])});energy+=expected[i]*expected[i]+expectedRight[i]*expectedRight[i];}
              }
              std::printf("Saved graph: maximum impulse error %%g, energy %%g, graph %%g ms, direct %%g ms (%%gx faster)\\n",error,energy,graphTime*1000,directTime*1000,graphTime/directTime);
              assert(energy>1e-5);assert(error<1e-4);
            }
            """.formatted(AdvancedSourceExportTest.quote(program[0]),Double.toString(p[0]),Double.toString(p[1]),Double.toString(p[2]),Double.toString(p[3]),Double.toString(p[4])));
        var executable=dir.resolve("test.exe");compile(cpp,executable,dir,true);run(List.of(executable.toString()),dir.resolve("run.log"));System.out.print(Files.readString(dir.resolve("run.log")));
    }
    private static void compile(Path source,Path executable,Path dir,boolean graph)throws Exception {
        var command=new ArrayList<>(List.of(System.getProperty("easyknack.test.cxx","clang++"),"-std=c++17","-O2","-static","-ffp-contract=off","-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),"-I",Path.of("native/audio-host/third_party").toAbsolutePath().toString(),"-I",Path.of("target/native/generated").toAbsolutePath().toString(),source.toString()));
        if(graph)command.add(Path.of("native/audio-host/src/nodes/LorisLibrary.cpp").toAbsolutePath().toString());command.addAll(List.of("-o",executable.toString()));run(command,dir.resolve("build.log"));
    }
    private static void run(List<String> command,Path log)throws Exception {
        var process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{assertTrue(process.waitFor(180,TimeUnit.SECONDS),"Native timeout: "+log);assertEquals(0,process.exitValue(),Files.readString(log));}
        finally{if(process.isAlive())process.destroyForcibly();}
    }
}
