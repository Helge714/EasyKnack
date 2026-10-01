package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLTabbedPane;

class AnalysisControlsTest {
    @Test void analyzerControlsAndTriggerVisibility(){
        var scope=new OscilloscopeNode();
        assertTrue(scope.layout(new NodeState(scope.defaults())).ports().stream().noneMatch(p->p.direction()==GLPortDirection.OUTPUT));
        for(BasicNode node:java.util.List.of(scope,new AutocorrelationNode())){
            var values=new java.util.HashMap<>(node.defaults());values.put("channel",GLNodeValue.of("right"));
            assertTrue(node.encode(new NodeState(values),p->-1).endsWith("right"));
        }
        var control=new AnalysisEditor(false).create("Scope",v->{});
        var plot=(AnalysisEditor.Plot)control.component();assertTrue(plot.isTriggersVisible());
        control.update().accept(GLNodeValue.of("hide-triggers"));assertFalse(plot.isTriggersVisible());
        control.update().accept(GLNodeValue.of(""));assertTrue(plot.isTriggersVisible());
    }
    @Test void navigationAndGroupSettingsExistOnlyInsideGroups()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Navigation",GroupUnitsTest.document(false));var w=c.workspace();w.setBounds(0,0,800,600);
            var tabs=w.getGLChildren().stream().filter(GLTabbedPane.class::isInstance).map(GLTabbedPane.class::cast).findFirst().orElseThrow();
            assertEquals(1,tabs.getTabCount());assertEquals(0,w.editor().getY());
            assertTrue(w.openComposite("test.asset"));assertEquals(4,tabs.getTabCount());
            assertEquals(GLTabbedPane.Placement.LEFT,tabs.getTabPlacement());assertEquals("Editors",tabs.getTitleAt(1));assertEquals("Group-General",tabs.getTitleAt(2));assertEquals("Group-IO",tabs.getTitleAt(3));assertTrue(w.editor().getY()>0);
            w.navigateBack();assertEquals(1,tabs.getTabCount());assertEquals(0,w.editor().getY());
        }});
    }
    @Test void oldScopeOutputBecomesAParallelAnalysisBranch(){
        var graph=new GLNodeGraph();var source=GLNodeId.random();var scope=GLNodeId.random();var sink=GLNodeId.random();
        graph.edit("Legacy scope",e->{
            e.addNode(new GLGraphNode(source,new OscillatorNode().registration().id(),new OscillatorNode().defaults()));
            e.addNode(new GLGraphNode(scope,new OscilloscopeNode().registration().id(),new OscilloscopeNode().defaults()));
            e.addNode(new GLGraphNode(sink,new OutputNode().registration().id(),new OutputNode().defaults()));
            e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(source,"audio-out"),new GLNodeEndpoint(scope,"audio-in")));
            e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(scope,"audio-out"),new GLNodeEndpoint(sink,"audio-in")));
        });
        var root=new com.vingame.glswing.node.document.GLNodeGraphAsset("root",1,com.vingame.glswing.node.document.GLNodeGraphAssetKind.PROJECT,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty());
        var migrated=NodeDocuments.prepare(new com.vingame.glswing.node.document.GLNodeGraphDocument(root,java.util.Map.of(),java.util.List.of()));
        assertEquals(2,migrated.root().graph().connections().size());
        assertTrue(migrated.root().graph().connections().stream().allMatch(w->w.output().nodeId().equals(source)));
    }
}
