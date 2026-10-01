package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
class UniversalDelayTest {
    @Test void onlyUniversalDelayIsListedAndTimeIsControl(){
        assertTrue(new DelayNode().isListedInCatalog());assertFalse(new BlockDelayNode().isListedInCatalog());assertFalse(new SampleDelayNode().isListedInCatalog());
        var delay=new DelayNode();var layout=delay.layout(new NodeState(delay.defaults()));
        assertEquals(ProcessingRate.CONTROL,layout.rows().stream().filter(r->r.key().equals(DelayNode.TIME)).findFirst().orElseThrow().rate());
    }
    @Test void oldDelaysMigrateWithoutChangingIdentityOrWiring(){
        for(var document:List.of(BlockDelayTest.document(),SampleFeedbackTest.document())){
            var migrated=DelayMigration.migrate(document);var old=document.root().graph();var graph=migrated.root().graph();
            assertEquals(old.nodes().get(0).id(),graph.nodes().get(0).id());assertEquals(old.connections().size(),graph.connections().size());
            var delay=graph.nodes().get(0);assertEquals("easyknack.audio.delay",delay.typeId());
            assertEquals((old.nodes().get(0).typeId().endsWith("sampledelay")?1.0:256.0)/com.vin.easyknack.audio.AudioProcessing.SAMPLE_RATE,((Number)delay.properties().get("time").toJavaValue()).doubleValue());
            assertEquals("sample",delay.properties().get("time.unit").toJavaValue());
            assertTrue(graph.connections().stream().filter(w->w.input().nodeId().equals(delay.id())).allMatch(w->w.input().portId().equals("audio-in")));
            assertTrue(graph.connections().stream().filter(w->w.output().nodeId().equals(delay.id())).allMatch(w->w.output().portId().equals("audio-out")));
            assertEquals(graph,DelayMigration.migrate(migrated).root().graph());
        }
    }
}
