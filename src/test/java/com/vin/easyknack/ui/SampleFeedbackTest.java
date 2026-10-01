package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class SampleFeedbackTest {
    static GLNodeGraphDocument document(){
        var doc=BlockDelayTest.document();var root=doc.root();var sample=new SampleDelayNode();
        var nodes=root.graph().nodes().stream().map(n->n.typeId().equals("easyknack.audio.blockdelay")?new GLGraphNode(n.id(),sample.registration().id(),sample.defaults()):n).toList();
        return new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(),root.version(),root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),root.graph().revision(),nodes,root.graph().connections()),root.viewState()),doc.embeddedAssets(),doc.externalAssets());
    }
    @Test void sampleFeedbackRoundTripsAndUsesOnlySupportedKernels()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Sample feedback",document());assertTrue(c.audioGraph().contains(" delay "));
            var saved=c.snapshotDocument();c.openDocument("Reloaded",saved);assertTrue(c.audioGraph().contains(" delay "));
            var node=c.workspace().currentGraph().snapshot().nodes().get(1);var oscillator=new RateConverterNode();
            var replacement=new GLGraphNode(node.id(),oscillator.registration().id(),oscillator.defaults());
            var graph=c.workspace().currentGraph().snapshot();var nodes=graph.nodes().stream().map(n->n.id().equals(node.id())?replacement:n).toList();
            var wires=graph.connections().stream().map(w->new GLGraphConnection(w.id(),w.output().nodeId().equals(node.id())?new GLNodeEndpoint(node.id(),RateConverterNode.OUT.value()):w.output(),w.input().nodeId().equals(node.id())?new GLNodeEndpoint(node.id(),RateConverterNode.IN.value()):w.input())).toList();
            var invalid=new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),nodes,wires);
            assertTrue(assertThrows(IllegalArgumentException.class,()->SampleFeedback.validate(invalid)).getMessage().contains("Rate Converter"));
        }});
    }
}
