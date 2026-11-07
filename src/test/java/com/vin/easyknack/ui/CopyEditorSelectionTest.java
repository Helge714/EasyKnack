package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class CopyEditorSelectionTest {
    private static Object field(Object owner,String name)throws ReflectiveOperationException{
        var field=owner.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(owner);
    }
    @Test void newInputSkipsTheOccupiedFirstSourceAndCanSelectCopyEditor()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var definition=new SpectrumNode();var seed=GroupAuthoringTest.asset(definition,"channel");
            var first=GroupInputSettings.availableSources(seed,ProcessingRate.AUTO).get(0);
            var asset=GroupAuthoringTest.asset(definition,first.property());var descriptor=asset.compositeDescriptor();
            var occupied=GroupEditorCopies.targets(descriptor.portMappings().get(0));
            assertTrue(occupied.containsAll(GroupInputSettings.availableSources(asset,ProcessingRate.AUTO).get(0).endpoints()));
            var ports=new ArrayList<>(descriptor.portMappings());ports.add(new GLCompositePortMapping(GLNodePortDescriptor.input("New",SignalCategory.VALUE.valueTypeId(),false,1),null));
            asset=GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),1,ports,descriptor.properties(),descriptor.propertyMappings()));
            controller.openDocument("Copy selection",GroupAuthoringTest.document(asset,GLNodeValue.of(GroupInputs.template(ports.get(0)).initial())));
            controller.workspace().openComposite(asset.id());var editor=controller.workspace().interfaceEditor();
            ((GLList<?>)field(editor,"inputs")).setSelectedIndex(1);
            var settings=(GroupInputSettings)field(field(editor,"applicationSettings"),"inputSettings");
            @SuppressWarnings("unchecked") var kind=(GLComboBox<GroupInputSettings.Kind>)field(settings,"kind");
            kind.setSelectedItem(GroupInputSettings.Kind.COPY);
            var selected=editor.selectedPort();assertEquals(GroupInputSettings.Kind.COPY,GroupInputSettings.selectedKind(selected));
            assertNull(settings.source.getSelectedItem());assertTrue(GroupEditorCopies.targets(selected).isEmpty());
            settings.source.setSelectedItem(settings.source.getItems().get(0));
            var linked=GroupEditorCopies.targets(editor.selectedPort());assertFalse(linked.isEmpty(),"The explicitly chosen free source must be linked");
            assertTrue(Collections.disjoint(occupied,linked));
            var snapshot=controller.snapshotDocument().embeddedAssets().get(asset.id());
            var available=GroupInputSettings.availableSources(snapshot,ProcessingRate.AUTO,"New");
            assertTrue(available.stream().flatMap(s->s.endpoints().stream()).anyMatch(linked::contains),"The current input must retain its own sources");
            assertTrue(available.stream().flatMap(s->s.endpoints().stream()).noneMatch(occupied::contains));
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}});
    }
    @Test void hilbertLengthCanBeChosenBeforeANestedInputClaimsThePort()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            var child=GroupUnitsTest.document(false).embeddedAssets().get("test.asset");
            var seed=GroupAuthoringTest.asset(new HilbertNode(),"length");
            var graph=new GLNodeGraph(seed.graph());
            graph.edit("Nested group",e->e.addNode(new GLGraphNode(GLNodeId.random(),child.compositeDescriptor().nodeTypeId(),Map.of())));
            var port=new GLCompositePortMapping(GLNodePortDescriptor.input("Length",SignalCategory.VALUE.valueTypeId(),false,1),null);
            var descriptor=new GLCompositeNodeDescriptor("test.hilbert.copy","test.hilbert.copy.asset",1,List.of(port));
            var asset=new com.vingame.glswing.node.document.GLNodeGraphAsset(descriptor.assetId(),1,seed.kind(),graph.snapshot(),seed.viewState(),Map.of(),descriptor);
            var doc=GroupAuthoringTest.document(asset,GLNodeValue.of(0.));
            controller.openDocument("Hilbert copy",new com.vingame.glswing.node.document.GLNodeGraphDocument(doc.root(),Map.of(asset.id(),asset,child.id(),child),List.of()));
            controller.workspace().openComposite(asset.id());var editor=controller.workspace().interfaceEditor();
            ((GLList<?>)field(editor,"inputs")).setSelectedIndex(0);
            var settings=(GroupInputSettings)field(field(editor,"applicationSettings"),"inputSettings");
            settings.kind.setSelectedItem(GroupInputSettings.Kind.COPY);
            assertFalse(editor.selectedPort().isBound());assertTrue(settings.source.isVisible());assertNull(settings.source.getSelectedItem());
            assertTrue(settings.source.getItems().stream().allMatch(s->s.type().equals(new HilbertNode().registration().id())),"Nested inputs with sockets are connected by wires and are excluded from standalone sources");
            settings.source.setSelectedItem(settings.source.getItems().stream().filter(s->s.type().equals(new HilbertNode().registration().id())&&s.property().equals("length")).findFirst().orElseThrow());
            var selected=editor.selectedPort();assertFalse(selected.isBound());
            assertEquals("257",GroupInputs.template(selected).initial());
            assertEquals("length",GroupEditorCopies.targets(selected).get(0).portId());
            assertDoesNotThrow(controller::audioGraph);
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}});
    }
    @Test void partlyOccupiedSourcesOfferOnlyTheirFreeTargets(){
        var asset=GroupAuthoringTest.asset(new SpectrumNode(),"size");var descriptor=asset.compositeDescriptor();var port=descriptor.portMappings().get(0);
        var targets=GroupEditorCopies.targets(port);var options=new HashMap<>(port.presentation().editorOptions());options.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(List.of(targets.get(0))));
        port=new GLCompositePortMapping(port.publicPort(),port.internalEndpoint(),new GLCompositePortPresentation(port.presentation().inputEditorId(),port.presentation().socketVisible(),options),port.additionalEndpoints());
        asset=GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),1,List.of(port),descriptor.properties(),descriptor.propertyMappings()));
        var source=GroupInputSettings.availableSources(asset,ProcessingRate.AUTO,"New").stream().filter(s->s.property().equals("size")).findFirst().orElseThrow();
        assertEquals(List.of(targets.get(1)),source.endpoints());
    }
}
