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

@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class MidiRenderingTest {
    @Test void midiControlsAndTooltipShadowRender() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,1100,440);
            drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            try{
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,1100,440);renderer=GLRenderer2D.create(gl);
                var root=new GLContainer(){@Override protected void record(GLContext2D c){}};fonts.attach(root);root.setBounds(0,0,1100,440);root.setGLTheme(ApplicationTheme.current().theme());
                int i=0;
                for(BasicNode definition:java.util.List.of(new MidiInputNode(),new MidiNoteNode(),new MidiTransposeNode(),new MidiDelayNode(),new VoiceMergeNode())){
                    var node=definition.createView(new NodeViewContext((id,key,value)->{},new com.vingame.glswing.history.GLHistoryManager()),new GLGraphNode(GLNodeId.random(),definition.registration().id(),definition.defaults()));
                    root.addGL(node);node.packGL();node.setLocation(20+(i++)*215,20);
                    for(var control:node.getContents())if(control instanceof KeyboardMidiEditor.Keyboard keyboard)keyboard.focusGained();
                }
                var type=Class.forName("com.vingame.glswing.ui.GLToolTipManager$Tip");var constructor=type.getDeclaredConstructor();constructor.setAccessible(true);
                var tip=(GLContainer)constructor.newInstance();root.addGL(tip);
                var text=type.getDeclaredMethod("setText",String.class,Color.class,float.class);text.setAccessible(true);
                text.invoke(tip,"Gate: 0 = released, 1 = held (including sustain pedal).\nConnect to Envelope Gate.",null,400f);tip.packGL();tip.setLocation(380,290);
                var frame=renderer.beginFrame(gl,1100,440,Projection2D.TOP_LEFT_PIXELS.create(1100,440));frame.clear(new Color(45,46,50));root.renderComponent(frame);renderer.endFrame();
                var pixels=ByteBuffer.allocateDirect(1100*440*4);gl.glReadPixels(0,0,1100,440,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                var image=new java.awt.image.BufferedImage(1100,440,java.awt.image.BufferedImage.TYPE_INT_RGB);
                for(int y=0;y<440;y++)for(int x=0;x<1100;x++){int n=((439-y)*1100+x)*4;image.setRGB(x,y,(pixels.get(n)&255)<<16|(pixels.get(n+1)&255)<<8|(pixels.get(n+2)&255));}
                int shadowY=(int)(tip.getY()+tip.getHeight()+3),shadowX=(int)(tip.getX()+tip.getWidth()/2);
                assertTrue((image.getRGB(shadowX,shadowY)&255)<50,"Tooltip shadow must extend below its bounds");
                javax.imageio.ImageIO.write(image,"png",new java.io.File("target/midi-controls-preview.png"));assertEquals(GL.GL_NO_ERROR,gl.glGetError());
            }catch(Exception e){throw new AssertionError(e);}finally{fonts.dispose(renderer);}
        });
    }
}
