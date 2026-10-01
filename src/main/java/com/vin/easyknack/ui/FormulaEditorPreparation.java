package com.vin.easyknack.ui;

import java.awt.EventQueue;
import java.util.List;
import com.vin.easyknack.formula.FormulaLanguage;
import com.vingame.glswing.ui.GLCodeEditor;
import com.vingame.glswing.ui.GLTextDocument;

/** Prepares the first editor on the UI thread while the startup screen is visible. */
public final class FormulaEditorPreparation {
    private static GLCodeEditor prepared;
    private FormulaEditorPreparation() { }

    public static void prepare() {
        requireEdt();
        if (prepared == null) prepared = create();
    }

    static GLCodeEditor acquire() {
        requireEdt();
        var editor = prepared;
        prepared = null;
        return editor == null ? create() : editor;
    }

    /** Populate the shared CPU atlas cache on the startup GL context. */
    public static void prepareFont(com.jogamp.opengl.GL3 gl) {
        var style = ApplicationTheme.current().theme().textEditorStyle();
        var font = new com.vingame.glswing.rendering.GLFont(
                com.vingame.glswing.rendering.GLFont.MONOSPACED,
                com.vingame.glswing.rendering.GLFont.PLAIN, style.font().size());
        try { font.initialize(gl); }
        finally { font.dispose(gl); }
    }

    private static GLCodeEditor create() {
        var language = new FormulaLanguage(List.of(), 3);
        // Warm the parser without putting preparation text into the user's undo history.
        language.diagnose("double seconds = (double) samplePosition / sampleRate;");
        return new GLCodeEditor(new GLTextDocument(), true, language);
    }

    private static void requireEdt() {
        if (!EventQueue.isDispatchThread()) throw new IllegalStateException("Formula editor preparation requires the UI thread");
    }
}
