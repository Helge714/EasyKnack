package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class TodoUiRegressionTest {
    private static Object field(Object owner,String name)throws Exception{var f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);}
    @Test void switchModeInsideGroupKeepsTheViewAndUndoAvailable()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var definition=new StepSwitchNode();var id=GLNodeId.random();var graph=new GLNodeGraph();graph.edit("Switch",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
            var descriptor=new GLCompositeNodeDescriptor("test.switch.group","test.switch.asset",1,List.of());
            var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
            var root=new GLNodeGraph();root.edit("Group",e->e.addNode(new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId(),Map.of())));
            c.openDocument("Switch group",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of()));c.workspace().openComposite(asset.id());
            c.workspace().editor().setSelectedNode(c.workspace().editor().getNodes().stream().filter(StepSwitchView.class::isInstance).findFirst().orElseThrow());
            c.workspace().propertyInspector().setSelectedProperty("mode",GLNodeValue.of("index"));
            assertEquals(1,c.workspace().editor().getNodes().stream().filter(StepSwitchView.class::isInstance).count());
            assertNull(c.workspace().editor().getNodes().stream().filter(StepSwitchView.class::isInstance).findFirst().orElseThrow().findPort("reset"));
            assertFalse(c.history().isInTransaction());assertTrue(c.history().undo());
            assertNotNull(c.workspace().editor().getNodes().stream().filter(StepSwitchView.class::isInstance).findFirst().orElseThrow().findPort("reset"));
            assertTrue(c.history().redo());
        }});
    }
    @Test void editedTemplateUpdatesExistingGroupDropdownAndUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var asset=GroupAuthoringTest.asset(new FilterNode(),"order");var old=GroupInputs.template(asset.compositeDescriptor().portMappings().get(0));
            c.openDocument("Template",GroupAuthoringTest.document(asset,GLNodeValue.of(old.initial())));c.editorTemplates().put(old);
            var entries=old.entries().stream().map(e->new EditorTemplates.Entry(e.id(),"Updated "+e.label(),e.value())).toList();
            var next=new EditorTemplates.Template(old.id(),old.label(),entries,old.initial());
            var settings=field(c.workspace().interfaceEditor(),"applicationSettings");var panel=field(settings,"templatesPanel");
            var edit=EditorTemplatesPanel.class.getDeclaredMethod("edit",Runnable.class);edit.setAccessible(true);edit.invoke(panel,(Runnable)()->c.editorTemplates().put(next));
            var group=GroupUnitsTest.group(c);assertTrue(group.getContents().stream().filter(GLComboBox.class::isInstance).map(GLComboBox.class::cast).flatMap(b->b.getItems().stream()).anyMatch(v->v.toString().startsWith("Updated")));
            assertTrue(c.history().undo());
            group=GroupUnitsTest.group(c);assertFalse(group.getContents().stream().filter(GLComboBox.class::isInstance).map(GLComboBox.class::cast).flatMap(b->b.getItems().stream()).anyMatch(v->v.toString().startsWith("Updated")));
            assertTrue(c.history().redo());
            group=GroupUnitsTest.group(c);assertTrue(group.getContents().stream().filter(GLComboBox.class::isInstance).map(GLComboBox.class::cast).flatMap(b->b.getItems().stream()).anyMatch(v->v.toString().startsWith("Updated")));
        }catch(Exception e){throw new AssertionError(e);}});
    }
    @Test void stepOrderMovesStablePortsAndSurvivesUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var definition=new StepSwitchNode();var id=GLNodeId.random();c.workspace().currentGraph().edit("Switch",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
            var source=new ValueNode();var sourceId=GLNodeId.random();var connection=new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(sourceId,"value-out"),new GLNodeEndpoint(id,"step-2"));
            c.workspace().currentGraph().edit("Source",e->{e.addNode(new GLGraphNode(sourceId,source.registration().id(),source.defaults()));e.addConnection(connection);});
            c.setNodeProperty(id,StepSwitchNode.ORDER,GLNodeValue.of(List.of(2,0,1,3)));
            var node=c.workspace().currentGraph().snapshot().findNode(id).orElseThrow();
            assertEquals(List.of("step-2","step-0","step-1","step-3"),definition.layout(new NodeState(node.properties())).rows().stream().filter(r->r.id().matches("step-[0-9]+")&&r.kind()==NodeBuilder.RowKind.INPUT).map(NodeBuilder.Row::id).toList());
            assertTrue(c.history().undo());assertEquals(List.of(0,1,2,3),StepSwitchNode.order(new NodeState(c.workspace().currentGraph().snapshot().findNode(id).orElseThrow().properties())));
            assertTrue(c.workspace().currentGraph().snapshot().connections().contains(connection));
        }});
    }
}
