package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
class LevelAnalysisTest {
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void hostAcceptsLiveMeterParameters()throws Exception{
  var node=new LevelAnalysisNode();
  try(var client=new com.vin.easyknack.audio.AudioHostClient()){
   client.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);
   for(var channel:List.of("left","right","mix"))for(int window:List.of(1,50,2000)){
    var values=new HashMap<>(node.defaults());values.put("channel",GLNodeValue.of(channel));values.put("window",GLNodeValue.of(window));values.put("smooth",GLNodeValue.of(true));
    String payload="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(values),p->0);
    assertEquals("OK",client.command("GRAPH 3 src osc -1 440 @ a a meter levelanalysis "+payload+" @ c a out out 257 0 @ a a"));
    assertEquals("OK",client.command("PLAY"));
   }
   assertEquals("OK",client.command("STOP"));
  }
 }
 @Test void registeredControlOutputsAndStableParameters(){
  var node=new LevelAnalysisNode();assertTrue(NodeRegistry.find(node.registration().id()).isPresent());
  var layout=node.layout(new NodeState(node.defaults()));
  for(var id:List.of("peak","rms"))assertEquals(ProcessingRate.CONTROL,layout.rows().stream().filter(r->r.id().equals(id)).findFirst().orElseThrow().rate());
  assertFalse(layout.rows().stream().filter(r->r.id().equals("attack")).findFirst().orElseThrow().isEnabled());
  var values=new HashMap<>(node.defaults());values.put("smooth",GLNodeValue.of(true));
  assertTrue(node.layout(new NodeState(values)).rows().stream().filter(r->r.id().equals("attack")).findFirst().orElseThrow().isEnabled());
  var graph=new GLNodeGraph();graph.edit("Meter",e->e.addNode(new GLGraphNode(GLNodeId.random(),node.registration().id(),node.defaults())));
  assertTrue(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()).endsWith("@ c a"));
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void nativeMeasurementsAndGraphScheduling()throws Exception{
  var node=new LevelAnalysisNode();String payload="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(node.defaults()),p->0);
  var dir=Files.createTempDirectory(Path.of("target"),"level-analysis-").toAbsolutePath();
  var source=dir.resolve("test.cpp");Files.writeString(source,"""
   #include "AudioGraph.hpp"
   #include <cassert>
   using namespace easyknack;
   bool approximatelyEqual(double a,double b){return std::abs(a-b)<1e-5;}
   int main(){
    LevelAnalysisNode node;std::istringstream config("%s");node.configure(config);node.windowMs=8;node.prepare(1000,256);
    float left[256]{},right[256]{},peak[256]{},rms[256]{};const float* inputs[]={left};const float* rights[]={right};float* outputs[]={peak,rms};
    ProcessContext c{inputs,1,peak,1,1000,left,8};c.outputs=outputs;c.outputCount=2;c.rightInputs=rights;
    for(int i=0;i<8;i++)left[i]=std::sin(2*3.141592653589793*i/8);
    node.process(c);assert(approximatelyEqual(peak[0],1)&&approximatelyEqual(rms[0],std::sqrt(.5)));
    for(int i=0;i<8;i++){left[i]=2;right[i]=4;}node.reset();node.process(c);assert(peak[0]==2&&rms[0]==2);
    node.channel=1;node.reset();node.process(c);assert(peak[0]==4&&rms[0]==4);
    node.channel=2;node.reset();node.process(c);assert(peak[0]==3&&rms[0]==3);
    for(int i=0;i<8;i++)right[i]=-left[i];node.reset();node.process(c);assert(peak[0]==0&&rms[0]==0);
    node.channel=0;node.reset();c.blockFrames=4;node.process(c);assert(rms[0]==2);
    std::fill_n(left,4,0);node.process(c);assert(approximatelyEqual(rms[0],std::sqrt(2.)));node.process(c);assert(rms[0]==0);
    std::fill_n(left,8,1);const float* voices[]={left,left};c.inputs=voices;c.rightInputs=nullptr;c.inputCount=2;c.blockFrames=8;
    node.reset();node.process(c);assert(peak[0]==2&&rms[0]==2);
    c.inputCount=0;node.process(c);assert(peak[0]==0&&rms[0]==0);
    c.inputs=inputs;c.inputCount=1;left[0]=std::numeric_limits<float>::quiet_NaN();left[1]=std::numeric_limits<float>::infinity();node.reset();node.process(c);assert(std::isfinite(rms[0]));
    std::fill_n(left,100,1);node.reset();node.smooth=true;node.attackMs=100;node.releaseMs=100;c.blockFrames=100;
    node.process(c);assert(approximatelyEqual(peak[0],1-std::exp(-1.))&&approximatelyEqual(rms[0],peak[0]));
    std::fill_n(left,100,0);node.process(c);assert(approximatelyEqual(peak[0],(1-std::exp(-1.))*std::exp(-1.)));
    LevelAnalysisNode live;live.windowMs=4;live.channel=1;auto* allocation=node.squares.data();node.applyParameters(live);assert(node.count==0&&node.window==4&&node.squares.data()==allocation);
    auto graph=AudioGraph::parse("GRAPH 3 src osc -1 1000 @ a a meter levelanalysis %s @ c a out out 1 0 @ a a");
    graph.process(peak,256);assert(peak[0]>.99f);for(int i=1;i<256;i++)assert(peak[i]==peak[0]);
   }
   """.formatted(payload,payload));
  RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
 }
}
