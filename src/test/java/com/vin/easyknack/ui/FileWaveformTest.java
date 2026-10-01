package com.vin.easyknack.ui;
import com.vin.easyknack.audio.FileWaveform;
import java.nio.*;import java.nio.file.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class FileWaveformTest {
 @TempDir Path temp;
 @Test void overviewPreservesTransientPeaksAndStereoAndRejectsTruncation()throws Exception{
  int frames=16384;var bytes=ByteBuffer.allocate(24+frames*8).order(ByteOrder.LITTLE_ENDIAN);bytes.put("EKPCM001".getBytes()).putInt(48000).putInt(2).putLong(frames);
  for(int i=0;i<frames;i++)bytes.putFloat(i==5?1:0).putFloat(i==6?-.75f:0);
  var path=temp.resolve("test.ekpcm");Files.write(path,bytes.array());var wave=FileWaveform.read(path);
  assertEquals(2048,wave.low()[0].length);assertEquals(1,wave.high()[0][0]);assertEquals(-.75f,wave.low()[1][0]);assertEquals(0,wave.low()[0][0]);
  Files.write(path,java.util.Arrays.copyOf(bytes.array(),64));assertThrows(java.io.EOFException.class,()->FileWaveform.read(path));
 }
 @Test void draggingCommitsOnlyOnReleaseAndRespectsEnd()throws Exception{
  var p=temp.resolve("drag.ekpcm");var b=ByteBuffer.allocate(24+480*4).order(ByteOrder.LITTLE_ENDIAN);b.put("EKPCM001".getBytes()).putInt(48000).putInt(1).putLong(480);Files.write(p,b.array());
  var plot=new FileWaveformEditor.Plot();var writes=new java.util.ArrayList<Double>();
  javax.swing.SwingUtilities.invokeAndWait(()->{plot.setSize(212,120);plot.onEdit((first,value)->{assertTrue(first);writes.add(value);});plot.configure("source.wav\n"+p,0,.01,.01);});
  var ready=new java.util.concurrent.atomic.AtomicBoolean();long deadline=System.nanoTime()+3_000_000_000L;
  do{javax.swing.SwingUtilities.invokeAndWait(()->ready.set(plot.hasWaveform()));if(!ready.get())Thread.sleep(10);}while(!ready.get()&&System.nanoTime()<deadline);assertTrue(ready.get());
  javax.swing.SwingUtilities.invokeAndWait(()->{plot.controlMousePressed(mouse(plot,6));plot.controlMouseDragged(mouse(plot,212));assertTrue(writes.isEmpty());plot.controlMouseReleased(mouse(plot,212));assertEquals(1,writes.size());assertEquals(.01-1.0/48000,writes.get(0),1e-12);});
 }
 private static com.vingame.glswing.input.GLMouseEvent mouse(FileWaveformEditor.Plot plot,float x){
  var original=new java.awt.event.MouseEvent(new javax.swing.JPanel(),java.awt.event.MouseEvent.MOUSE_PRESSED,0,0,(int)x,30,1,false,1);
  return new com.vingame.glswing.input.GLMouseEvent(original,new com.vingame.glswing.input.GLPickResult(1,plot,x,30,x,30,1,0,0,1,0,0),0,0);
 } @Test void zoomReadsActualVisibleSamplesAndSnapsOnlyCommonCrossings()throws Exception{
  int frames=1024;var bytes=ByteBuffer.allocate(24+frames*8).order(ByteOrder.LITTLE_ENDIAN);bytes.put("EKPCM001".getBytes()).putInt(48000).putInt(2).putLong(frames);
  for(int i=0;i<frames;i++)bytes.putFloat(i<500?-.5f:.5f).putFloat(i<500?-.25f:.25f);
  var path=temp.resolve("snap.ekpcm");Files.write(path,bytes.array());var wave=FileWaveform.read(path,499./frames,501./frames);assertEquals(2,wave.low()[0].length);assertEquals(-.5f,wave.low()[0][0]);assertEquals(.5f,wave.low()[0][1]);
  assertEquals(500./48000,FileWaveform.snap(path,510./48000,0,frames/48000.),1e-12);
  assertEquals(510./48000,FileWaveform.snap(path,510./48000,505./48000,frames/48000.),1e-12);
  assertThrows(java.util.concurrent.CancellationException.class,()->FileWaveform.read(path,0,1,()->true));
 }
 @Test void viewportClampsToFileAndSingleSample(){var plot=new FileWaveformEditor.Plot();plot.configure("",0,1,1);plot.view(-.2,.3);assertEquals(0,plot.viewStart());assertEquals(.5,plot.viewEnd());plot.view(.99,2);assertEquals(0,plot.viewStart());assertEquals(1,plot.viewEnd());plot.view(.5,.5);assertEquals(1./48000,plot.viewEnd()-plot.viewStart(),1e-12);} @Test void waveformCapturesNavigationBeforeNodeEditor()throws Exception{
  var editor=new com.vingame.glswing.node.GLNodeEditor();var plot=new FileWaveformEditor.Plot();editor.addGL(plot);
  var dispatcher=Class.forName("com.vingame.glswing.component.GLContainerInputDispatcher");
  var wheel=dispatcher.getDeclaredMethod("wheelCaptureTarget",com.vingame.glswing.input.GLMouseTarget.class);wheel.setAccessible(true);assertSame(plot,wheel.invoke(null,plot));
  var press=dispatcher.getDeclaredMethod("capturePress",com.vingame.glswing.input.GLPickResult.class,int.class);press.setAccessible(true);
  var hit=new com.vingame.glswing.input.GLPickResult(1,plot,20,20,20,20,1,0,0,1,0,0);
  assertSame(plot,((com.vingame.glswing.input.GLPickResult)press.invoke(null,hit,2)).getTarget());
  assertFalse(plot.capturesMousePress(1));
 }
 @Test void labelsFitEvenInNarrowNodes(){
  java.util.function.ToDoubleFunction<String> measure=text->text.length()*8;
  assertEquals("12 s",FileWaveformEditor.Plot.fitText("12 s",32,measure));
  assertTrue(measure.applyAsDouble(FileWaveformEditor.Plot.fitText("123456.123456 – 234567.123456 s",80,measure))<=80);
  assertEquals("",FileWaveformEditor.Plot.fitText("Loading",4,measure));
 } @Test void offscreenSelectionDoesNotSubmitZeroWidthRectangle(){
  assertDoesNotThrow(()->FileWaveformEditor.Plot.paintSelection(null,-100,-10,6,200,18,84));
  assertDoesNotThrow(()->FileWaveformEditor.Plot.paintSelection(null,210,300,6,200,18,84));
  assertDoesNotThrow(()->FileWaveformEditor.Plot.paintSelection(null,-100,6,6,200,18,84));
  assertDoesNotThrow(()->FileWaveformEditor.Plot.paintSelection(null,206,300,6,200,18,84));
  assertDoesNotThrow(()->FileWaveformEditor.Plot.paintSelection(null,20,20,6,200,18,84));
 } @Test void samplerDocumentIsValidAndHasClockResetAndEnvelope(){
  var doc=NodeDocuments.prepare(SamplerExample.create());assertEquals(9,doc.root().graph().nodes().size());
  assertTrue(doc.root().graph().connections().stream().anyMatch(c->c.input().portId().equals("reset")&&c.output().portId().equals("trigger")));
  assertDoesNotThrow(()->com.vin.easyknack.audio.AudioGraphEncoder.encode(doc.root().graph()));
 }
}
