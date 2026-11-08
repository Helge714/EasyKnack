package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.OscillatorNode;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.GLNodeValue;

class StartupCheckpointTest {
    @TempDir Path directory;

    private static boolean modified(DocumentActions documents) throws Exception {
        var method = DocumentActions.class.getDeclaredMethod("modified");
        method.setAccessible(true);
        return (boolean) method.invoke(documents);
    }

    @Test void firstThemedLayoutIsCleanButLaterEditsRemainUnsaved() throws Exception {
        check(false, false);
    }

    @Test void firstLayoutDoesNotSaveModelEdits() throws Exception {
        check(true, false);
    }

    @Test void firstLayoutDoesNotSaveRecoveredDocuments() throws Exception {
        check(false, true);
    }

    private void check(boolean edit, boolean recovered) throws Exception {
        var controller = new WorkspaceController[1];
        var documents = new DocumentActions[1];
        var window = new JFrame[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                window[0] = new JFrame();
                controller[0] = new WorkspaceController();
                var owner = new GLContainer() {
                    @Override protected void record(com.vingame.glswing.rendering.GLContext2D context) { }
                };
                documents[0] = new DocumentActions(controller[0], owner, window[0], s -> {}, directory);
                owner.setGLTheme(ApplicationTheme.current().theme());
                owner.addGL(controller[0].workspace());
                controller[0].workspace().setBounds(0, 0, 1280, 734);
                if (edit) changeFrequency(controller[0]);
                if (recovered) {
                    try {
                        var field = DocumentActions.class.getDeclaredField("checkpoint");
                        field.setAccessible(true);
                        field.set(documents[0], null);
                    } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
                }
                documents[0].initializeViewCheckpoint();
            });
            SwingUtilities.invokeAndWait(() -> {
                try {
                    assertEquals(edit || recovered, modified(documents[0]));
                    if (!edit && !recovered) changeFrequency(controller[0]);
                    documents[0].initializeViewCheckpoint();
                } catch (Exception e) { throw new AssertionError(e); }
            });
            SwingUtilities.invokeAndWait(() -> {
                try { assertTrue(modified(documents[0])); }
                catch (Exception e) { throw new AssertionError(e); }
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                if (documents[0] != null) documents[0].close();
                if (controller[0] != null) controller[0].close();
                if (window[0] != null) window[0].dispose();
            });
        }
    }

    private static void changeFrequency(WorkspaceController controller) {
        var node = controller.workspace().currentGraph().snapshot().nodes().stream()
                .filter(n -> n.typeId().equals(new OscillatorNode().registration().id())).findFirst().orElseThrow();
        controller.setNodeProperty(node.id(), OscillatorNode.FREQUENCY, GLNodeValue.of(330.0));
    }
}
