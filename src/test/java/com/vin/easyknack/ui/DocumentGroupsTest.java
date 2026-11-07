package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class DocumentGroupsTest {
    private static GLNodeGraphAsset group(String id,List<String> children){
        var graph=new GLNodeGraph();graph.edit("Children",edit->{for(var child:children)edit.addNode(new GLGraphNode(GLNodeId.random(),"test."+child,Map.of()));});
        return new GLNodeGraphAsset(id,1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of("easyknack.title",GLNodeValue.of(id)),new GLCompositeNodeDescriptor("test."+id,id,1,List.of()));
    }
    private static GLNodeGraphDocument document(boolean used){
        var a=group("A",List.of("B"));var b=group("B",List.of());var c=group("C",List.of());var root=new GLNodeGraph();
        if(used)root.edit("Instances",edit->{for(int i=0;i<2;i++)edit.addNode(new GLGraphNode(GLNodeId.random(),"test.A",Map.of(GLNodeState.HIDDEN,GLNodeValue.of(true))));});
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of("A",a,"B",b,"C",c),List.of());
    }
    private static DocumentGroups.Group find(WorkspaceController controller,String id){return controller.documentGroups().stream().filter(g->g.id().equals(id)).findFirst().orElseThrow();}
    @Test void countsIncludeHiddenInstancesAndReferencesFromUnusedDefinitions(){
        var groups=DocumentGroups.inspect(document(true),List.of());
        assertEquals(2,groups.stream().filter(g->g.id().equals("A")).findFirst().orElseThrow().uses().size());
        var nested=groups.stream().filter(g->g.id().equals("B")).findFirst().orElseThrow();assertEquals("A",nested.uses().get(0).assetId());assertFalse(nested.deletable());
        assertFalse(DocumentGroups.inspect(document(false),List.of("C")).stream().filter(g->g.id().equals("C")).findFirst().orElseThrow().deletable());
    }
    @Test void deletingUnusedParentsUnlocksChildrenAndUndoRestoresTheDefinitions()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            controller.openDocument("Groups",document(false));
            assertThrows(IllegalArgumentException.class,()->controller.deleteUnusedGroup("B"));
            controller.deleteUnusedGroup("A");assertTrue(find(controller,"B").deletable());
            controller.deleteUnusedGroup("B");assertEquals(Set.of("C"),controller.snapshotDocument().embeddedAssets().keySet());
            assertTrue(controller.history().undo());assertTrue(controller.history().undo());assertFalse(find(controller,"B").deletable());
            assertTrue(controller.workspace().openComposite("A"));assertEquals(1,controller.workspace().editor().getNodes().stream().filter(GroupViewFactory.GroupView.class::isInstance).count());
            assertDoesNotThrow(controller::audioGraph);
        }});
    }
    @Test void openGroupsAreProtectedAndForwardNavigationCannotResurrectDeletedGroups()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            controller.openDocument("Groups",document(false));assertTrue(controller.workspace().openComposite("A"));assertTrue(controller.workspace().openComposite("C"));
            assertThrows(IllegalArgumentException.class,()->controller.deleteUnusedGroup("A"));
            assertThrows(IllegalArgumentException.class,()->controller.deleteUnusedGroup("C"));
            controller.workspace().navigateToDepth(0);assertTrue(controller.workspace().canNavigateForward());controller.deleteUnusedGroup("C");
            assertFalse(controller.workspace().canNavigateForward());assertFalse(controller.snapshotDocument().embeddedAssets().containsKey("C"));
            assertTrue(controller.history().undo());assertTrue(controller.workspace().openComposite("C"));
            assertFalse(controller.history().redo(),"The group's local history cannot redo a root-level deletion");
            controller.workspace().navigateBack();assertTrue(controller.history().redo());assertFalse(controller.snapshotDocument().embeddedAssets().containsKey("C"));
            var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(controller.snapshotDocument(),bytes);
            assertFalse(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()).embeddedAssets().containsKey("C"));
        }catch(java.io.IOException failure){throw new AssertionError(failure);}});
    }
    @Test void managerListsUsesAndRefreshesAfterDeletingAndUndoing()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            controller.openDocument("Groups",document(true));
            try(var panel=new GroupManagerPanel(controller)){
                assertEquals(3,panel.groups.getModel().getSize());panel.groups.setSelectedIndex(0);assertFalse(panel.delete.isEnabled());assertEquals(2,panel.uses.getModel().getSize());
                panel.groups.setSelectedIndex(2);assertTrue(panel.delete.isEnabled());panel.delete.doClick();assertEquals(2,panel.groups.getModel().getSize());
                assertTrue(controller.history().undo());assertEquals(3,panel.groups.getModel().getSize());
            }
        }});
    }
}
