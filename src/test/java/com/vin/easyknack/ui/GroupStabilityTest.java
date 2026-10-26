package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GroupStabilityTest {
    @Test void copiedChoicesWithLegacyAliasedIdsControlOnlyTheirOwnTargets(){
        var graph=new GLNodeGraph();var fft=new SpectrumNode();var a=GLNodeId.random();var b=GLNodeId.random();
        graph.edit("FFTs",e->{e.addNode(new GLGraphNode(a,fft.registration().id(),fft.defaults()));e.addNode(new GLGraphNode(b,fft.registration().id(),fft.defaults()));});
        var d=new GLCompositeNodeDescriptor("test.choices","test.choices.asset",1,List.of());
        var asset=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
        var groups=new GLCompositeGraphRegistry();groups.registerAsset(asset);var ports=new ArrayList<GLCompositePortMapping>();
        var row=fft.layout(new NodeState(fft.defaults())).rows().stream().filter(r->r.id().equals("size")).findFirst().orElseThrow();
        var template=EditorTemplates.inherit("Window",(Editor.ComboBox<?>)row.editor());
        for(var id:List.of(a,b)){
            var options=Map.of("template",template.value(),"autoBind",GLNodeValue.of(true),GroupInputs.CHOICE,GLNodeValue.of("legacy-shared-choice"));
            ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input(id.equals(a)?"First FFT":"Second FFT","value",false,1),new GLNodeEndpoint(id,"size"),new GLCompositePortPresentation("easyknack.choice",false,options)));
        }
        asset=GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports));
        asset=GroupInputs.materialize(asset,groups);var normalized=asset.compositeDescriptor().portMappings();
        String first=GroupInputs.property(normalized.get(0)),second=GroupInputs.property(normalized.get(1));assertNotEquals(first,second);
        assertEquals(asset,GroupInputs.materialize(asset,groups)); // normalization is stable
        assertEquals(first,GroupInputs.allocateProperty(normalized.get(0))); // edits preserve identity
        groups.replaceAsset(asset);var rootGraph=new GLNodeGraph();rootGraph.edit("Group",e->e.addNode(new GLGraphNode(GLNodeId.random(),d.nodeTypeId(),Map.of(first,GLNodeValue.of("512"),second,GLNodeValue.of("2048")))));
        var root=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,rootGraph.snapshot(),GLNodeEditorViewState.empty());
        var prepared=GroupCompiler.prepare(root,groups);var result=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),GLCompositeExpansionLimits.DEFAULT);
        assertTrue(result.isValid(),result.diagnostics().toString());
        assertEquals(List.of("2048","512"),result.graph().orElseThrow().nodes().stream().filter(n->n.typeId().equals(fft.registration().id())).map(n->n.properties().get("size").toJavaValue().toString()).sorted().toList());
    }
    @Test void groupBoundaryBindingsSurviveReprojectionAndQueuedSynchronization()throws Exception{
        var holder=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();c.openDocument("Boundary",GroupUnitsTest.document(true));c.workspace().openComposite("test.outer.asset");
            var graph=c.workspace().currentGraph();var id=graph.snapshot().nodes().get(0).id();var asset=c.snapshotDocument().embeddedAssets().get("test.outer.asset");
            var before=c.workspace().editor().getNodes().stream().filter(n->n.findPort("Pitch")!=null&&n.findPort("Out")!=null).findFirst().orElseThrow();
            graph.edit("Change nested unit",e->e.setProperty(id,GroupUnits.unitKey("Pitch"),GLNodeValue.of("hz")));
            var after=c.workspace().editor().getNodes().stream().filter(n->n.findPort("Pitch")!=null&&n.findPort("Out")!=null).findFirst().orElseThrow();assertNotSame(before,after);
            assertEquals(asset.compositeDescriptor().portMappings(),c.snapshotDocument().embeddedAssets().get("test.outer.asset").compositeDescriptor().portMappings());
        });
        SwingUtilities.invokeAndWait(()->{try(var c=holder[0]){assertTrue(c.workspace().editor().getConnections().size()>=2);assertTrue(c.snapshotDocument().embeddedAssets().get("test.outer.asset").compositeDescriptor().portMappings().stream().allMatch(GLCompositePortMapping::isBound));}});
    }
    @Test void boundaryRewireHasOneModelUndoEntryAndPreservesOtherWires()throws Exception{
        var holder=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();c.openDocument("Boundary undo",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");c.history().reset();
            var editor=c.workspace().editor();var before=List.copyOf(editor.getConnections());var edge=before.stream().filter(e->e.getOutput().getId().equals("Pitch")).findFirst().orElseThrow();
            var input=edge.getOutput();var phase=edge.getInput().getNode().findPort("phase");editor.removeConnection(edge);editor.addConnection(new GLNodeConnection(input,phase));
            try{var method=GLNodeEditor.class.getDeclaredMethod("recordConnectionChange",List.class,List.class);method.setAccessible(true);method.invoke(editor,before,List.copyOf(editor.getConnections()));}catch(ReflectiveOperationException e){throw new AssertionError(e);}
            assertFalse(c.history().canUndo(),"The queued interface transaction owns boundary-wire history");
        });
        SwingUtilities.invokeAndWait(()->{try(var c=holder[0]){
            assertTrue(c.history().canUndo());assertTrue(c.history().undo());assertFalse(c.history().canUndo(),"One gesture must produce one undo entry");
            assertEquals("frequency",c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().get(0).internalEndpoint().portId());
            assertEquals(2,c.workspace().editor().getConnections().size());assertTrue(c.history().redo());
            assertEquals("phase",c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().get(0).internalEndpoint().portId());assertEquals(2,c.workspace().editor().getConnections().size());
        }});
    }
}
