package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.nio.ByteBuffer;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vin.easyknack.nodes.*;
import com.vingame.glswing.component.*;
import com.vingame.glswing.node.composite.*;
import com.vingame.glswing.node.model.*;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;

@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class GroupRenderingTest {
    static java.util.List<GLContainer> descendants(GLContainer c){var result=new java.util.ArrayList<GLContainer>();for(var child:c.getGLChildren()){result.add(child);result.addAll(descendants(child));}return result;}
    @Test void groupSettingsAndRightAlignedOutputsRender()throws Exception{
        SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
            controller.openDocument("Group",GroupUnitsTest.document(false));var group=GroupUnitsTest.group(controller);
            controller.workspace().openComposite("test.asset");var ui=controller.workspace().interfaceEditor();ui.setGLTheme(ApplicationTheme.current().theme());
            descendants(ui).stream().filter(n->n instanceof GLList<?> list&&list.getModel().getSize()>0&&list.getModel().getElementAt(0) instanceof GLCompositePortMapping m&&m.publicPort().direction()==GLPortDirection.INPUT).map(n->(GLList<?>)n).findFirst().orElseThrow().setSelectedIndex(0);
            assertNotNull(ui.selectedPort());
            var settings=ui.getGLChildren().stream().filter(GroupSettings.class::isInstance).map(GroupSettings.class::cast).findFirst().orElseThrow();
            var khz=descendants(settings).stream().filter(n->n instanceof GLCheckBox b&&b.getText().equals("Kilohertz")).map(GLCheckBox.class::cast).findFirst().orElseThrow();
            khz.doClick();assertFalse(((GLNodeValue.ListValue)ui.selectedPort().presentation().editorOptions().get(GroupUnits.ALLOWED)).values().contains(GLNodeValue.of("khz")));controller.history().undo();ui.refresh();
            var profile=GLProfile.get(GLProfile.GL3);var caps=new GLCapabilities(profile);caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null,caps,null,700,1250);drawable.display();var context=drawable.getContext();context.makeCurrent();GLRenderer2D renderer=null;var fonts=new OffscreenFonts(drawable);
            try{
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,700,1250);renderer=GLRenderer2D.create(gl);
                fonts.attach(group,ui);ui.selectSection(1);ui.setBounds(300,10,380,ui.getPreferredGLSize().height());ui.setBounds(300,10,380,ui.getPreferredGLSize().height());
                group.setGLTheme(ApplicationTheme.current().theme());group.packGL();group.setLocation(40,30);
                var mix=new MixNode();var view=mix.createView(new NodeViewContext((id,k,v)->{},controller.history()),new GLGraphNode(GLNodeId.random(),mix.registration().id(),mix.defaults()));fonts.attach(view);view.setGLTheme(ApplicationTheme.current().theme());view.packGL();view.setLocation(40,230);
                var frame=renderer.beginFrame(gl,700,1250,Projection2D.TOP_LEFT_PIXELS.create(700,1250));frame.clear(new Color(45,46,50));group.renderComponent(frame);view.renderComponent(frame);ui.renderComponent(frame);renderer.endFrame();
                var pixels=ByteBuffer.allocateDirect(700*1250*4);gl.glReadPixels(0,0,700,1250,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);var image=new java.awt.image.BufferedImage(700,1250,java.awt.image.BufferedImage.TYPE_INT_RGB);
                for(int y=0;y<1250;y++)for(int x=0;x<700;x++){int n=((1249-y)*700+x)*4;image.setRGB(x,y,(pixels.get(n)&255)<<16|(pixels.get(n+1)&255)<<8|(pixels.get(n+2)&255));}
                javax.imageio.ImageIO.write(image,"png",new java.io.File("target/group-settings-preview.png"));assertEquals(GL.GL_NO_ERROR,gl.glGetError());assertTrue(settings.getHeight()>0);assertTrue(settings.getY()+settings.getHeight()<=ui.getPreferredGLSize().height());
            }catch(Exception e){throw new AssertionError(e);}finally{fonts.dispose(renderer);}
        }});
    }
}
