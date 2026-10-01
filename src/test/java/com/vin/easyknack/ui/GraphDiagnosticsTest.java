package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.audio.SampleFeedback;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class GraphDiagnosticsTest {
    static GLNodeGraphDocument unsupported(){
        var graph=new GLNodeGraph();var delay=add(graph,new DelayNode(),Map.of("time",GLNodeValue.of(1./48000)));
        var converter=add(graph,new RateConverterNode());var out=add(graph,new OutputNode());
        connect(graph,delay,"audio-out",converter,"value");connect(graph,converter,"out",delay,"audio-in");connect(graph,delay,"audio-out",out,"audio-in");
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()));
    }
    static BasicNodeView view(WorkspaceController c,String type){return c.workspace().editor().getNodes().stream().filter(BasicNodeView.class::isInstance).map(BasicNodeView.class::cast).filter(v->c.workspace().currentGraph().snapshot().findNode(v.nodeId()).orElseThrow().typeId().equals(type)).findFirst().orElseThrow();}
    @Test void unsupportedFeedbackMarksNodeAndChangingDelayClearsIt()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Invalid feedback",unsupported());var converter=view(c,new RateConverterNode().registration().id());
            assertEquals(GLNodeConnectionStatus.ERROR,converter.getDiagnosticStatus());assertTrue(converter.getDiagnosticMessage().contains("Sample feedback"));
            var delay=c.workspace().currentGraph().snapshot().nodes().stream().filter(n->n.typeId().equals(new DelayNode().registration().id())).findFirst().orElseThrow();
            c.workspace().currentGraph().edit("Lengthen delay",e->e.setProperty(delay.id(),"time",GLNodeValue.of(.02)));
            assertEquals(GLNodeConnectionStatus.NORMAL,view(c,new RateConverterNode().registration().id()).getDiagnosticStatus());
            c.workspace().currentGraph().edit("Shorten delay",e->e.setProperty(delay.id(),"time",GLNodeValue.of(1./48000)));
            assertEquals(GLNodeConnectionStatus.ERROR,view(c,new RateConverterNode().registration().id()).getDiagnosticStatus());
        }});
    }
    @Test void unprotectedCycleMarksOnlyItsMembersAndExplainsConnections()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=new GLNodeGraph();var a=add(graph,new MathNode());var b=add(graph,new MathNode());var out=add(graph,new OutputNode());
            connect(graph,a,"audio-out",b,"a");connect(graph,b,"audio-out",out,"audio-in");
            c.openDocument("Cycle",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty())));
            connect(c.workspace().currentGraph(),b,"audio-out",a,"a");
            assertEquals(0,c.workspace().editor().getNodes().stream().filter(n->n.getDiagnosticStatus()==GLNodeConnectionStatus.WARNING).count());
            var errors=c.workspace().editor().getConnections().stream().filter(w->w.getStatus()==GLNodeConnectionStatus.WARNING).toList();assertEquals(1,errors.size());
            assertDoesNotThrow(c::audioGraph);assertTrue(errors.stream().noneMatch(GLNodeConnection::isActive));
            assertTrue(errors.stream().allMatch(w->w.getDiagnosticMessage().contains("Delay")));
            var saved=c.snapshotDocument();c.openDocument("Reload ignored cycle",saved);
            assertEquals(3,c.workspace().currentGraph().snapshot().connections().size());assertDoesNotThrow(c::audioGraph);
            assertEquals(1,c.workspace().editor().getConnections().stream().filter(w->!w.isActive()).count());
            var remove=c.workspace().currentGraph().snapshot().connections().stream().filter(w->w.input().nodeId().equals(a)).findFirst().orElseThrow();
            c.workspace().currentGraph().edit("Break loop",e->e.removeConnection(remove.id()));
            assertTrue(c.workspace().editor().getNodes().stream().noneMatch(n->n.getDiagnosticStatus()==GLNodeConnectionStatus.ERROR));
            assertTrue(c.workspace().editor().getConnections().stream().allMatch(w->w.getDiagnosticMessage()==null));
        }});
    }
    @Test void delayTimeFromInsideLoopIdentifiesTheOffendingConnection(){
        var graph=new GLNodeGraph();var delay=add(graph,new DelayNode());var math=add(graph,new MathNode());
        connect(graph,delay,"audio-out",math,"a");connect(graph,math,"audio-out",delay,"audio-in");connect(graph,math,"audio-out",delay,"time");
        var problem=SampleFeedback.diagnostics(graph.snapshot()).stream().filter(d->d.code().equals("feedback.block-input")).findFirst().orElseThrow();
        assertEquals(Set.of(delay),problem.nodeIds());assertEquals(1,problem.connectionIds().size());assertEquals("time",graph.snapshot().findConnection(problem.connectionIds().iterator().next()).orElseThrow().input().portId());
    }
    @Test void nestedGroupErrorsFollowNavigation()throws Exception{
        var f=FeedbackExportTest.fixture(1,true);var doc=f.document();var asset=doc.embeddedAssets().get("test.feedback.asset");
        var original=asset.graph().nodes().stream().filter(n->n.typeId().equals(new MathNode().registration().id())).findFirst().orElseThrow();var definition=new RateConverterNode();
        var nodes=asset.graph().nodes().stream().map(n->n.id().equals(original.id())?new GLGraphNode(n.id(),definition.registration().id(),definition.defaults()):n).toList();
        var wires=asset.graph().connections().stream().map(w->new GLGraphConnection(w.id(),w.output().nodeId().equals(original.id())?new GLNodeEndpoint(original.id(),"out"):w.output(),w.input().nodeId().equals(original.id())?new GLNodeEndpoint(original.id(),"value"):w.input())).toList();
        var assets=new HashMap<>(doc.embeddedAssets());assets.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(asset.graph().graphId(),0,nodes,wires),asset.viewState(),asset.metadata(),asset.compositeDescriptor()));
        var invalid=new GLNodeGraphDocument(doc.root(),assets,List.of());var controller=new AtomicReference<WorkspaceController>();
        try{
            SwingUtilities.invokeAndWait(()->{var c=new WorkspaceController();controller.set(c);c.openDocument("Nested",invalid);assertEquals(GLNodeConnectionStatus.ERROR,GroupUnitsTest.group(c).getDiagnosticStatus());c.workspace().openComposite("test.wrapper.asset");});
            SwingUtilities.invokeAndWait(()->{var c=controller.get();assertEquals(GLNodeConnectionStatus.ERROR,GroupUnitsTest.group(c).getDiagnosticStatus());c.workspace().openComposite("test.feedback.asset");});
            SwingUtilities.invokeAndWait(()->assertEquals(GLNodeConnectionStatus.ERROR,view(controller.get(),definition.registration().id()).getDiagnosticStatus()));
        }finally{SwingUtilities.invokeAndWait(()->{if(controller.get()!=null)controller.get().close();});}
    }
}
