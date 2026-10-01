package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;
class EditorTemplatesTest {
    private static GroupInputSettings selectInput(WorkspaceController c){
        c.workspace().openComposite("test.asset");c.workspace().inspectorTabs().setSelectedIndex(3);var ui=c.workspace().interfaceEditor();
        GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(n->(GLList<?>)n).findFirst().orElseThrow().setSelectedIndex(0);
        return GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();
    }
    @Test void ratesFilterEditorChoicesAndModeResizesSettings()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Modes",GroupUnitsTest.document(false));c.editorTemplates().put(frequencies());var settings=selectInput(c);
            assertEquals(List.of(GroupInputSettings.Kind.SLIDER,GroupInputSettings.Kind.CHECKBOX,GroupInputSettings.Kind.COMBO,GroupInputSettings.Kind.COPY),settings.kind.getItems());
            float sliderHeight=settings.getPreferredGLSize().height();settings.kind.setSelectedItem(GroupInputSettings.Kind.COMBO);
            assertTrue(settings.model.isVisible());assertFalse(settings.source.isVisible());assertTrue(settings.getPreferredGLSize().height()<sliderHeight);
            settings.rate.setSelectedItem(com.vin.easyknack.nodes.ProcessingRate.CONTROL);assertTrue(c.audioGraph().contains("rateconvert"));
            settings.rate.setSelectedItem(com.vin.easyknack.nodes.ProcessingRate.EVENT);
            assertEquals(List.of(GroupInputSettings.Kind.BUTTON,GroupInputSettings.Kind.COPY),settings.kind.getItems());assertFalse(settings.model.isVisible());assertFalse(settings.source.isVisible());
            settings.rate.setSelectedItem(com.vin.easyknack.nodes.ProcessingRate.AUTO);assertEquals(GroupInputSettings.Kind.SLIDER,settings.kind.getSelectedItem());
        }});
    }
    @Test void numericModelsSelectWaveformEntriesAndDisconnectRemovesFallbackBinding(){
        var doc=GroupUnitsTest.document(false);var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);var asset=doc.embeddedAssets().get("test.asset");var d=asset.compositeDescriptor();var old=d.portMappings().get(0);
        var port=new GLCompositePortMapping(old.publicPort(),new GLNodeEndpoint(old.internalEndpoint().nodeId(),"waveform"),old.presentation());
        var entries=List.of(new EditorTemplates.Entry("first","First",GLNodeValue.of(0.)),new EditorTemplates.Entry("second","Second",GLNodeValue.of(1.)));var template=new EditorTemplates.Template("waves","Waves",entries,"first");
        asset=GroupInputs.applyTemplate(asset,port,template,registry);var combo=GroupChoices.nodeEditor(asset.graph().findNode(port.internalEndpoint().nodeId()).orElseThrow(),"waveform");
        assertEquals(GLNodeValue.of(combo.items().get(1).id()),asset.compositeDescriptor().propertyMappings().get(0).mappedValue(GLNodeValue.of("second")));
        var current=asset.compositeDescriptor();var ports=new ArrayList<>(current.portMappings());ports.set(0,ports.get(0).withEndpoints(List.of()));
        asset=GroupInputs.materialize(GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(current.nodeTypeId(),current.assetId(),1,ports,current.properties(),current.propertyMappings())),registry);
        assertTrue(asset.compositeDescriptor().propertyMappings().isEmpty());
    }
    @Test void triggerButtonFollowsNestedConnectionsOnly(){
        var doc=GroupUnitsTest.document(true);var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);var outer=doc.embeddedAssets().get("test.outer.asset").compositeDescriptor();var child=outer.portMappings().get(0).internalEndpoint().nodeId();var inner=doc.embeddedAssets().get("test.asset").compositeDescriptor();var leaf=inner.portMappings().get(0).internalEndpoint();
        var received=new ArrayList<String>();com.vin.easyknack.audio.ManualTriggers.aliases(Map.of());com.vin.easyknack.audio.ManualTriggers.receiver(received::add);
        try{GroupInputTriggers.fire(registry,"instance",outer,outer.portMappings().get(0));assertEquals(List.of("instance/"+child+"/"+leaf.nodeId()+":"+leaf.portId()),received);}finally{com.vin.easyknack.audio.ManualTriggers.receiver(id->{});}
    }
    @Test void hidingSocketPreservesApplicationOwnedUnboundedSlider()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Unbounded",GroupUnitsTest.document(false));selectInput(c);var ui=c.workspace().interfaceEditor();var p=ui.selectedPort();var options=new HashMap<>(p.presentation().editorOptions());
            options.put("minimum",GLNodeValue.of(400.));options.put("maximum",GLNodeValue.of(1.));options.put("maximumEnabled",GLNodeValue.of(false));
            ui.updateSelectedPort(new GLCompositePortMapping(p.publicPort(),p.internalEndpoint(),new GLCompositePortPresentation(p.presentation().inputEditorId(),true,options),p.additionalEndpoints()));
            GroupEditingTest.descendants(ui).filter(n->n instanceof GLCheckBox b&&b.getText().equals("Show node socket")).map(GLCheckBox.class::cast).findFirst().orElseThrow().doClick();
            assertFalse(ui.selectedPort().presentation().socketVisible());assertEquals(GLNodeValue.of(400.),ui.selectedPort().presentation().editorOptions().get("minimum"));
        }});
    }
    @Test void connectedOscillatorEditorCanBeInheritedAndUndone()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            var doc=GroupUnitsTest.document(false);var a=doc.embeddedAssets().get("test.asset");var d=a.compositeDescriptor();var p=d.portMappings().get(0);
            var ports=new ArrayList<>(d.portMappings());ports.set(0,new GLCompositePortMapping(GLNodePortDescriptor.input("Waveform",com.vin.easyknack.nodes.SignalCategory.WAVETABLE.valueTypeId(),false,1),new GLNodeEndpoint(p.internalEndpoint().nodeId(),"waveform")));
            a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports,d.properties(),d.propertyMappings()));
            c.openDocument("Inherited",new GLNodeGraphDocument(doc.root(),Map.of(a.id(),a),List.of()));c.workspace().openComposite(a.id());c.workspace().inspectorTabs().setSelectedIndex(3);
            var ui=c.workspace().interfaceEditor();
            GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(n->(GLList<?>)n).findFirst().orElseThrow().setSelectedIndex(0);
            GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow().kind.setSelectedItem(GroupInputSettings.Kind.COPY);
            assertNotNull(GroupInputs.template(ui.selectedPort()));assertTrue(c.editorTemplates().all().isEmpty());
            assertTrue(GroupInputs.template(ui.selectedPort()).entries().size()>=4);assertEquals("waveform",c.snapshotDocument().embeddedAssets().get(a.id()).compositeDescriptor().propertyMappings().get(0).internalPropertyId());
            c.history().undo();assertTrue(c.editorTemplates().all().isEmpty());assertNull(GroupInputs.template(ui.selectedPort()));
            var inputSettings=GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();
            for(int i=0;i<3;i++){inputSettings.kind.setSelectedItem(GroupInputSettings.Kind.SLIDER);inputSettings.kind.setSelectedItem(GroupInputSettings.Kind.COPY);}
            assertTrue(c.editorTemplates().all().isEmpty());
            var saved=c.snapshotDocument();c.openDocument("Reloaded local editor",saved);assertTrue(c.editorTemplates().all().isEmpty());
            assertEquals("easyknack.choice",c.snapshotDocument().embeddedAssets().get(a.id()).compositeDescriptor().portMappings().get(0).presentation().inputEditorId());
        }});
    }
    @Test void hidingOuterSocketKeepsInternalInputAndItsConnections()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Hidden socket",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");c.workspace().inspectorTabs().setSelectedIndex(3);
            var ui=c.workspace().interfaceEditor();
            GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(n->(GLList<?>)n).findFirst().orElseThrow().setSelectedIndex(0);
            var boundary=c.workspace().editor().getNodes().stream().filter(n->n.findPort("Pitch")!=null).findFirst().orElseThrow();boundary.setLocation(-450,123);
            GroupEditingTest.descendants(ui).filter(n->n instanceof GLCheckBox b&&b.getText().equals("Show node socket")).map(GLCheckBox.class::cast).findFirst().orElseThrow().doClick();
            assertFalse(ui.selectedPort().presentation().socketVisible());
            var retained=c.workspace().editor().getNodes().stream().filter(n->n.findPort("Pitch")!=null).findFirst().orElseThrow();
            assertTrue(retained.findPort("Pitch").isSocketVisible());assertEquals(-450,retained.getX());assertEquals(123,retained.getY());
            assertTrue(c.workspace().editor().getConnections().stream().anyMatch(w->w.getOutput()==retained.findPort("Pitch")));
            var saved=c.snapshotDocument();assertEquals(1,saved.embeddedAssets().get("test.asset").compositeDescriptor().portMappings().get(0).internalEndpoints().size());
            c.workspace().navigateBack();assertFalse(GroupUnitsTest.group(c).findPort("Pitch").isSocketVisible());
            c.openDocument("Reload",saved);c.workspace().openComposite("test.asset");assertTrue(c.workspace().editor().getNodes().stream().anyMatch(n->n.findPort("Pitch")!=null));
        }});
    }
    static EditorTemplates.Template frequencies(){return new EditorTemplates.Template("frequencies","Frequency presets",List.of(new EditorTemplates.Entry("low","Low",GLNodeValue.of(220.)),new EditorTemplates.Entry("high","High",GLNodeValue.of(880.))),"low");}
    @Test void templateBindsTheParameterOfANestedGroupRatherThanItsPortName(){
        var doc=GroupUnitsTest.document(true);var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);
        var asset=doc.embeddedAssets().get("test.outer.asset");
        var updated=GroupInputs.applyTemplate(asset,asset.compositeDescriptor().portMappings().get(0),frequencies(),registry);
        assertEquals(1,updated.compositeDescriptor().propertyMappings().size());
        assertEquals(GroupUnits.key("Pitch"),updated.compositeDescriptor().propertyMappings().get(0).internalPropertyId());
        registry.replaceAsset(updated);var prepared=GroupCompiler.prepare(doc.root(),registry);
        var flat=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));
        assertTrue(flat.isValid(),flat.diagnostics().toString());
    }
    @Test void templateSelectionThroughIoPersistsItsValuesAndStableProperty()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Templates",GroupUnitsTest.document(false));c.editorTemplates().put(frequencies());
            assertTrue(c.workspace().openComposite("test.asset"));c.workspace().inspectorTabs().setSelectedIndex(3);
            var ui=c.workspace().interfaceEditor();
            GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(n->(GLList<?>)n).findFirst().orElseThrow().setSelectedIndex(0);
            var settings=GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();
            settings.kind.setSelectedItem(GroupInputSettings.Kind.COMBO);
            var port=ui.selectedPort();assertNotNull(GroupInputs.template(port));String property=GroupInputs.property(port);assertTrue(port.presentation().editorOptions().containsKey(GroupInputs.CHOICE));
            var saved=c.snapshotDocument();var a=saved.embeddedAssets().get("test.asset");assertEquals(1,a.compositeDescriptor().propertyMappings().stream().filter(m->m.publicPropertyId().equals(property)).count());
            assertEquals(GLNodeValue.of(220.),a.compositeDescriptor().propertyMappings().stream().filter(m->m.publicPropertyId().equals(property)).findFirst().orElseThrow().mappedValue(GLNodeValue.of("low")));
            c.workspace().navigateBack();
            var view=GroupUnitsTest.group(c);assertNotNull(view.findPort("Pitch"));
            @SuppressWarnings("unchecked") var combo=(GLComboBox<GroupChoices.Option>)view.getContents().stream().filter(GLComboBox.class::isInstance).findFirst().orElseThrow();
            combo.setSelectedItem(combo.getItems().stream().filter(v->v.id().equals("high")).findFirst().orElseThrow());
            assertTrue(c.audioGraph().contains("880.0"),c.audioGraph());
            try{var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(c.snapshotDocument(),bytes);c.openDocument("Reload",codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion()));}catch(Exception ex){throw new AssertionError(ex);}
            assertEquals(frequencies(),c.editorTemplates().all().get(0));assertTrue(c.audioGraph().contains("880.0"),c.audioGraph());
        }});
    }
    @Test void connectionValuesReplaceManualMappingsAndKeepStableInputIdentity(){
        var doc=GroupUnitsTest.document(false);var a=doc.embeddedAssets().get("test.asset");var port=a.compositeDescriptor().portMappings().get(0);
        a=GroupInputs.applyTemplate(a,port,frequencies());var d=a.compositeDescriptor();var p=d.portMappings().get(0);String property=GroupInputs.property(p);
        var maps=d.propertyMappings().stream().map(m->new GLCompositePropertyMapping(m.publicPropertyId(),m.internalNodeId(),m.internalPropertyId(),Map.of("low",GLNodeValue.of(330.),"high",GLNodeValue.of(660.)))).toList();
        var renamed=new GLCompositePortMapping(GLNodePortDescriptor.input("Renamed",p.publicPort().valueTypeId(),false,1),p.internalEndpoint(),p.presentation(),p.additionalEndpoints());
        var ports=new ArrayList<>(d.portMappings());ports.set(0,renamed);a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,ports,d.properties(),maps));
        a=GroupInputs.materialize(a);assertEquals(property,GroupInputs.property(a.compositeDescriptor().portMappings().get(0)));assertEquals(GLNodeValue.of(220.),a.compositeDescriptor().propertyMappings().get(0).mappedValue(GLNodeValue.of("low")));
    }
}
