package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import static com.vin.easyknack.ui.AdvancedSourceExportTest.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;

class DelayGroupingTest {
    @Test void groupingAndUngroupingFeedbackPreservesAllConnections() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for(int selection=0;selection<3;selection++)try(var c=new WorkspaceController()) {
                var g=new GLNodeGraph();var math=add(g,new MathNode());var delay=add(g,new DelayNode());var out=add(g,new OutputNode());
                connect(g,math,"audio-out",delay,"audio-in");connect(g,delay,"audio-out",math,"a");connect(g,delay,"audio-out",out,"audio-in");
                c.openDocument("Delay feedback",new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,g.snapshot(),GLNodeEditorViewState.empty())));
                var chosen=selection==0?Set.of(math,delay):selection==1?Set.of(delay):Set.of(math);
                c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView v&&chosen.contains(v.nodeId())).toList());
                c.actions().extractComposite();
                var grouped=c.snapshotDocument();
                int internal=grouped.embeddedAssets().values().stream().mapToInt(a->a.graph().connections().size()).sum();
                assertEquals(3,internal+grouped.root().graph().connections().size(),"Group selection "+selection);
                c.ungroupSelection();
                var unpacked=c.snapshotDocument().root().graph();
                long adapters=unpacked.nodes().stream().filter(n->n.typeId().startsWith("easyknack.math.unitconverter")).count();
                assertEquals(3+adapters,unpacked.connections().size(),"Ungroup selection "+selection);
                c.history().undo();c.history().redo();
                assertEquals(unpacked.connections(),c.snapshotDocument().root().graph().connections(),"Redo selection "+selection);
            }
        });
    }

    @Test void nestedFeedbackPreservesBoundaryOnlyConnectionsAndHistory() throws Exception {
        var g=new GLNodeGraph();var math=add(g,new MathNode());var delay=add(g,new DelayNode());
        connect(g,math,"audio-out",delay,"audio-in");connect(g,delay,"audio-out",math,"b");
        var descriptor=new GLCompositeNodeDescriptor("test.delay.group","test.delay.asset",1,List.of(
            input("In",SignalCategory.VALUE,math,"a"),output("Out",SignalCategory.AUDIO,delay,"audio-out")));
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,g.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var root=new GLNodeGraph();root.edit("Group",e->e.addNode(new GLGraphNode(GLNodeId.random(),descriptor.nodeTypeId())));
        var document=new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
        SwingUtilities.invokeAndWait(()->{
            for(int selection=0;selection<3;selection++)try(var c=new WorkspaceController()) {
                c.openDocument("Nested delay",document);c.workspace().openComposite(asset.id());
                var original=c.snapshotDocument().embeddedAssets().get(asset.id());
                var chosen=selection==0?Set.of(math,delay):selection==1?Set.of(delay):Set.of(math);
                c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView v&&chosen.contains(v.nodeId())).toList());
                c.actions().extractComposite();
                assertBound(c,asset.id());
                c.history().undo();assertEquals(original.compositeDescriptor(),c.snapshotDocument().embeddedAssets().get(asset.id()).compositeDescriptor());
                c.history().redo();assertBound(c,asset.id());
                c.workspace().editor().setSelectedNode(c.workspace().editor().getNodes().stream().filter(n->n instanceof GroupViewFactory.GroupView).findFirst().orElseThrow());
                c.ungroupSelection();assertBound(c,asset.id());
                assertFalse(c.workspace().editor().getNodes().stream().anyMatch(n->n instanceof GroupViewFactory.GroupView));
                c.history().undo();assertBound(c,asset.id());c.history().redo();assertBound(c,asset.id());
            }
        });
    }
    private static void assertBound(WorkspaceController c,String assetId) {
        var asset=c.snapshotDocument().embeddedAssets().get(assetId);
        for(var p:asset.compositeDescriptor().portMappings()) {
            assertEquals(1,p.internalEndpoints().size(),p.publicPort().id());
            assertTrue(asset.graph().findNode(p.internalEndpoint().nodeId()).isPresent(),p.toString());
        }
    }

    @Test void savedReverbRetainsConnectionsWhenPackingAndUnpacking() throws Exception {
        var filename=System.getProperty("easyknack.test.reverbDocument");
        org.junit.jupiter.api.Assumptions.assumeTrue(filename!=null,"Optional local document regression");
        var path=java.nio.file.Path.of(filename);
        GLNodeGraphDocument document;
        try(var files=new NodeDocuments()){document=files.read(path).toCompletableFuture().get();}
        var assets=NodeDocuments.assets(document);
        SwingUtilities.invokeAndWait(()->{
            for(var asset:assets)try(var c=new WorkspaceController()){
                c.openDocument("Reverb",document);
                if(!asset.id().equals(document.root().id()))c.workspace().openComposite(asset.id());
                var before=c.workspace().currentGraph().snapshot();
                if(before.nodes().isEmpty())continue;
                System.out.println("Packing "+asset.id()+" nodes="+before.nodes().size()+" wires="+before.connections().size());
                c.workspace().editor().setSelectedNodes(c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView).toList());
                c.actions().extractComposite();
                if(asset.compositeDescriptor()!=null){
                    var parent=c.snapshotDocument().embeddedAssets().get(asset.id());
                    assertEquals(asset.compositeDescriptor().portMappings().stream().mapToInt(p->p.internalEndpoints().size()).sum(),
                        parent.compositeDescriptor().portMappings().stream().mapToInt(p->p.internalEndpoints().size()).sum(),"Boundary endpoints after grouping "+asset.id());
                }
                c.ungroupSelection();
                assertTrue(c.workspace().currentGraph().snapshot().connections().size()>=before.connections().size(),asset.id());
            }
        });
    }
}
