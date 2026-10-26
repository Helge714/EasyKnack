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

class ForwardingAndViewerStateTest {
    private final GLNodeId group=GLNodeId.random(),viewer=GLNodeId.random();
    private GLNodeGraphDocument document(){
        var presentation=new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of("minimum",GLNodeValue.of(0.),"maximum",GLNodeValue.of(1.),"default",GLNodeValue.of(.25),GroupUnits.BASE,GLNodeValue.of("factor")));
        var descriptor=new GLCompositeNodeDescriptor("test.direct","test.direct.asset",1,List.of(
            new GLCompositePortMapping(GLNodePortDescriptor.input("In",SignalCategory.VALUE.valueTypeId(),false,1),null,presentation),
            new GLCompositePortMapping(GLNodePortDescriptor.output("Out",SignalCategory.VALUE.valueTypeId()),GLCompositeForwarding.input("In"))));
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,new GLNodeGraph().snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var graph=new GLNodeGraph();var definition=new ValueViewerNode();graph.edit("Nodes",e->{e.addNode(new GLGraphNode(group,descriptor.nodeTypeId(),Map.of()));e.addNode(new GLGraphNode(viewer,definition.registration().id(),definition.defaults()));e.addConnection(new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(group,"Out"),new GLNodeEndpoint(viewer,"value-0")));});
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
    }
    private void forwarded(WorkspaceController c){assertEquals(GLCompositeForwarding.input("In"),c.snapshotDocument().embeddedAssets().get("test.direct.asset").compositeDescriptor().portMappings().get(1).internalEndpoint());}
    @Test void outerParameterEditsPreserveDirectForwardingAndUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Forward",document());forwarded(c);
            c.setNodeProperty(group,new PropertyId(GroupUnits.key("In")),GLNodeValue.of(.7));forwarded(c);
            c.history().undo();forwarded(c);c.history().redo();forwarded(c);
            c.setNodeProperty(viewer,ValueViewerNode.WINDOW,GLNodeValue.of(5.));forwarded(c);
            assertTrue(c.workspace().openComposite("test.direct.asset"));assertEquals(1,c.workspace().editor().getConnections().size());c.workspace().navigateBack();forwarded(c);
        }});
    }
    private BasicNodeView view(WorkspaceController c){return c.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(viewer)).map(BasicNodeView.class::cast).findFirst().orElseThrow();}
    @Test void viewerSizeSurvivesRepeatedGroupNavigation()throws Exception{
        verifyViewerSize(true, false);
    }
    @Test void defaultViewerSizeSurvivesRepeatedGroupNavigation()throws Exception{
        verifyViewerSize(false, false);
    }
    @Test void hiddenViewerSizeSurvivesRepeatedGroupNavigation()throws Exception{
        verifyViewerSize(true, true);
    }
    @Test void hiddenDefaultViewerSizeSurvivesRepeatedGroupNavigation()throws Exception{
        verifyViewerSize(false, true);
    }
    private void verifyViewerSize(boolean custom,boolean hidden)throws Exception{
        var holder=new WorkspaceController[1];var expected=new float[2];
        SwingUtilities.invokeAndWait(()->{holder[0]=new WorkspaceController();holder[0].workspace().setGLTheme(ApplicationTheme.current().theme());holder[0].openDocument("Viewer",document());});
        SwingUtilities.invokeAndWait(()->{var c=holder[0];if(hidden)c.setNodeProperty(viewer,new PropertyId(GLNodeState.HIDDEN),GLNodeValue.of(true));var v=view(c);if(custom)v.setSize(443,577);expected[0]=v.getWidth();expected[1]=v.getHeight();assertTrue(c.workspace().openComposite("test.direct.asset"));assertTrue(c.workspace().navigateBack());});
        for(int i=0;i<3;i++)SwingUtilities.invokeAndWait(()->{var c=holder[0];var v=view(c);assertEquals(expected[0],v.getWidth(),.01,"viewer width");assertEquals(expected[1],v.getHeight(),.01,"viewer height");assertTrue(c.workspace().openComposite("test.direct.asset"));assertTrue(c.workspace().navigateBack());});
        SwingUtilities.invokeAndWait(()->holder[0].close());
    }
}
