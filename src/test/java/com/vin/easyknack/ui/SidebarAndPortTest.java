package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.ui.*;

class SidebarAndPortTest {
    @Test void unitsFollowTheNumericValueInNodesAndPropertySchemas() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var registry = new NodeRegistry(new GLNodeTypeRegistry(), new GLNodeCatalog());
            for (BasicNode definition : List.of(new OscillatorNode(), new AdditiveOscillatorNode(), new GainNode())) {
                var model = new GLGraphNode(GLNodeId.random(), definition.registration().id(), definition.defaults());
                var layout = definition.layout(new NodeState(model.properties()));
                var factory = new AudioNodeFactory((id, key, value) -> {}, new GLHistoryManager(), () -> {});
                var view = factory.createNode(model, new GLNodeTypeDescriptor(model.typeId(), layout.ports()));
                for (var row : layout.rows()) if (row.editor() instanceof UnitSlider spec) {
                    var slider = view.getContents().stream().filter(c -> c instanceof GLSlider s && s.getText().equals(row.label()))
                            .map(GLSlider.class::cast).findFirst().orElseThrow();
                    assertFalse(slider.getText().contains("("));
                    assertEquals(spec.selected().suffix(), slider.getUnit());
                    assertEquals("-12", slider.getValueFormatter().apply(-12));
                    var property = (GLCustomNodeProperty) registry.schemas().properties(model.typeId()).stream()
                            .filter(p -> p.id().equals(row.id())).findFirst().orElseThrow();
                    assertEquals(row.id(), property.id());
                }
            }
            try (var controller = new WorkspaceController()) {
                var workspace = controller.workspace();
                workspace.editor().setSelectedNode(workspace.editor().getNodes().stream().filter(n -> n.getTitle().equals("Gain")).findFirst().orElseThrow());
                var inspector = workspace.propertyInspector(); inspector.refresh();
                var slider = inspector.getGLChildren().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).findFirst().orElseThrow();
                assertEquals("Gain", slider.getText());
                assertEquals("×", slider.getUnit());
                assertEquals("-12", slider.getValueFormatter().apply(-12));
            }
        });
    }
    @Test void rateIsAPropertyWithoutANodeControl() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var math = new MathNode();
            var model = new GLGraphNode(GLNodeId.random(), math.registration().id(), math.defaults());
            var layout = math.layout(new NodeState(model.properties()));
            assertTrue(layout.rows().stream().filter(r -> r.key().equals(MathNode.RATE)).findFirst().orElseThrow().isInspectorOnly());
            assertEquals(GLNodeValue.of("auto"), layout.defaults().get("rate"));
            var registry = new NodeRegistry(new GLNodeTypeRegistry(), new GLNodeCatalog());
            assertTrue(registry.schemas().properties(model.typeId()).stream().anyMatch(p -> p.id().equals("rate")));
            var factory = new AudioNodeFactory((id, key, value) -> {}, new GLHistoryManager(), () -> {});
            var view = factory.createNode(model, new GLNodeTypeDescriptor(model.typeId(), layout.ports()));
            assertEquals(1, view.getContents().stream().filter(GLComboBox.class::isInstance).count(), "Only Operation is drawn in Math");
            assertThrows(IllegalStateException.class, () -> new NodeBuilder().addInput(MathNode.A, "A").inspectorOnly());
        });
    }

    @Test void coreGroupAndBoundaryViewsReceiveTheirPublicColors() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var types = new GLNodeTypeRegistry(); var composites = new GLCompositeGraphRegistry();
            var contract = new GLCompositeNodeDescriptor("test.group", "test.asset", 1, List.of(
                    new GLCompositePortMapping(GLNodePortDescriptor.output("out", SignalCategory.VALUE.valueTypeId()), null)));
            types.register(contract.nodeType());
            composites.registerAsset(new com.vingame.glswing.node.document.GLNodeGraphAsset(
                    "test.asset", 1, com.vingame.glswing.node.document.GLNodeGraphAssetKind.COMPOSITE,
                    new GLNodeGraph().snapshot(), com.vingame.glswing.node.binding.GLNodeEditorViewState.empty(), Map.of(), contract));
            var history = new GLHistoryManager();
            try (var workspace = new GLNodeWorkspace(types, composites, new GLNodeCatalog(),
                    new AudioNodeFactory((id, key, value) -> {}, history, () -> {}), GLGraphCyclePolicy.ACYCLIC, history)) {
                var colors = new CompositePortColors(workspace, composites);
                try {
                    var graph = new GLNodeGraph();
                    graph.edit("Groups", e -> {
                        e.addNode(new GLGraphNode(GLNodeId.random(), "test.group", Map.of()));
                        e.addNode(new GLGraphNode(GLNodeId.random(), "test.group", Map.of()));
                    });
                    workspace.openRoot("Test", graph, com.vingame.glswing.node.binding.GLNodeEditorViewState.empty());
                    colors.refresh();
                    assertEquals(2, workspace.editor().getNodes().size());
                    for (var node : workspace.editor().getNodes()) assertEquals(ApplicationTheme.current().signal(SignalCategory.VALUE), node.findPort("out").getColor());
                    assertTrue(workspace.openComposite("test.asset")); colors.refresh();
                    assertEquals(1, workspace.editor().getNodes().size());
                    assertEquals(ApplicationTheme.current().signal(SignalCategory.VALUE), workspace.editor().getNodes().get(0).findPort("out").getColor());
                } finally { colors.close(); }
            }
        });
    }

    @Test void groupColorsUseOnlyTheExplicitPublicPortType() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var group = new GLNode("Group");
            group.addInput("in", new GLLabel("In")); group.addOutput("out", new GLLabel("Out"));
            var descriptor = new GLCompositeNodeDescriptor("test.group", "test.asset", 1, List.of(
                    new GLCompositePortMapping(GLNodePortDescriptor.input("in", SignalCategory.VALUE.valueTypeId(), false, 1), null),
                    new GLCompositePortMapping(GLNodePortDescriptor.output("out", SignalCategory.VALUE.valueTypeId()),
                            new GLNodeEndpoint(GLNodeId.random(), "unrelated-inner-port"))));
            CompositePortColors.apply(group, descriptor);
            assertEquals(ApplicationTheme.current().signal(SignalCategory.VALUE), group.findPort("in").getColor());
            assertEquals(ApplicationTheme.current().signal(SignalCategory.VALUE), group.findPort("out").getColor());
        });
    }
}


