package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.AudioGraphEncoder;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
class UnifiedFftTest {
    @Test void singleFftFeedsBothAnalysisAndAudio(){
        assertFalse(new FFTStreamNode().isListedInCatalog());assertEquals(true,new SpectrumNode().defaults().get("synchronous").toJavaValue());
        var g=new GLNodeGraph();var fft=add(g,new SpectrumNode());var viewer=add(g,new SpectrumViewerNode());var correlation=add(g,new SpectrumCorrelationNode());var inverse=add(g,new IFFTNode());
        connect(g,fft,"analysis",viewer,"analysis");connect(g,fft,"analysis",correlation,"spectrum");connect(g,fft,"analysis",inverse,"spectrum");
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());
        assertTrue(GLNodeGraphValidator.validate(g.snapshot(),types,GLGraphCyclePolicy.DELAYED_FEEDBACK).diagnostics().stream().noneMatch(d->d.code().contains("type")));
        assertDoesNotThrow(()->AudioGraphEncoder.encode(g.snapshot()));assertTrue(GraphDiagnostics.fftWarnings(g.snapshot()).isEmpty());
        g.edit("Async",e->e.setProperty(fft,"synchronous",GLNodeValue.of(false)));
        var warnings=GraphDiagnostics.fftWarnings(g.snapshot());assertEquals(1,warnings.size());assertEquals(Set.of(inverse),warnings.get(0).nodeIds());assertTrue(warnings.get(0).connectionIds().isEmpty());
        assertDoesNotThrow(()->AudioGraphEncoder.encode(g.snapshot()));
    }
    @Test void migrationRetainsOldModesConnectionsAndNodeIds(){
        var g=new GLNodeGraph();var old=add(g,new FFTStreamNode());var inverse=add(g,new IFFTNode());var analysis=add(g,new SpectrumNode());
        g.edit("Old analysis",e->e.removeProperty(analysis,"synchronous"));connect(g,old,"spectrum",inverse,"spectrum");
        var document=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,g.snapshot(),GLNodeEditorViewState.empty()));
        var migrated=NodeDocuments.prepare(document).root().graph();
        assertEquals("easyknack.analysis.spectrum",migrated.findNode(old).orElseThrow().typeId().split("\\.variant\\.")[0]);
        assertEquals(true,migrated.findNode(old).orElseThrow().properties().get("synchronous").toJavaValue());
        assertEquals(false,migrated.findNode(analysis).orElseThrow().properties().get("synchronous").toJavaValue());
        assertEquals("analysis",migrated.connections().get(0).output().portId());assertEquals(g.snapshot().connections().get(0).id(),migrated.connections().get(0).id());
    }
    @Test void warningFollowsModifiersAndGroupsAndClearsWithoutMuting()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var g=new GLNodeGraph();var fft=add(g,new SpectrumNode(),Map.of("synchronous",GLNodeValue.of(false)));var shift=add(g,new SpectrumShiftNode());var inverse=add(g,new IFFTNode());var out=add(g,new OutputNode());
            connect(g,fft,"analysis",shift,"spectrum");connect(g,shift,"out",inverse,"spectrum");connect(g,inverse,"out",out,"audio-in");
            c.openDocument("Async FFT",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,g.snapshot(),GLNodeEditorViewState.empty())));
            assertEquals(1,c.workspace().editor().getNodes().stream().filter(n->n.getDiagnosticStatus()==com.vingame.glswing.node.GLNodeConnectionStatus.WARNING).count());
            assertTrue(c.workspace().editor().getConnections().stream().allMatch(w->w.isActive()));assertDoesNotThrow(c::audioGraph);
            c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView v&&Set.of(fft,shift).contains(v.nodeId())).toList());c.actions().extractComposite();
            assertEquals(1,c.workspace().editor().getNodes().stream().filter(n->n.getDiagnosticStatus()==com.vingame.glswing.node.GLNodeConnectionStatus.WARNING).count());assertDoesNotThrow(c::audioGraph);
            c.actions().openSelectedComposite();c.workspace().currentGraph().edit("Sync",e->e.setProperty(fft,"synchronous",GLNodeValue.of(true)));assertDoesNotThrow(c::audioGraph);
            c.workspace().navigateBack();assertTrue(c.workspace().editor().getNodes().stream().noneMatch(n->n.getDiagnosticStatus()==com.vingame.glswing.node.GLNodeConnectionStatus.WARNING));
        }});
    }
}
