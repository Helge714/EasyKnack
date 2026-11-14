package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.nio.file.*;
import com.vin.easyknack.nodes.*;
class AudioRecorderTest {
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void liveHostRecordsWithoutAnAudioDeviceAndFinalizesOnTransportStop()throws Exception{
  var dir=Files.createTempDirectory(Path.of("target"),"recording-host-").toAbsolutePath();var target=dir.resolve("Take.wav");
  var graph=new com.vingame.glswing.node.model.GLNodeGraph();var osc=new OscillatorNode();var recorder=new AudioRecorderNode();var source=com.vingame.glswing.node.model.GLNodeId.random();var sink=com.vingame.glswing.node.model.GLNodeId.random();
  var values=new java.util.HashMap<>(recorder.defaults());values.put("file",com.vingame.glswing.node.model.GLNodeValue.of(target.toString()));
  graph.edit("Record",e->{e.addNode(new com.vingame.glswing.node.model.GLGraphNode(source,osc.registration().id(),osc.defaults()));e.addNode(new com.vingame.glswing.node.model.GLGraphNode(sink,recorder.registration().id(),values));e.addConnection(new com.vingame.glswing.node.model.GLGraphConnection(com.vingame.glswing.node.model.GLNodeConnectionId.random(),new com.vingame.glswing.node.model.GLNodeEndpoint(source,"audio-out"),new com.vingame.glswing.node.model.GLNodeEndpoint(sink,"audio-in")));});
  try(var host=new com.vin.easyknack.audio.AudioHostClient()){
   host.start(com.vin.easyknack.audio.AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot())));host.command("PLAY");host.command("TRIGGER "+sink+":start");
   awaitState(host,sink.toString(),1);
   graph.edit("Frequency",e->e.setProperty(source,"frequency",com.vingame.glswing.node.model.GLNodeValue.of(660.)));assertEquals("OK",host.command(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot())));
   Thread.sleep(50);host.command("STOP");awaitState(host,sink.toString(),2);
   var bytes=Files.readAllBytes(dir.resolve("Take.wav"));assertTrue(bytes.length>44);assertEquals(3,bytes[20]);assertEquals(2,bytes[22]);assertFalse(Files.exists(dir.resolve("Take_0002.wav")),"Graph handover must retain the active take");
  }
 }
 private static void awaitState(com.vin.easyknack.audio.AudioHostClient host,String id,int state)throws Exception{
  long until=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
  do{Thread.sleep(25);var frame=com.vin.easyknack.audio.AudioAnalysis.parse(host.command("ANALYSIS")).get(id);if(frame!=null&&frame.values().length>=4&&frame.values()[0]==-9151&&frame.values()[1]==state)return;}while(System.nanoTime()<until);
  fail("Recorder did not report state "+state);
 }
 @Test void recorderHasTypedTriggersAndAFileTargetWithoutDecoding(){
  var node=new AudioRecorderNode();assertTrue(NodeRegistry.find(node.registration().id()).isPresent());
  var state=new NodeState(node.defaults());assertEquals(NodeCategory.OUTPUT,node.registration().category());
  assertEquals(3,node.processor().contract().inputs().size());assertEquals("Stereo",state.text(AudioRecorderNode.CHANNELS,""));
  assertFalse(state.flag(AudioRecorderNode.OVERWRITE,true));assertEquals(4,state.number(AudioRecorderNode.DIGITS,0));
  assertTrue(node.layout(state).rows().stream().filter(r->r.key().equals(AudioRecorderNode.START)||r.key().equals(AudioRecorderNode.STOP)).allMatch(r->r.rate()==ProcessingRate.EVENT));
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void asynchronousWavRecordingPreservesSamplesAndFiles()throws Exception{
  var dir=Files.createTempDirectory(Path.of("target"),"recording-native-").toAbsolutePath();
  var source=dir.resolve("record.cpp");var run=dir.resolve("record.exe");
  var cpp=Files.readString(Path.of("native/audio-host/tests/AudioRecorderTest.cpp"));
  // The shared test accepts a fresh workspace path; the Java runner takes no arguments.
  cpp=cpp.replace("int main(int argc,char** argv){","int main(){ const int argc=2; const char* argv[]={\"test\",R\"PATH("+dir.toString()+")PATH\"};");
  Files.writeString(source,cpp);RateConverterTest.compileAndRun(source,run,dir);
 }
}
