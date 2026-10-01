package com.vin.easyknack.ui;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.ui.GLSlider;
class LoadedSliderRenderingTest {
 @Test @EnabledIfSystemProperty(named="easyknack.test.document",matches=".+")
 void loadedSlidersRender()throws Exception{
  try(var files=new NodeDocuments()){
   var doc=files.read(java.nio.file.Path.of(System.getProperty("easyknack.test.document"))).toCompletableFuture().get();
   javax.swing.SwingUtilities.invokeAndWait(()->{try(var c=new WorkspaceController()){
    c.openDocument("Regression",doc);c.workspace().setBounds(0,0,1200,800);
    inspect(c.workspace());
    GraphImageExport.capturePixels(c,GraphImageExport.Area.ALL,1200,800,32,false);
   }});
  }
 }
 private void inspect(GLContainer c){
  if(c instanceof GLSlider s){
   org.junit.jupiter.api.Assertions.assertTrue(s.getVisibleMaximum()>s.getVisibleMinimum(),s.getText()+" has an empty display range");
  }
  for(var child:c.getGLChildren())inspect(child);
 }
}
