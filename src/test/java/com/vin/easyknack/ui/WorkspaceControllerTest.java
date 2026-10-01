package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Canvas;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vingame.glswing.input.GLKeyEvent;
import com.vingame.glswing.node.model.GLNodeState;

class WorkspaceControllerTest {
    @Test void spectrumCanConnectToAnalysisViewerFromEitherPort() throws Exception {
        onWorkspace(controller -> {
            var workspace=controller.workspace();
            workspace.currentGraph().edit("Add analysis", edit -> {
                for(var definition:List.of(new com.vin.easyknack.nodes.SpectrumNode(),new com.vin.easyknack.nodes.SpectrumViewerNode()))
                    edit.addNode(new com.vingame.glswing.node.model.GLGraphNode(com.vingame.glswing.node.model.GLNodeId.random(),definition.registration().id(),definition.defaults()));
            });
            var fft=workspace.editor().getNodes().stream().filter(n->n.getTitle().equals("FFT")).findFirst().orElseThrow();
            var viewer=workspace.editor().getNodes().stream().filter(n->n.getTitle().equals("Analysis Viewer")).findFirst().orElseThrow();
            var output=fft.findPort("analysis");var input=viewer.findPort("analysis");
            assertTrue(output.canConnectTo(input));assertTrue(input.canConnectTo(output));
            assertNotNull(workspace.editor().connect(output,input));
            assertDoesNotThrow(controller::audioGraph);
        });
    }
    @Test void mathControlsAreModelBoundAndSliderPortsUseSignals() throws Exception {
        onWorkspace(controller -> {
            var workspace = controller.workspace();
            var graph = workspace.currentGraph();
            var id = com.vingame.glswing.node.model.GLNodeId.random();
            graph.edit("Add math", edit -> edit.addNode(new com.vingame.glswing.node.model.GLGraphNode(id,
                    new com.vin.easyknack.nodes.MathNode().registration().id(),
                    new com.vin.easyknack.nodes.MathNode().defaults())));
            var math = workspace.editor().getNodes().stream().filter(n -> n.getTitle().equals("Math")).findFirst().orElseThrow();
            var combo = (com.vingame.glswing.ui.GLComboBox<com.vin.easyknack.model.MathOperation>) math.getContents().stream()
                    .filter(com.vingame.glswing.ui.GLComboBox.class::isInstance).findFirst().orElseThrow();
            var sliders = math.getContents().stream().filter(com.vingame.glswing.ui.GLSlider.class::isInstance)
                    .map(com.vingame.glswing.ui.GLSlider.class::cast).toList();
            var a = sliders.stream().filter(s -> s.getText().equals("A")).findFirst().orElseThrow();
            var b = sliders.stream().filter(s -> s.getText().equals("B")).findFirst().orElseThrow();
            var clamp = (com.vingame.glswing.ui.GLCheckBox) math.getContents().stream()
                    .filter(com.vingame.glswing.ui.GLCheckBox.class::isInstance).findFirst().orElseThrow();
            a.setValue(2); b.setValue(3);
            combo.setSelectedItem(com.vin.easyknack.model.MathOperation.ADD);
            clamp.doClick();
            assertEquals(2.0, graph.snapshot().findNode(id).orElseThrow().properties().get("a").toJavaValue());
            assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("math -1 2.0 -1 3.0 add 1"));
            assertTrue(controller.history().undo());
            assertFalse(clamp.isSelected());
            assertTrue(controller.history().redo());
            assertTrue(clamp.isSelected());
            var oscillator = workspace.editor().getNodes().get(0);
            var frequency = (com.vingame.glswing.ui.GLSlider) oscillator.getContents().stream()
                    .filter(com.vingame.glswing.ui.GLSlider.class::isInstance).findFirst().orElseThrow();
            frequency.setValue(880);
            assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("osc -1 880.0"));
            var connection = workspace.editor().connect(math.findPort("audio-out"), oscillator.findPort("frequency"));
            assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("osc 3 880.0"));
            assertTrue(oscillator.findPort("frequency").isContentHiddenByConnection());
            workspace.editor().removeConnection(connection);
            assertFalse(oscillator.findPort("frequency").isContentHiddenByConnection());
            assertEquals(880, frequency.getValue());
            workspace.editor().setSelectedNode(math);
            controller.actions().duplicate();
            assertEquals(2, graph.snapshot().nodes().stream().filter(n -> n.typeId().endsWith("math")
                    && n.properties().get("operation").toJavaValue().equals("add")
                    && n.properties().get("clamp").toJavaValue().equals(true)).count());
        });
    }
    @Test void audioCompilationUsesRootGraphAndResolvesCompositesAndMute() throws Exception {
        onWorkspace(controller -> {
            var workspace = controller.workspace();
            String initial = controller.audioGraph();
            assertTrue(initial.startsWith("GRAPH 3 "));
            assertTrue(initial.replaceAll(" ![0-9a-f]{16} "," ").contains("osc -1 440.0"));
            assertTrue(initial.replaceAll(" ![0-9a-f]{16} "," ").contains("gain 0 -12.0"));
            workspace.editor().setSelectedNode(workspace.editor().getNodes().get(1));
            controller.actions().toggleHidden();
            assertEquals(initial, controller.audioGraph(), "Hide must not change audio");
            controller.actions().toggleMuted();
            assertTrue(controller.audioGraph().startsWith("GRAPH 2 "));
            controller.history().undo();
            controller.history().undo();
            workspace.editor().setSelectedNodes(workspace.editor().getNodes().subList(0, 2));
            controller.actions().extractComposite();
            assertTrue(controller.audioGraph().startsWith("GRAPH 3 "));
            String compositeGraph = controller.audioGraph();
            controller.actions().openSelectedComposite();
            assertEquals(compositeGraph, controller.audioGraph(), "Navigation must not change root audio");
            var oscillator = workspace.currentGraph().snapshot().nodes().stream()
                    .filter(n -> n.typeId().endsWith("oscillator")).findFirst().orElseThrow();
            workspace.currentGraph().edit("Frequency", edit -> edit.setProperty(oscillator.id(), "frequency",
                    com.vingame.glswing.node.model.GLNodeValue.of(880.0)));
            assertTrue(controller.audioGraph().replaceAll(" ![0-9a-f]{16} "," ").contains("osc -1 880.0"));
        });
    }
    private void onWorkspace(java.util.function.Consumer<WorkspaceController> test) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) { test.accept(controller); }
        });
    }

    @Test void examplePreservesLayoutAndModelConnections() throws Exception {
        onWorkspace(controller -> {
            var workspace = controller.workspace();
            var editor = workspace.editor();
            assertEquals(3, workspace.currentGraph().snapshot().nodes().size());
            assertEquals(2, workspace.currentGraph().snapshot().connections().size());
            assertEquals(2, editor.getConnections().size());
            assertEquals(List.of(70f, 390f, 710f), editor.getNodes().stream().map(n -> n.getX()).toList());
            assertEquals(List.of(140f, 200f, 140f), editor.getNodes().stream().map(n -> n.getY()).toList());
            editor.setSelectedNode(editor.getNodes().get(1));
            controller.actions().delete();
            assertEquals(2, workspace.currentGraph().snapshot().nodes().size());
            assertTrue(workspace.currentGraph().snapshot().connections().isEmpty());
            assertTrue(controller.history().undo());
            assertEquals(2, workspace.currentGraph().snapshot().connections().size());
        });
    }

    @Test void clipboardActionsPreserveInternalConnectionsAndCreateFreshIds() throws Exception {
        onWorkspace(controller -> {
            var workspace = controller.workspace();
            var editor = workspace.editor();
            var originalIds = workspace.currentGraph().snapshot().nodesById().keySet();
            editor.setSelectedNodes(editor.getNodes());
            assertTrue(controller.actions().canCopy());
            controller.actions().copy();
            assertTrue(controller.actions().canPaste());
            controller.actions().paste(1000, 300);
            assertEquals(6, workspace.currentGraph().snapshot().nodes().size());
            assertEquals(4, workspace.currentGraph().snapshot().connections().size());
            assertEquals(6, workspace.currentGraph().snapshot().nodesById().size());
            controller.actions().cut();
            assertEquals(originalIds, workspace.currentGraph().snapshot().nodesById().keySet());
            controller.paste();
            assertEquals(6, workspace.currentGraph().snapshot().nodes().size());
            editor.confirmPlacement();
            assertTrue(controller.history().undo());
            assertEquals(3, workspace.currentGraph().snapshot().nodes().size());
            assertTrue(controller.history().redo());
            assertEquals(6, workspace.currentGraph().snapshot().nodes().size());
        });
    }

    @Test void duplicateAndInteractivePlacementUseModelActions() throws Exception {
        onWorkspace(controller -> {
            var workspace = controller.workspace();
            workspace.editor().setSelectedNodes(workspace.editor().getNodes());
            controller.actions().duplicate();
            assertEquals(6, workspace.currentGraph().snapshot().nodes().size());
            assertEquals(4, workspace.currentGraph().snapshot().connections().size());
            assertEquals(List.of(70f, 390f, 710f), workspace.editor().getSelectedNodes()
                    .stream().map(n -> n.getX()).sorted().toList());
            workspace.editor().confirmPlacement();
            var placement = controller.actions().beginDuplicate(500, 500);
            assertNotNull(placement);
            placement.moveTo(600, 500);
            placement.cancel();
            assertEquals(6, workspace.currentGraph().snapshot().nodes().size());
        });
    }

    @Test void hideAndMutePersistInModelAndUndo() throws Exception {
        onWorkspace(controller -> {
            var workspace = controller.workspace();
            workspace.editor().setSelectedNode(workspace.editor().getNodes().get(1));
            controller.actions().toggleHidden();
            assertEquals(1, workspace.currentGraph().snapshot().nodes().stream().filter(GLNodeState::isHidden).count());
            controller.actions().toggleMuted();
            assertEquals(1, workspace.currentGraph().snapshot().nodes().stream().filter(GLNodeState::isMuted).count());
            assertTrue(controller.history().undo());
            assertTrue(workspace.currentGraph().snapshot().nodes().stream().noneMatch(GLNodeState::isMuted));
            assertTrue(controller.history().undo());
            assertTrue(workspace.currentGraph().snapshot().nodes().stream().noneMatch(GLNodeState::isHidden));
        });
    }

    @Test void compositeExtractionAndNavigationKeepGraphAndLayout() throws Exception {
        onWorkspace(controller -> {
            var workspace = controller.workspace();
            var root = workspace.currentGraph();
            workspace.editor().setSelectedNodes(workspace.editor().getNodes().subList(0, 2));
            assertTrue(controller.actions().canExtractComposite());
            controller.actions().extractComposite();
            assertEquals(2, root.snapshot().nodes().size());
            assertEquals(1, root.snapshot().connections().size());
            assertTrue(controller.actions().canOpenSelectedComposite());
            controller.actions().openSelectedComposite();
            assertNotSame(root, workspace.currentGraph());
            assertEquals(2, workspace.currentGraph().snapshot().nodes().size());
            assertEquals(1, workspace.currentGraph().snapshot().connections().size());
            assertTrue(workspace.canNavigateBack());
            assertTrue(workspace.navigateBack());
            assertSame(root, workspace.currentGraph());
            assertTrue(workspace.navigateForward());
            assertTrue(workspace.navigateToDepth(0));
            var output = workspace.editor().getNodes().stream()
                    .filter(n -> n.getTitle().equals("Audio Output")).findFirst().orElseThrow();
            assertEquals(710f, output.getX());
            assertEquals(140f, output.getY());
        });
    }

    @Test void copyShortcutReachesWorkspaceClipboard() throws Exception {
        onWorkspace(controller -> {
            var editor = controller.workspace().editor();
            editor.setSelectedNodes(editor.getNodes());
            var event = new GLKeyEvent(new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_C, 'c'));
            editor.keyPressed(event);
            assertTrue(event.isConsumed());
            assertTrue(controller.actions().canPaste());
            controller.actions().paste(1000, 400);
            assertEquals(6, controller.workspace().currentGraph().snapshot().nodes().size());
        });
    }
}

