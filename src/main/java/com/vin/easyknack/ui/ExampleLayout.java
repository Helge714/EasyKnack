package com.vin.easyknack.ui;

import java.util.*;
import java.util.concurrent.CompletionException;
import javax.swing.SwingUtilities;
import com.vingame.glswing.node.binding.*;
import com.vingame.glswing.node.document.*;

/** Arrange authored examples with real widget sizes, retaining only presentation changes. */
final class ExampleLayout {
    static GLNodeGraphDocument arrange(GLNodeGraphDocument document) {
        if (SwingUtilities.isEventDispatchThread()) return onUi(document);
        var result = new GLNodeGraphDocument[1];
        try { SwingUtilities.invokeAndWait(() -> result[0] = onUi(document)); }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt(); throw new CompletionException(interrupted);
        } catch (java.lang.reflect.InvocationTargetException failure) {
            throw new CompletionException(failure.getCause());
        }
        return result[0];
    }
    private static GLNodeGraphDocument onUi(GLNodeGraphDocument document) {
        var views = new HashMap<String, GLNodeEditorViewState>();
        try (var controller = new WorkspaceController()) {
            var workspace = controller.workspace();
            workspace.setGLTheme(ApplicationTheme.loadSelection(BundledThemes.available().get(0).id()).theme());
            workspace.setBounds(0, 0, 1200, 800);
            controller.openDocument("Example layout", document);
            for (var asset : NodeDocuments.assets(document)) {
                if (asset != document.root()) workspace.openComposite(asset.id());
                workspace.arrangeAll();
                var nodes = workspace.editor().getNodes();
                if (!nodes.isEmpty()) {
                    float left = Float.POSITIVE_INFINITY, top = left;
                    float right = Float.NEGATIVE_INFINITY, bottom = right;
                    for (var node : nodes) {
                        left = Math.min(left, node.getX()); top = Math.min(top, node.getY());
                        right = Math.max(right, node.getX() + node.getWidth());
                        bottom = Math.max(bottom, node.getY() + node.getHeight());
                    }
                    float x = (left + right) / 2, y = (top + bottom) / 2;
                    for (var node : nodes) node.setLocation(node.getX() - x, node.getY() - y);
                }
                var snapshot = controller.snapshotDocument();
                var captured = asset == document.root() ? snapshot.root() : snapshot.embeddedAssets().get(asset.id());
                views.put(asset.id(), new GLNodeEditorViewState(preferred(captured.viewState().nodes()),
                    preferred(captured.viewState().auxiliaryNodes()), new GLNodeCameraState(0, 0)));
                workspace.navigateToDepth(0);
            }
        }
        var assets = new LinkedHashMap<String, GLNodeGraphAsset>();
        for (var asset : document.embeddedAssets().values()) assets.put(asset.id(), withView(asset, views.get(asset.id())));
        return new GLNodeGraphDocument(withView(document.root(), views.get(document.root().id())), assets, document.externalAssets());
    }
    private static <K> Map<K, GLNodeViewState> preferred(Map<K, GLNodeViewState> captured) {
        var result = new LinkedHashMap<K, GLNodeViewState>();
        captured.forEach((id, view) -> result.put(id, new GLNodeViewState(view.x(), view.y(), 0, 0, view.customTitle(), view.mirrored())));
        return result;
    }
    private static GLNodeGraphAsset withView(GLNodeGraphAsset asset, GLNodeEditorViewState view) {
        return new GLNodeGraphAsset(asset.id(), asset.version(), asset.kind(), asset.graph(), view, asset.metadata(), asset.compositeDescriptor());
    }
}
