package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLSlider;

class NodeConsistencyTest {
    @Test void inspectorCanSwitchBackFromBoundedBarToFreeButtons() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try(var c=new WorkspaceController()){
                var d=new ValueNode();var id=GLNodeId.random();var ws=c.workspace();
                ws.currentGraph().edit("Value",e->e.addNode(new GLGraphNode(id,d.registration().id(),d.defaults())));
                ws.editor().setSelectedNode(ws.editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow());
                java.util.function.Supplier<GLSlider> slider=()->(GLSlider)ws.propertyInspector().getGLChildren().stream().filter(n->n instanceof InspectorEditor&&n.getName().equals("value")).map(InspectorEditor.class::cast).findFirst().orElseThrow().control();
                c.setNodeProperty(id,ValueNode.STYLE,GLNodeValue.of("bar"));
                assertEquals(GLSlider.Presentation.BAR,slider.get().getPresentation());
                c.setNodeProperty(id,ValueNode.STYLE,GLNodeValue.of("buttons"));
                c.setNodeProperty(id,ValueNode.USE_MIN,GLNodeValue.of(false));c.setNodeProperty(id,ValueNode.USE_MAX,GLNodeValue.of(false));
                assertEquals(GLSlider.Presentation.BUTTONS,slider.get().getPresentation());
                assertTrue(slider.get().getValueMinimum().isEmpty());assertTrue(slider.get().getValueMaximum().isEmpty());
                slider.get().setValue(-12.5);assertEquals(-12.5,ws.currentGraph().snapshot().findNode(id).orElseThrow().properties().get("value").toJavaValue());
            }
        });
    }
    @Test void everyBuiltinEditorHasInspectorMetadataAndValidHints() {
        var registry=new NodeRegistry(new GLNodeTypeRegistry(),new GLNodeCatalog());
        for(var definition:NodeRegistry.builtinDefinitions()) {
            var model=registry.normalize(new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()));
            var properties=registry.schemas().properties(model.typeId()).stream().map(GLNodePropertyDescriptor::id).toList();
            for(var row:definition.layout(new NodeState(model.properties())).rows()) {
                assertFalse(row.label().isBlank(),definition.title()+"/"+row.id());
                assertFalse(NodeHints.tooltip(row).isBlank());
                if(row.editor()!=null&&!row.isMenuOnly()&&row.isInspectorVisible())assertTrue(properties.contains(row.id()),definition.title()+"/"+row.id());
            }
        }
    }
    @Test void inspectorRefreshDoesNotWriteBackAndUsesCurrentBounds() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var definition=new ValueNode();var model=new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()).withProperty("value",GLNodeValue.of(.75));
            var row=definition.layout(new NodeState(model.properties())).rows().stream().filter(r->r.key().equals(ValueNode.VALUE)).findFirst().orElseThrow();
            var property=InspectorEditor.property(row,definition);var writes=new ArrayList<GLNodeValue>();
            var inspector=(InspectorEditor)property.create().apply(model,writes::add);
            assertTrue(writes.isEmpty());
            var changed=model.withProperty("use-maximum",GLNodeValue.of(true)).withProperty("maximum",GLNodeValue.of(.5)).withProperty("value",GLNodeValue.of(.5));
            property.update().accept(inspector,changed);
            var slider=(GLSlider)inspector.control();assertEquals(.5,slider.getValue());assertEquals(.5,slider.getValueMaximum().orElseThrow());
            assertTrue(slider.getToolTipText().contains("0.5"));assertTrue(writes.isEmpty());
            slider.setValue(.25);assertEquals(List.of(GLNodeValue.of(.25)),writes);
        });
    }
    @Test void referenceHintsDoNotDescribeNumericControlRate(){
        var definition=new HistoryBufferNode();
        var row=definition.layout(new NodeState(definition.defaults())).rows().stream().filter(r->r.kind()==NodeBuilder.RowKind.OUTPUT).findFirst().orElseThrow();
        assertTrue(NodeHints.tooltip(row).contains("reference"));assertFalse(NodeHints.tooltip(row).contains("ControlRate"));
    }
    @Test void valueSettingsSurviveGroupingAndDocumentRoundTrip() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try(var controller=new WorkspaceController()){
                var definition=new ValueNode();var id=GLNodeId.random();var ws=controller.workspace();
                ws.currentGraph().edit("Value",e->e.addNode(new GLGraphNode(id,definition.registration().id(),definition.defaults())));
                controller.setNodeProperty(id,ValueNode.STYLE,GLNodeValue.of("bar"));
                controller.setNodeProperty(id,ValueNode.MIN,GLNodeValue.of(-2.));controller.setNodeProperty(id,ValueNode.MAX,GLNodeValue.of(3.));
                controller.setNodeProperty(id,ValueNode.VALUE,GLNodeValue.of(1.25));
                ws.editor().setSelectedNode(ws.editor().getNodes().stream().filter(n->AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow());
                controller.actions().extractComposite();
                var before=controller.snapshotDocument();var codec=new KnackDocumentCodec();var bytes=new java.io.ByteArrayOutputStream();codec.write(before,bytes);
                var loaded=codec.read(new java.io.ByteArrayInputStream(bytes.toByteArray()),codec.currentSchemaVersion());
                assertEquals(before,loaded);controller.openDocument("Roundtrip",loaded);
                var restored=controller.snapshotDocument().embeddedAssets().values().stream().flatMap(a->a.graph().nodes().stream()).filter(n->n.id().equals(id)).findFirst().orElseThrow();
                assertEquals("bar",restored.properties().get("slider-style").toJavaValue());assertEquals(1.25,restored.properties().get("value").toJavaValue());
                assertEquals(-2.,restored.properties().get("minimum").toJavaValue());assertEquals(3.,restored.properties().get("maximum").toJavaValue());
            }catch(Exception e){throw new AssertionError(e);}
        });
    }
}
