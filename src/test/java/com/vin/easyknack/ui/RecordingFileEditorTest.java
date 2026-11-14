package com.vin.easyknack.ui;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import com.vingame.glswing.node.model.GLNodeValue;
class RecordingFileEditorTest {
 @Test void inlineRenameKeepsTheSelectedFolderAndUsesAConnectedGroup()throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{
   var values=new java.util.ArrayList<GLNodeValue>();
   var control=new RecordingFileEditor().create("File",values::add);
   var group=(RecordingFileEditor.Target)control.component();
   group.setGLTheme(ApplicationTheme.load(null).theme());
   var original=Path.of("target","recordings","First.wav").toAbsolutePath();control.update().accept(GLNodeValue.of(original.toString()));
   assertEquals("First.wav",group.name.getText());assertNotNull(group.browse.getIcon());
   group.name.setText("Second");group.commit();
   assertEquals(original.resolveSibling("Second.wav").toString(),values.get(0).toJavaValue());
   group.setBounds(0,0,220,24);group.refreshConnections();
   assertEquals(group.name.getX()+group.name.getWidth(),group.browse.getX(),.01);
   assertEquals(0,group.name.getConnectedShape().topRight());
   assertEquals(0,group.browse.getConnectedShape().topLeft());
   group.name.setText("../Wrong");group.commit();assertEquals(1,values.size());
  });
 }
 @Test void namesKeepTheirFormatAndCannotContainPaths(){
  assertEquals("Take.wav",RecordingFileEditor.filename("Take"));
  assertEquals("Take.MP3",RecordingFileEditor.filename("Take.MP3"));
  assertEquals("Aufnahme ä.wav",RecordingFileEditor.filename("Aufnahme ä.wav"));
  for(String name:java.util.List.of("", "..", "a/b", "a\\b", "bad:name"))
   assertThrows(IllegalArgumentException.class,()->RecordingFileEditor.filename(name));
 }
}
