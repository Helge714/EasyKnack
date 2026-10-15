package com.vin.easyknack.ui;

import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vin.easyknack.nodes.*;

class UngroupTest {
    @Test void instanceValuesUnitsConnectionsAndDefinitionSurviveWithUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{for(boolean nested:List.of(false,true))try(var c=new WorkspaceController()){
            c.openDocument("Ungroup",GroupUnitsTest.document(nested));
            c.setNodeProperty(GroupUnitsTest.GROUP,new PropertyId(GroupUnits.key("Pitch")),GLNodeValue.of(880.));
            c.setNodeProperty(GroupUnitsTest.GROUP,new PropertyId(GroupUnits.unitKey("Pitch")),GLNodeValue.of("khz"));
            var before=c.snapshotDocument();var original=GroupUnitsTest.group(c);original.setLocation(400,300);
            c.workspace().editor().setSelectedNode(original);c.ungroupSelection();
            var after=c.snapshotDocument();assertTrue(after.root().graph().findNode(GroupUnitsTest.GROUP).isEmpty());
            assertEquals(before.embeddedAssets().keySet(),after.embeddedAssets().keySet());
            assertTrue(c.audioGraph().contains("khz"),c.audioGraph());
            assertTrue(c.audioGraph().contains("0.88"),c.audioGraph());
            if(nested)assertTrue(after.root().graph().nodes().stream().anyMatch(n->n.typeId().equals("test.group")));
            assertFalse(c.selectedNodeIds().isEmpty());
            c.history().undo();assertTrue(c.workspace().currentGraph().snapshot().findNode(GroupUnitsTest.GROUP).isPresent());
            assertEquals(400,GroupUnitsTest.group(c).getX());assertEquals(300,GroupUnitsTest.group(c).getY());
            c.history().redo();assertTrue(c.workspace().currentGraph().snapshot().findNode(GroupUnitsTest.GROUP).isEmpty());
            assertEquals(after.root().graph().nodes(),c.snapshotDocument().root().graph().nodes());
        }});
    }
    @Test void nestedUngroupRebindsContainingBoundaryAndCanUndoAfterNavigation()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Nested",GroupUnitsTest.document(true));c.workspace().openComposite("test.outer.asset");
            var before=c.snapshotDocument().embeddedAssets().get("test.outer.asset");
            c.workspace().editor().setSelectedNode(GroupUnitsTest.group(c));c.ungroupSelection();
            var after=c.snapshotDocument().embeddedAssets().get("test.outer.asset");
            for(var port:after.compositeDescriptor().portMappings())for(var e:port.internalEndpoints())assertTrue(after.graph().findNode(e.nodeId()).isPresent());
            assertTrue(after.graph().nodes().stream().anyMatch(n->n.typeId().equals(new OscillatorNode().registration().id())));
            c.workspace().navigateBack();assertDoesNotThrow(()->c.audioGraph());c.history().undo();
            assertEquals(before.compositeDescriptor(),c.snapshotDocument().embeddedAssets().get("test.outer.asset").compositeDescriptor());
        }});
    }
    @Test void coreExpansionRetainsFanoutAndOnlyUnpacksOneInstance(){
        var input=GLNodeId.random();var a=GLNodeId.random();var b=GLNodeId.random();var group=GLNodeId.random();var other=GLNodeId.random();
        var d=new GLCompositeNodeDescriptor("test.group","test.asset",1,List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.input("in","number",false,1),new GLNodeEndpoint(a,"in")).withEndpoints(List.of(new GLNodeEndpoint(a,"in"),new GLNodeEndpoint(b,"in")))));
        var asset=new GLNodeGraphAsset("test.asset",1,GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraphSnapshot(GLNodeGraphId.random(),0,List.of(new GLGraphNode(a,"test.child"),new GLGraphNode(b,"test.child")),List.of()),new GLNodeEditorViewState(Map.of(a,new GLNodeViewState(20,30),b,new GLNodeViewState(220,130))),Map.of(),d);
        var instance=new GLGraphNode(group,"test.group");
        var parent=new GLNodeGraphSnapshot(GLNodeGraphId.random(),0,List.of(new GLGraphNode(input,"test.source"),instance,new GLGraphNode(other,"test.group")),List.of(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(input,"out"),new GLNodeEndpoint(group,"in"))));
        var result=GLCompositeUngroup.expand(parent,new GLNodeEditorViewState(Map.of(group,new GLNodeViewState(400,300))),instance,asset);
        assertEquals(2,result.graph().connections().size());assertTrue(result.graph().findNode(other).isPresent());
        assertEquals(400,result.viewState().nodes().get(result.nodeIds().get(a)).x());assertEquals(600,result.viewState().nodes().get(result.nodeIds().get(b)).x());
    }
}
