package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import java.util.*;
class ExecutionContractsTest {
 @Test void executionContractsAreReusedPerThreadAndInvalidatedForPluginChanges()throws Exception{
  var before=NodeRegistry.executionContracts();assertSame(before,NodeRegistry.executionContracts());
  var other=new java.util.concurrent.atomic.AtomicReference<NodeRegistry.ExecutionContracts>();
  var thread=new Thread(()->other.set(NodeRegistry.executionContracts()));thread.start();thread.join();assertNotSame(before,other.get());
  var builtinIds=NodeRegistry.builtinDefinitions().stream().map(d->d.registration().id()).collect(java.util.stream.Collectors.toSet());
  var plugins=NodeRegistry.definitionsList().stream().filter(d->!builtinIds.contains(d.registration().id())).toList();
  NodeRegistry.installPlugins(plugins);assertNotSame(before,NodeRegistry.executionContracts());
 }
 @Test void immutableWavetableSamplesAreReusedWithoutAliasingSampleArrays(){
  String curve=CurveData.encode(WaveformEditor.sine());String encoded=WaveformSamples.encodedValue(curve);
  assertSame(encoded,WaveformSamples.encodedValue(curve));assertEquals(WaveformSamples.encode(WaveformSamples.fromValue(curve)),encoded);
  var samples=WaveformSamples.fromValue(curve);samples[10]=99;
  assertEquals(encoded,WaveformSamples.encodedValue(curve));
  assertEquals(encoded,WaveformSamples.encodedValue(WaveformSamples.PREFIX+encoded));
  assertThrows(IllegalArgumentException.class,()->WaveformSamples.encodedValue("samples:00"));
 }
}
