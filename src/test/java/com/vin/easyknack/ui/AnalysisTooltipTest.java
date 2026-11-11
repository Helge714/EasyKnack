package com.vin.easyknack.ui;

import com.vin.easyknack.nodes.AnalysisEditor;
import com.vin.easyknack.audio.AudioAnalysis;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.input.*;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;
import com.jogamp.opengl.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

class AnalysisTooltipTest {
    private static GLMouseEvent mouse(AnalysisEditor.Plot p,int type){
        var e=new MouseEvent(new java.awt.Canvas(),type,0,0,195,95,0,false,0);
        return new GLMouseEvent(e,new GLPickResult(1,p,195,95,195,95,1,0,0,1,0,0),0,0);
    }
    @Test void hoverReadoutUpdatesWithoutRenderingAndClearsOnExit()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            try{
                var p=new AnalysisEditor.Plot(true,true);p.setBounds(0,0,200,100);p.bind("hover-test");
                AudioAnalysis.publish(Map.of("hover-test",new AudioAnalysis.Frame(0,0,24000,new float[33])));
                p.mouseMoved(mouse(p,MouseEvent.MOUSE_MOVED));assertTrue(p.getToolTipText().startsWith("Bin "));
                var values=new float[33];java.util.Arrays.fill(values,.5f);
                AudioAnalysis.publish(Map.of("hover-test",new AudioAnalysis.Frame(0,0,24000,values)));
                assertTrue(p.getToolTipText().endsWith("y = 0.5"));
                p.mouseExited(mouse(p,MouseEvent.MOUSE_EXITED));assertFalse(p.getToolTipText().startsWith("Bin "));
            }finally{AudioAnalysis.clear();}
        });
    }
    @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
    void shownTooltipDoesNotInvalidateFramesOrBlockUnderlyingPicking()throws Exception{
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,680,520);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            var p=new AnalysisEditor.Plot(true,true);
            try{
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,680,520);renderer=GLRenderer2D.create(gl);
                var root=new GLContainer(){protected void record(GLContext2D c){}};fonts.attach(root);root.setBounds(0,0,680,520);
                var button=new GLButton("Underlying button");root.addGL(button);button.setBounds(0,0,680,520);
                root.addGL(p);p.setBounds(0,0,200,100);p.bind("tooltip-gpu");
                AudioAnalysis.publish(Map.of("tooltip-gpu",new AudioAnalysis.Frame(0,0,24000,new float[33])));
                p.mouseEntered(mouse(p,MouseEvent.MOUSE_ENTERED));p.mouseMoved(mouse(p,MouseEvent.MOUSE_MOVED));
                var sessions=GLToolTipManager.class.getDeclaredField("SESSIONS");sessions.setAccessible(true);
                var session=((Map<?,?>)sessions.get(null)).get(fonts.surface());assertNotNull(session);
                var show=session.getClass().getDeclaredMethod("show");show.setAccessible(true);show.invoke(session);
                var tip=fonts.surface().getGLChildren().get(1);
                for(int i=0;i<3;i++){
                    var frame=renderer.beginFrame(gl,680,520,Projection2D.TOP_LEFT_PIXELS.create(680,520));fonts.render(frame);renderer.endFrame();
                }
                assertFalse(root.isGLLayoutDirty(),"Rendering must settle instead of invalidating layout continuously");
                assertSame(button,renderer.readPickResult(gl,(int)(tip.getX()+tip.getWidth()/2),(int)(tip.getY()+tip.getHeight()/2)).getTarget());
                p.mouseExited(mouse(p,MouseEvent.MOUSE_EXITED));assertEquals(2,root.getGLChildren().size());
                assertEquals(1,fonts.surface().getGLChildren().size());
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            finally{GLToolTipManager.leave(p);AudioAnalysis.clear();fonts.dispose(renderer);}
        });
    }
}
