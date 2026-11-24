package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Canvas;
import java.awt.event.*;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vingame.glswing.input.*;
import com.vingame.glswing.rendering.*;

@EnabledIfSystemProperty(named="easyknack.test.gl", matches="true")
class ConnectionPickingTest {
    @Test void borderlessThemesAllowPickingDeletingAndUndoingRealConnections() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var previous = ApplicationTheme.current();
            var profile = GLProfile.get(GLProfile.GL3);
            var caps = new GLCapabilities(profile); caps.setOnscreen(false);
            var drawable = GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,1100,650);
            drawable.display(); drawable.getContext().makeCurrent();
            var gl = drawable.getGL().getGL3();
            GLRenderer2D renderer = GLRenderer2D.create(gl);
            var fonts = new OffscreenFonts(drawable);
            try {
                for (var theme : BundledThemes.available()) for (float curvature : new float[]{0,.5f}) {
                    var selected = ApplicationTheme.loadSelection(theme.id()); ApplicationTheme.activate(selected);
                    try (var controller = new WorkspaceController()) {
                        controller.openDocument("Picking", StarterDocument.create());
                        var workspace = controller.workspace(); workspace.setGLTheme(selected.theme());
                        fonts.attach(workspace); workspace.setBounds(0,0,1100,650);
                        var editor = workspace.editor(); editor.setConnectionHandleFactor(curvature); editor.setMinimumConnectionHandleLength(0);
                        var wire = editor.getConnections().get(0);
                        wire.getOutput().getNode().setLocation(40,100);
                        wire.getInput().getNode().setLocation(750,100);
                        gl.glViewport(0,0,1100,650);
                        var frame = renderer.beginFrame(gl,1100,650,Projection2D.TOP_LEFT_PIXELS.create(1100,650));
                        try { workspace.renderComponent(frame); assertTrue(frame.isColorRenderingEnabled(),"Pick-only stroke must restore the color mask"); }
                        finally { renderer.endFrame(); }
                        var a=wire.getOutput().getLastRenderedScreenBounds();var b=wire.getInput().getLastRenderedScreenBounds();
                        int x=Math.round((a[0]+a[2]/2+b[0]+b[2]/2)/2),y=Math.round((a[1]+a[3]/2+b[1]+b[3]/2)/2);
                        var pick=renderer.readPickResult(gl,x,y);
                        assertSame(wire,pick.getTarget(),theme+" curvature="+curvature);
                        var source=new Canvas();
                        wire.mousePressed(new GLMouseEvent(new MouseEvent(source,MouseEvent.MOUSE_PRESSED,0,0,x,y,1,false,MouseEvent.BUTTON1),pick,0,0));
                        assertSame(wire,editor.getSelectedConnection());
                        editor.keyPressed(new GLKeyEvent(new KeyEvent(source,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_DELETE,KeyEvent.CHAR_UNDEFINED)));
                        assertEquals(0,workspace.currentGraph().snapshot().connections().size());
                        assertEquals(2,workspace.currentGraph().snapshot().nodes().size());
                        assertTrue(controller.history().undo());
                        assertEquals(1,workspace.currentGraph().snapshot().connections().size());
                        fonts.surface().removeGL(workspace);
                    }
                }
            } finally { ApplicationTheme.activate(previous); fonts.dispose(renderer); }
        });
    }
}
