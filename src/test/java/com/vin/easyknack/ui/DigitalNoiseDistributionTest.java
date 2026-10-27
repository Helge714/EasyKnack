package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.GLNodeValue;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class DigitalNoiseDistributionTest {
    @Test void distributionUsesTheSharedNativeContract(){
        var node=new DigitalNoiseNode();var values=new HashMap<>(node.defaults());
        assertEquals("uniform",values.get("distribution").toJavaValue());
        values.put("distribution",GLNodeValue.of("gaussian"));
        assertTrue(node.encode(new NodeState(values),p->-1).endsWith("gaussian"));
        assertEquals(3,node.processor().inputs().size());assertTrue(node.supportsSampleFeedback());
        assertEquals(3,node.layout(new NodeState(values)).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).count());
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void timeWeightedDistributionsAndSampleFeedback()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"digital-noise-distribution-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/DigitalNoiseDistributionTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
