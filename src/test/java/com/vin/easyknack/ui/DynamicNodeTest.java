package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vin.easyknack.model.MathOperation;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class DynamicNodeTest {
    @Test void processingRateSurvivesUndoAndComposites() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var workspace = controller.workspace(); var graph = workspace.currentGraph();
                var id = GLNodeId.random(); var definition = new MathNode();
                graph.edit("Math", e -> e.addNode(new GLGraphNode(id, definition.registration().id(), definition.defaults())));
                controller.setNodeProperty(id, MathNode.RATE, GLNodeValue.of("control"));
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ c cc"));
                assertEquals(ApplicationTheme.current().signal(SignalCategory.VALUE), view(controller, id).findPort("a").getColor());
                controller.history().undo();
                assertEquals("auto",graph.snapshot().findNode(id).orElseThrow().properties().get("rate").toJavaValue());
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("@ c cc"));
                controller.history().redo();
                workspace.editor().setSelectedNode(view(controller, id));
                controller.actions().copy(); controller.actions().paste(1200, 350);
                assertEquals(2, controller.audioGraph().split("@ c cc", -1).length - 1);
                controller.actions().extractComposite();
                controller.actions().openSelectedComposite();
                assertEquals(2, controller.audioGraph().split("@ c cc", -1).length - 1);
            }
        });
    }
    private GLNode view(WorkspaceController controller, GLNodeId id) {
        return controller.workspace().editor().getNodes().stream()
                .filter(n -> AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
    }
    @Test void dynamicPortsArePerInstanceAndUndoRestoresCablesAndPlacement() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var workspace = controller.workspace(); var graph = workspace.currentGraph();
                var first = GLNodeId.random(); var second = GLNodeId.random();
                var definition = new MathNode();
                graph.edit("Two math nodes", e -> {
                    e.addNode(new GLGraphNode(first, definition.registration().id(), definition.defaults()));
                    e.addNode(new GLGraphNode(second, definition.registration().id(), definition.defaults()));
                });
                view(controller, first).setLocation(1000, 250);
                var original = graph.snapshot().nodes().get(0).id();
                workspace.editor().connect(view(controller, original).findPort("audio-out"), view(controller, first).findPort("b"));
                var cable = graph.snapshot().connections().get(2);
                workspace.editor().setSelectedNode(view(controller, first));
                var combo = (GLComboBox<MathOperation>) view(controller, first).getContents().stream()
                        .filter(GLComboBox.class::isInstance).findFirst().orElseThrow();
                combo.setSelectedItem(MathOperation.ABSOLUTE);
                assertNull(view(controller, first).findPort("b"));
                assertNotNull(view(controller, second).findPort("b"));
                assertEquals(2, graph.snapshot().connections().size());
                assertEquals(1000f, view(controller, first).getX());
                assertEquals(List.of(view(controller, first)), workspace.editor().getSelectedNodes());
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("math -1 0.0 -1 1.0 absolute 0"));
                assertTrue(controller.history().undo());
                assertNotNull(view(controller, first).findPort("b"));
                assertTrue(graph.snapshot().connections().contains(cable));
                assertEquals(1000f, view(controller, first).getX());
                assertTrue(controller.history().redo());
                assertNull(view(controller, first).findPort("b"));
                controller.actions().copy(); controller.actions().paste(1300, 350);
                assertNull(workspace.editor().getSelectedNodes().get(0).findPort("b"));
                var pastedId = AudioNodeFactory.idOf(workspace.editor().getSelectedNodes().get(0)).orElseThrow();
                controller.actions().cut(); controller.history().undo();
                assertNull(view(controller, pastedId).findPort("b"));
                // A direct Core/inspector property edit must pass through the same reconciliation.
                graph.edit("Inspector operation", e -> e.setProperty(first, "operation", GLNodeValue.of("add")));
                assertNotNull(view(controller, first).findPort("b"));
                controller.history().undo();
                assertNull(view(controller, first).findPort("b"));
            }
        });
    }
    @Test void dynamicPortsRemainEditableInsideComposites() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var workspace = controller.workspace(); var graph = workspace.currentGraph();
                var id = GLNodeId.random(); var definition = new MathNode();
                graph.edit("Math", e -> e.addNode(new GLGraphNode(id, definition.registration().id(), definition.defaults())));
                workspace.editor().setSelectedNode(view(controller, id)); controller.actions().extractComposite();
                controller.actions().openSelectedComposite();
                var innerId = workspace.currentGraph().snapshot().nodes().get(0).id();
                controller.setNodeProperty(innerId, MathNode.OPERATION, GLNodeValue.of("negate"));
                assertNull(view(controller, innerId).findPort("b"));
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("negate"));
                workspace.navigateBack(); workspace.navigateForward();
                assertNull(view(controller, innerId).findPort("b"));
                controller.history().undo();
                assertNotNull(view(controller, innerId).findPort("b"));
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("multiply"));
            }
        });
    }
    @Test void muteRoutingIsExplicitAndCapabilityGuardKeepsCoreActions() {
        var types = new GLNodeTypeRegistry(); new NodeRegistry(types, new GLNodeCatalog());
        assertTrue(types.muteBypasses(new OscillatorNode().registration().id()).isEmpty(), "Frequency must never bypass an oscillator");
        assertEquals(List.of(new GLNodeMuteBypass("a", "audio-out")), types.muteBypasses(new MathNode().registration().id()));
        int[] calls = {0};
        var guarded = new NodeEditingActions(new GLNodeEditingActions() {
            public void copy() { calls[0]++; } public void cut() {} public void paste(float x, float y) {}
            public void duplicate() {} public void extractComposite() {} public void delete() {}
            public void toggleMuted() { fail("Non-muteable nodes must not reach the mute action"); }
        }, () -> false);
        assertFalse(guarded.canToggleMuted()); guarded.toggleMuted(); guarded.copy(); assertEquals(1, calls[0]);
    }
    @Test void removingAnExportedDynamicInputUpdatesCompositeAndUndoRestoresOuterCable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var workspace = controller.workspace(); var root = workspace.currentGraph();
                var id = GLNodeId.random(); var definition = new MathNode();
                root.edit("Math", e -> e.addNode(new GLGraphNode(id, definition.registration().id(), definition.defaults())));
                workspace.editor().connect(workspace.editor().getNodes().get(0).findPort("audio-out"), view(controller, id).findPort("b"));
                workspace.editor().setSelectedNode(view(controller, id)); controller.actions().extractComposite();
                assertEquals(3, root.snapshot().connections().size());
                var cable = root.snapshot().connections().get(2);
                controller.actions().openSelectedComposite();
                var inner = workspace.currentGraph().snapshot().nodes().get(0).id();
                controller.setNodeProperty(inner, MathNode.OPERATION, GLNodeValue.of("absolute"));
                assertEquals(2, root.snapshot().connections().size());
                assertDoesNotThrow(controller::audioGraph);
                controller.history().undo();
                assertNotNull(view(controller, inner).findPort("b"));
                assertTrue(root.snapshot().connections().contains(cable));
                assertDoesNotThrow(controller::audioGraph);
                controller.history().redo();
                assertEquals(2, root.snapshot().connections().size());
                assertDoesNotThrow(controller::audioGraph);
            }
        });
    }
    @Test void unboundedSliderUsesCoreFreeModeWithOptionalBounds() throws Exception {
        var unbounded = Editor.Slider.button(1, null, null);
        assertDoesNotThrow(() -> unbounded.validate(GLNodeValue.of(1e20)));
        var lowerBounded = Editor.Slider.button(1, 0.0, null);
        assertThrows(IllegalArgumentException.class, () -> lowerBounded.validate(GLNodeValue.of(-1)));
        assertDoesNotThrow(() -> lowerBounded.validate(GLNodeValue.of(1e20)));
        assertThrows(IllegalArgumentException.class, () -> Editor.Slider.button(0, 1.0, 0.0));
    }
}

