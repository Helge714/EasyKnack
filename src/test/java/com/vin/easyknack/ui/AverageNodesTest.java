package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class AverageNodesTest {
    @Test void registeredContractsExposeTriggerAndHeldResults(){
        for(var node:new BasicNode[]{new ValueAverageNode(),new AnalysisAverageNode()}){
            assertTrue(NodeRegistry.find(node.registration().id()).isPresent());
            var layout=node.layout(new NodeState(node.defaults()));
            assertEquals(3,layout.rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).count());
            assertTrue(layout.rows().stream().anyMatch(r->r.id().equals("finished")&&r.dataType()==PortDataType.TRIGGER));
            assertFalse(node.encode(new NodeState(node.defaults()),p->-1).isBlank());
        }
        var node=new SpectralFeaturesNode();
        assertTrue(node.layout(new NodeState(node.defaults())).rows().stream().anyMatch(r->r.id().equals("spectrum")&&r.dataType()==PortDataType.ANALYSIS));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void finiteMeasurementTimingPowerCorrelationAndHold()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"averages-native-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/AverageNodesTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
