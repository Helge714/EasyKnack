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
import com.vingame.glswing.ui.*;

class GroupAuthoringTest {
    static GLNodeGraphAsset asset(BasicNode definition, String property){
        var graph=new GLNodeGraph();var a=GLNodeId.random();var b=GLNodeId.random();
        graph.edit("Nodes",e->{e.addNode(new GLGraphNode(a,definition.registration().id(),definition.defaults()));e.addNode(new GLGraphNode(b,definition.registration().id(),definition.defaults()));});
        var port=new GLCompositePortMapping(GLNodePortDescriptor.input("Größe ändern",SignalCategory.VALUE.valueTypeId(),false,1),null);
        var d=new GLCompositeNodeDescriptor("test.coupled","test.coupled.asset",1,List.of(port));
        var asset=new GLNodeGraphAsset(d.assetId(),1,GLNodeGraphAssetKind.COMPOSITE,graph.snapshot(),GLNodeEditorViewState.empty(),Map.of(),d);
        var registry=new GLCompositeGraphRegistry();registry.registerAsset(asset);
        port=GroupEditorCopies.copy(port,new GLNodeEndpoint(a,property),asset,registry);
        var options=new HashMap<>(port.presentation().editorOptions());options.put(GroupEditorCopies.TARGETS,GroupEditorCopies.targetsValue(List.of(new GLNodeEndpoint(a,property),new GLNodeEndpoint(b,property))));
        port=new GLCompositePortMapping(port.publicPort(),null,new GLCompositePortPresentation(port.presentation().inputEditorId(),false,options));
        return GroupInputs.descriptor(asset,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,List.of(port)));
    }
    static GLNodeGraphDocument document(GLNodeGraphAsset asset,GLNodeValue value){
        var p=asset.compositeDescriptor().portMappings().get(0);var key=GroupInputs.choice(p)?GroupInputs.property(p):GroupUnits.key(p.publicPort().id());
        var graph=new GLNodeGraph();graph.edit("Group",e->e.addNode(new GLGraphNode(GLNodeId.random(),asset.compositeDescriptor().nodeTypeId(),Map.of(key,value))));
        return new GLNodeGraphDocument(new GLNodeGraphAsset("root",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty()),Map.of(asset.id(),asset),List.of());
    }
    @Test void coupledEditorsControlBothTargetsAndRoundTrip()throws Exception{
        for(var property:List.of("order","compensate")){
            var asset=asset(new FilterNode(),property);var port=asset.compositeDescriptor().portMappings().get(0);
            GLNodeValue selection=property.equals("order")?GLNodeValue.of(GroupInputs.template(port).entries().get(2).id()):GLNodeValue.of(1.);
            var doc=document(asset,selection);var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(doc,bytes);doc=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
            var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);var prepared=GroupCompiler.prepare(doc.root(),registry);
            var result=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(result.isValid(),result.diagnostics().toString());
            assertEquals(2,result.graph().orElseThrow().nodes().size());
            for(var node:result.graph().orElseThrow().nodes())assertEquals(property.equals("order")?"6":true,node.properties().get(property).toJavaValue());
            assertFalse(port.presentation().socketVisible());assertTrue(port.internalEndpoints().isEmpty());
        }
    }
    @Test void coupledNumericEditorRemainsAnEditableGroupParameter(){
        var asset=asset(new CurveNode(),CurveNode.FROM_MIN.value());var doc=document(asset,GLNodeValue.of(-5.));
        var registry=new GLCompositeGraphRegistry();registry.registerDocument(doc);var prepared=GroupCompiler.prepare(doc.root(),registry);
        var result=GLCompositeGraphFlattener.flatten(prepared.root(),prepared.registry(),new GLCompositeExpansionLimits(16,256,512,10000));assertTrue(result.isValid(),result.diagnostics().toString());
        for(var n:result.graph().orElseThrow().nodes())assertEquals(-5.,n.properties().get(CurveNode.FROM_MIN.value()).toJavaValue());
    }
    @Test void internalEditorAndInspectorAreReadOnlyWhileCoupled()throws Exception{
        var holder=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();
            var asset=asset(new FilterNode(),"order");var p=asset.compositeDescriptor().portMappings().get(0);c.openDocument("Coupled",document(asset,GLNodeValue.of(GroupInputs.template(p).initial())));c.workspace().openComposite(asset.id());
        });
        SwingUtilities.invokeAndWait(()->{try(var c=holder[0]){
            var view=c.workspace().editor().getNodes().stream().filter(BasicNodeView.class::isInstance).map(BasicNodeView.class::cast).findFirst().orElseThrow();
            c.workspace().editor().setSelectedNode(view);
            assertTrue(view.getContents().stream().filter(v->v instanceof GLComboBox<?> b&&b.getItems().contains("4")).allMatch(v->!((GLComboBox<?>)v).isEnabled()));
            var before=c.workspace().currentGraph().snapshot().findNode(view.nodeId()).orElseThrow().properties().get("order");
            c.workspace().propertyInspector().setSelectedProperty("order",GLNodeValue.of("8"));
            assertEquals(before,c.workspace().currentGraph().snapshot().findNode(view.nodeId()).orElseThrow().properties().get("order"));
        }});
    }
    @Test void boundaryLayoutAndInsertionPreservePortsAndUndo()throws Exception{
        var holder=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();
            c.openDocument("Boundary",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");var ed=c.workspace().editor();
            var original=c.workspace().currentGraph().snapshot().nodes().get(0).id();var inner=ed.getNodes().stream().filter(BasicNodeView.class::isInstance).findFirst().orElseThrow();
            var input=ed.getNodes().stream().filter(n->!(n instanceof BasicNodeView)&&n.findPort("Pitch")!=null).findFirst().orElseThrow();
            var output=ed.getNodes().stream().filter(n->!(n instanceof BasicNodeView)&&n.findPort("Out")!=null).findFirst().orElseThrow();
            assertTrue(input.getX()+input.getWidth()<=inner.getX());assertTrue(output.getX()>=inner.getX()+inner.getWidth());
            for(boolean incoming:List.of(true,false)){
                var definition=new MathNode();var id=GLNodeId.random();c.workspace().currentGraph().edit("New math",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
                var node=ed.getNodes().stream().filter(v->v instanceof BasicNodeView b&&b.nodeId().equals(id)).findFirst().orElseThrow();
                var edge=ed.getConnections().stream().filter(w->incoming?w.getOutput().getId().equals("Pitch"):w.getInput().getId().equals("Out")).findFirst().orElseThrow();
                var handler=ed.getConnectionInsertionHandler();assertTrue(handler.canInsert(node,edge));assertTrue(handler.insert(node,edge,node.getX(),node.getY()));
                var mapping=c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals(incoming?"Pitch":"Out")).findFirst().orElseThrow();
                assertEquals(id,mapping.internalEndpoint().nodeId());
                c.history().undo();assertEquals(original,c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().stream().filter(p->p.publicPort().id().equals(incoming?"Pitch":"Out")).findFirst().orElseThrow().internalEndpoint().nodeId());
            }
            c.workspace().currentGraph().edit("Delete internal node",e->e.removeNode(original));
        });
        SwingUtilities.invokeAndWait(()->{try(var c=holder[0]){
            var ports=c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings();assertEquals(2,ports.size());assertTrue(ports.stream().noneMatch(GLCompositePortMapping::isBound));
        }});
    }
    @Test void displayUnitCanExcludeStoredUnit(){
        var p=new GLCompositePortMapping(GLNodePortDescriptor.input("Zeit",SignalCategory.VALUE.valueTypeId(),false,1),null,new GLCompositePortPresentation(GLNodeInputEditorRegistry.SLIDER,true,Map.of(GroupUnits.BASE,GLNodeValue.of("ms"),GroupUnits.ALLOWED,GLNodeValue.of(List.of("sample","second")),GroupUnits.INITIAL,GLNodeValue.of("sample"))));
        assertEquals(UnitFormat.SAMPLE,GroupUnits.initial(p,UnitFormat.MS));assertDoesNotThrow(()->new UnitFormats(UnitFormat.MS,GroupUnits.initial(p,UnitFormat.MS),GroupUnits.allowed(p,UnitFormat.MS)));
    }
    @Test void draggingOntoBoundaryCreatesOnePortWithInheritedEditorAndUndo()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
            c.openDocument("Drop",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");var ed=c.workspace().editor();
            var source=ed.getNodes().stream().filter(BasicNodeView.class::isInstance).findFirst().orElseThrow().findPort("waveform");
            var boundary=ed.getNodes().stream().filter(n->!(n instanceof BasicNodeView)&&n.findPort("Pitch")!=null).findFirst().orElseThrow();
            GLNodeEditor.ConnectionBodyDropHandler handler;
            try{var field=GLNodeEditor.class.getDeclaredField("bodyDropHandler");field.setAccessible(true);handler=(GLNodeEditor.ConnectionBodyDropHandler)field.get(ed);}catch(ReflectiveOperationException e){throw new AssertionError(e);}
            float x=boundary.getX()+boundary.getWidth()+ed.getConnectionSnapDistance()/ed.getZoom()/2,y=boundary.getY()+10;
            float height=boundary.getHeight();handler.preview(source,x,y);assertNotNull(handler.previewPort());assertEquals(2,boundary.getPorts().size());assertTrue(boundary.getHeight()>height);handler.clear();assertEquals(1,boundary.getPorts().size());assertEquals(height,boundary.getHeight());
            assertEquals(2,c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().size());
            handler.preview(source,x,y);assertTrue(handler.drop(source,x,y));
            var ports=c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings();assertEquals(3,ports.size());
            var added=ports.get(2);assertEquals(SignalCategory.WAVETABLE.valueTypeId(),added.publicPort().valueTypeId());assertNotNull(GroupInputs.template(added));assertTrue(added.isBound());assertTrue(c.editorTemplates().all().isEmpty());
            c.history().undo();assertEquals(2,c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().size());
            c.history().redo();assertEquals(3,c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().size());
        }});
    }
    @Test void editorSourcesAreGroupedAndExcludeInputsAndUnitSelectors(){
        var asset=asset(new SpectrumNode(),SpectrumNode.OVERLAP.value());
        var sources=GroupInputSettings.availableSources(asset,ProcessingRate.AUTO);
        var overlap=sources.stream().filter(v->v.property().equals("overlap")).toList();assertEquals(1,overlap.size());assertEquals(2,overlap.get(0).endpoints().size());
        for(var source:sources){assertFalse(source.property().endsWith(".unit"));var row=NodeRegistry.require(source.type()).layout(new NodeState(asset.graph().nodes().get(0).properties())).rows().stream().filter(r->r.id().equals(source.property())).findFirst().orElseThrow();assertEquals(NodeBuilder.RowKind.EDITOR,row.kind());assertFalse(row.isMenuOnly());}
    }
    @Test void quickPortsPreserveSemanticCategoryAndAutoRate(){
        var definition=new MathNode();var asset=asset(definition,"a");var node=asset.graph().nodes().get(0);
        for(var row:definition.layout(new NodeState(node.properties())).rows())if(row.kind()!=NodeBuilder.RowKind.EDITOR){
            var direction=row.kind()==NodeBuilder.RowKind.INPUT?GLPortDirection.INPUT:GLPortDirection.OUTPUT;
            var p=new GLCompositePortMapping(new GLNodePortDescriptor("New",direction,"audio",GLPortTiming.CURRENT,false,1),new GLNodeEndpoint(node.id(),row.id()));
            var inherited=GroupEditorCopies.inheritPort(p,p.internalEndpoint(),asset,new GLCompositeGraphRegistry());
            assertEquals(row.signal().valueTypeId(),inherited.publicPort().valueTypeId());assertEquals(row.adaptive()?ProcessingRate.AUTO:row.rate(),GroupInputSettings.selectedRate(inherited));
        }
    }
    @Test void coupledBarAndButtonSlidersAreDisabled()throws Exception{
        for(var definition:List.<BasicNode>of(new CurveNode(),new SpectrumNode())){
            var property=definition instanceof CurveNode?CurveNode.FROM_MIN:SpectrumNode.OVERLAP;var holder=new WorkspaceController[1];
            SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();var a=asset(definition,property.value());var d=a.compositeDescriptor();var port=d.portMappings().get(0);var options=new HashMap<>(port.presentation().editorOptions());options.put("default",GLNodeValue.of(.37));a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,List.of(new GLCompositePortMapping(port.publicPort(),null,new GLCompositePortPresentation(port.presentation().inputEditorId(),false,options)))));c.openDocument("Slider",document(a,GLNodeValue.of(.5)));c.workspace().openComposite(a.id());});
            SwingUtilities.invokeAndWait(()->{try(var c=holder[0]){for(var node:c.workspace().editor().getNodes())if(node instanceof BasicNodeView view){
                assertTrue(view.getContents().stream().anyMatch(v->v instanceof GLSlider slider&&!slider.isEnabled()));
                view.getContents().stream().filter(v->v instanceof GLSlider slider&&!slider.isEnabled()).map(GLSlider.class::cast).forEach(slider->assertEquals(.37,slider.getValue(),1e-9));
                var before=c.workspace().currentGraph().snapshot().findNode(view.nodeId()).orElseThrow();c.setNodeProperty(view.nodeId(),property,GLNodeValue.of(42.));c.setNodeProperty(view.nodeId(),new PropertyId(property.value()+".unit"),GLNodeValue.of("percent"));assertEquals(before,c.workspace().currentGraph().snapshot().findNode(view.nodeId()).orElseThrow());
            }}});
        }
    }
    @Test void copiedEditorsHideSpecificationsAndCanBeRemovedWithUndo()throws Exception{
        for(var definition:List.<BasicNode>of(new CurveNode(),new SpectrumNode(),new FilterNode())){
            String property=definition instanceof CurveNode?"from-min":definition instanceof SpectrumNode?"overlap":"order";
            SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
                var a=asset(definition,property);var port=a.compositeDescriptor().portMappings().get(0);
                c.openDocument("Copied",document(a,GroupInputs.choice(port)?GLNodeValue.of(GroupInputs.template(port).initial()):GLNodeValue.of(.5)));c.workspace().openComposite(a.id());
                var ui=c.workspace().interfaceEditor();
                GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(GLList.class::cast).findFirst().orElseThrow().setSelectedIndex(0);
                var settings=GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();
                assertEquals(GroupInputSettings.Kind.COPY,settings.kind.getSelectedItem());assertTrue(settings.source.isVisible());assertFalse(settings.sliderStyle.isVisible());assertEquals(!GroupInputs.choice(port),settings.initialUnit.isVisible());assertTrue(settings.rate.isVisible());assertFalse(settings.rate.isEnabled());assertFalse(settings.model.isVisible());
                assertEquals(GroupInputs.choice(port)?0:1,settings.getGLChildren().stream().filter(v->v instanceof GLTextEditor&&v.isVisible()).count());
                if(GroupInputs.choice(port)){
                    var option=settings.defaultChoice.getItems().get(1);settings.defaultChoice.setSelectedItem(option);assertEquals(option.id(),GroupInputs.template(ui.selectedPort()).initial());
                }else{
                    var field=settings.getGLChildren().stream().filter(v->v instanceof GLTextEditor&&v.isVisible()).map(GLTextEditor.class::cast).findFirst().orElseThrow();
                    var maximum=new NodeState(ui.selectedPort().presentation().editorOptions()).number("maximum",1);field.setText("1000000000");field.commit();assertEquals(maximum,new NodeState(ui.selectedPort().presentation().editorOptions()).number("default",0));
                    field.setText("0.25");field.commit();assertEquals(.25,new NodeState(ui.selectedPort().presentation().editorOptions()).number("default",0));
                    if(definition instanceof SpectrumNode){settings.initialUnit.setSelectedItem(UnitFormat.PERCENT);assertEquals(UnitFormat.PERCENT,GroupUnits.initial(ui.selectedPort(),UnitFormat.FACTOR));field.setText("50");field.commit();assertEquals(.5,new NodeState(ui.selectedPort().presentation().editorOptions()).number("default",0));}
                }
                settings.kind.setSelectedItem(GroupInputSettings.Kind.NONE);
                var removed=ui.selectedPort();assertEquals(GLNodeInputEditorRegistry.NONE,removed.presentation().inputEditorId());assertTrue(GroupEditorCopies.targets(removed).isEmpty());assertFalse(GroupInputs.choice(removed));assertEquals(port.publicPort().id(),removed.publicPort().id());
                var saved=c.snapshotDocument().embeddedAssets().get(a.id());assertTrue(saved.compositeDescriptor().propertyMappings().isEmpty());
                c.history().undo();assertEquals(GroupInputSettings.Kind.COPY,GroupInputSettings.selectedKind(ui.selectedPort()));assertEquals(2,GroupEditorCopies.targets(ui.selectedPort()).size());
            }});
        }
    }
    @Test void customStepEditorsCannotBeCopied(){
        var definition=new StepSwitchNode();var graph=new GLNodeGraph();graph.edit("Step switch",e->e.addNode(new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults())));var a=new GLNodeGraphAsset("test.steps",1,GLNodeGraphAssetKind.PROJECT,graph.snapshot(),GLNodeEditorViewState.empty());
        assertTrue(GroupInputSettings.availableSources(a,ProcessingRate.AUTO).stream().noneMatch(v->v.property().equals(StepSwitchNode.STEPS.value())));
        for(var row:definition.layout(new NodeState(definition.defaults())).rows())if(row.editor() instanceof Editor.Slider||row.editor() instanceof UnitSlider)assertFalse(row.isEditorCopyable());
    }
    @Test void connectedFrequencyIsCopiedInsteadOfUnrelatedStandaloneEditorAndDetaches()throws Exception{
        var holder=new WorkspaceController[1];
        SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();c.openDocument("Frequency",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");
            var curve=new CurveNode();c.workspace().currentGraph().edit("Other editors",e->e.addNode(new GLGraphNode(GLNodeId.random(),curve.registration().id(),curve.defaults())));
            var ui=c.workspace().interfaceEditor();GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0&&l.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(GLList.class::cast).findFirst().orElseThrow().setSelectedIndex(0);
            var settings=GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();settings.kind.setSelectedItem(GroupInputSettings.Kind.COPY);
            var p=ui.selectedPort();assertEquals(UnitFormat.HZ,GroupUnits.base(null,null,p));assertTrue(settings.initialUnit.getItems().contains(UnitFormat.HZ));assertFalse(settings.initialUnit.getItems().contains(UnitFormat.Q));assertFalse(settings.source.isVisible());
            var ed=c.workspace().editor();ed.removeConnection(ed.getConnections().stream().filter(w->w.getOutput().getId().equals("Pitch")).findFirst().orElseThrow());
        });
        SwingUtilities.invokeAndWait(()->{try(var c=holder[0]){var p=c.snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().stream().filter(v->v.publicPort().id().equals("Pitch")).findFirst().orElseThrow();assertTrue(p.internalEndpoints().isEmpty());assertEquals(GLNodeInputEditorRegistry.NONE,p.presentation().inputEditorId());assertFalse(p.presentation().editorOptions().containsKey("copySource"));}});
    }
    @Test void linkedDefaultsAreAReversibleDisplayProjection(){
        var a=asset(new CurveNode(),CurveNode.FROM_MIN.value());var n=a.graph().nodes().get(0);var p=a.compositeDescriptor().portMappings().get(0);var options=new HashMap<>(p.presentation().editorOptions());options.put("default",GLNodeValue.of(-7.));
        var d=a.compositeDescriptor();a=GroupInputs.descriptor(a,new GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),1,List.of(new GLCompositePortMapping(p.publicPort(),null,new GLCompositePortPresentation(p.presentation().inputEditorId(),false,options)))));
        assertEquals(-7.,GroupEditorCopies.previewDefaults(a,n).properties().get(CurveNode.FROM_MIN.value()).toJavaValue());assertEquals(n,GroupEditorCopies.previewDefaults(null,n));assertNotEquals(-7.,n.properties().get(CurveNode.FROM_MIN.value()).toJavaValue());
    }
    private static com.vingame.glswing.input.GLMouseEvent wireEvent(GLNodePort port,int kind,int x){
        var event=new java.awt.event.MouseEvent(new javax.swing.JPanel(),kind,0,java.awt.event.MouseEvent.BUTTON1_DOWN_MASK,x,30,1,false,java.awt.event.MouseEvent.BUTTON1);
        return new com.vingame.glswing.input.GLMouseEvent(event,new com.vingame.glswing.input.GLPickResult(1,port,x,30,x,30,1,0,0,1,0,0),0,0);
    }
    private static void wireCall(GLNodeEditor ed,String method,GLNodePort port,com.vingame.glswing.input.GLMouseEvent event,Boolean cancelled){
        try{var m=cancelled==null?GLNodeEditor.class.getDeclaredMethod(method,GLNodePort.class,com.vingame.glswing.input.GLMouseEvent.class):GLNodeEditor.class.getDeclaredMethod(method,GLNodePort.class,com.vingame.glswing.input.GLMouseEvent.class,boolean.class);m.setAccessible(true);if(cancelled==null)m.invoke(ed,port,event);else m.invoke(ed,port,event,cancelled);}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}
    }
    @Test void boundaryWireDragSurvivesEdtAndCancellationRestoresIt()throws Exception{
        for(boolean input:List.of(true,false)){
            var holder=new WorkspaceController[1];var source=new GLNodePort[1];
            SwingUtilities.invokeAndWait(()->{var c=holder[0]=new WorkspaceController();c.openDocument("Drag",GroupUnitsTest.document(false));c.workspace().openComposite("test.asset");var ed=c.workspace().editor();var wire=ed.getConnections().stream().filter(w->input?w.getOutput().getId().equals("Pitch"):w.getInput().getId().equals("Out")).findFirst().orElseThrow();source[0]=wire.getInput();wireCall(ed,"beginConnectionGesture",source[0],wireEvent(source[0],java.awt.event.MouseEvent.MOUSE_PRESSED,0),null);wireCall(ed,"dragConnectionGesture",source[0],wireEvent(source[0],java.awt.event.MouseEvent.MOUSE_DRAGGED,100),null);});
            SwingUtilities.invokeAndWait(()->{var ed=holder[0].workspace().editor();assertTrue(ed.isConnectionGestureActive());assertTrue(ed.getNodes().contains(source[0].getNode()));assertTrue(holder[0].snapshotDocument().embeddedAssets().get("test.asset").compositeDescriptor().portMappings().stream().allMatch(GLCompositePortMapping::isBound));wireCall(ed,"endConnectionGesture",source[0],wireEvent(source[0],java.awt.event.MouseEvent.MOUSE_RELEASED,100),true);});
            SwingUtilities.invokeAndWait(()->{try(var c=holder[0]){assertFalse(c.workspace().editor().isConnectionGestureActive());assertEquals(2,c.workspace().editor().getConnections().size());}});
        }
    }
    @Test void bakeRejectsUnsupportedCouplingsInsteadOfFreezingTheirValues(){
        var asset=asset(new FilterNode(),"compensate");var doc=document(asset,GLNodeValue.of(1.));
        var error=assertThrows(IllegalArgumentException.class,()->GroupSourceExporter.create(doc,doc.root().graph().nodes().get(0),true));
        assertEquals(AppText.text("group.editor.exportUnsupported"),error.getMessage());
    }
}
