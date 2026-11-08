package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class GroupForwardingTest {
    static final GLNodeId GROUP=GLNodeId.random(),SOURCE=GLNodeId.random(),SINK=GLNodeId.random();
    static GLNodeGraphDocument document(boolean forward,boolean connected){
        var presentation=new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of("minimum",GLNodeValue.of(0),"maximum",GLNodeValue.of(1),"default",GLNodeValue.of(.25),GroupUnits.BASE,GLNodeValue.of("factor")));
        var d=new GLCompositeNodeDescriptor("test.forward","test.forward.asset",1,List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.VALUE.valueTypeId(),false,1),null,presentation),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.VALUE.valueTypeId()),forward?GLCompositeForwarding.input("In"):null)));
        var asset=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraph().snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
        var root=new GLNodeGraph();var value=new ValueNode();var out=new OutputNode();
        root.edit("Test",e->{e.addNode(new GLGraphNode(GROUP,d.nodeTypeId(),Map.of()));e.addNode(new GLGraphNode(SOURCE,value.registration().id(),value.defaults()));e.addNode(new GLGraphNode(SINK,out.registration().id(),out.defaults()));
            if(connected)e.addConnection(wire(SOURCE,"value-out",GROUP,"In"));e.addConnection(wire(GROUP,"Out",SINK,"audio-in"));});
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,root.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
    }
    static GLGraphConnection wire(GLNodeId a,String ap,GLNodeId b,String bp){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(a,ap),new GLNodeEndpoint(b,bp));}
    @Test void directBoundaryWireSurvivesNavigationUndoAndFileRoundTrip()throws Exception{
        var holder=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();c.openDocument("Forward",document(false,true));assertTrue(c.workspace().openComposite("test.forward.asset"));
            var ed=c.workspace().editor();var in=ed.getNodes().stream().map(n->n.findPort("In")).filter(Objects::nonNull).findFirst().orElseThrow();var out=ed.getNodes().stream().map(n->n.findPort("Out")).filter(Objects::nonNull).findFirst().orElseThrow();
            assertTrue(ed.canConnect(in,out));ed.addConnection(new GLNodeConnection(in,out));
            assertTrue(c.workspace().navigateBack());assertTrue(c.workspace().openComposite("test.forward.asset"));
            assertEquals(1,c.workspace().editor().getConnections().size());});
        SwingUtilities.invokeAndWait(()->{try(var c=holder[0];var again=new WorkspaceController()){
            var saved=c.snapshotDocument();assertTrue(GLCompositeForwarding.isInput(saved.embeddedAssets().get("test.forward.asset").compositeDescriptor().portMappings().get(1).internalEndpoint()));
            c.history().undo();assertEquals(0,c.workspace().editor().getConnections().size());c.history().redo();assertEquals(1,c.workspace().editor().getConnections().size());
            assertTrue(c.workspace().navigateBack());assertFalse(c.audioGraph().contains(GLCompositeForwarding.TYPE));assertTrue(c.workspace().openComposite("test.forward.asset"));assertEquals(1,c.workspace().editor().getConnections().size());
            var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(c.snapshotDocument(),bytes);
            again.openDocument("Reopened",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));assertTrue(again.workspace().openComposite("test.forward.asset"));assertEquals(1,again.workspace().editor().getConnections().size());
        }catch(java.io.IOException e){throw new AssertionError(e);}});
    }
    @Test void coreForwardingHasNoExecutionNodeAndSupportsChains(){
        var doc=document(true,true);var registry=new GLCompositeGraphRegistry();doc.embeddedAssets().values().forEach(registry::registerAsset);
        var flat=GLCompositeGraphFlattener.flatten(doc.root(),registry,GLCompositeExpansionLimits.DEFAULT);assertTrue(flat.isValid(),flat.diagnostics().toString());
        assertEquals(2,flat.graph().orElseThrow().nodes().size());assertEquals(1,flat.graph().orElseThrow().connections().size());
        var connection=flat.graph().orElseThrow().connections().get(0);assertEquals(SOURCE,connection.output().nodeId());assertEquals(SINK,connection.input().nodeId());
        var graph=new GLNodeGraph(doc.root().graph());var second=GLNodeId.random();graph.edit("Chain",e->{e.removeConnection(connectionId(doc.root().graph(),GROUP,"Out"));e.addNode(new GLGraphNode(second,"test.forward",Map.of()));e.addConnection(wire(GROUP,"Out",second,"In"));e.addConnection(wire(second,"Out",SINK,"audio-in"));});
        var root=new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());
        var chained=GLCompositeGraphFlattener.flatten(root,registry,GLCompositeExpansionLimits.DEFAULT);assertTrue(chained.isValid(),chained.diagnostics().toString());assertEquals(1,chained.graph().orElseThrow().connections().size());assertEquals(2,chained.graph().orElseThrow().nodes().size());
    }
    static GLNodeConnectionId connectionId(GLNodeGraphSnapshot graph,GLNodeId from,String port){return graph.connections().stream().filter(c->c.output().equals(new GLNodeEndpoint(from,port))).findFirst().orElseThrow().id();}
    @Test void disconnectedGroupInputKeepsItsDefaultAndNoAnchorReachesAudio()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){c.openDocument("Default",document(true,false));String audio=c.audioGraph();assertFalse(audio.contains(GLCompositeForwarding.TYPE));assertTrue(audio.contains("0.25"),audio);}});
    }
    @Test void forwardedOutputsRetainTheInputUnit() {
        var doc=document(true,false);var original=doc.embeddedAssets().get("test.forward.asset");var d=original.compositeDescriptor();
        var ports=new ArrayList<>(d.portMappings());var input=ports.get(0);
        ports.set(0,new GLCompositePortMapping(input.publicPort(),null,GroupUnitsTest.slider()));
        var descriptor=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports);
        var asset=new GLNodeGraphAsset(original.id(),1,original.kind(),original.graph(),original.viewState(),Map.of(),descriptor);
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
        assertEquals(UnitFormat.HZ,GroupUnits.target(registry,descriptor,ports.get(1)));
        var prepared=GroupCompiler.prepare(doc.root(),registry);
        var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),GLCompositeExpansionLimits.DEFAULT);
        assertTrue(flat.isValid(),flat.diagnostics().toString());
        assertTrue(flat.graph().orElseThrow().nodes().stream().anyMatch(n->GLNodeValue.of(440.0).equals(n.properties().get("value"))));
    }
    @Test void forwardedOutputInheritsAnExplicitAudioInputRate() {
        var original=document(true,true).embeddedAssets().get("test.forward.asset");var d=original.compositeDescriptor();
        var ports=new ArrayList<>(d.portMappings());var input=ports.get(0);var options=new HashMap<>(input.presentation().editorOptions());
        options.put(GroupInputSettings.RATE,GLNodeValue.of("audio"));
        ports.set(0,new GLCompositePortMapping(input.publicPort(),null,new GLCompositePortPresentation(input.presentation().inputEditorId(),true,options)));
        var descriptor=new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports);
        var asset=new GLNodeGraphAsset(original.id(),1,original.kind(),original.graph(),original.viewState(),Map.of(),descriptor);
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
        assertEquals(ProcessingRate.AUDIO,new GroupPortRates(registry,Map.of()).rate(descriptor,ports.get(1)));
    }
}
