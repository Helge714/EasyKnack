package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class GroupRoutingTest {
    @Test void routingChoicesPersistIndependentlyAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Routing",GroupUnitsTest.document(false));
            c.workspace().openComposite("test.asset");
            c.workspace().interfaceEditor().selectSection(0);
            var boxes=GroupEditingTest.descendants(c.workspace().interfaceEditor()).filter(n->n instanceof GLComboBox<?> b&&b.getItems().contains("")).map(n->(GLComboBox<String>)n).toList();
            assertEquals(4,boxes.size());
            boxes.get(0).setSelectedItem("Pitch");boxes.get(1).setSelectedItem("Out");
            var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());
            var asset=c.snapshotDocument().embeddedAssets().get("test.asset");
            assertNotNull(GroupRouting.pair(asset,types,GroupRouting.INSERT_IN,GroupRouting.INSERT_OUT));
            assertNull(GroupRouting.pair(asset,types,GroupRouting.MUTE_IN,GroupRouting.MUTE_OUT));
            boxes.get(2).setSelectedItem("Pitch");boxes.get(3).setSelectedItem("Out");
            asset=c.snapshotDocument().embeddedAssets().get("test.asset");
            assertNotNull(GroupRouting.pair(asset,types,GroupRouting.MUTE_IN,GroupRouting.MUTE_OUT));
            c.history().undo();
            assertNull(GroupRouting.pair(c.snapshotDocument().embeddedAssets().get("test.asset"),types,GroupRouting.MUTE_IN,GroupRouting.MUTE_OUT));
            c.history().redo();
            try {
                var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(c.snapshotDocument(),bytes);
                var restored=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()).embeddedAssets().get("test.asset");
                assertEquals(asset.metadata(),restored.metadata());
            }catch(java.io.IOException e){throw new AssertionError(e);}
            c.workspace().navigateBack();
            var graph=c.workspace().currentGraph();var source=GLNodeId.random();var value=new ValueNode();
            var sink=graph.snapshot().connections().get(0).input();
            graph.edit("Prepare insertion",edit->{
                for(var edge:graph.snapshot().connections())edit.removeConnection(edge.id());
                edit.addNode(new GLGraphNode(source,value.registration().id(),value.defaults()));
                edit.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(source,"value-out"),sink));
            });
            var editor=c.workspace().editor();var node=GroupUnitsTest.group(c);node.setLocation(200,400);
            var connection=editor.getConnections().get(0);var from=connection.getOutput();var to=connection.getInput();
            float x=(from.getNode().getX()+from.getCenterXInNode()+to.getNode().getX()+to.getCenterXInNode())/2-node.getWidth()/2;
            float y=(from.getNode().getY()+from.getCenterYInNode()+to.getNode().getY()+to.getCenterYInNode())/2-node.getHeight()/2;
            editor.setSelectedNodes(List.of(node));var move=c.actions().beginMoveSelection(200,400);move.moveTo(x,y);
            assertNotNull(editor.getInsertionCandidate());move.confirm();
            assertEquals(2,graph.snapshot().connections().size());
            assertTrue(graph.snapshot().connections().stream().anyMatch(edge->edge.input().equals(new GLNodeEndpoint(GroupUnitsTest.GROUP,"Pitch"))));
            c.history().undo();assertEquals(1,graph.snapshot().connections().size());assertEquals(x,node.getX());
            c.history().redo();assertTrue(c.actions().canExclude());c.actions().exclude();
            assertTrue(graph.snapshot().findNode(GroupUnitsTest.GROUP).isEmpty());
            assertEquals(new GLNodeEndpoint(source,"value-out"),graph.snapshot().connections().get(0).output());
        }});
    }

    @Test void groupUsesThemeWidthAfterThemePropagation() throws Exception {
        var controller=new java.util.concurrent.atomic.AtomicReference<WorkspaceController>();
        SwingUtilities.invokeAndWait(()->{
            var c=new WorkspaceController();controller.set(c);
            c.workspace().setGLTheme(GLThemeLoader.loadResource("/easyknack.gltheme"));
            c.openDocument("Width",GroupUnitsTest.document(false));
        });
        SwingUtilities.invokeAndWait(()->{try(var c=controller.get()){
            var node=GroupUnitsTest.group(c);
            assertEquals(node.getGLStyle().minimumWidth(),node.getPreferredGLSize().width(),.01);
            assertEquals(node.getGLStyle().minimumWidth(),node.getWidth(),.01);
            node.setContentHidden(true);
            assertEquals(node.getGLStyle().minimumWidth(),node.getPreferredGLSize().width(),.01);
        }});
    }
}
