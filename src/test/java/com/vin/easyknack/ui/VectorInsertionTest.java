package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
class VectorInsertionTest {
    @Test void vectorProcessorsInsertIntoVectorConnectionsAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(()-> {
            for(BasicNode processor:List.of(new VectorMathNode(),new VectorMappingNode()))
                try(var controller=new WorkspaceController()) {
                    var graph=controller.workspace().currentGraph();var source=new VectorNode();var target=new VectorSeparateNode();
                    var sourceId=GLNodeId.random();var targetId=GLNodeId.random();var insertedId=GLNodeId.random();
                    graph.edit("Vector connection",e->{
                        for(var n:graph.snapshot().nodes())e.removeNode(n.id());
                        e.addNode(new GLGraphNode(sourceId,source.registration().id(),source.defaults()));
                        e.addNode(new GLGraphNode(targetId,target.registration().id(),target.defaults()));
                        e.addNode(new GLGraphNode(insertedId,processor.registration().id(),processor.defaults()));
                        e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(sourceId,VectorNode.OUT.value()),new GLNodeEndpoint(targetId,VectorSeparateNode.IN.value())));
                    });
                    var before=graph.snapshot();var editor=controller.workspace().editor();
                    var node=editor.getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(insertedId::equals).isPresent()).findFirst().orElseThrow();
                    var edge=editor.getConnections().get(0);var handler=editor.getConnectionInsertionHandler();
                    assertTrue(handler.canInsert(node,edge));assertTrue(handler.insert(node,edge,200,100));
                    String input=processor instanceof VectorMathNode?VectorMathNode.A.value():VectorMappingNode.IN.value();
                    assertEquals(2,graph.snapshot().connections().size());
                    assertTrue(graph.snapshot().connections().stream().anyMatch(c->c.input().equals(new GLNodeEndpoint(insertedId,input))));
                    assertTrue(graph.snapshot().connections().stream().anyMatch(c->c.output().equals(new GLNodeEndpoint(insertedId,"vector"))));
                    controller.history().undo();assertEquals(before.connections(),graph.snapshot().connections());
                }
        });
    }
}
