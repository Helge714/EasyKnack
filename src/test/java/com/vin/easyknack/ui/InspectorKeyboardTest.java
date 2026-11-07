package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.event.KeyEvent;
import com.vingame.glswing.component.*;
import com.vingame.glswing.input.*;
import com.vingame.glswing.ui.*;
class InspectorKeyboardTest {
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
