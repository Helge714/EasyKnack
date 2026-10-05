package com.vin.easyknack.ui;

import com.vingame.glswing.file.*;
import com.vingame.glswing.component.GLParent;
import java.nio.file.Path;
import java.util.concurrent.CompletionStage;

/** Application preference namespace; remembering and selection semantics live in Core. */
final class RememberingFileDialogs implements AutoCloseable {
    private static final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node("com/vin/easyknack/dialogs");
    private final GLRememberingFileDialogs dialogs = new GLRememberingFileDialogs(GLFileDialogs.themed(),
            new GLRememberingFileDialogs.DirectoryStore() {
                public Path load(String purpose) { String value = prefs.get(purpose, ""); return value.isBlank() ? null : Path.of(value); }
                public void save(String purpose, Path directory) {
                    prefs.put(purpose, directory.toString());
                    try { prefs.flush(); } catch (java.util.prefs.BackingStoreException ignored) { }
                }
            });
    CompletionStage<GLFileDialogResult> show(String purpose, GLFileDialogRequest request, GLParent owner) {
        // Preserve old remembered folders when upgrading from title-based keys.
        try { if (prefs.get(purpose, null) == null) {
            String old = prefs.get(Integer.toHexString(AppText.identity(request.title()).hashCode()),
                    prefs.get(Integer.toHexString(request.title().hashCode()), ""));
            if (!old.isBlank()) prefs.put(purpose, old);
        }
        } catch (RuntimeException ignored) { }
        return dialogs.show(purpose, request, owner);
    }
    public void close() { dialogs.close(); }
}
