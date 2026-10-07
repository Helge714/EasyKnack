package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vingame.glswing.component.GLContainer;

class InitialDocumentTest {
    @TempDir Path directory;

    @Test void startupFileReplacesDefaultWithoutConfirmationAndBecomesCheckpoint() throws Exception {
        Path file = directory.resolve("Startup ü.knack");
        var document = StarterDocument.create();
        try (var files = new NodeDocuments()) {
            files.write(file, document).toCompletableFuture().get(10, TimeUnit.SECONDS);
        }
        var result = new CompletableFuture<Void>();
        SwingUtilities.invokeAndWait(() -> {
            var controller = new WorkspaceController();
            var window = new JFrame();
            var owner = new GLContainer() {
                @Override protected void record(com.vingame.glswing.rendering.GLContext2D context) { }
            };
            try (var actions = new DocumentActions(controller, owner, window, ignored -> {})) {
                actions.openInitial(file);
                assertEquals(document.root().graph().nodes().stream().map(n -> n.id()).toList(),
                        controller.snapshotDocument().root().graph().nodes().stream().map(n -> n.id()).toList());
                assertEquals("Startup ü.knack — EasyKnack", window.getTitle());
                assertTrue(actions.requestClose().toCompletableFuture().getNow(false));
                result.complete(null);
            } catch (Throwable failure) { result.completeExceptionally(failure); }
            finally { controller.close(); window.dispose(); }
        });
        result.get(10, TimeUnit.SECONDS);
    }
}
