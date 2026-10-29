package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
class SpectralPaletteTest {
    @Test void frameOnlyProcessorsCannotBeEncodedAsTemporalNodes(){
        for(var definition:List.of(new BinContextNode(),new BinReadNode(),new BinOutputNode(),new BinRandomNode(),new BinDiscardNode(),new BinHoldNode(),new BinMemoryNode(),new BinUniformNode(),new BinTriggerNode())){
            assertTrue(definition.requiresSpectralContext(),definition.title());
            var graph=new com.vingame.glswing.node.model.GLNodeGraph();
            graph.edit("Frame-only node",e->e.addNode(new com.vingame.glswing.node.model.GLGraphNode(com.vingame.glswing.node.model.GLNodeId.random(),definition.registration().id(),definition.defaults())));
            var failure=assertThrows(IllegalArgumentException.class,()->com.vin.easyknack.audio.AudioGraphEncoder.encode(graph.snapshot()));
            assertTrue(failure.getMessage().contains(definition.title()));
        }
        assertFalse(new MathNode().requiresSpectralContext());assertTrue(new MathNode().supportsSpectralBins());
        assertFalse(new SpectralGraphNode().requiresSpectralContext());
    }

    @Test void popupPolicyAndCreationFollowTheCurrentGroup()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var normal=c.availableNodes();
            assertFalse(normal.stream().anyMatch(e->Set.of("binread","bincontext","binoutput","binrandom","bindiscard","binhold").contains(NodeRegistry.find(e.type().id()).map(d->d.processor().id()).orElse(""))));
            var create=normal.stream().filter(e->e.type().id().equals(SpectralGroups.CREATE_TYPE)).findFirst().orElseThrow();
            var oscillator=normal.stream().filter(e->e.type().id().equals(new OscillatorNode().registration().id())).findFirst().orElseThrow();
            int before=c.workspace().currentGraph().snapshot().nodes().size();c.workspace().createNode(create,314,159);
            assertEquals(before+1,c.workspace().currentGraph().snapshot().nodes().size());
            var selected=c.workspace().editor().getSelectedNodes().iterator().next();assertEquals(314,selected.getX());assertEquals(159,selected.getY());
            c.actions().openSelectedComposite();assertTrue(c.isSpectralContext());
            assertTrue(c.availableNodes().stream().allMatch(e->e.type().id().equals(FormulaGroups.CREATE)||SpectralGroups.supports(NodeRegistry.find(e.type().id()).orElse(null))));
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new BinContextNode().registration().id())));
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(FormulaGroups.CREATE)));
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new BinReadNode().registration().id())));assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new BinHoldNode().registration().id())));
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new ComparatorNode().registration().id())));
            assertTrue(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new BinRandomNode().registration().id())));
            before=c.workspace().currentGraph().snapshot().nodes().size();c.workspace().createNode(oscillator,0,0);c.workspace().createNode(create,0,0);
            assertEquals(++before,c.workspace().currentGraph().snapshot().nodes().size());
            var output=c.workspace().currentGraph().snapshot().nodes().stream().filter(n->NodeRegistry.find(n.typeId()).orElse(null) instanceof BinOutputNode).findFirst().orElseThrow();
            assertFalse(c.availableNodes().stream().anyMatch(e->e.type().id().equals(output.typeId())));
            c.workspace().currentGraph().edit("Remove output",e->e.removeNode(output.id()));
            var replacement=c.availableNodes().stream().filter(e->e.type().id().equals(new BinOutputNode().registration().id())).findFirst().orElseThrow();
            c.workspace().createNode(replacement,600,120);assertEquals(before,c.workspace().currentGraph().snapshot().nodes().size());
            c.workspace().navigateBack();assertFalse(c.isSpectralContext());assertTrue(c.availableNodes().contains(create));assertFalse(c.availableNodes().stream().anyMatch(e->e.type().id().equals(new BinReadNode().registration().id())));
        }});
    }
}
