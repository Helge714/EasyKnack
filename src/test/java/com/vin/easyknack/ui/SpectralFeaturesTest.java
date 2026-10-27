package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class SpectralFeaturesTest {
    @Test void frequencyScaleControlsMomentUnitsAndNativeContract(){
        var features=new SpectralFeaturesNode();var values=new HashMap<>(features.defaults());
        assertEquals("linear",values.get("frequency-scale").toJavaValue());
        values.put("frequency-scale",com.vingame.glswing.node.model.GLNodeValue.of("logarithmic"));
        var state=new NodeState(values);var rows=features.layout(state).rows();
        var scale=rows.stream().filter(r->r.id().equals("frequency-scale")).findFirst().orElseThrow();
        assertTrue(scale.isInspectorOnly());
        assertEquals(UnitFormat.OCTAVE,rows.stream().filter(r->r.id().equals("spread")).findFirst().orElseThrow().formats().stored());
        assertEquals(UnitFormat.HZ,rows.stream().filter(r->r.id().equals("centroid")).findFirst().orElseThrow().formats().stored());
        assertTrue(features.encode(state,p->3).endsWith("logarithmic"));
        values.put("spread.unit",com.vingame.glswing.node.model.GLNodeValue.of("cent"));
        assertEquals(UnitFormat.CENT,features.layout(new NodeState(values)).rows().stream().filter(r->r.id().equals("spread")).findFirst().orElseThrow().unit());
        values.put("frequency-scale",com.vingame.glswing.node.model.GLNodeValue.of("linear"));
        assertEquals(UnitFormat.HZ,features.layout(new NodeState(values)).rows().stream().filter(r->r.id().equals("spread")).findFirst().orElseThrow().unit());
    }
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
