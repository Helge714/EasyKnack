package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.quote;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class SmallFftTest {
    @Test void smallWindowsAreSelectableAndKeepTheDefault(){
        for(var definition:List.of(new SpectrumNode(),new FFTStreamNode())){
            assertEquals("2048",definition.defaults().get("size").toJavaValue());
            for(String size:List.of("64","128")){
                var values=new HashMap<>(definition.defaults());values.put("size",GLNodeValue.of(size));
                assertDoesNotThrow(()->definition.layout(new NodeState(values)));
            }
        }
        assertEquals(16,SpectrumNode.minimumSynchronousHop(64));
        assertEquals(32,SpectrumNode.minimumSynchronousHop(128));
        assertEquals(64,SpectrumNode.minimumSynchronousHop(256));
    }
    @Test void smallWindowsPersistAndExportInsideSpectralEffects()throws Exception{
        for(String size:List.of("64","128")){
            var fixture=SpectralGroupsTest.fixture();
            var assets=new HashMap<String,com.vingame.glswing.node.document.GLNodeGraphAsset>();
            for(var asset:fixture.document().embeddedAssets().values()){
                var graph=new GLNodeGraph(asset.graph());
                graph.edit("Small FFT",e->{for(var node:asset.graph().nodes()){
                    var definition=NodeRegistry.find(node.typeId()).orElse(null);
                    if(definition instanceof SpectrumNode||definition instanceof FFTStreamNode)
                        e.setProperty(node.id(),"size",GLNodeValue.of(size));
                }});
                assets.put(asset.id(),new com.vingame.glswing.node.document.GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor()));
            }
            var document=new com.vingame.glswing.node.document.GLNodeGraphDocument(fixture.document().root(),assets,List.of());
            var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(document,bytes);
            var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
            assertEquals(NodeDocuments.comparable(document),NodeDocuments.comparable(restored));
            assertNotNull(GroupSourceExporter.create(restored,fixture.instance()));
        }
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void nativeConfigurationAndStreamingReconstruction()throws Exception{
        var directory=Files.createTempDirectory(Path.of("target"),"small-fft-").toAbsolutePath();
        var checks=new StringBuilder();
        for(var definition:List.of(new SpectrumNode(),new FFTStreamNode()))for(int size:List.of(64,128)){
            var values=new HashMap<>(definition.defaults());values.put("size",GLNodeValue.of(Integer.toString(size)));
            if(definition instanceof SpectrumNode)values.put("overlap",GLNodeValue.of(1.));
            String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(values),p->-1);
            checks.append('{').append(definition.getClass().getSimpleName()).append(" node;std::istringstream in(")
                .append(quote(payload)).append(");node.configure(in);assert(node.size==").append(size)
                .append("&&node.hop==").append(size/4).append(");}\n");
        }
        var source=directory.resolve("configure.cpp");
        Files.writeString(source,"#include \"nodes/SpectrumNode.hpp\"\n#include \"nodes/FFTStreamNode.hpp\"\n#include <cassert>\nusing namespace easyknack;int main(){"+checks+"}");
        RateConverterTest.compileAndRun(source,directory.resolve("configure.exe"),directory);
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/SpectrumStreamTest.cpp").toAbsolutePath(),directory.resolve("stream.exe"),directory);
    }
}
