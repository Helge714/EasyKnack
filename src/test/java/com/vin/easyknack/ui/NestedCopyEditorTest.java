package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.binding.GLNodeEditorViewState;
import com.vingame.glswing.ui.*;

class NestedCopyEditorTest {
    private static GLNodeGraphAsset outer(GLNodeGraphAsset child,int level,GLCompositeGraphRegistry groups,boolean copy){
        var graph=new GLNodeGraph();var instance=new GLGraphNode(GLNodeId.random(),child.compositeDescriptor().nodeTypeId(),Map.of());graph.edit("Child",e->e.addNode(instance));
        var control=new GLCompositePortMapping(GLNodePortDescriptor.input("Control",SignalCategory.VALUE.valueTypeId(),false,1),null);
        var ports=new ArrayList<GLCompositePortMapping>();ports.add(control);
        for(var port:child.compositeDescriptor().portMappings())if(port.publicPort().direction()==GLPortDirection.OUTPUT)
            ports.add(new GLCompositePortMapping(port.publicPort(),new GLNodeEndpoint(instance.id(),port.publicPort().id())));
        var descriptor=new GLCompositeNodeDescriptor("test.nested.copy."+level,"test.nested.copy.asset."+level,1,ports);
        var asset=new GLNodeGraphAsset(descriptor.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),descriptor);
        groups.registerAsset(asset);
        if(copy){
            var sources=GroupInputSettings.availableSources(asset,ProcessingRate.CONTROL,"Control",groups);
            GLCompositePortMapping selected;
            if(child.compositeDescriptor().portMappings().get(0).presentation().socketVisible()){
                assertTrue(sources.isEmpty());
                var endpoint=new GLNodeEndpoint(instance.id(),child.compositeDescriptor().portMappings().get(0).publicPort().id());
                var connected=control.withEndpoints(List.of(endpoint));selected=GroupEditorCopies.inheritPort(connected,endpoint,asset,groups);
            }else{assertEquals(1,sources.size());selected=GroupEditorCopies.copy(control,sources.get(0).endpoint(),asset,groups);}
            ports.set(0,selected);
            asset=GroupInputs.materialize(GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(descriptor.nodeTypeId(),descriptor.assetId(),1,ports)),groups);
            groups.replaceAsset(asset);
        }
        return asset;
    }
    private static GLNodeGraphDocument document(GLNodeGraphAsset outer,GLCompositeGraphRegistry groups,GLNodeValue value){
        var port=outer.compositeDescriptor().portMappings().get(0);var graph=new GLNodeGraph();
        graph.edit("Root",e->e.addNode(new GLGraphNode(GLNodeId.random(),outer.compositeDescriptor().nodeTypeId(),Map.of(GroupEditorCopies.groupProperty(port),value))));
        var assets=new HashMap<String,GLNodeGraphAsset>();for(var asset:groups.assets())assets.put(asset.id(),asset);
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()),assets,List.of());
    }
    @Test void nestedSliderCheckboxAndChoiceControlTheLeafAndSurvivePersistence()throws Exception{
        for(String property:List.of("order","compensate",CurveNode.FROM_MIN.value(),VectorNode.VALUE.value())){
            BasicNode definition=property.equals(VectorNode.VALUE.value())?new VectorNode():property.equals(CurveNode.FROM_MIN.value())?new CurveNode():new FilterNode();
            var child=GroupAuthoringTest.asset(definition,property);var groups=new GLCompositeGraphRegistry();groups.registerAsset(child);
            GLNodeValue value=property.equals(VectorNode.VALUE.value())?VectorEditor.value(1,2,3,UnitFormat.METRE):property.equals("order")?GLNodeValue.of("6"):GLNodeValue.of(property.equals("compensate")?1.:-5.);
            var copied=outer(child,0,groups,true);var twice=outer(copied,1,groups,true);var document=document(twice,groups,value);
            var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(document,bytes);
            var restored=NodeDocuments.prepare(codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));
            var registry=new GLCompositeGraphRegistry();registry.registerDocument(restored);
            var flat=SpectralGroups.flatten(GroupCompiler.prepare(restored.root(),registry),new GLCompositeExpansionLimits(16,256,512,10000));
            assertTrue(flat.isValid(),flat.diagnostics().toString());assertEquals(2,flat.graph().orElseThrow().nodes().size());
            for(var node:flat.graph().orElseThrow().nodes())assertEquals(property.equals("compensate")?GLNodeValue.of(true):value,node.properties().get(property));
            var targets=GroupEditorCopies.targets(twice.compositeDescriptor().portMappings().get(0));
            assertTrue(GroupInputSettings.availableSources(twice,ProcessingRate.CONTROL,"Other",groups).isEmpty());
            assertTrue(GroupInputSettings.availableSources(twice,ProcessingRate.CONTROL,"Control",groups).stream().flatMap(s->s.endpoints().stream()).anyMatch(targets::contains));
        }
    }
    @Test void visibleGroupInputRemainsWireableAndExports(){
        var groups=new GLCompositeGraphRegistry();var source=GroupUnitsTest.document(false);source.embeddedAssets().values().forEach(groups::registerAsset);
        var child=source.embeddedAssets().get("test.asset");var copied=outer(child,0,groups,true);
        var port=copied.compositeDescriptor().portMappings().get(0);assertTrue(port.isBound());assertTrue(port.presentation().socketVisible());
        var document=document(copied,groups,GLNodeValue.of(880.));
        var flat=SpectralGroups.flatten(GroupCompiler.prepare(document.root(),groups),new GLCompositeExpansionLimits(16,256,512,10000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());
        assertTrue(flat.graph().orElseThrow().nodes().stream().anyMatch(n->GLNodeValue.of(880.).equals(n.properties().get("value"))));
        assertTrue(flat.graph().orElseThrow().connections().stream().anyMatch(c->c.input().portId().equals("frequency")));
        assertNotNull(GroupSourceExporter.create(document,document.root().graph().nodes().get(0)));
    }
    private static Object field(Object owner,String name)throws ReflectiveOperationException{var field=owner.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(owner);}
    @Test void coupledGroupEditorsAreDisabledAndUnlockAfterUnlinking()throws Exception{
        for(boolean visible:new boolean[]{false,true}){
        var holder=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{
            var child=visible?GroupUnitsTest.document(false).embeddedAssets().get("test.asset"):GroupAuthoringTest.asset(new FilterNode(),"order");var groups=new GLCompositeGraphRegistry();groups.registerAsset(child);
            var parent=outer(child,0,groups,true);var controller=holder[0]=new WorkspaceController();
            controller.openDocument("Controlled group",document(parent,groups,visible?GLNodeValue.of(880.):GLNodeValue.of("6")));controller.workspace().openComposite(parent.id());
        });
        SwingUtilities.invokeAndWait(()->{var controller=holder[0];{
            var group=(GroupViewFactory.GroupView)GroupUnitsTest.group(controller);
            var control=(GLControl)group.getContents().stream().filter(v->v instanceof GLComboBox<?>||v instanceof GLSlider).findFirst().orElseThrow();
            assertFalse(control.isEnabled());
            var editor=controller.workspace().interfaceEditor();try{((GLList<?>)field(editor,"inputs")).setSelectedIndex(0);}catch(ReflectiveOperationException e){throw new AssertionError(e);}
            var detached=GroupEditorCopies.detachSource(editor.selectedPort());
            editor.updateSelectedPort(new GLCompositePortMapping(detached.publicPort(),null,detached.presentation()));
        }});
        SwingUtilities.invokeAndWait(()->{try(var controller=holder[0]){
            var group=GroupUnitsTest.group(controller);
            assertTrue(((GLControl)group.getContents().stream().filter(v->v instanceof GLComboBox<?>||v instanceof GLSlider).findFirst().orElseThrow()).isEnabled());
        }});
        }
    }
    @Test void copyEditorPickerOffersTheEditorOfTheOnlyNestedGroup()throws Exception{
        SwingUtilities.invokeAndWait(()->{for(boolean visible:new boolean[]{false,true})try(var controller=new WorkspaceController()){
            var child=visible?GroupUnitsTest.document(false).embeddedAssets().get("test.asset"):GroupAuthoringTest.asset(new FilterNode(),"order");var groups=new GLCompositeGraphRegistry();groups.registerAsset(child);
            var asset=outer(child,0,groups,false);controller.openDocument("Nested Copy Editor",document(asset,groups,GLNodeValue.of(0.)));
            controller.workspace().openComposite(asset.id());var editor=controller.workspace().interfaceEditor();
            ((GLList<?>)field(editor,"inputs")).setSelectedIndex(0);var settings=field(field(editor,"applicationSettings"),"inputSettings");
            @SuppressWarnings("unchecked") var kind=(GLComboBox<GroupInputSettings.Kind>)field(settings,"kind");if(visible){assertFalse(kind.getItems().contains(GroupInputSettings.Kind.COPY));assertFalse(editor.selectedPort().isBound());continue;}kind.setSelectedItem(GroupInputSettings.Kind.COPY);
            var source=(GLComboBox<?>)field(settings,"source");assertFalse(source.getItems().isEmpty());
            assertNull(source.getSelectedItem());
            @SuppressWarnings("unchecked") var selection=(GLComboBox<Object>)source;selection.setSelectedItem(selection.getItems().get(0));
            if(visible){assertTrue(editor.selectedPort().isBound());assertTrue(editor.selectedPort().presentation().socketVisible());}
            else assertFalse(GroupEditorCopies.targets(editor.selectedPort()).isEmpty());
            assertEquals("",((GLLabel)field(settings,"status")).getText());
            assertDoesNotThrow(controller::audioGraph);
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}});
    }
}
