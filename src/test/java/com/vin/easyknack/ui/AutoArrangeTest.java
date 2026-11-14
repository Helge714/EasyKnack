package com.vin.easyknack.ui;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoArrangeTest {
    @Test void workspaceArrangeRetainsRoughRowsAndColumnsAndCanBeUndone()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=new com.vingame.glswing.node.model.GLNodeGraph();var definition=new com.vin.easyknack.nodes.ValueNode();
            graph.edit("Fixture",e->{for(int i=0;i<3;i++)e.addNode(new com.vingame.glswing.node.model.GLGraphNode(com.vingame.glswing.node.model.GLNodeId.random(),definition.registration().id(),definition.defaults()));});
            c.openDocument("Rough layout",new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty())));
            var editor=c.workspace().editor();var nodes=editor.getNodes();
            nodes.get(0).setLocation(900,30);nodes.get(1).setLocation(30,800);nodes.get(2).setLocation(20,20);
            var before=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            c.workspace().arrangeAll();assertEquals(nodes.get(0).getY(),nodes.get(2).getY());assertTrue(nodes.get(2).getX()<nodes.get(0).getX());assertTrue(nodes.get(1).getY()>nodes.get(2).getY());
            var after=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            c.workspace().arrangeAll();assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            assertTrue(c.history().undo());assertEquals(before,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
        }});
    }
    @Test void roughlyDiagonalRealSignalChainsDoNotExpandIntoEmptyRows()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=new com.vingame.glswing.node.model.GLNodeGraph();var definition=new com.vin.easyknack.nodes.GainNode();
            var ids=java.util.stream.IntStream.range(0,24).mapToObj(i->com.vingame.glswing.node.model.GLNodeId.random()).toList();
            graph.edit("Fixture",e->{for(int i=0;i<ids.size();i++){
                e.addNode(new com.vingame.glswing.node.model.GLGraphNode(ids.get(i),definition.registration().id(),definition.defaults()));
                if(i>0)e.addConnection(new com.vingame.glswing.node.model.GLGraphConnection(com.vingame.glswing.node.model.GLNodeConnectionId.random(),new com.vingame.glswing.node.model.GLNodeEndpoint(ids.get(i-1),"audio-out"),new com.vingame.glswing.node.model.GLNodeEndpoint(ids.get(i),"audio-in")));
            }});
            c.openDocument("Compact chain",new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty())));
            var editor=c.workspace().editor();var nodes=editor.getNodes();for(int i=0;i<nodes.size();i++)nodes.get(i).setLocation(i*350,i*250);
            var before=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            c.workspace().arrangeAll();double height=nodes.stream().mapToDouble(n->n.getY()+n.getHeight()).max().orElseThrow()-nodes.stream().mapToDouble(n->n.getY()).min().orElseThrow();
            assertTrue(height<=2*nodes.stream().mapToDouble(n->n.getHeight()).max().orElseThrow());
            var after=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            c.workspace().arrangeAll();assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            assertTrue(c.history().undo());assertEquals(before,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
        }});
    }
    @Test void realNodeGeometryCameraAndGraphSurviveArrangementAndUndo()throws Exception{
        var ref=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{
            var c=ref[0]=new WorkspaceController();var graph=new com.vingame.glswing.node.model.GLNodeGraph();
            com.vin.easyknack.nodes.BasicNode[] definitions={new com.vin.easyknack.nodes.OscillatorNode(),new com.vin.easyknack.nodes.FilterNode(),new com.vin.easyknack.nodes.GainNode(),new com.vin.easyknack.nodes.OutputNode(),new com.vin.easyknack.nodes.ValueNode(),new com.vin.easyknack.nodes.ValueNode()};
            var ids=java.util.stream.IntStream.range(0,definitions.length).mapToObj(i->com.vingame.glswing.node.model.GLNodeId.random()).toList();
            graph.edit("Fixture",e->{for(int i=0;i<definitions.length;i++)e.addNode(new com.vingame.glswing.node.model.GLGraphNode(ids.get(i),definitions[i].registration().id(),definitions[i].defaults()));
                for(int i=0;i<3;i++)e.addConnection(new com.vingame.glswing.node.model.GLGraphConnection(com.vingame.glswing.node.model.GLNodeConnectionId.random(),new com.vingame.glswing.node.model.GLNodeEndpoint(ids.get(i),"audio-out"),new com.vingame.glswing.node.model.GLNodeEndpoint(ids.get(i+1),"audio-in")));
                e.addConnection(new com.vingame.glswing.node.model.GLGraphConnection(com.vingame.glswing.node.model.GLNodeConnectionId.random(),new com.vingame.glswing.node.model.GLNodeEndpoint(ids.get(4),"value-out"),new com.vingame.glswing.node.model.GLNodeEndpoint(ids.get(1),"cutoff")));
            });
            c.openDocument("Real layout",new com.vingame.glswing.node.document.GLNodeGraphDocument(new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty())));c.workspace().setBounds(0,0,1200,800);
        });
        SwingUtilities.invokeAndWait(()->{try(var c=ref[0]){
            var editor=c.workspace().editor();editor.setZoom(1.25f);editor.setPan(73,-42);int i=0;for(var node:editor.getNodes())node.setLocation(20+i++*17,40+i*23);
            var before=editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();var graph=c.workspace().currentGraph().snapshot();
            c.workspace().arrangeAll();var after=editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();assertNotEquals(before,after);assertEquals(graph,c.workspace().currentGraph().snapshot());
            for(i=0;i<before.size();i++){assertEquals(before.get(i).width(),after.get(i).width());assertEquals(before.get(i).height(),after.get(i).height());}
            assertEquals(73,editor.getPanX());assertEquals(-42,editor.getPanY());assertEquals(1.25,editor.getZoom());
            assertTrue(c.history().undo());assertEquals(before,editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());assertTrue(c.history().redo());assertEquals(after,editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            c.workspace().arrangeAll();assertEquals(after,editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            var saved=c.snapshotDocument();try(var reopened=new WorkspaceController()){reopened.openDocument("Restored layout",saved);reopened.workspace().setBounds(0,0,1200,800);assertEquals(saved.root().viewState().nodes(),reopened.snapshotDocument().root().viewState().nodes());}
            assertTrue(c.history().undo());assertEquals(before,editor.getNodes().stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList(),"Repeated Arrange must not add a second Undo entry");
        }});
    }
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
            double componentBottom=nodes.subList(0,3).stream().mapToDouble(n->n.getY()+n.getHeight()).max().orElseThrow();
            assertTrue(nodes.get(3).getY()>=componentBottom+editor.getVerticalNodeSpacing()-.01);
            com.vingame.glswing.node.GLNodeAutoLayout.arrange(editor);
            assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
        });
    }
    @Test void layoutUsesThemeGapsAndUndoPreservesGraphAndGroupBoundaries()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Layout",GroupUnitsTest.document(false));
            c.workspace().openComposite("test.asset");
            var editor=c.workspace().editor();var nodes=List.copyOf(editor.getNodes());
            // New groups already start arranged; deliberately disturb their geometry.
            nodes.get(0).setLocation(777,333);
            var before=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            var graph=c.workspace().currentGraph().snapshot();
            var shortcut=new com.vingame.glswing.input.GLKeyEvent(new java.awt.event.KeyEvent(new java.awt.Canvas(),java.awt.event.KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),java.awt.event.InputEvent.CTRL_DOWN_MASK|java.awt.event.InputEvent.SHIFT_DOWN_MASK,java.awt.event.KeyEvent.VK_L,'L'));
            c.workspace().keyPressed(shortcut);
            assertTrue(shortcut.isConsumed());
            var after=nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList();
            assertNotEquals(before,after);assertEquals(graph,c.workspace().currentGraph().snapshot());
            var input=nodes.stream().filter(n->n.getAutoLayoutRole()==com.vingame.glswing.node.GLNodeAutoLayout.Role.INPUT_BOUNDARY).findFirst().orElseThrow();
            var output=nodes.stream().filter(n->n.getAutoLayoutRole()==com.vingame.glswing.node.GLNodeAutoLayout.Role.OUTPUT_BOUNDARY).findFirst().orElseThrow();
            for(var n:nodes)if(n!=input&&n!=output){assertTrue(input.getX()+input.getWidth()+editor.getHorizontalNodeSpacing()<=n.getX()+.01);assertTrue(n.getX()+n.getWidth()+editor.getHorizontalNodeSpacing()<=output.getX()+.01);}
            for(var edge:editor.getConnections())assertTrue(edge.getInput().getNode().getX()>=edge.getOutput().getNode().getX()+edge.getOutput().getNode().getWidth()+editor.getHorizontalNodeSpacing()-.01);
            c.history().undo();assertEquals(before,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            c.history().redo();assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
            c.workspace().arrangeAll();assertEquals(after,nodes.stream().map(com.vingame.glswing.node.binding.GLNodeViewState::capture).toList());
        }});
    }
}
