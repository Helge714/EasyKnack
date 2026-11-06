package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class HilbertNodeTest {
    @Test void registeredAudioPortsAndRouting(){
        var node=new HilbertNode();var state=new NodeState(node.defaults());
        assertTrue(NodeRegistry.find(node.registration().id()).isPresent());
        var layout=node.layout(state);
        assertEquals(List.of("direct","quadrature","audio-in","length"),layout.rows().stream().map(NodeBuilder.Row::id).toList());
        assertTrue(layout.rows().stream().filter(r->r.kind()!=NodeBuilder.RowKind.EDITOR).allMatch(r->r.rate()==ProcessingRate.AUDIO));
        var length=layout.rows().stream().filter(r->r.id().equals("length")).findFirst().orElseThrow();
        assertEquals(NodeBuilder.RowKind.EDITOR,length.kind());assertFalse(length.isInspectorOnly());
        assertEquals("257",state.text(HilbertNode.LENGTH,""));
        assertTrue(node.supportsSampleFeedback());
        var mute=new MuteRouting();node.mute(mute,state);assertEquals(1,mute.bypasses().size());
        var graph=new com.vingame.glswing.node.model.GLNodeGraph();
        var source=AdvancedSourceExportTest.add(graph,new OscillatorNode());
        var filter=AdvancedSourceExportTest.add(graph,node);
        var out=AdvancedSourceExportTest.add(graph,new OutputNode());
        AdvancedSourceExportTest.connect(graph,source,"audio-out",filter,"audio-in");
        AdvancedSourceExportTest.connect(graph,filter,"quadrature",out,"audio-in");
        assertTrue(com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()).contains("hilbert"));
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.audio",matches="true")
    void firPhaseAlignmentAndSsbRejection()throws Exception{
        var dir=Files.createTempDirectory(Path.of("target"),"hilbert-test-").toAbsolutePath();
        RateConverterTest.compileAndRun(Path.of("native/audio-host/tests/HilbertTest.cpp").toAbsolutePath(),dir.resolve("test.exe"),dir);
    }
}
