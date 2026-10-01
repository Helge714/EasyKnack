package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;

class BlockDelayTest {
    static GLNodeGraphDocument document(){
        var delay=new BlockDelayNode();var math=new MathNode();var out=new OutputNode();var a=GLNodeId.random();var b=GLNodeId.random();var c=GLNodeId.random();
        var values=new HashMap<>(math.defaults());values.put(MathNode.B.value(),GLNodeValue.of(.1));
        var graph=new GLNodeGraph();graph.edit("Feedback",e->{e.addNode(new GLGraphNode(a,delay.registration().id(),delay.defaults()));e.addNode(new GLGraphNode(b,math.registration().id(),values));e.addNode(new GLGraphNode(c,out.registration().id(),out.defaults()));
            e.addConnection(wire(a,"out",b,MathNode.A.value()));e.addConnection(wire(b,MathNode.OUT.value(),a,"in"));e.addConnection(wire(a,"out",c,OutputNode.IN.value()));});
        return new GLNodeGraphDocument(new GLNodeGraphAsset("feedback",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty()),Map.of(),List.of());
    }
    static GLGraphConnection wire(GLNodeId a,String out,GLNodeId b,String in){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,out),new GLNodeEndpoint(b,in));}
    @Test void delayedCycleIsValidAndZeroDelayCycleIsRejected(){
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());var graph=document().root().graph();
        assertTrue(GLNodeGraphValidator.validate(graph,types,GLGraphCyclePolicy.DELAYED_FEEDBACK).isValid());
        assertFalse(GLNodeGraphValidator.validate(graph,types,GLGraphCyclePolicy.ACYCLIC).isValid());
        var math=graph.nodes().get(1);var illegal=new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),List.of(math),List.of(wire(math.id(),MathNode.OUT.value(),math.id(),MathNode.A.value())));
        assertFalse(GLNodeGraphValidator.validate(illegal,types,GLGraphCyclePolicy.DELAYED_FEEDBACK).isValid());
    }
    @Test void feedbackRoundTripsAndEncodesThroughWorkspace()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Feedback",document());assertTrue(c.audioGraph().contains(" delay "));
            try{var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(c.snapshotDocument(),bytes);c.openDocument("Reloaded",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));}catch(Exception e){throw new AssertionError(e);}
            assertEquals(3,c.snapshotDocument().root().graph().connections().size());assertTrue(c.audioGraph().contains(" delay "));
            var workspace=c.workspace();workspace.editor().setSelectedNodes(workspace.editor().getNodes().subList(0,2));c.actions().extractComposite();
            assertTrue(c.audioGraph().contains(" delay "));var saved=c.snapshotDocument();
            var group=saved.root().graph().nodes().stream().filter(n->!n.typeId().equals(new OutputNode().registration().id())).findFirst().orElseThrow();
            assertTrue(GroupSourceExporter.create(saved,group).files().values().stream().anyMatch(text->text.contains("renderFeedbackGraph")));
            c.openDocument("Feedback group",saved);assertTrue(c.audioGraph().contains(" delay "));
        }});
    }
}
