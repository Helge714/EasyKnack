package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
class ControlOutputsTest {
 @TempDir Path temp;
 @Test void midiVoiceIsAppendedAndPitchWheelRangeStillDefaultsToTwo(){
  var node=new MidiNoteNode();assertEquals(2.,new NodeState(node.defaults()).number(MidiNoteNode.BEND,0));
  var outputs=node.layout(new NodeState(node.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).toList();
  assertEquals(List.of("frequency","gate","trigger","velocity","voice"),outputs.stream().map(NodeBuilder.Row::id).toList());
 }
 @Test void mutedDigitalNoiseKeepsNeutralOutputsAndOnlyItsFrequencyInput(){
  var graph=new GLNodeGraph();var ids=java.util.stream.IntStream.range(0,3).mapToObj(i->GLNodeId.random()).toList();
  var definitions=List.<BasicNode>of(new ValueNode(),new DigitalNoiseNode(),new MathNode());
  graph.edit("Neutral mute",e->{for(int i=0;i<3;i++)e.addNode(new GLGraphNode(ids.get(i),definitions.get(i).registration().id(),definitions.get(i).defaults()));e.setProperty(ids.get(1),GLNodeState.MUTED,GLNodeValue.of(true));
   for(String input:List.of("frequency","strength"))e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(0),"value-out"),new GLNodeEndpoint(ids.get(1),input)));
   e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(1),"factor"),new GLNodeEndpoint(ids.get(2),"a")));
   e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(ids.get(1),"frequency-out"),new GLNodeEndpoint(ids.get(2),"b")));
  });
  var contracts=NodeRegistry.executionContracts();var visible=contracts.definitions().normalize(graph.snapshot());
  assertEquals(1,MutedConnections.diagnostics(visible,contracts.types()).size());
  var executable=com.vin.easyknack.graph.GraphExecutionPolicy.executable(visible,contracts.types());var noise=executable.findNode(ids.get(1)).orElseThrow();
  assertFalse(GLNodeState.isMuted(noise));assertTrue(new NodeState(noise.properties()).flag(DigitalNoiseNode.MUTE_BYPASS,false));
  assertTrue(executable.connections().stream().anyMatch(w->w.input().equals(new GLNodeEndpoint(ids.get(1),"frequency"))));
  assertFalse(executable.connections().stream().anyMatch(w->w.input().equals(new GLNodeEndpoint(ids.get(1),"strength"))));
  assertEquals(3,executable.connections().size());
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
 void nativeNoiseMuteVoiceIndicesAndPitchWheelWork()throws Exception{
  RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/ControlOutputsTest.cpp").toAbsolutePath(),temp.resolve("controls.exe"),temp);
 }
}
