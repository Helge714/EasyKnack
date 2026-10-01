package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.GLSlider;

class AdditiveOscillatorTest {
    @Test void connectedPartialCountShowsHarmonicOptionsAndUndoHidesThem() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var graph = controller.workspace().currentGraph();
                var id = GLNodeId.random(); var source = GLNodeId.random();
                var definition = new AdditiveOscillatorNode(); var value = new ValueNode();
                graph.edit("Add nodes", e -> {
                    e.addNode(new GLGraphNode(id, definition.registration().id(), definition.defaults()));
                    e.addNode(new GLGraphNode(source, value.registration().id(), value.defaults()));
                });
                var editor = controller.workspace().editor();
                var view = editor.getNodes().stream().filter(n -> AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                var falloff = view.getContents().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).filter(s -> s.getText().equals("Falloff")).findFirst().orElseThrow();
                var spread = view.getContents().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).filter(s -> s.getText().equals("Spread")).findFirst().orElseThrow();
                assertFalse(falloff.isVisible()); assertFalse(spread.isVisible());
                var connection = new GLGraphConnection(GLNodeConnectionId.random(), new GLNodeEndpoint(source, ValueNode.OUT.value()), new GLNodeEndpoint(id, AdditiveOscillatorNode.HARMONICS.value()));
                controller.history().perform("Connect partials",
                        () -> graph.edit("Connect partials", e -> e.addConnection(connection)),
                        () -> graph.edit("Disconnect partials", e -> e.removeConnection(connection.id())));
                assertTrue(falloff.isVisible()); assertTrue(spread.isVisible());
                controller.history().undo();
                var restored = editor.getNodes().stream().filter(n -> AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                assertEquals(0, restored.findPort("harmonics").getConnectionCount());
                assertFalse(restored.getContents().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).filter(s -> s.getText().equals("Falloff") || s.getText().equals("Spread")).anyMatch(s -> s.isVisible()));
                controller.history().redo();
                restored = editor.getNodes().stream().filter(n -> AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                assertTrue(restored.getContents().stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).filter(s -> s.getText().equals("Falloff") || s.getText().equals("Spread")).allMatch(s -> s.isVisible()));
            }
        });
    }
    @Test void partialSliderContinuesDraggingWhenExtraRowsAppear() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var definition = new AdditiveOscillatorNode(); var id = GLNodeId.random();
                var workspace = controller.workspace();
                workspace.currentGraph().edit("Add", edit -> edit.addNode(new GLGraphNode(id, definition.registration().id(), definition.defaults())));
                var node = workspace.editor().getNodes().stream().filter(n -> AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                var slider = node.getContents().stream().filter(c -> c instanceof GLSlider s && s.getText().equals("Partials")).map(GLSlider.class::cast).findFirst().orElseThrow();
                float originalHeight = node.getHeight();
                slider.mousePressed(mouse(slider, java.awt.event.MouseEvent.MOUSE_PRESSED, 0));
                slider.mouseDragged(mouse(slider, java.awt.event.MouseEvent.MOUSE_DRAGGED, .2f));
                double first = slider.getValue(); assertTrue(first > 1);
                slider.mouseDragged(mouse(slider, java.awt.event.MouseEvent.MOUSE_DRAGGED, .8f));
                slider.mouseReleased(mouse(slider, java.awt.event.MouseEvent.MOUSE_RELEASED, .8f));
                assertTrue(slider.getValue() > first, "Drag must continue after showing Falloff and Spread");
                assertTrue(workspace.editor().getNodes().contains(node));
                assertTrue(node.findPort("falloff").isVisible());
                assertTrue(node.getHeight() > originalHeight, "New rows must fit inside the node bounds");
            }
        });
    }
    private static com.vingame.glswing.input.GLMouseEvent mouse(GLSlider slider, int kind, float position) {
        float x = slider.getWidth() * position, y = slider.getHeight() / 2;
        return new com.vingame.glswing.input.GLMouseEvent(new java.awt.event.MouseEvent(new javax.swing.JPanel(), kind, 0, 0,
                (int)x, (int)y, 1, false, java.awt.event.MouseEvent.BUTTON1),
                new com.vingame.glswing.input.GLPickResult(0, slider, x, y, x, y, 1, 0, 0, 1, 0, 0), kind == java.awt.event.MouseEvent.MOUSE_DRAGGED ? 10 : 0, 0);
    }
    @Test void valueAndWaveformPortsExposeTheirContracts() {
        var value = new ValueNode();
        var values = new HashMap<>(value.defaults()); values.put("value", GLNodeValue.of(-1234567.5));
        assertEquals("-1234567.5", value.encode(new NodeState(values), p -> -1));
        assertEquals(ProcessingRate.CONTROL, value.layout(new NodeState(values)).rows().get(0).rate());
        for (BasicNode oscillator : List.of(new OscillatorNode(), new AdditiveOscillatorNode())) {
            var rows = oscillator.layout(new NodeState(oscillator.defaults())).rows();
            var wave = rows.stream().filter(r -> r.id().equals("waveform")).findFirst().orElseThrow();
            assertEquals(SignalCategory.WAVETABLE, wave.signal());
            assertEquals(ProcessingRate.CONTROL, wave.rate());
            assertEquals(PortDataType.WAVEFORM.id(), oscillator.layout(new NodeState(oscillator.defaults())).ports().stream()
                    .filter(p -> p.id().equals("waveform")).findFirst().orElseThrow().valueTypeId());
        }
        var additive = new AdditiveOscillatorNode();
        values = new HashMap<>(additive.defaults()); values.put("spread", GLNodeValue.of(-100));
        var rows = additive.layout(new NodeState(values)).rows();
        assertEquals(NodeBuilder.RowKind.OUTPUT, rows.get(0).kind());
        assertEquals("waveform", rows.stream().filter(r -> r.kind() == NodeBuilder.RowKind.INPUT).findFirst().orElseThrow().id());
        assertEquals(com.vingame.glswing.node.GLNodePortOrder.OUTPUTS_FIRST, additive.portOrder());
        for (String id : List.of("harmonics", "falloff", "spread")) {
            var row = rows.stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
            assertEquals(NodeBuilder.RowKind.INPUT, row.kind());
            assertEquals(id.equals("harmonics"), (row.editor() instanceof com.vin.easyknack.nodes.UnitSlider unit ? unit.spec() : (Editor.Slider)row.editor()).bar());
        }
    }
    @Test void validatesAndEncodesAllParameters() {
        var node = new AdditiveOscillatorNode();
        assertEquals("-1 sine 1 1.0 0.0 -1 440.0 -1 0.0 inputs -1 -1 -1 -1", node.encode(new NodeState(node.defaults()), p -> -1));
        for (double invalid : new double[] {0, 145, 1.5}) {
            var values = new HashMap<>(node.defaults()); values.put("harmonics", GLNodeValue.of(invalid));
            assertThrows(IllegalArgumentException.class, () -> node.layout(new NodeState(values)));
        }
        assertTrue(node.defaults().keySet().containsAll(List.of("falloff", "spread")));
        assertNotEquals(PortDataType.SIGNAL.id(), PortDataType.WAVEFORM.id());
    }
    @Test void conditionalEditorsKeepTheirIdentityAndUndoWithoutLosingValuesOrCables() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var workspace = controller.workspace(); var graph = workspace.currentGraph();
                var id = GLNodeId.random(); var definition = new AdditiveOscillatorNode();
                graph.edit("Additive", e -> e.addNode(new GLGraphNode(id, definition.registration().id(), definition.defaults())));
                java.util.function.Supplier<com.vingame.glswing.node.GLNode> view = () -> workspace.editor().getNodes().stream()
                        .filter(n -> AudioNodeFactory.idOf(n).filter(id::equals).isPresent()).findFirst().orElseThrow();
                var original = view.get(); long small = original.getContents().stream().filter(c -> c.isVisible()).count();
                assertFalse(definition.layout(new NodeState(definition.defaults())).rows().stream().anyMatch(row -> row.id().equals("noise")));
                workspace.editor().connect(workspace.editor().getNodes().get(0).findPort("audio-out"), original.findPort("frequency"));
                var cables = graph.snapshot().connections();
                small = original.getContents().stream().filter(c -> c.isVisible()).count();
                controller.setNodeProperty(id, AdditiveOscillatorNode.HARMONICS, GLNodeValue.of(144));
                assertSame(original, view.get(), "Visibility changes must preserve the dragged slider");
                assertEquals(small + 2, view.get().getContents().stream().filter(c -> c.isVisible()).count());
                assertEquals(cables, graph.snapshot().connections());
                controller.setNodeProperty(id, AdditiveOscillatorNode.FALLOFF, GLNodeValue.of(2));
                controller.setNodeProperty(id, AdditiveOscillatorNode.HARMONICS, GLNodeValue.of(1));
                assertEquals(small, view.get().getContents().stream().filter(c -> c.isVisible()).count());
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("sine 1 2.0 0.0"));
                controller.history().undo();
                assertSame(original, view.get(), "Visibility changes must preserve the dragged slider");
                assertEquals(small + 2, view.get().getContents().stream().filter(c -> c.isVisible()).count());
                assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("sine 144 2.0 0.0"));
                assertEquals(cables, graph.snapshot().connections());
            }
        });
    }
}




