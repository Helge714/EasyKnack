package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.function.ToIntFunction;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.history.GLHistoryManager;
import com.vingame.glswing.node.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.ui.*;

class NodePresentationTest {
    @RegisterNode(id = "test.presentation", title = "Test", category = NodeCategory.MATH, kind = NodeKind.VALUE)
    public static final class PresentationNode extends BasicNode {
        boolean createdView;
        @Override public BasicNodeView createView(NodeViewContext context,GLGraphNode model) {
            createdView=true;
            return new BasicNodeView(this,context,model);
        }
        static final PropertyId IN = new PropertyId("in"), OUT = new PropertyId("out"), OPTIONAL = new PropertyId("optional");
        static final PropertyId PIN = new PropertyId("pin"), OTHER = new PropertyId("other");
        @Override public void define(NodeBuilder node, NodeState state) {
            node.addOutput(OUT, "Out").keepVisibleOnHide();
            node.addInput(IN, "In", Editor.Slider.button(0, null, null)).keepVisibleOnHide();
            node.addInput(OPTIONAL, "Optional");
            node.addEditor(PIN, "Pinned", Editor.checkBox(true)).keepVisibleOnHide(state.flag(PIN, true));
            node.addEditor(OTHER, "Other", Editor.checkBox(false));
        }
        @Override public NativeProcessor processor() { return new NativeProcessor("math", "nodes/MathNode.hpp"); }
        @Override public String encode(NodeState state, ToIntFunction<PropertyId> input) { return ""; }
    }
    @Test void allRowKindsCanStayVisibleAndStateChangesInvalidateTheProjection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var definition = new PresentationNode();
            var model = new GLGraphNode(GLNodeId.random(), definition.registration().id(), definition.defaults());
            var type = new GLNodeTypeDescriptor(model.typeId(), definition.layout(new NodeState(model.properties())).ports());
            List<PropertyId> changes = new ArrayList<>();
            var factory = new AudioNodeFactory((id, key, value) -> changes.add(key), new GLHistoryManager(), () -> {}, id -> definition);
            var view = factory.createNode(model, type); view.packGL();
            assertTrue(definition.createdView,"BasicNode must own view creation");
            var editor = new GLNodeEditor(); editor.addNode(view, 0, 0);
            var controls = view.getContents();
            var pinned = controls.stream().filter(c -> c instanceof GLCheckBox check && check.getText().equals("Pinned"))
                    .map(GLCheckBox.class::cast).findFirst().orElseThrow();
            var other = controls.stream().filter(c -> c instanceof GLCheckBox check && check.getText().equals("Other"))
                    .findFirst().orElseThrow();
            view.setContentHidden(true);
            assertTrue(view.findPort("out").isVisible());
            assertTrue(view.findPort("in").isVisible());
            assertTrue(pinned.isVisible()); assertFalse(other.isVisible());
            assertFalse(view.findPort("optional").isVisible());
            var slider = controls.stream().filter(GLSlider.class::isInstance).map(GLSlider.class::cast).findFirst().orElseThrow();
            assertTrue(slider.isVisible()); slider.setValue(4); pinned.doClick();
            assertEquals(List.of(PresentationNode.IN, PresentationNode.PIN), changes, "Both input editors and standalone editors send typed keys");
            var changed = model.withProperty(PresentationNode.PIN.value(), GLNodeValue.of(false));
            assertFalse(factory.isViewCurrent(view, changed, type));
            var source = new GLNode("Source"); var sourcePort = source.addOutput("out", new GLLabel("Out"));
            editor.addNode(source, 0, 200); editor.connect(sourcePort, view.findPort("optional"));
            assertTrue(view.findPort("optional").isVisible(), "Core keeps connected ports visible even without the flag");
            editor.removeNode(view); editor.removeNode(source);
        });
    }
    @Test void themeOwnsEditorMetricsAndAutomaticNodeSizeIncludingThemeChanges() throws Exception {
        GLNode[] view = new GLNode[1]; GLNodeEditor[] editor = new GLNodeEditor[1];
        SwingUtilities.invokeAndWait(() -> {
            var definition = new MathNode(); var model = new GLGraphNode(GLNodeId.random(), definition.registration().id(), definition.defaults());
            var factory = new AudioNodeFactory((id, key, value) -> {}, new GLHistoryManager(), () -> {});
            view[0] = factory.createNode(model, new GLNodeTypeDescriptor(model.typeId(), definition.layout(new NodeState(model.properties())).ports()));
            editor[0] = new GLNodeEditor(); editor[0].addNode(view[0], 70, 140);
        });
        SwingUtilities.invokeAndWait(() -> {
            var theme = editor[0].getGLTheme();
            editor[0].setGLTheme(theme.withSliderStyle(theme.sliderStyle().withGeometry(3, 18, 120))
                    .withComboBoxStyle(theme.comboBoxStyle().withGeometry(3, 19, 130, 20))
                    .withValues(theme.values().with("node.minimumWidth", "210").with("node.rowGap", "3")));
        });
        SwingUtilities.invokeAndWait(() -> {
            for (var control : view[0].getContents()) {
                if (control instanceof GLSlider) {
                    assertEquals(120f, control.getPreferredGLSize().width()); assertEquals(18f, control.getHeight());
                } else if (control instanceof GLComboBox) assertEquals(19f, control.getHeight());
            }
            assertEquals(view[0].getPreferredGLSize().width(), view[0].getWidth());
            assertEquals(view[0].getPreferredGLSize().height(), view[0].getHeight());
            assertEquals(70f, view[0].getX()); assertEquals(140f, view[0].getY());
            view[0].setSize(500, 500); // Explicit user sizing remains an override.
            var theme = editor[0].getGLTheme();
            editor[0].setGLTheme(theme.withSliderStyle(theme.sliderStyle().withGeometry(3, 20, 130)));
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(500f, view[0].getWidth()); assertEquals(500f, view[0].getHeight());
            editor[0].removeNode(view[0]);
        });
    }
    @Test void pinnedOperationSurvivesWorkspaceHideClipboardAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try (var controller = new WorkspaceController()) {
                var workspace = controller.workspace(); var definition = new MathNode();
                workspace.currentGraph().edit("Math", edit -> edit.addNode(new GLGraphNode(GLNodeId.random(), definition.registration().id(), definition.defaults())));
                var math = workspace.editor().getNodes().stream().filter(n -> n.getTitle().equals("Math")).findFirst().orElseThrow();
                workspace.editor().setSelectedNode(math); controller.actions().toggleHidden();
                assertTrue(math.getContents().stream().filter(GLComboBox.class::isInstance).findFirst().orElseThrow().isVisible());
                assertFalse(math.findPort(MathNode.A.value()).isVisible());
                controller.history().undo(); assertTrue(math.findPort(MathNode.A.value()).isVisible());
                controller.history().redo(); controller.actions().duplicate();
                var copy = workspace.editor().getSelectedNodes().get(0);
                assertTrue(copy.isContentHidden());
                assertTrue(copy.getContents().stream().filter(GLComboBox.class::isInstance).findFirst().orElseThrow().isVisible());
            }
        });
    }
}

