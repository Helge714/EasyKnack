package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class GroupInterfaceValidityTest {
    private static GLNodeGraphAsset live(WorkspaceController controller,String id){
        try{var field=WorkspaceController.class.getDeclaredField("composites");field.setAccessible(true);return ((GLCompositeGraphRegistry)field.get(controller)).findAsset(id).orElseThrow();}
        catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static GLNodeGraphAsset graph(GLNodeGraphAsset asset,GLNodeGraph graph){return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),graph.snapshot(),asset.viewState(),asset.metadata(),asset.compositeDescriptor());}
    @Test void deletingOneLinkedNodeKeepsTheOtherAndDeletingBothReleasesTheEditor(){
        var asset=GroupAuthoringTest.asset(new FilterNode(),"order");var original=asset;
        var targets=GroupEditorCopies.targets(asset.compositeDescriptor().portMappings().get(0));
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);var graph=new GLNodeGraph(asset.graph());
        graph.edit("Delete first",e->e.removeNode(targets.get(0).nodeId()));
        asset=GroupInputs.materialize(graph(asset,graph),registry);
        var port=asset.compositeDescriptor().portMappings().get(0);
        assertEquals(List.of(targets.get(1)),GroupEditorCopies.targets(port));
        assertTrue(asset.compositeDescriptor().propertyMappings().stream().allMatch(m->m.internalNodeId().equals(targets.get(1).nodeId())));
        assertEquals(asset,GroupInputs.materialize(asset,registry));
        graph.edit("Delete second",e->e.removeNode(targets.get(1).nodeId()));
        asset=GroupInputs.materialize(graph(asset,graph),registry);port=asset.compositeDescriptor().portMappings().get(0);
        assertTrue(GroupEditorCopies.targets(port).isEmpty());assertFalse(port.presentation().editorOptions().containsKey("copySource"));
        assertTrue(asset.compositeDescriptor().propertyMappings().isEmpty());assertEquals(1,asset.compositeDescriptor().portMappings().size());
        assertEquals(2,GroupEditorCopies.targets(original.compositeDescriptor().portMappings().get(0)).size());
        var replacement=GLNodeId.random();var filter=new FilterNode();graph.edit("New target",e->e.addNode(new GLGraphNode(replacement,filter.registration().id(),filter.defaults())));
        var linked=GroupEditorCopies.copy(port,new GLNodeEndpoint(replacement,"order"),graph(asset,graph),registry);
        assertEquals(List.of(new GLNodeEndpoint(replacement,"order")),GroupEditorCopies.targets(linked));
    }
    @Test void changingANodeTypeInvalidatesItsFormerEditor(){
        var asset=GroupAuthoringTest.asset(new FilterNode(),"order");var graph=new GLNodeGraph(asset.graph());var osc=new OscillatorNode();
        graph.edit("Change types",e->{for(var node:asset.graph().nodes()){e.removeNode(node.id());e.addNode(new GLGraphNode(node.id(),osc.registration().id(),osc.defaults()));}});
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
        var cleaned=GroupInputs.materialize(graph(asset,graph),registry);
        assertTrue(GroupEditorCopies.targets(cleaned.compositeDescriptor().portMappings().get(0)).isEmpty());
        assertTrue(cleaned.compositeDescriptor().propertyMappings().isEmpty());
    }
    private static GLGraphConnection wire(GLNodeId from,String output,GLNodeId to,String input){return new GLGraphConnection(GLNodeConnectionId.random(),new GLNodeEndpoint(from,output),new GLNodeEndpoint(to,input));}
    @Test void signalValidationKeepsValidConnectionsAndUnknownPlugins(){
        var osc=new OscillatorNode();var filter=new FilterNode();var fft=new SpectrumNode();
        var a=new GLGraphNode(GLNodeId.random(),osc.registration().id(),osc.defaults());var b=new GLGraphNode(GLNodeId.random(),filter.registration().id(),filter.defaults());
        var c=new GLGraphNode(GLNodeId.random(),fft.registration().id(),fft.defaults());var plugin=new GLGraphNode(GLNodeId.random(),"missing.plugin",Map.of());
        var valid=wire(a.id(),"audio-out",b.id(),"audio-in");var unknown=wire(plugin.id(),"out",b.id(),"audio-in");
        var graph=new GLNodeGraphSnapshot(new GLNodeGraph().snapshot().graphId(),0,List.of(a,b,c,plugin),List.of(valid,wire(a.id(),"gone",b.id(),"audio-in"),wire(c.id(),"analysis",b.id(),"audio-in"),unknown));
        assertEquals(List.of(valid,unknown),GroupInterfaceValidity.connections(graph,new GLCompositeGraphRegistry()).connections());
    }
    @Test void removedDynamicInputsDetachBoundariesAndPropertyMappings(){
        var definition=new StepSwitchNode();var node=new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()).withProperty("steps",GLNodeValue.of(1.));
        var graph=new GLNodeGraph();graph.edit("Switch",e->e.addNode(node));
        var input=new GLCompositePortMapping(GLNodePortDescriptor.input("Input",SignalCategory.VALUE.valueTypeId(),false,1),new GLNodeEndpoint(node.id(),"step-3"));
        var output=new GLCompositePortMapping(GLNodePortDescriptor.output("Output",SignalCategory.VALUE.valueTypeId()),new GLNodeEndpoint(node.id(),StepSwitchNode.OUT.value()));
        var descriptor=new GLCompositeNodeDescriptor("test.dynamic.group","test.dynamic.asset",1,List.of(input,output),
            List.of(new GLCompositePropertyDescriptor("Default","number",GLCompositeValueKind.DECIMAL,GLNodeValue.of(0.))),List.of(new GLCompositePropertyMapping("Default",node.id(),"step-3")));
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(),Map.of(),descriptor);
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);var clean=GroupInterfaceValidity.clean(asset,registry);
        assertEquals(2,clean.compositeDescriptor().portMappings().size());
        assertFalse(clean.compositeDescriptor().portMappings().get(0).isBound());assertTrue(clean.compositeDescriptor().portMappings().get(1).isBound());
        assertTrue(clean.compositeDescriptor().propertyMappings().isEmpty());
    }
    @Test void unsupportedCopiedChoiceValuesReleaseTheirTargets(){
        var asset=GroupAuthoringTest.asset(new FilterNode(),"order");var port=asset.compositeDescriptor().portMappings().get(0);var template=GroupInputs.template(port);
        var entries=new ArrayList<>(template.entries());var first=entries.get(0);entries.set(0,new EditorTemplates.Entry(first.id(),first.label(),GLNodeValue.of("removed-option")));
        var options=new HashMap<>(port.presentation().editorOptions());options.put("template",new EditorTemplates.Template(template.id(),template.label(),entries,template.initial()).value());
        var replacement=new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),new GLCompositePortPresentation(port.presentation().inputEditorId(),port.presentation().socketVisible(),options),port.additionalEndpoints());
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
        assertTrue(GroupEditorCopies.targets(GroupInterfaceValidity.port(replacement,asset,registry)).isEmpty());
    }
    @Test void deletingLinkedNodesAndUndoRestoreBothGraphAndLinksAsOneAction()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var asset=GroupAuthoringTest.asset(new FilterNode(),"order");controller.openDocument("Linked editors",GroupAuthoringTest.document(asset,GLNodeValue.of(GroupInputs.template(asset.compositeDescriptor().portMappings().get(0)).initial())));
            controller.workspace().openComposite(asset.id());controller.history().reset();
            var target=asset.graph().nodes().get(0).id();
            controller.workspace().editor().setSelectedNode(controller.workspace().editor().getNodes().stream().filter(n->n instanceof BasicNodeView b&&b.nodeId().equals(target)).findFirst().orElseThrow());
            controller.actions().delete();
            assertEquals(1,GroupEditorCopies.targets(live(controller,asset.id()).compositeDescriptor().portMappings().get(0)).size());
            var after=controller.snapshotDocument().embeddedAssets().get(asset.id());assertEquals(1,GroupEditorCopies.targets(after.compositeDescriptor().portMappings().get(0)).size());
            assertTrue(controller.history().undo());assertFalse(controller.history().canUndo());
            var restored=controller.snapshotDocument().embeddedAssets().get(asset.id());assertEquals(2,restored.graph().nodes().size());assertEquals(2,GroupEditorCopies.targets(restored.compositeDescriptor().portMappings().get(0)).size());
            assertTrue(controller.history().redo());assertEquals(1,GroupEditorCopies.targets(controller.snapshotDocument().embeddedAssets().get(asset.id()).compositeDescriptor().portMappings().get(0)).size());
        }});
    }
    @Test void groupingLinkedNodesDetachesParentReferencesAndUndoRestoresThem()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var asset=GroupAuthoringTest.asset(new FilterNode(),"order");controller.openDocument("Group linked nodes",GroupAuthoringTest.document(asset,GLNodeValue.of(GroupInputs.template(asset.compositeDescriptor().portMappings().get(0)).initial())));
            controller.workspace().openComposite(asset.id());controller.history().reset();
            controller.workspace().editor().setSelectedNodes(controller.workspace().editor().getNodes().stream().filter(BasicNodeView.class::isInstance).toList());
            controller.actions().extractComposite();
            assertTrue(GroupEditorCopies.targets(live(controller,asset.id()).compositeDescriptor().portMappings().get(0)).isEmpty());
            assertTrue(GroupEditorCopies.targets(controller.snapshotDocument().embeddedAssets().get(asset.id()).compositeDescriptor().portMappings().get(0)).isEmpty());
            assertTrue(controller.history().undo());assertFalse(controller.history().canUndo());
            assertEquals(2,GroupEditorCopies.targets(controller.snapshotDocument().embeddedAssets().get(asset.id()).compositeDescriptor().portMappings().get(0)).size());
            assertTrue(controller.history().redo());
            assertTrue(GroupEditorCopies.targets(live(controller,asset.id()).compositeDescriptor().portMappings().get(0)).isEmpty());
        }});
    }
    @Test void directGraphChangesAreReconciledAfterTheEditCompletes()throws Exception{
        var holder=new WorkspaceController[1];var id=new String[1];
        SwingUtilities.invokeAndWait(()->{
            var controller=holder[0]=new WorkspaceController();var asset=GroupAuthoringTest.asset(new FilterNode(),"order");id[0]=asset.id();
            controller.openDocument("Graph changes",GroupAuthoringTest.document(asset,GLNodeValue.of(GroupInputs.template(asset.compositeDescriptor().portMappings().get(0)).initial())));
            controller.workspace().openComposite(asset.id());
        });
        SwingUtilities.invokeAndWait(()->{
            var controller=holder[0];var asset=live(controller,id[0]);
            assertEquals(2,GroupEditorCopies.targets(asset.compositeDescriptor().portMappings().get(0)).size());
            controller.workspace().currentGraph().edit("Replace nodes",e->{for(var node:asset.graph().nodes())e.removeNode(node.id());var filter=new FilterNode();e.addNode(new GLGraphNode(GLNodeId.random(),filter.registration().id(),filter.defaults()));});
        });
        SwingUtilities.invokeAndWait(()->{try(var controller=holder[0]){
            assertTrue(GroupEditorCopies.targets(live(controller,id[0]).compositeDescriptor().portMappings().get(0)).isEmpty());
            assertTrue(live(controller,id[0]).compositeDescriptor().propertyMappings().isEmpty());
            assertDoesNotThrow(controller::audioGraph);
        }});
    }
}
