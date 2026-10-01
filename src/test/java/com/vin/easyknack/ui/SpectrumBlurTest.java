package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
class SpectrumBlurTest {
    @Test void legacyRandomModesAndCheckboxes(){
        var node=new BinRandomNode();
        for(String old:List.of("fixed","frame")){
            var state=node.normalizeState(new NodeState(Map.of("mode",com.vingame.glswing.node.model.GLNodeValue.of(old))));
            assertTrue(state.flag(BinRandomNode.BY_BIN,false));assertEquals(old.equals("frame"),state.flag(BinRandomNode.BY_FRAME,false));
        }
        for(boolean bin:List.of(false,true))for(boolean frame:List.of(false,true)){
            var values=new HashMap<>(node.defaults());values.put("by-bin",com.vingame.glswing.node.model.GLNodeValue.of(bin));values.put("by-frame",com.vingame.glswing.node.model.GLNodeValue.of(frame));
            assertEquals("0 "+(bin?frame?"frame":"fixed":frame?"shared-frame":"constant"),node.encode(new NodeState(values),p->-1));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void blurPreservesPowerAndZeroIsExactBypass()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"blur-test-").toAbsolutePath();
        var source=dir.resolve("test.cpp");Files.writeString(source,"""
            #include "nodes/SpectrumBlurNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){SpectrumBlurNode node;node.prepare(48000,256);SpectrumStream input;input.prepare(256);
              input.size=input.windowSize=256;input.sampleRate=48000;input.count=1;input.hop=64;input.frames[0].start=42;
              const DspResource* resources[]{&input};const float* inputs[]{nullptr,nullptr,nullptr};float out=0,silence=0;ProcessContext c{inputs,3,&out,1,48000,&silence};c.resourceInputs=resources;
              for(int bin:{0,1,40,127,128})for(int width:{0,1,5,128}){
                std::fill(input.frames[0].bins.begin(),input.frames[0].bins.end(),std::complex<double>{});input.frames[0].bins[bin]={2,0};SpectrumStream::realSymmetry(input.frames[0],256);
                node.width=width;node.process(c);assert(node.stream.frames[0].start==42);
                double before=0,after=0;for(int k=0;k<256;k++){before+=std::norm(input.frames[0].bins[k]);after+=std::norm(node.stream.frames[0].bins[k]);}
                assert(std::abs(before-after)<1e-9);
                if(!width)for(int k=0;k<256;k++)assert(node.stream.frames[0].bins[k]==input.frames[0].bins[k]);
                else {assert(std::abs(node.stream.frames[0].bins[bin])<2);for(int k=1;k<128;k++)assert(node.stream.frames[0].bins[256-k]==std::conj(node.stream.frames[0].bins[k]));}
              }
              node.logarithmic=true;float factor=2;inputs[2]=&factor;
              for(int bin:{0,1,40,128})for(float value:{0.f,1.f,1.01f,2.f,8.f,100000.f}){
                factor=value;std::fill(input.frames[0].bins.begin(),input.frames[0].bins.end(),std::complex<double>{});input.frames[0].bins[bin]={2,0};SpectrumStream::realSymmetry(input.frames[0],256);
                node.process(c);double before=0,after=0;for(int k=0;k<256;k++){before+=std::norm(input.frames[0].bins[k]);after+=std::norm(node.stream.frames[0].bins[k]);}assert(std::abs(before-after)<1e-9);
                assert(node.stream.frames[0].bins[0]==input.frames[0].bins[0]);
                if(value<=1)for(int k=0;k<256;k++)assert(node.stream.frames[0].bins[k]==input.frames[0].bins[k]);
                if(bin==40&&value==2){
                  assert(std::abs(node.stream.frames[0].bins[19])<1e-8&&std::abs(node.stream.frames[0].bins[81])<1e-8);
                  assert(std::abs(std::norm(node.stream.frames[0].bins[20])/std::norm(node.stream.frames[0].bins[40])-2)<1e-9);
                }
              }
              input.count=0;node.process(c);assert(node.stream.count==0);
              resources[0]=nullptr;node.process(c);assert(node.stream.count==0&&node.stream.size==0);
            }
            """);
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
