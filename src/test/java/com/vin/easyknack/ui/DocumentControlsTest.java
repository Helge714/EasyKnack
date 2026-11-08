package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class DocumentControlsTest {
    @Test void savingPrunesUnreachableGroupsButPreservesNestedDependenciesAndPolicy()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController();var reopened=new WorkspaceController()){
            c.openDocument("Settings",GroupUnitsTest.document(true));
            var full=c.snapshotDocument();assertEquals(2,DocumentSettings.forSave(full).embeddedAssets().size());
            c.workspace().currentGraph().edit("Remove group",e->e.removeNode(GroupUnitsTest.GROUP));
            var pruned=DocumentSettings.forSave(c.snapshotDocument());assertTrue(pruned.embeddedAssets().isEmpty());
            assertEquals(2,c.snapshotDocument().embeddedAssets().size(),"Saving must not destroy live assets or undo state");
            c.preserveUnusedGroups=true;
            var saved=DocumentSettings.forSave(c.snapshotDocument());assertEquals(2,saved.embeddedAssets().size());
            try {
                var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(saved,bytes);
                reopened.openDocument("Retained",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
            } catch(java.io.IOException failure){throw new AssertionError(failure);}
            assertTrue(reopened.preserveUnusedGroups);
            c.history().undo();assertEquals(2,DocumentSettings.forSave(c.snapshotDocument()).embeddedAssets().size());
        }});
    }
    @Test void fitCentersEvenVeryLargeGraphsWithoutMovingNodes()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var editor=new GLNodeEditor();editor.setSize(800,600);editor.setZoomRange(.25f,2.5f);
            var a=new GLNode("A");a.setSize(200,100);editor.addNode(a,-10000,-5000);
            var b=new GLNode("B");b.setSize(200,100);editor.addNode(b,10000,5000);
            var before=editor.getNodes().stream().map(GLNodeViewState::capture).toList();editor.fitAllNodes();
            assertTrue(editor.getZoom()<.25);
            for(var n:editor.getNodes()){
                double x=400+editor.getPanX()+editor.getZoom()*(n.getX()-400);
                double y=300+editor.getPanY()+editor.getZoom()*(n.getY()-300);
                assertTrue(x>=0&&x+n.getWidth()*editor.getZoom()<=800.01);
                assertTrue(y>=0&&y+n.getHeight()*editor.getZoom()<=600.01);
            }
            assertEquals(before,editor.getNodes().stream().map(GLNodeViewState::capture).toList());
        });
    }
    @Test void mutedDeviceInputStillSuppliesAnExplicitZero(){
        var graph=new GLNodeGraph();var source=add(graph,new InputNode());var target=add(graph,new MathNode());
        connect(graph,source,"audio-out",target,"a");graph.edit("Mute",e->e.setProperty(source,GLNodeState.MUTED,GLNodeValue.of(true)));
        var types=new GLNodeTypeRegistry();new NodeRegistry(types,new GLNodeCatalog());
        assertTrue(MutedConnections.diagnostics(graph.snapshot(),types).isEmpty());
    }
    @Test void missingMuteSourceShowsEditorAndRestoresWhenConnected()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var graph=new GLNodeGraph();var a=add(graph,new MathNode());var b=add(graph,new MathNode());var out=add(graph,new MathNode());
            connect(graph,a,"audio-out",b,"a");connect(graph,b,"audio-out",out,"a");
            graph.edit("Mute chain",e->{e.setProperty(a,GLNodeState.MUTED,GLNodeValue.of(true));e.setProperty(b,GLNodeState.MUTED,GLNodeValue.of(true));});
            c.openDocument("Muted",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty())));
            assertEquals(2,c.workspace().editor().getConnections().stream().filter(w->!w.isActive()).count());
            assertTrue(c.workspace().editor().getConnections().stream().noneMatch(w->w.getInput().isContentHiddenByConnection()));
            c.workspace().currentGraph().edit("Enable source",e->e.setProperty(a,GLNodeState.MUTED,GLNodeValue.of(false)));
            assertTrue(c.workspace().editor().getConnections().stream().allMatch(w->w.isActive()));
            assertTrue(c.workspace().editor().getConnections().stream().allMatch(w->w.getInput().isContentHiddenByConnection()));
            assertDoesNotThrow(c::audioGraph);
        }});
    }
}
