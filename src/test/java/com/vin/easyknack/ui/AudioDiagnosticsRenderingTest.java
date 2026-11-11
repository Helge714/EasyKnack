package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.nio.ByteBuffer;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vin.easyknack.audio.*;
import com.vingame.glswing.rendering.*;
@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class AudioDiagnosticsRenderingTest {
    @Test void diagnosticsRenderOnCanvas()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,1100,320);drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            try{
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,1100,320);renderer=GLRenderer2D.create(gl);
                var panel=new AudioDiagnosticsPanel(()->{});fonts.attach(panel);panel.setGLTheme(ApplicationTheme.current().theme());panel.setVisible(true);panel.setBounds(0,0,1100,320);panel.state(AudioHostService.State.RUNNING);
                for(int i=0;i<240;i++)panel.accept(new AudioDiagnostics(1,i+1,25+12*Math.sin(i*.05),60+30*Math.sin(i*.08),1300,8000,i>140?1:0,256,48000,0,0,0,i>200?1:0,0,0,3,"Clock: WASAPI","Buffers: test"),i*250_000_000L);
                var frame=renderer.beginFrame(gl,1100,320,Projection2D.TOP_LEFT_PIXELS.create(1100,320));frame.clear(new Color(26,27,31));panel.renderComponent(frame);renderer.endFrame();
                var pixels=ByteBuffer.allocateDirect(1100*320*4);gl.glReadPixels(0,0,1100,320,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);var image=new java.awt.image.BufferedImage(1100,320,java.awt.image.BufferedImage.TYPE_INT_RGB);
                int lit=0;for(int y=0;y<320;y++)for(int x=0;x<1100;x++){int n=((319-y)*1100+x)*4,r=pixels.get(n)&255,g=pixels.get(n+1)&255,b=pixels.get(n+2)&255;image.setRGB(x,y,r<<16|g<<8|b);if(r>150&&g>150)lit++;}
                assertTrue(lit>200);assertEquals(GL.GL_NO_ERROR,gl.glGetError());try{javax.imageio.ImageIO.write(image,"png",new java.io.File("target/diagnostics-preview.png"));}catch(java.io.IOException e){throw new AssertionError(e);}
            }finally{fonts.dispose(renderer);}
        });
    }
}
