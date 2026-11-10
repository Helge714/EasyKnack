package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.document.KnackDocumentCodec;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;

class SineCloudTest {
    @Test void registeredNodeEncodesControlInputsAndPersists()throws Exception{
        var node=new SineCloudNode();assertTrue(NodeRegistry.find(node.registration().id()).isPresent());
        assertEquals(4,node.processor().inputs().size());
        assertFalse(node.supportsSampleFeedback());
        var values=new java.util.HashMap<>(node.defaults());values.put("count",GLNodeValue.of(10000000));values.put("gaussian",GLNodeValue.of(true));
        assertEquals(GLNodeValue.of(true),values.get("randomize"));values.put("randomize",GLNodeValue.of(false));
        double nyquist=com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE*.5;
        values.put("maximum",GLNodeValue.of(nyquist));values.put("minimum",GLNodeValue.of(nyquist-1000));
        for(String input:java.util.List.of("minimum","maximum")){
            var row=node.layout(new NodeState(values)).rows().stream().filter(r->r.id().equals(input)).findFirst().orElseThrow();
            assertEquals(nyquist,((UnitSlider)row.editor()).spec().max());
        }
        var graph=new GLNodeGraph();var id=GLNodeId.random();
        graph.edit("Research stimulus",e->e.addNode(new GLGraphNode(id,node.registration().id(),values)));
        var document=new com.vingame.glswing.node.document.GLNodeGraphDocument(
            new com.vingame.glswing.node.document.GLNodeGraphAsset("test",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()),java.util.Map.of(),java.util.List.of());
        var codec=new KnackDocumentCodec();var out=new java.io.ByteArrayOutputStream();codec.write(document,out);
        var restored=codec.read(new java.io.ByteArrayInputStream(out.toByteArray()),codec.currentSchemaVersion());
        assertEquals(values,restored.root().graph().findNode(id).orElseThrow().properties());
        assertTrue(com.vin.easyknack.audio.AudioGraphEncoder.encode(restored.root().graph()).contains("sinecloud"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void equalSpectralAmplitudesTenMillionToneCapacityAndRealtimeHandover()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"sine-cloud-test-").toAbsolutePath();
        Files.writeString(dir.resolve("ParameterContracts.hpp"),NativeContractGenerator.generate(NodeRegistry.builtinDefinitions()));
        var definition=new SineCloudNode();var parameters=new java.util.HashMap<>(definition.defaults());
        parameters.put("maximum",GLNodeValue.of(com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE*.5));
        String payload="!"+NativeContractGenerator.fingerprint(definition)+" "+definition.encode(new NodeState(parameters),p->-1);
        var source=dir.resolve("test.cpp");
        Files.writeString(source,Files.readString(Path.of("native/audio-host/tests/SineCloudTest.cpp")).replace("SINE_CLOUD_PAYLOAD",AdvancedSourceExportTest.quote(payload)));
        RateConverterTest.compileAndRun(source,dir.resolve("test.exe"),dir);
    }
}
