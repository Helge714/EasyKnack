package com.vin.easyknack.ui;
import com.vin.easyknack.document.KnackDocumentCodec;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

class NodeDocumentsTest {
    @TempDir Path directory;
    private static <T> T edt(Callable<T> action) throws Exception {
        var result = new CompletableFuture<T>();
        SwingUtilities.invokeAndWait(() -> { try { result.complete(action.call()); } catch (Throwable failure) { result.completeExceptionally(failure); } });
        return result.get();
    }
    @Test void roundTripKeepsNestedGroupsDynamicPropertiesAndActiveInnerLayout() throws Exception {
        var source = edt(WorkspaceController::new);
        var target = edt(WorkspaceController::new);
        try (var files = new NodeDocuments()) {
            var before = edt(() -> {
                var workspace = source.workspace();
                var math = new MathNode(); var id = GLNodeId.random();
                workspace.currentGraph().edit("Math", e -> e.addNode(new GLGraphNode(id, math.registration().id(), math.defaults())));
                source.setNodeProperty(id, MathNode.RATE, GLNodeValue.of("control"));
                source.setNodeProperty(id, MathNode.OPERATION, GLNodeValue.of("absolute"));
                workspace.editor().setSelectedNodes(workspace.editor().getNodes()); source.actions().extractComposite();
                source.actions().openSelectedComposite();
                var mathView = workspace.editor().getNodes().stream().filter(v -> v.findPort("a") != null).findFirst().orElseThrow();
                workspace.editor().setSelectedNode(mathView); source.actions().toggleHidden(); source.actions().toggleMuted();
                source.actions().extractComposite(); source.actions().openSelectedComposite();
                workspace.editor().getNodes().get(0).setLocation(123, 456);
                return source.snapshotDocument();
            });
            assertEquals(2, before.embeddedAssets().size());
            var file = directory.resolve("nested.knack");
            files.write(file, before).toCompletableFuture().get(10, TimeUnit.SECONDS);
            var decoded = files.read(file).toCompletableFuture().get(10, TimeUnit.SECONDS);
            assertEquals(before, decoded);
            var legacy = directory.resolve("legacy.glgraph"); Files.copy(file, legacy);
            assertEquals(before, files.read(legacy).toCompletableFuture().get(10, TimeUnit.SECONDS));
            assertEquals("legacy.knack", DocumentActions.saveName(legacy));
            assertEquals(AppText.text("untitled")+".knack", DocumentActions.saveName(null));
            assertEquals(Set.of("knack", "glgraph"), new KnackDocumentCodec().fileExtensions());
            edt(() -> {
                String expectedAudio = source.audioGraph();
                target.openDocument("nested", decoded);
                assertFalse(target.history().canUndo());
                assertEquals(expectedAudio, target.audioGraph());
                assertEquals(NodeDocuments.comparable(before), NodeDocuments.comparable(target.snapshotDocument()));
                var inner = decoded.embeddedAssets().values().stream().filter(a -> a.graph().nodes().stream()
                        .anyMatch(n -> NodeRegistry.find(n.typeId()).filter(MathNode.class::isInstance).isPresent())).findFirst().orElseThrow();
                assertTrue(target.workspace().openComposite(inner.id()));
                var view = target.workspace().editor().getNodes().stream().filter(v -> v.findPort("a") != null).findFirst().orElseThrow();
                assertEquals(123f, view.getX()); assertEquals(456f, view.getY()); assertNull(view.findPort("b"));
                assertTrue(view.isContentHidden());
                // Opening repeatedly must replace the group registries, not accumulate duplicate IDs.
                target.openDocument("nested again", decoded);
                assertEquals(expectedAudio, target.audioGraph());
                return null;
            });
        } finally { edt(() -> { source.close(); target.close(); return null; }); }
    }
    @Test void invalidDocumentDoesNotReplaceTheCurrentGraph() throws Exception {
        var controller = edt(WorkspaceController::new);
        try {
            edt(() -> {
                var before = controller.snapshotDocument(); var root = before.root();
                var bad = new GLNodeGraphSnapshot(root.graph().graphId(), 0,
                        List.of(new GLGraphNode(GLNodeId.random(), "missing.plugin.node", Map.of())), List.of());
                var document = new GLNodeGraphDocument(new GLNodeGraphAsset(root.id(), root.version(), root.kind(), bad, root.viewState()));
                assertThrows(IllegalArgumentException.class, () -> controller.openDocument("bad", document));
                assertEquals(before, controller.snapshotDocument());
                return null;
            });
        } finally { edt(() -> { controller.close(); return null; }); }
    }
    @Test void malformedReadAndFailedWriteLeaveExistingFilesAlone() throws Exception {
        var controller = edt(WorkspaceController::new);
        try (var files = new NodeDocuments()) {
            var snapshot = edt(controller::snapshotDocument);
            var file = directory.resolve("invalid.knack"); Files.writeString(file, "not a graph");
            assertThrows(ExecutionException.class, () -> files.read(file).toCompletableFuture().get(10, TimeUnit.SECONDS));
            assertThrows(ExecutionException.class, () -> files.write(file.resolve("child.knack"), snapshot).toCompletableFuture().get(10, TimeUnit.SECONDS));
            assertEquals("not a graph", Files.readString(file));
        } finally { edt(() -> { controller.close(); return null; }); }
    }
}


