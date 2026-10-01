package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.jogamp.opengl.*;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.rendering.*;
import com.vingame.glswing.ui.*;
@EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
class VerticalTabsRenderingTest {
    @Test void rotatedLabelsAndEmptyNestedViewportsRender()throws Exception{
        javax.swing.SwingUtilities.invokeAndWait(()->{
            var caps=new GLCapabilities(GLProfile.get(GLProfile.GL3));caps.setOnscreen(false);
            var drawable=GLDrawableFactory.getFactory(caps.getGLProfile()).createOffscreenAutoDrawable(null,caps,null,480,440);drawable.display();drawable.getContext().makeCurrent();
            GLRenderer2D renderer=null;
            try{
                var gl=drawable.getGL().getGL3();gl.glViewport(0,0,480,440);renderer=GLRenderer2D.create(gl);
                var tabs=new GLTabbedPane();tabs.setGLTheme(ApplicationTheme.current().theme());tabs.setTabPlacement(GLTabbedPane.Placement.LEFT);tabs.setBounds(0,0,480,440);
                for(String name:java.util.List.of("Properties","Editors","Group-General","Group-IO"))tabs.addTab(name,new GLLabel(name));
                var nested=new GLScrollPane(new GLLabel("Zero-sized nested content"));nested.setBounds(0,0,0,0);
                var host=new GLContainer(){protected void record(GLContext2D c){}};host.addGL(nested);tabs.addTab("Empty",host);
                for(int index=0;index<5;index++){
                    tabs.setSelectedIndex(index);var context=renderer.beginFrame(gl,480,440,Projection2D.TOP_LEFT_PIXELS.create(480,440));context.clear(java.awt.Color.DARK_GRAY);tabs.renderComponent(context);renderer.endFrame();
                }
                var pixels=java.nio.ByteBuffer.allocateDirect(480*440*4);gl.glReadPixels(0,0,480,440,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                var image=new java.awt.image.BufferedImage(480,440,java.awt.image.BufferedImage.TYPE_INT_RGB);
                for(int y=0;y<440;y++)for(int x=0;x<480;x++){int i=((439-y)*480+x)*4;image.setRGB(x,y,(pixels.get(i)&255)<<16|(pixels.get(i+1)&255)<<8|(pixels.get(i+2)&255));}
                try{javax.imageio.ImageIO.write(image,"png",new java.io.File("target/vertical-tabs-preview.png"));}catch(java.io.IOException e){throw new RuntimeException(e);}
                assertNotNull(renderer.readPickResult(gl,8,12).getTarget());
                for(int y=5;y<325;y+=3)for(int x=4;x<25;x+=3){
                    var hit=renderer.readPickResult(gl,x,y);
                    if(hit.getTarget() instanceof GLControl tab){
                        assertEquals(x-tab.getX(),hit.getLocalX(),.05,"Tab pick X at "+x+","+y);
                        assertEquals(y-tab.getY(),hit.getLocalY(),.05,"Tab pick Y at "+x+","+y);
                    }
                }
                try(var controller=new WorkspaceController()){
                    controller.openDocument("Sidebar",GroupUnitsTest.document(false));var workspace=controller.workspace();workspace.setBounds(0,0,480,1200);workspace.setInspectorVisible(true);workspace.openComposite("test.asset");
                    for(int selected:new int[]{3,2,3,1,3,2,3}){
                        workspace.inspectorTabs().setSelectedIndex(selected);
                        var c=renderer.beginFrame(gl,480,440,Projection2D.TOP_LEFT_PIXELS.create(480,440));c.clear(java.awt.Color.DARK_GRAY);workspace.renderComponent(c);renderer.endFrame();
                        if(selected==1){var panel=GroupEditingTest.descendants(workspace).filter(EditorTemplatesPanel.class::isInstance).findFirst().orElseThrow();assertTrue(panel.getGLChildren().stream().allMatch(row->row.getX()==12 && row.getWidth()<=panel.getWidth()-24));} if(selected==3){
                            gl.glReadPixels(0,0,480,440,GL.GL_RGBA,GL.GL_UNSIGNED_BYTE,pixels);
                            for(int y=0;y<440;y++)for(int x=0;x<480;x++){int i=((439-y)*480+x)*4;image.setRGB(x,y,(pixels.get(i)&255)<<16|(pixels.get(i+1)&255)<<8|(pixels.get(i+2)&255));}
                            try{javax.imageio.ImageIO.write(image,"png",new java.io.File("target/sidebar-io-preview.png"));}catch(java.io.IOException e){throw new RuntimeException(e);}
                            var ui=workspace.interfaceEditor();
                            var settings=GroupEditingTest.descendants(ui).filter(GroupInputSettings.class::isInstance).map(GroupInputSettings.class::cast).findFirst().orElseThrow();
                            controller.editorTemplates().put(EditorTemplatesTest.frequencies());
                            for(var mode:java.util.List.of(GroupInputSettings.Kind.COMBO,GroupInputSettings.Kind.SLIDER,GroupInputSettings.Kind.COPY)){
                                settings.kind.setSelectedItem(mode);
                                var frame=renderer.beginFrame(gl,480,440,Projection2D.TOP_LEFT_PIXELS.create(480,440));workspace.renderComponent(frame);renderer.endFrame();
                                assertTrue(settings.getHeight()>=settings.getPreferredGLSize().height(),"Editor settings clipped after mode switch");
                            }
                            assertEquals(1,ui.selectedSection());
                            var list=GroupEditingTest.descendants(ui).filter(n->n instanceof GLList<?> l&&l.getModel().getSize()>0).findFirst().orElseThrow();
                            assertTrue(list.getWidth()>0&&list.getHeight()>0,"IO list bounds: "+list.getWidth()+"x"+list.getHeight());
                            for(GLContainer parent=list;parent!=null;parent=parent.getGLParent() instanceof GLContainer p?p:null){assertTrue(parent.isVisible(),parent.getClass().toString());assertTrue(parent.getWidth()>0&&parent.getHeight()>0,"Empty ancestor: "+parent.getClass());}
                        }
                    }
                }
            }finally{if(renderer!=null)renderer.dispose(drawable.getGL().getGL3());drawable.getContext().release();drawable.destroy();}
        });
    }
}






