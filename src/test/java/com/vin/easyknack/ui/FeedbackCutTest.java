package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.audio.FeedbackConnections;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class FeedbackCutTest {
    @Test void mutedDelayCutsOnlyItsReturnConnectionAndPreservesSideBranches()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var g=new GLNodeGraph();var a=add(g,new MathNode());var b=add(g,new MathNode());var delay=add(g,new DelayNode());var out=add(g,new OutputNode());
            connect(g,a,"audio-out",b,"a");connect(g,b,"audio-out",delay,"audio-in");connect(g,delay,"audio-out",a,"a");connect(g,delay,"audio-out",out,"audio-in");
            c.openDocument("Loop",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,g.snapshot(),GLNodeEditorViewState.empty())));
            c.workspace().currentGraph().edit("Mute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(true)));
            assertDoesNotThrow(c::audioGraph);
            var ignored=c.workspace().editor().getConnections().stream().filter(w->!w.isActive()).toList();assertEquals(1,ignored.size());
            var model=c.workspace().currentGraph().snapshot().findConnection(ignored.get(0).getModelId()).orElseThrow();
            assertEquals(delay,model.output().nodeId());assertEquals(a,model.input().nodeId());
            assertEquals(3,c.workspace().editor().getConnections().stream().filter(w->w.isActive()).count());
            assertFalse(ignored.get(0).getInput().isContentHiddenByConnection());
            c.workspace().currentGraph().edit("Unmute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(false)));
            assertTrue(c.workspace().editor().getConnections().stream().allMatch(w->w.isActive()));assertDoesNotThrow(c::audioGraph);
        }});
    }
    @Test void independentCyclesAreCutSeparatelyAndDeterministically(){
        var g=new GLNodeGraph();var a=add(g,new MathNode());var b=add(g,new MathNode());var c=add(g,new MathNode());var d=add(g,new MathNode());
        connect(g,a,"audio-out",b,"a");connect(g,b,"audio-out",a,"a");connect(g,c,"audio-out",d,"a");connect(g,d,"audio-out",c,"a");
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());var result=FeedbackConnections.resolve(g.snapshot(),types);
        assertEquals(2,result.ignored().size());assertEquals(2,result.graph().connections().size());
        assertEquals(result.ignored(),FeedbackConnections.resolve(g.snapshot(),types).ignored());
        assertTrue(GLNodeGraphValidator.validate(result.graph(),types,GLGraphCyclePolicy.DELAYED_FEEDBACK).isValid());
    }
    @Test void overlappingCyclesAreRecheckedAfterEachCut(){
        var g=new GLNodeGraph();var a=add(g,new MathNode());var b=add(g,new MathNode());var c=add(g,new MathNode());
        connect(g,a,"audio-out",b,"a");connect(g,b,"audio-out",a,"a");connect(g,b,"audio-out",c,"a");connect(g,c,"audio-out",b,"b");
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());var result=FeedbackConnections.resolve(g.snapshot(),types);
        assertEquals(2,result.ignored().size());assertTrue(GLNodeGraphValidator.validate(result.graph(),types,GLGraphCyclePolicy.DELAYED_FEEDBACK).isValid());
    }
    @Test void mutedDelayCutFollowsNestedGroupInput()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var inner=new GLNodeGraph();var a=add(inner,new MathNode());var b=add(inner,new MathNode());connect(inner,a,"audio-out",b,"a");
            var descriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.cut.inner","cut.inner",1,List.of(input("In",SignalCategory.AUDIO,a,"a"),output("Out",SignalCategory.AUDIO,b,"audio-out")));
            var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,inner.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
            var wrapper=new GLNodeGraph();var child=GLNodeId.random();wrapper.edit("Group",e->e.addNode(new GLGraphNode(child,descriptor.nodeTypeId())));
            var outerDescriptor=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor("test.cut.outer","cut.outer",1,List.of(input("In",SignalCategory.AUDIO,child,"In"),output("Out",SignalCategory.AUDIO,child,"Out")));
            var outer=new GLNodeGraphAsset(outerDescriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,wrapper.snapshot(),GLNodeEditorViewState.empty(),Map.of(),outerDescriptor);
            var root=new GLNodeGraph();var group=GLNodeId.random();root.edit("Group",e->e.addNode(new GLGraphNode(group,outerDescriptor.nodeTypeId())));
            var delay=add(root,new DelayNode());var out=add(root,new OutputNode());
            connect(root,group,"Out",delay,"audio-in");connect(root,delay,"audio-out",group,"In");connect(root,delay,"audio-out",out,"audio-in");
            c.openDocument("Nested cut",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset,outer.id(),outer),List.of()));
            c.workspace().currentGraph().edit("Mute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(true)));
            assertDoesNotThrow(c::audioGraph);
            var inactive=c.workspace().editor().getConnections().stream().filter(w->!w.isActive()).toList();assertEquals(1,inactive.size());
            var wire=c.workspace().currentGraph().snapshot().findConnection(inactive.get(0).getModelId()).orElseThrow();assertEquals(delay,wire.output().nodeId());assertEquals(group,wire.input().nodeId());
        }});
    }
    @Test void protectedFeedbackAndAcyclicBranchesStayUnchanged(){
        var g=new GLNodeGraph();var a=add(g,new MathNode());var delay=add(g,new DelayNode());var out=add(g,new OutputNode());
        connect(g,a,"audio-out",delay,"audio-in");connect(g,delay,"audio-out",a,"a");connect(g,delay,"audio-out",out,"audio-in");
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());var result=FeedbackConnections.resolve(g.snapshot(),types);
        assertTrue(result.ignored().isEmpty());assertEquals(g.snapshot().connections(),result.graph().connections());
    }
    @Test void groupingMutedFeedbackPreservesFallback()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            for(int selection=0;selection<3;selection++)try(var c=new WorkspaceController()){
                var g=new GLNodeGraph();var a=add(g,new MathNode());var b=add(g,new MathNode());var delay=add(g,new DelayNode());var out=add(g,new OutputNode());
                connect(g,a,"audio-out",b,"a");connect(g,b,"audio-out",delay,"audio-in");connect(g,delay,"audio-out",a,"a");connect(g,delay,"audio-out",out,"audio-in");
                c.openDocument("Group muted loop",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,g.snapshot(),GLNodeEditorViewState.empty())));
                c.workspace().currentGraph().edit("Mute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(true)));
                var selected=selection==0?Set.of(a,b,delay):selection==1?Set.of(a,b):Set.of(delay);
                c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(v->v instanceof BasicNodeView n&&selected.contains(n.nodeId())).toList());
                c.actions().extractComposite();
                assertDoesNotThrow(c::audioGraph,"Selection "+selection);
                assertTrue(c.workspace().editor().getNodes().stream().allMatch(n->n.getDiagnosticStatus()==com.vingame.glswing.node.GLNodeConnectionStatus.NORMAL),()->"Node diagnostics: "+c.workspace().editor().getNodes().stream().map(n->String.valueOf(n.getDiagnosticMessage())).toList());
                assertTrue(c.workspace().editor().getConnections().stream().noneMatch(w->w.getStatus()==com.vingame.glswing.node.GLNodeConnectionStatus.ERROR));
                var group=c.workspace().currentGraph().snapshot().nodes().stream().filter(n->n.typeId().startsWith("easyknack.composite.")).findFirst().orElseThrow();
                c.workspace().currentGraph().edit("Hide group",e->{e.setProperty(group.id(),GLNodeState.HIDDEN,GLNodeValue.of(true));e.setProperty(group.id(),GLNodeState.MUTED,GLNodeValue.of(false));});
                assertDoesNotThrow(c::audioGraph);
                var saved=c.snapshotDocument();c.openDocument("Reload",saved);assertDoesNotThrow(c::audioGraph);
            }
        });
    }
    @Test void exposedRequiredInputIsConnectedInsideGroup()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var g=new GLNodeGraph();var gain=add(g,new GainNode());var delay=add(g,new DelayNode());var out=add(g,new OutputNode());
            connect(g,gain,"audio-out",delay,"audio-in");connect(g,delay,"audio-out",gain,"audio-in");connect(g,delay,"audio-out",out,"audio-in");
            c.openDocument("Required input",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,g.snapshot(),GLNodeEditorViewState.empty())));
            c.workspace().currentGraph().edit("Mute",e->e.setProperty(delay,GLNodeState.MUTED,GLNodeValue.of(true)));
            c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(v->v instanceof BasicNodeView n&&n.nodeId().equals(gain)).toList());
            c.actions().extractComposite();c.actions().openSelectedComposite();
            assertTrue(c.workspace().editor().getNodes().stream().noneMatch(n->n.getDiagnosticStatus()==com.vingame.glswing.node.GLNodeConnectionStatus.ERROR),()->"Diagnostics: "+c.workspace().editor().getNodes().stream().map(n->String.valueOf(n.getDiagnosticMessage())).toList());
            assertDoesNotThrow(c::audioGraph);
        }});
    }
}
