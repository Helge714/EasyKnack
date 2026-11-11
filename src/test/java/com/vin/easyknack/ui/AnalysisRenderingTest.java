package com.vin.easyknack.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.nio.ByteBuffer;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;

@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class AnalysisRenderingTest {
    @Test void analysisAndCompactPreviewRender() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,680,520);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            try(var controller=new WorkspaceController()){
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,680,520);renderer=GLRenderer2D.create(gl);
                var root=new GLContainer(){@Override protected void record(GLContext2D c){}};
                fonts.attach(root);root.setBounds(0,0,680,520);root.setGLTheme(ApplicationTheme.current().theme());
                for(int i=0;i<3;i++){
                    BasicNode definition=i==0?new OscilloscopeNode():new AutocorrelationNode();
                    var id=GLNodeId.random();float[] samples=new float[256];for(int n=0;n<256;n++)samples[n]=(float)(Math.cos(n*.12)*(i==0?1:Math.exp(-n/300.0)));
                    com.vin.easyknack.audio.AudioAnalysis.publish(java.util.Map.of(id.toString(),new com.vin.easyknack.audio.AudioAnalysis.Frame(440,.98,.02,samples)));
                    var node=definition.createView(new NodeViewContext((key,property,value)->{},controller.history()),new GLGraphNode(id,definition.registration().id(),definition.defaults()));
                    root.addGL(node);node.setContentHidden(false);node.setBounds(20+i*220,20,node.getPreferredGLSize().width(),node.getPreferredGLSize().height());
                    if(i==1)node.setSize(node.getWidth()+20,node.getHeight()+90);
                    if(i==2){node.setContentHidden(true);node.setSize(node.getWidth(),node.getPreferredGLSize().height());}
                }
                var frame=renderer.beginFrame(gl,680,520,Projection2D.TOP_LEFT_PIXELS.create(680,520));frame.clear(new Color(26,27,31));root.renderComponent(frame);renderer.endFrame();
                var pixels=ByteBuffer.allocateDirect(680*520*4);gl.glReadPixels(0,0,680,520,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                var image=new java.awt.image.BufferedImage(680,520,java.awt.image.BufferedImage.TYPE_INT_RGB);
                int lit=0;for(int y=0;y<520;y++)for(int x=0;x<680;x++){int n=((519-y)*680+x)*4,r=pixels.get(n)&255,g=pixels.get(n+1)&255,b=pixels.get(n+2)&255;image.setRGB(x,y,r<<16|g<<8|b);if(r>150&&b>150)lit++;}
                assertTrue(lit>100,"Analysis lines, controls and labels must be visible");
                try{javax.imageio.ImageIO.write(image,"png",new java.io.File("target/analysis-preview.png"));}catch(java.io.IOException e){throw new AssertionError(e);}
                assertEquals(GL.GL_NO_ERROR,gl.glGetError());
                var source=new AnalysisEditor.Plot(true,true);source.bind("expanded-render");source.configure(1,0,0);
                float[] correlation=new float[8192];for(int i=0;i<correlation.length;i++)correlation[i]=(float)Math.cos(i*.02);
                com.vin.easyknack.audio.AudioAnalysis.publish(java.util.Map.of("expanded-render",new com.vin.easyknack.audio.AudioAnalysis.Frame(0,0,8191.0/48000,true,correlation)));
                var expanded=new AnalysisFocusView(()->{});root.addGL(expanded);expanded.setBounds(0,0,680,520);expanded.open(source);
                frame=renderer.beginFrame(gl,680,520,Projection2D.TOP_LEFT_PIXELS.create(680,520));root.renderComponent(frame);renderer.endFrame();
                gl.glReadPixels(0,0,680,520,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                for(int y=0;y<520;y++)for(int x=0;x<680;x++){int n=((519-y)*680+x)*4;image.setRGB(x,y,(pixels.get(n)&255)<<16|(pixels.get(n+1)&255)<<8|(pixels.get(n+2)&255));}
                try{javax.imageio.ImageIO.write(image,"png",new java.io.File("target/analysis-expanded.png"));}catch(java.io.IOException e){throw new AssertionError(e);}
                assertEquals(GL.GL_NO_ERROR,gl.glGetError());expanded.clear();
                var status=new GLLabel("Connection reset by peer");status.setSelectable(true);status.setBounds(20,450,640,30);status.setVerticalAlignment(GLLabel.VerticalAlignment.CENTER);root.addGL(status);status.select(0,10);
                frame=renderer.beginFrame(gl,680,520,Projection2D.TOP_LEFT_PIXELS.create(680,520));root.renderComponent(frame);renderer.endFrame();
                assertSame(status,renderer.readPickResult(gl,650,465).getTarget(),"Blank label area must remain selectable");
                assertEquals("Connection",status.getSelectedText());status.setSelectable(false);
                frame=renderer.beginFrame(gl,680,520,Projection2D.TOP_LEFT_PIXELS.create(680,520));root.renderComponent(frame);renderer.endFrame();
                assertNotSame(status,renderer.readPickResult(gl,650,465).getTarget());assertEquals(GL.GL_NO_ERROR,gl.glGetError());
            }finally{fonts.dispose(renderer);}
        });
    }
}
