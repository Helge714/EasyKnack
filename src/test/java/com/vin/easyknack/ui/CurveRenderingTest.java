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
class CurveRenderingTest {
    @Test void curveAndCompactPreviewRenderAndPick() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,680,520);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            try(var controller=new WorkspaceController()){
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,680,520);renderer=GLRenderer2D.create(gl);
                var root=new GLContainer(){@Override protected void record(GLContext2D c){}};
                fonts.attach(root);root.setBounds(0,0,680,520);root.setGLTheme(ApplicationTheme.current().theme());
                var envelope=new EnvelopeNode();var mapping=new CurveNode();
                var shape=com.vingame.glswing.ui.GLFunctionCurve.identity().insert(.5,.5).mode(0,GLFunctionCurve.Mode.BEZIER).mode(1,GLFunctionCurve.Mode.BEZIER).handle(0,false,.2,0).handle(2,true,.8,1);
                for(int i=0;i<3;i++){
                    BasicNode definition=i==0?mapping:envelope;
                    var values=new java.util.HashMap<>(definition.defaults());if(i==0)values.put(CurveNode.CURVE.value(),GLNodeValue.of(CurveData.encode(shape)));
                    var node=definition.createView(new NodeViewContext((id,key,value)->{},controller.history()),new GLGraphNode(GLNodeId.random(),definition.registration().id(),values));
                    root.addGL(node);node.setContentHidden(false);node.setBounds(20+i*180,20,node.getPreferredGLSize().width(),node.getPreferredGLSize().height());
                    if(i==1)node.setSize(node.getWidth()+30,node.getHeight()+90);
                    if(i==2){node.setContentHidden(true);node.setSize(node.getWidth(),node.getPreferredGLSize().height());}
                }
                var curve=new GLCurveEditor();curve.setCurve(shape);curve.setLarge(true);curve.setAxes(.5,.5);root.addGL(curve);curve.setBounds(390,200,270,250);
                var frame=renderer.beginFrame(gl,680,520,Projection2D.TOP_LEFT_PIXELS.create(680,520));frame.clear(new Color(26,27,31));root.renderComponent(frame);renderer.endFrame();
                assertSame(curve,renderer.readPickResult(gl,500,300).getTarget());
                var pixels=ByteBuffer.allocateDirect(680*520*4);gl.glReadPixels(0,0,680,520,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                var image=new java.awt.image.BufferedImage(680,520,java.awt.image.BufferedImage.TYPE_INT_RGB);
                int lit=0;for(int y=0;y<520;y++)for(int x=0;x<680;x++){int n=((519-y)*680+x)*4,r=pixels.get(n)&255,g=pixels.get(n+1)&255,b=pixels.get(n+2)&255;image.setRGB(x,y,r<<16|g<<8|b);if(r>150&&b>150)lit++;}
                assertTrue(lit>100,"Curve lines, controls and labels must be visible");
                try{javax.imageio.ImageIO.write(image,"png",new java.io.File("target/curve-editor-preview.png"));}catch(java.io.IOException e){throw new AssertionError(e);}
                assertEquals(GL.GL_NO_ERROR,gl.glGetError());
            }finally{fonts.dispose(renderer);}
        });
    }
}
