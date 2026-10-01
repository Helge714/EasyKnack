package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
class ReaderWrapTest {
 @TempDir Path temp;
 @Test void wrapDefaultsPreserveExistingGraphs(){assertEquals(true,new com.vin.easyknack.nodes.HistoryReaderNode().defaults().get("wrap").toJavaValue());assertEquals(true,new com.vin.easyknack.nodes.PositionReaderNode().defaults().get("wrap").toJavaValue());}
 @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true") void readersHoldBoundariesWithoutInterpolatingAcrossThem()throws Exception{
  var source=temp.resolve("test.cpp");Files.writeString(source,"""
  #include "AudioGraph.hpp"
  #include "nodes/HistoryReaderNode.hpp"
  #include "nodes/PositionReaderNode.hpp"
  #include <cassert>
  using namespace easyknack;
  int main(){
   HistoryResource h;h.samples={1,2,3,4};h.sampleRate=48000;h.end=4;h.immutable=true;
   float audio[8]{},distance[8]{},position[8]{},silence[8]{};float* outs[]{audio,distance,position};const float* inputs[4]{};const DspResource* resources[]{&h,nullptr,nullptr,nullptr};
   ProcessContext c{inputs,4,audio,8,48000,silence,8};c.outputs=outs;c.outputCount=3;c.resourceInputs=resources;
   HistoryReaderNode reader;reader.wrapping=false;reader.process(c);for(int i=0;i<8;i++)assert(audio[i]==std::min(i+1,4));reader.process(c);for(float v:audio)assert(v==4);
   reader.speed=-1;reader.process(c);assert(audio[0]==4&&audio[1]==3&&audio[7]==1);
   PositionReaderNode fixed;fixed.wrapping=false;fixed.position=1;fixed.process(c);for(float v:audio)assert(v==4);fixed.position=1.5;fixed.process(c);assert(audio[0]==4);fixed.position=-.5;fixed.process(c);assert(audio[0]==1);fixed.position=.875;fixed.process(c);assert(audio[0]==4);
   fixed.wrapping=true;fixed.process(c);assert(audio[0]==2.5);
   HistoryResource live;live.prepare(48000,4./48000,2);resources[0]=&live;c.bufferSize=2;HistoryReaderNode fast;fast.speed=2;fast.wrapping=false;
   for(int block=0;block<4;block++){live.beginBlock(2);live.append(block*2+1);live.append(block*2+2);fast.process(c);assert(audio[0]<=block*2+1&&audio[1]<=block*2+2);assert(audio[0]>=std::max(1,block*2-2));}
   // A new block's last write cannot leak into interpolation at its first sample.
   live.beginBlock(2);live.append(9);live.append(100);auto value=readBufferClamped(live,8.75,0);assert(value.audio==9);
   HistoryReaderNode legacy;std::istringstream old("!b99cd9f79b33ffd5 normalized-position-v3 -1 -1 1 -1 0 -1");legacy.configure(old);assert(legacy.wrapping);
   PositionReaderNode oldPosition;std::istringstream old2("!5eedf3ee01ec24b1 -1 -1 0 -1 0");oldPosition.configure(old2);assert(oldPosition.wrapping);
  }
  """);RateConverterTest.compileAndRun(source,temp.resolve("test.exe"),temp);
 }
}
