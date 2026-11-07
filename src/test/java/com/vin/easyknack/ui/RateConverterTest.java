package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class RateConverterTest {
    @Test void ratesAndReductionSettingsAreTypedAndPersisted() {
        var d=new RateConverterNode();
        assertTrue(NodeRegistry.find(d.registration().id()).isPresent());
        var graph=new GLNodeGraph();var id=GLNodeId.random();
        graph.edit("Converter",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
        for(var rate:List.of("control","audio")){
            graph.edit("Rate",e->e.setProperty(id,"rate",GLNodeValue.of(rate)));
            var n=graph.snapshot().findNode(id).orElseThrow();var layout=d.layout(new NodeState(n.properties()));
            assertEquals(rate,layout.rows().stream().filter(r->r.id().equals("out")).findFirst().orElseThrow().rate().id());
            assertEquals(rate.equals("control"),layout.rows().stream().filter(r->r.id().equals("reduction")).findFirst().orElseThrow().isEnabled());
            assertTrue(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()).endsWith("@ "+ProcessingRate.fromId(rate).code()+" a"));
        }
    }

    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void blockReductionsEventsAndStereoAreCorrect()throws Exception{
        var d=new RateConverterNode();var dir=Files.createTempDirectory(Path.of("target"),"rate-test-").toAbsolutePath();
        var cpp=new StringBuilder("""
                #include "AudioGraph.hpp"
                #include <cassert>
                using namespace easyknack;
                int main(){
                    std::array<float,5> samples{-2,-1,0,1,4};float output[5]{};const float* inputs[]={samples.data()};
                    ProcessContext c{inputs,1,output,1,48000,samples.data(),5};
                """);
        var expected=Map.of("first",-2.,"last",4.,"mean",.4,"rms",Math.sqrt(22./5),"minimum",-2.,"maximum",4.,"peak",4.);
        for(var mode:RateConverterNode.Reduction.values()){
            var values=new HashMap<>(d.defaults());values.put("reduction",GLNodeValue.of(mode.id()));
            String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->0);
            cpp.append("{RateConverterNode node;std::istringstream p(\"").append(payload).append("\");node.configure(p);node.process(c);assert(std::abs(output[0]- (").append(expected.get(mode.id())).append("))<1e-5);}\n");
        }
        String event=payload(d,"event",0),control=payload(d,"control",0),audio=payload(d,"audio",0);
        cpp.append("""
                    {RateConverterNode node;std::istringstream p("%s");node.configure(p);c.bufferSize=5;node.process(c);for(int i=0;i<5;i++)assert(output[i]==samples[i]);}
                    auto events=AudioGraph::parse("GRAPH 4 src osc -1 440 @ a a convert rateconvert %s @ e a math math 1 0 -1 2 multiply 0 @ e ee out out 2 0 @ a a");
                    std::array<float,256> left{},right{};events.process(left.data(),17);
                    for(int i=0;i<17;i++){assert(events.nodes[1].eventOffsets[0].test(i));assert(std::abs(left[i]-events.nodes[0].buffer[i]*2)<1e-6);}
                    auto stereo=AudioGraph::parse("GRAPH 3 src context @ c - convert rateconvert %s @ c a out out 1 0 @ a a");
                    for(int frames:{1,17,256}){stereo.process(left.data(),frames,0,nullptr);stereo.process(right.data(),frames,1,nullptr);for(int i=0;i<frames;i++){assert(left[i]==-1);assert(right[i]==1);}}
                }
                """.formatted(audio,event,control));
        var source=dir.resolve("test.cpp");Files.writeString(source,cpp);var executable=dir.resolve("test.exe");
        compileAndRun(source,executable,dir);
    }
    static String payload(BasicNode d,String rate,int source){var values=new HashMap<>(d.defaults());values.put("rate",GLNodeValue.of(rate));return "!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(values),p->source);}
    static void compileAndRun(Path source,Path executable,Path includes)throws Exception{
        run(List.of(System.getProperty("easyknack.test.cxx"),"-std=c++17","-O2","-static","-I",includes.toString(),"-I",Path.of("native/audio-host/src").toAbsolutePath().toString(),"-I",Path.of("target/native/generated").toAbsolutePath().toString(),source.toString(),Path.of("native/audio-host/src/nodes/LorisLibrary.cpp").toAbsolutePath().toString(),"-o",executable.toString()));
        run(List.of(executable.toString()));
    }
    static void run(List<String> command)throws Exception{
        var output=Files.createTempFile(Path.of("target"),"native-test-",".log");
        var process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try{assertTrue(process.waitFor(120,java.util.concurrent.TimeUnit.SECONDS),"Native test timed out");assertEquals(0,process.exitValue(),Files.readString(output));}
        finally{if(process.isAlive()){process.destroyForcibly();process.waitFor();}}
    }
}
