package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.GLNodeConnectionStatus;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class DelayMuteTest {
    @Test void muteBypassesDelayAndUnmuteRestoresIt()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=new GLNodeGraph();var source=add(graph,new OscillatorNode());var delay=add(graph,new DelayNode());var out=add(graph,new OutputNode());
            connect(graph,source,"audio-out",delay,"audio-in");connect(graph,delay,"audio-out",out,"audio-in");
            c.openDocument("Delay",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty())));
            c.workspace().currentGraph().edit("Mute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(true)));
            var encoded=c.audioGraph();assertTrue(encoded.contains(" osc "));assertFalse(encoded.contains(" delay "));assertTrue(encoded.startsWith("GRAPH 2 "));
            c.workspace().currentGraph().edit("Unmute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(false)));assertTrue(c.audioGraph().contains(" delay "));
        }});
    }
    @Test void bypassCannotRemoveTheOnlyFeedbackBoundarySilently()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Feedback",SampleFeedbackTest.document());var delay=c.workspace().currentGraph().snapshot().nodes().stream().filter(n->n.typeId().equals(new DelayNode().registration().id())).findFirst().orElseThrow();
            c.workspace().currentGraph().edit("Mute",e->e.setProperty(delay.id(),GLNodeState.MUTED,GLNodeValue.of(true)));
            assertDoesNotThrow(c::audioGraph);
            var input=GraphDiagnosticsTest.view(c,new MathNode().registration().id()).findPort("a");
            assertEquals(1,input.getConnectionCount());assertEquals(0,input.getActiveConnectionCount());assertFalse(input.isContentHiddenByConnection());
            assertEquals(1,c.workspace().editor().getConnections().stream().filter(w->!w.isActive()).count());
            assertEquals(3,c.snapshotDocument().root().graph().connections().size());
            assertEquals(GLNodeConnectionStatus.NORMAL,GraphDiagnosticsTest.view(c,new MathNode().registration().id()).getDiagnosticStatus());
            c.workspace().currentGraph().edit("Unmute",e->e.setProperty(delay.id(),GLNodeState.MUTED,GLNodeValue.of(false)));
            assertDoesNotThrow(c::audioGraph);assertEquals(GLNodeConnectionStatus.NORMAL,GraphDiagnosticsTest.view(c,new MathNode().registration().id()).getDiagnosticStatus());
            assertTrue(GraphDiagnosticsTest.view(c,new MathNode().registration().id()).findPort("a").isContentHiddenByConnection());
            assertTrue(c.workspace().editor().getConnections().stream().allMatch(w->w.isActive()));
        }});
    }
}
