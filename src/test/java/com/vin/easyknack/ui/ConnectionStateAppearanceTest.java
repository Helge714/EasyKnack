package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vingame.glswing.node.*;
import com.vingame.glswing.ui.GLLabel;
import com.vingame.glswing.rendering.GLComponentImageCapture;
class ConnectionStateAppearanceTest {
 @Test @EnabledIfSystemProperty(named="easyknack.test.gl",matches="true")
 void stateOutlinesAreRenderedWhileOrdinaryWiresStayBorderless()throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{
   for(var choice:BundledThemes.available())for(float curvature:new float[]{0,.5f}){
    var editor=new GLNodeEditor();editor.setGLTheme(ApplicationTheme.loadSelection(choice.id()).theme());editor.setSize(800,400);
    editor.setConnectionHandleFactor(curvature);editor.setMinimumConnectionHandleLength(0);
    var a=new GLNode("A");var b=new GLNode("B");var out=a.addOutput("out",new GLLabel("Out"));var in=b.addInput("in",new GLLabel("In"));
    out.setColor(Color.MAGENTA);in.setColor(Color.MAGENTA);editor.addNode(a,-1400,40);editor.addNode(b,1400,180);var wire=editor.connect(out,in);
    var normal=capture(editor);assertTrue(count(normal,Color.MAGENTA)>0);assertEquals(0,count(normal,editor.getSelectedConnectionBorder()));
    editor.setSelectedConnection(wire);assertTrue(count(capture(editor),editor.getSelectedConnectionBorder())>0,"Selection: "+choice);
    editor.setSelectedConnection(null);
    for(var status:new GLNodeConnectionStatus[]{GLNodeConnectionStatus.WARNING,GLNodeConnectionStatus.ERROR}){
     wire.setDiagnostic(status,"Diagnostic");var color=status==GLNodeConnectionStatus.ERROR?editor.getErrorConnectionBorder():editor.getWarningConnectionBorder();
     assertTrue(count(capture(editor),color)>0,status+": "+choice);
    }
    wire.clearDiagnostic();
    var dragged=new GLNode("Insert");editor.addNode(dragged,350,0);dragged.setSize(100,400);
    editor.setConnectionInsertionHandler(new GLNodeEditor.ConnectionInsertionHandler(){
     public boolean canInsert(GLNode node,GLNodeConnection connection){return true;}
     public boolean insert(GLNode node,GLNodeConnection connection,float x,float y){return false;}
    });
    editor.previewConnectionInsertion(dragged);assertSame(wire,editor.getInsertionCandidate());
    assertTrue(count(capture(editor),editor.getSelectedConnectionBorder())>0,"Insertion target: "+choice);
    editor.previewConnectionInsertion(null);assertEquals(0,count(capture(editor),editor.getSelectedConnectionBorder()));
   }
  });
 }
 private static BufferedImage capture(GLNodeEditor editor){return GLComponentImageCapture.capture(editor,800,400,800,400,false);}
 private static int count(BufferedImage image,Color color){
  int count=0;for(int y=0;y<image.getHeight();y++){
   var pixel=new Color(image.getRGB(200,y),true);
   if(Math.abs(pixel.getRed()-color.getRed())+Math.abs(pixel.getGreen()-color.getGreen())+Math.abs(pixel.getBlue()-color.getBlue())<12)count++;
  }return count;
 }
}
