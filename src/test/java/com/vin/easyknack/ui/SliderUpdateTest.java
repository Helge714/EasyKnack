package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;
class SliderUpdateTest {
    @Test void valueUpdatesOnlyProjectTheChangedNode() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var graph=new GLNodeGraph();var types=new GLNodeTypeRegistry();types.register(new GLNodeTypeDescriptor("test",List.of()));
            var first=GLNodeId.random();var second=GLNodeId.random();graph.edit("Nodes",e->{e.addNode(new GLGraphNode(first,"test",Map.of()));e.addNode(new GLGraphNode(second,"test",Map.of()));});
            Map<GLNodeId,Integer> updates=new HashMap<>();
            var factory=new GLNodeViewFactory(){public GLNode createNode(GLGraphNode n,GLNodeTypeDescriptor t){return new GLNode("Test");} public void updateNode(GLNode v,GLGraphNode n,GLNodeTypeDescriptor t){updates.merge(n.id(),1,Integer::sum);}};
            try(var binding=new GLNodeEditorBinding(graph,types,new GLNodeEditor(),factory,GLGraphCyclePolicy.ACYCLIC)) {
                updates.clear();var before=graph.snapshot();graph.replace("Value",new GLNodeGraphSnapshot(before.graphId(),before.revision(),before.nodes().stream().map(n->n.id().equals(first)?n.withProperty("value",GLNodeValue.of(1)):n).toList(),before.connections()));
                assertEquals(1,updates.get(first));assertNull(updates.get(second));binding.refresh();assertEquals(1,updates.get(second));
            }
        });
    }
    @Test void repeatedSliderChangesKeepTheControlAndSingleGestureUndo() throws Exception {
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var def=new ValueNode();var id=GLNodeId.random();var graph=c.workspace().currentGraph();
            graph.edit("Nodes",e->{for(int i=0;i<80;i++)e.addNode(new GLGraphNode(i==0?id:GLNodeId.random(),def.registration().id(),def.defaults()));});
            var view=c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(id)).findFirst().orElseThrow();
            var slider=(com.vingame.glswing.ui.GLSlider)view.getContents().stream().filter(com.vingame.glswing.ui.GLSlider.class::isInstance).findFirst().orElseThrow();
            long started=System.nanoTime();slider.mousePressed(mouse(slider,java.awt.event.MouseEvent.MOUSE_PRESSED));
            for(int i=1;i<=100;i++)slider.setValue(i);
            slider.mouseReleased(mouse(slider,java.awt.event.MouseEvent.MOUSE_RELEASED));
            System.out.printf("100 slider model updates with 80 extra nodes: %.1f ms%n",(System.nanoTime()-started)/1e6);
            assertTrue(c.workspace().editor().getNodes().contains(view));assertEquals(100.,new NodeState(graph.snapshot().findNode(id).orElseThrow().properties()).number(ValueNode.VALUE,-1));
            c.history().undo();assertEquals(0.,new NodeState(graph.snapshot().findNode(id).orElseThrow().properties()).number(ValueNode.VALUE,-1));
        }});
    }
    private static com.vingame.glswing.input.GLMouseEvent mouse(com.vingame.glswing.ui.GLSlider slider,int kind) {
        float x=slider.getWidth()/2,y=slider.getHeight()/2;
        return new com.vingame.glswing.input.GLMouseEvent(new java.awt.event.MouseEvent(new javax.swing.JPanel(),kind,0,0,(int)x,(int)y,1,false,java.awt.event.MouseEvent.BUTTON1),new com.vingame.glswing.input.GLPickResult(0,slider,x,y,x,y,1,0,0,1,0,0),0,0);
    }

}
