package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.nio.ByteBuffer;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.*;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;

@EnabledIfSystemProperty(named="easyknack.test.gl", matches="true")
class SliderPortRenderingTest {
    @Test void diamondHoverAndSeparateUnitRenderOnGpu() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var profile = GLProfile.get(GLProfile.GL3); var caps = new GLCapabilities(profile); caps.setOnscreen(false);
            var drawable = GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null, caps, null, 360, 180);
            drawable.display(); var nativeContext = drawable.getContext(); nativeContext.makeCurrent();
            GLRenderer2D renderer = null;var fonts=new OffscreenFonts(drawable);
            try {
                var gl = drawable.getGL().getGL3(); gl.glViewport(0,0,360,180); renderer = GLRenderer2D.create(gl);
                float[] zoom = {1};
                var root = new GLContainer() { @Override protected void record(GLContext2D c) { }
                    @Override protected void transformComponent(GLContext2D c) { c.setTransform(c.getTransform().copy().scale(zoom[0])); } };
                fonts.attach(root);root.setBounds(0,0,360,180); root.setGLTheme(ApplicationTheme.current().theme());
                var port = GLNodePort.input("control"); port.setShape(GLNodePort.Shape.DIAMOND); port.setColor(Color.ORANGE);
                port.setBounds(20,25,20,20); root.addGL(port);
                var slider = GLSlider.fixed("Frequency",0,20000,440); slider.setUnit("Hz"); slider.setBounds(60,20,260,30); root.addGL(slider);
                var editing = GLSlider.fixed("Frequency",0,20000,440); editing.setUnit("Hz"); editing.setBounds(60,70,260,30); root.addGL(editing);
                editing.startValueEditing();
                var editor = (GLTextEditor)editing.getGLChildren().get(0); assertEquals("440",editor.getText());
                draw(gl,renderer,root); var normal = pixels(gl); assertColor(normal,30,35,Color.ORANGE);
                assertSame(port, renderer.readPickResult(gl,30,35).getTarget());
                port.mouseEntered(null); draw(gl,renderer,root); var hovered = pixels(gl);
                assertColor(hovered,30,35,GLNodeStyle.fromTheme(port.getGLTheme()).portHover());
                assertSame(port, renderer.readPickResult(gl,30,35).getTarget());
                port.mouseExited(null); draw(gl,renderer,root); assertColor(pixels(gl),30,35,Color.ORANGE);
                // Borderless event ports retain a node-colored center hole, not a decorative border.
                port.setShape(GLNodePort.Shape.DOUBLE_DIAMOND);draw(gl,renderer,root);
                assertColor(pixels(gl),30,35,GLNodeStyle.fromTheme(port.getGLTheme()).background());
                assertColor(pixels(gl),30,28,Color.ORANGE);
                assertSame(port,renderer.readPickResult(gl,30,35).getTarget());
                port.mouseEntered(null);draw(gl,renderer,root);
                assertColor(pixels(gl),30,35,GLNodeStyle.fromTheme(port.getGLTheme()).background());
                assertColor(pixels(gl),30,28,GLNodeStyle.fromTheme(port.getGLTheme()).portHover());
                port.mouseExited(null);
                editing.setUnit(""); draw(gl,renderer,root); var withoutUnit = pixels(gl);
                editing.setUnit("Hz"); draw(gl,renderer,root); var withUnit = pixels(gl);
                int changed = 0;
                for(int y=73;y<97;y++) for(int x=290;x<317;x++) if(rgb(withUnit,x,y)!=rgb(withoutUnit,x,y)) changed++;
                assertTrue(changed>4,"The non-editable suffix must be drawn over the inline editor");
                assertEquals("440",editor.getText());
                editor.setText("123456789012345678901234567890123456789012345678901234567890");
                for (int caret : new int[] {0, 25, editor.getText().length()}) {
                    editor.setCaretPosition(caret); draw(gl,renderer,root); var scrolled = pixels(gl);
                    for (int y=72;y<98;y++) for (int x=298;x<318;x++)
                        assertEquals(rgb(withUnit,x,y),rgb(scrolled,x,y), "Scrolling text must not enter the fixed unit area");
                }
                editor.setText("440"); draw(gl,renderer,root);
                // Fractional clipping edges must not expose a bright glyph sliver beyond the fade.
                for (float scale : new float[] {.65f,.85f,1,1.25f,1.5f}) for (float width=120.125f;width<140;width+=.5f) {
                    zoom[0]=scale;
                    editing.setSize(width,30); editor.setText(""); draw(gl,renderer,root); var empty = pixels(gl);
                    editor.setText("888888888888888888888888888888888888888888888888888888888888888888");
                    editor.setCaretPosition(0); draw(gl,renderer,root); var overflowing = pixels(gl);
                    int edge = (int)Math.ceil(scale * (editing.getX() + width - editor.getGLStyle().padding().right())) - 1;
                    for (int y=(int)Math.ceil(74*scale);y<(int)(96*scale);y++) {
                        Color expected = new Color(rgb(empty,edge,y)), actual = new Color(rgb(overflowing,edge,y));
                        assertTrue(Math.abs(actual.getRed()-expected.getRed())<20
                                && Math.abs(actual.getGreen()-expected.getGreen())<20
                                && Math.abs(actual.getBlue()-expected.getBlue())<20,
                                "Bright pixel at right fade edge, scale="+scale+", width="+width+", y="+y);
                    }
                }
                assertTrue(editing.getGLChildren().stream().allMatch(c->c==editor));
                var image = new java.awt.image.BufferedImage(360,180,java.awt.image.BufferedImage.TYPE_INT_RGB);
                for(int y=0;y<180;y++) for(int x=0;x<360;x++) image.setRGB(x,y,rgb(withUnit,x,y));
                try { javax.imageio.ImageIO.write(image,"png",new java.io.File("target/slider-unit-preview.png")); }
                catch(java.io.IOException e) { throw new RuntimeException(e); }
                editing.cancelValueEditing();
            } finally { fonts.dispose(renderer); }
        });
    }
    private static void draw(GL3 gl, GLRenderer2D renderer, GLContainer root) {
        var context=renderer.beginFrame(gl,360,180,Projection2D.TOP_LEFT_PIXELS.create(360,180));
        context.clear(new Color(25,28,32)); root.renderComponent(context);
        assertNull(context.getMouseTarget()); renderer.endFrame();
    }
    private static ByteBuffer pixels(GL3 gl) { var pixels=ByteBuffer.allocateDirect(360*180*4);gl.glReadPixels(0,0,360,180,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);return pixels; }
    private static int rgb(ByteBuffer p,int x,int y) { int i=((179-y)*360+x)*4;return (p.get(i)&255)<<16|(p.get(i+1)&255)<<8|p.get(i+2)&255; }
    private static void assertColor(ByteBuffer p,int x,int y,Color color) { var actual=new Color(rgb(p,x,y));assertTrue(Math.abs(actual.getRed()-color.getRed())<3&&Math.abs(actual.getGreen()-color.getGreen())<3&&Math.abs(actual.getBlue()-color.getBlue())<3,actual+" vs "+color); }
}

