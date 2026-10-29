package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
class SpectrumPruneTest {
    @Test void octaveAndLegacyFactorUseDifferentInputs(){
        var node=new SpectrumBlurNode();
        var values=new HashMap<>(node.defaults());values.put("mode",com.vingame.glswing.node.model.GLNodeValue.of("octaves"));
        var payload=node.encode(new NodeState(values),p->p.equals(SpectrumBlurNode.OCTAVES)?7:-1);
        assertTrue(payload.endsWith("octaves 7 0.0 gaussian 0 noise"),payload);
        values.put("mode",com.vingame.glswing.node.model.GLNodeValue.of("factor"));values.put("factor",com.vingame.glswing.node.model.GLNodeValue.of(2.));
        assertTrue(node.encode(new NodeState(values),p->p.equals(SpectrumBlurNode.FACTOR)?5:-1).contains("5 2.0 factor -1"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void prunesLoudestPreservesPhaseAndOctavesMatchFactors()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"prune-test-").toAbsolutePath();var source=dir.resolve("test.cpp");
        var definition=new SpectrumPruneNode();String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(definition.defaults()),p->-1);
        Files.writeString(source,"""
            #include "nodes/SpectrumPruneNode.hpp"
            #include "nodes/SpectrumBlurNode.hpp"
            #include <cassert>
            using namespace easyknack;
            int main(){SpectrumPruneNode node;std::istringstream params(%s);node.configure(params);node.prepare(48000,256);
              SpectrumStream input;input.prepare(256);input.size=input.windowSize=256;input.sampleRate=48000;input.count=2;input.hop=64;
              for(int f=0;f<2;f++){input.frames[f].start=f*64;input.frames[f].bins[0]={3,0};input.frames[f].bins[10]={0,2};input.frames[f].bins[20]={-2,0};input.frames[f].bins[128]={1,0};SpectrumStream::realSymmetry(input.frames[f],256);}
              float keep=1,out=0,silence=0;const float* inputs[]{nullptr,&keep};const DspResource* resources[]{&input};ProcessContext c{inputs,2,&out,1,48000,&silence};c.resourceInputs=resources;
              for(float value:{0.f,1.f,1.f/129,2.f/129,-1.f,2.f}){
                keep=value;node.process(c);
                for(int f=0;f<2;f++){assert(node.stream.frames[f].start==f*64);auto& result=node.stream.frames[f].bins;
                  if(value<=0)for(int k=0;k<256;k++)assert(result[k]==std::complex<double>{});
                  else if(value>=1)for(int k=0;k<256;k++)assert(result[k]==input.frames[f].bins[k]);
                  else {assert(result[10]==input.frames[f].bins[10]);assert(result[0]==std::complex<double>{});assert(result[128]==std::complex<double>{});assert((result[20]!=std::complex<double>{})==(value>1.f/129));assert(result[246]==std::conj(result[10]));}
                }
              }
              SpectrumBlurNode factor,octave;factor.prepare(48000,256);octave.prepare(48000,256);factor.logarithmic=octave.logarithmic=true;octave.octaveMode=true;
              const float* empty[]{nullptr,nullptr,nullptr,nullptr};c.inputs=empty;c.inputCount=4;
              for(float value:{0.f,.5f,1.f,2.f}){factor.factor=std::exp2(value);octave.octaves=value;factor.process(c);octave.process(c);for(int k=0;k<256;k++)assert(std::abs(factor.stream.frames[0].bins[k]-octave.stream.frames[0].bins[k])<1e-6);}
              input.count=0;node.process(c);assert(!node.stream.count);resources[0]=nullptr;node.process(c);assert(!node.stream.size);
            }
            """.formatted(AdvancedSourceExportTest.quote(payload)));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
