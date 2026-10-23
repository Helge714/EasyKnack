package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class SpectralFeaturesTest {
    @Test void nodeContractsExposeAllDescriptorsAndDynamicViewerInputs(){
        var features=new SpectralFeaturesNode();var layout=features.layout(new NodeState(features.defaults()));
        assertEquals(8,layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).count());
        assertTrue(layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).allMatch(r->r.rate()==ProcessingRate.CONTROL));
        assertTrue(NodeRegistry.find(features.registration().id()).isPresent());
        var viewer=new ValueViewerNode();var values=new HashMap<>(viewer.defaults());values.put("inputs",com.vingame.glswing.node.model.GLNodeValue.of(8.));
        assertEquals(8,viewer.layout(new NodeState(values)).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.INPUT).count());
        assertTrue(viewer.encode(new NodeState(values),p->3).endsWith("3 3 3 3 3 3 3 3"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void momentsFluxFrameCadenceAndPeakCapture()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"features-native-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/SpectralFeaturesTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
