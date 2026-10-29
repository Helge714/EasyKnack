package com.vin.easyknack.ui;
import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;
class MemoryFilterTest {
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void exportedMemoryUsesTheSameFrameState()throws Exception{
        var f=SpectralUniformTest.fixture(true);var node=SpectralUniformTest.compiled(f);var def=NodeRegistry.require(node.typeId());
        String direct="!"+NativeContractGenerator.fingerprint(def)+" "+def.encode(new NodeState(node.properties()),p->p.equals(SpectralGraphNode.IN)?0:p.equals(SpectralGraphNode.external(0))?1:-1);
        var export=GroupSourceExporter.create(f.document(),f.instance());var d=export.definition();
        String payload="!"+NativeContractGenerator.fingerprint(d)+" "+d.encode(new NodeState(d.defaults()),p->p.value().equals("input_0")?0:1);
        var dir=Files.createTempDirectory(Path.of("target"),"memory-export-").toAbsolutePath();
        Files.writeString(dir.resolve(export.className()+".hpp"),export.files().get("native/"+export.className()+".hpp"));
        var definitions=new ArrayList<>(NodeRegistry.builtinDefinitions());definitions.add(d);Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(definitions));
        String code="""
            #include "AudioGraph.hpp"
            #include "%s.hpp"
            #include <cassert>
            using namespace easyknack;
            template<class T> void check(const char* payload){
                T node;std::istringstream in(payload);node.configure(in);node.prepare(48000,256);
                SpectrumStream input;input.prepare(256);input.size=input.windowSize=1024;input.hop=256;input.identity=7;input.count=1;
                float factor=0,out[256]{},silence[256]{};const float* inputs[17]{};inputs[1]=&factor;char rates[17]{};std::fill_n(rates,17,'c');
                const DspResource* resources[17]{};resources[0]=&input;ProcessContext c{inputs,17,out,256,48000,silence,256};c.sourceRates=rates;c.resourceInputs=resources;
                float expected=10;
                for(int f=0;f<4;f++){
                    factor=f==0||f==2?0:f==1?.25:1;float value=10*(f+1);expected=expected*(1-factor)+value*factor;
                    input.blockStart=f*256;input.frames[0].start=f*256-768;
                    for(int k=0;k<=512;k++)input.frames[0].bins[k]={value+k,0};SpectrumStream::realSymmetry(input.frames[0],1024);
                    node.process(c);auto* result=static_cast<SpectrumStream*>(node.resourceOutput(0));assert(result->count==1);
                    for(int k=0;k<=512;k++)assert(result->frames[0].bins[k].real()==expected+k);
                }
            }
            int main(){check<SpectralGraphNode>(%s);check<%s>(%s);}
            """.formatted(export.className(),AdvancedSourceExportTest.quote(direct),export.className(),AdvancedSourceExportTest.quote(payload));
        var source=dir.resolve("test.cpp");Files.writeString(source,code);RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
    @Test void defaultsRetainFilterSound(){
        var node=new FilterNode();var state=new NodeState(node.defaults());
        assertEquals("2",state.text(FilterNode.ORDER,""));assertFalse(state.flag(FilterNode.COMPENSATE,true));
        assertTrue(node.encode(state,p->-1).endsWith("lowpass 2 0"));
        assertTrue(new BinMemoryNode().supportsSpectralBins());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void memoryAndFilterResponse()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"memory-filter-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/MemoryFilterTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
