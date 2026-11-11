package com.vin.easyknack.ui;

import com.jogamp.opengl.GLAutoDrawable;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.component.GLParent;
import com.vingame.glswing.component.GLSwingPanel;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.rendering.GLRenderer2D;

/** Gives manually rendered controls the same per-surface font ownership as a real window. */
final class OffscreenFonts {
    private final GLAutoDrawable drawable;
    private final GLSwingPanel owner = new GLSwingPanel() { };

    OffscreenFonts(GLAutoDrawable drawable) {
        this.drawable = drawable;
        owner.setSize(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
    }

    void attach(GLContainer... roots) {
        for (var root : roots) owner.addGL(root);
    }

    GLParent surface() { return owner; }

    void render(GLContext2D frame) {
        for (var root : owner.getGLChildrenInPaintOrder()) root.renderComponent(frame);
    }

    void dispose(GLRenderer2D renderer) {
        try {
            if (renderer != null) renderer.dispose(drawable.getGL().getGL3());
        } finally {
            try {
                owner.dispose(drawable);
                owner.removeAllGL();
            } finally {
                try { drawable.getContext().release(); }
                finally { drawable.destroy(); }
            }
        }
    }
}
