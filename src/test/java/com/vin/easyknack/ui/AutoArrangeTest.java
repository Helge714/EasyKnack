package com.vin.easyknack.ui;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoArrangeTest {
    @Test void cyclesAndDisconnectedNodesRemainSeparateAndStable()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var editor=new com.vingame.glswing.node.GLNodeEditor();
            var nodes=new ArrayList<com.vingame.glswing.node.GLNode>();
            for(int i=0;i<4;i++){var n=new com.vingame.glswing.node.GLNode("N"+i);n.setSize(140+i*20,90+i*10);editor.addNode(n,30+i*7,50+i*9);nodes.add(n);}
            for(int i=0;i<3;i++)editor.addProjectedConnection(new com.vingame.glswing.node.GLNodeConnection(nodes.get(i).addOutput("out",new com.vingame.glswing.ui.GLLabel("Out")),nodes.get((i+1)%3).addInput("in",new com.vingame.glswing.ui.GLLabel("In"))));
            com.vingame.glswing.node.GLNodeAutoLayout.arrange(editor);
            var after=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            assertEquals(3,editor.getConnections().size());
            for(int i=0;i<4;i++)for(int j=i+1;j<4;j++){var a=after.get(i);var b=after.get(j);assertFalse(new java.awt.geom.Rectangle2D.Float(a.x(),a.y(),a.width(),a.height()).intersects(b.x(),b.y(),b.width(),b.height()));}
            assertEquals(editor.getVerticalNodeSpacing(),nodes.get(3).getY()-nodes.get(0).getY()-nodes.get(0).getHeight(),.01);
            com.vingame.glswing.node.GLNodeAutoLayout.arrange(editor);
            assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
        });
    }
    @Test void layoutUsesThemeGapsAndUndoPreservesGraphAndGroupBoundaries()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Layout",GroupUnitsTest.document(false));
            c.workspace().openComposite("test.asset");
            var editor=c.workspace().editor();var nodes=List.copyOf(editor.getNodes());
            var before=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            var graph=c.workspace().currentGraph().snapshot();
            var shortcut=new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(new java.awt.Canvas(),java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),java.awt.event.InputEvent.CTRL_DOWN_MASK|java.awt.event.InputEvent.SHIFT_DOWN_MASK,java.awt.event.KeyEvent.VK_L,'L'));
            c.workspace().keyPressed(shortcut);
            assertTrue(shortcut.isConsumed());
            var after=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            assertNotEquals(before,after);assertEquals(graph,c.workspace().currentGraph().snapshot());
            for(var edge:editor.getConnections())assertTrue(edge.getInput().getNode().getX()>=edge.getOutput().getNode().getX()+edge.getOutput().getNode().getWidth()+editor.getHorizontalNodeSpacing()-.01);
            c.history().undo();assertEquals(before,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            c.history().redo();assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            c.workspace().arrangeAll();assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
        }});
    }
}
