package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.model.*;
import java.nio.*;import java.nio.file.*;import java.util.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class FileBufferTest {
 @TempDir Path temp;
 Path pcm()throws Exception{var p=temp.resolve("stereo.ekpcm");var b=ByteBuffer.allocate(24+16*8).order(ByteOrder.LITTLE_ENDIAN);b.put("EKPCM001".getBytes()).putInt(48000).putInt(2).putLong(16);for(int i=0;i<16;i++)b.putFloat(i).putFloat(-i);Files.write(p,b.array());return p;}
 @Test void memoryLabelSurvivesPresentationRefresh()throws Exception{var n=new FileBufferNode();var state=new HashMap<>(n.defaults());state.put("file",GLNodeValue.of("source.wav\n"+pcm()));var row=n.layout(new NodeState(state)).rows().stream().filter(r->r.id().equals("memory")).findFirst().orElseThrow();var control=row.editor().create(row.label(),v->{}).component();String before=((com.vingame.glswing.ui.GLLabel)control).getText();EditorPresentation.update(row,control);assertEquals(before,((com.vingame.glswing.ui.GLLabel)control).getText());assertTrue(before.contains("MB"));}
 @Test void selectionSlidersAreFixedAndRespectEachOther()throws Exception{
  var n=new FileBufferNode();var values=new HashMap<>(n.defaults());values.put("file",GLNodeValue.of("source.wav\n"+pcm()));values.put("start",GLNodeValue.of(4.0/48000));values.put("end",GLNodeValue.of(12.0/48000));
  var rows=n.layout(new NodeState(values)).rows();var start=(UnitSlider)rows.stream().filter(r->r.id().equals("start")).findFirst().orElseThrow().editor();var end=(UnitSlider)rows.stream().filter(r->r.id().equals("end")).findFirst().orElseThrow().editor();
  assertTrue(start.spec().bar());assertEquals(11.0/48000,start.spec().max(),1e-12);assertEquals(5.0/48000,end.spec().min(),1e-12);assertEquals(16.0/48000,end.spec().max(),1e-12);
  values.put("end",GLNodeValue.of(0.0));assertEquals(16.0/48000,n.normalizeState(new NodeState(values)).number("end",0),1e-12);
 }
 @Test void exposesSameReferenceTypeAsRecordingBuffer(){var n=new FileBufferNode();assertEquals(new HistoryBufferNode().layout(new NodeState(new HistoryBufferNode().defaults())).rows().get(0).dataType(),n.layout(new NodeState(n.defaults())).rows().get(0).dataType());}
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true") void sharedStereoBufferWorksWithExistingReaders()throws Exception{
  var p=pcm();var node=new FileBufferNode();var state=new HashMap<>(node.defaults());state.put("file",GLNodeValue.of("source.wav\n"+p));String config="!"+NativeContractGenerator.fingerprint(node)+" "+node.encode(new NodeState(state),id->-1);
  var source=temp.resolve("test.cpp");Files.writeString(source,"""
  #include "AudioGraph.hpp"
  #include "nodes/FileBufferNode.hpp"
  #include "nodes/HistoryReaderNode.hpp"
  #include "nodes/PositionReaderNode.hpp"
  #include <cassert>
  using namespace easyknack;
  int main(){FileBufferNode left,right;std::istringstream a("%s"),b("%s");left.configure(a);right.configure(b);right.shareChannelState(left);assert(left.data==right.data);
   auto* l=static_cast<HistoryResource*>(left.resourceOutput(0));auto* r=static_cast<HistoryResource*>(right.resourceOutput(0));assert(l!=r&&l->samples.size()==16&&r->samples[5]==-5);assert(l->writeHeadAt(0)==0&&l->writeHeadAt(255)==0);
   float audio[8]{},distance[8]{},position[8]{},silence[8]{};float* outs[]{audio,distance,position};const DspResource* resources[]{l,nullptr,nullptr,nullptr}; const float* inputs[4]{};
   ProcessContext c{inputs,4,audio,8,48000,silence,8};c.outputs=outs;c.outputCount=3;c.resourceInputs=resources;
   HistoryReaderNode reader;reader.process(c);for(int i=0;i<8;i++)assert(audio[i]==i);reader.process(c);for(int i=0;i<8;i++)assert(audio[i]==i+8);reader.process(c);assert(audio[0]==0);
   // RANGE_TEST
   PositionReaderNode fixed;fixed.position=.25;fixed.process(c);assert(audio[0]==4&&distance[0]==.5f);resources[0]=r;fixed.process(c);assert(audio[0]==-4);
   resources[0]=l;HistoryReaderNode slow;c.sampleRate=96000;slow.process(c);for(int i=0;i<8;i++)assert(audio[i]==i*.5f);
  }
  """.formatted(config,config).replace("// RANGE_TEST", "auto clip=FileBufferData::load("+quote(p.toString())+",4.0/48000,8.0/48000);assert(clip->channels[0].samples.size()==4);assert(clip->channels[0].samples[0]==4);assert(clip->channels[1].samples[3]==-7);auto wrapped=readBufferPosition(clip->channels[0],4,0);assert(wrapped.audio==4);"));RateConverterTest.compileAndRun(source,temp.resolve("test.exe"),temp);
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true") void graphAcceptsFileBufferAndReader()throws Exception{
  var graph=new GLNodeGraph();var file=add(graph,new FileBufferNode(),Map.of("file",GLNodeValue.of("source.wav\n"+pcm())));var reader=add(graph,new HistoryReaderNode());var out=add(graph,new OutputNode());connect(graph,file,"history",reader,"history");connect(graph,reader,"audio-out",out,"audio-in");
  try(var host=new AudioHostClient()){host.start(AudioHostClient.defaultExecutable(),true);assertEquals("OK",host.command(AudioGraphEncoder.encode(graph.snapshot())));assertEquals("OK",host.command("PLAY"));assertTrue(host.command("STATS").startsWith("OK"));assertEquals("OK",host.command("STOP"));}
 }
}
