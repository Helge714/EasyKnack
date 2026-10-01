package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;

class ContextInputTest {
    @Test void contextIsRegisteredAndEncodesThroughMappingIntoOscillator()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var context=new ContextInputNode();var map=new MapRangeNode();var oscillator=new OscillatorNode();var out=new OutputNode();
            List<BasicNode> definitions=List.of(context,map,oscillator,out);var ids=definitions.stream().map(n->GLNodeId.random()).toList();
            var graph=controller.workspace().currentGraph();
            graph.edit("Stereo context",e->{
                for(var old:graph.snapshot().nodes())e.removeNode(old.id());
                for(int i=0;i<definitions.size();i++)e.addNode(new GLGraphNode(ids.get(i),definitions.get(i).registration().id(),definitions.get(i).defaults()));
                e.setProperty(ids.get(1),MapRangeNode.RATE.value(),GLNodeValue.of("control"));
                e.setProperty(ids.get(1),MapRangeNode.FROM_MIN.value(),GLNodeValue.of(-1));
                e.setProperty(ids.get(1),MapRangeNode.TO_MIN.value(),GLNodeValue.of(220));
                e.setProperty(ids.get(1),MapRangeNode.TO_MAX.value(),GLNodeValue.of(440));
                connect(e,ids.get(0),ContextInputNode.CHANNEL,ids.get(1),MapRangeNode.IN);
                connect(e,ids.get(1),MapRangeNode.OUT,ids.get(2),OscillatorNode.FREQUENCY);
                connect(e,ids.get(2),OscillatorNode.OUT,ids.get(3),OutputNode.IN);
            });
            var rows=context.layout(new NodeState(context.defaults())).rows();
            assertEquals(3,rows.size());assertTrue(rows.stream().allMatch(r->r.rate()==ProcessingRate.CONTROL&&r.kind()==NodeBuilder.RowKind.OUTPUT));
            String encoded=controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ");assertTrue(encoded.contains(" context  @ c -"));assertTrue(encoded.contains("maprange 0 0.0 -1 -1.0"));
            var view=controller.workspace().editor().getNodes().stream().filter(n->n.getTitle().equals("Context Input")).findFirst().orElseThrow();
            assertEquals(com.vingame.glswing.node.GLNodePort.Shape.DIAMOND,view.findPort("channel").getShape());
            assertTrue(view.findPort("channel").getToolTipText().contains("left"));
        }});
    }
    private static void connect(com.vingame.glswing.node.model.GLNodeGraph.Editor e,GLNodeId from,PropertyId output,GLNodeId to,PropertyId input){
        e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(from,output.value()),new GLNodeEndpoint(to,input.value())));
    }
}
