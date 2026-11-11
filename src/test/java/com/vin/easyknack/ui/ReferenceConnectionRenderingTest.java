package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.nio.ByteBuffer;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vingame.glswing.node.*;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;
import com.vin.easyknack.nodes.*;

@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class ReferenceConnectionRenderingTest {
    @Test void referenceCablesHaveRealGapsInBothCurvePaths() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,700,250);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            try {
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,700,250);renderer=GLRenderer2D.create(gl);
                var editor=new GLNodeEditor();fonts.attach(editor);editor.setGLTheme(ApplicationTheme.current().theme());editor.setBounds(0,0,700,250);
                var source=new GLNode("Source");var out=source.addOutput("out",new GLLabel("Reference"));
                var target=new GLNode("Target");var in=target.addInput("in",new GLLabel("Reference"));
                editor.addNode(source,20,70);editor.addNode(target,550,70);
                source.setSize(110,source.getPreferredGLSize().height());target.setSize(110,target.getPreferredGLSize().height());
                out.setColor(Color.YELLOW);in.setColor(Color.YELLOW);var connection=editor.connect(out,in);
                for(float handles:new float[]{0,.5f})for(boolean dashed:new boolean[]{false,true}) {
                    editor.setMinimumConnectionHandleLength(0);editor.setConnectionHandleFactor(handles);out.setConnectionDashed(dashed);
                    assertEquals(dashed,connection.isDashed());
                    var frame=renderer.beginFrame(gl,700,250,Projection2D.TOP_LEFT_PIXELS.create(700,250));frame.clear(Color.BLACK);editor.renderComponent(frame);renderer.endFrame();
                    var pixels=ByteBuffer.allocateDirect(700*250*4);gl.glReadPixels(0,0,700,250,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                    int y=Math.round(source.getY()+out.getCenterYInNode()),visible=0,gaps=0;
                    for(int x=180;x<500;x++) {int n=((249-y)*700+x)*4;boolean yellow=(pixels.get(n)&255)>180&&(pixels.get(n+1)&255)>180&&(pixels.get(n+2)&255)<100;if(yellow)visible++;else gaps++;}
                    assertTrue(visible>80,"Connection must render");
                    if(dashed)assertTrue(gaps>60,"Reference cable must have visible gaps, including its border");else assertTrue(gaps<5,"Value cable must remain solid");
                }
                assertEquals(GL.GL_NO_ERROR,gl.glGetError());
            }finally{fonts.dispose(renderer);}
        });
    }
}
