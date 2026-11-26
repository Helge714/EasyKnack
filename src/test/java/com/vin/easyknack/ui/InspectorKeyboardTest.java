package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.event.KeyEvent;
import com.vingame.glswing.component.*;
import com.vingame.glswing.input.*;
import com.vingame.glswing.ui.*;
class InspectorKeyboardTest {
 @Test void resizeKeepsCameraCenterWhileInspectorToggleKeepsScreenPositions()throws Exception{
  SwingUtilities.invokeAndWait(()->{try(var controller=new WorkspaceController()){
   controller.openDocument("Camera",StarterDocument.create());var workspace=controller.workspace();
   workspace.setInspectorVisible(false);workspace.setBounds(0,0,1000,700);var editor=workspace.editor();
   var graph=workspace.currentGraph().snapshot();
   for(float zoom:new float[]{.5f,1,2}){
    editor.setZoom(zoom);editor.setPan(73,-42);
    float x=editor.getWidth()*.5f-editor.getPanX()/zoom,y=editor.getHeight()*.5f-editor.getPanY()/zoom;
    workspace.setBounds(0,0,workspace.getWidth()==1000?1400:1000,workspace.getHeight()==700?900:700);
    assertEquals(x,editor.getWidth()*.5f-editor.getPanX()/zoom,.001);
    assertEquals(y,editor.getHeight()*.5f-editor.getPanY()/zoom,.001);
    float screenX=editor.getWidth()*.5f*(1-zoom)+editor.getPanX();
    float screenY=editor.getHeight()*.5f*(1-zoom)+editor.getPanY();float width=editor.getWidth();
    workspace.setInspectorVisible(true);assertTrue(editor.getWidth()<width);
    assertEquals(screenX,editor.getWidth()*.5f*(1-zoom)+editor.getPanX(),.001);
    assertEquals(screenY,editor.getHeight()*.5f*(1-zoom)+editor.getPanY(),.001);
    workspace.setInspectorVisible(false);
   }
   assertEquals(graph,workspace.currentGraph().snapshot());
  }});
 }
 @Test void windowFallbackWorksWithoutFocusOnBothSurfaceTypes()throws Exception{
  SwingUtilities.invokeAndWait(()->{
   for(GLParent host:java.util.List.of(new GLSwingPanel(){},new GLSwingContainerCanvas(){})){
    var frame=new JFrame();WorkspaceView view=null;
    try{
     view=new WorkspaceView(frame);host.addGL(view);
     var workspace=(com.vingame.glswing.node.GLNodeWorkspace)field(view,"workspace");
     var surface=(java.awt.Component)host;surface.setSize(900,600);view.setBounds(0,0,900,600);
     host.requestGLFocus(null);workspace.setInspectorVisible(false);
     send(surface,KeyEvent.VK_N);assertTrue(workspace.isInspectorVisible(),host.getClass().getSuperclass().getSimpleName());
     var outside=new GLButton("Other area");host.addGL(outside);host.requestGLFocus(outside);
     send(surface,KeyEvent.VK_N);assertFalse(workspace.isInspectorVisible());
     var text=new GLTextEditor("Name");host.addGL(text);host.requestGLFocus(text);
     send(surface,KeyEvent.VK_N);assertFalse(workspace.isInspectorVisible());
     var popup=new GLPopupMenu();popup.addItem("Test",e->{});popup.show(host,5,5);
     send(surface,KeyEvent.VK_N);assertFalse(workspace.isInspectorVisible());
     send(surface,KeyEvent.VK_ESCAPE);assertFalse(popup.isShowing());
     host.requestGLFocus(null);send(surface,KeyEvent.VK_N);assertTrue(workspace.isInspectorVisible());
    }catch(Exception failure){throw new AssertionError(failure);}finally{
     if(view!=null)view.close();frame.dispose();
     if(host instanceof GLSwingPanel panel)panel.destroy();else ((GLSwingContainerCanvas)host).destroy();
    }
   }
  });
 }
 private static void send(java.awt.Component surface,int code){
  var event=new KeyEvent(surface,KeyEvent.KEY_PRESSED,0,0,code,(char)code);
  for(var listener:surface.getKeyListeners())listener.keyPressed(event);
 }
 private static Object field(Object object,String name)throws Exception{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
 private static GLKeyEvent key(int code){return new GLKeyEvent(new KeyEvent(new java.awt.Canvas(),KeyEvent.KEY_PRESSED,0,0,code,(char)code));}
 private static void dispatch(GLKeyTarget target,GLKeyEvent event)throws Exception{
  var type=Class.forName("com.vingame.glswing.component.GLContainerInputDispatcher");var method=type.getDeclaredMethod("dispatchKeyPressed",GLKeyTarget.class,GLKeyEvent.class);method.setAccessible(true);method.invoke(null,target,event);
 }
 @Test void shortcutsWorkFromInspectorControlsButNotDuringTextInput()throws Exception{
  SwingUtilities.invokeAndWait(()->{
   var frame=new JFrame();var host=new GLSwingPanel(){};WorkspaceView view=null;
   try{
    view=new WorkspaceView(frame);host.addGL(view);var workspace=(com.vingame.glswing.node.GLNodeWorkspace)field(view,"workspace");
    host.setSize(900,600);view.setBounds(0,0,900,600);
    host.requestGLFocus(workspace.editor());
    var menuBar=(GLMenuBar)field(view,"menuBar");var menuItem=menuBar.getItems().get(0);
    var press=GLSwingPanel.class.getDeclaredMethod("dispatchMousePressed",java.awt.event.MouseEvent.class,GLPickResult.class);press.setAccessible(true);
    press.invoke(host,new java.awt.event.MouseEvent(host,java.awt.event.MouseEvent.MOUSE_PRESSED,0,0,5,5,1,false,java.awt.event.MouseEvent.BUTTON1),
      new GLPickResult(1,menuItem,5,5,5,5,1,0,0,1,0,0));
    menuItem.doClick();
    menuBar.closeMenu();
    assertSame(workspace.editor(),host.getGLFocusOwner(),"Closing a menu must restore the graph keyboard target");
    workspace.setInspectorVisible(true);var afterMenu=key(KeyEvent.VK_N);dispatch(host.getGLFocusOwner(),afterMenu);
    assertTrue(afterMenu.isConsumed());assertFalse(workspace.isInspectorVisible());
    workspace.setInspectorVisible(true);var button=new GLButton("Test");workspace.propertyInspector().addGL(button);host.requestGLFocus(button);
    var n=key(KeyEvent.VK_N);dispatch(button,n);assertTrue(n.isConsumed());assertFalse(workspace.isInspectorVisible());
    workspace.setInspectorVisible(true);host.requestGLFocus(button);var escape=key(KeyEvent.VK_ESCAPE);dispatch(button,escape);assertTrue(escape.isConsumed());assertFalse(workspace.isInspectorVisible());
    workspace.setInspectorVisible(true);var text=new GLTextEditor("Name");workspace.propertyInspector().addGL(text);host.requestGLFocus(text);
    dispatch(text,key(KeyEvent.VK_N));assertTrue(workspace.isInspectorVisible());
    var unchanged=key(KeyEvent.VK_ESCAPE);dispatch(text,unchanged);assertFalse(workspace.isInspectorVisible());
   }catch(Exception failure){throw new AssertionError(failure);}finally{if(view!=null)view.close();frame.dispose();host.destroy();}
  });
 }
}
