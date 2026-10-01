package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.ui.*;
class GroupFanoutTest {
    @Test void secondBoundaryConnectionSurvivesEditorChangesUndoAndSave() throws Exception {
        var holder=new WorkspaceController[1];var second=GLNodeId.random();
        SwingUtilities.invokeAndWait(()->{
            var c=holder[0]=new WorkspaceController();var doc=GroupUnitsTest.document(false);var asset=doc.embeddedAssets().get("test.asset");
            var nodes=new ArrayList<>(asset.graph().nodes());var osc=new OscillatorNode();nodes.add(new GLGraphNode(second,osc.registration().id(),osc.defaults()));
            var updated=new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),new GLNodeGraphSnapshot(asset.graph().graphId(),0,nodes,asset.graph().connections()),asset.viewState(),asset.metadata(),asset.compositeDescriptor());
            c.openDocument("Fanout",new GLNodeGraphDocument(doc.root(),Map.of(asset.id(),updated),List.of()));c.workspace().openComposite(asset.id());
            var boundary=c.workspace().editor().getNodes().stream().filter(n->!(n instanceof BasicNodeView)&&n.findPort("Pitch")!=null).findFirst().orElseThrow();
            var target=c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(second)).findFirst().orElseThrow();
            c.workspace().editor().addConnection(new GLNodeConnection(boundary.findPort("Pitch"),target.findPort("frequency")));
        });
        try { SwingUtilities.invokeAndWait(()->{
            var c=holder[0];assertEquals(2,c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().get(0).internalEndpoints().size());
            var ui=c.workspace().interfaceEditor();GroupRenderingTest.descendants(ui).stream().filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(n->(GLList<?>)n).findFirst().orElseThrow().setSelectedIndex(0);
            GroupRenderingTest.descendants(ui).stream().filter(n->n instanceof GLNumberSpinner spinner&&spinner.getValue()==20000).map(GLNumberSpinner.class::cast).findFirst().orElseThrow().setValue(10000);
            assertEquals(2,ui.selectedPort().internalEndpoints().size());assertEquals(2,c.workspace().editor().getConnections().stream().filter(w->w.getOutput().getId().equals("Pitch")).count());
            c.history().undo();assertEquals(2,ui.selectedPort().internalEndpoints().size());c.history().redo();
            try {var bytes=new java.io.ByteArrayOutputStream();var codec=new KnackDocumentCodec();codec.write(c.snapshotDocument(),bytes);var saved=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
                assertEquals(2,saved.embeddedAssets().get("test.asset").compositeDescriptor().portMappings().get(0).internalEndpoints().size());
                var registry=new GLCompositeGraphRegistry();saved.embeddedAssets().values().forEach(registry::registerAsset);var prepared=GroupCompiler.prepare(saved.root(),registry);
                var flattened=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(flattened.isValid(),flattened.diagnostics().toString());
                assertEquals(2,flattened.graph().orElseThrow().connections().stream().filter(w->w.input().portId().equals("frequency")).count());
                // Exercise generic Core fanout too, without EasyKnack's synthetic adapters.
                var source=GLNodeId.random();var root=saved.root();var rootNodes=new ArrayList<>(root.graph().nodes());rootNodes.add(new GLGraphNode(source,new ValueNode().registration().id(),new ValueNode().defaults()));
                var wires=new ArrayList<>(root.graph().connections());wires.add(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(source,"value-out"),new GLNodeEndpoint(GroupUnitsTest.GROUP,"Pitch")));
                var connectedRoot=new GLNodeGraphAsset(root.id(),root.version(),root.kind(),new GLNodeGraphSnapshot(root.graph().graphId(),0,rootNodes,wires),root.viewState());
                var raw=GLCompositeGraphFlattener.flatten(connectedRoot,registry,new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(raw.isValid(),raw.diagnostics().toString());
                assertEquals(2,raw.graph().orElseThrow().connections().stream().filter(w->w.output().nodeId().equals(source)&&w.input().portId().equals("frequency")).count());
            }catch(java.io.IOException e){throw new AssertionError(e);}
        }); } finally {SwingUtilities.invokeAndWait(()->holder[0].close());}
    }
}
