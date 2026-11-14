package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.input.*;
import com.vingame.glswing.rendering.GLComponentImageCapture;
class CurveControlsVisibilityTest {
 @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
 void renderedPointsAndBezierControlsDisappearAfterFocusLoss()throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{
   for(var editor:java.util.List.of(new GLCurveEditor(),new com.vin.easyknack.nodes.WavetableCanvas())){
    editor.setGLTheme(ApplicationTheme.load(null).theme());editor.setBounds(0,0,200,120);
    editor.setCurve(GLFunctionCurve.identity().insert(.5,.5).mode(0,GLFunctionCurve.Mode.BEZIER).mode(1,GLFunctionCurve.Mode.BEZIER).handle(1,true,.4,.8).handle(1,false,.6,.2));
    var plain=capture(editor);editor.mouseEntered(mouse(editor,MouseEvent.MOUSE_ENTERED));
    assertTrue(different(plain,capture(editor))>10);editor.mouseExited(mouse(editor,MouseEvent.MOUSE_EXITED));assertEquals(0,different(plain,capture(editor)));
    editor.focusGained();editor.mousePressed(mouse(editor,MouseEvent.MOUSE_PRESSED));editor.mouseReleased(mouse(editor,MouseEvent.MOUSE_RELEASED));
    editor.mouseExited(mouse(editor,MouseEvent.MOUSE_EXITED));assertTrue(different(plain,capture(editor))>10);
    editor.focusLost();assertEquals(0,different(plain,capture(editor)));
   }
  });
 }
 @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
 void envelopeUsesExactThirdsAndHasNoHoldMarker()throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{
   var node=new com.vin.easyknack.nodes.EnvelopeNode();
   var curve=(com.vingame.glswing.ui.GLCurveEditor)((com.vin.easyknack.nodes.CurveEditor)node.layout(new com.vin.easyknack.nodes.NodeState(node.defaults())).rows().stream().filter(r->r.key().equals(com.vin.easyknack.nodes.EnvelopeNode.CURVE)).findFirst().orElseThrow().editor()).create("Envelope",v->{}).component();
   curve.setGLTheme(ApplicationTheme.load(null).theme());curve.setBounds(0,0,300,120);
   node.updateEditor(com.vin.easyknack.nodes.EnvelopeNode.CURVE,curve,new com.vin.easyknack.nodes.NodeState(node.defaults()));
   assertEquals(3,curve.getGridColumns());assertEquals(1.0/3,curve.getCurve().points().get(1).x());assertEquals(2.0/3,curve.getCurve().points().get(2).x());
   var image=GLComponentImageCapture.capture(curve,300,120,600,240,false);var grid=curve.getGLTheme().values().getColor("curve.grid").getRGB();
   // Away from the curve/horizontal grid, each separator is centered exactly at a third.
   for(int x:new int[]{200,400})assertTrue(image.getRGB(x,40)==grid||image.getRGB(x-1,40)==grid);
   var marker=curve.getGLTheme().values().getColor("curve.marker").getRGB();int count=0;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if(image.getRGB(x,y)==marker)count++;
   assertEquals(0,count,"No separate Hold marker");
  });
 }
 private static GLMouseEvent mouse(GLCurveEditor editor,int type){return new GLMouseEvent(new MouseEvent(new java.awt.Canvas(),type,0,0,100,60,1,false,MouseEvent.BUTTON1),new GLPickResult(1,editor,100,60,100,60,1,0,0,1,0,0),0,0);}
 private static BufferedImage capture(GLCurveEditor editor){return GLComponentImageCapture.capture(editor,200,120,400,240,false);}
 private static int different(BufferedImage a,BufferedImage b){int changed=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getRGB(x,y)!=b.getRGB(x,y))changed++;return changed;}
}
