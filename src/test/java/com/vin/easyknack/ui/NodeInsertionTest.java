package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class NodeInsertionTest {
    @Test void mathExclusionUsesPreferredAOrConnectedBFallback() throws Exception {
        SwingUtilities.invokeAndWait(()-> {
            for(boolean connectA:new boolean[]{true,false}) try(var controller=new WorkspaceController()) {
                var graph=controller.workspace().currentGraph();
                var output=graph.snapshot().nodes().stream().filter(n->NodeRegistry.require(n.typeId()) instanceof OutputNode).findFirst().orElseThrow();
                var osc=new OscillatorNode(); var math=new MathNode();
                var a=GLNodeId.random(); var b=GLNodeId.random(); var id=GLNodeId.random();
                graph.edit("Multi input math",edit->{
                    for(var edge:graph.snapshot().connections()) edit.removeConnection(edge.id());
                    edit.addNode(new GLGraphNode(a,osc.registration().id(),osc.defaults()));
                    edit.addNode(new GLGraphNode(b,osc.registration().id(),osc.defaults()));
                    edit.addNode(new GLGraphNode(id,math.registration().id(),math.defaults()));
                    edit.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(b,OscillatorNode.OUT.value()),new GLNodeEndpoint(id,MathNode.B.value())));
                    if(connectA) edit.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,OscillatorNode.OUT.value()),new GLNodeEndpoint(id,MathNode.A.value())));
                    edit.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(id,MathNode.OUT.value()),new GLNodeEndpoint(output.id(),OutputNode.IN.value())));
                });
                var before=graph.snapshot();
                var editor=controller.workspace().editor();
                editor.setSelectedNode(editor.getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow());
                assertTrue(controller.actions().canExclude()); controller.actions().exclude();
                assertTrue(graph.snapshot().findNode(id).isEmpty()); assertEquals(1,graph.snapshot().connections().size());
                assertEquals(connectA?a:b,graph.snapshot().connections().get(0).output().nodeId());
                controller.history().undo(); assertEquals(before.connections(),graph.snapshot().connections());
            }
        });
    }
    @Test void mathUsesExplicitPortsThroughWorkspacePlacementAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(()-> {
            try(var controller=new WorkspaceController()) {
                var editor=controller.workspace().editor();
                var graph=controller.workspace().currentGraph();
                var math=new MathNode(); var id=GLNodeId.random();
                graph.edit("Add math",edit->edit.addNode(new GLGraphNode(id,math.registration().id(),math.defaults())));
                var node=editor.getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                node.setLocation(200,400);
                var before=graph.snapshot();
                var connection=editor.getConnections().get(0);
                var from=connection.getOutput(); var to=connection.getInput();
                float x=(from.getNode().getX()+from.getCenterXInNode()+to.getNode().getX()+to.getCenterXInNode())/2-node.getWidth()/2;
                float y=(from.getNode().getY()+from.getCenterYInNode()+to.getNode().getY()+to.getCenterYInNode())/2-node.getHeight()/2;
                editor.setSelectedNodes(List.of(node));
                var move=controller.actions().beginMoveSelection(200,400);
                assertNotNull(move); move.moveTo(x,y);
                assertNotNull(editor.getInsertionCandidate());
                move.confirm();
                assertEquals(before.connections().size()+1,graph.snapshot().connections().size());
                assertTrue(graph.snapshot().connections().stream().anyMatch(c->c.input().equals(new GLNodeEndpoint(id,MathNode.A.value()))));
                assertTrue(graph.snapshot().connections().stream().anyMatch(c->c.output().equals(new GLNodeEndpoint(id,MathNode.OUT.value()))));
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("math "));
                controller.history().undo();
                assertEquals(before.connections(),graph.snapshot().connections());
                assertEquals(x,node.getX()); assertEquals(y,node.getY());
                controller.history().undo();
                assertEquals(200,node.getX()); assertEquals(400,node.getY());
                controller.history().redo(); controller.history().redo();
                assertEquals(before.connections().size()+1,graph.snapshot().connections().size());
                assertTrue(controller.actions().canExclude());
                var key=new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(new java.awt.Canvas(),
                        java.awt.event.KeyEvent.KEY_PRESSED,System.currentTimeMillis(),java.awt.event.InputEvent.CTRL_DOWN_MASK,
                        java.awt.event.KeyEvent.VK_DELETE,java.awt.event.KeyEvent.CHAR_UNDEFINED));
                editor.keyPressed(key); assertTrue(key.isConsumed());
                assertTrue(graph.snapshot().findNode(id).isEmpty());
                assertEquals(before.connections().size(),graph.snapshot().connections().size());
                controller.history().undo(); assertTrue(graph.snapshot().findNode(id).isPresent());
            }
        });
    }
}

